package quina.http;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.util.collection.IndexKeyValueList;
import quina.util.collection.ObjectList;
import quina.util.collection.TreeKey;

/**
 * Http受信時のヘッダ管理用オブジェクト.
 *
 * サーバモードの場合はrequest時のヘッダ情報.
 *
 * クライアントモードの場合はresponse時のヘッダ情報.
 *
 * 内部的には「HttpIndexHeaders」の固定Httpヘッダを管理し
 * put及びremove時の条件をIndexMapで管理します.
 */
public class HttpReceiveHeader implements Header {

	// 追加条件.
	private static final int MODE_PUT = 1;
	// 削除条件.
	private static final int MODE_REMOVE = 2;

	// 追加・削除用のHeaderValue.
	private static final class PutDeleteValue {
		private final int mode;
		private final String value;

		public PutDeleteValue(int mode, String value) {
			this.mode = mode;
			this.value = value;
		}

		public int getMode() {
			return mode;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return new StringBuilder("mode: ")
				.append((mode == MODE_PUT ? "PUT" : "REMOVE"))
				.append(", value: ")
				.append(value).toString();
		}
	}

	// PutDelete用ヘッダ.
	private IndexKeyValueList<Object, PutDeleteValue> putRemoveHeader;

	// HttpIndexHeader.
	private HttpIndexHeaders httpIndexHeaders;

	// 現在の有効なキー情報群.
	private String[] keys = null;
	
	// Read-Writeロックオブジェクト.
	private final ReentrantReadWriteLock lock =
		new ReentrantReadWriteLock();

	/**
	 * コンストラクタ.
	 * @param h
	 */
	public HttpReceiveHeader(HttpIndexHeaders h) {
		httpIndexHeaders = h;
	}

	/**
	 * コンストラクタ.
	 * @param headerBin ヘッダ情報のバイナリを設定します.
	 */
	public HttpReceiveHeader(final byte[] headerBin) {
		/**
		 * バイナリの設定範囲は、以下のように行う必要があります.
		 * --------------------------------------------
		 * Location: https://www.google.com/\r\n
		 * Content-Type: text/html; charset=UTF-8\r\n
		 * Date: Mon, 12 Apr 2021 08:02:34 GMT\r\n
		 * Expires: Wed, 12 May 2021 08:02:34 GMT\r\n
		 * Cache-Control: public, max-age=2592000\r\n
		 * Server: gws\r\n
		 * Content-Length: 220\r\n
		 * Connection: close\r\n
		 * --------------------------------------------
		 * key: value[\r\n]まで設定することでヘッダ行が認識されます.
		 */
		httpIndexHeaders = new HttpIndexHeaders(headerBin);
	}

	/**
	 * コンストラクタ.
	 * @param headerBin ヘッダ情報のバイナリを設定します.
	 * @param off 対象のオフセット値を設定します.
	 * @param len 対象の長さを設定します.
	 */
	public HttpReceiveHeader(final byte[] headerBin, int off, int len) {
		byte[] h = new byte[len];
		System.arraycopy(headerBin, off, h, 0, len);
		httpIndexHeaders = new HttpIndexHeaders(h);
	}

