package quina.annotation.quina;

import quina.QuinaService;
import quina.annotation.AnnotationUtil;
import quina.annotation.Switch;
import quina.annotation.cdi.CdiHandleScoped;
import quina.annotation.cdi.CdiScoped;
import quina.exception.QuinaException;
import quina.http.MimeTypes;
import quina.util.Env;
import quina.util.FileUtil;

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
			throw new QuinaException("The specified argument is Null.");
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
			throw new QuinaException("The specified argument is Null.");
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
	 * 指定オブジェクトにConfigDirectoryが定義されてるかチェック.
	 * @param o 対象のObjectを設定します.
	 * @return boolean trueの場合定義されています.
	 */
	public static final boolean isConfigDirectory(Object o) {
		if(o == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return isConfigDirectory(o.getClass());
	}
	
	/**
	 * 指定クラスにConfigDirectoryが定義されてるかチェック.
	 * @param c 対象のClassを設定します.
	 * @return boolean trueの場合定義されています.
	 */
	public static final boolean isConfigDirectory(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return c.isAnnotationPresent(ConfigDirectory.class);
	}
	
	/**
	 * Annotationに定義されてるConfigDirectoryのパスを取得.
	 * @param o 対象のObjectを設定します.
	 * @return String Configディレクトリのパスが返却されます.
	 *                nullの場合、Configディレクトリのパスが設定されていません.
	 */
	public static final String loadConfigDirectory(Object o) {
		if(o == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return loadConfigDirectory(o.getClass());
	}
	
	/**
	 * Annotationに定義されてるConfigDirectoryのパスを取得.
	 * @param c コンポーネントクラスを設定します.
	 * @return String Configディレクトリのパスが返却されます.
	 *                nullの場合、Configディレクトリのパスが設定されていません.
	 */
	public static final String loadConfigDirectory(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		ConfigDirectory qconf = c.getAnnotation(ConfigDirectory.class);
		if(qconf == null) {
			return null;
		}
		// 存在しない場合はnull返却.
		String confDir = qconf.value();
		if(confDir == null || confDir.isEmpty() ||
			(confDir = confDir.trim()).isEmpty()) {
			return null;
		}
		// ￥パスを / パスに変換.
		confDir = AnnotationUtil.slashPath(confDir);
		// 文字列の最後の / が存在する場合除外.
		if(confDir.endsWith("/")) {
			confDir = confDir.substring(0, confDir.length() - 1);
		}
		// envPathを反映.
		confDir = Env.path(confDir);
		// 対象ディレクトリが存在しない場合.
		if(!FileUtil.isDir(confDir)) {
			// 例外.
			throw new QuinaException(
				"The specified config directory \"" +
					confDir + "\" does not exist. ");
		}
		return confDir;
	}
	
	/**
	 * Annotationで定義されてるAppendMimeTypeを読み込んで
	 * MimeTypesに設定します.
	 * @param c 対象のObjectを設定します.
	 * @return boolean 正しく読み込まれた場合 true が返却されます.
	 */
	public static final boolean loadAppendMimeType(Object c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
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
			throw new QuinaException("The specified argument is Null.");
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
	 * AnnotationにQuinaServiceScoped定義されてるか取得.
	 * @param o オブジェクトを設定します.
	 * @return boolean true の場合定義されています.
	 */
	public static final boolean isQuinaServiceScoped(Object o) {
		if(o == null) {
			throw new QuinaException("The specified argument is Null.");
		} else if(o instanceof QuinaService) {
			return false;
		}
		return o.getClass().isAnnotationPresent(QuinaServiceScoped.class);
	}
	
	/**
	 * AnnotationにQuinaServiceScoped定義されてるか取得.
	 * @param c クラスを設定します.
	 * @return boolean true の場合定義されています.
	 */
	public static final boolean isQuinaServiceScoped(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return c.isAnnotationPresent(QuinaServiceScoped.class);
	}
	
	/**
	 * Annotationに定義されてるQuinaServiceScopedのサービス名を取得.
	 * @param o QuinaServiceを設定します.
	 * @return String サービス名が返却されます.
	 *                nullの場合、QuinaServiceScopedが設定されていません.
	 */
	public static final String loadQuinaServiceScoped(Object o) {
		if(o == null) {
			throw new QuinaException("The specified argument is Null.");
		} else if(!(o instanceof QuinaService)) {
			throw new QuinaException("The specified argument is not an " +
				"object that inherits from the QuinaService interface. ");
		}
		QuinaServiceScoped service = o.getClass().getAnnotation(
			QuinaServiceScoped.class);
		if(service == null) {
			return null;
		}
		return service.value();
	}
	
	/**
	 * AnnotationにCdiScopedが定義されてるか取得.
	 * @param o オブジェクトを設定します.
	 * @return boolean true の場合 CdiScopedが定義されています.
	 */
	public static final boolean isCdiScoped(Object o) {
		if(o == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return isCdiScoped(o.getClass());
	}
	
	/**
	 * AnnotationにCdiScopedが定義されてるか取得.
	 * @param c コンポーネントクラスを設定します.
	 * @return boolean true の場合 CdiScopedが定義されています.
	 */
	public static final boolean isCdiScoped(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return c.isAnnotationPresent(CdiScoped.class);
	}
	
	/**
	 * AnnotationにCdiHandleScoped定義されてるか取得.
	 * @param c CdiHandleScopedを設定します.
	 * @return boolean true の場合定義されています.
	 */
	public static final boolean isCdiHandleScoped(Object o) {
		if(o == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return o.getClass().isAnnotationPresent(CdiHandleScoped.class);
	}
	
	/**
	 * AnnotationにisCdiHandleScoped定義されてるか取得.
	 * @param c クラスオブジェクトを設定します.
	 * @return boolean true の場合定義されています.
	 */
	public static final boolean isCdiHandleScoped(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		return c.isAnnotationPresent(CdiHandleScoped.class);
	}

}
