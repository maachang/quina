package quina.test;

import quina.Quina;
import quina.annotation.log.LogConfig;

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
