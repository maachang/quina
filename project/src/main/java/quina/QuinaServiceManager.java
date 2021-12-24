package quina;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import quina.annotation.cdi.AnnotationCdiConstants;
import quina.annotation.quina.AnnotationQuina;
import quina.exception.QuinaException;
import quina.util.Flag;
import quina.util.collection.ObjectList;

/**
 * QuinaService管理オブジェクト.
 * 
 * 管理方法は２種類.
 * 
 * 1)１つは対象のQuinaServiceに対して、サービス名を設定して登録.
 * 
 * 2)もう１つは対象のQuinaServiceに対して、サービス名とサービス定義
 * 名を設定して登録.
 * 
 * 違いは、サービス名だけを定義したものは、それ自体がサービスとして
 * 登録されQuina実行時に組み込まれます.
 * 
 * 一方のサービス名とサービス定義名を定義したサービス登録は、実際には
 * Quina実行時に組み込まれるサービスとして、登録されません.
 * 
 * サービス定義名の存在定義として、同一サービス名で複数のサービスを
 * 定義し、それをQuinaのMain実行時にサービス定義名を指定して、その
 * サービスを利用するためのものです.
 * 
 * 例としてStorageと言うサービスに対してMemoryStorageとJDBCStorage
 * をそれぞれ"memory"と"jdbc"で登録します.
 * 
 * <例>
 * 
 * ＠QuinaServiceScoped(name="storage", define="memory")
 * public class MemoryStorageService implements QuinaService {
 *   ........
 * }
 * 
 * ＠QuinaServiceScoped(name="storage", define="jdbc")
 * public class JDBCStorageService implements QuinaService {
 *   ........
 * }
 * 
 * これを、QuinaMain実行時に以下のように定義することで"jdbc"が
 * 利用可能となり、JDBCStorageServiceがQuinaのサービスとして
 * 読み込まれます.
 * 
 * <例>
 * 
 * ＠QuinaServiceSelection(name="storage", define="jdbc")
 * public class QuinaMain {
 *   public static void main(String[] args) {
 *     Quina.init(QuinaMain.class, args);
 *     Quina.get().startAwait();
 *   }
 * }
 * 
 * このサービス定義名が存在する理由として、アノテーションで
 * 各種サービス登録がされるのですが、それに対してこの定義がない
 * 場合、利用したいサービスの選択が出来なくなります.
 * 
 * そのため、このサービス定義が必要となります.
 * 
 * あとサービスの実行順に関してはサービス登録IDを指定することで
 * 対応が出来て、値が低いほど先に実行されます.
 */
public final class QuinaServiceManager {
	/**
	 * QuinaServiceScopedアノテーション自動読み込み実行用クラス名.
	 */
	public static final String AUTO_READ_QUINA_SERVICE_CLASS = "LoadQuinaService";

	/**
	 * QuinaServiceScopedアノテーション自動読み込み実行用メソッド名.
	 */
	public static final String AUTO_READ_QUINA_SERVICE_METHOD = "load";
	
	// サービス管理リスト.
	private final ObjectList<QuinaServiceEntry> list =
		new ObjectList<QuinaServiceEntry>();
	
	// サービス定義リスト.
	private final ObjectList<QuinaServiceEntry> defineList =
		new ObjectList<QuinaServiceEntry>();
	
	// Fixフラグ.
	private final Flag fixFlag = new Flag(false);

	/**
	 * コンストラクタ.
	 */
	public QuinaServiceManager() {}

