package quina.storage;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.QuinaConfig;
import quina.QuinaService;
import quina.util.Flag;
import quina.util.collection.TypesClass;

/**
 * MemoryStorageサービス.
 */
public class MemoryStorageService
	implements QuinaService {
	
	// デフォルトのStorage保存先ファイル名.
	private static final String SAVE_FILE_NAME = "./.qmss";
	
	// MemoryStorageマネージャー.
	private MemoryStorageManager manager;
	
	// MemoryStorageタイムアウト監視Loop要素.
	private MemoryStorageLoopElement loopElement;
	
	// QuinaConfig.
	private QuinaConfig config = new QuinaConfig(
		StorageConstants.CONFIG_NAME
		,"timeout", TypesClass.Long, StorageConstants.getTimeout()
		,"timing", TypesClass.Long, StorageConstants.getCheckTiming()
		,"saveFile", TypesClass.String, SAVE_FILE_NAME
	);
	
	// 開始サービスフラグ.
	private final Flag startFlag = new Flag(false);
	
	// Read-Writeロックオブジェクト.
	private final ReentrantReadWriteLock lock =
		new ReentrantReadWriteLock();
	
	/**
	 * コンストラクタ.
	 */
	public MemoryStorageService() {}
	
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
		return config;
	}
	
	/**
	 * サービス開始処理[startService()]が実行されたかチェック.
	 * @return boolena [true]の場合、サービス開始処理が呼び出されています.
	 */
	@Override
	public boolean isStartService() {
		return startFlag.get();
	}

}
