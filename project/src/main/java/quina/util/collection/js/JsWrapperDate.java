package quina.util.collection.js;

import java.time.LocalDate;
import java.time.ZoneId;

import org.graalvm.polyglot.proxy.ProxyDate;

import quina.exception.QuinaException;
import quina.util.DateUtil;

/**
 * JavaのLocalDateをJavascriptで利用するラッパー.
 */
public class JsWrapperDate implements ProxyDate {
	// localDate.
	private LocalDate localDate;
	
	/**
	 * コンストラクタ.
	 * @param localDate LocalDateを設定します.
	 */
	public JsWrapperDate(LocalDate localDate) {
		if(localDate == null) {
			throw new QuinaException("Argument is null.");
		}
		this.localDate = localDate;
	}
	
	/**
	 * コンストラクタ.
	 * @param date java.util.Dateを設定します.
	 */
	public JsWrapperDate(java.util.Date date) {
		init(date);
	}
	
	/**
	 * コンストラクタ.
	 * @param date longを設定します.
	 */
	public JsWrapperDate(long time) {
		init(new java.util.Date(time));
	}
	
	/**
	 * コンストラクタ.
	 * @param date Stringを設定します.
	 */
	public JsWrapperDate(String date) {
		if(date == null) {
			throw new QuinaException("Argument is null.");
		}
		init(DateUtil.parseDate(date));
	}
	
	// 初期処理.
	private final void init(java.util.Date date) {
		if(date == null) {
			throw new QuinaException("Argument is null.");
		}
		this.localDate = date.toInstant()
			.atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
	/**
	 * JsDateを取得.
	 * @param date dateを設定します.
	 * @return JsDate オブジェクトが返却されます.
	 */
	public static final JsWrapperDate from(LocalDate date) {
		return new JsWrapperDate(date);
	}
	
	/**
	 * JsDateを取得.
	 * @param date dateを設定します.
	 * @return JsDate オブジェクトが返却されます.
	 */
	public static final JsWrapperDate from(java.util.Date date) {
		return new JsWrapperDate(date);
	}
	
	/**
	 * JsDateを取得.
	 * @param date dateを設定します.
	 * @return JsDate オブジェクトが返却されます.
	 */
	public static final JsWrapperDate from(long date) {
		return new JsWrapperDate(date);
	}
	
	/**
	 * JsDateを取得.
	 * @param date dateを設定します.
	 * @return JsDate オブジェクトが返却されます.
	 */
	public static final JsWrapperDate from(String date) {
		return new JsWrapperDate(date);
	}
	
	@Override
	public LocalDate asDate() {
		return localDate;
	}
}
