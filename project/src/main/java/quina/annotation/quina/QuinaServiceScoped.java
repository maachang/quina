package quina.annotation.quina;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * QuinaServiceに関するScopedアノテーション.
 * 
 * このアノテーションをQuinaServiceを継承するオブジェクトに
 * 設定することで、このQuinaServiceがQuina.init() 時に自動
 * 登録され、実行されます.
 * 
 * 本来ならば、独自のQuinServiceを反映するには、
 * 以下のような実装で登録が必要となりますが、
 * 
 * <例>
 * 
 * // 初期処理の前にサービス登録.
 * Quina.get().getQuinaServiceManager().put(
 *   "hoge", new HogeQuinaService());
 * 
 * // Quina初期処理.
 * Quina.init(Xxx.class, args);
 * 
 * これを以下のように実装することで上記と同様の事が実現
 * できます.
 * 
 * <例>
 * 
 * ＠QuinaServiceScoped(name="hoge")
 * public class HogeQuinaService implements QuinaService {
 *   ........
 * }
 * 
 * あと例えばStorageサービスに対して、１つはメモリでStorage保持を
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
 * ＠QuinaServiceSelect(name="storage", define="jdbc")
 * public class QuinaMain {
 *   public static void main(String[] args) {
 *     Quina.init(QuinaMain.class, args);
 *     Quina.get().startAwait();
 *   }
 * }
 * 
 * これでstorageサービスに対してJDBCStorageServiceが選択されます.
 * 
 * またこのアノテーションは、QuinaServiceを継承してない場合は
 * 登録が無視されます.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QuinaServiceScoped {
	/**
	 * 登録するサービス名.
	 */
	public String name();
	
	/**
	 * サービス定義名.
	 * "" の場合は定義なし.
	 */
	public String define() default "";
}

