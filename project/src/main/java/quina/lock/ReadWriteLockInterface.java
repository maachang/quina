package quina.lock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Read/Writeロックを提供するインターフェイス.
 */
public interface ReadWriteLockInterface {

	/**
	 * ロックオブジェクトを取得.
	 * @return ReentrantReadWriteLock
	 *     ロックオブジェクトが返却されます.
	 */
	public ReentrantReadWriteLock lock();
}
