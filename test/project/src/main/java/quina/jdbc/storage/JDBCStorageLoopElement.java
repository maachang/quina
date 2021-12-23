package quina.jdbc.storage;

import quina.QuinaThreadStatus;
import quina.jdbc.io.IoStatement;
import quina.jdbc.io.QueryResult;
import quina.storage.StorageConstants;
import quina.util.collection.ObjectList;
import quina.worker.QuinaLoopElement;

/**
 * JDBCStorageManagerのタイムアウト監視する
 * LoopElement.
 */
final class JDBCStorageLoopElement
	implements QuinaLoopElement {
	
	// タイムアウト時間.
	protected long timeout;
	
	// 確認タイミング.
	protected long checkTiming;
	
	// MemoryStorageマネージャ.
	protected JDBCStorageManager manager;
	
	// 前回確認したタイムアウト値.
	protected long beforeTimeout;
	
	/**
	 * コンストラクタ.
	 * @param timeout 各StorageManagerのタイムアウト時間を
	 *                設定します.
	 * @param checkTiming LoopElementを実行するタイミングを
	 *                    設定します.
	 * @param manager JDBCStorageマネージャを設定します.
	 */
	protected JDBCStorageLoopElement(
		long timeout, long checkTiming,
		JDBCStorageManager manager) {
		this.timeout = StorageConstants.getTimeout(timeout);
		this.checkTiming = StorageConstants.getCheckTiming(checkTiming);
		this.manager = manager;
		this.beforeTimeout =
			System.currentTimeMillis() + this.checkTiming;
	}
	
	@Override
	public void execute(QuinaThreadStatus status)
		throws Throwable {
		final long nowTime = System.currentTimeMillis();
		// タイムアウトチェックを行わない場合.
		if(beforeTimeout > nowTime) {
			sleep();
			return;
		}
		// タイムアウト処理.
		executeTimeout(nowTime);
		// 次のチェックタイミングを設定.
		beforeTimeout = System.currentTimeMillis() + checkTiming;
	}
	
	/**
	 * タイムアウト処理を実行.
	 * @param time 現在の時間が設定されます.
	 */
	protected void executeTimeout(long nowTime) {
		IoStatement.execute(manager.getQuinaDataSource(), (ios) -> {
			// この時間以下の場合タイムアウトなミリ秒を取得.
			long nowTimeout = nowTime - timeout;
			// Storage管理テーブルから、タイムアウトされたIDの一覧を取得.
			QueryResult res = ios.selectSQL(
				JDBCStorageConstants.MANAGE_TABLE_NAME, "id")
				.sql("where update_time < ?")
				.params(nowTimeout)
				.executeQuery();
			// ID一覧を取得.
			final ObjectList<Long> list = new ObjectList<Long>();
			while(res.hasNext()) {
				list.add(res.next().getLong("id"));
			}
			// タイムアウト結果が１件も無い場合.
			if(list.size() == 0) {
				// 処理しない.
				return null;
			}
			// 存在する場合はそれぞれを削除処理.
			// タイムアウト対象の要素テーブル群を削除.
			ios.deleteSQL(JDBCStorageConstants.ELEMENT_TABLE_NAME)
				.sql("where man_id in(")
				.paramsSQL(list.size())
				.sql(")")
				.params(list);
			// タイムアウト対象の管理テーブルを削除.
			ios.deleteSQL(JDBCStorageConstants.MANAGE_TABLE_NAME)
				.sql("where id in(")
				.paramsSQL(list.size())
				.sql(")")
				.params(list);
			return null;
		});
	}
}
