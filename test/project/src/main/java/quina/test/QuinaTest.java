package quina.test;

import quina.Quina;
import quina.annotation.log.LogConfig;

/**
 * QuinaTest.
 */
@LogConfig(directory="./logDir")
public class QuinaTest {
	private QuinaTest() {}
	public static final void main(String[] args) throws Exception {
		// 初期化処理して開始処理.
		Quina.init(QuinaTest.class, args).startAwait();
	}

}
