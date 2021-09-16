package quina;

import quina.exception.QuinaException;
import quina.json.Json;
import quina.util.Env;
import quina.util.FileUtil;
import quina.util.collection.IndexMap;

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
				"quina.config.path",
				"quina.config",
				"config.dir",
				"config.directory",
				"config.folder",
				"config.path",
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
	 * @param name ファイル名(拡張子なし)を設定します.
	 * @return BinarySearchMap<String, Object> JSON情報が返却されます.
	 */
	@SuppressWarnings("unchecked")
	public static final IndexMap<String, Object> loadJson(
		String configDir, String name) {
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
				fileName = configDir + name +
					JSON_CONFIG_EXTENSION[i];
				break;
			}
		}
		// コンフィグファイルが存在しない.
		if(fileName == null) {
			return null;
		}
		try {
			// JSON解析をして、Map形式のみ処理をする.
			final Object json = Json.decode(true,
				FileUtil.getFileString(fileName, "UTF8"));
			if(!(json instanceof IndexMap)) {
				return null;
			}
			return (IndexMap<String, Object>)json;
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}

	/**
	 * その他リソース情報をロード.
	 * @param configDir コンフィグディレクトリ名を設定します.
	 * @param name ファイル名を設定します.
	 * @return byte[] リソースバイナリがロードされます.
	 */
	public static final byte[] loadResourse(String configDir, String name) {
		configDir = getDir(configDir);
		if(!configDir.endsWith("/")) {
			configDir += "/";
		}
		// 環境変数が定義されている場合は置き換える.
		configDir = Env.path(configDir);
		String fileName = configDir + name;
		if(!FileUtil.isFile(fileName)) {
			return null;
		}
		try {
			return FileUtil.getFile(fileName);
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
