package quina.jdbc.console;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.QuinaConfig;
import quina.annotation.cdi.ServiceScoped;
import quina.exception.QuinaException;
import quina.http.Request;
import quina.http.Response;
import quina.util.SeabassCipher;
import quina.util.Xor128;
import quina.util.collection.IndexKeyValueList;

/**
 * JDBC-Consoleサービス.
 */
@ServiceScoped
public class JDBCConsoleService {
	
	// ログインキー長.
	private static final int LOGIN_KEY_LENGTH = 48;
	
	// シグニチャー用拡張キー(quinaConsole).
	private static final String SignatureSrc = "9u!Na@c0n8O1e";
	
	// Read-Writeロックオブジェクト.
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	// ログイン管理リスト.
	private IndexKeyValueList<String, Long> loginInfo =
		new IndexKeyValueList<String, Long>();
	
	// ランダムオブジェクト(ThreadLocal).
	private final ThreadLocal<Xor128> randLocal = new ThreadLocal<Xor128>();
	
	/**
	 * コンストラクタ.
	 */
	public JDBCConsoleService() {}
	
	// QuinaServiceを取得.
	private static final QuinaJDBCConsoleService service() {
		QuinaJDBCConsoleService ret = QuinaJDBCConsoleService.getService();
		if(ret == null) {
			throw new QuinaException("Failed to get QuinaJDBCConsoleService.");
		}
		return ret;
	}
	
	// タイムアウト値を取得.
	private final long getTimeout() {
		Long timeout = service().getConfig().getLong("loginTimeout");
		// タイムアウトが存在しない場合はデフォルト値.
		if(timeout == null) {
			timeout = QuinaJDBCConsoleConstants.DEF_LOGIN_TIMEOUT;
		// タイムアウトが最小時間の場合.
		} else if(timeout <= QuinaJDBCConsoleConstants.MIN_LOGIN_TIMEOUT) {
			timeout = QuinaJDBCConsoleConstants.MIN_LOGIN_TIMEOUT;
		// タイムアウトが最大時間の場合.
		} else if(timeout >= QuinaJDBCConsoleConstants.MAX_LOGIN_TIMEOUT) {
			timeout = QuinaJDBCConsoleConstants.MAX_LOGIN_TIMEOUT;
		}
		return timeout;
	}
	
