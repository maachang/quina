package quina.component.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import quina.exception.CoreException;
import quina.exception.QuinaException;
import quina.http.HttpStatus;
import quina.http.Request;
import quina.http.Response;
import quina.util.Base64;
import quina.util.CRC64;
import quina.util.FileUtil;
import quina.util.NamedLock;

/**
 * ファイルコンテンツに対するETAG管理.
 */
public final class EtagManager {

	//
	// FileComponentなどで利用するキャッシュ機構を
	// サポートします.
	//

	/**
	 * ETAGが有効かチェック.
	 */
	private boolean doneUseEtagFlag;

	/**
	 * ETAGとして計算可能な最大ファイルサイズ.
	 */
	private int doneMaxFileSize;

	/**
	 * Etag管理定義情報.
	 */
	private EtagManagerInfo info;

	/**
	 * ローカルファイルフルパスに対するETAG管理.
	 * Key: ローカルファイルのフルパスが設定される.
	 * Value: EtagElementが設定される.
	 */
	private Map<String, EtagElement> manager = new
		ConcurrentHashMap<String, EtagElement>();

	/**
	 * メタファイルI/O時のロック処理.
	 */
	private NamedLock sync;

	/**
	 * コンストラクタ
	 */
	public EtagManager() {
		this(true, -1);
	}

	/**
	 * コンストラクタ.
	 * @param lockSize NamedLockの数を設定します.
	 */
	public EtagManager(int lockSize) {
		this(true, lockSize);
	}

	/**
	 * コンストラクタ.
	 * @param inForceFlag Etag管理の有効・無効を設定します.
	 * @param lockSize NamedLockの数を設定します.
	 */
	public EtagManager(boolean inForceFlag, int lockSize) {
		info = new EtagManagerInfo(this);
		info.setInForce(inForceFlag);
		info.setPathLockSize(lockSize);
	}

	/**
	 * 管理しているETAG情報を全てクリア.
	 */
	public void clear() {
		manager.clear();
	}

	/**
	 * Etag管理定義情報を取得.
	 * @return EtagManagerInfo Etag管理定義情報が返却されます.
	 */
	public EtagManagerInfo getInfo() {
		return info;
	}

	/**
	 * 設定を確定します.
	 * この処理を行わない限りEtagを取得出来ません.
	 */
	protected void done() {
		if(info.setDone(true)) {
			throw new QuinaException("The settings have already been completed.");
		}
		// 確定処理を行う.
		doneUseEtagFlag = info.isInForce();
		doneMaxFileSize = info.getMaxFileSize();
		sync = new NamedLock(info.getPathLockSize());
	}

	/**
	 * Etagに対するレスポンス設定.
	 * @param path 対象のコンテンツ名を設定します.
	 * @param req 対象のリクエストを設定します.
	 * @param res 対象のレスポンスを設定します.
	 * @return boolean trueの場合、この情報は接続元のキャッシュと同様です.
	 */
	protected boolean setResponse(String path, Request req, Response<?> res) {
		// ここでは２つの処理を実施します.
		//
		// 1) Requestに設定されているEtagと比較する.
		// 2) (1)でEtagが存在しない、比較が一致しない場合はEtagの付与をResponseに行い返却する.
		// 3) (1)でRequestのEtagが一致した場合はキャッシュが有効をResponseに設定して返却する.
		
		// ただしres.isCacheMode() == false の場合は処理しない.
		if(!res.isCacheMode()) {
			return false;
		}

		// このローカルパスのEtagを取得.
		final String etag = get(path);
		// Etagが取得できた場合.
		if(etag != null) {
			// リクエストにキャッシュされたEtag情報が設定されている場合.
			final String reqEtag = req.getHeader().getString("If-None-Match");
			// サーバー側のEtagと一致する場合.
			if(reqEtag != null && etag.equals(reqEtag)) {
				// 一致する場合はキャッシュデータとしてレスポンス設定して返却する.
				res.setStatus(HttpStatus.NotModified);
				// キャッシュ一致を通知.
				return true;
			}
			// サーバー側のEtagと一致しない場合はレスポンスにEtagをセット.
			res.getHeader().put("Etag", etag);
		}
		// キャッシュが一致しないことを通知.
		return false;
	}

