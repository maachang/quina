package quina.component;

import static quina.component.ComponentConstants.ATTRIBUTE_ANY;
import static quina.component.ComponentConstants.ATTRIBUTE_ERROR;
import static quina.component.ComponentConstants.ATTRIBUTE_FILE;
import static quina.component.ComponentConstants.ATTRIBUTE_RESTFUL;
import static quina.component.ComponentConstants.ATTRIBUTE_SYNC;

import quina.exception.QuinaException;
import quina.http.Method;

/**
 * コンポーネントタイプを取得.
 */
public enum ComponentType {
	/** Anyコンポーネント. **/
	Any(ATTRIBUTE_ANY,
		ATTRIBUTE_ANY + ComponentConstants.HTTP_METHOD_ALL,
		"Any"),
	
	/** Any GetMethod用コンポーネント. **/
	AnyGet(ATTRIBUTE_ANY, ATTRIBUTE_ANY + Method.GET.getType(),
		"AnyGet"),

	/** Any PostMethod用コンポーネント. **/
	AnyPost(ATTRIBUTE_ANY, ATTRIBUTE_ANY + Method.POST.getType(),
		"AnyPost"),
	
	/** Any DeleteMethod用コンポーネント. **/
	AnyDelete(ATTRIBUTE_ANY, ATTRIBUTE_ANY + Method.DELETE.getType(),
		"AnyDelete"),

	/** Any PutMethod用コンポーネント. **/
	AnyPut(ATTRIBUTE_ANY, ATTRIBUTE_ANY + Method.PUT.getType(),
		"AnyPut"),
	
	/** Any PathMethod用コンポーネント. **/
	AnyPatch(ATTRIBUTE_ANY, ATTRIBUTE_ANY + Method.PATCH.getType(),
		"AnyPatch"),

	/** [同期]コンポーネント. **/
	Sync(ATTRIBUTE_SYNC,
		ATTRIBUTE_SYNC + ComponentConstants.HTTP_METHOD_ALL,
		"Sync"),
	
	/** [同期]GetMethod用コンポーネント. **/
	SyncGet(ATTRIBUTE_SYNC, ATTRIBUTE_SYNC + Method.GET.getType(),
		"SyncGet"),

	/** [同期]PostMethod用コンポーネント. **/
	SyncPost(ATTRIBUTE_SYNC, ATTRIBUTE_SYNC + Method.POST.getType(),
		"SyncPost"),
	
	/** [同期]DeleteMethod用コンポーネント. **/
	SyncDelete(ATTRIBUTE_SYNC, ATTRIBUTE_SYNC + Method.DELETE.getType(),
		"SyncDelete"),

	/** [同期]PutMethod用コンポーネント. **/
	SyncPut(ATTRIBUTE_SYNC, ATTRIBUTE_SYNC + Method.PUT.getType(),
		"SyncPut"),
	
	/** [同期]PathMethod用コンポーネント. **/
	SyncPatch(ATTRIBUTE_SYNC, ATTRIBUTE_SYNC + Method.PATCH.getType(),
		"SyncPatch"),

	/** RESTful 用コンポーネント. **/
	RESTful(ATTRIBUTE_RESTFUL,
		ATTRIBUTE_RESTFUL + ComponentConstants.HTTP_METHOD_ALL,
		"RESTful"),

	/** RESTful GetMethod用コンポーネント. **/
	RESTfulGet(ATTRIBUTE_RESTFUL, ATTRIBUTE_RESTFUL + Method.GET.getType(),
		"RESTfulGet"),

	/** [同期]RESTful GetMethod用コンポーネント. **/
	RESTfulGetSync(ATTRIBUTE_SYNC, 
		ATTRIBUTE_SYNC + ATTRIBUTE_RESTFUL + Method.GET.getType(),
		"RESTfulGetSync"),

	/** RESTful PostMethod用コンポーネント. **/
	RESTfulPost(ATTRIBUTE_RESTFUL, ATTRIBUTE_RESTFUL + Method.POST.getType(),
		"RESTfulPost"),

	/** [同期]RESTful PostMethod用コンポーネント. **/
	RESTfulPostSync(ATTRIBUTE_SYNC,
		ATTRIBUTE_SYNC + ATTRIBUTE_RESTFUL + Method.POST.getType(),
		"RESTfulPostSync"),

	/** RESTful DeleteMethod用コンポーネント. **/
	RESTfulDelete(ATTRIBUTE_RESTFUL, ATTRIBUTE_RESTFUL + Method.DELETE.getType(),
		"RESTfulDelete"),

	/** [同期]RESTful DeleteMethod用コンポーネント. **/
	RESTfulDeleteSync(ATTRIBUTE_SYNC,
		ATTRIBUTE_SYNC + ATTRIBUTE_RESTFUL + Method.DELETE.getType(),
		"RESTfulDeleteSync"),

	/** RESTful PutMethod用コンポーネント. **/
	RESTfulPut(ATTRIBUTE_RESTFUL, ATTRIBUTE_RESTFUL + Method.PUT.getType(),
		"RESTfulPut"),

	/** [同期]RESTful PutMethod用コンポーネント. **/
	RESTfulPutSync(ATTRIBUTE_SYNC,
		ATTRIBUTE_SYNC + ATTRIBUTE_RESTFUL + Method.PUT.getType(),
		"RESTfulPutSync"),

	/** RESTful PatchMethod用コンポーネント. **/
	RESTfulPatch(ATTRIBUTE_RESTFUL, ATTRIBUTE_RESTFUL + Method.PATCH.getType(),
		"RESTfulPatch"),

	/** [同期]RESTful PatchMethod用コンポーネント. **/
	RESTfulPatchSync(ATTRIBUTE_SYNC,
		ATTRIBUTE_SYNC + ATTRIBUTE_RESTFUL + Method.PATCH.getType(),
		"RESTfulPatchSync"),

	/** ファイルコンポーネント. **/
	File(ATTRIBUTE_FILE, ATTRIBUTE_FILE, "File"),

	/** エラーコンポーネント. **/
	Error(ATTRIBUTE_ERROR, ATTRIBUTE_ERROR, "Error"),
	
	/** [同期]エラーコンポーネント. **/
	ErrorSync(ATTRIBUTE_ERROR, ATTRIBUTE_SYNC + ATTRIBUTE_ERROR,
		"ErrorSync")
	;

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
	 * 対象のHttpメソッドを取得.
	 * @return Method Httpメソッドが返却されます.
	 */
	public Method getMethod() {
		int methodType = type & ComponentConstants.HTTP_METHOD_ALL;
		if(methodType == ComponentConstants.HTTP_METHOD_ALL) {
			return Method.All;
		} else if(methodType == ComponentConstants.HTTP_METHOD_GET) {
			return Method.GET;
		} else if(methodType == ComponentConstants.HTTP_METHOD_POST) {
			return Method.POST;
		} else if(methodType == ComponentConstants.HTTP_METHOD_DELETE) {
			return Method.DELETE;
		} else if(methodType == ComponentConstants.HTTP_METHOD_PUT) {
			return Method.PUT;
		} else if(methodType == ComponentConstants.HTTP_METHOD_PATCH) {
			return Method.PATCH;
		}
		throw new QuinaException(
			"Failed to get Http method information: " + methodType);
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
