package quina.component.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HttpResponse返却のCrossドメインモードを設定するAnnotation.
 * 
 * quina.component.Componentインターフェイスを継承した
 * コンポーネントに対してResponseのCrossドメインモードの有無を設定します.
 * <例>
 * 
 * @CorsSwitch(true)
 * public class JsonGetSync implements RESTfulGetSync {
 *   public Object get(Request req, SyncResponse res, Params params) {
 *     return new ResultJson("params", params);
 *   }
 * }
 * 
 * この設定によりCrossドメインモードがOnにされたレスポンス返却が行われます.
 * 
 * また @CorsSwitch や @CorsSwitch() 定義の場合は Crossドメインからのアクセスは
 * 許可されます.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CorsSwitch {
	public boolean value() default true;
}

