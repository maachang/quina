package quina.test;

import quina.Quina;
import quina.annotation.cdi.CdiScoped;
import quina.annotation.log.LogConfig;
import quina.annotation.log.LogDefine;
import quina.annotation.quina.ConfigDirectory;
import quina.logger.Log;

/**
 * CDIを使ったQuinaテスト.
 */
@CdiScoped
@ConfigDirectory("./")
@LogConfig(directory="./logDir")
@LogConfig(name="greeting", directory="./logDir")
public class QuinaTest {
	@LogDefine
	private static Log log;
	private QuinaTest() {}
	public static final void main(String[] args) throws Exception {
		// 初期化処理して開始処理.
		Quina.init(QuinaTest.class, args);
		
		log.info("start Quina Test");
		
		// 開始＋Await.
		Quina.get().startAwait();
	}

}
