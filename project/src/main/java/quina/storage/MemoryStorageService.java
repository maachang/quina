package quina.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.Quina;
import quina.QuinaConfig;
import quina.QuinaService;
import quina.exception.QuinaException;
import quina.util.FileUtil;
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
	
	/**
	 * サービスを開始.
	 */
	@Override
	public void startService() {
		// 一度起動している場合はエラー.
		if(startFlag.setToGetBefore(true)) {
			throw new QuinaException(this.getClass().getName() +
				" service has already started.");
		}
		InputStream in = null;
		MemoryStorageManager man;
		MemoryStorageLoopElement em;
		wlock();
		try {
			// 前回保存されたStorage保存先ファイルが存在する場合.
			if(FileUtil.isFile(config.getString("saveFile"))) {
				// InputStreamを取得.
				in = new BufferedInputStream(
					new FileInputStream(config.getString("saveFile")));
				// Storageの読み込み.
				man = new MemoryStorageManager(in);
				in.close();
				in = null;
			} else {
				// 空のStorageを生成.
				man = new MemoryStorageManager();
			}
			// Storageタイムアウト監視Loop要素を生成.
			em = new MemoryStorageLoopElement(
				config.getLong("timeout")
				,config.getLong("timing")
				,man);
			// timeoutLoopElementをQuinaLoopThreadに登録.
			Quina.get().getQuinaLoopManager().regLoopElement(em);
			// スタートアップ完了.
			this.manager = man;
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		} finally {
			wulock();
			if(in != null) {
				try {
					in.close();
				} catch(Exception ee) {}
			}
		}
	}
	
	/**
	 * サービスを停止.
	 */
	@Override
	public void stopService() {
		OutputStream out = null;
		wlock();
		try {
			// 開始していない or 既に停止してる場合
			if(!startFlag.get()) {
				return;
			}
			// MemoryStorage内容の保存先を生成.
			out = new BufferedOutputStream(
				new FileOutputStream(config.getString("saveFile")));
			// 保存処理.
			manager.save(out);
			// クローズ.
			out.flush();
			out.close();
			out = null;
			// サービス停止.
			startFlag.set(false);
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		} finally {
			if(out != null) {
				try {
					out.close();
				} catch(Exception e) {}
			}
			wulock();
		}
	}
}
