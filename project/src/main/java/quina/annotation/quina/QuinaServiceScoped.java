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
 * ＠QuinaServiceScoped("hoge")
 * public class HogeQuinaService implements QuinaService {
 *   ........
 * }
 * 
 * またこのアノテーションは、QuinaServiceを継承してない場合は
 * 登録が無視されます.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QuinaServiceScoped {
	/**
	 * 登録するQuinaService名.
	 */
	public String value();
}

