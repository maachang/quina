package quina.component;

/**
 * コンポーネントタイプを取得.
 */
public enum ComponentType {
	/** 通常コンポーネント. **/
	NORMAL(0x00, "normal"),

	/** ファイルコンポーネント. **/
	FILE(0x01, "file"),

	/** 同期用コンポーネント. **/
	Sync(0x10, "Sync"),

	/** RESTful 用コンポーネント. **/
	RESTful(0x20, "RESTful"),

	/** RESTful GetMethod用コンポーネント. **/
	RESTfulGet(0x21, "RESTfulGet"),

	/** RESTful PostMethod用コンポーネント. **/
	RESTfulPost(0x22, "RESTfulPost"),

	/** RESTful DeleteMethod用コンポーネント. **/
	RESTfulDelete(0x23, "RESTfulDelete"),

	/** RESTful PutMethod用コンポーネント. **/
	RESTfulPut(0x24, "RESTfulPut"),

	/** RESTful PatchMethod用コンポーネント. **/
	RESTfulPatch(0x25, "RESTfulPatch"),

	/** [同期]RESTful GetMethod用コンポーネント. **/
	RESTfulGetSync(0x31, "RESTfulGetSync"),

	/** [同期]RESTful PostMethod用コンポーネント. **/
	RESTfulPostSync(0x32, "RESTfulPostSync"),

	/** [同期]RESTful DeleteMethod用コンポーネント. **/
	RESTfulDeleteSync(0x33, "RESTfulDeleteSync"),

	/** [同期]RESTful PutMethod用コンポーネント. **/
	RESTfulPutSync(0x34, "RESTfulPutSync"),

	/** [同期]RESTful PatchMethod用コンポーネント. **/
	RESTfulPatchSync(0x35, "RESTfulPatchSync"),


	/** エラーコンポーネント. **/
	ERROR(0x8f, "error");

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
		return (type & RESTful.type) == RESTful.type;
	}

	/**
	 * 同期系のコンポーネントタイプかチェック.
	 * @return boolean trueの場合は同期系のコンポーネントです.
	 */
	public boolean isSync() {
		return (type & Sync.type) == Sync.type;
	}
}
