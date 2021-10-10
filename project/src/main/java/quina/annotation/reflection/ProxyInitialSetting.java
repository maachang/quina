package quina.annotation.reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ProxyInitialSettingアノテーション定義.
 * 
 * この定義は１クラスに必ず１つ定義が必要です.
 * 定義なし、複数定義の場合は例外が発生します.
 * 
 * またこのフィールドがpublic および protected以外
 * の場合は例外が発生します.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProxyInitialSetting {
}
