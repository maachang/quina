package quina;

import quina.component.Component;
import quina.component.ComponentManager;
import quina.component.ErrorComponent;
import quina.component.EtagManager;
import quina.component.EtagManagerInfo;
import quina.component.RegisterComponent;
import quina.validate.Validation;

/**
 * URLアクセスに対するコンポーネントを管理.
 */
public class Router {
	// 基本マッピングパス.
	private String path = "/";

	// コンポーネントマネージャ.
	private final ComponentManager manager = new ComponentManager();

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
	 * 指定URLでコンポーネントが見つからなかった場合に
	 * 実行されるコンポーネントを設定します.
	 * @param component 実行コンポーネントを設定します.
	 * @return Router このオブジェクトが返却されます.
	 */
	public Router any(Component component) {
		return any(null, component);
	}

	/**
	 * 指定URLでコンポーネントが見つからなかった場合に
	 * 実行されるコンポーネントを設定します.
	 * @param validation Validationを設定します.
	 * @param component 実行コンポーネントを設定します.
	 * @return Router このオブジェクトが返却されます.
	 */
	public Router any(Validation validation, Component component) {
		manager.put(validation, component);
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
	 * @param path コンポーネント実行するURLのパスを設定します.
	 * @param validation 対象のValidationを設定します.
	 * @param component 実行コンポーネントを設定します.
	 * @return Router このオブジェクトが返却されます.
	 */
	public Router route(String path, Component component) {
		return route(path, null, component);
	}

	/**
	 * 対象コンポーネントとルートを紐付けます.
	 * @param path コンポーネント実行するURLのパスを設定します.
	 * @param validation 対象のValidationを設定します.
	 * @param component 実行コンポーネントを設定します.
	 * @return Router このオブジェクトが返却されます.
	 */
	public Router route(String path, Validation validation, Component component) {
		if(path.startsWith("/")) {
			path = this.path + path.substring(1);
		} else {
			path = this.path + path;
		}
		manager.put(path, validation, component);
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

	/**
	 * Etag管理オブジェクトを取得.
	 * @return EtagManager Etag管理オブジェクトが返却されます.
	 */
	public EtagManager getEtagManager() {
		return manager.getEtagManagerInfo().getEtagManager();
	}

	/**
	 * Etag管理定義情報を取得.
	 * @return EtagManagerInfo Etag管理定義情報が返却されます.
	 */
	public EtagManagerInfo getEtagManagerInfo() {
		return manager.getEtagManagerInfo();
	}

	@Override
	public String toString() {
		return manager.toString();
	}
}
