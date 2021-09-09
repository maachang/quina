package quina.annotation.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import quina.logger.LogLevel;

/**
 * Log定義を設定するAnnotation.
 * 
 * Quinaを実行するmainクラスに対して、LogConfigアノテーションを
 * 設定しQuina.init()処理を行う事で、Log定義が行えます.
 * 
 * @LogConfig(name="hoge", level=LogLevel.Trace,
 *     console=false, size="50m" directory="./log")
 * public class QuinaTest {
 *   public static main(String[] args) throws Exception {
 *     // Quina初期処理.
 *     Quina.init(QuinaTest.class, args);
 *     
 *     //(略).....
 *   }
 * 
 * これによって LogFactory.log("hoge") で設定されたログ条件のログが
 * 取得できます.
 * 
 * また取得対象のログはTraceレベルまで出力され、ConsoleはONに
 * １つのログファイルサイズは５０Mbyteで出力先は ./log 配下に
 * なります.
 * 
 * あと name を指定しない場合はQuinaのシステムログ "system" の設定に
 * なります.
 * 
 * またこのAnnotationは複数定義が可能です.
 */

@Target(ElementType.TYPE)
@Repeatable(quina.annotation.log.LogConfigArray.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogConfig {
	/**
	 * ログ定義名.
	 * 空の場合は "system" ログになります.
	 */
	public String name() default "";
	
	/**
	 * ログレベル.
	 */
	public LogLevel level() default LogLevel.DEBUG;
	
	/**
	 * コンソール出力モード.
	 */
	public boolean console() default true;
	
	/**
	 * １つのログ出力サイズ.
	 * 
	 * "1024" = 1,024 byte.
	 * "1k" = 1,024 byte.
	 * "1m" = 1,048,576 byte.
	 * "1g" = 1,073,741,824 byte.
	 * "1t" = 1,099,511,627,776 byte.
	 * "1p" = 1,125,899,906,842,624 byte.
	 */
	public String size() default "";
	
	/**
	 * ログ出力先ディレクトリ.
	 */
	public String directory() default "";

}

/**
 * 複数のSystemPropertyアノテーション.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface LogConfigArray {
	public LogConfig[] value();
}