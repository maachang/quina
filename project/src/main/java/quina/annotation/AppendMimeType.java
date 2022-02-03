package quina.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import quina.annotation.AppendMimeType.AppendMimeTypeArray;

/**
 * Quina内で利用されるMimeType管理情報に対して新たなMimeTypeを
 * 設定できるAnnotation.
 * 
 * Quinaを実行するmainクラスに対して、AppendMimeTypeアノテーションを
 * 設定しQuina.init()処理を行う事で、Quina内で利用されるMimeTypesに
 * 設定したMimeTypeが反映されます.
 * 
 * ＠AppendMimeType(extension="hoge",
 *   mimeType="application/hoge",
 *   charset=Switch.On)
 * public class QuinaTest {
 *   public static main(String[] args) throws Exception {
 *     // Quina初期処理.
 *     Quina.init(QuinaTest.class, args);
 *     
 *     //(略).....
 *   }
 * 
 * これによってMimeTypesに対して、拡張子が hoge MimeTypeが
 * application/hoge 文字コードの付与がONの条件が新たに追加されます.
 * 
 * またこのAnnotationは複数定義が可能です.
 */
@Target(ElementType.TYPE)
@Repeatable(AppendMimeTypeArray.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface AppendMimeType {
	/**
	 * 複数のAppendMimeTypeアノテーション.
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	static @interface AppendMimeTypeArray {
		public AppendMimeType[] value();
	}
	
	/**
	 * MimeTypeの拡張子.
	 */
	public String extension();
	
	/**
	 * MimeType.
	 */
	public String mimeType();
	
	/**
	 * キャラクターセットの付加が可能かの条件.
	 */
	public Switch charset() default Switch.None;
}
