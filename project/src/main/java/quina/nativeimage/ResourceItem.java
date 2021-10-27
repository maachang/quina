package quina.nativeimage;

import quina.util.StringUtil;
import quina.util.collection.ObjectList;

/**
 * GraalVMでのNative-Imageの引数
 * -H:ResourceConfigurationFilesの
 * 設定先ファイル内容を作成します.
 */
public class ResourceItem implements NativeImageConfig {
	private ResourceItem() {}
	
	// シングルトン.
	private static final ResourceItem SNGL = new ResourceItem();
	
	// java.util.ResourceBundle対するL10NのResourse定義名を設定.
	private final ObjectList<String> bundleList =
		new ObjectList<String>();
	
	// Class.getResource()やClass.getResourceAsStream()に対する
	// Resourse定義追加条件を正規表現で設定.
	private final ObjectList<String> includeRegExpList =
		new ObjectList<String>();
	
	// Class.getResource()やClass.getResourceAsStream()に対する
	// Resourse定義除外条件を正規表現で設定.
	private final ObjectList<String> excludeRegExpList =
		new ObjectList<String>();
	
	/**
	 * オブジェクトを取得.
	 * @return ResourceItem ResourceItemが返却されます.
	 */
	public static final ResourceItem get() {
		return SNGL;
	}
	
	/**
	 * 管理情報をクリア.
	 */
	@Override
	public void clear() {
		bundleList.clear();
		includeRegExpList.clear();
		excludeRegExpList.clear();
	}
	
	/**
	 * java.util.ResourceBundleで読むこむL10Nリソースを設定します.
	 * @param name L10Nリソースのパッケージ名＋ファイル名を設定します.
	 * @return ResourceItem このオブジェクトが返却されます.
	 */
	public ResourceItem addBundleItem(String name) {
		bundleList.add(name);
		return this;
	}
	
	/**
	 * Class.getResource()やClass.getResourceAsStream()に対する
	 * Resourse定義追加条件を正規表現で設定します.
	 * @param regexp 正規表現を設定します.
	 *               ＜例＞ ".*／Resource.*txt$"
	 * @return ResourceItem このオブジェクトが返却されます.
	 */
	public ResourceItem addIncludeItem(String regexp) {
		includeRegExpList.add(regexp);
		return this;
	}
	
	/**
	 * Class.getResource()やClass.getResourceAsStream()に対する
	 * Resourse定義除外条件を正規表現で設定します.
	 * @param regexp 正規表現を設定します.
	 *               ＜例＞ ".*／Resource.*txt$"
	 * @return ResourceItem このオブジェクトが返却されます.
	 */
	public ResourceItem addExcludeItem(String regexp) {
		excludeRegExpList.add(regexp);
		return this;
	}
	
	/**
	 * 出力先コンフィグファイル名を取得.
	 * @return String 出力先コンフィグファイル名が返却されます.
	 */
	@Override
	public String getConfigName() {
		return "resource.json";
	}
	
	/**
	 * 定義内容を文字列で出力.
	 * @return String 文字列が返却されます.
	 */
	@Override
	public String outString() {
		StringBuilder buf = new StringBuilder();
		StringUtil.println(buf, 0, "{");
		
		// bounds定義を出力.
		boolean commaFlag = outBundle(buf, 1);
		
		// Resource定義を出力.
		outResource(buf, 1, commaFlag);
		
		StringUtil.println(buf, 0, "}");
		return buf.toString();
	}
	
	// boundsListを出力.
	private boolean outBundle(StringBuilder buf, int tab) {
		if(bundleList.size() > 0) {
			StringUtil.println(buf, tab, "\"bundles\": [");
			
			String comma = "";
			final int len = bundleList.size();
			for(int i = 0; i < len; i ++) {
				StringUtil.println(buf, tab + 1,
					comma +
					"{\"name\": \"" + bundleList.get(i) + "\"}"
				);
				comma = ",";
			}
			
			StringUtil.println(buf, tab, "]");
			return true;
		}
		return false;
	}
	
	private void outResource(StringBuilder buf, int tab, boolean commaFlag) {
		if(includeRegExpList.size() > 0 && excludeRegExpList.size() > 0) {
			StringUtil.println(buf, tab,
				(commaFlag ? "," : "") + "\"resources\": {");
			commaFlag = false;
			
			// include用のResource出力.
			if(includeRegExpList.size() > 0) {
				StringUtil.println(buf, tab + 1, "\"includes\": [");
				
				String comma = "";
				final int len = includeRegExpList.size();
				for(int i = 0; i < len; i ++) {
					StringUtil.println(buf, tab + 2,
						comma +
						"{\"pattern\": \"" + includeRegExpList.get(i) + "\"}"
					);
					comma = ",";
				}
				
				StringUtil.println(buf, tab + 1, "]");
				commaFlag = true;
			}
			
			// exclude用のResource出力.
			if(excludeRegExpList.size() > 0) {
				StringUtil.println(buf, tab + 1,
					(commaFlag ? "," : "") + "\"excludes\": [");
				
				String comma = "";
				final int len = excludeRegExpList.size();
				for(int i = 0; i < len; i ++) {
					StringUtil.println(buf, tab + 2,
						comma +
						"{\"pattern\": \"" + excludeRegExpList.get(i) + "\"}"
					);
					comma = ",";
				}
				
				StringUtil.println(buf, tab + 1, "]");
				commaFlag = true;
			}
			
			// 終端.
			StringUtil.println(buf, tab, "}");
		}
	}

}
