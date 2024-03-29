package quina.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * AtomicなInteger管理.
 */
public class AtomicNumber {
	private final AtomicInteger ato = new AtomicInteger(0);

	/**
	 * コンストラクタ.
	 */
	public AtomicNumber() {

	}

	/**
	 * コンストラクタ.
	 *
	 * @param n
	 *            初期値を設定します.
	 */
	public AtomicNumber(int n) {
		ato.set(n);
	}

	/**
	 * int値を取得.
	 *
	 * @return int int値が返されます.
	 */
	public int get() {
		return ato.get();
	}

	/**
	 * int値を設定.
	 *
	 * @param n
	 *            int値を設定します.
	 */
	public void set(int n) {
		while (!ato.compareAndSet(ato.get(), n))
			;
	}

	/**
	 * int値を設定して前回の値を取得.
	 *
	 * @param n
	 *            int値を設定します.
	 * @return int 前回の値が返却されます.
	 */
	public int put(int n) {
		int ret;
		while (!ato.compareAndSet((ret = ato.get()), n))
			;
		return ret;
	}

	/**
	 * 指定数の足し算.
	 *
	 * @param no
	 *            対象の数値を設定します.
	 * @return int 結果内容が返されます.
	 */
	public int add(int no) {
		int n, r;
		while (!ato.compareAndSet((n = ato.get()), (r = n + no)))
			;
		return r;
	}

	/**
	 * 指定数の引き算.
	 *
	 * @param no
	 *            対象の数値を設定します.
	 * @return int 結果内容が返されます.
	 */
	public int remove(int no) {
		int n, r;
		while (!ato.compareAndSet((n = ato.get()), (r = n - no)))
			;
		return r;
	}

	/**
	 * 1インクリメント.
	 *
	 * @return int 結果内容が返されます.
	 */
	public int inc() {
		int n, r;
		while (!ato.compareAndSet((n = ato.get()), (r = n + 1)))
			;
		return r;
	}

	/**
	 * 1デクリメント.
	 *
	 * @return int 結果内容が返されます.
	 */
	public int dec() {
		int n, r;
		while (!ato.compareAndSet((n = ato.get()), (r = n - 1)))
			;
		return r;
	}

	/**
	 * aの値で上書きできる場合は情報を更新.
	 * @param a 更新前の条件を設定します.
	 * @param b 更新したい条件を設定します.
	 * @return boolean  trueの場合は更新できました.
	 */
	public boolean compareAndSet(int a, int b) {
		return ato.compareAndSet(a, b);
	}

	/**
	 * 文字変換.
	 *
	 * @return String 文字に変換します.
	 */
	public String toString() {
		return String.valueOf(ato.get());
	}
}
