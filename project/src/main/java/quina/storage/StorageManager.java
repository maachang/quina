package quina.storage;

/**
 * StorageManager.
 */
public interface StorageManager {
	/**
	 * 新しいStorageを作成.
	 * @param name Storage名を設定します.
	 * @return Storage 新しいStorageが返却されます.
	 */
	public Storage createStorage(String name);
	
	/**
	 * 指定Storage名のStorageを削除.
	 * @param name Storage名を設定します.
	 */
	public void removeStorage(String name);
	
	/**
	 * Storage名を指定してStorageを取得.
	 * @param name Storage名を設定します.
	 * @return Storage Storageが返却されます.
	 */
	public Storage getStorage(String name);
	
	/**
	 * 指定Storage名のStorageが存在するかチェック.
	 * @param name Storage名を設定します.
	 * @return boolean trueの場合、存在します.
	 */
	public boolean isStorage(String name);
	
	/**
	 * 登録Storage数を取得.
	 * @return int 登録Storage数が返却されます.
	 */
	public int size();
}
