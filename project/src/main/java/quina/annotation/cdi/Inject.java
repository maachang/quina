package quina.annotation.cdi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Serviceアノテーション定義のクラスをInject（注入）します.
 * 
 * このInjectアノテーションはServiceアノテーションが設定された
 * クラスをquina.component.Componentオブジェクト内や
 * quina.annotation.ErrorComponentオブジェクト内や
 * quina.annotation.service.ServiceScopedアノテーションの
 * オブジェクト内で利用が可能です.
 * 
 * Injectの定義は２つの方法で定義することができます.
 * 
 *   @Inject
 *   private XxxxService service;
 * 
 * この定義の場合XxxxServiceは基底クラスやインターフェイスでない
 * １つだけ存在する場合に利用が可能です.
 * 
 * また１つ以上存在する場合は、例外が発生します.
 * 
 * 基底クラスやインターフェイスで継承されたServiceアノテーション定義
 * クラスが存在する場合は、以下のように定義する必要があります.
 * 
 *   @Inject("abc.xyz.YyyyService")
 *   private XxxxService service;
 * 
 * このようにパッケージ名＋クラス名を指定することで正常にInjectする
 * 事ができます.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
	public String value() default "";
}
