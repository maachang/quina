package quina.annotation.nativeimage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * GraalVMのNative-Imageに対するコンパイルや実行に
 * 関する設定を行います.
 * 
 * Native-Imageでは主に以下の条件に関して別途条件設定
 * を行う必要があります.
 * 
 * ・Reflection.
 * ・Resource.
 * ・Jni.
 * ・DynamicProxy.
 * 
 * これらに対して明示的にNative-Imageに指定することで
 * 正しくコンパイルされ実行することが出来ます.
 * 
 * このAnnotationでは、これらNativeImageに対する定義に
 * 関してのスコープ設定を行う事で、それら定義に関しての
 * Json出力を行い、正しくNative-Imageコンパイルを行います.
 * 
 * また＠NativeConfigScopedに関しては、３つの定義が
 * 可能です.
 * 
 * ・＠NativeConfigScoped
 * 　定義対象のオブジェクトを読み込みます.
 * 
 * ・＠NativeConfigScoped("org.postgresql.Driver")
 * 　　"org.postgresql.Driver" のクラスが存在する場合に
 * 　　定義対象のオブジェクトを読み込みます.
 * 
 * ・＠NativeConfigScoped({"com.mysql.cj.jdbc.Driver",
 * 　　"com.mysql.jdbc.Driver"})
 * 　　"com.mysql.cj.jdbc.Driver" または
 * 　　"com.mysql.jdbc.Driver"のクラスが存在する場合に
 * 　　定義対象のオブジェクトを読み込みます.
 * 
 * またこのスコープ設定されたオブジェクトに対して実行する
 * メソッドに対して NativeBuildStepアノテーションが定義
 * されてるメソッドを実行します.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeConfigScoped {
	public String[] value() default {};
}
