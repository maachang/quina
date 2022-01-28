package quina.compile.tool.graalvm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NativeConfigScopedアノテーションを定義していて
 * NativeImageConfigを生成しているメソッドに定義
 * する事で、そのメソッドが実行されて、正しく
 * NativeImageConfigが作成されます.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeBuildStep {
}
