package quina.test;

import quina.Quina;
import quina.logger.annotation.LogConfig;

/**
 * QuinaTest.
 */
@LogConfig(directory="./log")
public class QuinaTest {
	private QuinaTest() {}
	public static final void main(String[] args) throws Exception {
		// 初期化処理.
		Quina.init(QuinaTest.class, args);
		
		// 開始処理.
		Quina.get()
			.start()
			.await();
	}

}
