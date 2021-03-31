package quina;

import quina.json.Json;
import quina.util.Env;
import quina.util.FileUtil;
import quina.util.collection.BinarySearchMap;

/**
 * Quina関連のユーティリティ.
 */
public final class QuinaUtil {
	private QuinaUtil() {}

	// JSONコンフィグの拡張子.
	private static final String[] JSON_CONFIG_EXTENSION = new String[] {
		".json", ".JSON", ".conf", ".CONF"
	};

	/**
	 * スリープ処理.
	 * @param time スリープ時間をミリ秒で設定します.
	 */
	public static final void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch(Exception e) {}
	}

	/**
	 * スペースをセット.
	 * @param buf StringBuilderを設定します.
	 * @param space スペースの数を設定します.
	 */
	public static final void setSpace(StringBuilder buf, int space) {
		for(int i = 0; i < space; i ++) {
			buf.append(" ");
		}
	}

	/**
	 * コンフィグディレクトリ名を取得.
	 * @return String コンフィグディレクトリ名が返却されます.
	 */
	public static final String getConfigDirectory() {
		try {
			String ret;
			String[] check;
			// システムプロパティから取得.
			check = new String[] {
				"quina.config.dir",
				"quina.config.directory",
				"quina.config.folder",
				"quina.config",
				"config.dir",
				"config.directory",
				"config.folder",
				"config"
			};
			for(int i = 0; i < check.length; i ++) {
				ret = System.getProperty(check[i]);
				if(ret != null) {
					return FileUtil.getFullPath(ret);
				}
			}
			// 環境変数から取得.
			ret = System.getenv("QUINA_CONFIG");
			if(ret != null) {
				return FileUtil.getFullPath(ret);
			}
			// ディレクトリが存在する場合.
			check = new String[] {
				"./conf/",
				"./config/"
			};
			for(int i = 0; i < check.length; i ++) {
				if(FileUtil.isDir(check[i])) {
					return FileUtil.getFullPath(check[i]);
				}
			}
			return null;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}

	/**
	 * json情報をロード.
	 * @param configDir コンフィグディレクトリ名を設定します.
	 * @param target QuinaInfoオブジェクトを設定します.
	 * @return BinarySearchMap<String, Object> JSON情報が返却されます.
	 */
	public static final BinarySearchMap<String, Object> loadConfig(String configDir, QuinaInfo target) {
		// 最初は[infoオブジェクト]のパッケージ名を含めたクラス名での
		// コンフィグファイル定義を検索.
		Class<?> c = target.getClass();
		String fileName = c.getName();
		BinarySearchMap<String, Object> ret = loadJson(configDir, fileName);
		// infoのパッケージ名＋クラス名では、コンフィグファイルは存在しない場合.
		if(ret == null) {
			// つぎに[infoオブジェクト」のクラス名だけ(先頭文字を小文字変換)
			// でのコンフィグファイル定義を検索.
			fileName = c.getSimpleName();
			fileName = fileName.substring(0, 1).toLowerCase()
				+ fileName.substring(1);
			// json情報を取得.
			ret = loadJson(configDir, fileName);
			if(ret == null) {
				return null;
			}
		}
		return ret;
	}

	/**
	 * json情報をロード.
	 * @param configDir コンフィグディレクトリ名を設定します.
	 * @param name ファイル名(拡張子なし)を設定します.
	 * @return BinarySearchMap<String, Object> JSON情報が返却されます.
	 */
	@SuppressWarnings("unchecked")
	public static final BinarySearchMap<String, Object> loadJson(String configDir, String name) {
		configDir = getDir(configDir);
		if(!configDir.endsWith("/")) {
			configDir += "/";
		}
		// 環境変数が定義されている場合は置き換える.
		configDir = Env.path(configDir);
		// コンフィグファイルが存在するかチェック.
		int len = JSON_CONFIG_EXTENSION.length;
		String fileName = null;
		for(int i = 0; i < len; i ++) {
			if(FileUtil.isFile(name + JSON_CONFIG_EXTENSION[i])) {
				fileName = configDir + name + JSON_CONFIG_EXTENSION[i];
				break;
			}
		}
		// コンフィグファイルが存在しない.
		if(fileName == null) {
			return null;
		}
		try {
			// JSON解析をして、Map形式のみ処理をする.
			final Object json = Json.decode(true, FileUtil.getFileString(fileName, "UTF8"));
			if(!(json instanceof BinarySearchMap)) {
				return null;
			}
			return (BinarySearchMap<String, Object>)json;
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}

	// ディレクトリ名を取得.
	private static final String getDir(String dir) {
		// ディレクトリ名が存在していない場合.
		if(dir == null || dir.isEmpty()) {
			try {
				// 該当するコンフィグディレクトリを探す.
				dir = QuinaUtil.getConfigDirectory();
			} catch(Exception e) {
			}
			// 取得できなかった場合はカレントディレクトリを対象とする.
			if(dir == null) {
				try {
					dir = FileUtil.getFullPath("./");
				} catch(Exception e) {}
			}
		}
		return dir;
	}
}
