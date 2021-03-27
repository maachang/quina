package quina.validate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import quina.util.Alphabet;
import quina.util.DateUtil;
import quina.util.StringUtil;

/**
 * １つのValidateチェック要素群.
 */
final class VCheckElements {
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

	/**
	 * コンストラクタ.
	 * @param vtype このValidateの変換型情報を設定します.
	 * @param validate validateチェック条件を設定します.
	 */
	public VCheckElements(VT vtype, String validate) {
		analysis(list, vtype, validate);
	}

	// Validateチェック条件を解析.
	private static final void analysis(List<VCheckElement> out, VT type, String validate) {
		if (!StringUtil.useString(validate) || Alphabet.eq("none", validate)) {
			// 条件が空か"none"の場合はチェックしない.
			out.clear();
			return;
		}
		// 情報をカット.
		List<String> list = new ArrayList<String>();
		StringUtil.cutString(list, true, false, validate, " 　\t_|(),");
		if (list.size() == 0) {
			out.clear();
			return;
		}
		// 区切り条件毎に解析を行う.
		VCheckType vc;
		Object v1, v2;
		boolean notFlag = false;
		final int len = list.size();
		for (int pos = 0; pos < len; pos ++) {
			// Validateチェックタイプを取得.
			vc = VCheckType.getStringByVCheckType(list.get(pos));
			// 取得に失敗した場合.
			if(vc == null) {
				throw new ValidateException(
					"Unknown validation condition: " + list.get(pos));
			// "none"が設定されている場合.
			} else if(vc == VCheckType.None) {
				// 解析終了.
				return;
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
				// 正規表現の場合.
				if(vc.equals(VCheckType.EXP)) {
					// 正規表現コンパイル.
					v1 = Pattern.compile(list.get(++pos));
				} else {
					// 正規表現以外の場合は型に合わせたデータ変換.
					v1 = VT.convert(type, list.get(++pos));
				}
				out.add(new VCheckElement(vc, notFlag, v1));
				v1 = null;
				notFlag = false;
			// Validateチェックタイプのパラメータが2個の場合.
			} else if(vc.getArgsLength() == 2) {
				v1 = VT.convert(type, list.get(++pos));
				v2 = VT.convert(type, list.get(++pos));
				out.add(new VCheckElement(vc, notFlag, v1, v2));
				v1 = null; v2 = null;
				notFlag = false;
			// その他.
			} else {
				throw new ValidateException(
					"Unknown validation condition: " + list.get(pos));
			}
		}
	}

