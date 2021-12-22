package quina.storage;

import quina.Quina;
import quina.QuinaService;

/**
 * StorageQuinaService.
 */
public interface StorageQuinaService
	extends QuinaService {
	
	/**
	 * StorageManagerを取得.
	 * @return StorageManagerが返却されます.
	 */
	public StorageManager getStorageManager();
	
	/**
	 * StorageManagerを取得.
	 * @return StorageManager QuinaServiceで登録された
	 *         StorageManagerが返却されます.
	 */
	public static StorageManager get() {
		// 登録されているサービスを取得.
		QuinaService service = Quina.get()
			.getQuinaServiceManager()
			.get(StorageConstants.SERVICE_NAME);
		// 存在しないか対象サービスがStorageQuinaService
		// でない場合.
		if(service == null ||
			!(service instanceof StorageQuinaService)) {
			// null返却.
			return null;
		}
		// StorageManagerを返却.
		return ((StorageQuinaService)service).getStorageManager();
	}
}
