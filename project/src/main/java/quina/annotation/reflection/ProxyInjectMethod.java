package quina.annotation.reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrideメソッドに対する注入メソッドを
 * 定義します.
 * 
 * この定義は１クラスに１つだけ設定が可能です.
 * 複数定義されてる場合例外が発生します.
 * 
 * またこのフィールドがpublic および protected以外
 * の場合は例外が発生します.
 * 
 * あとこのメソッドの引数が存在する場合は例外が
 * 発生します.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProxyInjectMethod {
}
