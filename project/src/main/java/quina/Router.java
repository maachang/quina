package quina;

import java.lang.reflect.InvocationTargetException;

import quina.annotation.component.LoadAnnotationComponent;
import quina.annotation.component.ResponseInitialSetting;
import quina.annotation.route.LoadAnnotationRoute;
import quina.annotation.validate.LoadAnnotationValidate;
import quina.component.Component;
import quina.component.ComponentManager;
import quina.component.ErrorAttributeComponent;
import quina.component.EtagManager;
import quina.component.EtagManagerInfo;
import quina.component.RegisterComponent;
import quina.exception.QuinaException;
import quina.http.Method;
import quina.validate.Validation;

/**
 * URLアクセスに対するコンポーネントを管理.
 */
public class Router {
	/**
	 * Routeアノテーション自動読み込み実行用クラス名.
	 */
	public static final String AUTO_READ_ROUTE_CLASS = "LoadRouter";

	/**
	 * Routeアノテーション自動読み込み実行用メソッド名.
	 */
	public static final String AUTO_READ_ROUTE_METHOD = "load";
	
	// 基本マッピングパス.
	private String path = "/";

	// コンポーネントマネージャ.
	private final ComponentManager manager = new ComponentManager();
	
	/**
	 * コンストラクタ.
	 */
	public Router() {
		
	}
	
	// ComponentにAnnotationを注入.
	private static final ResponseInitialSetting injectComponent(
		Component component) {
		// アノテーションを注入.
		Quina.get().injectAnnotation(component);
		// annotationのResponse初期設定を取得.
		return LoadAnnotationComponent.loadResponse(component);
	}
	
	// ErrorAttributeComponentにAnnotationを注入.
	private static final void injectComponent(ErrorAttributeComponent component) {
		// アノテーションを注入.
		Quina.get().injectAnnotation(component);
	}
	
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
		// validationが直接指定されてない場合.
		if(validation == null) {
			// annotationのvalidationを取得.
			validation = LoadAnnotationValidate.loadValidation(component);
		}
		// annotationのResponse初期設定を取得.
		ResponseInitialSetting responseInitialSetting = injectComponent(component);
		// any登録.
		manager.put(validation, responseInitialSetting, component);
		return this;
	}

	/**
	 * エラーが発生した時の実行コンポーネントを設定します.
	 * @param component エラーコンポーネントを設定します.
	 * @return Router このオブジェクトが返却されます.
	 */
	public Router error(ErrorAttributeComponent component) {
		// annotationの定義からHttpステータス条件を取得.
		int[] startEndStatus = LoadAnnotationRoute.loadErrorRoute(component);
		if(startEndStatus == null) {
			// 存在しない場合は共通エラー登録.
			return error(0, 0, component);
		}
		// 存在する場合はAnnotation定義のHttpステータスを設定.
		return error(startEndStatus[0], startEndStatus[1], component);
	}
	
	/**
	 * エラーが発生した時の実行コンポーネントを設定します.
	 * @param status 対象Httpステータスが発生した場合にエラー実行します.
	 * @param component 実行コンポーネントを設定します.
	 * @return Router このオブジェクトが返却されます.
	 */
	public Router error(int status, ErrorAttributeComponent component) {
		if(status <= 0) {
			throw new QuinaException(
				"The specified Http status is out of range.");
		}
		return error(status, 0, component);
	}
	
	/**
	 * エラーが発生した時の実行コンポーネントを設定します.
	 * @param startStatus 開始範囲のHttpステータスを設定します.
	 * @param endStatus 終了範囲のHttpステータスを設定します.
	 * @param component 実行コンポーネントを設定します.
	 * @return Router このオブジェクトが返却されます.
	 */
	public Router error(int startStatus, int endStatus,
		ErrorAttributeComponent component) {
		// Annotationを注入.
		injectComponent(component);
		// エラー登録.
		manager.putError(startStatus, endStatus, component);
		return this;
	}

	/**
	 * 対象コンポーネントとルートを紐付けます.
	 * @param component 実行コンポーネントを設定します.
	 *                  このコンポーネントに対して @Route のアノテーション定義
	 *                  が設定されている必要があります.
	 * @return Router このオブジェクトが返却されます.
	 */
	public Router route(Component component) {
		final String path = LoadAnnotationRoute.loadRoute(component);
		if(path == null) {
			throw new QuinaException(
				"Route annotation definition does not exist " +
				"in the specified component:" + component.getClass());
		}
		return route(path, null, component);
	}

	/**
	 * 対象コンポーネントとルートを紐付けます.
	 * @param path コンポーネント実行するURLのパスを設定します.
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
		// validationが直接指定されてない場合.
		if(validation == null) {
			// annotationのvalidationを取得.
			validation = LoadAnnotationValidate.loadValidation(component);
		}
		// annotationのResponse初期設定を取得.
		ResponseInitialSetting responseInitialSetting = injectComponent(component);
		// パスを指定して登録.
		manager.put(path, validation, responseInitialSetting, component);
		return this;
	}
	
	/**
	 * AutoRoute実行.
	 * @return Router このオブジェクトが返却されます.
	 */
	protected final Router autoRoute() {
		java.lang.Class<?> clazz;
		java.lang.reflect.Method method;
		try {
			// AutoRoute実行用のクラスを取得.
			clazz = Class.forName(
				QuinaConstants.CDI_PACKAGE_NAME + "." + AUTO_READ_ROUTE_CLASS);
			// 実行メソッドを取得.
			method = clazz.getMethod(AUTO_READ_ROUTE_METHOD);
		} catch(Exception e) {
			// クラスローディングやメソッド読み込みに失敗した場合は処理終了.
			return this;
		}
		try {
			// Methodをstatic実行.
			method.invoke(null);
		} catch(InvocationTargetException it) {
			// メソッド実行でエラーの場合はエラー返却.
			throw new QuinaException(it.getCause());
		} catch(Exception e) {
			// メソッド実行でエラーの場合はエラー返却.
			throw new QuinaException(e);
		}
		return this;
	}
	
	/**
	 * 指定URLに対するコンポーネントを取得.
	 * @param url urlを設定します.
	 * @param method HttpMethodを設定します.
	 * @return RegisterComponent 登録コンポーネントが返却されます.
	 */
	public RegisterComponent get(String url, Method method) {
		return manager.get(url, method);
	}

	/**
	 * 指定URLに対するコンポーネントを取得.
	 * @param url urlを設定します.
	 * @param urls [/]で配列化されたURLを設定します.
	 * @param method HttpMethodを設定します.
	 * @return RegisterComponent 登録コンポーネントが返却されます.
	 */
	public RegisterComponent get(String url, String[] urls, Method method) {
		return manager.get(url, urls, method);
	}

	/**
	 * エラー発生時に呼び出されるコンポーネントを取得.
	 * @param status 対象のHTTPエラーステータスを設定します.
	 * @return ErrorAttributeComponent エラーコンポーネントが返却されます.
	 */
	public ErrorAttributeComponent getError(int status) {
		return manager.getError(status);
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
