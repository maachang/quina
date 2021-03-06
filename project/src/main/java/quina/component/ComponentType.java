package quina.component;

import static quina.component.ComponentConstants.*;

/**
 * コンポーネントタイプを取得.
 */
public enum ComponentType {
	/** 通常コンポーネント. **/
	NORMAL(TYPE_NORMAL, "normal"),

	/** 同期用コンポーネント. **/
	Sync(TYPE_SYNC, "Sync"),

	/** RESTful 用コンポーネント. **/
	RESTful(TYPE_RESTFUL, "RESTful"),

	/** RESTful GetMethod用コンポーネント. **/
	RESTfulGet(TYPE_RESTFUL + 1, "RESTfulGet"),

	/** [同期]RESTful GetMethod用コンポーネント. **/
	RESTfulGetSync(TYPE_SYNC + TYPE_RESTFUL + 1, "RESTfulGetSync"),

	/** RESTful PostMethod用コンポーネント. **/
	RESTfulPost(TYPE_RESTFUL + 2, "RESTfulPost"),

	/** [同期]RESTful PostMethod用コンポーネント. **/
	RESTfulPostSync(TYPE_SYNC + TYPE_RESTFUL + 2, "RESTfulPostSync"),

	/** RESTful DeleteMethod用コンポーネント. **/
	RESTfulDelete(TYPE_RESTFUL + 3, "RESTfulDelete"),

	/** [同期]RESTful DeleteMethod用コンポーネント. **/
	RESTfulDeleteSync(TYPE_SYNC + TYPE_RESTFUL + 3, "RESTfulDeleteSync"),

	/** RESTful PutMethod用コンポーネント. **/
	RESTfulPut(TYPE_RESTFUL + 4, "RESTfulPut"),

	/** [同期]RESTful PutMethod用コンポーネント. **/
	RESTfulPutSync(TYPE_SYNC + TYPE_RESTFUL + 4, "RESTfulPutSync"),

	/** RESTful PatchMethod用コンポーネント. **/
	RESTfulPatch(TYPE_RESTFUL + 5, "RESTfulPatch"),

	/** [同期]RESTful PatchMethod用コンポーネント. **/
	RESTfulPatchSync(TYPE_SYNC + TYPE_RESTFUL + 5, "RESTfulPatchSync"),

	/** ファイルコンポーネント. **/
	FILE(TYPE_FILE, "file"),

	/** エラーコンポーネント. **/
	ERROR(TYPE_ERROR, "error");

	private int type;
	private String name;

	private ComponentType(int type, String name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * コンポーネントタイプ番号を取得.
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * コンポーネント名を取得.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * RESTful系のコンポーネントタイプかチェック.
	 * @return boolean trueの場合はRESTful系のコンポーネントです.
	 */
	public boolean isRESTful() {
		return (type & TYPE_RESTFUL) == TYPE_RESTFUL;
	}

	/**
	 * 同期系のコンポーネントタイプかチェック.
	 * @return boolean trueの場合は同期系のコンポーネントです.
	 */
	public boolean isSync() {
		return (type & TYPE_SYNC) == TYPE_SYNC;
	}
}
