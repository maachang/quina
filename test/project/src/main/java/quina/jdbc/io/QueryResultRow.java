package quina.jdbc.io;

import java.util.Map;

import quina.util.collection.AbstractEntryIterator;
import quina.util.collection.AbstractKeyIterator;
import quina.util.collection.TypesKeyValue;

/**
 * QueryResultの１行データを表す.
 */
public interface QueryResultRow
	extends Map<String, Object>,
	AbstractKeyIterator.Base<String>,
	AbstractEntryIterator.Base<String, Object>,
	TypesKeyValue<String, Object> {
	
	/**
	 * オブジェクトのコピーを作成.
	 * @return QueryResultRow コピーされたオブジェクトが
	 *                     返却されます.
	 */
	public QueryResultRow getCopy();
}

