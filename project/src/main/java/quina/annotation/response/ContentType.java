package quina.annotation.response;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import quina.http.HttpCharset;
import quina.http.MediaType;

/**
 * HttpResponse返却のContentTypeを設定するAnnotation.
 * 
 * quina.component.Componentインターフェイスを継承した
 * コンポーネントに対してルートパスを設定します.
 * <例>
 * 
 * @ContentType(mimeType=MediaType.JSON, charset=HttpCharset.UTF8)
 * public class JsonGetSync implements RESTfulGetSync {
 *   public Object get(Request req, SyncResponse res, Params params) {
 *     return new ResultJson("params", params);
 *   }
 * }
 * 
 * これによりHttpResponseヘッダに
 *   Content-Type: application/json; charset=utf-8
 * が設定されます.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ContentType {
	/**
	 * MimeType.
	 */
	public MediaType mimeType();
	
	/**
	 * エンコードキャラクターセット.
	 */
	public HttpCharset charset() default HttpCharset.NONE;
}