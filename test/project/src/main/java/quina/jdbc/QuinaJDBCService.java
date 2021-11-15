package quina.jdbc;

import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.Quina;
import quina.QuinaConfig;
import quina.QuinaService;
import quina.QuinaUtil;
import quina.annotation.log.LogDefine;
import quina.annotation.quina.QuinaServiceScoped;
import quina.exception.QuinaException;
import quina.logger.Log;
import quina.util.Flag;
import quina.util.collection.IndexKeyValueList;
import quina.util.collection.IndexMap;
import quina.util.collection.TypesClass;

/**
 * QuinaJDBCService.
 */
@QuinaServiceScoped("jdbc")
public class QuinaJDBCService implements QuinaService {
	@LogDefine
	private Log log;
	
	// コンフィグ名.
	private static final String CONFIG_NAME = "jdbc";
	
	// QuinaConfig.
	private QuinaConfig config = new QuinaConfig(
		"pooling",
		"timeout", TypesClass.Long, QuinaJDBCConstants.DEF_POOLING_TIMEOUT
	);
	
	// データソース管理.
	private IndexKeyValueList<String, QuinaDataSource> dataSources =
		new IndexKeyValueList<String, QuinaDataSource>();
	
	// タイムアウトスレッド.
	private QuinaJDBCTimeoutThread timeoutThread;
	
	// サービス開始フラグ.
	private final Flag startFlag = new Flag(false);
	
	// Read-Writeロックオブジェクト.
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	/**
	 * コンストラクタ.
	 */
	public QuinaJDBCService() {
		
	}
	
	/**
	 * QuinaJDBCServiceを取得.
	 * @return QuinaJDBCService QuinaJDBCServiceが返却されます.
	 */
	public static final QuinaJDBCService getService() {
		return (QuinaJDBCService)
			Quina.get().getQuinaServiceManager().get("jdbc");
	}
	
	/**
	 * 対象のデーターソースオブジェクトを取得.
	 * @param name 対象のデータソース名を設定します.
	 * @return QuinaDataSource 対象のQuinaDataSourceが返却されます.
	 */
	public static final QuinaDataSource dataSource(String name) {
		return getService().getDataSource(name);
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean loadConfig(String configDir) {
		// 既に開始してる場合はエラー.
		if(startFlag.get()) {
			throw new QuinaException(
				"The service has already started.");
		}
		boolean ret = false;
		lock.writeLock().lock();
		try {
			// コンフィグ読み込み.
			final IndexMap<String, Object> json = QuinaUtil.loadJson(
				configDir, CONFIG_NAME);
			if(json == null) {
				return true;
			}
			// pooling定義.
			final String poolingConfigName = config.getName();
			Object o = json.get(poolingConfigName);
			if(o != null && o instanceof Map) {
				config.setConfig((Map)o);
			}
			
			// kind定義.
			final int len = json.size();
			for(int i = 0; i < len; i ++) {
				// pooling定義以外はkind定義として読み込む.
				if(poolingConfigName.equals(json.keyAt(i))) {
					continue;
				}
				// value定義がkind定義の場合.
				// dataSourceを生成してセット.
				o = json.valueAt(i);
				if(o != null && o instanceof Map) {
					QuinaJDBCConfig conf = QuinaJDBCConfig.create(
						json.keyAt(i), (Map)o);
					conf.fix();
					dataSources.put(conf.getName(),
						new QuinaDataSource(this, conf));
					ret = true;
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
		return ret;
	}
	
	@Override
	public boolean isStartService() {
		return startFlag.get();
	}

	@Override
	public void startService() {
		// 一度起動している場合はエラー.
		if(startFlag.setToGetBefore(true)) {
			throw new QuinaException(this.getClass().getName() +
				" service has already started.");
		}
		lock.writeLock().lock();
		try {
			// DataSourceの登録が存在する場合のみ
			// TimeoutThreadを生成して開始して、
			// 各DataSourceに登録する.
			if(dataSources.size() > 0) {
				// TimeoutThreadを生成.
				timeoutThread = new QuinaJDBCTimeoutThread(
					dataSources, config.getLong("timeout"));
				// TimeoutThreadを開始.
				timeoutThread.startThread();
			}
		} finally {
			lock.writeLock().unlock();
		}
		log.info("@ startService " + this.getClass().getName());
	}
	
	@Override
	public boolean isStarted() {
		lock.readLock().lock();
		try {
			if(timeoutThread != null) {
				return timeoutThread.isStartupThread();
			}
			return startFlag.get();
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	public boolean awaitStartup(long timeout) {
		int len = 0;
		QuinaJDBCTimeoutThread q = null;
		lock.readLock().lock();
		try {
			len = dataSources.size();
			q = timeoutThread;
		} finally {
			lock.readLock().unlock();
		}
		boolean ret = true;
		// timeoutThreadが存在し
		// DataSourceが１件以上存在する場合.
		if(q != null && len > 0) {
			// TimeoutThradが開始してない場合は
			// false返却.
			if(!q.awaitStartup(timeout)) {
				ret = false;
			}
		}
		return ret;
	}

	@Override
	public void stopService() {
		lock.writeLock().lock();
		try {
			// DataSoutceに登録されてる内容を削除.
			if(dataSources.size() > 0) {
				// TimeoutThreadを停止処理.
				if(timeoutThread != null) {
					timeoutThread.stopThread();
				}
				final int len = dataSources.size();
				for(int i = 0; i < len; i ++) {
					dataSources.valueAt(i).destroy();
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
		if(startFlag.get()) {
			log.info("@ stopService " + this.getClass().getName());
		}
		startFlag.set(false);
	}

	@Override
	public boolean isExit() {
		lock.readLock().lock();
		try {
			if(timeoutThread != null) {
				return timeoutThread.isStopThread();
			}
			return true;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean awaitExit(long timeout) {
		long len = 0;
		QuinaJDBCTimeoutThread q = null;
		lock.readLock().lock();
		try {
			len = dataSources.size();
			q = timeoutThread;
		} finally {
			lock.readLock().unlock();
		}
		boolean ret = true;
		// timeoutThreadが存在し
		// DataSourceが１件以上存在する場合.
		if(q != null && len > 0) {
			// TimeoutThradが終了してない場合は
			// false返却.
			if(!q.awaitExit(timeout)) {
				ret = false;
			}
		}
		return ret;
	}

	@Override
	public QuinaConfig getConfig() {
		lock.readLock().lock();
		try {
			return config;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * 指定名のDataSourceを取得.
	 * @param name 対象名を設定します.
	 * @return QuinaDataSource QuinaDataSourcが返却されます.
	 */
	public QuinaDataSource getDataSource(String name) {
		// サービスが開始してない場合はエラー.
		if(!startFlag.get()) {
			throw new QuinaException(
				"The service has not started.");
		}
		QuinaDataSource ret = null;
		lock.readLock().lock();
		try {
			ret = dataSources.get(name);
		} finally {
			lock.readLock().unlock();
		}
		// 存在しない場合は例外.
		if(ret == null) {
			throw new QuinaException("The target DataSource \"" +
				name + "\" does not exist. ");
		}
		return ret;
	}
}
