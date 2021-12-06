package quina.http.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.QuinaConfig;
import quina.QuinaService;
import quina.exception.QuinaException;
import quina.util.BinaryIO;
import quina.util.Xor128;
import quina.util.collection.TypesClass;

/**
 * HttpSessionサービス.
 */
public class HttpSessionService implements QuinaService {
	
	// QuinaConfig.
	private QuinaConfig config = new QuinaConfig(
		"httpSession"
		// セッションタイムアウト(30分).
		,"loginTimeout", TypesClass.Long, HttpSessionConstants.getSessionTimeout()
		// シグニチャキー長.
		,"signatureLength", TypesClass.Integer, HttpSessionConstants.getSignatureLength()
		// セッションキー長.
		,"keyLength", TypesClass.Integer, HttpSessionConstants.getSessionKeyLength()
		// HttpSessionService終了時に現在のセッション情報を保存先ファイル名.
		,"saveSession", TypesClass.String, HttpSessionConstants.getSaveSessionFileName()
	);
	
	// ログイン管理リスト.
	// key = シグニチャ.
	// value セッション要素.
	private Map<String, SessionElement> sessions;
	
	// Read-Writeロックオブジェクト.
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	// ランダムオブジェクト(ThreadLocal).
	private final ThreadLocal<Xor128> randLocal = new ThreadLocal<Xor128>();

	@Override
	public QuinaConfig getConfig() {
		return config;
	}

	@Override
	public boolean isStartService() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public void startService() {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void stopService() {
		// TODO 自動生成されたメソッド・スタブ
		
	}
	
	/**
	 * 全体のセッションの読み込み.
	 * @param in 読み込み元のInputStreamを設定します.
	 * @throws IOException I/O例外.
	 */
	protected void loadSession(InputStream in)
		throws IOException {
		final byte[] tmp = BinaryIO.createTmp();
		int len = HttpSessionConstants
			.SESSION_SERIALIZABLE_HEADER.length;
		in.read(tmp, 0, len);
		for(int i = 0; i < len; i ++) {
			if(HttpSessionConstants
				.SESSION_SERIALIZABLE_HEADER[i] != tmp[i]) {
				throw new QuinaException(
					"Not serializable QuinaSession information.");
			}
		}
		// 登録セッション数を取得.
		len = BinaryIO.readSavingInt(in, tmp);
		// セッション情報が存在しない場合は処理しない.
		if(len == 0) {
			this.sessions =
				new ConcurrentHashMap<String, SessionElement>();
			return;
		}
		// セッション情報を登録.
		String key;
		SessionElement value;
		Map<String, SessionElement> kvs =
			new ConcurrentHashMap<String, SessionElement>();
		for(int i = 0; i < len; i ++) {
			// シグニチャー情報をセット.
			key = BinaryIO.readString(in, tmp);
			value = new SessionElement();
			value.load(in);
			kvs.put(key, value);
			key = null; value = null;
		}
		this.sessions = kvs;
	}
	
	/**
	 * 全体のセッションの保存.
	 * @param kvs 保存セッション情報を設定します.
	 * @param out 保存先のOutputStreamを設定します.
	 * @throws IOException I/O例外.
	 */
	protected static final void saveSession(
		Map<String, SessionElement> kvs, OutputStream out)
		throws IOException {
		final byte[] tmp = BinaryIO.createTmp();
		// ヘッダ情報を保存.
		out.write(HttpSessionConstants.SESSION_SERIALIZABLE_HEADER);
		// 登録セッション数を取得.
		BinaryIO.writeSavingBinary(out, tmp, kvs.size());
		// 各セッション情報を登録.
		Entry<String, SessionElement> e;
		Iterator<Entry<String, SessionElement>> it =
			kvs.entrySet().iterator();
		while(it.hasNext()) {
			e = it.next();
			// シグニチャー情報を登録.
			BinaryIO.writeString(out, tmp, e.getKey());
			// value情報の登録.
			e.getValue().save(out);
		}
	}

}
