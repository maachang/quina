package quina.annotation.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Component を Scope するAnnotation.
 * 
 * このアノテーションをクラスに指定する事で、
 * quina.component.Componentインターフェイスを
 * 継承せずとも、Quinaコンポーネントとして
 * 利用が可能です.
 * 
 * ＠ComponentScoped("/hoge")
 * public class Exsample {
 * 
 *   ＠POST
 *   ＠Route("/exsample/hello")
 *   ＠PathParams({"name", "age"})
 *   public Object hello(String name, int age) {
 *     return "hello " + name + " age: " + age;
 *   }
 * }
 * 
 * この実装によって、以下の実装と同じような形で動作します.
 * 
 * ＠Route("/hoge/exsample/hello")
 * public class Exsample implements SyncPostComponent {
 *   private final Exsample exsample = new Exsample();
 *   public Object post(Request req, SyncResponse res) {
 *     Params params = req.getParams();
 *     return hello(
 *       params.getString("name")
 *       ,params.getInteger("age")
 *     );
 *   }
 * }
 * 
 * 
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentScoped {
	/**
	 * 基本パス情報.
	 */
	public String value() default "/";
}