package quina.test;

import quina.QuinaConfig;
import quina.QuinaService;
import quina.annotation.log.LogDefine;
import quina.annotation.quina.QuinaServiceScoped;
import quina.logger.Log;
import quina.util.collection.TypesClass;

@QuinaServiceScoped("test")
public class QuinaServiceTest implements QuinaService {
	@LogDefine
	private Log log;
	
	private QuinaConfig config = new QuinaConfig("test",
		"test", TypesClass.String, "hoge");
	
	private boolean startFlag = false;

	@Override
	public QuinaConfig getConfig() {
		return config;
	}

	@Override
	public boolean isStartService() {
		return startFlag;
	}

	@Override
	public void startService() {
		log.info(" start " + this.getClass());
		startFlag = true;
	}

	@Override
	public void stopService() {
		log.info(" stop " + this.getClass());
		startFlag = false;
	}
}
