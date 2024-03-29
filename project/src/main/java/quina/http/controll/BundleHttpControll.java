package quina.http.controll;

import java.util.List;

import quina.exception.QuinaException;
import quina.http.Request;
import quina.http.Response;
import quina.util.collection.ObjectList;

/**
 * 複数のアクセスコントロールを束ねたアクセスコントロール.<br>
 * <br>
 * このオブジェクトを利用することで、複数のアクセスコントロールを
 * 束ねて、quina.http.Requestのアクセスを評価します.<br>
 * <br>
 * モードは４つあり<br>
 * 　1)１つでも一致する場合は許可する.<br>
 * 　2)全部一致することで許可する.<br>
 * 　3)１つでも一致する場合は許可しない.<br>
 * 　4)全部一致すると許可しない.<br>
 * があります.<br>
 * <br>
 * デフォルトでは(1)が適用されます.<br>
 */
public class BundleHttpControll
	implements HttpControll {
	
	// １つでも一致する場合は許可する.
	private static final int ONE_MATCH =0x0001;
	
	// 全部一致することで許可する
	private static final int ALL_MATCH =0x0002;
	
	// 否定マッチ.
	private static final int NOT_MATCH = 0x0080;
	
	// マスク値.
	private static final int ALL_MASK = ONE_MATCH | ALL_MATCH | NOT_MATCH;
	
	/**
	 * アクセスモード: １つでも一致する場合は許可する.
	 */
	public static final int ALLOW_IF_ONE_MATCHES = ONE_MATCH;
	
	/**
	 * アクセスモード: 全部一致することで許可する.
	 */
	public static final int ALLOW_IF_ALL_MATCHES = ALL_MATCH;
	
	/**
	 * アクセスモード: １つでも一致する場合は許可しない.
	 *                 全く一致しない場合は許可する.
	 */
	public static final int NOT_ALLOWED_IF_EVEN_ONE_MATCHES = ONE_MATCH | NOT_MATCH;
	
	/**
	 * アクセスモード: 全部一致する場合は許可しない.
	 *                 １つでも一致しない場合は許可する.
	 */
	public static final int NOT_PERMITTED_IF_ALL_MATCHE = ALL_MATCH | NOT_MATCH;
	
	// 束ねたアクセスコントロール.
	private final ObjectList<HttpControll> list =
		new ObjectList<HttpControll>();
	
	// アクセスモード.
	private int accessMode = ALLOW_IF_ONE_MATCHES;
	
	/**
	 * コンストラクタ.
	 */
	public BundleHttpControll() {}
	
	/**
	 * コンストラクタ.
	 * @param args 束ねるアクセスコントロール群を設定します.
	 */
	public BundleHttpControll(HttpControll... args) {
		
	}
	
	/**
	 * コンストラクタ.
	 * @param args 束ねるアクセスコントロール群を設定します.
	 */
	public BundleHttpControll(
		ObjectList<BundleHttpControll> args) {
		
	}
	
	/**
	 * コンストラクタ.
	 * @param args 束ねるアクセスコントロール群を設定します.
	 */
	public BundleHttpControll(
		List<BundleHttpControll> args) {
		
	}
	
	/**
	 * コンストラクタ.
	 * @param mode アクセスモードを設定します.
	 * @param args 束ねるアクセスコントロール群を設定します.
	 */
	public BundleHttpControll(int mode, HttpControll... args) {
		setAccessMode(mode);
		final int len = args == null ? 0 : args.length;
		for(int i = 0; i < len; i ++) {
			add(args[i]);
		}
	}
	
	/**
	 * コンストラクタ.
	 * @param mode アクセスモードを設定します.
	 * @param args 束ねるアクセスコントロール群を設定します.
	 */
	public BundleHttpControll(int mode,
		ObjectList<BundleHttpControll> args) {
		setAccessMode(mode);
		final int len = args == null ? 0 : args.size();
		for(int i = 0; i < len; i ++) {
			add(args.get(i));
		}
	}
	
	/**
	 * コンストラクタ.
	 * @param mode アクセスモードを設定します.
	 * @param args 束ねるアクセスコントロール群を設定します.
	 */
	public BundleHttpControll(int mode,
		List<BundleHttpControll> args) {
		setAccessMode(mode);
		final int len = args == null ? 0 : args.size();
		for(int i = 0; i < len; i ++) {
			add(args.get(i));
		}
	}
	
	/**
	 * 束ねたいアクセスコントロールを追加します.
	 * @param c 追加するアクセスコントロールを設定します.
	 * @return BundleAccessControll このオブジェクトが返却されます.
	 */
	protected BundleHttpControll add(HttpControll c) {
		if(c == null) {
			throw new QuinaException(
				"The access control to be added is not set.");
		}
		list.add(c);
		return this;
	}
	
	/**
	 * アクセスモードを設定.
	 * @param mode アクセスモードを設定します.
	 * @return BundleAccessControll このオブジェクトが返却されます.
	 */
	public BundleHttpControll setAccessMode(int mode) {
		mode = ALL_MASK & mode;
		// 設定されていないか、否定のみの場合.
		if(mode == 0 || mode == NOT_MATCH) {
			// 未確定な設定.
			throw new QuinaException(
				"The correct access mode is not set.");
		}
		accessMode = mode;
		return this;
	}
	
	/**
	 * アクセスモードが返却されます.
	 * @return int アクセスモードが返却されます.
	 */
	public int getAccessMode() {
		return accessMode;
	}
	
	/**
	 * アクセスモードを文字列で取得
	 * @return String アクセスモードを文字列で返却します.
	 */
	public String getAccessModeToString() {
		return "";
	}
	
	/**
	 * アクセスが制限されてるかチェック.
	 * @param req HttpRequestを設定します.
	 * @param res HttpResponseを設定します.
	 * @return boolean trueの場合アクセス可能です.
	 */
	@Override
	public boolean isAccess(Request req, Response<?> res) {
		if(list.size() == 0) {
			return true;
		}
		final int len = list.size();
		// 不一致条件の場合はtrue.
		final boolean notRet = (accessMode & NOT_MATCH) == NOT_MATCH;
		// 全てが一致する場合の処理.
		if((accessMode & ALL_MATCH) == ALL_MATCH) {
			for(int i = 0; i < len; i ++) {
				// 要素の１つが一致しない場合.
				if(!list.get(i).isAccess(req, res)) {
					return notRet;
				}
			}
			// 全てが一致する場合.
			return !notRet;
		// １つが一致する場合の処理.
		} else if((accessMode & ONE_MATCH) == ONE_MATCH) {
			for(int i = 0; i < len; i ++) {
				// 要素の１つが一致する場合.
				if(list.get(i).isAccess(req, res)) {
					return !notRet;
				}
			}
			// 全てが一致しない場合.
			return notRet;
		}
		return true;
	}
}
