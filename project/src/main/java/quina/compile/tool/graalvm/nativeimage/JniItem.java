package quina.compile.tool.graalvm.nativeimage;

import quina.exception.QuinaException;
import quina.util.StringUtil;
import quina.util.collection.ObjectList;

/**
 * GraalVMでのNative-Imageの引数
 * -H:JNIConfigurationFilesの
 * 設定先ファイル内容を作成します.
 */
public class JniItem implements NativeImageConfig {
	private JniItem() {}
	
	// シングルトン.
	private static final JniItem SNGL = new JniItem();
	
	// 詳細定義リスト.
	private final ObjectList<DetailNativeItem> detailList =
		new ObjectList<DetailNativeItem>();
	
	/**
	 * オブジェクトを取得.
	 * @return JniItem JniItemが返却されます.
	 */
	public static final JniItem get() {
		return SNGL;
	}
	
	/**
	 * 管理情報をクリア.
	 */
	@Override
	public void clear() {
		detailList.clear();
	}
	
	/**
	 * JniItem を追加.
	 * @param name 対象クラス名を設定します.
	 * @return JniItem このオブジェクトが返却されます.
	 */
	public JniItem addItem(String name) {
		if(name == null || (name = name.trim()).isEmpty()) {
			throw new QuinaException("The class name is not set.");
		}
		detailList.add(DetailNativeItem.create(name));
		return this;
	}
	
	/**
	 * 詳細定義のJniItem を追加.
	 * @param item 詳細定義を設定します.
	 * @return JniItem このオブジェクトが返却されます.
	 */
	public JniItem addItem(DetailNativeItem item) {
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
		return "jni.json";
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
		int len = detailList.size();
		for(int i = 0; i < len; i ++) {
			detailList.get(i).outString(buf, 1, commaFlag);
			commaFlag = true;
		}
		StringUtil.println(buf, 0, "]");
		return buf.toString();
	}

}
