package quina.annotation.route;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ErrorComponentを設定するアノテーション.
 * 
 * quina.component.ErrorComponentインターフェイスを継承した
 * コンポーネントに対してAnyパスを設定します.
 * <例>
 * 
 * @ErrorRoute
 * public class NewErrorComponent implements ErrorComponent {
 *   public void call(int state, boolean restful, Request req, NormalResponse res, Throwable e);
 *     String message = "error!!";
 *     if(e != null) {
 *       message = e.getMessage();
 *     }
 *     if(restful) {
 *       res.sendJSON(ResultJson.of("error", message));
 *     } else {
 *       res.send(message);
 *     }
 *   }
 * }
 * 
 * 上記内容をqRouteOutコマンドからAutoRoute可能な設定を行う事で、
 * GraalVMのNative-Imageでコンパイルが可能となります.
 * 
 * これによりエラーが発生した場合にNewErrorComponentが呼び出されます.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ErrorRoute {
}
