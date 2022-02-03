package quina.test;

import quina.Quina;
import quina.annotation.QuinaServiceSelection;
import quina.compile.cdi.annotation.CdiScoped;
import quina.logger.Log;
import quina.logger.annotation.LogConfig;
import quina.logger.annotation.LogDefine;

/**
 * CDIを使ったQuinaテスト.
 */
@CdiScoped
@LogConfig(directory="./logDir")
@LogConfig(name="greeting", directory="./logDir")
@QuinaServiceSelection(name="storage", define="jdbc")
public class QuinaTest {
	@LogDefine
	private static Log log;
	
	// コンストラクタ.
	private QuinaTest() {}
	
	// メイン実行.
	public static final void main(String[] args) throws Exception {
		// 初期化処理して開始処理.
		Quina.init(QuinaTest.class, args);
		
		log.info("start Quina Test");
		
		// 開始＋Await.
		Quina.get().startAwait();
	}

}
