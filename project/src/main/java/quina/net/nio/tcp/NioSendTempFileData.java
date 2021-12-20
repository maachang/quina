package quina.net.nio.tcp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import quina.util.AtomicNumber;

/**
 * Nioテンポラリ送信ファイルデータ.
 *
 * 指定されたReadStream系(InputStream, ReadableByteChannel, Reader)
 * を設定して、送信対象の一時ファイルを作成します。
 *
 * また作成された一時ファイルは「処理完了後に削除」されます.
 */
public class NioSendTempFileData extends AbstractNioSendData {
	/**
	 * ランダムファイル名拡張子.
	 */
	public static final String NAME_EXTENSION = ".tmp";

	// 先頭ランダムファイル名
	private static final String NAME_HEAD = "swsnd-";

	/** ディレクトリ名. **/
	private String directoryName = null;

	/** ファイル名. **/
	private String fileName = null;

	/** フルパス名. **/
	private String fullPath = null;

	/** 書き込み専用ファイルチャネル. **/
	private FileChannel channel = null;

	/** オブジェクトコピーカウント. **/
	protected AtomicNumber copyCount = new AtomicNumber(1);

	/**
	 * コンストラクタ.
	 * @param in 送信元のオブジェクトを設定します.
	 *           オブジェクトは[InputStream],[Reader],[ReadableByteChannel]
	 *           である必要があります.
	 * @exception IOException I/O例外.
	 */
	public NioSendTempFileData(Object in)
		throws IOException {
		init(in, NioConstants.getTcpBodyFileDir(), NioRand.get(),
			NioConstants.getTempFileNameLength());
	}

	/**
	 * コンストラクタ.
	 * @param in 送信元のオブジェクトを設定します.
	 *           オブジェクトは[InputStream],[Reader],[ReadableByteChannel]
	 *           である必要があります.
	 * @param baseDir データを格納するディレクトリ名を設定します.
	 * @param rand テンポラリファイル名を生成するランダムオブジェクトを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioSendTempFileData(Object in, String baseDir)
		throws IOException {
		init(in, baseDir, NioRand.get(), NioConstants.getTempFileNameLength());
	}

	/**
	 * コンストラクタ.
	 * @param in 送信元のオブジェクトを設定します.
	 *           オブジェクトは[InputStream],[Reader],[ReadableByteChannel]
	 *           である必要があります.
	 * @param baseDir データを格納するディレクトリ名を設定します.
	 * @param rand テンポラリファイル名を生成するランダムオブジェクトを設定します.
	 * @param randNameLength テンポラリファイル名を生成する長さを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioSendTempFileData(Object in, String baseDir, int randNameLength)
		throws IOException {
		init(in, baseDir, NioRand.get(), randNameLength);
	}

	/**
	 * コンストラクタ.
	 * @param in 送信元のオブジェクトを設定します.
	 *           オブジェクトは[InputStream],[Reader],[ReadableByteChannel]
	 *           である必要があります.
	 * @param baseDir データを格納するディレクトリ名を設定します.
	 * @param rand テンポラリファイル名を生成するランダムオブジェクトを設定します.
	 * @param randNameLength テンポラリファイル名を生成する長さを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioSendTempFileData(Object in, String baseDir, NioRand rand,
		int randNameLength)
		throws IOException {
		init(in, baseDir, rand, randNameLength);
	}

	/**
	 * コンストラクタ.
	 * テンポラリファイルなどを別途作成した場合に呼び出します.
	 *
	 * @param fileName 対象のファイル名を設定します.
	 * @param length 対象のデータ長を設定します.
	 * @exception IOException I/O例外.
	 */
	public NioSendTempFileData(String fileName, long length)
		throws IOException {
		this(null, NioConstants.getTcpBodyFileDir(), fileName, length);
	}

	/**
	 * コンストラクタ.
	 * テンポラリファイルなどを別途作成した場合に呼び出します.
	 *
	 * @param directoryName 対象のディレクトリ名を設定します.
	 * @param fileName 対象のファイル名を設定します.
	 * @param length 対象のデータ長を設定します.
	 * @exception IOException I/O例外.
	 */
	public NioSendTempFileData(String directoryName, String fileName, long length)
		throws IOException {
		this(null, directoryName, fileName, length);
	}

