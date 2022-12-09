package quina.util.collection.js;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * [graalvm専用].
 * JavaのオブジェクトをJavascriptで利用するラッパー.
 */
public final class JsWrapperUtil {
	private JsWrapperUtil() {}
	
	/**
	 * 取得時に対してJs向けの変換を行う.
	 * @param o オブジェクトを設定します.
	 * @return Object オブジェクトが返却されます.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Object convertJsProxy(Object o) {
		if(o == null) {
			return null;
		} else if(o instanceof Map) {
			return new JsWrapperMap((Map)o);
		} else if(o instanceof List) {
			return new JsWrapperList((List)o);
		} else if(o instanceof LocalDate) {
			return new JsWrapperDate((LocalDate)o);
		} else if(o instanceof java.util.Date) {
			return new JsWrapperDate((java.util.Date)o);
		} else if(o.getClass().isArray()) {
			return new JsWrapperArray(o);
		}
		return o;
	}

}
