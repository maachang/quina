package quina.net.nio.tcp;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * NioのAtomic関連オブジェクト群.
 */
@SuppressWarnings("unchecked")
public class NioAtomicValues {
	private NioAtomicValues() {}

	// boolean 管理.
	public static final class Bool {
		private final AtomicInteger ato = new AtomicInteger(0);
		public Bool() {}
		public Bool(final boolean n) {
			ato.set(n ? 1 : 0);
		}
		public final boolean get() {
			return ato.get() == 1;
		}
		public final void set(final boolean n) {
			while (!ato.compareAndSet(ato.get(), n ? 1 : 0));
		}
		public final boolean setToGetBefore(final boolean n) {
			int ret;
			while (!ato.compareAndSet((ret = ato.get()), n ? 1 : 0));
			return ret == 1;
		}
	}
	// オブジェクト管理.
	public static final class Value<T> {
		private final AtomicReference<Object> ato = new AtomicReference<Object>();
		public Value() {}
		public Value(final T n) {
			ato.set(n);
		}
		public final T get() {
			return (T)ato.get();
		}
		public final void set(final T n) {
			while (!ato.compareAndSet(ato.get(), n));
		}
		public final T put(final Object n) {
			Object ret;
			while (!ato.compareAndSet((ret = ato.get()), n));
			return (T)ret;
		}
	}
	// ３２ビット数値管理.
	public static final class Number32 {
		private final AtomicInteger ato = new AtomicInteger(0);
		public Number32() {}
		public Number32(final int n) {
			ato.set(n);
		}
		public final int get() {
			return ato.get();
		}
		public final void set(final int n) {
			while (!ato.compareAndSet(ato.get(), n));
		}
		public final int put(final int n) {
			int ret;
			while (!ato.compareAndSet((ret = ato.get()), n));
			return ret;
		}
		public final int inc() {
			int n, r;
			while (!ato.compareAndSet((n = ato.get()), (r = n + 1)));
			return r;
		}
		public final int dec() {
			int n, r;
			while (!ato.compareAndSet((n = ato.get()), (r = n - 1)));
			return r;
		}
		public int add(int no) {
			int n, r;
			while (!ato.compareAndSet((n = ato.get()), (r = n + no)));
			return r;
		}
		public int remove(int no) {
			int n, r;
			while (!ato.compareAndSet((n = ato.get()), (r = n - no)));
			return r;
		}
		public boolean compareAndSet(int a, int b) {
			return ato.compareAndSet(a, b);
		}
	}
	// 64ビット数値管理.
	public static final class Number64 {
		private final AtomicLong ato = new AtomicLong(0);
		public Number64() {}
		public Number64(final long n) {
			ato.set(n);
		}
		public final long get() {
			return ato.get();
		}
		public final void set(final long n) {
			while (!ato.compareAndSet(ato.get(), n));
		}
		public final long put(final long n) {
			long ret;
			while (!ato.compareAndSet((ret = ato.get()), n));
			return ret;
		}
		public final long inc() {
			long n, r;
			while (!ato.compareAndSet((n = ato.get()), (r = n + 1)));
			return r;
		}
		public final long dec() {
			long n, r;
			while (!ato.compareAndSet((n = ato.get()), (r = n - 1)));
			return r;
		}
		public long add(long no) {
			long n, r;
			while (!ato.compareAndSet((n = ato.get()), (r = n + no)));
			return r;
		}
		public long remove(long no) {
			long n, r;
			while (!ato.compareAndSet((n = ato.get()), (r = n - no)));
			return r;
		}
		public boolean compareAndSet(long a, long b) {
			return ato.compareAndSet(a, b);
		}
	}
}
