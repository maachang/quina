package quina.validate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import quina.util.Alphabet;
import quina.util.DateUtil;
import quina.util.NumberUtil;
import quina.util.StringUtil;

/**
 * １つのValidateチェック要素群.
 */
final class VCheckElements {
	
	// valueがnullの時のメッセージ.
	private static final String NULL_MESSAGE = "The value of '{0}' is {1}.";
	
	// 予約正規表現のエラーメッセージ.
	private static final String EXP_RESERV_ERROR_MESSAGE = "\"{0}\" is not a {1} format.";
	
	// 正規表現のエラーメッセージ.
	private static final String EXP_ERROR_MESSAGE = "The contents of \"{0}\" do not apply.";
	
	// MinErrorMessage.
	private static final String MIN_ERROR_MESSAGE = "Length of '{0}' is out of condition: min({1})";
	
	// MaxErrorMessage.
	private static final String MAX_ERROR_MESSAGE = "Length of '{0}' is out of condition: max({1})";
	
	// RangeErrorMessage.
	private static final String RANGE_ERROR_MESSAGE = "Length of '{0}' is out of condition: range({1}, {2})";
	
	/**
	 * Validateチェック要素.
	 */
	private static final class VCheckElement {
		private VCheckType type;
		private boolean notFlag;
		private Object[] args;

		/**
		 * コンストラクタ.
		 * @param type
		 * @param notFlag
		 * @param args
		 */
		public VCheckElement(VCheckType type, boolean notFlag, Object... args) {
			this.type = type;
			this.notFlag = notFlag;
			this.args = args;
		}

		/**
		 * VCheckTypeを取得.
		 * @return
		 */
		public VCheckType getType() {
			return type;
		}

		/**
		 * この条件が反転結果であるか取得.
		 * @return
		 */
		public boolean isNot() {
			return notFlag;
		}

		/**
		 * パラメータを取得.
		 * @return
		 */
		public Object[] getArgs() {
			return args;
		}
	}

	// Validateチェック要素を管理するリスト.
	private List<VCheckElement> list = new ArrayList<VCheckElement>();
	
	// オリジナルメッセージ.
	private String originMessage = null;

	/**
	 * コンストラクタ.
	 * @param vtype このValidateの変換型情報を設定します.
	 * @param validate validateチェック条件を設定します.
	 */
	public VCheckElements(VType vtype, String validate) {
		this.originMessage = analysis(list, vtype, validate);
	}

	// Validateチェック条件を解析.
	private static final String analysis(List<VCheckElement> out, VType type, String validate) {
		if (!StringUtil.useString(validate) || Alphabet.eq("none", validate)) {
			// 条件が空か"none"の場合はチェックしない.
			out.clear();
			return null;
		}
		// 情報をカット.
		List<String> list = new ArrayList<String>();
		StringUtil.cutString(list, true, false, validate, " 　\t_|(),");
		if (list.size() == 0) {
			out.clear();
			return null;
		}
		// 区切り条件毎に解析を行う.
		String message = null;
		VCheckType vc;
		Object v1, v2;
		boolean notFlag = false;
		final int len = list.size();
		for (int pos = 0; pos < len; pos ++) {
			// Validateチェックタイプを取得.
			vc = VCheckType.getStringByVCheckType(list.get(pos));
			// 取得に失敗した場合.
			if(vc == null) {
				throw new ValidationException(
					"Unknown validation condition: " + list.get(pos));
			// "none"が設定されている場合.
			} else if(vc == VCheckType.None) {
				// 解析終了.
				return message;
			// "not"定義の場合.
			} else if(vc.equals(VCheckType.Not)) {
				// NotフラグをONにする.
				notFlag = true;
			// Validateチェックタイプのパラメータが0個の場合.
			} else if(vc.getArgsLength() == 0) {
				out.add(new VCheckElement(vc, notFlag));
				notFlag = false;
				// Validateチェックタイプのパラメータが1個の場合.
			} else if(vc.getArgsLength() == 1) {
				// オリジナルメッセージ.
				if(vc.equals(VCheckType.Message)) {
					message = list.get(++pos);
					continue;
				// 正規表現の場合.
				} else if(vc.equals(VCheckType.EXP)) {
					// 正規表現コンパイル.
					v1 = Pattern.compile(list.get(++pos));
				} else {
					// 正規表現以外の場合は型に合わせたデータ変換.
					v1 = VType.convert(type, list.get(++pos));
				}
				out.add(new VCheckElement(vc, notFlag, v1));
				v1 = null;
				notFlag = false;
			// Validateチェックタイプのパラメータが2個の場合.
			} else if(vc.getArgsLength() == 2) {
				v1 = VType.convert(type, list.get(++pos));
				v2 = VType.convert(type, list.get(++pos));
				out.add(new VCheckElement(vc, notFlag, v1, v2));
				v1 = null; v2 = null;
				notFlag = false;
			// その他.
			} else {
				throw new ValidationException(
					"Unknown validation condition: " + list.get(pos));
			}
		}
		return message;
	}

