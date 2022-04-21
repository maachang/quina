package quina.smple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.Proxy;

import quina.exception.QuinaException;
import quina.json.Json;
import quina.json.JsonOut;
import quina.textanalysis.TextScript;
import quina.textanalysis.UserAnalysis;
import quina.util.Alphabet;
import quina.util.Quotation;
import quina.util.ResourceUtil;
import quina.util.StringUtil;
import quina.util.collection.ObjectList;

/**
 * Simple Template解析処理.
 * 仕様については
 * https://github.com/maachang/quina/issues/1
 * を参照してください.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SmpleAnalysis {
	
	/**
	 * java パッケージ名.
	 */
	public static final String JAVA_PACKAGE = "quinax.smple";
	
	// smple定義用のJsonシンボル.
	private static final String SMPLE = "$smple";
	
	// smple出力シンボル.
	private static final String OUT_SIMBOLE = "$out";
	
	// smpleの処理結果を返却する.
	private static final String SMPLE_$OUT_RESULT = "resultOut";
	
	
	// [java]Smpleオブジェクト.
	private static final String JAVA_SMPLE_OBJECT = "Smple";
	
	// [java]SmpleOutオブジェクト.
	private static final String JAVA_SMPLE_OUT_OBJECT = "SmpleOut";
	
	// [java]SmpleBeanオブジェクト.
	private static final String JAVA_SMPLE_BEAN_OBJECT = "SmpleBean";
	
	// [java]SmpleBeanのデータ格納先名.
	private static final String JAVA_SMPLE_BEAN_VALUES = "values";
	
	// [java]デフォルトJava Import群.
	private static final String[] JAVA_DEFAULT_JAVA_IMPORTS = new String[] {
		"java.util.*"
		,"quina.smple.*"
	};
	
	// [java]smpleメソッド引数名.
	private static final String JAVA_SMPLE_METHOD_ARGS = "args";
	
	// [java]smpleBean定義.
	private static final String JAVA_SMPLE_BEAN_KEY_DEFINE = SMPLE + ".beans.";
	
	// [javascript]smpleパラメータ名.
	private static final String JS_SMPLE_PARAMS_NAME = "args";
	
	/**
	 * smple for Javaコンパイル実行.
	 * @param outClassName new String[1]を定義することで、生成された
	 *                     javaオブジェクトのパッケージ名＋クラス名が
	 *                     取得できます.
	 * @param name このSmpleのオブジェクト名となる条件を設定します.
	 *             smple内に $smple.name 定義が行われてる場合はこの条件は
	 *             不要です.
	 *             またこの条件は主に smpleをリソース管理している場合の
	 *             パッケージ名とsmpleファイル名を指定します.
	 * @param template テンプレート情報を設定します.
	 * @return String smpleコンパイル結果が返却されます.
	 */
	public static final String compileJava(
		String[] outClassName, String name, String template) {
		TextScript ts = new TextScript(template);
		
		// ${ ... } の内容を <%= ... %> に変換.
		changeDollarBrackets(ts);
		
		// $smpleのJSON定義を取得.
		Map<String, Object> json = getJavaJson(SMPLE, ts);
		
		// smple実行クラス名を設定します.
		String className = getSmpleJavaObjectName(name, json);
		
		// 読み込みパッケージ名を取得.
		String packageNames = getJavaImportPackages(json);
		
		// javaBeansを取得.
		String[] beans = convertJavaBean(1, json);
		
		// javaArgs変換を取得.
		String args = getJavaConvertArgs(2, json);
		
		// 有効な<% ... %> のポジションを取得.
		ObjectList<Integer> list = getSmplePosList(ts);
		
		// プログラム実行が可能なスクリプト変換.
		String smple = convertSmpleByExecuteProgram(
			2, ts, list);
		list = null;
		
		// 結合.
		StringBuilder buf = new StringBuilder();
		
		// package名をセット.
		buf.append("package ")
			.append(JAVA_PACKAGE)
			.append(";\n\n");
		
		// import package名をセット.
		buf.append(packageNames)
			.append("\n");
		packageNames = null;
		
		// クラス名定義をセット.
		buf.append(getJavaSmpleClass(0, className));
		
		// 各自JavaBeanをセット.
		int len = beans.length;
		for(int i = 0; i < len; i ++) {
			if(i == 0) {
				buf.append("\n");
			}
			buf.append(beans[i])
				.append("\n");
		}
		
		// bean生成メソッドをセット.
		buf.append(getJavaCreateSmpleMethod(1, json))
			.append("\n");
		
		// 実行メソッド名をセット.
		appendTab(buf, 1)
			.append(getJavaSmpleMethod())
			.append("\n");
		
		// args変換定義をセット.
		buf.append(args)
			.append("\n");
		
		// SmpleOutオブジェクトを生成.
		appendTab(buf, 2)
			.append(getJavaSmpleOut())
			.append("\n");
		
		// 実行可能なsmple定義をセット.
		buf.append(smple);
		
		// SmpleOutを文字列変換する定義をセット.
		buf.append("\n")
			.append(resultJavaSmpleOut(2));
		
		// メソッド終端をセット.
		buf.append(endIndent(1));
		
		// クラス終端をセット.
		buf.append(endIndent(0));
		
		// クラス名を返却する場合.
		if(outClassName != null && outClassName.length > 0) {
			// 出力対象のパッケージ名＋クラス名を返却.
			outClassName[0] = JAVA_PACKAGE + "." + className;
		}
		
		return buf.toString();
	}
	
	/**
	 * smple for Javascriptコンパイル実行.
	 * @param template テンプレート情報を設定します.
	 * @return String smpleコンパイル結果が返却されます.
	 */
	public static final String compileJs(String template) {
		TextScript ts = new TextScript(template);
		
		// ${ ... } の内容を <%= ... %> に変換.
		changeDollarBrackets(ts);
		
		// $smpleのJSON定義を取得.
		Map<String, Object> json = getJavaJson(SMPLE, ts);
		
		// 有効な<% ... %> のポジションを取得.
		ObjectList<Integer> list = getSmplePosList(ts);
		
		// プログラム実行が可能なスクリプト変換.
		String smple = convertSmpleByExecuteProgram(
			0, ts, list);
		list = null;
		
		// 結合.
		StringBuilder buf = new StringBuilder();
		
		// header.
		buf.append(getJsHeaderScript());
		
		// $smpleJSONが存在する場合.
		if(json != null) {
			// jsonを展開.
			buf.append(SMPLE)
				.append(" = ")
				.append(JsonOut.toString(json))
				.append("\n");
		}
		
		// smpleOut.
		buf.append(getJsSmpleOutScript());
		
		// テンプレートスクリプト.
		buf.append('\n')
			.append(smple)
			.append(resultJSSmpleOut(0));
		
		// footer.
		buf.append(getJsFooterScript());
		
		return buf.toString();
	}

	
	// クォーテーションとコメントに対応した読み込み処理.
	private static final class QuotationAndComment
		implements UserAnalysis {
		private boolean javaFlag;
		
		/**
		 * コンストラクタ.
		 * @param javaFlag Java or Javascriptの判別の
		 *                 場合は trueを設定.
		 */
		protected QuotationAndComment(boolean javaFlag) {
			this.javaFlag = javaFlag;
		}
		
		@Override
		public boolean analysis(TextScript ts) {
			boolean ret = false;
			// クォーテーション[" ']
			// コメント[/* ... */ // #] を
			// 読み飛ばす.
			while(!ts.isEOF()) {
				// スペース等を読み飛ばす.
				ts.moveToSkipBlank();
				// クォーテーション.
				if(ts.moveToBlockQuotation()) {
					ret = true;
					continue;
				}
				// [/* ... */]コメント.
				if(ts.moveToBlockComment()) {
					ret = true;
					continue;
				}
				// [//]コメント.
				if(ts.moveToLineTwoSlashComment()) {
					ret = true;
					continue;
				}
				// javaの場合は不可.
				// [#]コメント.
				if(!javaFlag && ts.moveToLineTwoSlashComment()) {
					ret = true;
					continue;
				}
				break;
			}
			return ret;
		}
	}
	// quinaJSONの解析用.
	private static final UserAnalysis
		quotationAndComment = new QuotationAndComment(false);
	
	// Java or Javascript用の解析用.
	private static final UserAnalysis
		javaQuotationAndComment = new QuotationAndComment(true);
	
	// java用のJSONとテンプレート内容を分離.
	private static final Map<String, Object> getJavaJson(
		String simbol, TextScript ts) {
		int startPos, middlePos;
		// ポジションクリア.
		ts.clearPosition();
		while(true) {
			// シンボルを検索.
			if(!ts.moveToIndexOf(simbol)) {
				// 存在しない場合.
				ts.clearPosition();
				return null;
			}
			// $smpleの開始ポジションを取得.
			startPos = ts.getPosition();
			
			// simbol { ... }
			//   or simbol : { ... }
			//   or simbol = { ... }
			// のどれかを検索.
			if(!ts.moveToAllMatch(simbol, "{") &&
				!ts.moveToAllMatch(simbol, ":", "{") &&
				!ts.moveToAllMatch(simbol, "=", "{")) {
				// 見つからない場合は、次の $smple を検索.
				ts.setPosition(startPos + 1);
				continue;
			}
			
			// { の分まで移動してるので、ポジションを１つ戻る.
			ts.before();
			middlePos = ts.getPosition();
			
			// 終端を取得.
			if(ts.moveToBracketsByRange(quotationAndComment)) {
				// 終端以降の改行は読み飛ばす.
				ts.moveToSkipEnter();
				int endPos = ts.getPosition();
				// 戻り値は $smple { でなく { の部分から取得.
				// コメントをカットして、JSONデコードして返却.
				return (Map<String, Object>)Json.decode(
					Json.cutComment(false,
						ts.substring(startPos, endPos)
							.substring(middlePos - startPos)));
			} else {
				// 見つからない場合は、次の $smple を検索.
				ts.setPosition(startPos + 1);
			}
		}
	}
	
	// ${ ... } を <%= .... %> に変換する.
	private static final void changeDollarBrackets(
		TextScript ts) {
		int startPos, endPos;
		ts.clearPosition();
		// ${ ... } の条件をあるまで検索する.
		while(ts.moveToIndexOf("${")) {
			// 開始ポジションを取得.
			startPos = ts.getPosition();
			// 開始ポジションの文字を移動.
			ts.next(2);
			
			// } が見つかるまでループ.
			while(!ts.isEOF()) {
				// スペース等を読み飛ばす.
				ts.moveToSkipBlank();
				
				// java解析用の条件が存在する場合.
				if(javaQuotationAndComment.analysis(ts)) {
					continue;
				}
				
				// 終端を取得.
				if(!ts.isString("}")) {
					// 終端で無い場合.
					ts.next();
					continue;
				}
				
				// 終端を取得.
				endPos = ts.getPosition();
				
				// 置き換え処理は後ろから.
				ts.pluginString(endPos, endPos + 1, "%>");
				ts.pluginString(startPos, startPos + 2, "<%=");
				break;
			}
		}
	}
	
	// Smpleテンプレートを解析してポジションを取得.
	private static final ObjectList<Integer> getSmplePosList(
		TextScript ts) {
		int startPos, endPos;
		final ObjectList<Integer> ret = new ObjectList<Integer>();
		ts.clearPosition();
		// <% ... %> の条件をあるまで検索する.
		while(ts.moveToIndexOf("<%")) {
			// 開始ポジションを取得.
			startPos = ts.getPosition();
			// 開始ポジションの文字を移動.
			ts.next(2);
			
			// %> が見つかるまでループ.
			while(!ts.isEOF()) {
				// スペース等を読み飛ばす.
				ts.moveToSkipBlank();
				
				// java解析用の条件が存在する場合.
				if(javaQuotationAndComment.analysis(ts)) {
					continue;
				}
				
				// 終端を取得.
				if(!ts.isString("%>")) {
					ts.next();
					continue;
				}
				
				// 終端を取得.
				endPos = ts.getPosition();
				
				// 位置情報を取得.
				ret.add(startPos);
				ret.add(endPos + 2);
				break;
			}
		}
		return ret;
	}
	
	// タブを追加
	private static final StringBuilder appendTab(
		StringBuilder buf, int count) {
		for(int i = 0; i < count; i ++) {
			buf.append("\t");
		}
		return buf;
	}
	
	// テンプレート部分の出力を変換.
	private static final String convertTemplate(String template) {
		// 改行とタブをupper.
		template = StringUtil.changeString(template, "\n", "\\n");
		template = StringUtil.changeString(template, "\t", "\\t");
		// ダブルクォーテーションをupper.
		return Quotation.upDoubleQuotation(template);
	}
	
	// smpleをプログラム実行用に変換.
	private static final String convertSmpleByExecuteProgram(
		int tabCount, TextScript ts, ObjectList<Integer> posList) {
		final String script = ts.toString();
		final StringBuilder buf = new StringBuilder();
		final int len = posList.size();
		
		int pos = 0;
		int startPos, endPos;
		String executeScript;
		for(int i = 0; i < len; i += 2) {
			startPos = posList.get(i);
			endPos = posList.get(i + 1);
			// テンプレート部分を出力.
			appendTab(buf, tabCount).
				append(OUT_SIMBOLE).append(".print(\"")
				.append(convertTemplate(script.substring(pos, startPos)))
				.append("\");\n");
			// 次のテンプレート開始位置.
			pos = endPos;
			
			// 実行スクリプトを取得.
			executeScript = script
				.substring(startPos + 2, endPos - 2);
			
			// 実行スクリプトの頭に = が存在する場合.
			if(executeScript.startsWith("=")) {
				// 直接結果を出力する.
				appendTab(buf, tabCount)
					.append(OUT_SIMBOLE).append(".print(")
					.append(executeScript.substring(1).trim())
					.append(");\n");
				
			// 実行スクリプトの頭に = が存在する場合.
			} else if(executeScript.startsWith("//") ||
				executeScript.startsWith("#")) {
				// コメントなので読み飛ばす.
				
			// それ以外.
			} else {
				// 前後のスペースを除外.
				String trimScript = executeScript.trim();
				// インデントの終端.
				if(Alphabet.eq("$start", trimScript)) {
					appendTab(buf, tabCount).append("{\n");
				// インデントの終端.
				} else if(Alphabet.eq("$end", trimScript)) {
					appendTab(buf, tabCount).append("}\n");
				// 通常の実行スクリプトの場合.
				} else {
					appendTab(buf, tabCount)
						.append(executeScript)
						.append("\n");
				}
			}
		}
		
		// 一番終端のテンプレート部分を出力.
		appendTab(buf, tabCount)
			.append(OUT_SIMBOLE).append(".print(\"")
			.append(convertTemplate(script.substring(pos)))
			.append("\");\n");
		
		return buf.toString();
	}
	
	// [Java]変数定義を取得.
	// たとえば
	//   "String hoge"
	// の場合、この処理での変換結果は
	//   new String[] {"String", "hoge"}
	// となります.
	private static final String[] javaVariable(String define) {
		if(define == null) {
			throw new QuinaException(
				"Java variable definition does not exist.");
		}
		define = define.trim();
		if(define.isEmpty()) {
			throw new QuinaException(
				"Java variable definition does not exist.");
		}
		if(define.endsWith(";")) {
			define = define
				.substring(0, define.length() - 1)
				.trim();
		}
		char c;
		final int len = define.length();
		for(int i = len-1; i >= 0; i --) {
			c = define.charAt(i);
			if(c == ' ' || c == '\t' || c == '\r' || c == '\n') {
				return new String[] {
					define.substring(0, i).trim(),
					define.substring(i + 1).trim()
				};
			}
		}
		throw new QuinaException("Target string: \"" +
			define + "\" is not a Java variable definition. ");
	}
	
	// [java]指定変数定義がjavaのprimitive型の場合、オブジェクト名に変換.
	private static final String convertJavaPrimitive(String key) {
		if("boolean".equals(key)) {
			return "Boolean";
		} else if("byte".equals(key)) {
			return "Byte";
		} else if("char".equals(key)) {
			return "Character";
		} else if("short".equals(key)) {
			return "Short";
		} else if("int".equals(key)) {
			return "Integer";
		} else if("long".equals(key)) {
			return "Long";
		} else if("float".equals(key)) {
			return "Float";
		} else if("double".equals(key)) {
			return "Double";
		}
		return key;
	}
	
	// [java]setterのset処理定義を取得.
	private static final String convertJavaSetterSet(
		String name) {
		return JAVA_SMPLE_BEAN_VALUES +
			".put(\"" + name + "\", value);";
	}
	
	// [java]getter / setter 名を取得.
	private static final String convertJavaGetterSetter(
		boolean setter, String key) {
		String ret = setter ? "set" : "get";
		return ret
			+ key.substring(0, 1).toUpperCase()
			+ key.substring(1);
	}
	
	// [java]getterのreturn定義を取得.
	private static final String convertJavaGetterReturn(
		String key, String name) {
		if("boolean".equals(key) ||
			"Boolean".equals(key)) {
			return JAVA_SMPLE_BEAN_VALUES +
				".getBoolean(\"" + name + "\");";
		} else if("byte".equals(key) ||
			"Byte".equals(key)) {
			return JAVA_SMPLE_BEAN_VALUES +
				".getByte(\"" + name + "\");";
		} else if("char".equals(key) ||
			"Character".equals(key)) {
			return JAVA_SMPLE_BEAN_VALUES +
				".get(\"" + name + "\") == null ? " +
				"null : (char)" + JAVA_SMPLE_BEAN_VALUES +
				".getInteger(\"" + name + "\")";
		} else if("short".equals(key) ||
			"Short".equals(key)) {
			return JAVA_SMPLE_BEAN_VALUES +
				".getShort(\"" + name + "\");";
		} else if("int".equals(key) ||
			"Integer".equals(key)) {
			return JAVA_SMPLE_BEAN_VALUES +
				".getInteger(\"" + name + "\");";
		} else if("long".equals(key) ||
			"Long".equals(key)) {
			return JAVA_SMPLE_BEAN_VALUES +
				".getLong(\"" + name + "\");";
		} else if("float".equals(key) ||
			"Float".equals(key)) {
			return JAVA_SMPLE_BEAN_VALUES +
				".getFloat(\"" + name + "\");";
		} else if("double".equals(key) ||
			"Double".equals(key)) {
			return JAVA_SMPLE_BEAN_VALUES +
				".getDouble(\"" + name + "\");";
		} else if("String".equals(key) ||
			"java.lang.String".equals(key)) {
			return JAVA_SMPLE_BEAN_VALUES +
				".getString(\"" + name + "\");";
		} else if("Data".equals(key) ||
			"java.util.Date".equals(key)) {
			return JAVA_SMPLE_BEAN_VALUES +
				".getDate(\"" + name + "\");";
		}
		return "(" + key + ")" + JAVA_SMPLE_BEAN_VALUES +
			".get(\"" + name + "\");";
	}
	
	// [java]smple内のjavaBean定義を取得.
	private static final String[] convertJavaBean(
		int tabCount, Map<String, Object> json) {
		// beans一覧を取得.
		Object o = json.get("beans");
		if(o == null || !(o instanceof Map) ||
			((Map)o).size() == 0) {
			// 存在しない場合.
			return new String[] {};
		}
		int len, i;
		String name, key;
		String[] val;
		int count = 0;
		List<String> vals;
		final Map<String, Object> beans = (Map<String, Object>)o;
		final String[] ret = new String[beans.size()];
		final Iterator<String> itr = beans.keySet().iterator();
		// beans毎の処理実行.
		while(itr.hasNext()) {
			// bean名.
			name = itr.next();
			// bean内のgetter/setterを生成.
			o = beans.get(name);
			if(o instanceof List) {
				vals = (List<String>)o;
			} else if(o instanceof String) {
				vals = new ArrayList<String>(1);
				vals.add((String)o);
			} else {
				vals = new ArrayList<String>(1);
			}
			// staticなクラスを生成.
			StringBuilder buf = new StringBuilder();
			appendTab(buf, tabCount)
				.append("public static final class ")
				.append(name)
				.append(" extends ")
				.append(JAVA_SMPLE_BEAN_OBJECT)
				.append(" {\n");
			// getter / setterメソッドを生成.
			len = vals.size();
			for(i = 0; i < len; i ++) {
				// "String hoge" の定義を
				// new String[] {"String", "hoge"}に変換.
				val = javaVariable(vals.get(i));
				// primitive型の場合はオブジェクト名に変換.
				key = convertJavaPrimitive(val[0]);
				
				// 改行.
				if(i != 0) {
					buf.append("\n");
				}
				
				// setter.
				appendTab(buf, tabCount + 1)
					.append("public void ")
					.append(convertJavaGetterSetter(true, val[1]))
					.append("(")
					.append(key)
					.append(" value) {\n");
				appendTab(buf, tabCount + 2)
					.append(convertJavaSetterSet(val[1]))
					.append("\n");
				appendTab(buf, tabCount + 1)
					.append("}\n");
				
				// 改行.
				buf.append("\n");
				
				// getter.
				appendTab(buf, tabCount + 1)
					.append("public ")
					.append(key)
					.append(" ")
					.append(convertJavaGetterSetter(false, val[1]))
					.append("() {\n");
				appendTab(buf, tabCount + 2)
					.append("return ")
					.append(convertJavaGetterReturn(key, val[1]))
					.append("\n");
				appendTab(buf, tabCount + 1)
					.append("}\n");
			}
			// 終端.
			appendTab(buf, tabCount)
				.append("}\n");
			
			// 生成完了.
			ret[count ++] = buf.toString();
		}
		return ret;
	}
	
	// [java]createSmpleBeanを生成するメソッドを定義.
	private static final String getJavaCreateSmpleMethod(
		int tabCount, Map<String, Object> json) {
		// beans一覧を取得.
		Object o = json.get("beans");
		if(o == null || !(o instanceof Map) ||
			((Map)o).size() == 0) {
			// 存在しない場合.
			return "";
		}
		String beanName;
		final Map<String, Object> beans = (Map<String, Object>)o;
		final Iterator<String> itr = beans.keySet().iterator();
		StringBuilder buf = new StringBuilder();
		appendTab(buf, tabCount)
			.append("public final SmpleBean createBean(String name) {\n");
		appendTab(buf, tabCount + 1)
			.append("name = name.trim().toLowerCase();\n");
		// beans毎の処理実行.
		while(itr.hasNext()) {
			// bean名.
			beanName = itr.next();
			appendTab(buf, tabCount + 1)
				.append("if(\"")
				.append(beanName.toLowerCase())
				.append("\".equals(name)) {\n");
			appendTab(buf, tabCount + 2)
				.append("return new ")
				.append(beanName)
				.append("();\n");
			appendTab(buf, tabCount + 1)
				.append("}\n");
		}
		appendTab(buf, tabCount + 1)
			.append("return null;\n");
		appendTab(buf, tabCount)
			.append("}\n");
		return buf.toString();
	}

	
	// [java]smpleクラス定義を取得.
	private static final String getJavaSmpleClass(
		int tabCount, String className) {
		StringBuilder buf = new StringBuilder();
		appendTab(buf, tabCount)
			.append("@SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n");
		return appendTab(buf, tabCount)
			.append("public final class ")
			.append(className)
			.append(" implements ")
			.append(JAVA_SMPLE_OBJECT)
			.append(" {\n")
			.toString();
	}
	
	// [java]各インデントの終端.
	private static final String endIndent(int tabCount) {
		return appendTab(new StringBuilder(), tabCount)
			.append("}\n")
			.toString();
	}
	
	// [java]smpleメソッド定義を取得.
	private static final String getJavaSmpleMethod() {
		return "public final String execute(Object... " +
			JAVA_SMPLE_METHOD_ARGS + ") {";
	}
	
	// [java]smpleOut定義を取得.
	private static final String getJavaSmpleOut() {
		return "final " + JAVA_SMPLE_OUT_OBJECT + " " + OUT_SIMBOLE +
			" = new " + JAVA_SMPLE_OUT_OBJECT + "();";
	}
	
	// [java]SmpleOutの結果を出力.
	private static final String resultJavaSmpleOut(int tabCount) {
		return appendTab(new StringBuilder(), tabCount)
			.append("return ")
			.append(OUT_SIMBOLE)
			.append(".")
			.append(SMPLE_$OUT_RESULT)
			.append("();\n")
			.toString();
	}
	
	// [java]テンプレートに渡された引数の変換定義を取得.
	private static final String getJavaConvertArgs(
		int tabCount, Map<String, Object> json) {
		// values一覧を取得.
		Object o = json.get("values");
		if(o == null || !(o instanceof List) ||
			((List)o).size() == 0) {
			// 存在しない場合.
			return "";
		}
		String key;
		String[] val;
		StringBuilder buf = new StringBuilder();
		final List<String> list = (List<String>)o;
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			// "String hoge" の定義を
			// new String[] {"String", "hoge"}に変換.
			val = javaVariable(list.get(i));
			// primitive型の場合はオブジェクト名に変換.
			key = convertJavaPrimitive(val[0]);
			// $smple.beans定義の場合.
			if(key.startsWith(JAVA_SMPLE_BEAN_KEY_DEFINE)) {
				// $smple.beans定義を切り取る.
				key = key.substring(JAVA_SMPLE_BEAN_KEY_DEFINE.length())
					.trim();
			}
			// 変換処理.
			appendTab(buf, tabCount)
				.append(key)
				.append(" ")
				.append(val[1])
				.append(" = (")
				.append(key)
				.append(")convertArgs(\"")
				.append(key)
				.append("\", ")
				.append(JAVA_SMPLE_METHOD_ARGS)
				.append("[")
				.append(i)
				.append("]);\n");
		}
		return buf.toString();
	}
	
	// [java]import定義を取得.
	private static final String getJavaImportPackages(
		Map<String, Object> json) {
		StringBuilder buf = new StringBuilder();
		int len = JAVA_DEFAULT_JAVA_IMPORTS.length;
		for(int i = 0; i < len; i ++) {
			buf.append("import ")
				.append(JAVA_DEFAULT_JAVA_IMPORTS[i])
				.append(";\n");
		}
		
		// packages一覧を取得.
		Object o = json.get("packages");
		if(o == null || !(o instanceof List) ||
			((List)o).size() == 0) {
			// 存在しない場合.
			return buf.toString();
		}
		final List<String> list = (List<String>)o;
		len = list.size();
		for(int i = 0; i < len; i ++) {
			buf.append("import ")
				.append(list.get(i))
				.append(";\n");
		}
		return buf.toString();
	}
	
	// Javaクラス名で利用可能な文字以外はアンダーバー変換
	protected static final String getJavaClassName(String name) {
		char c;
		StringBuilder buf = new StringBuilder();
		final int len = name.length();
		for(int i = 0; i < len; i ++) {
			c = name.charAt(i);
			if((c >= 'a' && c <= 'z') ||
				(c >= 'A' && c <= 'Z') ||
				(c >= '0' && c <= '9') ||
				c == '_' || c == '$') {
				buf.append(c);
			} else {
				buf.append('_');
			}
		}
		name = buf.toString();
		c = name.charAt(0);
		if(c == '_' || !(c >= '0' && c <= '9')) {
			throw new QuinaException(
				"Target class name: \"" +
				name + "\" is incorrect as the class name. ");
		}
		return name.substring(0, 1).toUpperCase() +
			name.substring(1);
	}
	
	// [java]Smpleオブジェクト名を作成.
	// json.name が定義されている場合は、そこから作成.
	// 定義されていない場合はsmpleResourcePathで作成.
	private static final String getSmpleJavaObjectName(
		String smpleResourcePath, Map<String, Object> json) {
		String name = null;
		// json.nameで取得.
		Object o = json.get("name");
		if(o == null || !(o instanceof String)) {
			name = (String)o;
			if(name == null || name.isEmpty()) {
				name = null;
			}
		}
		// json.nameが存在しない場合.
		if(name == null) {
			// smpleResourcePathで処置.
			name = smpleResourcePath;
		}
		// 存在しない場合.
		if(name == null || name.isEmpty()) {
			throw new QuinaException(
				"The object name to expand the sample is not defined.");
		}
		// Javaのクラス名で使えない文字は全て「アンダーバー」変換.
		// 半角英数字(a～z,A～Z,0～9)とアンダーバー(_)とドル記号($)以外.
		return getJavaClassName(name);
	}
	
	// [js]smple先頭定義.
	private static final String getJsHeaderScript() {
		return "(function() {\n";
	}
	
	// [js]smple終端定義.
	private static final String getJsFooterScript() {
		return "\n})();\n";
	}
	
	// [js]smpleOut定義.
	private static final String getJsSmpleOutScript() {
		return
		"var $out = (function() {\n" +
		"var out = '';var ret = {};\n" +
		"ret.clear = function() {out = '';}\n" +
		"ret.print = function(o) {out += o;}\n" +
		"ret.println = function(o) {out += o + '\\n';}\n" +
		"ret." + SMPLE_$OUT_RESULT + " = function() {return out;}\n" +
		"return ret;})();\n";
	}
	
	// [js]SmpleOutの結果を出力.
	private static final String resultJSSmpleOut(int tabCount) {
		return appendTab(new StringBuilder(), tabCount)
			.append("return ")
			.append(OUT_SIMBOLE)
			.append(".")
			.append(SMPLE_$OUT_RESULT)
			.append("();\n")
			.toString();
	}
	
	/**
	 * [js]js用smpleを実行して結果を取得.
	 * @param jsCmpSmple Js用のコンパイル済みSmpleを設定します.
	 * @param params 注入するパラメータを設定します.
	 *               Listを利用する場合は JsArrayを利用してください.
	 *               Mapを利用する場合は JsObjectを利用してください.
	 * @return String Smple実行結果が返却されます.
	 */
	public static final String executeJsSmple(
		String jsCmpSmple, Proxy params) {
		if(jsCmpSmple == null || jsCmpSmple.isEmpty()) {
			throw new QuinaException(
				"The contents of the sample script do not exist.");
		}
		Context ctx = null;
		try {
			// graalvmのjs実行コンテキストを生成.
			ctx = Context.newBuilder()
				// これをつけることで、putMemberのJavaオブジェクトに
				// アクセスできる.
				//.allowAllAccess(true)
				//
				// ただしJsSmple実行の場合はProxyパラメータのみアクセス可能
				// とするので allowAllAccess(true) としない.
				.allowAllAccess(false)
				.build();
			
			// パラメータセット.
			if(params != null) {
				Value value = ctx.getBindings("js");
				value.putMember(
					JS_SMPLE_PARAMS_NAME, params);
			}
			
			// コンパイル済みjsSmpleを実行.
			Value ret = ctx.eval("js", jsCmpSmple);
			if(ret == null) {
				throw new QuinaException(
					"sample The compilation process result is null.");
			}
			final String result = ret.toString();
			if(result == null) {
				throw new QuinaException(
					"sample The compilation process result is null.");
			}
			return result;
		} finally {
			if(ctx != null) {
				try {
					ctx.close();
				} catch(Exception e) {}
			}
		}
	}
	
	// test.
	public static final void main(String[] args) throws Exception {
		/*
		// 基本テスト.
		String file = "z:/home/maachang/project/quina/TestSmple.sml";
		String script = FileUtil.getFileString(file);
		
		String[] out = new String[1];
		String javaObject = compileJava(out, "hoge", script);
		System.out.println("#package + className: " + out[0]);
		System.out.println("#javaObject: \n" + javaObject);
		
		System.out.println()
		
		String jsOut = compileJs(script);
		System.out.println("#jsOut: \n" + jsOut);
		*/
		
		// [jsテスト].
		//String file = "z:/home/maachang/project/quina/LoadCdiReflect.java.smj";
		
		// 読み込み.
		//final String script = FileUtil.getFileString(file);
		
		//String resource = "quina/resources/compile/LoadRouter.java.smj";
		String resource = "quina/resources/compile/LoadCdiService.java.smj";
		final String script = ResourceUtil.getString(resource);
		
		
		// 展開.
		String jsSmple = compileJs(script);
		System.out.println(jsSmple);
		/*
		
		// パラメータ.
		Proxy params = JsArray.of(
			JsObject.of(
				"name", "Hoge",
				"fields", JsArray.of(
					"abc",
					"def"
				)
			)
			,
			JsObject.of(
				"name", "Xyz",
				"fields", JsArray.of(
					"ryo",
					"kaori"
				)
			)
		);
		
		// JS実行.
		String result = executeJsSmple(jsSmple, params);
		System.out.println(result);
		*/
	}
}
