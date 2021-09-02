package quina.annotation.response;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HttpResponse返却のHeader群を設定するAnnotation.
 * 
 * このAnnotationは複数設定することが可能です.
 * 
 * quina.component.Componentインターフェイスを継承した
 * コンポーネントに対してルートパスを設定します.
 * <例>
 * 
 * ＠Header(key="X-Test-Value", value="100")
 * ＠Header(key="X-Hoge-Moge", value="abc")
 * public class JsonGetSync implements RESTfulGetSync {
 *   public Object get(Request req, SyncResponse res, Params params) {
 *     return new ResultJson("params", params);
 *   }
 * }
 * 
 * これによりHttpResponseのHttpヘッダに
 *   X-Test-Value: 100
 *   X-Hoge-Moge: abc
 * が追加されます.
 */
@Target(ElementType.TYPE)
@Repeatable(quina.annotation.response.HeaderArray.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Header {
	/**
	 * Key情報.
	 */
	public String key();
	
	/**
	 * Value情報.
	 */
	public String value();
}

/**
 * 複数のHttpHeaderアノテーション.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface HeaderArray {
	public Header[] value();
}