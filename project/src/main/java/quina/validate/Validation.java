package quina.validate;

import java.util.ArrayList;
import java.util.List;

import quina.http.Header;
import quina.http.Params;
import quina.http.Request;
import quina.util.StringUtil;

/**
 * Validate処理.
 *
 * 以下のように実装することで、Httpパラメータを検証します.
 *
 * Validation v = new Validation(
 * 	"name", "String", "not null", // name文字パラメータで、必須情報.
 * 	"age", "Number", "", // age数値パラメータ.
 * 	"comment", "String", "max 128", // comment文字パラメータで、最大文字が128文字.
 * 	"X-Test-Code", "String", "not null" // X-Test-CodeHttpヘッダパラメータで、必須.
 * );
 *
 * これを以下のように行う事で検証処理が実現出来ます.
 *
 * Params newParams = v.validate(req, params);
 *
 */
public class Validation {
	// validateリスト.
	private List<VElement> list = new ArrayList<VElement>();

	/**
	 * コンストラクタ.
	 * @param args パラメータの検証条件を設定します.
	 */
	public Validation(Object... args) {
		final int len = args.length;
		for(int i = 0; i < len; i += 3) {
			list.add(
				new VElement(
					StringUtil.parseString(args[i]),
					StringUtil.parseString(args[i + 1]),
					StringUtil.parseString(args[i + 2])
				)
			);
		}
	}

	/**
	 * 検証処理を実行.
	 * @param req HTTPリクエストを設定します.
	 * @param params HTTPパラメータを設定します.
	 * @return Params 新しいパラメータが返却されます.
	 */
	public Params execute(Request req, Params params) {
		VElement em;
		Object value;
		final Params ret = new Params();
		final Header head = req.getHeader();
		final int len = list.size();
		// 解析されたvalidateリストで、検証処理.
		for(int i = 0; i < len; i ++) {
			// Httpヘッダから情報を取得する場合.
			if((em = list.get(i)).isHeader()) {
				value = head.get(em.getHeaderColumn());
			// パラメータから情報を取得する場合.
			} else {
				value = params.get(em.getColumn());
			}
			// 検証処理.
			value = em.validate(value);
			// 検証結果の内容をセット.
			ret.put(em.getColumn(), value);
		}
		return ret;
	}
}
