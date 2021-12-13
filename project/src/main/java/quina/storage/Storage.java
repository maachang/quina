package quina.storage;

import java.util.Date;

import quina.util.collection.TypesClass;

/**
 * ストレージ情報.
 */
public interface Storage {
	
	/**
	 * 最終アクセス時間を取得.
	 * @return long 最終アクセス時間が返却されます.
	 */
	public long getUpdateTime();
	
	/**
	 * クリアー.
	 */
	public void clear();
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public Storage set(String key, Boolean value);
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public Storage set(String key, Byte value);
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public Storage set(String key, Short value);
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public Storage set(String key, Integer value);
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public Storage set(String key, Long value);
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public Storage set(String key, Float value);
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public Storage set(String key, Double value);
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public Storage set(String key, String value);
	
	/**
	 * アイテムをセット
	 * @param key キー名を設定します.
	 * @param value セット対象の要素を設定します.
	 * @return Storage Storageオブジェクトが返却されます.
	 */
	public Storage set(String key, Date value);
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Boolean 要素が返却されます.
	 */
	public Boolean getBoolean(String key);
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Byte 要素が返却されます.
	 */
	public Byte getByte(String key);
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Short 要素が返却されます.
	 */
	public Short getShort(String key);
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Integer 要素が返却されます.
	 */
	public Integer getInteger(String key);
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Long 要素が返却されます.
	 */
	public Long getLong(String key);
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Float 要素が返却されます.
	 */
	public Float getFloat(String key);
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Double 要素が返却されます.
	 */
	public Double getDouble(String key);

	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return String 要素が返却されます.
	 */
	public String getString(String key);
	
	/**
	 * アイテムを取得
	 * @param key キー名を設定します.
	 * @return Date 要素が返却されます.
	 */
	public Date getDate(String key);
	
	/**
	 * 要素のタイプを取得.
	 * @param key キー名を設定します.
	 * @return TypesClass 要素のタイプが返却されます.
	 *                    TypesClass.Objectの場合Storageです.
	 */
	public TypesClass getType(String key);
	
	/**
	 * 要素存在確認.
	 * @param key キー名を設定します.
	 * @return boolean trueの場合、存在します.
	 */
	public boolean contains(String key);
	
	/**
	 * 指定要素を削除.
	 * @param key キー名を設定します.
	 */
	public void remove(String key);
	
	/**
	 * 要素数を取得.
	 * @return
	 */
	public int size();
}
