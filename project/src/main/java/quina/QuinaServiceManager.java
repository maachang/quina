package quina;

import java.lang.reflect.InvocationTargetException;

import quina.annotation.cdi.AnnotationCdiConstants;
import quina.annotation.quina.AnnotationQuina;
import quina.exception.QuinaException;
import quina.util.Flag;
import quina.util.collection.ObjectList;

/**
 * QuinaService管理オブジェクト.
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
	
	// Fixフラグ.
	private final Flag fixFlag = new Flag(false);

	/**
	 * コンストラクタ.
	 */
	public QuinaServiceManager() {}

	// 既に登録されてる名前の登録番号を検索.
	private static final int search(
		ObjectList<QuinaServiceEntry> list, String name) {
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			if(name.equals(list.get(i).getName())) {
				return i;
			}
		}
		return -1;
	}
	
	// サービス定義名が一致するEntryを取得.
	private static final QuinaServiceEntry search(
		QuinaServiceEntry top, String define) {
		QuinaServiceEntry e = top;
		while(e != null) {
			// 一致する内容を返却.
			if(define.equals(e.getDefine())) {
				return e;
			}
			e = e.getNext();
		}
		return null;
	}
	
	// サービス定義名が一致するEntryを取得して削除.
	private static final QuinaServiceEntry searchByDelete(
		QuinaServiceEntry top, String define) {
		QuinaServiceEntry e = top;
		QuinaServiceEntry bef = null;
		while(e != null) {
			// 一致する内容を削除して返却.
			if(define.equals(e.getDefine())) {
				if(bef == null) {
					
				}
				return e;
			}
			bef = e;
			e = e.getNext();
		}
		return null;
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
	
	/**
	 * AutoQuinaService実行.
	 * @return QuinaServiceManager このオブジェクトが返却されます.
	 */
	public final QuinaServiceManager autoQuinaService() {
		if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		}
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
	
	// QuinaServiceにコンフィグ内容をセット.
	private static final void _quinaServiceByAppendConfig(
		QuinaService sv) {
		Quina quina = Quina.get();
		String dir = quina.getConfigDirectory();
		// コンフィグディレクトリが設定されていない場合
		if(dir == null || dir.isEmpty()) {
			// 処理しない.
			return;
		}
		// 初期化済みの場合のみ設定する.
		// また１度もloadConfig処理が呼ばれてない場合.
		if(quina.isInit() && !sv.isLoadConfig()) {
			// コンフィグ情報をロードする.
			sv.loadConfig(dir);
		}
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
		} else if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		}
		final String[] nameDefine = AnnotationQuina.
			loadQuinaServiceScoped(service);
		if(nameDefine == null) {
			throw new QuinaException(
				"QuinaServiceScoped annotation is not defined for the " +
				"specified QuinaService.");
		}
		// 登録処理.
		return put(nameDefine[0], nameDefine[1], service);
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
		if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		} else if(name == null || service == null) {
			if(name == null) {
				throw new QuinaException(
					"The designated registration name is null.");
			}
			throw new QuinaException(
				"The Quina Service to be registered is null.");
		}
		// 定義名が設定されている場合.
		if(define != null && !(define = define.trim()).isEmpty()) {
			// 登録済みの番号を検索.
			final int p = search(list, name);
			if(p == -1) {
				// 存在しない場合.
				list.add(new QuinaServiceEntry(
					name, define, service));
				return null;
			}
			// 存在する場合、既に定義名で登録済みの
			// サービスを検索.
			QuinaServiceEntry top = list.get(p);
			QuinaServiceEntry e = search(top, define);
			if(e != null) {
				// 存在する場合は上書き.
				return e.setService(service);
			}
			// 存在しない場合は、新規作成.
			e = new QuinaServiceEntry(
				name, define, service);
			QuinaServiceEntry nxt = top.getNext();
			// セット.
			top.putNext(e);
			e.putNext(nxt);
			return null;
		}
		// コンフィグ読み込み.
		_quinaServiceByAppendConfig(service);
		// 登録済みの番号を検索.
		final int p = search(list, name);
		if(p == -1) {
			// 存在しない場合.
			list.add(new QuinaServiceEntry(
				name, null, service));
			return null;
		}
		// 一番最後に再設定して返却.
		QuinaServiceEntry e = list.remove(p);
		list.add(e);
		return e.setService(service);
	}

	/**
	 * 登録名を指定して取得.
	 * @param name 取得したい登録名を設定します.
	 * @return QuinaService 対象のサービスが返却されます.
	 */
	public QuinaService get(String name) {
		if(name != null) {
			final int p = search(list, name);
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
			final int p = search(list, name);
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
			return list.get(no).getService();
		}
		return null;
	}

	/**
	 * 登録名を指定して削除.
	 * @param name 削除対象の登録名を設定します.
	 * @return QuinaService 削除されたサービスが返却されます.
	 */
	public QuinaService remove(String name) {
		if(fixFlag.get()) {
			throw new QuinaException("Already completed.");
		}
		if(name != null) {
			final int p = search(list, name);
			if(p != -1) {
				return list.remove(p).getService();
			}
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
	 * 1つのQuinaService要素.
	 */
	private static final class QuinaServiceEntry {
		private boolean regFlag;
		private String name;
		private String define;
		private QuinaService service;
		private QuinaServiceEntry next;

		/**
		 * コンストラクタ.
		 * @param name サービス登録名を設定します.
		 * @param define サービス定義名を設定します.
		 * @param service 登録サービスを設定します.
		 */
		protected QuinaServiceEntry(
			String name, String define, QuinaService service) {
			this.name = name;
			this.define = define;
			this.service = service;
			this.next = null;
			// サービス定義名が存在しない場合はサービス登録対象.
			this.regFlag = define == null;
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
		protected QuinaService setService(QuinaService newService) {
			QuinaService ret = service;
			service = newService;
			return ret;
		}
		
		// サービス登録対象のサービスの場合 trueをセット.
		protected void setRegService(boolean flg) {
			regFlag = flg;
		}
		
		// サービス登録対象のサービスの場合 true返却.
		protected boolean isRegService() {
			return regFlag;
		}
		
		// このEntryの下にEntryをセット.
		protected void putNext(QuinaServiceEntry entry) {
			this.next = entry;
		}
		
		// このEntryの下のEntryを取得.
		protected QuinaServiceEntry getNext() {
			return this.next;
		}
	}
}
