package quina;

import java.lang.reflect.Method;
import java.sql.Date;

import quina.json.Json;
import quina.util.FileUtil;
import quina.util.collection.BinarySearchMap;
import quina.util.collection.IndexMap;
import quina.util.collection.ObjectList;

/**
 * Quina関連のユーティリティ.
 */
public final class QuinaUtil {
	private QuinaUtil() {}

	// JSONコンフィグの拡張子.
	private static final String[] JSON_CONFIG_EXTENSION = new String[] {
		".json", ".JSON"
	};

	// 対象オブジェクトに対する、methodのhead + メソッド名のものを取得.
	private static final IndexMap<String, ObjectList<Method>> getMethodList(
		String head, Class<?> clazz) {
		String key;
		Method m;
		ObjectList<Method> mlst;
		IndexMap<String, ObjectList<Method>> ret =
			new IndexMap<String, ObjectList<Method>>();
		Method[] lst = clazz.getMethods();
		int len = lst.length;
		for(int i = 0; i < len; i ++) {
			m = lst[i];
			key = m.getName();
			if(!head.equals(key) && key.startsWith(head)) {
				if((mlst = ret.get(key)) == null) {
					mlst = new ObjectList<Method>();
					ret.put(key, mlst);
				}
				mlst.add(m);
			}
		}
		return ret;
	}

	// コンフィグ情報として設定可能なパラメータかを判別して取得.
	private static final int getMethodParamsType(Class<?>[] params) {
		if(params.length != 1) {
			return -1;
		}
		Class<?> c = params[0];
		if(String.class.equals(c)) {
			return 0;
		} else if(Date.class.equals(c)) {
			return 1;
		} else if(Boolean.class.equals(c)) {
			return 2;
		} else if(Byte.class.equals(c)) {
			return 3;
		} else if(Short.class.equals(c)) {
			return 4;
		} else if(Integer.class.equals(c)) {
			return 5;
		} else if(Long.class.equals(c)) {
			return 6;
		} else if(Float.class.equals(c)) {
			return 7;
		} else if(Double.class.equals(c)) {
			return 8;
		} else if(Boolean.TYPE.equals(c)) {
			return 12;
		} else if(Byte.TYPE.equals(c)) {
			return 13;
		} else if(Short.TYPE.equals(c)) {
			return 14;
		} else if(Integer.TYPE.equals(c)) {
			return 15;
		} else if(Long.TYPE.equals(c)) {
			return 16;
		} else if(Float.TYPE.equals(c)) {
			return 17;
		} else if(Double.TYPE.equals(c)) {
			return 18;
		}
		return -1;
	}

