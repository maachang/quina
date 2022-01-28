package quina.compile.tool.graalvm.nativeimage;

/**
 * NativeImage用のConfig定義オブジェクト.
 *
 */
public interface NativeImageConfig {
	/**
	 * 管理情報をクリア.
	 */
	public void clear();
	
	/**
	 * 出力先コンフィグファイル名を取得.
	 * @return String 出力先コンフィグファイル名が返却されます.
	 */
	public String getConfigName();
	
	/**
	 * 定義内容を文字列で出力.
	 * @return String 文字列が返却されます.
	 */
	public String outString();

}
