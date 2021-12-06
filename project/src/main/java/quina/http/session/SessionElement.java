package quina.http.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import quina.util.BinaryIO;
import quina.util.Flag;
import quina.worker.timeout.TimeoutElement;

/**
 * セッション要素.
 */
public class SessionElement extends SessionStorage
	implements TimeoutElement {
	
	// セッションタイプ名.
	private String sessionType;
	
	// セッションValue.
	private String sessionValue;
	
	// タイムアウト監視登録フラグ.
	private final Flag regTimeFlag = new Flag(false);
	
	// セッションタイムアウト.
	private long sessionTimeout = -1L;
	
	/**
	 * コンストラクタ.
	 */
	protected SessionElement() {
		// 空の情報を生成.
		// Serializableから情報生成する場合に利用.
	}
	
	/**
	 * コンストラクタ.
	 * @param sessionValue セッションValueを設定します.
	 */
	protected SessionElement(String sessionValue) {
		this(null, sessionValue);
	}
	
	/**
	 * コンストラクタ.
	 * @param sessionType セッションタイプ名を設定します.
	 * @param sessionValue セッションValueを設定します.
	 */
	protected SessionElement(
		String sessionType, String sessionValue) {
		if(sessionType == null ||
			(sessionType = sessionType.trim()).isEmpty()) {
			sessionType = HttpSessionConstants.NORMAL_SESSION_TYPE;
		}
		this.sessionType = sessionType;
		this.sessionValue = sessionValue;
		this.sessionTimeout = System.currentTimeMillis();
	}
	
	/**
	 * タイムアウト監視登録.
	 * @return boolean trueの場合既に登録してます.
	 */
	@Override
	public boolean regTimeout() {
		return regTimeFlag.setToGetBefore(true);
	}
	
	/**
	 * アクセス時間を取得.
	 * @return long アクセス時間が返却されます.
	 */
	@Override
	public long getTime() {
		return sessionTimeout;
	}
	
	/**
	 * アクセス時間を更新.
	 */
	protected void updateTime() {
		sessionTimeout = System.currentTimeMillis();
	}
	
	/**
	 * セッションタイプを取得.
	 * @return セッションタイプが返却されます.
	 */
	public String getSessionType() {
		return sessionType;
	}
	
	/**
	 * セッションValueを取得.
	 * @return String セッションValueが返却されます.
	 */
	public String getSessionValue() {
		return sessionValue;
	}
	
	/**
	 * セッションの読み込み.
	 * @param in 読み込み元のInputStreamを設定します.
	 * @throws IOException I/O例外.
	 */
	protected void load(InputStream in)
		throws IOException {
		final byte[] tmp = BinaryIO.createTmp();
		// sessionType.
		this.sessionType = BinaryIO.readString(in, tmp);
		// sessionValue.
		this.sessionValue = BinaryIO.readString(in, tmp);
		//sessionTimeout.
		this.sessionTimeout = BinaryIO.readLong(in, tmp);
		// storage読み込み.
		super.load(in);
	}
	
	/**
	 * セッションの保存.
	 * @param out 保存先のOutputStreamを設定します.
	 * @throws IOException I/O例外.
	 */
	protected void save(OutputStream out)
		throws IOException {
		final byte[] tmp = BinaryIO.createTmp();
		// sessionType.
		BinaryIO.writeString(out, tmp, sessionType);
		// sessionValue.
		BinaryIO.writeString(out, tmp, sessionValue);
		//sessionTimeout.
		BinaryIO.writeLong(out, tmp, sessionTimeout);
		// storage書き込み.
		super.save(out);
	}
}
