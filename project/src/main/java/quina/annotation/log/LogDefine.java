package quina.annotation.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定フィールドのLog定義を指定するアノテーション.<br>
 * <br>
 * 以下のように定義することで、Logの初期定義を行います.<br>
 * <例><br>
 * <br>
 * // クラスにアノテーション定義.<br>
 * ＠LogDefine("service")<br>
 * public class LogExample {<br>
 *   private Log log;<br>
 * }<br>
 * <br>
 * // フィールドにアノテーション定義.<br>
 * public class LogExample {<br>
 *   ＠LogDefine()<br>
 *   private Log sysLog;<br>
 *<br>
 *   ＠LogDefine("service")<br>
 *   private Log srvLog;<br>
 *<br>
 * }<br>
 *<br>
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
