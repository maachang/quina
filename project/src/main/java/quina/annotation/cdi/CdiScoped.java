package quina.annotation.cdi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CdiScopedを示すAnnotation.
 * 
 * この内容を設定することで＠LogDefineや＠Injectなどの
 * annotation定義が利用可能になります.
 * 
 * <例>
 * ＠CdiScoped
 * public class QuinaMain {
 *   ＠LogDefine
 *   private static Log log;
 *   
 *   ＠Inject
 *   private static HogeSample hogeSample;
 *   
 *   public static final void main(String[] args) {
 *     // quina 初期化.
 *     Quina.init(QuinaMain.class, args);
 *     log.info("hoge");
 *     log.info(hogeSample.helloHoge());
 *     
 *     ・・・・・・・・・
 *   }
 * }
 * 
 * CdiScopedのannotationを設定する事で、Quina.initメソッドで
 * これらの条件が読み込まれてAnnotation定義が実行されます.
 * 
 * また上記の場合は、CdiScopedで処理されるFieldは static が対象と
 * なりますが、以下のように行うことで、フィールド全体を対象とでき
 * ます.
 * 
 * <例>
 *     QuinaMain obj = new QuinaMain();
 *     Quina.init(obj, args);
 * 
 * また、通常のオブジェクトでCdiScopedを有効にする場合は、
 * 以下のように実装することで、対応出来ます.
 * 
 * ＠CdiScoped
 * public class Exsample {
 *   
 *   ＠LogDefine("exsample")
 *   private Log log;
 *   
 *   ＠Inject
 *   private HogeSample hogeSample;
 *   
 *   public Exsample() {
 *     // CdiScopedを読み込む.
 *     Quina.injectScoped(this);
 *   }
 *   
 *   public void hello() {
 *     log.info("hello " + hogeSample.hoge());
 *   }
 * }
 * 
 * Exsample ex = new Exsample();
 * ex.hello();
 * 
 * > hello hoge
 * 
 * ※ただし、これらの利用は Quina.init();処理を先に読み込む
 *   必要があります。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CdiScoped {
}
