package quina.compile.graalvm.item;

import quina.exception.QuinaException;
import quina.util.collection.ObjectList;

/**
 * GraalVMのNativeImageに対して
 * ビルド時に初期化される
 * –initialize-at-build-time
 * 設定に必要なクラスを定義した
 * ファイルを作成します.
 */
public class InitializeBuildItem implements NativeImageConfig {
	private InitializeBuildItem() {}
	
	// シングルトン.
	private static final InitializeBuildItem SNGL =
		new InitializeBuildItem();
	
	// 定義リスト.
	private final ObjectList<String> list = new ObjectList<String>();
	
	/**
	 * オブジェクトを取得.
	 * @return InitializeBuildTimeItem
	 *		InitializeBuildTimeItemが返却されます.
	 */
	public static final InitializeBuildItem get() {
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
	 * InitializeBuildTimeItem を追加.
	 * @param name 対象クラス名を設定します.
	 * @return InitializeBuildTimeItem このオブジェクトが返却されます.
	 */
	public InitializeBuildItem addItem(String name) {
		if(name == null || (name = name.trim()).isEmpty()) {
			throw new QuinaException("The class name is not set.");
		}
		list.add(name);
		return this;
	}
	
	/**
	 * 出力先コンフィグファイル名を取得.
	 * @return String 出力先コンフィグファイル名が返却されます.
	 */
	@Override
	public String getConfigName() {
		return "initializeAtBuildTime.txt";
	}
	
	/**
	 * 定義内容を文字列で出力.
	 * @return String 文字列が返却されます.
	 */
	@Override
	public String outString() {
		StringBuilder buf = new StringBuilder();
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				buf.append(",");
			}
			buf.append(list.get(i));
		}
		return buf.toString();
	}
}
