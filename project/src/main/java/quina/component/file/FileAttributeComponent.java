package quina.component.file;

import quina.component.Component;
import quina.component.ComponentConstants;
import quina.component.ComponentType;

/**
 * ファイル属性コンポーネント.
 */
public interface FileAttributeComponent extends Component {
	/**
	 * EtagManagerを設定.
	 * @param etagManager EtagManagerを設定します.
	 */
	public void setEtagManager(EtagManager etagManager);

	/**
	 * EtagManagerを取得.
	 * @return EtagManager EtagManagerが返却されます.
	 */
	public EtagManager getEtagManager();
	
	/**
	 * キャッシュモードを取得.
	 * @return Boolean [true]の場合キャッシュモードは有効です.
	 *                 null の場合、デフォルトのキャッシュモードが
	 *                 利用されます.
	 */
	public Boolean getCacheMode();

	/**
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	default ComponentType getType() {
		return ComponentType.File;
	}

	/**
	 * 対応HTTPメソッド定義を取得.
	 * @return int このコンポーネントが対応するHTTPメソッド定義が返却されます.
	 */
	@Override
	default int getMethod() {
		return ComponentConstants.HTTP_METHOD_ALL;
	}
}