	/**
	 * コンストラクタ.
	 * 元のオブジェクトをコピーする場合はこの処理を呼び出します.
	 *
	 * @param copyCount 対象のコピーカウントを設定します.
	 * @param header 対象のヘッダを設定します.
	 * @param directoryName 対象のディレクトリ名を設定します.
	 * @param fileName 対象のファイル名を設定します.
	 * @param length 対象のデータ長を設定します.
	 * @exception IOException I/O例外.
	 */
	private NioSendTempFileData(AtomicNumber copyCount, String directoryName,
		String fileName, long length)
		throws IOException {
		String fpath = directoryName + fileName + NAME_EXTENSION;
		FileChannel ch = openChannel(fpath);
		this.copyCount = copyCount == null ? new AtomicNumber(1) : copyCount;
		this.directoryName = directoryName;
		this.fileName = fileName;
		this.fullPath = fpath;
		this.length = length;
		this.position = 0L;
		this.closeFlag = false;
		this.channel = ch;

		// コピーカウントを１インクリメント.
		if(copyCount != null) {
			this.copyCount.inc();
		}
	}

	/**
	 * テンポラリファイルの作成.
	 * @param out 作成されたテンポラリファイル名（拡張子なし）が返却されます.
	 * @return OutputStream 書き込み先のテンポラリファイルが返却されます.
	 * @exception IOException I/O例外.
	 */
	public static final OutputStream createTempFile(String[] out)
		throws IOException {
		return createTempFile(out, NioConstants.getTcpBodyFileDir(),
			NioRand.get(), NioConstants.getTempFileNameLength());
	}

	/**
	 * テンポラリファイルの作成.
	 * @param out 作成されたテンポラリファイル名（拡張子なし）が返却されます.
	 * @param baseDir データを格納するディレクトリ名を設定します.
	 * @return OutputStream 書き込み先のテンポラリファイルが返却されます.
	 * @exception IOException I/O例外.
	 */
	public static final OutputStream createTempFile(String[] out, String baseDir)
		throws IOException {
		return createTempFile(out, baseDir, NioRand.get(),
			NioConstants.getTempFileNameLength());
	}

	/**
	 * テンポラリファイルの作成.
	 * @param out 作成されたテンポラリファイル名（拡張子なし）が返却されます.
	 * @param baseDir データを格納するディレクトリ名を設定します.
	 * @param randNameLength テンポラリファイル名を生成する長さを設定します.
	 * @return OutputStream 書き込み先のテンポラリファイルが返却されます.
	 * @exception IOException I/O例外.
	 */
	public static final OutputStream createTempFile(
		String[] out, String baseDir, int randNameLength)
		throws IOException {
		return createTempFile(out, baseDir, NioRand.get(), randNameLength);
	}

	/**
	 * テンポラリファイルの作成.
	 * @param out 作成されたテンポラリファイル名（拡張子なし）が返却されます.
	 * @param baseDir データを格納するディレクトリ名を設定します.
	 * @param rand テンポラリファイル名を生成するランダムオブジェクトを設定します.
	 * @param randNameLength テンポラリファイル名を生成する長さを設定します.
	 * @return OutputStream 書き込み先のテンポラリファイルが返却されます.
	 * @exception IOException I/O例外.
	 */
	public static final OutputStream createTempFile(
		String[] out, String baseDir, NioRand rand, int randNameLength)
		throws IOException {
		// テンポラリ名の長さをチェック.
		if(randNameLength < NioConstants.MIN_TMP_NAME_COUNT) {
			randNameLength = NioConstants.MIN_TMP_NAME_COUNT;
		}
		// ディレクトリ名の最後にセパレータが無い場合はセット.
		if (!baseDir.endsWith(File.pathSeparator)) {
			baseDir += File.pathSeparator;
		}
		// 指定ディレクトリに一意のファイルを作成.
		String name = NioUtil.createRandomFile(
			rand, baseDir, NAME_HEAD, NAME_EXTENSION, randNameLength);
		out[0] = name;
		return new BufferedOutputStream(
			new FileOutputStream(baseDir + name + NAME_EXTENSION));
	}

