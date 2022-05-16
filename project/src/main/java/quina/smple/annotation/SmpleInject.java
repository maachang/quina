package quina.smple.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 利用SmpleオブジェクトをInjectします.
 * 
 * ＜例＞
 * Smpleを利用する: Xxx.java
 * ________________________________________________________
 * public class Xxx {
 *   @smpleInject("hoge.Moge")
 *   private Smple moge;
 *   ・・・
 *   ・・
 *   ・
 * }
 * ________________________________________________________
 * 
 * 
 * Smpleファイル: hoge.Moge.sml
 * ________________________________________________________
 * $smple {
 *   name: hoge.Moge,
 *   values: [
 *     String value,
 *   ]
 * }
 * hello ${value}.
 * ________________________________________________________
 * 
 * Inject名の設定が必須です.
 * Inject先のフィールドオブジェクトはSmpleオブジェクトである必要があります.
 * 
 * ※対象のフィールドに final をつけると設定されないので注意が必要です.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SmpleInject {
	public String value() default "";
}
