package quina.annotation.quina;

import quina.CdiManager;
import quina.Quina;
import quina.annotation.AnnotationUtil;
import quina.annotation.Switch;
import quina.annotation.cdi.CdiScoped;
import quina.annotation.cdi.AnnotationCdi;
import quina.annotation.log.AnnotationLog;
import quina.exception.QuinaException;
import quina.http.MimeTypes;

/**
 * Quina.init で処理される annotationの読み込み処理を
 * 行います.
 */
public class AnnotationQuina {
	private AnnotationQuina() {}
	
	/**
	 * Annotationで定義されてるSystemPropertyを読み込んで
	 * System.setProperty()を設定します.
	 * @param c 対象のObjectを設定します.
	 * @return boolean 正しく読み込まれた場合 true が返却されます.
	 */
	public static final boolean loadSystemProperty(Object c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		return loadSystemProperty(c.getClass());
	}
	
	/**
	 * Annotationで定義されてるSystemPropertyを読み込んで
	 * System.setProperty()を設定します.
	 * @param c 対象のクラスを設定します.
	 * @return boolean 正しく読み込まれた場合 true が返却されます.
	 */
	public static final boolean loadSystemProperty(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		// 対象コンポーネントからSystemPropertyArrayアノテーション定義を取得.
		SystemPropertyArray array = c.getAnnotation(
			SystemPropertyArray.class);
		// 存在しない場合.
		if(array == null) {
			// 単体で取得.
			SystemProperty p = c.getAnnotation(SystemProperty.class);
			if(p != null) {
				// システムプロパティを設定.
				System.setProperty(p.key(), p.value());
				return true;
			}
			return false;
		}
		// 複数のSystemPropertyアノテーション定義を取得.
		SystemProperty[] list = array.value();
		// 存在しない場合.
		if(list == null || list.length == 0) {
			return false;
		}
		// System.setPropertyの登録を実行.
		int len = list.length;
		for(int i = 0; i < len; i ++) {
			// システムプロパティを設定.
			System.setProperty(
				list[i].key(), list[i].value());
		}
		return len > 0;
	}
	
	/**
	 * Annotationに定義されてるConfigDirectoryのパスを取得.
	 * @param c 対象のObjectを設定します.
	 * @return String Configディレクトリのパスが返却されます.
	 *                nullの場合、Configディレクトリのパスが設定されていません.
	 */
	public static final String loadConfigDirectory(Object c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		return loadConfigDirectory(c.getClass());
	}
	
	/**
	 * Annotationに定義されてるConfigDirectoryのパスを取得.
	 * @param c コンポーネントクラスを設定します.
	 * @return String Configディレクトリのパスが返却されます.
	 *                nullの場合、Configディレクトリのパスが設定されていません.
	 */
	public static final String loadConfigDirectory(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		ConfigDirectory configDir = c.getAnnotation(ConfigDirectory.class);
		if(configDir == null) {
			return null;
		}
		// 存在しない場合はnull返却.
		String s = configDir.value();
		if(s == null || s.isEmpty() || (s = s.trim()).isEmpty()) {
			return null;
		}
		// ￥パスを / パスに変換して、最後の / が存在する場合削除.
		if((s = AnnotationUtil.slashPath(s)).endsWith("/")) {
			return s.substring(0, s.length() - 1);
		}
		return s;
	}
	
	/**
	 * Annotationで定義されてるAppendMimeTypeを読み込んで
	 * MimeTypesに設定します.
	 * @param c 対象のObjectを設定します.
	 * @return boolean 正しく読み込まれた場合 true が返却されます.
	 */
	public static final boolean loadAppendMimeType(Object c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		return loadAppendMimeType(c.getClass());
	}
	
