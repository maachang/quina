package quina.jdbc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
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
@QuinaServiceScoped(name=QuinaJDBCService.SERVICE_AND_CONFIG_NAME)
public class QuinaJDBCService implements QuinaService {
	// サービス/コンフィグ名.
	protected static final String SERVICE_AND_CONFIG_NAME = "jdbc";
	
	// ログ情報.
	@LogDefine
	private Log log;
	
	// QuinaConfig.
	private QuinaConfig config = new QuinaConfig(
		"jdbcPooling"
		,"timeout", TypesClass.Long, QuinaJDBCConstants.getPoolingTimeout()
	);
	
	// デフォルトのデータソース.
	private QuinaDataSource defaultDataSource = null;
	
	// データソース管理.
	private IndexKeyValueList<String, QuinaDataSource> dataSources = null;
	
	// TimeoutLoopElement.
	private TimeoutLoopElement timeoutLoopElement = null;
	
	// サービス開始フラグ.
	private final Flag startFlag = new Flag(false);
	
	// Read-Writeロックオブジェクト.
	private final ReentrantReadWriteLock lock =
		new ReentrantReadWriteLock();
	
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
		return (QuinaJDBCService)Quina.get().getQuinaServiceManager()
			.get(SERVICE_AND_CONFIG_NAME);
	}
	
	/**
	 * 対象のデーターソースオブジェクトを取得.
	 * @param name 対象のデータソース名を設定します.
	 * @return QuinaDataSource 対象のQuinaDataSourceが返却されます.
	 */
	public static final QuinaDataSource dataSource(
		String name) {
		return getService().getDataSource(name);
	}
	
	@Override
	public ReadWriteLock getLock() {
		return lock;
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean loadConfig(String configDir) {
		// 既に開始してる場合はエラー.
		boolean ret = false;
		wlock();
		try {
			// 既にサービスが開始している場合はエラー.
			checkService(true);
			// コンフィグ読み込み.
			final QuinaMap<String, Object> json = QuinaUtil.loadJson(
				configDir, SERVICE_AND_CONFIG_NAME);
			if(json == null) {
				// コンフィグ情報が存在しない.
				return true;
			}
			// pooling定義.
			final String poolingConfigName = config.getName();
			Object o = json.get(poolingConfigName);
			if(o != null && o instanceof QuinaMap) {
				config.setConfig((QuinaMap)o);
			}
			
			// kind定義.
			QuinaDataSource ds;
			QuinaDataSource topDs = null;
			QuinaDataSource defDs = null;
			IndexKeyValueList man = null;
			final int len = json.size();
			if(len > 0) {
				man = new IndexKeyValueList<String, QuinaDataSource>();
				for(int i = 0; i < len; i ++) {
					// pooling定義以外はkind定義として読み込む.
					if(poolingConfigName.equals(json.keyAt(i))) {
						continue;
					}
					// value定義がkind定義の場合.
					// dataSourceを生成してセット.
					o = json.valueAt(i);
					if(o != null && o instanceof Map) {
						// コンフィグを読み込み.
						QuinaJDBCConfig conf = QuinaJDBCConfig.create(
							json.keyAt(i), (Map)o);
						// コンフィグをFix.
						conf.fix();
						// データソースを生成.
						ds = new QuinaDataSource(
							i, conf.getName(), this, conf);
						// 一番最初に定義されてるDataSourceを取得.
						if(topDs == null) {
							topDs = ds;
						}
						// 対象のデーターソースがデフォルトデーターソースの場合.
						if(ds.isDefault()) {
							// デフォルトデーターソースが既に定義されている場合.
							if(defDs != null) {
								// エラー
								throw new QuinaException(
									"Multiple default data sources are defined.");
							}
							// デフォルトデーターソースとして登録.
							defDs = ds;
						}
						// データーソースを登録.
						man.put(conf.getName(), ds);
						ret = true;
					}
				}
				// デフォルトのデーターソースが定義されていない場合.
				if(defDs == null) {
					// 一番最初に定義されたものをデフォルトのデーターソース
					// として定義.
					defDs = topDs;
				}
			}
			// 登録.
			this.defaultDataSource = defDs;
			this.dataSources = man;
		} finally {
			wulock();
		}
		return ret;
	}
	
	@Override
	public boolean isStartService() {
		return startFlag.get();
	}
	
	// 最大タイムアウト監視キュー設定時間.
	private static final long MAX_DOUBT_TIMEOUT = 1500L;
	
	// タイムアウト監視キュー設定時間を取得.
	private static final long getDoubtTimeout(long timeout) {
		timeout /= 10L;
		if(MAX_DOUBT_TIMEOUT < timeout) {
			return MAX_DOUBT_TIMEOUT;
		}
		return timeout;
	}

	@Override
	public void startService() {
		wlock();
		try {
			// 既にサービスが開始している場合はエラー.
			checkService(true);
			// DataSourceの登録が存在する場合のみ
			// TimeoutThreadを生成して開始して、
			// 各DataSourceに登録する.
			if(dataSources != null && dataSources.size() > 0) {
				// timeoutLoopElementを生成.
				timeoutLoopElement = new TimeoutLoopElement(
					config.getLong("timeout"),
					getDoubtTimeout(config.getLong("timeout")),
					new QuinaJDBCTimeoutHandler());
				// timeoutLoopElementを登録.
				Quina.get().getQuinaLoopManager().regLoopElement(timeoutLoopElement);
			}
			// サービス開始.
			startFlag.set(true);
		} finally {
			wulock();
		}
		log.info("@ startService " + this.getClass().getName());
	}
	
	@Override
	public void stopService() {
		wlock();
		try {
			if(startFlag.setToGetBefore(false)) {
				log.info("@ stopService " + this.getClass().getName());
			}
		} finally {
			wulock();
		}
	}

	@Override
	public QuinaConfig getConfig() {
		rlock();
		try {
			return config;
		} finally {
			rulock();
		}
	}
	
	/**
	 * デフォルトのDataSourceを取得.
	 * @return QuinaDataSource QuinaDataSourcが返却されます.
	 */
	public QuinaDataSource getDataSource() {
		return getDataSource(null);
	}

	/**
	 * 指定名のDataSourceを取得.
	 * @param name 対象名を設定します.
	 * @return QuinaDataSource QuinaDataSourcが返却されます.
	 */
	public QuinaDataSource getDataSource(String name) {
		QuinaDataSource ret = null;
		rlock();
		try {
			// サービスが開始してない場合はエラー.
			checkService(false);
			// 対象名が設定されてない場合.
			if(name == null || (name = name.trim()).isEmpty()) {
				// デフォルトのデーターソース返却.
				ret = defaultDataSource;
			// 対象名が設定されている場合.
			} else {
				// データソースを取得.
				ret = dataSources == null ?
					null : dataSources.get(name);
			}
		} finally {
			rulock();
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
		rlock();
		try {
			final int len = dataSources == null ?
				0 : dataSources.size();
			for(int i = 0; i < len; i ++) {
				out.add(dataSources.keyAt(i));
			}
			return len;
		} finally {
			rulock();
		}
	}
	
	// タイムアウトLoopElementを取得.
	protected TimeoutLoopElement getTimeoutLoopElement() {
		rlock();
		try {
			return timeoutLoopElement;
		} finally {
			rulock();
		}
	}
}
