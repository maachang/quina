package quina.annotation.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import quina.annotation.log.LogConfig.LogConfigArray;
import quina.logger.LogLevel;

/**
 * Log定義を設定するAnnotation.<br>
 * <br>
 * Quinaを実行するmainクラスに対して、LogConfigアノテーションを
 * 設定しQuina.init()処理を行う事で、Log定義が行えます.<br>
 * <pre>{@code
 * ＠LogConfig(name="hoge", level=LogLevel.Trace,
 *     console=false, size="50m" directory="./log")
 * public class QuinaTest {
 *   public static main(String[] args) throws Exception {
 *     // Quina初期処理.
 *     Quina.init(QuinaTest.class, args);
 *     
 *     //(略).....
 *   }
 * }
 * }</pre>
 * <br>
 * これによって LogFactory.log("hoge") で設定されたログ条件のログが
 * 取得できます.<br>
 * <br>
 * また取得対象のログはTraceレベルまで出力され、ConsoleはONに
 * １つのログファイルサイズは50Mbyteで出力先は ./log 配下に
 * なります.<br>
 * <br>
 * あと name を指定しない場合はQuinaのシステムログ "system" の設定に
 * なります.<br>
 * <br>
 * またこのAnnotationは複数定義が可能です.
 */

@Target(ElementType.TYPE)
@Repeatable(LogConfigArray.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogConfig {
	/**
	 * 複数のLogConfigアノテーション.
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	static @interface LogConfigArray {
		public LogConfig[] value();
	}
	
	/**
	 * ログ定義名.
	 * 空の場合は "system" ログになります.
	 */
	public String name() default "";
	
	/**
	 * ログレベル.
	 */
	public LogLevel level() default LogLevel.INFO;
	
	/**
	 * コンソール出力モード.
	 */
	public boolean console() default true;
	
	/**
	 * １つのログ出力サイズ.
	 */
	public String size() default "";
	
	/**
	 * ログ出力先ディレクトリ.
	 */
	public String directory() default "";
}
