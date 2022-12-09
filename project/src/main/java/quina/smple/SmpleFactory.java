package quina.smple;

import quina.exception.QuinaException;
import quina.util.Flag;
import quina.util.collection.IndexKeyValueList;

/**
 * Smpleファクトリ.
 * Smple展開済みオブジェクトを管理します.
 */
public final class SmpleFactory {
	
	// シングルトン.
	private static final SmpleFactory SNGL = new SmpleFactory();
	
	// コンストラクタ.
	private SmpleFactory() {}
	
	/**
	 * オブジェクトを取得.
	 * @return SmpleFactory オブジェクトが返却されます.
	 */
	public static final SmpleFactory getInstance() {
		return SNGL;
	}
	
	// Smple管理.
	private IndexKeyValueList<String, Smple> manager =
		new IndexKeyValueList<String, Smple>();
	
	// fixフラグ.
	private final Flag fixFlag = new Flag(false);
	
	/**
	 * Smpleを登録.
	 * @param name Smple名を設定します.
	 * @param instance smpleインスタンスを設定します.
	 */
	public void register(String name, Smple instance) {
		// 入力エラー.
		if(name == null || name.isBlank() || instance == null) {
			throw new QuinaException("The argument is null.");
		} else if(fixFlag.get()) {
			throw new QuinaException("It has already been confirmed.");
		}
		// 登録.
		manager.put(name, instance);
	}
	
	/**
	 * Smple登録を確定.
	 */
	public void fix() {
		fixFlag.set(true);
	}
	
	/**
	 * Smple登録が確定しているか取得.
	 * @return boolean trueの場合確定しています.
	 */
	public boolean isFix() {
		return fixFlag.get();
	}
	
	/**
	 * Smpleオブジェクトを取得.
	 * @param name Smple名を設定します.
	 * @return Smple Smpleオブジェクトが返却されます.
	 */
	public Smple get(String name) {
		if(name == null || name.isBlank()) {
			throw new QuinaException("The argument is null.");
		}
		return manager.get(name);
	}
	
	/**
	 * SmpleBeanを取得.
	 * @param smpleName Smple名を設定します.
	 * @param beanName SmpleBean名を設定します.
	 * @return SmpleBean 空のSmpleBeanオブジェクトが返却されます.
	 */
	public SmpleBean smpleBean(
		String smpleName, String beanName) {
		return smpleBean(get(smpleName), beanName);
	}
	
	/**
	 * SmpleBeanを取得.
	 * @param smple 対象のSmpleを設定します.
	 * @param beanName SmpleBean名を設定します.
	 * @return SmpleBean 空のSmpleBeanオブジェクトが返却されます.
	 */
	public SmpleBean smpleBean(
		Smple smple, String beanName) {
		if(smple == null) {
			return null;
		}
		return smple.createBean(beanName);
	}
	
	/**
	 * Smple登録数を取得.
	 * @return int Smple登録数が返却されます.
	 */
	public int size() {
		return manager.size();
	}
	
	/**
	 * Smple登録名群を取得.
	 * @return String[] Smple登録名群が返却されます.
	 */
	public String[] names() {
		final int len = manager.size();
		final String[] ret = new String[len];
		for(int i = 0; i < len; i ++) {
			ret[i] = manager.keyAt(i);
		}
		return ret;
	}
	
	/**
	 * Smpleオブジェクトを取得.
	 * @param name Smple名を設定します.
	 * @return Smple Smpleオブジェクトが返却されます.
	 */
	public static final Smple getSmple(String name) {
		return SNGL.get(name);
	}
	
}