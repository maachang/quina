package quina.compile.tool.graalvm.nativeimage;

import quina.exception.QuinaException;
import quina.util.StringUtil;
import quina.util.collection.ObjectList;

/**
 * GraalVMでのNative-Imageの引数
 * -H:DynamicProxyConfigurationFilesの
 * 設定先ファイル内容を作成します.
 */
public class ProxyItem implements NativeImageConfig {
	private ProxyItem() {}
	
	// シングルトン.
	private static final ProxyItem SNGL = new ProxyItem();
	
	// Proxyリスト.
	private ObjectList<String[]> list =
		new ObjectList<String[]>();
	
	/**
	 * オブジェクトを取得.
	 * @return ProxyItem ProxyItemが返却されます.
	 */
	public static final ProxyItem get() {
		return SNGL;
	}
	
	/**
	 * 管理情報をクリア.
	 */
	@Override
	public void clear() {
		list.clear();
	}
	
	/**
	 * ProxyItem を追加.
	 * @param names 対象クラス名群を設定します.
	 * @return ProxyItem このオブジェクトが返却されます.
	 */
	public ProxyItem addItem(String... names) {
		if(names == null || names.length <= 0) {
			throw new QuinaException("Proxy class name is not set.");
		}
		final int len = names.length;
		for(int i = 0; i < len; i ++) {
			if(names[i] == null || (names[i] = names[i].trim()).isEmpty()) {
				throw new QuinaException(
					"Proxy class name is not set: " + arrayString(null, names));
			}
		}
		list.add(names);
		return this;
	}
	
	/**
	 * 出力先コンフィグファイル名を取得.
	 * @return String 出力先コンフィグファイル名が返却されます.
	 */
	@Override
	public String getConfigName() {
		return "proxy.json";
	}
	
	/**
	 * 定義内容を文字列で出力.
	 * @return String 文字列が返却されます.
	 */
	@Override
	public String outString() {
		StringBuilder buf = new StringBuilder();
		StringUtil.println(buf, 0, "[");
		int len = list.size();
		for(int i = 0; i < len; i ++) {
			StringUtil.print(buf, 1);
			if(i != 0) {
				buf.append(",");
			}
			arrayString(buf, list.get(i));
			buf.append("\n");
		}
		StringUtil.println(buf, 0, "]");
		return buf.toString();
	}
	
	// 配列文字列を出力.
	private static final String arrayString(
		StringBuilder buf, String[] list) {
		if(buf == null) {
			buf = new StringBuilder();
		}
		buf.append("[");
		final int len = list.length;
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				buf.append(", ");
			}
			buf.append("\"").append(list[i]).append("\"");
		}
		return buf.append("]").toString();
	}
}
