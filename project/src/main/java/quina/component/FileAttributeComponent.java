package quina.component;

/**
 * ファイル属性コンポーネント.
 */
interface FileAttributeComponent extends Component {
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
	 * コンポーネントタイプを取得.
	 * @return ComponentType コンポーネントタイプが返却されます.
	 */
	@Override
	default ComponentType getType() {
		return ComponentType.FILE;
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