	@Override
	public void clear() {
		lock.writeLock().lock();
		try {
			// httpIndexHeadersの内容を削除としてputRemoveHeaderに登録する.
			putRemoveHeader = new IndexKeyValueList<Object, PutDeleteValue>();
			int len = httpIndexHeaders.size();
			for(int i = 0; i < len; i ++) {
				putRemoveHeader.put(new TreeKey(httpIndexHeaders.getKey(i)),
					new PutDeleteValue(MODE_REMOVE, null));
			}
			// 有効なキー情報を初期化.
			keys = null;
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public String put(String key, String value) {
		if(key == null || value == null) {
			return null;
		}
		String ret;
		lock.writeLock().lock();
		try {
			// put及びremove条件が存在しない場合.
			if(putRemoveHeader == null) {
				putRemoveHeader = new IndexKeyValueList
					<Object, PutDeleteValue>();
				ret = httpIndexHeaders.get(key);
				putRemoveHeader.put(
					new TreeKey((String)key),
					new PutDeleteValue(MODE_PUT, value));
				// 有効なキー情報を初期化.
				keys = null;
			// put及びremove条件の条件とhttpIndexHeadersで処理する.
			} else {
				PutDeleteValue val = null;
				val = putRemoveHeader.get(key);
				// put及びremove条件の条件が存在する場合.
				if(val != null) {
					ret = val.getValue();
				// 存在しない場合はhttpIndexHeaders内容を返却値とする.
				} else {
					ret = httpIndexHeaders.get(key);
				}
				val = new PutDeleteValue(MODE_PUT, value);
				putRemoveHeader.put(new TreeKey((String)key), val);
				// 有効なキー情報を初期化.
				keys = null;
			}
		} finally {
			lock.writeLock().unlock();
		}
		return ret;
	}

	@Override
	public String remove(Object key) {
		if(key == null) {
			return null;
		}
		lock.writeLock().lock();
		try {
			// put及びremove条件が存在しない場合.
			if(putRemoveHeader == null) {
				// httpIndexHeadersにキー情報が存在しない場合.
				if(!httpIndexHeaders.contains((String)key)) {
					return null;
				}
				// 削除情報をセット.
				putRemoveHeader = new IndexKeyValueList<Object, PutDeleteValue>();
				putRemoveHeader.put(
					new TreeKey((String)key),
					new PutDeleteValue(MODE_REMOVE, null)
					);
				// 有効なキー情報を初期化.
				keys = null;
				return httpIndexHeaders.get((String)key);
			}
			// put及びremove条件から今回のキー情報を取得.
			PutDeleteValue val = putRemoveHeader.get(key);
			if(val != null) {
				// put条件が存在する場合は削除.
				if(val.getMode() == MODE_PUT) {
					putRemoveHeader.remove(key);
					// httpIndexHeadersに存在する場合.
					if(httpIndexHeaders.contains((String)key)) {
						// 削除条件を登録.
						putRemoveHeader.put(
							new TreeKey((String)key),
							new PutDeleteValue(MODE_REMOVE, null)
							);
					}
					// 有効なキー情報を初期化.
					keys = null;
					// put及びremove条件内容が０件の場合.
					if(putRemoveHeader.size() == 0) {
						// クリア.
						putRemoveHeader = null;
					}
					return val.getValue();
				// 削除条件が既に存在する場合は処理しない.
				} else if(val.getMode() == MODE_REMOVE) {
					return null;
				}
			}
			// httpIndexHeadersにキー情報が存在しない場合.
			if(!httpIndexHeaders.contains((String)key)) {
				return null;
			}
			// 削除条件を登録.
			putRemoveHeader.put(
				new TreeKey((String)key),
				new PutDeleteValue(MODE_REMOVE, null)
				);
			// 有効なキー情報を初期化.
			keys = null;
			return httpIndexHeaders.get((String)key);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void putAll(Map<? extends String, ? extends String> m) {
		lock.writeLock().lock();
		try {
			if(m == null || m.size() == 0) {
				return;
			} else if(putRemoveHeader == null) {
				putRemoveHeader = new IndexKeyValueList
					<Object, PutDeleteValue>();
			}
			Entry<String, String> e;
			Iterator<?> itr = m.entrySet().iterator();
			while(itr.hasNext()) {
				e = (Entry)itr.next();
				putRemoveHeader.put(
					new TreeKey(e.getKey()),
					new PutDeleteValue(MODE_PUT, e.getValue()));
			}
			// 有効なキー情報を初期化.
			keys = null;
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public boolean containsKey(Object key) {
		if(key == null) {
			return false;
		}
		lock.readLock().lock();
		try {
			// put及び削除条件が存在する場合.
			if(putRemoveHeader != null) {
				final PutDeleteValue val = putRemoveHeader.get(key);
				if(val != null) {
					// putの場合は存在を示す.
					if(val.getMode() == MODE_PUT) {
						return true;
					// remove条件は存在しない.
					} else if(val.getMode() == MODE_REMOVE) {
						return false;
					}
				}
			}
			// httpIndexHeadersから取得.
			return httpIndexHeaders.contains((String)key);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String get(Object key) {
		if(key == null) {
			return null;
		}
		lock.readLock().lock();
		try {
			// put及び削除条件が存在する場合.
			if(putRemoveHeader != null) {
				final PutDeleteValue val = putRemoveHeader.get(key);
				if(val != null) {
					// putの場合はその条件を取得.
					if(val.getMode() == MODE_PUT) {
						return val.getValue();
					// remove条件はnull返却.
					} else if(val.getMode() == MODE_REMOVE) {
						return null;
					}
				}
			}
			// httpIndexHeadersから取得.
			return httpIndexHeaders.get((String)key);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * 現在の有効なキー情報を取得.
	 * ただしput及びremove条件が存在しない場合は有効なキーは作成されない.
	 */
	private void _useKeys() {
		// 有効なキー情報を再作成する必要はない場合.
		if(keys != null || putRemoveHeader == null) {
			return;
		}
		PutDeleteValue val;
		ObjectList<String> list = new ObjectList<String>(
			httpIndexHeaders.size() +
			(putRemoveHeader == null ? 0 : putRemoveHeader.size()));
		// put及びremove条件が存在する場合.
		if(putRemoveHeader != null) {
			int len = putRemoveHeader.size();
			for(int i = 0; i < len; i ++) {
				val = putRemoveHeader.valueAt(i);
				// putのみを対象として有効なキーとする.
				if(val.getMode() == MODE_PUT) {
					list.add(((TreeKey)putRemoveHeader.keyAt(i)).getKey());
				}
			}
		}
		// httpIndexHeaders条件のうちput及びremove条件にマッチしないものを登録.
		String key;
		int len = httpIndexHeaders.size();
		for(int i = 0; i < len; i ++) {
			key = httpIndexHeaders.getKey(i);
			if(!putRemoveHeader.containsKey(key)) {
				list.add(key);
			}
		}
		len = list.size();
		keys = new String[len];
		for(int i = 0; i < len; i ++) {
			keys[i] = list.get(i);
		}
	}

	@Override
	public String getValue(int no) {
		lock.writeLock().lock();
		try {
			_useKeys();
			if(keys == null) {
				return httpIndexHeaders.getValue(no);
			}
			return get(keys[no]);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public String getKey(int no) {
		lock.writeLock().lock();
		try {
			_useKeys();
			if(keys == null) {
				return httpIndexHeaders.getKey(no);
			}
			return keys[no];
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public int size() {
		lock.writeLock().lock();
		try {
			_useKeys();
			if(keys == null) {
				return httpIndexHeaders.size();
			}
			return keys.length;
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * HttpIndexHeadersを取得.
	 * @return HttpIndexHeaders HttpIndexHeadersを取得します.
	 */
	public HttpIndexHeaders getHttpIndexHeaders() {
		lock.readLock().lock();
		try {
			return httpIndexHeaders;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			StringBuilder buf = new StringBuilder();
			int len = size();
			for(int i = 0; i < len; i ++) {
				if(i != 0) {
					buf.append("\n");
				}
				buf.append(getKey(i)).append(": ").append(getValue(i));
			}
			return buf.toString();
		} finally {
			lock.readLock().unlock();
		}
	}
}
