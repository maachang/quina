package quina.component;

/**
 * コンポーネントタイプを取得.
 */
public enum ComponentType {
	/** 通常コンポーネント. **/
	NORMAL(0x00, "normal"),

	/** ファイルコンポーネント. **/
	FILE(0x01, "file"),

	/** RESTful 用コンポーネント. **/
	RESTful(0x10, "RESTful"),

	/** RESTful GetMethod用コンポーネント. **/
	RESTfulGet(0x11, "RESTfulGet"),

	/** RESTful PostMethod用コンポーネント. **/
	RESTfulPost(0x12, "RESTfulPost"),

	/** RESTful DeleteMethod用コンポーネント. **/
	RESTfulDelete(0x13, "RESTfulDelete"),

	/** RESTful PutMethod用コンポーネント. **/
	RESTfulPut(0x14, "RESTfulPut"),

	/** RESTful PatchMethod用コンポーネント. **/
	RESTfulPatch(0x15, "RESTfulPatch"),

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
}
