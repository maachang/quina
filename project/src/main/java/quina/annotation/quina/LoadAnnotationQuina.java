package quina.annotation.quina;

import quina.annotation.AnnotationUtil;
import quina.exception.QuinaException;

/**
 * QuinaのAnnotationを取得して、Quina初期設定
 * を取得します.
 */
public class LoadAnnotationQuina {
	private LoadAnnotationQuina() {}
	
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
}
