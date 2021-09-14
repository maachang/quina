package quina.annotation.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定フィールドのLog定義を指定するアノテーション.<br>
 * <br>
 * 以下のように定義することで、Logの初期定義を行います.<br>
 * <pre>{@code
 * // クラスにアノテーション定義.
 * ＠LogDefine("service")
 * public class LogExample {
 *   private Log log;
 * }
 * 
 * // フィールドにアノテーション定義.
 * public class LogExample {
 *   ＠LogDefine
 *   private Log sysLog;
 *
 *   ＠LogDefine("service")
 *   private Log srvLog;
 *
 * }
 * }</pre>
 * <br>
 * // LogDefineアノテーションを反映.<br>
 * LogExample exp = LogAnnotationLog.loadLogDefine(<br>
 *   new LogExample());<br>
 *<br>
 * これにより利用ログをプログラムから分離してLogを取得することが
 * 可能です.<br>
 * <br>
 * クラスにアノテーションを定義する場合は、対象のLogオブジェクト定義が
 * １つ以上の場合、例外となります.<br>
 * <br>
 * また ＠LogDefine() で設定した場合は LogFactory の system ログが
 * 対象となります.<br>
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogDefine {
	public String value() default "";
}
