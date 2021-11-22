package quina.test;

import quina.QuinaConfig;
import quina.QuinaService;
import quina.annotation.log.LogDefine;
import quina.annotation.quina.ConfigElement;
import quina.annotation.quina.ConfigName;
import quina.annotation.quina.QuinaServiceScoped;
import quina.logger.Log;
import quina.util.collection.TypesClass;

//@QuinaServiceScoped("test")
public class QuinaServiceTest implements QuinaService {
	@LogDefine
	private Log log;
	
	@ConfigName("testConf")
	@ConfigElement(name="name", type=TypesClass.String)
	@ConfigElement(name="age", type=TypesClass.Integer)
	@ConfigElement(name="sex", type=TypesClass.Boolean)
	private QuinaConfig config;
	
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
		log.info("  config: " + config.getName());
		log.info("    name: " + config.getString("name"));
		log.info("    age: " + config.getInteger("age"));
		log.info("    sex: " + config.getBoolean("sex"));
		
		startFlag = true;
	}

	@Override
	public void stopService() {
		log.info(" stop " + this.getClass());
		startFlag = false;
	}
}
