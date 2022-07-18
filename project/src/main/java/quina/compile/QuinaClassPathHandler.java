package quina.compile;

import quina.compile.QuinaCTClassLoad.AbstractClassPathHandler;

/**
 * ClassPathHandler.
 */
public class QuinaClassPathHandler
	extends AbstractClassPathHandler {
	
	// クラス.
	private static final String CLASS = ".class";
	
	// プロパティ.
	private static final String PROP = ".properties";
	
	// smple.
	private static final String SMPLE = ".sml";
	
	/**
	 * コンストラクタ.
	 * @param params CompileToolパラメータを設定します.
	 */
	public QuinaClassPathHandler(QuinaCTParams params) {
		super(params);
	}

	
	@Override
	public String[] targetExtension() {
		// 対応する拡張子を返却.
		return new String[] {
			CLASS
			,PROP
			,SMPLE
		};
	}

	@Override
	public void addClassName(
		QuinaCTParams params, String className, String extension) {
		// クラス拡張子.
		if(extension.equals(CLASS)) {
			params.classFileList.add(className);
		// smple拡張子.
		} else if(extension.equals(SMPLE)) {
			params.smpleList.add(className);
		// リソース拡張を読み込む設定の場合.
		} else if(extension.equals(PROP) && params.registerResourceItemFlag) {
			params.regResourceList.add(className);
		}
	}

}
