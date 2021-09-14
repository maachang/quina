package quina.annotation.cdi;

import quina.component.Component;
import quina.component.ErrorAttributeComponent;
import quina.exception.QuinaException;

/**
 * Cdi(Contexts and Dependency Injection)オブジェクトタイプ.
 */
public enum CdiObjectType {
	// 不明.
	Unknown("unknown", false),
	// コンポーネント.
	Component("component", true),
	// サービス.
	Service("service", true);
	
	// タイプ名.
	private String name;
	
	// 有効なCdiオブジェクトの場合 true.
	private boolean useCdiObject;
	
	// コンストラクタ.
	private CdiObjectType(String name, boolean useCdiObject) {
		this.name = name;
		this.useCdiObject = useCdiObject;
	}
	
	/**
	 * タイプ名を取得.
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 有効なCdiオブジェクトかチェック.
	 * @return boolean trueの場合、有効です.
	 */
	public boolean isCdiObject() {
		return useCdiObject;
	}
	
	/**
	 * 指定クラスから、Cdiタイプを取得.
	 * @param c
	 * @return
	 */
	public static final CdiObjectType getType(Object o) {
		if(o == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		if(o instanceof Component || o instanceof ErrorAttributeComponent) {
			return Component;
		} else if(LoadAnnotationCdi.loadServiceScoped(o)) {
			return Service;
		}
		return Unknown;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}