	/**
	 * 初期処理.
	 * @param in 送信元のオブジェクトを設定します.
	 *           オブジェクトは[InputStream],[Reader],[ReadableByteChannel]
	 *           である必要があります.
	 * @param baseDir データを格納するディレクトリ名を設定します.
	 * @param rand テンポラリファイル名を生成するランダムオブジェクトを設定します.
	 * @param randNameLength テンポラリファイル名を生成する長さを設定します.
	 * @exception IOException I/O例外.
	 */
	private final void init(Object in, String baseDir, NioRand rand, int randNameLength)
		throws IOException {
		int inType = in == null ? -1 :
			(in instanceof InputStream ? 1 : (in instanceof ReadableByteChannel ? 2 :
				(in instanceof Reader ? 3 : -1)));
		if(inType == -1) {
			throw new IOException(
				"The source object is not 'InputStream' or " +
				"'ReadableByteChannel' or 'Reader'.");
		}
		// テンポラリ名の長さをチェック.
		if(randNameLength < NioConstants.MIN_TMP_NAME_COUNT) {
			randNameLength = NioConstants.MIN_TMP_NAME_COUNT;
		}
		// ディレクトリ名の最後にセパレータが無い場合はセット.
		if (!baseDir.endsWith(File.pathSeparator)) {
			baseDir += File.pathSeparator;
		}
		try {
			// 指定ディレクトリに一意のファイルを作成.
			String name = NioUtil.createRandomFile(
				rand, baseDir, NAME_HEAD, NAME_EXTENSION, randNameLength);
			// フルパス取得.
			String fpath = baseDir + name + NAME_EXTENSION;
			// InputStreamを当該ファイルに書き込む.
			long len = outFile(inType, fpath, in);
			// 対象の書き込み用ファイルチャネルを作成.
			// メンバー変数に設定.
			this.directoryName = baseDir;
			this.fileName = name;
			this.fullPath = fpath;
			this.length = len;
			this.position = 0L;
			this.closeFlag = false;
			this.channel = openChannel(fpath);
		} catch (IOException io) {
			throw io;
		} catch (Exception e) {
			throw new NioException(e);
		}
	}

	/** チャネルをオープンして読み込み可能にする. **/
	private final FileChannel openChannel(String fpath) throws IOException {
		return FileChannel.open(
			Paths.get(fpath),
			//StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE);
			StandardOpenOption.READ);
	}


