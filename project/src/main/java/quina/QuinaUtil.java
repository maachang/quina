package quina;

import quina.json.Json;
import quina.util.FileUtil;
import quina.util.StringUtil;
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
		String dir = configDir;
		if(!dir.endsWith("/")) {
			dir += "/";
		}
		// 環境変数が定義されている場合は置き換える.
		dir = StringUtil.envPath(configDir);
		// コンフィグファイルが存在するかチェック.
		int len = JSON_CONFIG_EXTENSION.length;
		String fileName = null;
		for(int i = 0; i < len; i ++) {
			if(FileUtil.isFile(name + JSON_CONFIG_EXTENSION[i])) {
				fileName = dir + name + JSON_CONFIG_EXTENSION[i];
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

}
