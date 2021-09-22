package quina.annotation.quina;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import quina.annotation.quina.SystemProperty.SystemPropertyArray;

/**
 * システムプロパティを設定するAnnotation.
 * 
 * Quinaを実行するmainクラスに対して、SystemPropertyアノテーションを
 * 設定しQuina.init()処理を行う事で、System.setPropertyが実施されます.
 * 
 * @SystemProperty(key="jdk.tls.client.protocols", value="TLSv1.2")
 * public class QuinaTest {
 *   public static main(String[] args) throws Exception {
 *     // Quina初期処理.
 *     Quina.init(QuinaTest.class, args);
 *     
 *     //(略).....
 *   }
 * 
 * これによってSystem.setProperty()の設定をAnnotationで定義できます.
 * 
 * またこのAnnotationは複数定義が可能です.
 */
@Target(ElementType.TYPE)
@Repeatable(SystemPropertyArray.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface SystemProperty {
	/**
	 * 複数のSystemPropertyアノテーション.
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	static @interface SystemPropertyArray {
		public SystemProperty[] value();
	}
	
	/**
	 * Key情報.
	 */
	public String key();
	
	/**
	 * Value情報.
	 */
	public String value();
}
