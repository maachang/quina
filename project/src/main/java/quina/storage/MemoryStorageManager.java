package quina.storage;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.exception.QuinaException;
import quina.util.BinaryIO;
import quina.util.Flag;
import quina.util.collection.IndexKeyValueList;

/**
 * MemoryStorageManager.
 */
public class MemoryStorageManager
	implements StorageManager {
	
	// メモリStorageを永続化するためのファイルヘッダ名.
	// quinaStge
	private static final byte[] PERSISTENCE_STORAGE_HEADER = new byte[] {
		(byte)0x90, (byte)0x17, (byte)0xa8, (byte)'t', (byte)0x9e
	};
	
	// 管理オブジェクト.
	protected final IndexKeyValueList<String, MemoryStorage> manager;
	
	// Read-Writeロックオブジェクト.
	protected final ReadWriteLock lock = new ReentrantReadWriteLock();
	
	// 破棄フラグ.
	protected final Flag destroyFlag = new Flag(false);
	
	/**
	 * コンストラクタ.
	 */
	public MemoryStorageManager() {
		manager = new IndexKeyValueList<String, MemoryStorage>();
	}
	
	/**
	 * コンストラクタ.
	 * @param in 永続化されたファイルのInputStreamを設定します.
	 */
	public MemoryStorageManager(InputStream in) {
		try {
			int i;
			final byte[] tmp = BinaryIO.createTmp();
			final int plen = PERSISTENCE_STORAGE_HEADER.length;
			int len = in.read(tmp, 0, plen);
			// 永続化ヘッダ長と読み込みデータが一致しない.
			if(len != plen) {
				throw new QuinaException(
					"The persisted content does not match the Memory " +
					"Storage Manager.");
			}
			// 永続化ヘッダ内容が一致しない.
			for(i = 0; i < plen; i ++) {
				if(PERSISTENCE_STORAGE_HEADER[i] != tmp[i]) {
					throw new QuinaException(
						"The persisted content does not match the Memory " +
						"Storage Manager.");
				}
			}
			MemoryStorage storage;
			String name;
			final IndexKeyValueList<String, MemoryStorage> man = new
				IndexKeyValueList<String, MemoryStorage>();
			// 永続化したMemoryStorage数を取得して読み込み.
			final int storageLen = BinaryIO.readSavingInt(in, tmp);
			for(i = 0; i < storageLen; i ++) {
				// MemoryStorage名を取得.
				name = BinaryIO.readString(in, tmp);
				// MemoryStorageを取得.
				storage = new MemoryStorage(this, name);
				storage.load(in);
				// マネージャにセット.
				man.put(name, storage);
			}
			this.manager = man;
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
	
	/**
	 * オブジェクトを破棄.
	 */
	protected void destroy() {
		// 破棄フラグをON.
		destroyFlag.set(true);
	}
	
	// 名前チェック.
	protected String checkName(String name) {
		if(name == null || (name = name.trim()).isEmpty()) {
			throw new QuinaException(
				"The specified Storage name has not been set.");
		}
		return name;
	}
	
	@Override
	public Storage createStorage(String name) {
		// 破棄済みの場合は処理しない.
		if(destroyFlag.get()) {
			return null;
		}
		name = checkName(name);
		lock.writeLock().lock();
		try {
			if(manager.containsKey(name)) {
				// 既に存在している場合.
				throw new QuinaException(
					"Storage \"" + name +
					"\" with the specified name already exists.");
			}
			MemoryStorage ret = new MemoryStorage(this, name);
			manager.put(name, ret);
			return ret;
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void removeStorage(String name) {
		// 破棄済みの場合は処理しない.
		if(destroyFlag.get()) {
			return;
		}
		name = checkName(name);
		lock.writeLock().lock();
		try {
			manager.remove(name);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public Storage getStorage(String name) {
		// 破棄済みの場合は処理しない.
		if(destroyFlag.get()) {
			return null;
		}
		name = checkName(name);
		lock.readLock().lock();
		try {
			return manager.get(name);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean isStorage(String name) {
		// 破棄済みの場合は処理しない.
		if(destroyFlag.get()) {
			return false;
		}
		name = checkName(name);
		lock.readLock().lock();
		try {
			return manager.containsKey(name);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int size() {
		// 破棄済みの場合は処理しない.
		if(destroyFlag.get()) {
			return 0;
		}
		lock.readLock().lock();
		try {
			return manager.size();
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * 項番を指定してキー名を取得.
	 * @param no 対象の項番を設定します.
	 * @return String キー名が返却されます.
	 */
	protected String keyAt(int no) {
		// 破棄済みの場合は処理しない.
		if(destroyFlag.get()) {
			return null;
		}
		lock.readLock().lock();
		try {
			return manager.keyAt(no);
		} catch(Exception e) {
			return null;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * 現在のMemoryStorageを永続化.
	 * @param out 対象のOutputStreamを設定します.
	 */
	public void save(OutputStream out) {
		// 破棄済みの場合は処理しない.
		if(destroyFlag.get()) {
			return;
		}
		lock.readLock().lock();
		try {
			final byte[] tmp = BinaryIO.createTmp();
			// ヘッダ.
			out.write(PERSISTENCE_STORAGE_HEADER);
			// 長さ.
			final int len = manager.size();
			BinaryIO.writeSavingBinary(out, tmp, len);
			// 各MemoryStorageを永続化.
			for(int i = 0; i < len; i ++) {
				BinaryIO.writeString(out, tmp, manager.keyAt(i));
				manager.valueAt(i).save(out);
			}
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		} finally {
			lock.readLock().unlock();
		}
	}
}