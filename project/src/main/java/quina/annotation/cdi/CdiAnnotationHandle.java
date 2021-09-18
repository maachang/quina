package quina.annotation.cdi;

/**
 * Cdi(Contexts and Dependency Injection)の定義アノテーション
 * をカスタムローディングを行うハンドル.
 */
public interface CdiAnnotationHandle {
	/**
	 * カスタムなCdi定義アノテーションをローディング.
	 * @param man CdiReflectManagerが設定されます.
	 * @param o Cdi定義を読み込むオブジェクトを設定します.
	 *          null の場合 Cdi対象のフィールドはstatic のみ
	 *          ローディングされます.
	 * @param c Cdi定義の読み込みクラスを設定します.
	 * @exception Exception 例外.
	 */
	public void load(CdiReflectManager man, Object o, Class<?> c)
		throws Exception;
}