	/**
	 * チェック処理.
	 * @param vtype
	 * @param column
	 * @param value
	 * @return
	 */
	public Object check(VT vtype, String column, Object value) {
		VCheckElement em;
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			em = list.get(i);
			switch(em.getType()) {
			case None:
			case Not: continue; // 何もしない.
			case Null:
				value = isNull(em, vtype, column, value);
				break;
			case Date:
				value = date(em, column, value);
				break;
			case Time:
				value = time(em, column, value);
				break;
			case Zip:
				value = zip(em, column, value);
				break;
			case Tel:
				value = tel(em, column, value);
				break;
			case Ipv4:
				value = ipv4(em, column, value);
				break;
			case Url:
				value = url(em, column, value);
				break;
			case Email:
				value = email(em, column, value);
				break;
			case EXP:
				value = exp(em, column, value);
				break;
			case LT:
				value = min(em, true, vtype, column, value);
				break;
			case LE:
				value = min(em, false, vtype, column, value);
				break;
			case GT:
				value = max(em, true, vtype, column, value);
				break;
			case GE:
				value = max(em, false, vtype, column, value);
				break;
			case Min:
				value = min(em, true, vtype, column, value);
				break;
			case Max:
				value = max(em, true, vtype, column, value);
				break;
			case Range:
				value = range(em, vtype, column, value);
				break;
			case Default:
				value = defaultValue(em, vtype, column, value);
			}
		}
		return value;
	}

	// not が付与されている場合の例外メッセージ追加.
	private static final String notOut(VCheckElement em) {
		return em.isNot() ? " not" : "";
	}

	// null.
	private static final Object isNull(VCheckElement em, VT vtype, String column, Object value) {
		boolean res;
		if(VT.String.equals(vtype)) {
			// 文字列の場合はnullか空の場合はデフォルト定義.
			res = (value == null || value.toString().isEmpty());
		} else {
			res = (value == null);
		}
		if (res == !em.isNot()) {
			throw new ValidateException(400,
				"The value of '" + column + "' is" + notOut(em) + " null");
		}
		return value;
	}

	// date.
	private static final Object date(VCheckElement em, String column, Object value) {
		try {
			if((DateUtil.parseDate(value) != null) == em.isNot()) {
				return value;
			}
		} catch(Exception e) {}
		return exp(DATE_EXP, em, column, value, "\"" + column + "\" is not a date format.");
	}

	// time.
	private static final Object time(VCheckElement em, String column, Object value) {
		return exp(TIME_EXP, em, column, value, "\"" + column + "\" is not a time format.");
	}

	// zip.
	private static final Object zip(VCheckElement em, String column, Object value) {
		return exp(ZIP_EXP, em, column, value, "\"" + column + "\" is not a zip format.");
	}

	// tel.
	private static final Object tel(VCheckElement em, String column, Object value) {
		return exp(TEL_EXP, em, column, value, "\"" + column + "\" is not a telephone format.");
	}
	// ipv4.
	private static final Object ipv4(VCheckElement em, String column, Object value) {
		return exp(IPV4_EXP, em, column, value, "\"" + column +
			"\" is not in the format of IP address (IPV4).");
	}

	// url.
	private static final Object url(VCheckElement em, String column, Object value) {
		return exp(URL_EXP, em, column, value, "\"" + column + "\" is not a url format.");
	}

	// email.
	private static final Object email(VCheckElement em, String column, Object value) {
		return exp(EMAIL_EXP, em, column, value, "\"" + column + "\" is not a email format.");
	}

	// exp.
	private static final Object exp(VCheckElement em, String column, Object value) {
		return exp((Pattern)em.getArgs()[0], em, column, value,
			"The contents of \"" + column + "\" do not apply.");
	}

	// min [number].
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final Object min(VCheckElement em, boolean eq, VT vtype, String column, Object value) {
		if (value == null) {
			throw new ValidateException(400,
				"The value of '" + column + "' is null.");
		}
		Comparable s = (Comparable)VT.convert(vtype, column, value);
		Comparable d = (Comparable)em.getArgs()[0];
		// valueの方が大きい場合はエラー.
 		if((eq && (s.compareTo(d) >= 0) == em.isNot()) ||
 			(!eq && (s.compareTo(d) > 0) == em.isNot())) {
			throw new ValidateException(400,
				"Length of '" + column + "' is out of condition: min(" + d + ")");
		}
		return value;
	}

	// max [number].
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final Object max(VCheckElement em, boolean eq, VT vtype, String column, Object value) {
		if (value == null) {
			throw new ValidateException(400,
				"The value of '" + column + "' is null.");
		}
		Comparable s = (Comparable)VT.convert(vtype, column, value);
		Comparable d = (Comparable)em.getArgs()[0];
		// valueの方が小さい場合はエラー.
		if((eq && (s.compareTo(d) <= 0) == em.isNot()) ||
			(!eq && (s.compareTo(d) < 0) == em.isNot())) {
			throw new ValidateException(400,
				"Length of '" + column + "' is out of condition: max(" + d + ")");
		}
		return value;
	}

	// range [number number].
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final Object range(VCheckElement em, VT vtype, String column, Object value) {
		if (value == null) {
			throw new ValidateException(400,
				"The value of '" + column + "' is null.");
		}
		Comparable s = (Comparable)VT.convert(vtype, column, value);
		Comparable d1 = (Comparable)em.getArgs()[0];
		Comparable d2 = (Comparable)em.getArgs()[1];
		// valueが範囲外の場合はエラー.
		if((s.compareTo(d1) >= 0) == em.isNot() || (s.compareTo(d2) <= 0) == em.isNot()) {
			throw new ValidateException(400,
				"Length of '" + column + "' is out of condition: range(" + d1 + ", " + d2 + ")");
		}
		return value;
	}

	// default [value].
	private static final Object defaultValue(VCheckElement em, VT vtype, String column, Object value) {
		if(VT.String.equals(vtype)) {
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
	private static final Object exp(Pattern p, VCheckElement em, String column, Object value, String message) {
		if (value == null) {
			throw new ValidateException(400, "The value of '" + column + "' is null.");
		}
		final Matcher mc = p.matcher(StringUtil.parseString(value));
		if (em.isNot()) {
			if (mc.find()) {
				throw new ValidateException(400, message);
			}
		} else if (!mc.find()) {
			throw new ValidateException(400, message);
		}
		return value;
	}
}
