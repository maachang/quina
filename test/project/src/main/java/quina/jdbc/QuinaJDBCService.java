package quina.jdbc;

import java.util.List;
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
import quina.util.collection.QuinaMap;
import quina.util.collection.TypesClass;
import quina.worker.timeout.TimeoutLoopElement;

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
		"jdbcPooling"
		,"timeout", TypesClass.Long, QuinaJDBCConstants.getPoolingTimeout()
	);
	
	// データソース管理.
	private IndexKeyValueList<String, QuinaDataSource> dataSources =
		new IndexKeyValueList<String, QuinaDataSource>();
	
	// TimeoutLoopElement.
	private TimeoutLoopElement timeoutLoopElement = null;
	
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
			final QuinaMap<String, Object> json = QuinaUtil.loadJson(
				configDir, CONFIG_NAME);
			if(json == null) {
				return true;
			}
			// pooling定義.
			final String poolingConfigName = config.getName();
			Object o = json.get(poolingConfigName);
			if(o != null && o instanceof QuinaMap) {
				config.setConfig((QuinaMap)o);
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
				// timeoutLoopElementを生成.
				timeoutLoopElement = new TimeoutLoopElement(
					config.getLong("timeout"),
					config.getLong("timeout") / 10L,
					new QuinaJDBCTimeoutHandler());
				// timeoutLoopElementを登録.
				Quina.get().getQuinaLoopManager().regLoopElement(timeoutLoopElement);
			}
		} finally {
			lock.writeLock().unlock();
		}
		log.info("@ startService " + this.getClass().getName());
	}
	
	@Override
	public boolean isStarted() {
		return startFlag.get();
	}
	
	@Override
	public void stopService() {
		if(startFlag.get()) {
			log.info("@ stopService " + this.getClass().getName());
		}
		startFlag.set(false);
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
	
	/**
	 * データソース名群を取得.
	 * @param out 格納するListを設定します.
	 * @return int データソース名数が返却されます.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int getDataSouceNames(List out) {
		final int len = dataSources.size();
		for(int i = 0; i < len; i ++) {
			out.add(dataSources.keyAt(i));
		}
		return len;
	}
	
	// タイムアウトLoopElementを取得.
	protected TimeoutLoopElement getTimeoutLoopElement() {
		return timeoutLoopElement;
	}
}
