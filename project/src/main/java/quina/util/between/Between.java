package quina.util.between;

/**
 * 範囲情報.
 */
public interface Between<V> {
	
	/**
	 * 指定したDateが範囲内かチェック.
	 * @param o 範囲内か調べるDateを設定します.
	 * @return boolean trueの場合、範囲内です.
	 */
	public boolean between(Object o);
	
	/**
	 * 設定されたValueを取得します.
	 * @return V 対象のValueが返却.
	 */
	public V getValue();
}
