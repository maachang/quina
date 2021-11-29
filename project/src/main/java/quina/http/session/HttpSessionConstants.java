package quina.http.session;

import quina.net.nio.tcp.NioAtomicValues.Number32;
import quina.net.nio.tcp.NioAtomicValues.Number64;
import quina.util.AtomicObject;

/**
 * HttpSession定数定義.
 */
public final class HttpSessionConstants {
	private HttpSessionConstants() {}
	
	// デフォルトタイムアウト.
	// 30分.
	private static final long DEF_SESSION_TIMEOUT = 30L * 60L * 1000L;

	// 最小タイムアウト.
	// 5分.
	private static final long MIN_SESSION_TIMEOUT = 5L * 60L * 1000L;
	
	// 最大タイムアウト.
	// 1週間.
	private static final long MAX_SESSION_TIMEOUT = 7L * 24 * 60L * 60L * 1000L;
	
	// セッションタイムアウト.
	private static final Number64 sessionTimeout =
		new Number64(DEF_SESSION_TIMEOUT);
	
	/**
	 * セッションタイムアウト値を設定.
	 * @param timeout セッションタイムアウト値を設定します.
	 */
	public static final void setSessionTimeout(long timeout) {
		if(timeout < MIN_SESSION_TIMEOUT) {
			timeout = MIN_SESSION_TIMEOUT;
		} else if(timeout > MAX_SESSION_TIMEOUT) {
			timeout = MAX_SESSION_TIMEOUT;
		}
		sessionTimeout.set(timeout);
	}
	
	/**
	 * セッションタイムアウト値を取得.
	 * @return long セッションタイムアウト値が返却されます.
	 */
	public static final long getSessionTimeout() {
		return sessionTimeout.get();
	}
	
	// デフォルトセッションキー長.
	// 48文字.
	private static final int DEF_SESSION_KEY_LENGTH = 48;
	
	// 最小セッションキー長.
	// 16文字.
	private static final int MIN_SESSION_KEY_LENGTH = 16;
	
	
	// 最小セッションキー長.
	// 256文字.
	private static final int MAX_SESSION_KEY_LENGTH = 256;
	
	// セッションキー長.
	private static final Number32 sessionKeyLength =
		new Number32(DEF_SESSION_KEY_LENGTH);
	
	/**
	 * セッションキー長を設定.
	 * @param len セッションキー長を設定します.
	 */
	public static final void setSessionKeyLength(int len) {
		// ４の倍数に合わせる.
		len = ((len >> 2) << 2) | ((len & 0x03) != 0 ? 4 : 0);
		if(len < MIN_SESSION_KEY_LENGTH) {
			len = MIN_SESSION_KEY_LENGTH;
		} else if(len > MAX_SESSION_KEY_LENGTH) {
			len = MAX_SESSION_KEY_LENGTH;
		}
		sessionKeyLength.set(len);
	}
	
	/**
	 * セッションキー長を取得.
	 * @return int セッションキー長が返却されます.
	 */
	public static final int getSessionKeyLength() {
		return sessionKeyLength.get();
	}
	
	// デフォルトのセッション保存先ファイル名.
	private static final String DEF_SAVE_SESSION_FILE_NAME = "./.qssn";
	
	// セッション保存先ファイル名.
	private static final AtomicObject<String> saveSessionFileName =
		new AtomicObject<String>(DEF_SAVE_SESSION_FILE_NAME);
	
	/**
	 * セッション保存先ファイル名を設定.
	 * @param name セッション保存先ファイル名を設定します.
	 */
	public static final void setSaveSessionFileName(String name) {
		if(name == null || (name = name.trim()).isEmpty()) {
			name = DEF_SAVE_SESSION_FILE_NAME;
		}
		saveSessionFileName.set(name);
	}
	
	/**
	 * セッション保存先ファイル名を取得.
	 * @return String セッション保存先ファイル名が返却されます.
	 */
	public static final String getSaveSessionFileName() {
		return saveSessionFileName.get();
	}
}
