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

	@Override
	default ComponentType getType() {
		return ComponentType.FILE;
	}
}
