package quina.annotation;

/**
 * On/Offを示すスイッチ.
 */
public enum Switch {
	/**
	 * 未定義.
	 */
	None("none", null),
	/**
	 * モードON.
	 */
	On("on", true),
	/**
	 * モードOFF.
	 */
	Off("off", false);
	
	private Boolean mode;
	private String name;
	
	private Switch(String name, Boolean mode) {
		this.name = name;
		this.mode = mode;
	}
	
	/**
	 * 名前を取得.
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * モードを取得.
	 * @return Boolean
	 */
	public Boolean getMode() {
		return mode;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
