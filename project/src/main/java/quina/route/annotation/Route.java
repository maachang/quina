package quina.route.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ルートパスを設定するアノテーション.
 * 
 * quina.component.Componentインターフェイスを継承した
 * コンポーネントに対してルートパスを設定します.
 * <例>
 * 
 * @Route("/hoge/moge/${id}/a/${name}/")
 * public class JsonGetSync implements RESTfulGetSync {
 *   public Object get(Request req, SyncResponse res, Params params) {
 *     return new ResultJson("params", params);
 *   }
 * }
 * 
 * 上記内容をqRouteOutコマンドからAutoRoute可能な設定を行う事で、
 * GraalVMのNative-Imageでコンパイルが可能となります.
 * 
 * これによりJsonGetSyncオブジェクトのURLパスが
 *  "/hoge/moge/${id}/a/${name}/"
 * としてルート設定されます.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {
	/**
	 * Route先のPath.
	 */
	public String value();
}

