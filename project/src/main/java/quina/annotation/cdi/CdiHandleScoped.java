package quina.annotation.cdi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cdiフィールド注入に対する拡張処理を行い、Quinaに自動登録を
 * 行うためのアノテーションを定義します.
 * 
 * <例>
 * 
 * ＠CdiHandleScoped
 * public class ExsampleCdiHandle extends CdiHandle {
 *   ＠Override
 *   public void load(CdiReflectManager man, Object o, Class<?> c)
 *     throws Exception {
 *       .......
 *   }
 * }
 * 
 * この実装により ExsampleCdiHandle でオリジナルのQuinaのCdiフィールド
 * 注入対応が自動的に読み込まれます.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CdiHandleScoped {
}
