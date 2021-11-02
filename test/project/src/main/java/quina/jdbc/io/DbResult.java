package quina.jdbc.io;

import java.io.Closeable;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import quina.exception.QuinaException;
import quina.jdbc.QuinaProxyResultSet;
import quina.json.JsonOut;
import quina.util.collection.AbstractEntryIterator;
import quina.util.collection.AbstractKeyIterator;
import quina.util.collection.TypesKeyValue;

/**
 * Result返却オブジェクト.
 */
public class DbResult implements
	Iterator<Map<String, Object>>, Closeable {
	private DbResult() {}
	
	// IoStatement.
	private AbstractIoStatement<?> ioStmt = null;
	// resultSet.
	private QuinaProxyResultSet rs = null;
	// keyIndex.
	private KeyIndex metaColumns = null;
	// metaNames.
	private String[] metaNames = null;
	// metaTypes.
	private int[] metaTypes = null;
	// ResultRow.
	private DbResultValue row = null;
	// 現在取得注のResultRow.
	private DbResultValue nowRow = null;
	
	// メタデータの中身を取得.
	private final void getMeta(final ResultSetMetaData data)
		throws SQLException {
		String s;
		final int len = data.getColumnCount();
		KeyIndex m = new KeyIndex(len);
		String[] n = new String[len];
		int[] t = new int[len];
		for(int i = 0; i < len; i ++) {
			s = data.getColumnName(i + 1);
			m.put(s, i);
			n[i] = s;
			t[i] = data.getColumnType(i + 1);
		}
		metaColumns = m.fix(); // カラム群.
		metaNames = n; // カラム名群.
		metaTypes = t; // タイプ群.
		
		// 出力オブジェクトを生成.
		row = new DbResultValue(this);
	}
	
	/**
	 * 行取得処理を生成.
	 * @param rs
	 * @param conn
	 * @param stmt
	 * @return
	 */
	protected static final DbResult create(
		final QuinaProxyResultSet rs,
		final AbstractIoStatement<?> ioStmt) {
		try {
			DbResult ret = new DbResult();
			ret.ioStmt = ioStmt;
			ret.rs = rs;
			ret.getMeta(rs.getMetaData());
			return ret;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
	
	/**
	 * クローズ.
	 */
	public void close() throws IOException {
		if(rs != null) {
			try {
				rs.close();
			} catch(Exception e) {}
			rs = null;
		}
		if(ioStmt != null) {
			AbstractIoStatement<?> stmt = ioStmt;
			ioStmt = null;
			stmt.close();
		}
		rs = null;
		row = null;
		nowRow = null;
		metaColumns = null;
		metaTypes = null;
	}
	
	/**
	 * クローズ済みか取得.
	 * @return boolean trueの場合クローズしています.
	 */
	public boolean isClose() {
		try {
			if(rs == null || ioStmt == null) {
				return true;
			} else if(ioStmt.isClosed()) {
				close();
				return true;
			}
			return false;
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
	
	// チェック処理.
	private void check() {
		if(isClose()) {
			throw new QuinaException(
				"DbResult is already closed.");
		}
	}

	@Override
	public boolean hasNext() {
		if(isClose()) {
			return false;
		} else if(nowRow == null && !_row()) {
			try {
				close();
			} catch(Exception e) {}
			return false;
		}
		return true;
	}

	@Override
	public Map<String, Object> next() {
		check();
		if(rs == null || (nowRow == null && !_row())) {
			try {
				close();
			} catch(Exception e) {}
			throw new NoSuchElementException();
		}
		final Map<String, Object> ret = nowRow;
		nowRow = null;
		return ret;
	}
	
	/**
	 * 全情報をListとして取得.
	 * @return List<Map<String, Object>> 全情報が返却されます.
	 */
	protected List<Map<String, Object>> getRows() {
		return getRows(-1);
	}
	
	/**
	 * 指定サイズの情報までをListとして取得.
	 * @param max 取得サイズを設定します.
	 *            -1を設定した場合、全体を取得します.
	 * @return List<Map<String, Object>> 取得情報が返却されます.
	 */
	protected List<Map<String, Object>> getRows(int limit) {
		check();
		limit = limit <= -1 ? -1 : limit;
		final List<Map<String, Object>> ret =
			new ArrayList<Map<String, Object>>();
		while(hasNext()) {
			if(limit != -1 && ret.size() >= limit) {
				break;
			}
			ret.add(new DbResultCopyValue(row));
			row = null;
		}
		return ret;
	}
	
	/**
	 * 文字列変換.
	 * @return String 文字列が返却されます.
	 */
	@Override
	public String toString() {
		return JsonOut.toString(this);
	}
	
	// 1行の情報を取得.
	private boolean _row() {
		nowRow = null;
		try {
			if(rs.next()) {
				nowRow = row;
				return true;
			}
			return false;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
	
	// KeyComparable.
	private static final class KeyComparable implements Comparable<Object> {
		protected String key;
		protected int no;
		
		// コンストラクタ.
		public KeyComparable(String key, int no) {
			this.key = key;
			this.no = no;
		}

		// comparteTo.
		@Override
		public int compareTo(Object o) {
			if(o instanceof String) {
				return key.compareTo((String)o);
			}
			return key.compareTo(((KeyComparable)o).key);
		}
	}
	
	// キーインデックス
	private static final class KeyIndex {
		private int pos;
		private KeyComparable[] list;
		
		// コンストラクタ.
		protected KeyIndex(int len) {
			pos = 0;
			list = new KeyComparable[len];
		}
		
		// データセット.
		public void put(String key, int no) {
			list[pos ++] = new KeyComparable(
				key.toLowerCase(), no);
		}
		
		// データセット終了.
		public KeyIndex fix() {
			Arrays.sort(list);
			return this;
		}
		
		// 検索.
		public int search(String key) {
			if(key == null) {
				return -1;
			}
			int no = Arrays.binarySearch(
				list, key.toLowerCase());
			if(no == -1) {
				return -1;
			}
			return list[no].no;
		}
		
		// データサイズを取得.
		public int size() {
			return list.length;
		}
		
		//キー情報を取得.
		public String keyAt(int no) {
			return list[no].key;
		}
	}
	
	// 1行のデータ.
	@SuppressWarnings("rawtypes")
	private static final class DbResultValue
		implements Map<String, Object>, AbstractKeyIterator.Base<String>,
		AbstractEntryIterator.Base<String, Object>, TypesKeyValue<String, Object> {
		private final DbResult parent;
		
		// コンストラクタ.
		protected DbResultValue(DbResult p) {
			this.parent = p;
		}
		
		@Override
		public void clear() {
		}
		
		@Override
		public Object put(String key, Object value) {
			return null;
		}
		
		@Override
		public boolean containsKey(Object key) {
			if (key == null) {
				return false;
			}
			return parent.metaColumns.search(
				key.toString()) != -1;
		}

		@Override
		public Object get(Object key) {
			if (key == null) {
				return null;
			}
			int no = parent.metaColumns.search(
				key.toString());
			if(no != -1) {
				try {
					return DbUtil.getResultColumn(
						parent.rs, parent.metaTypes[no], no + 1);
				} catch(Exception e) {
					throw new QuinaException(e);
				}
			}
			return null;
		}

		@Override
		public Object remove(Object key) {
			return null;
		}

		@Override
		public boolean isEmpty() {
			return parent.metaColumns.size() == 0;
		}

		@Override
		public void putAll(Map toMerge) {
		}

		@Override
		public boolean containsValue(Object value) {
			Object o;
			ResultSet rs = parent.rs;
			int[] metaTypes = parent.metaTypes;
			final int len = metaTypes.length;
			try {
				if (value == null) {
					for (int i = 0; i < len; i++) {
						o = DbUtil.getResultColumn(
							rs, metaTypes[i], i + 1);
						if(o == null) {
							return true;
						}
					}
				} else {
					for (int i = 0; i < len; i++) {
						o = DbUtil.getResultColumn(
							rs, metaTypes[i], i + 1);
						if (value.equals(o)) {
							return true;
						}
					}
				}
				return false;
			} catch(Exception e) {
				throw new QuinaException(e);
			}
		}

		@Override
		public int size() {
			return parent.metaTypes.length;
		}

		@Override
		public String toString() {
			return JsonOut.toString(this);
		}

		@Override
		public Collection<Object> values() {
			Object o;
			ResultSet rs = parent.rs;
			int[] metaTypes = parent.metaTypes;
			final int len = metaTypes.length;
			final ArrayList<Object> ret = new ArrayList<Object>(len);
			try {
				for (int i = 0; i < len; i++) {
					o = DbUtil.getResultColumn(rs, metaTypes[i], i + 1);
					ret.add(o);
				}
				return ret;
			} catch(Exception e) {
				throw new QuinaException(e);
			}
		}

		@Override
		public Set<String> keySet() {
			return new AbstractKeyIterator.Set<>(this);
		}

		@Override
		public Set<java.util.Map.Entry<String, Object>> entrySet() {
			return new AbstractEntryIterator.Set<>(this);
		}

		@Override
		public String getKey(int no) {
			return parent.metaNames[no];
		}

		@Override
		public Object getValue(int no) {
			try {
				return DbUtil.getResultColumn(
					parent.rs, parent.metaTypes[no], no + 1);
			} catch(Exception e) {
				throw new QuinaException(e);
			}
		}
	}
	
	// 1行のCopyデータ.
	@SuppressWarnings("rawtypes")
	private static final class DbResultCopyValue
		implements Map<String, Object>, AbstractKeyIterator.Base<String>,
		AbstractEntryIterator.Base<String, Object>, TypesKeyValue<String, Object> {
		private KeyIndex keyIndex;
		private Object[] values;
		
		// コンストラクタ.
		protected DbResultCopyValue(DbResultValue rv) {
			KeyIndex index = rv.parent.metaColumns;
			final int len = index.size();
			final Object[] vals = new Object[len];
			for(int i = 0; i < len; i ++) {
				vals[i] = rv.get(index.keyAt(i));
			}
			this.keyIndex = index;
			this.values = vals;
		}
		
		@Override
		public void clear() {
		}
		
		@Override
		public Object put(String key, Object value) {
			return null;
		}
		
		@Override
		public boolean containsKey(Object key) {
			if (key == null) {
				return false;
			}
			return keyIndex.search(
				key.toString()) != -1;
		}

		@Override
		public Object get(Object key) {
			if (key == null) {
				return null;
			}
			int no = keyIndex.search(
				key.toString());
			if(no != -1) {
				return values[no];
			}
			return null;
		}

		@Override
		public Object remove(Object key) {
			return null;
		}

		@Override
		public boolean isEmpty() {
			return keyIndex.size() == 0;
		}

		@Override
		public void putAll(Map toMerge) {
		}

		@Override
		public boolean containsValue(Object value) {
			final int len = values.length;
			if(value == null) {
				for(int i = 0; i < len; i ++) {
					if(values[i] == null) {
						return true;
					}
				}
			} else {
				for(int i = 0; i < len; i ++) {
					if(value.equals(values[i])) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public int size() {
			return values.length;
		}

		@Override
		public String toString() {
			return JsonOut.toString(this);
		}

		@Override
		public Collection<Object> values() {
			final int len = values.length;
			final ArrayList<Object> ret = new ArrayList<Object>(len);
			try {
				for (int i = 0; i < len; i++) {
					ret.add(values[i]);
				}
				return ret;
			} catch(Exception e) {
				throw new QuinaException(e);
			}
		}

		@Override
		public Set<String> keySet() {
			return new AbstractKeyIterator.Set<>(this);
		}

		@Override
		public Set<java.util.Map.Entry<String, Object>> entrySet() {
			return new AbstractEntryIterator.Set<>(this);
		}

		@Override
		public String getKey(int no) {
			return keyIndex.keyAt(no);
		}

		@Override
		public Object getValue(int no) {
			try {
				return values[no];
			} catch(Exception e) {
				throw new QuinaException(e);
			}
		}
	}
}
