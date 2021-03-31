package quina.promise;

import quina.QuinaUtil;
import quina.util.AtomicNumber;
import quina.util.NumberUtil;
import quina.util.collection.BinarySearchMap;

/**
 * PromiseWorkerConfig.
 */
public class PromiseWorkerConfig {
	// ワーカーコンフィグファイル名.
	private static final String WORKER_CONFIG_NAME = "promise";

	// ワーカースレッド数.
	private final AtomicNumber workerLength = new AtomicNumber(-1);

	/**
	 * コンストラクタ.
	 */
	public PromiseWorkerConfig() {
	}

	/**
	 * ディレクトリを設定してコンフィグのロード.
	 * @param dir コンフィグファイルが設定されているディレクトリを設定します.
	 */
	public void loadConfig(String dir) {
		// コンフィグファイルから取得.
		if(!load(dir)) {
			// -Dパラメータ定義から取得.
			if(!loadWorkerLength()) {
				// 存在しない場合はデフォルト定義.
				setWorkerLength(
					PromiseConstants.getWorkerThreadLength());
			}
		}
	}

	/**
	 * ワーカースレッド数を取得.
	 * @return int ワーカースレッド数が返却されます.
	 */
	public int getWorkerLength() {
		if(workerLength.get() == -1) {
			loadConfig(null);
		}
		return workerLength.get();
	}

	/**
	 * ワーカースレッド数を設定.
	 * @param len ワーカースレッド数を設定します.
	 */
	public void setWorkerLength(int len) {
		if(PromiseConstants.MIN_WORKER_THREAD_LENGTH > len) {
			len = PromiseConstants.MIN_WORKER_THREAD_LENGTH;
		} else if(PromiseConstants.MAX_WORKER_THREAD_LENGTH < len) {
			len = PromiseConstants.MAX_WORKER_THREAD_LENGTH;
		}
		workerLength.set(len);
	}

	// コンフィグ情報をロード.
	private boolean load(String dir) {
		try {
			// 対象のコンフィグ情報を読み込む.
			BinarySearchMap<String, Object> conf =
				QuinaUtil.loadJson(dir, WORKER_CONFIG_NAME);
			// 取得できなかった場合.
			if(conf == null) {
				return false;
			}
			// ワーカースレッド長を取得.
			Integer len = conf.getInt("workerLength");
			if(len != null) {
				setWorkerLength(len);
				return true;
			}
		} catch(Exception e) {
		}
		return false;
	}

	// ワーカースレッド数をロード.
	private boolean loadWorkerLength() {
		// パラメータからワーカースレッド数を取得.
		String o;
		String[] check;
		// システムプロパティから取得.
		check = new String[] {
			"promise.workerSize",
			"promise.workerLength",
			"promise.worker",
			"promise.length"
		};
		for(int i = 0; i < check.length; i ++) {
			o = System.getProperty(check[i]);
			if(o != null && NumberUtil.isNumeric(o)) {
				setWorkerLength(NumberUtil.parseInt(o));
				return true;
			}
		}
		return false;
	}

}
