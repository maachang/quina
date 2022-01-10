package quina.jdbc.storage;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.Quina;
import quina.QuinaConfig;
import quina.QuinaUtil;
import quina.annotation.quina.QuinaServiceScoped;
import quina.exception.QuinaException;
import quina.storage.StorageConstants;
import quina.storage.StorageManager;
import quina.storage.StorageQuinaService;
import quina.util.Flag;
import quina.util.collection.TypesClass;

/**
 * JDBCStorageService.
 */
@QuinaServiceScoped(name=StorageConstants.SERVICE_NAME,
	define=JDBCStorageService.SERVICE_DEFINE)
public class JDBCStorageService
	implements StorageQuinaService {
	
	// サービス定義名.
	protected static final String SERVICE_DEFINE = "jdbc";
	
	// JDBCStorageマネージャー.
	private JDBCStorageManager manager;
	
	// QuinaConfig.
	private QuinaConfig config = new QuinaConfig(
		StorageConstants.CONFIG_NAME
		,StorageConstants.TIMEOUT, TypesClass.Long, StorageConstants.getTimeout()
		,StorageConstants.TIMING, TypesClass.Long, StorageConstants.getCheckTiming()
		,"dataSource", TypesClass.String, null
	);
	
	// 開始サービスフラグ.
	private final Flag startFlag = new Flag(false);
	
	// Read-Writeロックオブジェクト.
	private final ReentrantReadWriteLock lock =
		new ReentrantReadWriteLock();
	
	/**
	 * コンストラクタ.
	 */
	public JDBCStorageService() {}
	
	/**
	 * Read/WriteLockを取得.
	 * @return ReadWriteLock Read/WriteLockを取得します.
	 */
	@Override
	public ReadWriteLock getLock() {
		return lock;
	}
	
	/**
	 * QuinaConfigを取得.
	 * @return QuinaConfig QuinaConfigが返却されます.
	 *                     null の場合コンフィグ情報は
	 *                     対応しません.
	 */
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
	 * サービス開始処理[startService()]が実行されたかチェック.
	 * @return boolena [true]の場合、サービス開始処理が呼び出されています.
	 */
	@Override
	public boolean isStartService() {
		return startFlag.get();
	}
	
	/**
	 * サービスを開始.
	 */
	@Override
	public void startService() {
		JDBCStorageLoopElement em = null;
		JDBCStorageManager man = null;
		wlock();
		try {
			// 一度起動している場合はエラー.
			checkService(true);
			// JDBCStorageManagerを生成.
			man = new JDBCStorageManager(
				config.getString("dataSource"));
			// Storageタイムアウト監視Loop要素を生成.
			em = new JDBCStorageLoopElement(
				config.getLong(StorageConstants.TIMEOUT)
				,config.getLong(StorageConstants.TIMING)
				,man);
			// timeoutLoopElementをQuinaLoopThreadに登録.
			Quina.get().getQuinaLoopManager().regLoopElement(em);
			// 開始ログ出力.
			QuinaUtil.startServiceLog(this);
			// スタートアップ完了.
			this.manager = man;
			// サービス開始.
			startFlag.set(true);
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		} finally {
			wulock();
		}
	}
	
	/**
	 * サービスを停止.
	 */
	@Override
	public void stopService() {
		wlock();
		try {
			// 開始していない or 既に停止してる場合
			if(!startFlag.get()) {
				return;
			}
			// マネージャを破棄.
			manager.destroy();
			// サービス停止.
			startFlag.set(false);
			// 停止ログ出力.
			QuinaUtil.stopServiceLog(this);
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		} finally {
			wulock();
		}
	}
	
	/**
	 * StorageManagerを取得.
	 * @return StorageManagerが返却されます.
	 */
	@Override
	public StorageManager getStorageManager() {
		rlock();
		try {
			return manager;
		} finally {
			rulock();
		}
	}
}
