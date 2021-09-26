package quina.util;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Atomicなオブジェクト管理.
 */
public class AtomicObject<T> {
	private final AtomicReference<T> ato = new AtomicReference<T>();
	
	/**
	 * コンストラクタ.
	 */
	public AtomicObject() {

	}

	/**
	 * コンストラクタ.
	 * 
	 * @param n
	 *            初期値を設定します.
	 */
	public AtomicObject(T n) {
		ato.set(n);
	}

	/**
	 * オブジェクトを取得.
	 * 
	 * @return T オブジェクトが返されます.
	 */
	public T get() {
		return ato.get();
	}

	/**
	 * オブジェクトを設定.
	 * 
	 * @param n
	 *            オブジェクトを設定します.
	 */
	public void set(T n) {
		while (!ato.compareAndSet(ato.get(), n))
			;
	}
	
	/**
	 * オブジェクトを設定して前回の値を取得.
	 * 
	 * @param n
	 *            オブジェクトを設定します.
	 * @return T 前回の値が返却されます.
	 */
	public T put(T n) {
		T ret;
		while (!ato.compareAndSet((ret = ato.get()), n))
			;
		return ret;
	}
}
