package quina.annotation.route;

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
 * 上記内容を以下のようにRouterに登録することでルートパスの設定が可能です.
 * 
 * quina.getRouter().route(new JsonGetSync());
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {
	public String value();
}

