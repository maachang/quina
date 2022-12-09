package quina.thread;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * Read/Writeロック実装.
 */
public interface RwLockAtualization {

	/**
	 * Read/WriteLockを取得.
	 * @return ReadWriteLock Read/WriteLockを取得します.
	 */
	public ReadWriteLock getLock();
	
	/**
	 * 読み込みロック.
	 */
	default void rlock() {
		final ReadWriteLock rw = getLock();
		if(rw == null) {
			// 何もしない.
		}
		rw.readLock().lock();
	}
	
	/**
	 * 読み込みアンロック.
	 */
	default void rulock() {
		final ReadWriteLock rw = getLock();
		if(rw == null) {
			// 何もしない.
		}
		rw.readLock().unlock();
	}
	
	/**
	 * 書き込みロック.
	 */
	default void wlock() {
		final ReadWriteLock rw = getLock();
		if(rw == null) {
			// 何もしない.
		}
		rw.writeLock().lock();
	}
	
	/**
	 * 書き込みアンロック.
	 */
	default void wulock() {
		final ReadWriteLock rw = getLock();
		if(rw == null) {
			// 何もしない.
		}
		rw.writeLock().unlock();
	}
}
