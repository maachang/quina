package quina;

import quina.component.Component;
import quina.component.ComponentManager;
import quina.component.ErrorComponent;
import quina.component.RegisterComponent;

/**
 * URLアクセスに対するコンポーネントを管理.
 */
public class Router {
	// 基本マッピングパス.
	private String path = "/";

	// コンポーネントマネージャ.
	private ComponentManager manager = new ComponentManager();

	/**
	 * パスのマッピング.
	 * @param path 基本パスを設定します.
	 *             先頭[/]の場合はフルパスで再定義します.
	 * @return Router このオブジェクトが返却されます.
	 */
	public Router mapping(String path) {
		if(!path.startsWith("/")) {
			path = "/" + path;
		}
		if(!path.endsWith("/")) {
			path = path + "/";
		}
		this.path = path;
		return this;
	}

	/**
	 * URLが見つからない場合に実行するコンポーネントを設定します.
	 * @param component 実行コンポーネントを設定します.
	 * @return Router このオブジェクトが返却されます.
	 */
	public Router any(Component component) {
		manager.put(component);
		return this;
	}

	/**
	 * エラーが発生した時の実行コンポーネントを設定します.
	 * @param component 実行コンポーネントを設定します.
	 * @return Router このオブジェクトが返却されます.
	 */
	public Router error(ErrorComponent component) {
		manager.putError(component);
		return this;
	}

	/**
	 * 対象コンポーネントとルートを紐付けます.
	 *
	 * @param path コンポーネント実行するURLのパスを設定します.
	 * @param component 実行コンポーネントを設定します.
	 * @return Router このオブジェクトが返却されます.
	 */
	public Router route(String path, Component component) {
		if(path.startsWith("/")) {
			path = this.path + path.substring(1);
		} else {
			path = this.path + path;
		}
		manager.put(path, component);
		return this;
	}

	/**
	 * 指定URLに対するコンポーネントを取得.
	 * @param url urlを設定します.
	 * @return RegisterComponent 登録コンポーネントが返却されます.
	 */
	public RegisterComponent get(String url) {
		return manager.get(url);
	}

	/**
	 * 指定URLに対するコンポーネントを取得.
	 * @param url urlを設定します.
	 * @param urls [/]で配列化されたURLを設定します.
	 * @return RegisterComponent 登録コンポーネントが返却されます.
	 */
	public RegisterComponent get(String url, String[] urls) {
		return manager.get(url, urls);
	}

	/**
	 * エラー発生時に呼び出されるコンポーネントを取得.
	 * @return ErrorComponent エラーコンポーネントが返却されます.
	 */
	public ErrorComponent getError() {
		return manager.getError();
	}
}
