package quina.compile.cdi.annotation.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrideメソッドに対する注入メソッドを
 * 定義します.
 * 
 * この定義が行われた内容は、Proxy継承元のOverride
 * されたメソッドに対して、必ず先頭で呼び出されます.
 * 
 * ＜例＞
 * public interface Abc {
 *   public void test();
 * }
 * 
 * @ProxyScoped
 * public abstract class TestProxy implements Abc {
 *   private boolean check = false;
 *   @ProxyField
 *   protected Abc abc;
 *   @ProxyInitialSetting
 *   protected void setting(Abc abc) {
 *     this.abc = abc;
 *   }
 *   @ProxyInjectMethod
 *   protected void check() {
 *     if(check) {
 *       throw new RuntimeException("check: true");
 *     }
 *   }
 * }
 * 
 * ＜結果＞
 * @SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
 * public class AutoProxyTestProxy extends TestProxy {
 *   public final void __initialSetting(quina.compile.cdi.annotation.proxy.ProxySettingArgs args) {
 *     try {
 *       super.setting(
 *         (Abc)args.getArgs(0)
 *       );
 *     } catch(quina.exception.QuinaException qe) {
 *       throw qe;
 *     } catch(Exception e) {
 *       throw new quina.exception.QuinaException(e);
 *     }
 *   }
 *   @Override
 *   public void test() {
 *     super.check();
 *     abc.test();
 *   }
 * }
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
