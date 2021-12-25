package quina.test;

import quina.Quina;
import quina.annotation.cdi.CdiScoped;
import quina.annotation.log.LogConfig;
import quina.annotation.log.LogDefine;
import quina.annotation.quina.QuinaServiceSelection;
import quina.logger.Log;

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