	// メソッドにデータをセット.
	private static final boolean setMethodParams(
		BinarySearchMap<String, Object> config, String key, Object object,
		Method method, int paramsType) {
		if(paramsType == -1) {
			return false;
		}
		// 対象パラメータタイプがプリミティブ型かチェック.
		boolean primitiveType = false;
		if(paramsType > 10) {
			primitiveType = true;
			paramsType -= 10;
		}
		try {
			switch(paramsType) {
			case 0: // String.
				method.invoke(object, config.getString(key));
				return true;
			case 1: // Date.
				method.invoke(object, config.getDate(key));
				return true;
			case 2: // Boolean.
				if(primitiveType) {
					method.invoke(object, config.getBoolean(key).booleanValue());
				} else {
					method.invoke(object, config.getBoolean(key));
				}
				return true;
			case 3: // Byte.
				if(primitiveType) {
					method.invoke(object, config.getByte(key).byteValue());
				} else {
					method.invoke(object, config.getByte(key));
				}
				return true;
			case 4: // Short.
				if(primitiveType) {
					method.invoke(object, config.getShort(key).shortValue());
				} else {
					method.invoke(object, config.getShort(key));
				}
				return true;
			case 5: // Integer.
				if(primitiveType) {
					method.invoke(object, config.getInteger(key).intValue());
				} else {
					method.invoke(object, config.getInteger(key));
				}
				return true;
			case 6: // Long.
				if(primitiveType) {
					method.invoke(object, config.getLong(key).longValue());
				} else {
					method.invoke(object, config.getLong(key));
				}
				return true;
			case 7: // Float.
				if(primitiveType) {
					method.invoke(object, config.getFloat(key).floatValue());
				} else {
					method.invoke(object, config.getFloat(key));
				}
				return true;
			case 8: // Double.
				if(primitiveType) {
					method.invoke(object, config.getDouble(key).doubleValue());
				} else {
					method.invoke(object, config.getDouble(key));
				}
				return true;
			}
		} catch(Exception e) {}
		return false;
	}

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
	 * @param name ファイル名を設定します.
	 * @return BinarySearchMap<String, Object> JSON情報が返却されます.
	 */
	@SuppressWarnings("unchecked")
	public static final BinarySearchMap<String, Object> loadJson(String configDir, String name) {
		String dir = configDir;
		if(!dir.endsWith("/")) {
			dir += "/";
		}
		// コンフィグファイルが存在するかチェック.
		int len = JSON_CONFIG_EXTENSION.length;
		boolean flg = false;
		for(int i = 0; i < len; i ++) {
			if(FileUtil.isFile(name + JSON_CONFIG_EXTENSION[i])) {
				name = dir + name + JSON_CONFIG_EXTENSION[i];
				flg = true;
				break;
			}
		}
		// コンフィグファイルが存在しない.
		if(!flg) {
			return null;
		}
		try {
			// JSON解析をして、Map形式のみ処理をする.
			final Object json = Json.decode(FileUtil.getFileString(name, "UTF8"));
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

	/**
	 * コンフィグ情報をQuinaInfoオブジェクトに設定.
	 * @param configDir コンフィグディレクトリ名を設定します.
	 * @param info 設定するQuinaInfoオブジェクトを設定します.
	 */
	public static void readConfig(String configDir, QuinaInfo info) {

		/**
		 * 最初[登録されているQuinaInfo]の情報を元にファイル名として、
		 * パッケージ名＋クラス名[xxx.yyy.zzz.Hoge.json]のような
		 * コンフィグファイル名を持って、コンフィグファイルとして検索し
		 * それが無い場合は[hoge.json]でコンフィグファイルを検索して
		 * コンフィグ情報を読み込みます.
		 */

		// 最初にパッケージ名＋クラス名からコンフィグファイル名を生成.
		String fileName;
		BinarySearchMap<String, Object> config;
		final Class<?> c = info.getClass();
		// 最初は[infoオブジェクト]のパッケージ名を含めたクラス名での
		// コンフィグファイル定義を検索.
		fileName = c.getName();
		config = loadJson(configDir, fileName);
		// infoのパッケージ名＋クラス名では、コンフィグファイルは存在しない場合.
		if(config == null) {
			// つぎに[infoオブジェクト」のクラス名だけ(先頭文字を小文字変換)
			// でのコンフィグファイル定義を検索.
			fileName = c.getSimpleName();
			fileName = fileName.substring(0, 1).toLowerCase()
				+ fileName.substring(1);
			// json情報を取得.
			config = loadJson(configDir, fileName);
			if(config == null) {
				// 存在しない場合は処理しない.
				return;
			}
		}
		try {
			// 対象オブジェクトのset系のメソッド群を取得.
			IndexMap<String, ObjectList<Method>> setMethods = getMethodList("set", c);

			// コンフィグ情報を対象オブジェクトに割り当てる.
			Method m;
			int paramsType, lenJ;
			String key, methodName;
			ObjectList<Method> mlst;
			int len = config.size();
			for(int i = 0; i < len; i ++) {
				// コンフィグキー名から、setメソッドを生成.
				key = config.keyAt(i);
				methodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
				// setメソッド名から、メソッド群を取得.
				mlst = setMethods.get(methodName);
				if(mlst != null) {
					// 複数のメソッドで処理.
					lenJ = mlst.size();
					for(int j = 0; j < lenJ; j ++) {
						m = mlst.get(j);
						// setメソッドの第一引数で利用可能なタイプを取得.
						paramsType = getMethodParamsType(m.getParameterTypes());
						// タイプに対してのセット処理を実行.
						if(setMethodParams(config, key, info, m, paramsType)) {
							// 1件でも設定が出来た場合は、セット処理官僚.
							break;
						}
					}
				}
			}
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}

	/**
	 * 指定QuinaInfoオブジェクトの内容を文字出力.
	 * @param out 出力先のStringBuilderオブジェクトを設定します.
	 * @param space 行出力時のインデント値を設定します.
	 * @param info 対象のQuinaInfoオブジェクトを設定します.
	 */
	public static final void toString(StringBuilder out, int space, QuinaInfo info) {
		Object o;
		Method m;
		int lenJ;
		boolean f;
		String key, methodName;
		ObjectList<Method> mlst;
		Class<?> c = info.getClass();
		// 対象オブジェクトのget系のメソッド群を取得.
		IndexMap<String, ObjectList<Method>> getMethods =
			getMethodList("get", c);
		int len = getMethods.size();
		f = false;
		for(int i = 0; i < len; i ++) {
			methodName = getMethods.keyAt(i);
			// Object標準クラスは対象外.
			if("getClass".equals(methodName)) {
				continue;
			}
			key = methodName.substring(3, 4).toLowerCase()
				+ methodName.substring(4);
			mlst = getMethods.get(methodName);
			if(mlst != null) {
				// 複数のメソッドで処理.
				lenJ = mlst.size();
				for(int j = 0; j < lenJ; j ++) {
					// getXXXX() の形式の内容を呼び出して、情報出力.
					if((m = mlst.get(j)).getParameterTypes() == null ||
						m.getParameterTypes().length == 0) {
						try {
							// メソッド実行.
							o = m.invoke(info);
							if(f) {
								out.append("\n");
							}
							f = true;
							// スペースセット.
							setSpace(out, space);
							out.append(key).append(": ");
							// 情報の型に合わせた出力.
							if(o == null) {
								out.append("null");
							} else if(o instanceof String) {
								out.append("\"").append(o).append("\"");
							} else if(o instanceof Boolean) {
								out.append(o);
							} else if(o instanceof Byte) {
								out.append(o);
							} else if(o instanceof Number) {
								out.append(o);
							} else if(o instanceof Date) {
								out.append(Json.dateToString((Date)o));
							}
						} catch(Exception e) {}
					}
				}
			}
		}
	}

}
