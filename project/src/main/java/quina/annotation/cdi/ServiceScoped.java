package quina.annotation.cdi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Serviceクラスを定義するアノテーション.
 * 
 * 指定されたクラスをServiceクラスに定義し、@Injectを使って
 * フィルドに注入することができます.
 * <例>
 * 
 * @ServiceScoped
 * public class GreetingService {
 *   public String greeting(String message) {
 *     return "hello " + message ;
 *   }
 * }
 * 
 * @Route("/greeting/{name}/get")
 * public class ResultGreeting implements RESTfulGetSync {
 *   @Inject
 *   GreetingService service;
 *   public Object get(Request req, SyncResponse res, Params params) {
 *     return new ResultJson("greeting", service.greeting(
 *       params.getString("name")));
 *   }
 * }
 * 
 * 上記内容をqRouteOutコマンドによって、GraalVMのNative-Imageで
 * コンパイルが可能となります.
 * 
 * これによりPath "/greeting/world/get" でアクセスする事で
 *  {greeting: "hello world"}
 * が返却されます.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceScoped {
}
