package quina.http.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import quina.QuinaConfig;
import quina.QuinaService;
import quina.util.Xor128;
import quina.util.collection.TypesClass;

/**
 * HttpSessionサービス.
 */
public class HttpSessionService implements QuinaService {
	
	// QuinaConfig.
	private QuinaConfig config = new QuinaConfig(
		"httpSession"
		// セッションタイムアウト(30分).
		,"loginTimeout", TypesClass.Long, HttpSessionConstants.getSessionTimeout()
		// セッションキー長.
		,"keyLength", TypesClass.Integer, HttpSessionConstants.getSessionKeyLength()
		// HttpSessionService終了時に現在のセッション情報を保存する先.
		,"saveSession", TypesClass.String, HttpSessionConstants.getSaveSessionFileName()
	);
	
	// ログイン管理リスト.
	private Map<String, SessionStorage> session =
		new ConcurrentHashMap<String, SessionStorage>();
	
	// Read-Writeロックオブジェクト.
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	// ランダムオブジェクト(ThreadLocal).
	private final ThreadLocal<Xor128> randLocal = new ThreadLocal<Xor128>();

	
	
}
