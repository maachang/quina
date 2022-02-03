package quina.component.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HttpResponse返却のGzip圧縮モードを設定するAnnotation.
 * 
 * quina.component.Componentインターフェイスを継承した
 * コンポーネントに対してResponseのGzip圧縮の有無を設定します.
 * <例>
 * 
 * @GzipSwitch(true)
 * public class JsonGetSync implements RESTfulGetSync {
 *   public Object get(Request req, SyncResponse res, Params params) {
 *     return new ResultJson("params", params);
 *   }
 * }
 * 
 * この設定によりGzip圧縮されたレスポンス返却が行われます.
 * 
 * また @GzipSwitch や @GzipSwitch() 定義の場合は Gzipは有効に
 * なります.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GzipSwitch {
	public boolean value() default true;
}
