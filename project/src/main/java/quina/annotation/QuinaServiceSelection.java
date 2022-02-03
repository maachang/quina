package quina.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * QuinaServiceに関する定義サービス確定用アノテーション.
 * 
 * このアノテーションをQuinaServiceを継承するオブジェクトに
 * 設定することで、このQuinaServiceがQuina.init() 時に自動
 * 登録され、実行されます.
 * 
 * 例えばStorageサービスに対して、１つはメモリでStorage保持を
 * する場合と、もう１つはRDBMSで保持するサービスがあるとします。
 * この場合は以下のように定義する事で、プログラム実行時に選択されて
 * 実行することが出来ます.
 * 
 * <例>
 * 
 * ＠QuinaServiceScoped(name="storage", define="memory")
 * public class MemoryStorageService implements QuinaService {
 *   ........
 * }
 * 
 * ＠QuinaServiceScoped(name="storage", define="jdbc")
 * public class JDBCStorageService implements QuinaService {
 *   ........
 * }
 * 
 * ＠QuinaServiceSelection(name="storage", define="jdbc")
 * public class QuinaMain {
 *   public static void main(String[] args) {
 *     Quina.init(QuinaMain.class, args);
 *     Quina.get().startAwait();
 *   }
 * }
 * 
 * これでstorageサービスに対してJDBCStorageServiceが選択されます.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QuinaServiceSelection {
	/**
	 * 複数のQuinaServiceSelectionアノテーション.
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	static @interface QuinaServiceSelectionArray {
		public QuinaServiceSelection[] value();
	}
	
	/**
	 * 登録するサービス名.
	 */
	public String name();
	
	/**
	 * サービス定義名.
	 */
	public String define();
}
