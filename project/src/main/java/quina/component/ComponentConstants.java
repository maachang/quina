package quina.component;

/**
 * コンポーネント定義.
 */
public class ComponentConstants {
	private ComponentConstants() {}

	/**
	 * コンポーネント定義: 同期タイプ.
	 */
	public static final int TYPE_SYNC = 0x0100;

	/**
	 * コンポーネント定義: RESTfulタイプ.
	 */
	public static final int TYPE_RESTFUL = 0x0200;

	/**
	 * コンポーネント定義: ノーマルコンポーネント.
	 */
	public static final int TYPE_NORMAL = 0x0000;

	/**
	 * コンポーネント定義: ファイルコンポーネント.
	 */
	public static final int TYPE_FILE = 0x00ff;

	/**
	 * コンポーネント定義: エラーコンポーネントタイプ
	 */
	public static final int TYPE_ERROR = 0x8000;
}
