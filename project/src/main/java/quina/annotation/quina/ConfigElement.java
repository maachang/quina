package quina.annotation.quina;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import quina.util.collection.TypesClass;

/**
 * QuinaConfig要素を定義するアノテーション.
 * 
 * QuinaConfigフィールドに対してコンフィグ要素を定義を設定します.
 * 
 * ＜例＞
 * 
 * ＠CdiScoped
 * public class ExsampleObject {
 *   // コンフィグ情報.
 *   ＠ConfigName("exsample")
 *   ＠ConfigElement(name="name", type=TypedClass.String);
 *   ＠ConfigElement(name="age", type=TypedClass.Integer);
 *   ＠ConfigElement(name="sex", type=TypedClass.Boolean);
 *   ＠ConfigElement(name="zip", type=TypedClass.Integer);
 *   ＠ConfigElement(name="address", type=TypedClass.String);
 *   private QuinaConfig userInfo;
 *   
 *   // コンストラクタ.
 *   public ExsampleObject() {
 *     // CdiScopedを読み込む.
 *     Quina.loadCdiScoped(this);
 *   }
 * }
 * 
 * ConfigElementアノテーションを定義する場合は、必ず
 * ConfigNameアノテーションを定義する必要があります.
 * また、その時のフィールドは必ずQuinaConfigである必要が
 * あります.
 * 
 * またこのアノテーションを有効にするにはScopedアノテーション
 * を定義する必要があります.
 * 
 * またこの場合Configファイルの読み込みも自動で行われます.
 */
@Target(ElementType.FIELD)
@Repeatable(quina.annotation.quina.ConfigDefineArray.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigElement {
	/**
	 * コンフィグパラメータ名.
	 */
	public String name();
	
	/**
	 * コンフィグタイプ.
	 */
	public TypesClass type() default TypesClass.String;
	
	/**
	 * デフォルト値.
	 */
	public String defVal() default "";
}

/**
 * 複数のQuinaConfigDefineアノテーション.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface ConfigDefineArray {
	public ConfigElement[] value();
}
