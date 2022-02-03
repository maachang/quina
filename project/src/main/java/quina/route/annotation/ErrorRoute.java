package quina.route.annotation;

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
 *   public void call(int state, boolean restful, Request req,
 *     AnyResponse res, Throwable e);
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
 * 
 * また設定方法として以下の３通りの設定方法があります。
 * 
 *  1. 指定Httpステータスのエラー登録
 *   @ErrorRoute(status=404)
 *     Httpステータス４０４でのエラー登録.
 *  
 *  2. 範囲Httpステータスのエラー登録
 *   @ErrorRoute(start=500, end=599)
 *     Httpステータス５００～５９９の範囲のエラー登録.
 *  
 *  3. 範囲URLルート登録.
 *   @ErrorRoute(route="/jdbc/console/")
 *     /jdbc/console/* のURLでエラーが発生した場合に呼び出されます.
 *  
 *  4. 1と2と3以外のエラー登録.
 *   @ErrorRoute()
 *     1と2と3で登録した以外のHttpステータスが発生した場合に呼び出されます.
 *  
 *  また1と3,または2と3は併用定義が可能です.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ErrorRoute {
	/**
	 * 指定Httpステータス.
	 */
	public int status() default 0;

	/**
	 * 範囲Httpステータス開始値.
	 */
	public int start() default 0;

	/**
	 * 範囲Httpステータス終了値.
	 */
	public int end() default 0;
	
	/**
	 * 範囲URLルート.
	 */
	public String route() default "";
}
