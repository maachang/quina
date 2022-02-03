package quina.component.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import quina.annotation.Switch;

/**
 * HttpResponse返却のモードを設定するAnnotation.
 * 
 * quina.component.Componentインターフェイスを継承した
 * コンポーネントに対してResponse判別設定を設定します.
 * <例>
 * 
 * @ResponseSwitch(gzip=Switch.On, cache=Switch.Off, cors=Switch.On)
 * public class JsonGetSync implements RESTfulGetSync {
 *   public Object get(Request req, SyncResponse res, Params params) {
 *     return new ResultJson("params", params);
 *   }
 * }
 * 
 * この設定によりGzip圧縮され、キャッシュモードOFFで
 * クロスドメインを許可するレスポンス返却が行われます.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseSwitch {
	/**
	 * Gzip圧縮モード.
	 */
	public Switch gzip() default Switch.None;
	
	/**
	 * レスポンスキャッシュモード.
	 */
	public Switch cache() default Switch.None;
	
	/**
	 * クロスドメイン許可モード.
	 */
	public Switch cors() default Switch.None;
}
