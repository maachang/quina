package quina.component.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import quina.http.HttpStatus;

/**
 * HttpResponse返却のStatusを設定するAnnotation.
 * 
 * quina.component.Componentインターフェイスを継承した
 * コンポーネントに対してHttpステータスを設定します.
 * <例>
 * 
 * @Status(status=HttpStatus.OK, message="hello world!!")
 * public class JsonGetSync implements RESTfulGetSync {
 *   public Object get(Request req, SyncResponse res, Params params) {
 *     return new ResultJson("params", params);
 *   }
 * }
 * 
 * これによりHttpResponseでステータス２００でメッセージにHello world!!
 * が返却されます.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Status {
	/**
	 * ステータス.
	 */
	public HttpStatus status();
	
	/**
	 * メッセージ.
	 */
	public String message() default "";
}