	// inの内容を送信データとしてテンポラリファイルに出力.
	private static final long outFile(int inType, String fpath, Object in)
		throws IOException {
		long ret = 0L;
		// InputStreamの場合.
		if(inType == 1) {
			InputStream input = (InputStream)in;
			OutputStream out = null;
			try {
				int len;
				byte[] buf = new byte[1024];
				out = new BufferedOutputStream(new FileOutputStream(fpath));
				while((len = input.read(buf)) != -1) {
					out.write(buf, 0, len);
					ret += (long)len;
				}
				out.flush(); out.close(); out = null;
				input.close(); input = null;
				return ret;
			} finally {
				if(out != null) {
					try {
						out.close();
					} catch(Exception e) {}
				}
				if(input != null) {
					try {
						input.close();
					} catch(Exception e) {}
				}
			}
		// FileChannelの場合.
		} else if(inType == 2) {
			ReadableByteChannel input = (ReadableByteChannel)in;
			FileChannel out = null;
			try {
				ByteBuffer buf = ByteBuffer.allocateDirect(1024);
				out = FileChannel.open(Paths.get(fpath)
					, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
				buf.clear();
				while(input.read(buf) != -1) {
					buf.flip();
					ret += (long)out.write(buf);
					buf.clear();
				}
				out.close(); out = null;
				input.close(); input = null;
				return ret;
			} finally {
				if(out != null) {
					try {
						out.close();
					} catch(Exception e) {}
				}
				if(input != null) {
					try {
						input.close();
					} catch(Exception e) {}
				}
			}
		// Readerの場合.
		// Readerの文字列からのバイナリ変換はUTF8のみ.
		} else if(inType == 3) {
			Reader reader = (Reader)in;
			OutputStream out = null;
			try {
				int len, bLen;
				char[] buf = new char[1024];
				byte[] bin = new byte[buf.length << 2];
				out = new BufferedOutputStream(new FileOutputStream(fpath));
				while((len = reader.read(buf)) != -1) {
					bLen = utf8ToBinary(bin, 0, buf, 0, len);
					out.write(bin, 0, bLen);
					ret += (long)bLen;
				}
				out.flush(); out.close(); out = null;
				reader.close(); reader = null;
				return ret;
			} finally {
				if(out != null) {
					try {
						out.close();
					} catch(Exception e) {}
				}
				if(reader != null) {
					try {
						reader.close();
					} catch(Exception e) {}
				}
			}
		}
		return -1L;
	}

	/**
	 * UTF8文字列(char[])をバイナリ変換.
	 */
	private static final int utf8ToBinary(final byte[] out, final int oOff,
		final char[] value, final int off, final int len) {
		if (value == null || len == 0) {
			return 0;
		}
		int c;
		int o = oOff;
		int ret = 0;
		try {
			for (int i = 0; i < len; i++) {
				c = (int) value[off + i];
				// サロゲートペア処理.
				if (c >= 0xd800 && c <= 0xdbff) {
					c = 0x10000 + (((c - 0xd800) << 10) | ((int) value[off + i + 1] - 0xdc00));
					i ++;
				}
				if ((c & 0xffffff80) == 0) {
					out[o ++] = (byte)c;
				} else if (c < 0x800) {
					out[o ++] = (byte) ((c >> 6) | 0xc0);
					out[o ++] = (byte) ((c & 0x3f) | 0x80);
				} else if (c < 0x10000) {
					out[o ++] = (byte) ((c >> 12) | 0xe0);
					out[o ++] = (byte) (((c >> 6) & 0x3f) | 0x80);
					out[o ++] = (byte) ((c & 0x3f) | 0x80);
				} else {
					out[o ++] = (byte) ((c >> 18) | 0xf0);
					out[o ++] = (byte) (((c >> 12) & 0x3f) | 0x80);
					out[o ++] = (byte) (((c >> 6) & 0x3f) | 0x80);
					out[o ++] = (byte) ((c & 0x3f) | 0x80);
				}
			}
			ret = o - oOff;
		} catch(Exception e) {
		}
		return ret;
	}

	/**
	 * クローズ処理.
	 * @exception IOException
	 */
	@Override
	public void close() throws IOException {
		super.close();
		if(channel != null) {
			FileChannel cl = channel;
			channel = null;
			try {
				cl.close();
			} catch(Exception e) {
			}
			// copyCountを１デクリメント.
			if(copyCount.dec() <= 0) {
				// コピーオブジェクトを含めた物全てがクローズ
				// された場合は、一時ファイルを削除する.
				deleteFile();
			}
		}
	}

	// ファイル削除処理.
	private final void deleteFile() {
		// 本来は DELETE_ON_CLOSE を利用するが
		// この送信処理系は「複数送信先」が想定されるので
		// 手動で削除するように変更.
		for(int i = 0; i < NioConstants.DELETE_RETRY; i ++) {
			try {
				// クローズ後削除処理.
				if(Files.deleteIfExists(Paths.get(fullPath))) {
					return;
				}
			} catch(Exception e) {}
			try {
				Thread.sleep(5L);
			} catch(Exception e) {}
		}
	}

	/**
	 * 処理前チェック.
	 */
	@Override
	protected void check() {
		super.check();
	}

	/**
	 * NioSendDataをコピー.
	 * この処理は「複数先に同じものを送信したい場合」に利用します.
	 * @return NioSendData コピーされたNioSendDataが返却されます.
	 */
	@Override
	public NioSendData copy() {
		check();
		try {
			return new NioSendTempFileData(
				copyCount, directoryName, fileName, length);
		} catch(Exception e) {
			throw new NioException(e);
		}
	}

	/**
	 * データ取得.
	 * @param buf 対象のByteBufferを設定します.
	 * @return int 読み込まれたデータ数が返却されます.
	 *             -1 の場合EOFに達しました.
	 * @exception IOException I/O例外.
	 */
	@Override
	public int read(ByteBuffer buf) throws IOException {
		check();
		if(!buf.hasRemaining()) {
			return 0;
		}
		// ファイル内容を読み込む.
		final int readLen = channel.read(buf);
		// EOF.
		if(readLen <= 0) {
			return -1;
		}
		position += (long)readLen;
		return (int)readLen;
	}

	/**
	 * ディレクトリ名を取得.
	 * @return String ディレクトリ名が返却されます.
	 */
	public String getDirectoryName() {
		check();
		return directoryName;
	}

	/**
	 * ファイル名を取得.
	 * @return String ファイル名が返却されます.
	 */
	public String getFileName() {
		check();
		return fileName + NAME_EXTENSION;
	}

	/**
	 * フルパスを取得.
	 * @return String フルパスが返却されます.
	 */
	public String getFullpath() {
		check();
		return fullPath;
	}

	@Override
	public String toString() {
		check();
		return new StringBuilder("[TempFileBody]")
			.append(" directoryName: \"").append(directoryName).append("\"")
			.append(", fileName: \"").append(fileName).append(NAME_EXTENSION).append("\"")
			.append(", fullPath: \"").append(fullPath).append("\"")
			.append(", position: ").append(position)
			.append(", length: ").append(length)
			.toString();
	}
}
