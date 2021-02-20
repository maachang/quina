package quina;

public class Quina {
	private static final Quina SNGL = new Quina();

	/**
	 * quinaを取得.
	 * @return Quina quinaが返却されます.
	 */
	public static final Quina get() {
		return SNGL;
	}

	/**
	 * routerを取得.
	 * @return Router ルータが返却されます.
	 */
	public static final Router router() {
		return SNGL.getRouter();
	}

	private Quina() {}

	/**
	 * ルータを取得.
	 * @return Router ルータが返却されます.
	 */
	public Router getRouter() {
		return null;
	}
}