	/**
	 * チェック処理.
	 * @param vtype
	 * @param column
	 * @param value
	 * @return
	 */
	public Object check(VType vtype, String column, Object value) {
		VCheckElement em;
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			em = list.get(i);
			switch(em.getType()) {
			case None:
			case Not: continue; // 何もしない.
			case Null:
				value = isNull(em, originMessage, vtype, column, value);
				break;
			case Date:
				value = date(em, originMessage, column, value);
				break;
			case Time:
				value = time(em, originMessage, column, value);
				break;
			case Zip:
				value = zip(em, originMessage, column, value);
				break;
			case Tel:
				value = tel(em, originMessage, column, value);
				break;
			case Ipv4:
				value = ipv4(em, originMessage, column, value);
				break;
			case Url:
				value = url(em, originMessage, column, value);
				break;
			case Email:
				value = email(em, originMessage, column, value);
				break;
			case EXP:
				value = exp(em, originMessage, column, value);
				break;
			case LT:
				value = max(em, originMessage, true, vtype, column, value);
				break;
			case LE:
				value = max(em, originMessage, false, vtype, column, value);
				break;
			case GT:
				value = min(em, originMessage, true, vtype, column, value);
				break;
			case GE:
				value = min(em, originMessage, false, vtype, column, value);
				break;
			case Min:
				value = min(em, originMessage, true, vtype, column, value);
				break;
			case Max:
				value = max(em, originMessage, true, vtype, column, value);
				break;
			case Range:
				value = range(em, originMessage, vtype, column, value);
				break;
			case Default:
				value = defaultValue(em, vtype, column, value);
				break;
			case Message:
				// 何もしない.
				break;
			}
		}
		return value;
	}
	
	// null.
	private static final Object isNull(VCheckElement em, String origin, VType vtype,
		String column, Object value) {
		boolean res;
		if(VType.String.equals(vtype)) {
			// 文字列の場合はnullか空の場合はデフォルト定義.
			res = (value == null || value.toString().isEmpty());
		} else {
			res = (value == null);
		}
		if (res == !em.isNot()) {
			throw new ValidationException(400,
				message(origin, NULL_MESSAGE, column, em.isNot() ? " not null" : "null"));
		}
		return value;
	}
	
	// date.
	private static final Object date(VCheckElement em, String origin, String column, Object value) {
		try {
			if((DateUtil.parseDate(value) != null) == em.isNot()) {
				return value;
			}
		} catch(Exception e) {}
		return exp(DATE_EXP, em, origin, column, value,
			message(origin, EXP_RESERV_ERROR_MESSAGE, column, "date"));
	}

	// time.
	private static final Object time(VCheckElement em, String origin, String column, Object value) {
		return exp(TIME_EXP, em, origin, column, value,
			message(origin, EXP_RESERV_ERROR_MESSAGE, column, "time"));

	}

	// zip.
	private static final Object zip(VCheckElement em, String origin, String column, Object value) {
		return exp(ZIP_EXP, em, origin, column, value,
			message(origin, EXP_RESERV_ERROR_MESSAGE, column, "zip"));
	}

	// tel.
	private static final Object tel(VCheckElement em, String origin, String column, Object value) {
		return exp(TEL_EXP, em, origin, column, value,
			message(origin, EXP_RESERV_ERROR_MESSAGE, column, "telephone"));
	}
	// ipv4.
	private static final Object ipv4(VCheckElement em, String origin, String column, Object value) {
		return exp(IPV4_EXP, em, origin, column, value,
			message(origin, EXP_RESERV_ERROR_MESSAGE, column, "ipAddress(ipv4)"));
	}

	// url.
	private static final Object url(VCheckElement em, String origin, String column, Object value) {
		return exp(URL_EXP, em, origin, column, value,
			message(origin, EXP_RESERV_ERROR_MESSAGE, column, "url"));
	}

	// email.
	private static final Object email(VCheckElement em, String origin, String column, Object value) {
		return exp(EMAIL_EXP, em, origin, column, value,
			message(origin, EXP_RESERV_ERROR_MESSAGE, column, "email") );
	}
	
	// exp.
	private static final Object exp(VCheckElement em, String origin, String column, Object value) {
		return exp((Pattern)em.getArgs()[0], em, origin, column, value,
			message(origin, EXP_ERROR_MESSAGE, column));
	}
	
	// min [number].
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final Object min(VCheckElement em, String origin, boolean eq, VType vtype,
		String column, Object value) {
		Comparable d = (Comparable)em.getArgs()[0];
		if (value == null) {
			throw new ValidationException(400, message(origin, MIN_ERROR_MESSAGE, column, d));
		}
		Comparable s = (Comparable)VType.convert(vtype, column, value);
		// valueの方が大きい場合はエラー.
		if((eq && (s.compareTo(d) >= 0) == em.isNot()) ||
			(!eq && (s.compareTo(d) > 0) == em.isNot())) {
			throw new ValidationException(400, message(origin, MIN_ERROR_MESSAGE, column, d));
		}
		return value;
	}
	
	// max [number].
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final Object max(VCheckElement em, String origin, boolean eq, VType vtype,
		String column, Object value) {
		Comparable d = (Comparable)em.getArgs()[0];
		if (value == null) {
			throw new ValidationException(400, message(origin, MAX_ERROR_MESSAGE, column, d));
		}
		Comparable s = (Comparable)VType.convert(vtype, column, value);
		// valueの方が小さい場合はエラー.
		if((eq && (s.compareTo(d) <= 0) == em.isNot()) ||
			(!eq && (s.compareTo(d) < 0) == em.isNot())) {
			throw new ValidationException(400, message(origin, MAX_ERROR_MESSAGE, column, d));
		}
		return value;
	}
	
	// range [number number].
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final Object range(VCheckElement em, String origin, VType vtype, String column,
		Object value) {
		Comparable s = (Comparable)VType.convert(vtype, column, value);
		Comparable d1 = (Comparable)em.getArgs()[0];
		Comparable d2 = (Comparable)em.getArgs()[1];
		if (value == null) {
			throw new ValidationException(400, message(origin, RANGE_ERROR_MESSAGE, column, d1, d2));
		}
		// valueが範囲外の場合はエラー.
		if((s.compareTo(d1) >= 0) == em.isNot() || (s.compareTo(d2) <= 0) == em.isNot()) {
			throw new ValidationException(400, message(origin, RANGE_ERROR_MESSAGE, column, d1, d2));
		}
		return value;
	}

	// default [value].
	private static final Object defaultValue(VCheckElement em, VType vtype, String column, Object value) {
		if(VType.String.equals(vtype)) {
			// 文字列の場合はnullか空の場合はデフォルト定義.
			if(value == null || value.toString().isEmpty()) {
				return em.getArgs()[0];
			}
		} else if(value == null) {
			return em.getArgs()[0];
		}
		return value;
	}

	// 予約正規表現.
	private static final Pattern DATE_EXP = Pattern
		.compile("^\\d{2,4}\\/([1][0-2]|[0][1-9]|[1-9])\\/([3][0-1]|[1-2][0-9]|[0][1-9]|[1-9])$");
	private static final Pattern TIME_EXP = Pattern
		.compile("^([0-1][0-9]|[2][0-3]|[0-9])\\:([0-5][0-9]|[0-9])$");
	private static final Pattern ZIP_EXP = Pattern.compile("^\\d{3}-\\d{4}$");
	private static final Pattern TEL_EXP = Pattern.compile("^[0-9]+\\-[0-9]+\\-[0-9]+$");
	private static final Pattern IPV4_EXP = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
	private static final Pattern URL_EXP = Pattern.compile("https?://[\\w/:%#\\$&\\?\\(\\)~\\.=\\+\\-]+");
	private static final Pattern EMAIL_EXP = Pattern.compile("\\w{1,}[@][\\w\\-]{1,}([.]([\\w\\-]{1,})){1,3}$");

	// exp.
	private static final Object exp(Pattern p, VCheckElement em, String origin, String column,
		Object value, String message) {
		if (value == null) {
			throw new ValidationException(400, message);
		}
		final Matcher mc = p.matcher(StringUtil.parseString(value));
		if (em.isNot()) {
			if (mc.find()) {
				throw new ValidationException(400, message);
			}
		} else if (!mc.find()) {
			throw new ValidationException(400, message);
		}
		return value;
	}
	
	// エラーメッセージを出力.
	// origin オリジナルメッセージをセット.
	//        nullか空の場合はデフォルトメッセージを利用.
	// def デフォルトメッセージをセット.
	// args パラメータをセット.
	private static final String message(String origin, String def, Object... args) {
		if(origin != null && !origin.isEmpty()) {
			return message(origin, args);
		}
		return message(def, args);
	}
	
	// エラーメッセージを出力.
	// メッセージのフォーマットは以下の通り.
	//
	// msg="xxxx{0} yyyy{1}"
	// message(msg, "hoge", "moge");
	// 結果: "xxxxhoge yyyymoge"
	//
	private static final String message(String msg, Object... args) {
		int p, pp, no, b = 0;
		String s;
		final StringBuilder buf = new StringBuilder();
		while(true) {
			p = msg.indexOf("{", b);
			if(p == -1) {
				buf.append(msg.substring(b));
				break;
			}
			buf.append(msg.substring(b, p));
			pp = msg.indexOf("}", p);
			if(pp == -1) {
				buf.append(msg.substring(p));
				break;
			}
			s = msg.substring(p + 1, pp).trim();
			if(!NumberUtil.isNumeric(s)) {
				buf.append(msg.substring(p, pp));
				b = pp;
			} else {
				no = NumberUtil.parseInt(s);
				if(no < 0 || no >= args.length) {
					buf.append(msg.substring(p, pp));
					b = pp;
				} else {
					buf.append(args[no]);
					b = pp + 1;
				}
			}
		}
		return buf.toString();
	}
}
