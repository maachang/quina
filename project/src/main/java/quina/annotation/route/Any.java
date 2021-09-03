package quina.annotation.route;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anyパスを設定するアノテーション.
 * 
 * quina.component.Componentインターフェイスを継承した
 * コンポーネントに対してAnyパスを設定します.
 * <例>
 * 
 * @Any
 * public class JsonGetSync implements RESTfulGetSync {
 *   public Object get(Request req, SyncResponse res, Params params) {
 *     return new ResultJson("params", params);
 *   }
 * }
 * 
 * 上記内容をqRouteOutコマンドからAutoRoute可能な設定を行う事で、
 * GraalVMのNative-Imageでコンパイルが可能となります.
 * 
 * これによりRouterに登録されてるComponentに対してURLが対象外だった
 * 場合に、このJsonGetSyncが呼び出されるようになります.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Any {
}
