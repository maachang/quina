package quina.compile.tool.graalvm.nativeimage;

import quina.exception.QuinaException;
import quina.util.StringUtil;
import quina.util.collection.ObjectList;

/**
 * GraalVMでのNative-Imageの引数
 * -H:ReflectionConfigurationFilesの
 * 設定先ファイル内容を作成します.
 */
public class ReflectionItem implements NativeImageConfig {
	private ReflectionItem() {}
	
	// １つの定義要素.
	private static final class Element {
		// 対象クラス名.
		private String className;
		// コンストラクタ一覧を取得許可.
		private boolean allDeclaredConstructors;
		// メソッド一覧を取得許可.
		private boolean allDeclaredMethods;
		// フィールド一覧を取得許可.
		private boolean allDeclaredFields;
		// クラス一覧を取得許可.
		private boolean allDeclaredClasses;
		
		/**
		 * コンストラクタ.
		 * @param name 対象クラス名を設定します.
		 * @param constructors 対象コンストラクタ一覧の許可を設定します.
		 * @param methods 対象メソッド一覧の許可を設定します.
		 * @param fields 対象フィールド一覧の許可を設定します.
		 * @param classes 対象クラス一覧の許可を設定します.
		 */
		public Element(String name, boolean constructors, boolean methods,
			boolean fields, boolean classes) {
			this.className = name;
			this.allDeclaredConstructors = constructors;
			this.allDeclaredMethods = methods;
			this.allDeclaredFields = fields;
			this.allDeclaredClasses = classes;
		}
		
		/**
		 * 文字列として出力.
		 * @param buf 出力先のStringBuilderを設定します.
		 * @param tab タブ数を設定します.
		 * @param commaFlag カンマフラグを設定します.
		 */
		public void outString(StringBuilder buf, int tab, boolean commaFlag) {
			if(allDeclaredConstructors || allDeclaredMethods ||
				allDeclaredFields || allDeclaredClasses) {
				StringUtil.println(buf, tab,
					(commaFlag ? "," : "") + "{");
				StringUtil.println(buf, tab + 1, "\"name\": \"" + className + "\"");
				if(allDeclaredConstructors) {
					StringUtil.println(buf, tab + 1, ",\"allDeclaredConstructors\": true");
				}
				if(allDeclaredMethods) {
					StringUtil.println(buf, tab + 1, ",\"allDeclaredMethods\": true");
				}
				if(allDeclaredFields) {
					StringUtil.println(buf, tab + 1, ",\"allDeclaredFields\": true");
				}
				if(allDeclaredClasses) {
					StringUtil.println(buf, tab + 1, ",\"allDeclaredClasses\": true");
				}
				StringUtil.println(buf, tab, "}");
			} else {
				StringUtil.println(buf, tab,
					(commaFlag ? "," : "") + "{\"name\": \"" + className + "\"}");
			}
		}
	}
	
	// シングルトン.
	private static final ReflectionItem SNGL = new ReflectionItem();
	
	// 管理リスト.
	private final ObjectList<Element> list = new ObjectList<Element>();
	
	// 詳細定義リスト.
	private final ObjectList<DetailNativeItem> detailList =
		new ObjectList<DetailNativeItem>();
	
	/**
	 * オブジェクトを取得.
	 * @return ReflectionItem ReflectionItemが返却されます.
	 */
	public static final ReflectionItem get() {
		return SNGL;
	}
	
	/**
	 * 管理情報をクリア.
	 */
	@Override
	public void clear() {
		list.clear();
		detailList.clear();
	}
	
	/**
	 * ReflectionBuildItem を追加.
	 * @param name 対象クラス名を設定します.
	 * @return ReflectionItem このオブジェクトが返却されます.
	 */
	public ReflectionItem addItem(String name) {
		return addItem(name, false, false, false, false);

	}
	
	/**
	 * ReflectionBuildItem を追加.
	 * @param name 対象クラス名を設定します.
	 * @param constructors 対象コンストラクタ一覧の許可を設定します.
	 * @return ReflectionItem このオブジェクトが返却されます.
	 */
	public ReflectionItem addItem(String name, boolean constructors) {
		return addItem(name, constructors, false, false, false);
	}
	
	/**
	 * ReflectionBuildItem を追加.
	 * @param name 対象クラス名を設定します.
	 * @param constructors 対象コンストラクタ一覧の許可を設定します.
	 * @param methods 対象メソッド一覧の許可を設定します.
	 * @return ReflectionItem このオブジェクトが返却されます.
	 */
	public ReflectionItem addItem(String name, boolean constructors,
		boolean methods) {
		return addItem(name, constructors, methods, false, false);
	}
	
	/**
	 * ReflectionBuildItem を追加.
	 * @param name 対象クラス名を設定します.
	 * @param constructors 対象コンストラクタ一覧の許可を設定します.
	 * @param methods 対象メソッド一覧の許可を設定します.
	 * @param fields 対象フィールド一覧の許可を設定します.
	 * @return ReflectionItem このオブジェクトが返却されます.
	 */
	public ReflectionItem addItem(String name, boolean constructors,
		boolean methods, boolean fields) {
		return addItem(name, constructors, methods, fields, false);
	}
	
	/**
	 * ReflectionBuildItem を追加.
	 * @param name 対象クラス名を設定します.
	 * @param constructors 対象コンストラクタ一覧の許可を設定します.
	 * @param methods 対象メソッド一覧の許可を設定します.
	 * @param fields 対象フィールド一覧の許可を設定します.
	 * @param classes 対象のクラス一覧の許可を設定します.
	 * @return ReflectionItem このオブジェクトが返却されます.
	 */
	public ReflectionItem addItem(String name, boolean constructors,
		boolean methods, boolean fields, boolean classes) {
		if(name == null || (name = name.trim()).isEmpty()) {
			throw new QuinaException("The class name is not set.");
		}
		list.add(new Element(name, constructors, methods, fields, classes));
		return this;
	}
	
	/**
	 * 詳細定義のReflectionBuildItem を追加.
	 * @param item 詳細定義を設定します.
	 * @return ReflectionItem このオブジェクトが返却されます.
	 */
	public ReflectionItem addItem(DetailNativeItem item) {
		if(item == null) {
			throw new QuinaException("The detail native item is not set.");
		}
		detailList.add(item);
		return this;
	}
	
	/**
	 * 出力先コンフィグファイル名を取得.
	 * @return String 出力先コンフィグファイル名が返却されます.
	 */
	@Override
	public String getConfigName() {
		return "reflection.json";
	}
	
	/**
	 * 定義内容を文字列で出力.
	 * @return String 文字列が返却されます.
	 */
	@Override
	public String outString() {
		StringBuilder buf = new StringBuilder();
		StringUtil.println(buf, 0, "[");
		boolean commaFlag = false;
		int len = list.size();
		for(int i = 0; i < len; i ++) {
			list.get(i).outString(buf, 1, commaFlag);
			commaFlag = true;
		}
		len = detailList.size();
		for(int i = 0; i < len; i ++) {
			detailList.get(i).outString(buf, 1, commaFlag);
			commaFlag = true;
		}
		StringUtil.println(buf, 0, "]");
		return buf.toString();
	}
}
