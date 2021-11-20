package quina.component.resource;

import quina.component.Component;
import quina.component.ComponentConstants;
import quina.component.ComponentType;

/**
 * Thread.currentThread().getClassLoader().getResourceAsStream(xxxx)
 * の条件で、Jar内のリソース情報を取得して返却処理する.
 */
public class ResourceComponent implements Component {
	// ターゲットResourceディレクトリ.
	protected String targetResourceDir = null;

	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	public ComponentType getType() {
		return ComponentType.File;
	}

	/**
	 * 対応HTTPメソッド定義を取得.
	 * @return int このコンポーネントが対応するHTTPメソッド定義が返却されます.
	 */
	@Override
	public int getMethod() {
		return ComponentConstants.HTTP_METHOD_ALL;
	}

}
