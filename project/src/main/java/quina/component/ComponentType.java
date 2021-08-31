package quina.component;

import static quina.component.ComponentConstants.*;

/**
 * コンポーネントタイプを取得.
 */
public enum ComponentType {
	/** Anyコンポーネント. **/
	NORMAL(ATTRIBUTE_NORMAL, ATTRIBUTE_NORMAL, "normal"),

	/** [同期]Anyコンポーネント. **/
	Sync(ATTRIBUTE_SYNC, ATTRIBUTE_SYNC, "Sync"),

	/** RESTful 用コンポーネント. **/
	RESTful(ATTRIBUTE_RESTFUL, ATTRIBUTE_RESTFUL, "RESTful"),

	/** RESTful GetMethod用コンポーネント. **/
	RESTfulGet(ATTRIBUTE_RESTFUL, ATTRIBUTE_RESTFUL + 1, "RESTfulGet"),

	/** [同期]RESTful GetMethod用コンポーネント. **/
	RESTfulGetSync(ATTRIBUTE_SYNC, ATTRIBUTE_SYNC + ATTRIBUTE_RESTFUL + 1,
		"RESTfulGetSync"),

	/** RESTful PostMethod用コンポーネント. **/
	RESTfulPost(ATTRIBUTE_RESTFUL, ATTRIBUTE_RESTFUL + 2, "RESTfulPost"),

	/** [同期]RESTful PostMethod用コンポーネント. **/
	RESTfulPostSync(ATTRIBUTE_SYNC, ATTRIBUTE_SYNC + ATTRIBUTE_RESTFUL + 2,
		"RESTfulPostSync"),

	/** RESTful DeleteMethod用コンポーネント. **/
	RESTfulDelete(ATTRIBUTE_RESTFUL, ATTRIBUTE_RESTFUL + 3, "RESTfulDelete"),

	/** [同期]RESTful DeleteMethod用コンポーネント. **/
	RESTfulDeleteSync(ATTRIBUTE_SYNC, ATTRIBUTE_SYNC + ATTRIBUTE_RESTFUL + 3,
		"RESTfulDeleteSync"),

	/** RESTful PutMethod用コンポーネント. **/
	RESTfulPut(ATTRIBUTE_RESTFUL, ATTRIBUTE_RESTFUL + 4, "RESTfulPut"),

	/** [同期]RESTful PutMethod用コンポーネント. **/
	RESTfulPutSync(ATTRIBUTE_SYNC, ATTRIBUTE_SYNC + ATTRIBUTE_RESTFUL + 4,
		"RESTfulPutSync"),

	/** RESTful PatchMethod用コンポーネント. **/
	RESTfulPatch(ATTRIBUTE_RESTFUL, ATTRIBUTE_RESTFUL + 5, "RESTfulPatch"),

	/** [同期]RESTful PatchMethod用コンポーネント. **/
	RESTfulPatchSync(ATTRIBUTE_SYNC, ATTRIBUTE_SYNC + ATTRIBUTE_RESTFUL + 5,
		"RESTfulPatchSync"),

	/** ファイルコンポーネント. **/
	FILE(ATTRIBUTE_FILE, ATTRIBUTE_FILE, "file"),

	/** エラーコンポーネント. **/
	ERROR(ATTRIBUTE_ERROR, ATTRIBUTE_ERROR, "error");

	private int attributeType;
	private int type;
	private String name;

	/**
	 * コンストラクタ.
	 * @param attributeType コンポーネント属性を設定します.
	 * @param type コンポーネントタイプを設定します.
	 * @param name 名前を設定します.
	 */
	private ComponentType(int attributeType, int type, String name) {
		this.attributeType = attributeType;
		this.type = type;
		this.name = name;
	}

	/**
	 * コンポーネント属性番号を取得.
	 * @return int コンポーネント属性番号が返却されます.
	 */
	public int getAttributeType() {
		return attributeType;
	}

	/**
	 * コンポーネントタイプ番号を取得.
	 * @return int コンポーネントタイプ番号が返却されます.
	 */
	public int getType() {
		return type;
	}

	/**
	 * コンポーネントタイプ名を取得.
	 * @return String コンポーネントタイプ名を返却します.
	 */
	public String getName() {
		return name;
	}

	/**
	 * RESTful系のコンポーネントタイプかチェック.
	 * @return boolean trueの場合はRESTful系のコンポーネントです.
	 */
	public boolean isRESTful() {
		return (type & ATTRIBUTE_RESTFUL) == ATTRIBUTE_RESTFUL;
	}

	/**
	 * 同期系のコンポーネントタイプかチェック.
	 * @return boolean trueの場合は同期系のコンポーネントです.
	 */
	public boolean isSync() {
		return (type & ATTRIBUTE_SYNC) == ATTRIBUTE_SYNC;
	}

	/**
	 * コンポーネントタイプの属性一致をチェックします.
	 * @param ctype チェック対象のコンポーネントタイプを設定します.
	 * @return int 一致した属性を返却します.
	 *             [-1]が返却された場合は一致しませんでした.
	 */
	public int getAttributeMatch(ComponentType ctype) {
		if(attributeType == ctype.attributeType) {
			return attributeType;
		}
		// 一致しない場合.
		return -1;
	}
}
