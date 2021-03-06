package quina.component;

/**
 * コンポーネント定義.
 */
public class ComponentConstants {
	private ComponentConstants() {}

	/**
	 * 同期タイプ.
	 */
	public static final int TYPE_SYNC = 0x0100;

	/**
	 * RESTfulタイプ.
	 */
	public static final int TYPE_RESTFUL = 0x0200;

	/**
	 * ノーマルコンポーネント.
	 */
	public static final int TYPE_NORMAL = 0x0000;

	/**
	 * ファイルコンポーネント.
	 */
	public static final int TYPE_FILE = 0x00ff;

	/**
	 * エラーコンポーネントタイプ
	 */
	public static final int TYPE_ERROR = 0x8000;
}
