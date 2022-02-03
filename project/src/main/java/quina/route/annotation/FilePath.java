package quina.route.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FileComponetに対してローカルファイルパスを設定する
 * アノテーション.
 * 
 * quina.component.FileComponentクラスを継承した
 * コンポーネントに対してローカルファイルパスを設定します.
 * <例>
 * 
 * @Route("/public/*")
 * @FilePath("./target/")
 * @ResponseSwitch(cache=Switch.On)
 * public class LocalFileComponent extends FileComponent {
 *   // 空定義.
 * }
 * 
 * 上記定義により "/public/abc/def.txt" でアクセスされた場合
 * ローカルファイルの "./target/abc/def.txt" のファイルを
 * 取得してレスポンス返却します.
 * 
 * また @ResponseSwitch で cache を On に設定することで、
 * Etagでのキャッシュレスポンスが対応できます.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilePath {
	/**
	 * FileCompomentに対するPath.
	 */
	public String value();
}