	/**
	 * 指定パス名のEtagを取得.
	 * @param path 対象のコンテンツ名を設定します.
	 * @return String Etag情報が返却されます.
	 *                [null]の場合、取得出来ませんでした.
	 */
	public String get(String path) {
		// etagの生成を行わない場合、または設定の確定が行われてない場合.
		if(!doneUseEtagFlag || !info.isDone()) {
			return null;
		}
		// マネージャに登録されているEtag要素を取得.
		EtagElement e = manager.get(path);
		// マネージャに存在しない場合.
		// もしくは取得したmetaファイルの情報とコンテンツ
		// ファイルタイムが一致しない場合.
		if(e == null || e.getLastTime() != mtime(path)) {
			// 存在しない場合は生成.
			if((e = createEtag(path)) == null) {
				return null;
			}
			// 存在する場合はマネージャ登録.
			manager.put(path, e);
		}
		// 対象のetagを返却.
		return e.getEtag();
	}

	// 指定ファイルからEtagを作成.
	private final EtagElement createEtag(String path) {
		// メタファイル名を取得.
		final String meta = createEtagMetaFileName(path);
		// コンテンツのファイルサイズ.
		final long fileLength;
		// ロックオブジェクト.
		Lock lock;
		try {
			// コンテンツのファイルサイズを取得.
			fileLength = FileUtil.getFileLength(path);
			// ファイルサイズが -1L(ファイルが存在しない) か
			// 最大ファイルサイズ以上の場合はEtagで処理しない.
			if(fileLength == -1L || fileLength >= doneMaxFileSize) {
				// ファイルが存在しない場合はメタファイルも削除する.
				if(FileUtil.isFile(meta)) {
					// パス名単位でロックする.
					(lock = sync.get(path)).lock();
					try {
						FileUtil.removeFile(meta);
					} finally {
						lock.unlock();
					}
				}
				return null;
			}
		} catch(Exception ex) {
			if(ex instanceof CoreException) {
				throw (CoreException)ex;
			}
			throw new QuinaException(ex);
		}

		// パス名単位でロックする.
		(lock = sync.get(path)).lock();
		try {
			EtagElement em = null;
			// メタファイルからEtagElementを取得.
			if((em = loadEtagElement(path, meta)) != null) {
				return em;
			}
			// メタファイルが存在しないか古い場合は新たにメタファイルを再作成する.
			// コンテンツのの更新時間を取得.
			final long lastTime = FileUtil.mtime(path);
			// コンテンツからCRCコードを取得.
			final long code = getContentByCrc64(path, fileLength);
			// Etagを生成して保存.
			final byte[] etagBin = createEtagCode(fileLength, code);
			saveEtagElement(meta, lastTime, etagBin);
			// EtagElementを生成.
			return new EtagElement(convertEtag(etagBin, 0), lastTime);
		} catch(Exception ex) {
			if(ex instanceof CoreException) {
				throw (CoreException)ex;
			}
			throw new QuinaException(ex);
		} finally {
			lock.unlock();
		}
	}

	// メタファイルの拡張子.
	private static final String META_EXTENSION = ".quina";

	// メタファイル名を取得.
	private static final String createEtagMetaFileName(String path) {
		int p = path.lastIndexOf("/");
		if(p == -1) {
			return "." + path + META_EXTENSION;
		}
		return path.substring(0, p) + "/." + path.substring(p + 1) + META_EXTENSION;
	}

	// longからBinaryに変換.
	private static final byte[] longByBinary(int off, byte[] b, long value) {
		b[off] = (byte)((value & 0xff00000000000000L) >> 56L);
		b[off+1] = (byte)((value & 0x00ff000000000000L) >> 48L);
		b[off+2] = (byte)((value & 0x0000ff0000000000L) >> 40L);
		b[off+3] = (byte)((value & 0x000000ff00000000L) >> 32L);
		b[off+4] = (byte)((value & 0x00000000ff000000L) >> 24L);
		b[off+5] = (byte)((value & 0x0000000000ff0000L) >> 16L);
		b[off+6] = (byte)((value & 0x000000000000ff00L) >> 8L);
		b[off+7] = (byte)((value & 0x00000000000000ffL) >> 0L);
		return b;
	}

	// binaryからLongに変換.
	private static final long binaryByLong(int off, byte[] b) {
		return (((long)b[off] & 0x0ff) << 56L) |
			(((long)b[off+1] & 0x0ff) << 48L) |
			(((long)b[off+2] & 0x0ff) << 40L) |
			(((long)b[off+3] & 0x0ff) << 32L) |
			(((long)b[off+4] & 0x0ff) << 24L) |
			(((long)b[off+5] & 0x0ff) << 16L) |
			(((long)b[off+6] & 0x0ff) << 8L) |
			(((long)b[off+7] & 0x0ff) << 0L);
	}

