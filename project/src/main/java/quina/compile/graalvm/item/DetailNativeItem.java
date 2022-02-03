package quina.compile.graalvm.item;

import quina.exception.QuinaException;
import quina.util.StringUtil;
import quina.util.collection.ObjectList;

/**
 * DetailItem.
 * 
 * Reflection定義やJni定義に対して個別のクラスに
 * 対して、個別のフィールドやメソッドに対して
 * 定義を行います.
 */
public class DetailNativeItem {
	private DetailNativeItem() {}
	// フィールド定義.
	private static final class DetailField {
		// フィールド名.
		private String name;
		// allowWriteモード.
		private Boolean allowWrite;
		
		/**
		 * コンストラクタ.
		 * @param name
		 * @param allowWrite
		 */
		public DetailField(String name, Boolean allowWrite) {
			if(name == null || (name = name.trim()).isEmpty()) {
				throw new QuinaException("The field name is not set.");
			}
			this.name = name;
			if(allowWrite != null && allowWrite) {
				this.allowWrite = allowWrite;
			}
		}
		
		// フィールド定義を出力.
		public void outString(StringBuilder buf, int tab, String comma) {
			if(allowWrite != null && allowWrite) {
				StringUtil.println(buf, tab, comma +
					"{ \"name\" : \"" + name + "\", \"allowWrite\" : true }");
			} else {
				StringUtil.println(buf, tab, comma +
					"{ \"name\" : \"" + name + "\"}");
			}
		}
	}
	// メソッド定義.
	private static final class DetailMethod {
		// メソッド名.
		private String name;
		// パラメータ.
		private Class<?>[] params;
		
		/**
		 * コンストラクタ.
		 * @param className
		 * @param name
		 * @param params
		 */
		public DetailMethod(String className, String name, Class<?>... params) {
			if(name == null || (name = name.trim()).isEmpty()) {
				throw new QuinaException("The method name is not set.");
			} else if(params != null && params.length > 0) {
				final int len = params.length;
				for(int i = 0; i < len; i ++) {
					if(params[i] == null) {
						throw new QuinaException(
							"There is no parameter for the specified method \"" +
								name + "\" of the specified class \"" +
								className + "\". ");
					}
				}
			}
			this.name = name;
			this.params = params;
		}
		
		// フィールド定義を出力.
		public void outString(StringBuilder buf, int tab, String comma) {
			if(params != null) {
				StringUtil.print(buf, tab, comma +
					"{ \"name\" : \"" + name + "\", \"parameterTypes\" : [");
				final int len = params.length;
				for(int i = 0; i < len; i ++) {
					if(i != 0) {
						buf.append(", ");
					}
					buf.append(StringUtil.getClassName(params[i]));
				}
				StringUtil.println(buf, tab, "]");
			} else {
				StringUtil.println(buf, tab, comma +
					"{ \"name\" : \"" + name + "\"}");
			}
		}
	}
	
	// クラス名.
	private String name;
	// field定義群.
	private ObjectList<DetailField> fieldList;
	// method定義群.
	private ObjectList<DetailMethod> methodList;
	
	/**
	 * 新しいDetailNativeItemを取得.
	 * @param name クラス名を設定します.
	 * @return DetailItem DetailItemが返却されます.
	 */
	public static final DetailNativeItem create(String name) {
		if(name == null || (name = name.trim()).isEmpty()) {
			throw new QuinaException("The class name is not set.");
		}
		DetailNativeItem ret = new DetailNativeItem();
		ret.name = name;
		return ret;
	}
	
	/**
	 * フィールド情報を追加.
	 * @param name 対象のフィールド名を設定します.
	 * @param allowWrite final定義のフィールドに
	 *                   書き込みを許可する場合はtrue.
	 * @return DetailItem このオブジェクトが返却されます.
	 */
	public DetailNativeItem addField(String name) {
		return addField(name, false);
	}
	
	/**
	 * フィールド情報を追加.
	 * @param name 対象のフィールド名を設定します.
	 * @param allowWrite final定義のフィールドに
	 *                   書き込みを許可する場合はtrue.
	 * @return DetailItem このオブジェクトが返却されます.
	 */
	public DetailNativeItem addField(String name, boolean allowWrite) {
		DetailField f = new DetailField(name, allowWrite);
		if(fieldList == null) {
			fieldList = new ObjectList<DetailField>();
		}
		fieldList.add(f);
		return this;
	}
	
	/**
	 * コンストラクタ情報を追加.
	 * @param name 対象のメソッド名を設定します.
	 * @param params メソッドのパラメータを設定します.
	 * @return DetailItem このオブジェクトが返却されます.
	 */
	public DetailNativeItem addConstructor(Class<?>... params) {
		DetailMethod m = new DetailMethod(this.name, "<init>", params);
		if(methodList == null) {
			methodList = new ObjectList<DetailMethod>();
		}
		methodList.add(m);
		return this;
	}
	
	/**
	 * メソッド情報を追加.
	 * @param name 対象のメソッド名を設定します.
	 * @param params メソッドのパラメータを設定します.
	 * @return DetailItem このオブジェクトが返却されます.
	 */
	public DetailNativeItem addMethod(String name, Class<?>... params) {
		DetailMethod m = new DetailMethod(this.name, name, params);
		if(methodList == null) {
			methodList = new ObjectList<DetailMethod>();
		}
		methodList.add(m);
		return this;
	}
	
	/**
	 * 文字列として出力.
	 * @param buf 出力先のStringBuilderを設定します.
	 * @param tab タブ数を設定します.
	 * @param commaFlag カンマフラグを設定します.
	 */
	protected void outString(StringBuilder buf, int tab, boolean commaFlag) {
		// fieldListかmethodList情報が存在する場合.
		if(fieldList != null || methodList != null) {
			StringUtil.println(buf, tab,
				(commaFlag ? "," : "") + "{");
			
			// クラス名を出力.
			StringUtil.println(buf, tab + 1,
				"\"name\" : \"" + name + "\"");
			
			// フィールド情報群を出力.
			outFields(buf, tab + 1);
			
			// メソッド情報群を出力.
			outMethods(buf, tab + 1);
			
			StringUtil.println(buf, tab, "}");
		// クラス名のみの設定.
		} else {
			StringUtil.println(buf, tab,
				"{\"name\" : \"" + name + "\"}");
		}
	}

	// フィールド情報を出力.
	private final void outFields(StringBuilder buf, int tab) {
		if(fieldList != null) {
			StringUtil.println(buf, tab, ",\"fields\" : [");
			
			String comma = "";
			final int len = fieldList.size();
			for(int i = 0; i < len; i ++) {
				fieldList.get(i).outString(buf, tab + 1, comma);
				comma = ",";
			}
			
			StringUtil.println(buf, tab, "]");
		}
	}
	
	// メソッド情報を出力.
	private final void outMethods(StringBuilder buf, int tab) {
		if(fieldList != null) {
			StringUtil.println(buf, tab, ",\"methods\" : [");
			
			String comma = "";
			final int len = methodList.size();
			for(int i = 0; i < len; i ++) {
				methodList.get(i).outString(buf, tab + 1, comma);
				comma = ",";
			}
			
			StringUtil.println(buf, tab, "]");
		}
	}
}
