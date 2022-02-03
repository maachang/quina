package quina.route.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ResourceFileComponetに対してリソースパッケージを設定する
 * アノテーション.
 * 
 * quina.component.ResourceFileComponentクラスを継承した
 * コンポーネントに対してリソースパッケージを設定します.
 * <例>
 * 
 * @Route("/public/*")
 * @ResourcePackage("quina.jdbc.console.resource")
 * public class QuinaJdbcConsoleComponent
 *   extends ResourceFileComponent {
 *   // 空定義.
 * }
 * 
 * 上記定義により "/public/login.html" でアクセスされた場合
 * classpath内の "quina.jdbc.console.login.html" のファイルを
 * 取得してレスポンス返却します.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourcePackage {
	/**
	 * ResourceFileCompomentに対するResourcePackage.
	 */
	public String value();
}