	// ファイルサイズとCRC64でETAGのバイナリを作成.
	private static final byte[] createEtagCode(long len, long crc64) {
		byte[] b = new byte[16];
		longByBinary(0, b, crc64);
		longByBinary(8, b, len);
		// ファイルサイズだと[AAAAAAAAADg==]になるので、なりづらいように変換.
		for(int i = 0; i < 8; i ++) {
			b[8 + i] = (byte)(b[i] ^ b[8 + i]);
		}
		return b;
	}

	// 指定ファイルからCrc64コードを取得.
	private static final long getContentByCrc64(String path, long fileSize)
		throws Exception {
		// CRC64でEtagコードを生成.
		long ret = -1L;
		InputStream in = null;
		try {
			int len;
			final byte[] b = new byte[1024];
			final CRC64 crc64 = new CRC64();
			// ファイルバイナリデータからCRC64を作成.
			in = new BufferedInputStream(new FileInputStream(path));
			while((len = in.read(b)) != -1) {
				crc64.update(b, 0, len);
			}
			in.close();
			in = null;
			// コンテンツのファイルサイズをCRC64にセット.
			longByBinary(0, b, fileSize);
			crc64.update(b, 0, 8);
			// CRC64を取得.
			ret = crc64.getValue();
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch(Exception e) {}
			}
		}
		return ret;
	}

	// バイナリのetagを文字列に変換.
	private static final String convertEtag(byte[] b, int off) {
		// base64に変換して最後の"="を除去する.
		final String ret = Base64.encode(b, off, 16);
		return ret.substring(0, ret.length() - 2);
		/*
		String ret = Base64.encode(b, off, 16);
		int len = ret.length();
		for(int i = len - 1; i >= 0; i --) {
			if(ret.charAt(i) != '=') {
				if(i + 1 == len) {
					return ret;
				}
				return ret.substring(0, i + 1);
			}
		}
		return ret;
		*/
	}

	// metaファイルからEtag要素をロード.
	private static final EtagElement loadEtagElement(String path, String meta)
		throws Exception {
		// 最初にメタファイルが存在するかチェック.
		if(FileUtil.isFile(meta)) {
			// 存在する場合は、そこからEtagElementを取得.
			final byte[] b = new byte[24];
			InputStream in = null;
			try {
				// etag管理のメタファイルを読み込む.
				in = new BufferedInputStream(new FileInputStream(meta));
				final int len = in.read(b);
				in.close();
				in = null;
				// etag管理のメタファイルサイズの場合.
				if(len == 24) {
					// メタファイル内に格納されている、コンテンツファイル時間を取得.
					long lastTime = binaryByLong(0, b);
					// 現在のコンテンツ情報とメタファイル内に登録されているコンテンツ時間を比較.
					if(FileUtil.mtime(path) == lastTime) {
						// 同じ場合はEtag要素を返却
						return new EtagElement(convertEtag(b, 8), lastTime);
					}
				}
			} finally {
				if(in != null) {
					try {
						in.close();
						in = null;
					} catch(Exception e) {}
				}
			}
		}
		return null;
	}

	// metaデータを保存.
	private static final void saveEtagElement(String meta, long lastTime, byte[] etagBin)
		throws Exception {
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(meta));
			// 最終更新時間.
			out.write(longByBinary(0, new byte[8], lastTime));
			// etagのバイナリ情報.
			out.write(etagBin);
			out.flush();
			out.close();
			out = null;
		} finally {
			if(out != null) {
				try {
					out.close();
				} catch(Exception e) {}
			}
		}
	}

	// ファイル最終更新時間を取得.
	private static final long mtime(String path) {
		try {
			return FileUtil.mtime(path);
		} catch(Exception e) {
			return -1L;
		}
	}

	/**
	 * 1つのETAG要素.
	 */
	private static final class EtagElement {
		// ETAG情報.
		private final String etag;
		// コンテンツのファイルタイム.
		private final long lastTime;

		/**
		 * コンストラクタ.
		 * @param etag
		 * @param lastTime
		 */
		public EtagElement(String etag, long lastTime) {
			this.etag = etag;
			this.lastTime = lastTime;
		}

		/**
		 * Etagを取得.
		 * @return
		 */
		public String getEtag() {
			return etag;
		}

		/**
		 * コンテンツのファイル更新時間を取得.
		 * @return
		 */
		public long getLastTime() {
			return lastTime;
		}
	}
}
