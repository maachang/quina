package quina.logger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定フィールドのLog定義を指定するアノテーション.
 * 
 * 以下のように定義することで、Logの初期定義を行います.
 * <例>
 * 
 * 
 * // クラスにアノテーション定義.
 * 
 * @LogDefine("service")
 * public class LogExample {
 *   private Log log;
 *   
 * }
 * 
 * // フィールドにアノテーション定義.
 * 
 * public class LogExample {
 *   @LogDefine()
 *   private Log sysLog;
 *   
 *   @LogDefine("service")
 *   private Log srvLog;
 *   
 * }
 * 
 * // LogDefineアノテーションを反映.
 * LogExample exp = LogAnnotationLog.loadLogDefine(
 *   new LogExample());
 *
 * これにより利用ログをプログラムから分離してLogを取得することが
 * 可能です.
 * 
 * クラスにアノテーションを定義する場合は、対象のLogオブジェクト定義が
 * １つ以上の場合、例外となります.
 * 
 * また @LogDefine() で設定した場合は LogFactory の system ログが
 * 対象となります.
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogDefine {
	public String value() default "";
}
