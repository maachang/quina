package quina.http.controll.ipv4;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;

import quina.Quina;
import quina.QuinaConfig;
import quina.QuinaService;
import quina.QuinaUtil;
import quina.util.Flag;
import quina.util.collection.QuinaMap;

/**
 * IpV4用のパーミッションアクセスコントロールサービス.
 */
public class IpPermissionControllService
	implements QuinaService {
	
	// コンフィグ名.
	private static final String CONFIG_NAME = "ipPermission";
	
	// Ipパーミッションアクセスコントロール管理.
	private final Map<String, IpPermissionControll> manager =
		new ConcurrentHashMap<String, IpPermissionControll>();
	
	// ロードコンフィグフラグ.
	private final Flag loadConfigFlag = new Flag(false);
	
	// サービス開始フラグ.
	private final Flag startFlag = new Flag(false);
	
	/**
	 * IpPermissionAccessControllServiceを取得.
	 * @return IpPermissionAccessControllService
	 *		IpPermissionAccessControllServiceが返却されます.
	 */
	public static final IpPermissionControllService getService() {
		return Quina.get().getIpPermissionAccessControllService();
	}
	
	/**
	 * 登録名を設定してIpPermissionAccessControllを取得.
	 * @param name 登録名を設定します.
	 * @return IpPermissionAccessControll 登録されてるアクセスコントロールが
	 *                                    返却されます.
	 */
	public IpPermissionControll get(String name) {
		// サービスが開始していない場合はエラー.
		checkService(false);
		return manager.get(name);
	}
	
	/**
	 * 登録数を取得.
	 * @return int 登録数が返却されます.
	 */
	public int size() {
		// サービスが開始していない場合はエラー.
		checkService(false);
		return manager.size();
	}
	
	/**
	 * 登録名一覧を取得.
	 * @return String[] 登録名一覧が返却されます.
	 */
	public String[] getNames() {
		// サービスが開始していない場合はエラー.
		checkService(false);
		final int len = manager.size();
		if(len == 0) {
			return new String[0];
		}
		int cnt = 0;
		final String[] ret = new String[len];
		Iterator<String> itr = manager.keySet().iterator();
		while(itr.hasNext()) {
			ret[cnt ++] = itr.next();
		}
		return ret;
	}
	
	@Override
	public ReadWriteLock getLock() {
		// ロックオブジェクトは利用しない.
		return null;
	}
	
	@Override
	public boolean isLoadConfig() {
		return loadConfigFlag.get();
	}
	
	@Override
	public QuinaConfig getConfig() {
		// コンフィグは利用しない.
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean loadConfig(String configDir) {
		// サービスが開始している場合はエラー.
		checkService(true);
		// 既にロード済み.
		if(loadConfigFlag.get()) {
			return true;
		}
		// Ipパーミッションのコンフィグ読み込み.
		QuinaMap<String, Object> json = QuinaUtil.loadJson(
			configDir, CONFIG_NAME);
		// コンフィグ情報が存在しない場合.
		if(json == null || json.size() == 0) {
			return false;
		}
		
		// Json-format.
		// "ipPermissionName1": [
		//   ,"192.168.0.0/24"
		//   ,"192.168.10.1 - 192.168.10.10"
		//   ,"maachang.com"
		// ],
		// "ipPermissionName2": [
		//   ....
		//
		
		String key;
		List<String> list;
		IpPermissionControll ipp;
		boolean ret = false;
		final int len = json.size();
		for(int i = 0; i < len; i ++) {
			if(!(json.valueAt(i) instanceof List)) {
				continue;
			}
			key = json.keyAt(i);
			list = (List<String>)json.valueAt(i);
			if(list.size() > 0) {
				ipp = IpPermissionControll.createDefine(list);
				manager.put(key, ipp);
				ret = true;
			}
		}
		return ret;
	}
	
	/**
	 * コンフィグ読み込みをFix.
	 */
	public void fixConfig() {
		// コンフィグ読み込み完了.
		loadConfigFlag.set(true);
	}
	
	@Override
	public boolean isStartService() {
		return startFlag.get();
	}

	@Override
	public void startService() {
		startFlag.set(true);
	}

	@Override
	public void stopService() {
		startFlag.set(false);
	}
}