	/**
	 * Annotationで定義されてるAppendMimeTypeを読み込んで
	 * MimeTypesに設定します.
	 * @param c 対象のクラスを設定します.
	 * @return boolean 正しく読み込まれた場合 true が返却されます.
	 */
	public static final boolean loadAppendMimeType(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		// MimeTypes を取得.
		MimeTypes mime = MimeTypes.getInstance();
		// 対象コンポーネントからAppendMimeTypeArrayアノテーション定義を取得.
		AppendMimeTypeArray array = c.getAnnotation(
				AppendMimeTypeArray.class);
		// 存在しない場合.
		if(array == null) {
			// 単体で取得.
			AppendMimeType m = c.getAnnotation(AppendMimeType.class);
			if(m != null) {
				// MimeTypeに追加.
				setMimeTypes(mime, m);
				return true;
			}
			return false;
		}
		// 複数のAppendMimeTypeアノテーション定義を取得.
		AppendMimeType[] list = array.value();
		// 存在しない場合.
		if(list == null || list.length == 0) {
			return false;
		}
		// System.setPropertyの登録を実行.
		int len = list.length;
		for(int i = 0; i < len; i ++) {
			// MimeTypeに追加.
			setMimeTypes(mime, list[i]);
		}
		return len > 0;
	}
	
	// AppendMimeTypeをMimeTypesに反映.
	private static final void setMimeTypes(MimeTypes mime, AppendMimeType type) {
		if(type.charset() == Switch.None) {
			mime.put(type.extension(), type.mimeType());
		} else {
			mime.put(type.extension(), type.mimeType(), type.charset().getMode());
		}
	}
	
	/**
	 * AnnotationのCdi定義を反映されます.
	 * @param o オブジェクトを設定します.
	 */
	public static final void loadCdi(Object o) {
		if(o == null) {
			throw new QuinaException("The specified component is Null.");
		}
		loadCdi(Quina.get().getCdiManager(), o, o.getClass());
	}
	
	/**
	 * AnnotationのCdi定義を反映されます.
	 * @param c コンポーネントクラスを設定します.
	 */
	public static final void loadCdi(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		loadCdi(Quina.get().getCdiManager(), null, c);
	}

	
	/**
	 * AnnotationのCdi定義を反映されます.
	 * @param man Cdiマネージャを設定します.
	 * @param o オブジェクトを設定します.
	 */
	public static final void loadCdi(CdiManager man, Object o) {
		if(o == null) {
			throw new QuinaException("The specified component is Null.");
		}
		loadCdi(man, o, o.getClass());
	}
	
	/**
	 * AnnotationのCdi定義を反映されます.
	 * @param man Cdiマネージャを設定します.
	 * @param c コンポーネントクラスを設定します.
	 */
	public static final void loadCdi(CdiManager man, Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		loadCdi(man, null, c);
	}
	
	// AnnotationのCdi定義を反映されます.
	private static final void loadCdi(CdiManager man, Object o, Class<?> c) {
		// Objectが存在する場合.
		if(o != null) {
			// Cdiの条件を読み込む.
			AnnotationCdi.loadInject(man, o);
			// Logの条件を読み込む.
			AnnotationLog.loadLogDefine(o);
		// Objectが存在しない場合.
		} else {
			// Cdiの条件を読み込む.
			AnnotationCdi.loadInject(man, c);
			// Logの条件を読み込む.
			AnnotationLog.loadLogDefine(c);
		}
	}
	
	/**
	 * AnnotationにCdiScopedが定義されてるか取得.
	 * @param o オブジェクトを設定します.
	 * @return boolean true の場合 CdiScopedが定義されています.
	 */
	public static final boolean isCdiScoped(Object o) {
		if(o == null) {
			throw new QuinaException("The specified component is Null.");
		}
		return o.getClass().isAnnotationPresent(CdiScoped.class);
	}
	
	/**
	 * AnnotationにCdiScopedが定義されてるか取得.
	 * @param c コンポーネントクラスを設定します.
	 * @return boolean true の場合 CdiScopedが定義されています.
	 */
	public static final boolean isCdiScoped(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified component is Null.");
		}
		return c.isAnnotationPresent(CdiScoped.class);
	}
}