	// 既に登録されてる名前の登録番号を検索.
	private static final int searchService(
		ObjectList<QuinaServiceEntry> list, String name) {
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			if(name.equals(list.get(i).getName())) {
				return i;
			}
		}
		return -1;
	}
	
	// 既に登録されてる名前と定義名の登録番号を検索.
	private static final int searchDefine(
		ObjectList<QuinaServiceEntry> list, String name,
		String define) {
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			if(name.equals(list.get(i).getName()) &&
				define.equals(list.get(i).getDefine())) {
				return i;
			}
		}
		return -1;
	}

	
	/**
	 * 登録を完了させる.
	 */
	public void fix() {
		fixFlag.set(true);
	}
	
	/**
	 * 登録が完了済みかチェック.
	 * @return boolean trueの場合完了しています.
	 */
	public boolean isFix() {
		return fixFlag.get();
	}
	
	// fixされてる場合はエラー.
	protected void checkFix() {
		if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		}
	}
	
	/**
	 * AutoQuinaService実行.
	 * @return QuinaServiceManager このオブジェクトが返却されます.
	 */
	public final QuinaServiceManager autoQuinaService() {
		checkFix();
		java.lang.Class<?> clazz;
		java.lang.reflect.Method method;
		try {
			// AutoRoute実行用のクラスを取得.
			clazz = Class.forName(
				AnnotationCdiConstants.CDI_PACKAGE_NAME + "." +
				AUTO_READ_QUINA_SERVICE_CLASS);
			// 実行メソッドを取得.
			method = clazz.getMethod(AUTO_READ_QUINA_SERVICE_METHOD);
		} catch(Exception e) {
			// クラスローディングやメソッド読み込みに失敗した場合は処理終了.
			return this;
		}
		try {
			// Methodをstatic実行.
			method.invoke(null);
		} catch(InvocationTargetException it) {
			// メソッド実行でエラーの場合はエラー返却.
			throw new QuinaException(it.getCause());
		} catch(Exception e) {
			// メソッド実行でエラーの場合はエラー返却.
			throw new QuinaException(e);
		}
		return this;
	}
	
	/**
	 * QuinaServiceSelection登録処理.
	 * @param c 対象のメインクラスを設定します.
	 * @return QuinaServiceManager このオブジェクトが返却されます.
	 */
	public final QuinaServiceManager regQuinaServiceSelection(
		Class<?> c) {
		// QuinaServiceSelection登録処理.
		AnnotationQuina.regQuinaServiceSelection(this, c);
		return this;
	}
	
	/**
	 * データセット.
	 * @param service 登録サービスを設定します.
	 * @return QuinaService 前回登録されていたサービスが返却されます.
	 */
	public QuinaService put(QuinaService service) {
		if(service == null) {
			throw new QuinaException(
				"The Quina Service to be registered is null.");
		}
		checkFix();
		final Object[] svcDef = AnnotationQuina.
			loadQuinaServiceScoped(service);
		if(svcDef == null) {
			throw new QuinaException(
				"QuinaServiceScoped annotation is not defined " +
				"for the specified QuinaService.");
		}
		// 登録処理.
		return put((long)svcDef[0], (String)svcDef[1],
			(String)svcDef[2], service);
	}
	
	/**
	 * データセット.
	 * @param name サービス登録名を設定します.
	 * @param service 登録サービスを設定します.
	 * @return QuinaService 前回登録されていたサービスが返却されます.
	 */
	public QuinaService put(
		String name, QuinaService service) {
		return put(QuinaConstants.NONE_SERVICE_ID,
			name, null, service);
	}
	
	/**
	 * データセット.
	 * @param name サービス登録名を設定します.
	 * @param define サービス定義名を設定します.
	 *               nullの場合サービス定義名は存在しません.
	 * @param service 登録サービスを設定します.
	 * @return QuinaService 前回登録されていたサービスが返却されます.
	 */
	public QuinaService put(
		String name, String define, QuinaService service) {
		return put(QuinaConstants.NONE_SERVICE_ID,
			name, define, service);
	}
	
	/**
	 * データセット.
	 * @param id 登録IDを設定します.
	 *           この値が小さいほど先にサービスが実行されます.
	 * @param name サービス登録名を設定します.
	 * @param service 登録サービスを設定します.
	 * @return QuinaService 前回登録されていたサービスが返却されます.
	 */
	public QuinaService put(
		long id, String name, QuinaService service) {
		return put(id, name, null, service);
	}

	
	/**
	 * データセット.
	 * @param id 登録IDを設定します.
	 *           この値が小さいほど先にサービスが実行されます.
	 * @param name サービス登録名を設定します.
	 * @param define サービス定義名を設定します.
	 *               nullの場合サービス定義名は存在しません.
	 * @param service 登録サービスを設定します.
	 * @return QuinaService 前回登録されていたサービスが返却されます.
	 */
	public QuinaService put(
		long id, String name, String define, QuinaService service) {
		if(name == null) {
			throw new QuinaException(
				"The designated registration name is null.");
		} else if(service == null) {
			throw new QuinaException(
				"The Quina Service to be registered is null.");
		}
		checkFix();
		// 定義名が設定されている場合.
		if(define != null && !(define = define.trim()).isEmpty()) {
			// 定義登録済みの番号を検索.
			final int p = searchDefine(defineList, name, define);
			if(p == -1) {
				// 存在しない場合登録.
				defineList.add(new QuinaServiceEntry(
					id, name, define, service));
				return null;
			}
			// 再設定して返却.
			QuinaServiceEntry e = defineList.get(p);
			return e.setService(id, service);
		}
		// サービス登録済みの番号を検索.
		final int p = searchService(list, name);
		if(p == -1) {
			// 存在しない場合.
			list.add(new QuinaServiceEntry(
				id, name, null, service));
			return null;
		}
		// 再設定して返却.
		QuinaServiceEntry e = list.get(p);
		return e.setService(id, service);
	}

	/**
	 * 登録名を指定して取得.
	 * @param name 取得したい登録名を設定します.
	 * @return QuinaService 対象のサービスが返却されます.
	 */
	public QuinaService get(String name) {
		if(name != null) {
			final int p = searchService(list, name);
			if(p != -1) {
				return list.get(p).getService();
			}
		}
		return null;
	}

	/**
	 * 登録名を指定して登録項番を取得.
	 * @param name 取得したい登録名を設定します.
	 * @return int 登録項番が返却されます.
	 */
	public int getNo(String name) {
		if(name != null) {
			final int p = searchService(list, name);
			if(p != -1) {
				return p;
			}
		}
		return -1;
	}

	/**
	 * 項番を指定して登録名を取得.
	 * @param no 対象の項番を設定します.
	 * @return String 登録名が返却されます.
	 */
	public String nameAt(int no) {
		if(no >= 0 && no < list.size()) {
			return list.get(no).getName();
		}
		return null;
	}

	/**
	 * 項番を設定して取得.
	 * @param no 対象の項番を設定します.
	 * @return QuinaService 対象のサービスが返却されます.
	 */
	public QuinaService get(int no) {
		if(no >= 0 && no < list.size()) {
			QuinaServiceEntry e = list.get(no);
			System.out.println("no: " + no + " " + e);
			return list.get(no).getService();
		}
		return null;
	}

	/**
	 * 登録数を取得.
	 * @return int サービスの登録数が返却されます.
	 */
	public int size() {
		return list.size();
	}
	
	/**
	 * 定義登録されているサービスを実行サービスとして登録.
	 * @param name サービス名を設定します.
	 * @param define サービス定義名を設定します.
	 */
	public void putDefineToService(String name, String define) {
		checkFix();
		if(name == null) {
			throw new QuinaException(
				"The designated registration name is null.");
		} else if(define == null ||
			(define = define.trim()).isEmpty()) {
			throw new QuinaException(
				"The designated registration define is null or empty.");
		}
		// 既に同一のサービス名が登録されている場合.
		if(searchService(list, name) != -1) {
			throw new QuinaException(
				"The service with the specified name \"" +
				name + "\" already exists. ");
		}
		// サービスに登録する定義サービスを取得.
		int no = searchDefine(defineList, name, define);
		if(no == -1) {
			// 存在しない場合エラー.
			throw new QuinaException(
				"The definition name \"" + define +
				"\" does not exist for the specified service name \"" +
				name + "\". ");
		}
		final QuinaServiceEntry e = defineList.remove(no);
		// サービスに登録.
		list.add(e);
	}
	
	/**
	 * サービス定義リストをクリア.
	 */
	protected void clearDefine() {
		defineList.clear();
	}
	
	// サービス登録IDでソート処理.
	protected void sort() {
		if(!fixFlag.get()) {
			throw new QuinaException("Already not completed.");
		}
		Arrays.sort(list.rawArray(), 0, list.size());
	}

	/**
	 * 1つのQuinaService要素.
	 */
	protected static final class QuinaServiceEntry
		implements Comparable<QuinaServiceEntry> {
		private long id;
		private String name;
		private String define;
		private QuinaService service;

		/**
		 * コンストラクタ.
		 * @param id 登録IDを設定します.
		 * @param name サービス登録名を設定します.
		 * @param define サービス定義名を設定します.
		 * @param service 登録サービスを設定します.
		 */
		protected QuinaServiceEntry(
			long id, String name, String define,
			QuinaService service) {
			this.id = id;
			this.name = name;
			this.define = define;
			this.service = service;
			System.out.println("name: " + name + " service: " + service);
		}
		
		/**
		 * 登録IDを取得.
		 * @return long 登録IDが返却されます.
		 */
		public long getId() {
			return id;
		}
		
		/**
		 * サービス登録名を取得.
		 * @return String サービス登録名が返却されます.
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * サービス定義名を取得.
		 * @return String サービス定義名が返却されます.
		 */
		public String getDefine() {
			return define;
		}

		/**
		 * QuinaServiceを取得.
		 * @return QuinaService QuinaServiceが返却されます.
		 */
		public QuinaService getService() {
			return service;
		}
		
		// QuinaServiceを設定.
		protected QuinaService setService(long id, QuinaService newService) {
			QuinaService ret = this.service;
			this.id = id;
			this.service = newService;
			System.out.println("name: " + name + " service: " + newService);
			return ret;
		}
		
		@Override
		public int compareTo(QuinaServiceEntry o) {
			// サービス登録IDが低い項番順に並べ替えて
			// サービス実行を行う.
			if(id > o.id) {
				return 1;
			} else if(id < o.id) {
				return -1;
			}
			return 0;
		}
	}
}
