package quina.component;

/**
 * コンポーネント定義.
 */
public class ComponentConstants {
	private ComponentConstants() {}

	/**
	 * コンポーネント属性: 同期タイプ.
	 */
	public static final int ATTRIBUTE_SYNC = 0x0100;

	/**
	 * コンポーネント属性: RESTfulタイプ.
	 */
	public static final int ATTRIBUTE_RESTFUL = 0x0200;

	/**
	 * コンポーネント属性: ノーマルコンポーネント.
	 */
	public static final int ATTRIBUTE_NORMAL = 0x0000;

	/**
	 * コンポーネント属性: ファイルコンポーネント.
	 */
	public static final int ATTRIBUTE_FILE = 0x00ff;

	/**
	 * コンポーネント属性: エラーコンポーネントタイプ
	 */
	public static final int ATTRIBUTE_ERROR = 0x8000;
}
