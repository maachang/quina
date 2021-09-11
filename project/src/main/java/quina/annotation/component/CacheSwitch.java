package quina.annotation.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HttpResponse返却のCacheモードを設定するAnnotation.
 * 
 * quina.component.Componentインターフェイスを継承した
 * コンポーネントに対してResponseのCacheモードの有無を設定します.
 * <例>
 * 
 * @CacheSwitch(true)
 * public class JsonGetSync implements RESTfulGetSync {
 *   public Object get(Request req, SyncResponse res, Params params) {
 *     return new ResultJson("params", params);
 *   }
 * }
 * 
 * この設定によりCacheモードがOnにされたレスポンス返却が行われます.
 * 
 * また @CacheSwitch や @CacheSwitch() 定義の場合は cacheは有効に
 * なります.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheSwitch {
	public boolean value() default true;
}