	// ログイン状態がタイムアウトしたかチェック.
	private final void loginTimeoutCheck(long timeout) {
		if(timeout < 0) {
			timeout = getTimeout();
		}
		long now = System.currentTimeMillis();
		lock.writeLock().lock();
		try {
			final int len = loginInfo.size();
			for(int i = len - 1; i >= 0; i --) {
				// タイムアウトしている場合.
				if(loginInfo.valueAt(i) + timeout < now) {
					// ログイン情報を削除.
					loginInfo.remove(loginInfo.keyAt(i));
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	// 文字列コード.
	private static final String STRCODE =
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ+abcdefghijklmnopqrstuvwxyz/0123456789";
	
	// 4バイトのLoginKeyCodeを取得
	private static final byte getLoginKey4byteCode(int value, int no) {
		switch(no) {
		case 0: return (byte)STRCODE.charAt((value & 0x003f));
		case 1: return (byte)STRCODE.charAt((value & 0x003f00) >> 8);
		case 2: return (byte)STRCODE.charAt((value & 0x003f0000) >> 16);
		}
		// case 3:
		return (byte)STRCODE.charAt((value & 0x3f000000) >> 24);
	}
	
	// ランダムオブジェクトを取得.
	private final Xor128 getRand() {
		Xor128 ret = randLocal.get();
		if(ret == null) {
			ret = new Xor128(System.nanoTime());
			randLocal.set(ret);
		}
		return ret;
	}
	
	// ログインKey情報を取得.
	private final byte[] getLoginKey(Xor128 rand, int size) {
		size = size >> 2 + ( (size & 3) != 0 ? 1 : 0);
		int value;
		int cnt = 0;
		final byte[] ret = new byte[size];
		final int len = size >> 2;
		for(int i = 0; i < len; i ++) {
			value = rand.nextInt();
			// １回の情報をセット.
			ret[cnt ++] = getLoginKey4byteCode(value, 0);
			ret[cnt ++] = getLoginKey4byteCode(value, 1);
			ret[cnt ++] = getLoginKey4byteCode(value, 2);
			ret[cnt ++] = getLoginKey4byteCode(value, 3);
		}
		return ret;
	}
	
	// ログインKey情報に現在時刻をセットして取得.
	private final byte[] getLoginKeyAddTime(byte[] loginKey, long now) {
		final int len = loginKey.length;
		byte[] ret = new byte[len + 8];
		int cnt = 0;
		for(int i = 0; i < len; i ++) {
			ret[cnt ++ ] = loginKey[i];
		}
		// 時間をセット.
		ret[cnt ++] = (byte)(now & 0x00ffL);
		ret[cnt ++] = (byte)((now & 0x00ff00L) >> 8L);
		ret[cnt ++] = (byte)((now & 0x00ff0000L) >> 16L);
		ret[cnt ++] = (byte)((now & 0x00ff000000L) >> 24L);
		ret[cnt ++] = (byte)((now & 0x00ff00000000L) >> 32L);
		ret[cnt ++] = (byte)((now & 0x00ff0000000000L) >> 40L);
		ret[cnt ++] = (byte)((now & 0x00ff000000000000L) >> 48L);
		ret[cnt ++] = (byte)((now & 0xff00000000000000L) >> 56L);
		return ret;
	}
	
	// ログイン認証トークンが返却されます.
	private final String getAuthLoginToken(
		Xor128 rand, long now, byte[] binarySignature, String loginKey) {
		// 返却コードを取得.
		return SeabassCipher.binaryEncode(rand, getLoginKeyAddTime(
			loginKey.getBytes(), now), binarySignature);
	}
	
	/**
	 * ユーザー名とパスワードが正しいかチェック.
	 * @param user ユーザー名を設定します.
	 * @param password パスワードを設定します.
	 * @return boolean trueの場合パスワード認証が出来ました.
	 */
	public boolean isAuthLogin(String user, String password) {
		final QuinaConfig config = service().getConfig();
		String confUser = config.getString("user");
		String confPassword = config.getString("password");
		if(confUser == null || confUser.isEmpty()) {
			confUser = "";
		}
		if(confPassword == null || confPassword.isEmpty()) {
			confPassword = "";
		}
		return confUser.equals(user) && confPassword.equals(password);
	}
	
	/**
	 * 対象のログイン認証トークンが正しいかチェック.
	 * @param req 対象のHttpRequestを設定します.
	 * @param res 対象のHttpResponseを設定します.
	 * @return boolean trueの場合正しいログイン情報です.
	 */
	public boolean isLoginToken(Request req, Response<?> res) {
		// Requestヘッダからシグニチャーを取得.
		String segnature = req.getHeader().get(
			QuinaJDBCConsoleConstants.LOGIN_SIGNETUER_KEY);
		// Requestヘッダからログイン認証トークンを取得.
		String token = req.getHeader().get(
			QuinaJDBCConsoleConstants.LOGIN_AUTH_TOKEN);
		// ログイン認証.
		String[] out = new String[1];
		boolean ret = isLoginToken(out, segnature, token);
		// 正しく認証された場合.
		if(ret) {
			// 返却された新しいログイン認証トークンを
			// Responseヘッダに登録.
			res.getHeader().put(
				QuinaJDBCConsoleConstants.LOGIN_AUTH_TOKEN,
				out[0]);
		}
		return ret;
	}
	
	/**
	 * 対象のログイン認証トークンが正しいかチェック.
	 * @param out 新しいログイン認証トークンが返却されます.
	 *            この内容を処理結果のHTTPResponseのヘッダに設定して返却します.
	 * @param signature シグニチャーを設定します.
	 * @param token 今回認証チェックするログイン認証トークンを設定します.
	 * @return boolean trueの場合正しいログイン情報です.
	 */
	public boolean isLoginToken(
		String[] out, String signature, String token) {
		if(signature == null || signature.isEmpty()) {
			throw new QuinaException("No signature has been set.");
		} else if(token == null || token.isEmpty()) {
			throw new QuinaException("login token is not set.");
		}
		if(out != null && out.length > 0) {
			out[0] = null;
		}
		// タイムアウト値を取得.
		final long timeout = getTimeout();
		// 既存ログイン情報のチェック及び削除.
		loginTimeoutCheck(timeout);
		// 指定のシグニチャーをSeabassCipher向けのシグニチャー変換.
		byte[] binarySignature = SeabassCipher.createSignature(
			signature, SignatureSrc);
		// デコード変換.
		byte[] bin = null;
		try {
			bin = SeabassCipher.binaryDecode(
				token, binarySignature);
		} catch(Exception e) {
			// 解析に失敗した場合は[false]返却.
			return false;
		}
		// 現在時刻を取得.
		long now = System.currentTimeMillis();
		// 後半８バイトが前回のアクセス時間.
		int p = bin.length - 8;
		long time = (bin[p] & 0x00ffL) |
			((bin[p + 1] & 0x00ffL) << 8L) |
			((bin[p + 2] & 0x00ffL) << 16L) |
			((bin[p + 3] & 0x00ffL) << 24L) |
			((bin[p + 4] & 0x00ffL) << 32L) |
			((bin[p + 5] & 0x00ffL) << 40L) |
			((bin[p + 6] & 0x00ffL) << 48L) |
			((bin[p + 7] & 0x00ffL) << 56L);
		// タイムアウト値を超えてる場合.
		if(time + timeout < now) {
			return false;
		}
		// キーコードを取得.
		final String loginKey = new String(
			bin, 0, bin.length - 8);
		// 対象のキー情報が存在するかチェック.
		boolean ret;
		lock.readLock().lock();
		try {
			ret = loginInfo.containsKey(loginKey);
		} finally {
			lock.readLock().unlock();
		}
		// ログイン情報が存在する場合.
		if(ret) {
			lock.writeLock().lock();
			try {
				// ログインキーコードを更新.
				loginInfo.put(
					loginKey, now);
			} finally {
				lock.writeLock().unlock();
			}
			// 認証が成功した場合のみ、
			// 新しいログイン認証トークンを生成してoutにセット.
			if(out != null && out.length > 0) {
				out[0] = getAuthLoginToken(
					getRand(), now, binarySignature, loginKey);
			}
		}
		return ret;
	}
	
	/**
	 * ログイン認証トークンを作成.
	 * @param signature シグニチャーを設定します.
	 * @return String ログイン認証トークンが返却されます.
	 */
	public String createLoginToken(String signature) {
		return createLoginToken(signature, LOGIN_KEY_LENGTH);
	}
	
	/**
	 * 新しいログイン認証トークンを作成.
	 * @param signature シグニチャーを設定します.
	 * @param keyLen ログインキー長を設定します.
	 * @return String ログイン認証トークンが返却されます.
	 */
	public String createLoginToken(String signature, int keyLen) {
		if(signature == null || signature.isEmpty()) {
			throw new QuinaException(
				"No login signature has been set.");
		} else if(keyLen < 16) {
			keyLen = 16;
		} else if(keyLen > 256) {
			keyLen = 256;
		}
		// ログインタイムアウトチェック.
		loginTimeoutCheck(-1);
		// ランダムオブジェクト.
		Xor128 rand = getRand();
		// ログインキーを生成.
		final String loginKey = new String(
			getLoginKey(rand, keyLen));
		// 現在時刻を取得.
		final long now = System.currentTimeMillis();
		// 新しいログイン情報を登録.
		lock.writeLock().lock();
		try {
			// ログインキーをセット.
			loginInfo.put(loginKey, now);
		} finally {
			lock.writeLock().unlock();
		}
		// ログインキーから、新しいログイン認証トークンを
		// 生成して返却.
		return getAuthLoginToken(
			rand, now, SeabassCipher.createSignature(
				signature, SignatureSrc),
			loginKey);
	}

}
