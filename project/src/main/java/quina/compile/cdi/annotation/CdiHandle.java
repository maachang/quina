package quina.compile.cdi.annotation;

/**
 * Cdi(Contexts and Dependency Injection)の定義アノテーション
 * をカスタムローディングを行うハンドル.
 * 
 * CdiHandleはScope系のアノテーションに対して、フィールドに
 * Cdi(注入処理)を行いますが、これに対してオリジナルの設定
 * 定義を行うための拡張機能を実装するためのハンドルです.
 */
public interface CdiHandle {
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
