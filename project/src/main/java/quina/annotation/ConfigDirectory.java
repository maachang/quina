package quina.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * コンフィグディレクトリを設定するAnnotation.
 * 
 * Quinaを実行するmainクラスに対して、ConfigDirectory
 * アノテーションを設定しQuina.init()処理を行う事で、
 * Quinaに対してConfigディレクトリ登録が実施されて
 * 設定内容が反映されます.
 * 
 * @ConfigDirectory("./conf")
 * public class QuinaTest {
 *   public static main(String[] args) throws Exception {
 *     // Quina初期処理.
 *     Quina.init(QuinaTest.class, args);
 *     
 *     //(略).....
 *   }
 * 
 * これによってQuinaに ./conf のConfigディレクトリが設定されて、
 * 設定内容が反映されます.
 * 
 * ※逆に設定しない場合は
 *   Quina.get().loadConfig("./conf");
 * と設定が必要です.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigDirectory {
	/**
	 * コンフィグディレクトリ.
	 */
	public String value();
}