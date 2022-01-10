package quina.annotation.quina;

import java.lang.reflect.Field;

import quina.Quina;
import quina.QuinaConfig;
import quina.QuinaService;
import quina.QuinaServiceManager;
import quina.annotation.AnnotationUtil;
import quina.annotation.Switch;
import quina.annotation.cdi.CdiHandleScoped;
import quina.annotation.cdi.CdiReflectElement;
import quina.annotation.cdi.CdiScoped;
import quina.annotation.quina.AppendMimeType.AppendMimeTypeArray;
import quina.annotation.quina.ConfigElement.ConfigElementArray;
import quina.annotation.quina.QuinaServiceSelection.QuinaServiceSelectionArray;
import quina.annotation.quina.SystemProperty.SystemPropertyArray;
import quina.exception.QuinaException;
import quina.http.MimeTypes;
import quina.logger.LogException;
import quina.util.Env;
import quina.util.FileUtil;

/**
 * Quina.init で処理される annotationの読み込み処理を
 * 行います.
 */
public class AnnotationQuina {
	private AnnotationQuina() {}
	
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
	 * @return Object[] [0]サービス登録ID, [0]サービス名, [1]サービス定義名が
	 *                  返却されます.
	 *                  nullの場合、QuinaServiceScopedが設定されていません.
	 */
	public static final Object[] loadQuinaServiceScoped(Object o) {
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
		long id = service.id();
		String name = service.name();
		String define = service.define().trim();
		if(define.isEmpty()) {
			define = null;
		}
		return new Object[] {id, name, define};
	}
	
	/**
	 * Annotationで定義されてるQuinaServiceSelectionを読み込んで
	 * QuinaService定義された内容を登録します.
	 * @param c 対象のクラスを設定します.
	 * @return boolean 正しく読み込まれた場合 true が返却されます.
	 */
	public static final boolean regQuinaServiceSelection(
		QuinaServiceManager man, Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is Null.");
		}
		QuinaServiceSelection[] list = null;
		// 対象コンポーネントからQuinaServiceSelectionArray
		// アノテーション定義を取得.
		QuinaServiceSelectionArray array = c.getAnnotation(
			QuinaServiceSelectionArray.class);
		// 存在しない場合.
		if(array == null) {
			// 単体で取得.
			QuinaServiceSelection p = c.getAnnotation(
				QuinaServiceSelection.class);
			if(p != null) {
				list = new QuinaServiceSelection[] {p};
			}
		} else {
			// 複数のQuinaServiceSelectionアノテーション定義を取得.
			list = array.value();
		}
		// 存在しない場合.
		if(list == null || list.length == 0) {
			return false;
		}
		// QuinaServiceManagerで定義登録されている内容を
		// サービス登録.
		int len = list.length;
		for(int i = 0; i < len; i ++) {
			// サービス登録.
			man.putDefineToService(
				list[i].name(), list[i].define());
		}
		return len > 0;
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
	 * フィールド定義されてるQuinaConfigアノテーション定義を
	 * 読み込んで、QuinaConfigをフィールドに注入します.
	 * @param o 対象のオブジェクトを設定します.
	 */
	public static final void injectQuinaConfig(Object o) {
		if(o == null) {
			throw new QuinaException("The specified argument is null.");
		}
		injectQuinaConfig(o, o.getClass());
	}
	
	/**
	 * フィールド定義されてるQuinaConfigアノテーション定義を
	 * 読み込んで、QuinaConfigをフィールドに注入します.
	 * @param c 対象のクラスを設定します.
	 */
	public static final void injectQuinaConfig(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is null.");
		}
		injectQuinaConfig(null, c);
	}
	
	// フィールド定義されてるQuinaConfigアノテーション定義を
	// 読み込んで、QuinaConfigをフィールドに注入します.
	private static final void injectQuinaConfig(Object o, Class<?> c) {
		QuinaConfig conf;
		ConfigElement[] elist;
		Field targetField;
		final CdiReflectElement list = Quina.get()
			.getCdiReflectManager().get(c);
		if(list == null) {
			return;
		}
		final int len = list.size();
		final Class<?> clazz = QuinaConfig.class;
		for(int i = 0; i < len; i ++) {
			// QuinaConfigクラスを対象とする.
			if(list.get(i).getType() == clazz) {
				targetField = list.get(i);
				// 対象コンフィグ要素を取得.
				elist = getConfigElementArray(targetField);
				// 対象のコンフィグ情報を取得.
				if((conf = getQuinaConfig(targetField)) == null) {
					// コンフィグ名が設定されてなくて、
					// コンフィグ要素が存在する場合
					if(elist != null && elist.length > 0) {
						throw new QuinaException(
							"The config name is not defined.");
					}
					conf = null;
					continue;
				// コンフィグ名が設定されていて、
				// コンフィグ要素が存在しない場合.
				} else if(elist == null || elist.length == 0) {
					throw new QuinaException(
						"The config element does not exist.");
				}
				// コンフィグ要素をセット.
				setQuinaConfig(conf, elist);
				
				// コンフィグファイルを読み込む.
				loadConfigFile(conf);
				
				// コンフィグ情報を登録.
				try {
					list.setValue(i, o, conf);
				} catch(Exception e) {
					throw new LogException(e);
				}
			}
		}
	}
	
	// QuinaConfigを作成
	private static final QuinaConfig getQuinaConfig(Field targetField) {
		ConfigName cname = targetField.getAnnotation(ConfigName.class);
		if(cname == null || cname.value() == null ||
			cname.value().isEmpty()) {
			return null;
		}
		return new QuinaConfig(cname.value());
	}
	
	// 定義されてるConfigElement要素群を取得.
	private static final ConfigElement[] getConfigElementArray(
		Field targetField) {
		// 対象コンポーネントからConfigElementArrayアノテーション定義を取得.
		ConfigElementArray array = targetField.getAnnotation(
			ConfigElementArray.class);
		// 存在しない場合.
		if(array == null) {
			// 単体で取得.
			ConfigElement conf = targetField.getAnnotation(
				ConfigElement.class);
			if(conf != null) {
				return new ConfigElement[] {conf};
			}
			return null;
		}
		// 複数のConfigElementアノテーション定義を取得.
		ConfigElement[] list = array.value();
		// 存在しない場合.
		if(list == null || list.length == 0) {
			return null;
		}
		return list;
	}
	
	// ConfigElement群をQuinaConfigに設定.
	private static final void setQuinaConfig(
		QuinaConfig conf, ConfigElement[] elist) {
		ConfigElement em;
		int len = elist.length;
		for(int i = 0; i < len; i ++) {
			em = elist[i];
			conf.putReservation(
				em.name(), em.type(), em.defVal());
		}
	}
	
	// QuinaConfigに外部ファイルをローディング.
	private static final void loadConfigFile(QuinaConfig conf) {
		String dir = Quina.configDirectory();
		// コンフィグディレクトリが設定されていない場合
		if(dir == null || dir.isEmpty()) {
			// 処理しない.
			return;
		}
		// 初期化済みの場合のみ設定する.
		if(Quina.isInit()) {
			conf.loadConfig(dir);
		}
	}
	
}
