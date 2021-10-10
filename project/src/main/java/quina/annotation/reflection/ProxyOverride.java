package quina.annotation.reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ProxyMethodのOverrideアノテーション定義.
 * 
 * この定義が行われた内容が継承対象のクラスに対する
 * ProxyClassのメソッドとして定義されます.
 * 
 * この定義が行われてない場合例外が発生します.
 * 
 * またこのフィールドがpublic および protected以外
 * の場合は例外が発生します.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProxyOverride {
}
