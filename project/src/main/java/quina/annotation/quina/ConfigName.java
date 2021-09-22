package quina.annotation.quina;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * QuinaConfig定義名を設定するアノテーション.
 * 
 * QuinaConfigフィールドに対してコンフィグ定義名を設定します.
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
 *     Quina.injectScoped(this);
 *   }
 * }
 * 
 * ConfigNameアノテーションを利用する場合は１つ以上の
 * ConfigElementアノテーションを定義する必要があります.
 * また、その時のフィールドは必ずQuinaConfigである必要が
 * あります.
 * 
 * またこのアノテーションを有効にするにはScopedアノテーション
 * を定義する必要があります.
 * 
 * またこの場合Configファイルの読み込みも自動で行われます.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigName {
	/**
	 * QuinaConfig名の定義.
	 */
	public String value();
}