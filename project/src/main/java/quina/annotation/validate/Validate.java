package quina.annotation.validate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import quina.validate.VType;

/**
 * Validateアノテーション.
 * 
 * このAnnotationは複数設定することが可能です.
 * 
 * quina.component.Componentインターフェイスを継承した
 * コンポーネントに対してValidate条件を設定します.
 * <例>
 * 
 * @Validate(name="id", type=VType.Number, conditions=">= 10")
 * @Validate(name="name", type=VType.String, conditions="not null",
 *           message="名前が設定されていません")
 * @Validate(name="abc", type=VType.String, defVal="moge")
 * public class JsonGetSync implements RESTfulGetSync {
 *   public Object get(Request req, SyncResponse res, Params params) {
 *     return new ResultJson("params", params);
 *   }
 * }
 * 
 * 上記条件で対象Routerのrouteに以下のValidate定義が行われます.
 *   数字型のid名のパラメータで、10以下の場合はエラー.
 *   文字列型のname名のパラメータで、定義されてない場合はエラー.
 *     エラーの場合「名前が設定されていません」が返却されます.
 *   文字列型のabc名のパラメータで、定義されてない場合は"moge"がセットされる.
 * 
 * これらの定義がquina.validate.Validate.of() の定義と同様に定義されます.
 */
@Target(ElementType.TYPE)
@Repeatable(quina.annotation.validate.ValidateArray.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Validate {
	/**
	 * Validate対象のパラメーター名を設定します.
	 * ※必須.
	 */
	public String name();
	
	/**
	 * Validateのオブジェクト型を設定します.
	 */
	public VType type() default VType.String;
	
	/**
	 * Validate定義を設定します.
	 */
	public String conditions() default "";
	
	/**
	 * 未定義や空の場合の定義条件が設定します.
	 */
	public String defVal() default "";
	
	/**
	 * エラー時のメッセージを設定します.
	 */
	public String message() default "";
}

/**
 * 複数のValidateアノテーション.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface ValidateArray {
	public Validate[] value();
}