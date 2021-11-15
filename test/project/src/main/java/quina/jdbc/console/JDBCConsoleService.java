package quina.jdbc.console;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.QuinaConfig;
import quina.annotation.cdi.ServiceScoped;
import quina.exception.QuinaException;
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
		// タイムアウトが存在しない場合は１５分タイムアウト.
		if(timeout == null) {
			timeout = 900000L;
		// タイムアウトが５分以下の場合５分タイムアウト.
		} else if(timeout <= 300000L) {
			timeout = 300000L;
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
	
	// ログイン認証コードが返却されます.
	private final String getAuthLoginCode(
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
	 * 対象のログイン情報の内容が正しいかチェック.
	 * @param out 新しいログイン認証コードが返却されます.
	 *            この内容を処理結果のHTTPResponseのヘッダに設定して返却します.
	 * @param signature シグニチャーを設定します.
	 * @param value ログイン情報を設定します.
	 * @return boolean trueの場合正しいログイン情報です.
	 */
	public boolean isLoginValue(String[] out, String signature, String value) {
		if(signature == null || signature.isEmpty()) {
			throw new QuinaException("No signature has been set.");
		} else if(value == null || value.isEmpty()) {
			throw new QuinaException("login value is not set.");
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
			bin = SeabassCipher.binaryDecode(value, binarySignature);
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
		final String loginKey = new String(bin, 0, bin.length - 8);
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
			// 新しい認証コードを生成してoutにセット.
			if(out != null && out.length > 0) {
				out[0] = getAuthLoginCode(
					getRand(), now, binarySignature, loginKey);
			}
		}
		return ret;
	}
	
	/**
	 * ログイン情報を作成.
	 * @param signature シグニチャーを設定します.
	 * @return String ログイン結果の情報が返却されます.
	 */
	public String createLoginValue(String signature) {
		return createLoginValue(signature, LOGIN_KEY_LENGTH);
	}
	
	/**
	 * 新しいログイン認証コードを作成.
	 * @param signature シグニチャーを設定します.
	 * @param keyLen ログインキー長を設定します.
	 * @return String ログイン認証コードが返却されます.
	 */
	public String createLoginValue(String signature, int keyLen) {
		if(signature == null || signature.isEmpty()) {
			throw new QuinaException("No login signature has been set.");
		} else if(keyLen < 16) {
			keyLen = 16;
		} else if(keyLen > 256) {
			keyLen = 256;
		}
		// ログインタイムアウトチェック.
		loginTimeoutCheck(-1);
		// ランダムオブジェクト.
		Xor128 rand = getRand();
		// キーコードを取得.
		final String loginKey = new String(
			getLoginKey(rand, keyLen));
		// 現在時刻を取得.
		final long now = System.currentTimeMillis();
		// 新しいログイン情報を登録.
		lock.writeLock().lock();
		try {
			// 新しい情報をセット.
			loginInfo.put(loginKey, now);
		} finally {
			lock.writeLock().unlock();
		}
		// ログイン認証コードを返却.
		return getAuthLoginCode(
			rand, now, SeabassCipher.createSignature(
				signature, SignatureSrc),
			loginKey);
	}

}
