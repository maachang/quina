package quina.annotation.log;

import java.lang.reflect.Field;

import quina.Quina;
import quina.annotation.cdi.CdiReflectElement;
import quina.exception.QuinaException;
import quina.logger.Log;
import quina.logger.LogDefineElement;
import quina.logger.LogException;
import quina.logger.LogFactory;

/**
 * LogのAnnotationを取得して、Logに関する定義処理を
 * 実現します.
 */
public class LoadLog {
	private LoadLog() {}

	/**
	 * Annotationで定義されてるLogConfigを読み込んで
	 * ログの定義を設定します.
	 * @param c 対象のObjectを設定します.
	 * @return boolean 正しく読み込まれた場合 true が返却されます.
	 */
	public static final boolean loadLogConfig(Object c) {
		if(c == null) {
			throw new QuinaException("The specified argument is null.");
		}
		return loadLogConfig(c.getClass());
	}
	
	/**
	 * Annotationで定義されてるLogConfigを読み込んで
	 * ログの定義を設定します.
	 * @param c 対象のクラスを設定します.
	 * @return boolean 正しく読み込まれた場合 true が返却されます.
	 */
	public static final boolean loadLogConfig(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is null.");
		}
		// 対象コンポーネントからLogConfigArrayアノテーション定義を取得.
		LogConfigArray array = c.getAnnotation(
			LogConfigArray.class);
		// 存在しない場合.
		if(array == null) {
			// 単体で取得.
			LogConfig conf = c.getAnnotation(LogConfig.class);
			if(conf != null) {
				regLogElement(conf);
				return true;
			}
			return false;
		}
		// 複数のLogConfigアノテーション定義を取得.
		LogConfig[] list = array.value();
		// 存在しない場合.
		if(list == null || list.length == 0) {
			return false;
		}
		// LogFactoryにLogConfigの登録を実行.
		int len = list.length;
		for(int i = 0; i < len; i ++) {
			regLogElement(list[i]);
		}
		return len > 0;
	}
	
	// LogElementを登録.
	private static final void regLogElement(LogConfig conf) {
		// LogConfigからLogDefineElementを生成.
		LogDefineElement em = new LogDefineElement(conf);
		if(conf.name().isEmpty()) {
			// LogConfigのname()が空の場合はSystemログ設定.
			LogFactory.getInstance().register(em);
		} else {
			// LogConfigのname()が存在する場合はログ定義名でのログ設定.
			LogFactory.getInstance().register(conf.name(), em);
		}
	}
	
	/**
	 * 指定オブジェクトのLogDefineアノテーションを反映.
	 * @param o オブジェクトを設定します.
	 */
	public static final void loadLogDefine(Object o) {
		if(o == null) {
			throw new QuinaException("The specified argument is null.");
		}
		// クラス定義の場合.
		LogDefine def = o.getClass().getAnnotation(LogDefine.class);
		if(def != null) {
			// クラス定義のLogDefineを割り当てる.
			loadLogDefineByClass(o, o.getClass(), def);
		} else {
			// フィールド定義のLogDefinewo割り当てる.
			loadLogDefineByField(o, o.getClass());
		}
	}
	
	/**
	 * 指定オブジェクトのLogDefineアノテーションを反映.
	 * @param c クラスを設定します.
	 */
	public static final void loadLogDefine(Class<?> c) {
		if(c == null) {
			throw new QuinaException("The specified argument is null.");
		}
		// クラス定義の場合.
		LogDefine def = c.getAnnotation(LogDefine.class);
		if(def != null) {
			// クラス定義のLogDefineを割り当てる.
			loadLogDefineByClass(null, c, def);
		} else {
			// フィールド定義のLogDefinewo割り当てる.
			loadLogDefineByField(null, c);
		}
	}

	
	// クラス定義されてるLogDefineアノテーションの定義処理.
	private static final void loadLogDefineByClass(Object o, Class<?> c, LogDefine def) {
		Field targetField = null;
		final CdiReflectElement list = Quina.get().getCdiReflectManager().get(c);
		if(list == null) {
			return;
		}
		final int len = list.size();
		final Class<?> log = Log.class;
		for(int i = 0; i < len; i ++) {
			// ログクラスを対象とする.
			if(list.get(i).getType() == log) {
				// ログクラスが複数設定されてる場合はエラー.
				if(targetField != null) {
					throw new LogException(
						"You cannot define multiple Log fields with the LogDefine"+
						"annotation in the class definition. ");
				}
				targetField = list.get(i);
			}
		}
		// ログクラスが存在しない場合.
		if(targetField == null) {
			return;
		}
		// 存在する場合、フィールドにLog定義を設定.
		try {
			if(def.value() == null || def.value().isEmpty()) {
				targetField.set(o, LogFactory.log());
			} else {
				targetField.set(o, LogFactory.log(def.value()));
			}
		} catch(Exception e) {
			throw new LogException(e);
		}
	}
	
	// フィールド定義されてるLogDefineアノテーションの定義処理.
	private static final void loadLogDefineByField(Object o, Class<?> c) {
		LogDefine def;
		Field targetField;
		final CdiReflectElement list = Quina.get()
			.getCdiReflectManager().get(c);
		if(list == null) {
			return;
		}
		final int len = list.size();
		final Class<?> log = Log.class;
		for(int i = 0; i < len; i ++) {
			// ログクラスを対象とする.
			if(list.get(i).getType() == log) {
				targetField = list.get(i);
				// フィールドからLogDefineアノテーションを取得.
				if((def = targetField.getAnnotation(LogDefine.class)) != null) {
					// 存在する場合、フィールドにLog定義を設定.
					try {
						if(def.value() == null || def.value().isEmpty()) {
							list.setValue(i, o, LogFactory.log());
						} else {
							list.setValue(i, o, LogFactory.log(def.value()));
						}
					} catch(Exception e) {
						throw new LogException(e);
					}
				}
			}
		}
	}
}
