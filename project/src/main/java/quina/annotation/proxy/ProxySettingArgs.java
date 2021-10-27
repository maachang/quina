package quina.annotation.proxy;

/**
 * ProxySetting用のパラメータオブジェクト.
 * 
 * Reflectionを利用する場合 Methodの引数が
 * (Object... args)なので、ここに 配列引数を
 * 定義した場合、IllegalArgumentExceptionの例外で
 * ”wrong number of arguments” が出るので、
 * 可変の変数に対しては、このオブジェクトで対応.
 */
public class ProxySettingArgs {
	// 可変パラメータ.
	private Object[] args;
	
	/**
	 * コンストラクタ.
	 * @param args 可変引数を設定します.
	 */
	public ProxySettingArgs(Object... args) {
		this.args = args;
	}
	
	/**
	 * 可変引数のサイズを取得.
	 * @return int 可変引数のサイズが返却されます.
	 */
	public int size() {
		return args.length;
	}
	
	/**
	 * 項番を指定して可変引数を取得.
	 * @param no 対象の項番を設定します.
	 * @return Object 指定項番の可変引数が返却されます.
	 */
	public Object getArgs(int no) {
		return args[no];
	}
}
