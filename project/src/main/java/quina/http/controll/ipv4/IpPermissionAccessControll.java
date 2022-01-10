package quina.http.controll.ipv4;

import java.net.InetSocketAddress;
import java.util.List;

import quina.exception.QuinaException;
import quina.http.Request;
import quina.http.controll.AccessControll;
import quina.http.server.HttpServerRequest;
import quina.net.IpPermission;
import quina.net.IpV4Range;
import quina.util.collection.ObjectList;

/**
 * 指定Ipアドレス範囲設定群を許可.
 */
public class IpPermissionAccessControll
	implements AccessControll {
	
	// Ipパーミッション.
	private IpPermission ipPermission;
	
	/**
	 * IpPermission定義なし.
	 */
	protected static final void notDefine() {
		throw new QuinaException("Ip permission definition is not set.");
	}
	
	/**
	 * 指定アドレス範囲設定群に対するアクセスコントロール生成.
	 * @param args 指定範囲アドレス群を設定します
	 * @return IpPermissionAccessControll アクセスコントロールが返却されます.
	 */
	public static final IpPermissionAccessControll create(
		IpV4Range... args) {
		if(args == null || args.length == 0) {
			notDefine();
		}
		ObjectList<IpV4Range> list = new ObjectList<IpV4Range>();
		int len = args.length;
		for(int i = 0; i < len; i ++) {
			list.add(args[i]);
		}
		return new IpPermissionAccessControll(list);
	}
	
	/**
	 * 指定アドレス範囲設定群に対するアクセスコントロール生成.
	 * @param args 指定範囲アドレス群を設定します
	 * @return IpPermissionAccessControll アクセスコントロールが返却されます.
	 */
	public static final IpPermissionAccessControll create(
		ObjectList<IpV4Range> args) {
		if(args == null || args.size() == 0) {
			notDefine();
		}
		ObjectList<IpV4Range> list = new ObjectList<IpV4Range>();
		int len = args.size();
		for(int i = 0; i < len; i ++) {
			list.add(args.get(i));
		}
		return new IpPermissionAccessControll(list);
	}
	
	/**
	 * 指定アドレス範囲設定群に対するアクセスコントロール生成.
	 * @param args 指定範囲アドレス群を設定します
	 * @return IpPermissionAccessControll アクセスコントロールが返却されます.
	 */
	public static final IpPermissionAccessControll create(
		List<IpV4Range> args) {
		if(args == null || args.size() == 0) {
			notDefine();
		}
		ObjectList<IpV4Range> list = new ObjectList<IpV4Range>();
		int len = args.size();
		for(int i = 0; i < len; i ++) {
			list.add(args.get(i));
		}
		return new IpPermissionAccessControll(list);
	}
	
	/**
	 * 指定アドレス範囲設定群に対するアクセスコントロール生成.
	 * @param args 指定アドレスの文字列群を設定します.
	 * @return IpPermissionAccessControll アクセスコントロールが返却されます.
	 */
	public static final IpPermissionAccessControll createDefine(
		String... args) {
		if(args == null || args.length == 0) {
			notDefine();
		}
		IpV4Range r;
		ObjectList<IpV4Range> list = new ObjectList<IpV4Range>();
		int len = args.length;
		for(int i = 0; i < len; i ++) {
			r = new IpV4Range(args[i]);
			list.add(r);
			r = null;
		}
		return new IpPermissionAccessControll(list);
	}
	
	/**
	 * 指定アドレス範囲設定群に対するアクセスコントロール生成.
	 * @param args 指定アドレスの文字列群を設定します
	 * @return IpPermissionAccessControll アクセスコントロールが返却されます.
	 */
	public static final IpPermissionAccessControll createDefine(
		ObjectList<String> args) {
		if(args == null || args.size() == 0) {
			notDefine();
		}
		IpV4Range r;
		ObjectList<IpV4Range> list = new ObjectList<IpV4Range>();
		int len = args.size();
		for(int i = 0; i < len; i ++) {
			r = new IpV4Range(args.get(i));
			list.add(r);
			r = null;
		}
		return new IpPermissionAccessControll(list);
	}
	
	/**
	 * 指定アドレス範囲設定群に対するアクセスコントロール生成.
	 * @param args 指定アドレスの文字列群を設定します
	 * @return IpPermissionAccessControll アクセスコントロールが返却されます.
	 */
	public static final IpPermissionAccessControll createDefine(
		List<String> args) {
		if(args == null || args.size() == 0) {
			notDefine();
		}
		IpV4Range r;
		ObjectList<IpV4Range> list = new ObjectList<IpV4Range>();
		int len = args.size();
		for(int i = 0; i < len; i ++) {
			r = new IpV4Range(args.get(i));
			list.add(r);
			r = null;
		}
		return new IpPermissionAccessControll(list);
	}
	
	// コンストラクタ.
	protected IpPermissionAccessControll() {}
	
	/**
	 * コンストラクタ.
	 * @param args IpV4Range群を設定します.
	 */
	protected IpPermissionAccessControll(
		ObjectList<IpV4Range> args) {
		IpPermission pm = new IpPermission(args);
		this.ipPermission = pm;
	}
	
	@Override
	public boolean isAccess(Request req) {
		if(req instanceof HttpServerRequest) {
			final InetSocketAddress addr = ((HttpServerRequest)req)
				.getElement().getRemoteAddress();
			return ipPermission.isRange(addr.getAddress());
		}
		return false;
	}
}
