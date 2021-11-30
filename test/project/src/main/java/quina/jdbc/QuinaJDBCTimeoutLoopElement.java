package quina.jdbc;

import java.util.Iterator;
import java.util.Queue;

import quina.QuinaThreadStatus;
import quina.util.collection.IndexKeyValueList;
import quina.worker.QuinaLoopElement;

/**
 * PoolingConnectionタイムアウト監視スレッド.
 */
class QuinaJDBCTimeoutLoopElement
	implements QuinaLoopElement {
	
	// 利用中を示すタイムアウト値.
	protected static final long NONE_TIMEOUT = -1L;
	
	// 破棄を示すタイムアウト値.
	protected static final long DESTROY_TIMEOUT = 0L;
	
	// タイムアウト値.
	private long timeout;
	
	// チェックDataSourceNo.
	private int checkTimeoutNo;
	
	// データソース管理.
	private IndexKeyValueList<String, QuinaDataSource> dataSources;
	
	/**
	 * コンストラクタ.
	 * @param dataSources dataSourceを管理するオブジェクトを設定します.
	 * @param timeout タイムアウト値を設定します.
	 */
	protected QuinaJDBCTimeoutLoopElement(
		IndexKeyValueList<String, QuinaDataSource> dataSources,
		long timeout) {
		this.dataSources = dataSources;
		this.timeout = timeout;
		this.checkTimeoutNo = 0;
	}

	/**
	 * Loop実行.
	 * @param status QuinaThreadステータスが設定されます.
	 * @exception Throwable 例外.
	 */
	@Override
	public void execute(QuinaThreadStatus status)
		throws Throwable {
		final int len = dataSources.size();
		// タイムアウトチェック対象のDataSourceの項番を取得.
		if(checkTimeoutNo >= len) {
			checkTimeoutNo = 0;
		}
		// 処理対象のDataSourceを取得.
		QuinaDataSource ds = dataSources.valueAt(checkTimeoutNo ++);
		// Pooling対象のQueueを取得.
		Queue<QuinaConnection> q = ds.getPooling();
		// Queueに情報が存在しない場合.
		if(q.size() <= 0) {
			if(!status.isStopThread()) {
				Thread.sleep(5L);
			}
			return;
		}
		// Queueに情報が存在する場合タイムアウトチェック.
		QuinaConnection conn = null;
		Iterator<QuinaConnection> it = q.iterator();
		while(it.hasNext()) {
			if(status.isStopThread()) {
				break;
			}
			try {
				conn = it.next();
				// 破棄されてない場合.
				if(!conn.isDestroy()) {
					// タイムアウト.
					if(conn.getLastPoolingTime() + timeout <
						System.currentTimeMillis()) {
						// 破棄.
						conn.destroy();
						// 削除.
						it.remove();
					}
				}
			} catch(Exception e ) {
				// エラーが発生した場合.
				if(conn != null) {
					// コネクションを破棄.
					try {
						conn.destroy();
					} catch(Exception ee) {}
					// 削除.
					it.remove();
				}
			}
		}
	}
}
