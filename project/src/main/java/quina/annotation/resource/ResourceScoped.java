package quina.annotation.resource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Resourceファイルのコピーを行う場合の
 * スコープアノテーション.
 * 
 * javaソースのパッケージ内にリソースファイルを
 * 保存していても、コンパイル時にコピーされないので
 * このアノテーションがセットされたオブジェクトで、
 * コンパイルされたClassパッケージ内にコピーすること
 * ができます。
 * 
 * ＜例＞
 * 
 * ＠ResourceScoped
 * public class CopyJavaConsoleResource {
 *   // copy file list.
 *   private static final String[] COPY_LIST = new String[] {
 *     "login.html",
 *     "console.html",
 *     "base.js",
 *     "console.js",
 *     "console.css"
 *   };
 *   
 *   // directoryからjavaPackageにコピー.
 *   ＠BuildResource(
 *     src="./console",
 *     srcMode=ResourceMode.Directory,
 *     dest="quina.jdbc.console.resource",
 *     destMode=ResourceMode.JavaPackage)
 *   public String[] copyStep1() {
 *     return COPY_LIST;
 *   }
 * 
 *   // javaPackageからclassPackageにコピー.
 *   ＠BuildResource(
 *     src="quina.jdbc.console.resource",
 *     srcMode=ResourceMode.JavaPackage,
 *     dest="quina.jdbc.console.resource",
 *     destMode=ResourceMode.ClassPackage)
 *   public String[] copyStep2() {
 *     return COPY_LIST;
 *   }
 * }
 * 
 * これにより./consoleディレクトリ内のファイルを
 * javaソースのquina.jdbc.console.resourceパッケージに
 * コピーして、その内容をコピーされたJavaソースのパッケージ
 * から、Classのパッケージにコピーします.
 * 
 * またBuildResponseアノテーションがセットされたメソッドは
 * その名前で昇順ソートされて、その順番で実行されます.
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceScoped {
}
