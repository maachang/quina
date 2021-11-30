package quina.annotation.quina;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * QuinaLoopThreadでループ実行を行う処理を登録するためのScoped.
 * 
 * <例>
 * 
 * ＠QuinaLoopScoped
 * public class ExsampleLoopElement extends QuinaLoopElement {
 *   ＠Override
 *   public void execute(QuinaThreadStatus status) {
 *       .......
 *   }
 * }
 * 
 * この実装により ExsampleLoopElement がループ実行されます.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QuinaLoopScoped {

}
