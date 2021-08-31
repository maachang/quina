package quina.component;

import java.util.Map;

import quina.exception.QuinaException;
import quina.util.AtomicNumber;
import quina.util.BooleanUtil;
import quina.util.Flag;
import quina.util.NumberUtil;

/**
 * Etag管理定義情報.
 */
public class EtagManagerInfo {
	/**
	 * ETAGとして計算可能なデフォルトの最大ファイルサイズ.
	 * (5MByte).
	 */
	private static final int DEFAULT_MAX_FILE_SIZE = 0x00100000 * 5;

	/**
	 * ETAGマネージャの設定が完了したかチェック.
	 */
	private final Flag doneFlag = new Flag(false);

	/**
	 * ETAGが有効かチェック.
	 */
	private final Flag inForce = new Flag(true);

	/**
	 * ETAGとして計算可能な最大ファイルサイズ.
	 */
	private final AtomicNumber maxFileSize = new AtomicNumber(DEFAULT_MAX_FILE_SIZE);

	/**
	 * パス名でロック管理するサイズ.
	 */
	private final AtomicNumber pathLockSize = new AtomicNumber(-1);

	/**
	 * Etagマネージャ.
	 */
	private EtagManager manager;

	/**
	 * コンストラクタ.
	 * @param manager EtagManagerを設定します.
	 */
	protected EtagManagerInfo(EtagManager manager) {
		this.manager = manager;
	}

	// 確定済みの場合はエラー返却.
	protected final void check() {
		if(doneFlag.get()) {
			throw new QuinaException("The settings have already been completed.");
		}
	}

	/**
	 * 確定・未確定条件をセット.
	 * @param flg [true]の場合、確定します.
	 * @return boolean 前回の条件が返却されます.
	 */
	protected final boolean setDone(boolean flg) {
		return doneFlag.setToGetBefore(flg);
	}

	/**
	 * 確定されているかチェック.
	 * @return boolean trueの場合確定しています.
	 */
	public boolean isDone() {
		return doneFlag.get();
	}

	/**
	 * Etag管理の有効・無効を取得.
	 * @return boolean [true]の場合有効です.
	 */
	public boolean isInForce() {
		return inForce.get();
	}

	/**
	 * Etag管理の有効・無効を設定します.
	 * @param inForceFlag [true]の場合有効になります.
	 */
	public void setInForce(boolean inForceFlag) {
		check();
		inForce.set(inForceFlag);
	}

	/**
	 * Etag計算を行う事を許容するファイルサイズを取得.
	 * @return int ファイルサイズが返却されます.
	 */
	public int getMaxFileSize() {
		return maxFileSize.get();
	}

	/**
	 * Etag計算を行う事を許容するファイルサイズを設定.
	 * @param size ファイルサイズを設定します.
	 */
	public void setMaxFileSize(int size) {
		check();
		maxFileSize.set(size);
	}

	/**
	 * パス名別でロックを行う管理数を取得.
	 * @return int パス名別でロックを行う管理数が返却されます.
	 */
	public int getPathLockSize() {
		return pathLockSize.get();
	}

	/**
	 * パス名別でロックを行う管理数を設定.
	 * @param size パス名別でロックを行う管理数を設定します.
	 */
	public void setPathLockSize(int size) {
		check();
		pathLockSize.put(size);
	}

	/**
	 * コンフィグ定義を設定して、定義を設定.
	 * @param json json情報を設定します.
	 */
	public void config(Map<String, Object> json) {
		check();
		Object o;
		// EtagManagerの有効フラグを取得.
		if((o = json.get("inForce")) != null &&
			BooleanUtil.isBool(o)) {
			setInForce(BooleanUtil.parseBoolean(o));
		}
		// Etag変換する最大ファイル長を取得.
		if((o = json.get("maxFileSize")) != null &&
			NumberUtil.isNumeric(o)) {
			setMaxFileSize(NumberUtil.parseInt(o));
		}
		// パス名に対するロック管理情報数を取得.
		if((o = json.get("lockSize")) != null &&
			NumberUtil.isNumeric(o)) {
			setPathLockSize(NumberUtil.parseInt(o));
		}
	}

	/**
	 * 設定を確定します.
	 * この処理を行わない限りEtagを取得出来ません.
	 */
	public void done() {
		manager.done();
	}

	/**
	 * EtagManagerを取得.
	 * @return EtagManager EtagManagerが返却されます.
	 */
	public EtagManager getEtagManager() {
		return manager;
	}
}
