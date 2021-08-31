package quina.command.shutdown;

import java.util.List;

/**
 * コマンドライン引数管理オブジェクト.
 *
 * たとえば args = ["-a", "hoge"] のような実行引数が設定されていた場合.
 *
 * String[] args = ...;
 * Args argsObject = new Args(args);
 * String value = argsObject.get("-a");
 * value.equals("hoge") == true;
 *
 * のような形で取得が出来ます.
 */
public class Args {
	// Argsオブジェクトの内容.
	private String[] args;

	/**
	 * コンストラクタ.
	 */
	public Args() {
		this(new String[0]);
	}

	/**
	 * コンストラクタ.
	 *
	 * @param args
	 *            mainの引数を設定します.
	 */
	public Args(String... args) {
		this.args = args;
	}

	/**
	 * コンストラクタ.
	 *
	 * @param list 引数群を設定します.
	 */
	public Args(List<String> list) {
		int len = list.size();
		args = new String[len];
		for(int i = 0; i < len; i ++) {
			args[i] = list.get(i);
		}
	}

	/**
	 * このオブジェクトに設定されたコマンド引数を取得.
	 *
	 * @return String[]
	 */
	public String[] getArgs() {
		return args;
	}

	/**
	 * 対象文字が数字かチェック.
	 * @param n
	 * @return
	 */
	protected static final boolean isNumeric(String n) {
		try {
			Double.parseDouble(n);
		} catch(Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 指定ヘッダ名を設定して、要素を取得します.
	 *
	 * @param names 対象のヘッダ名を設定します.
	 * @return String 文字列が返却されます.
	 */
	public String get(String... names) {
		return next(0, names);
	}

	/**
	 * 番号指定での指定ヘッダ名を指定した要素取得処理.
	 *
	 * たとえば
	 * > -i abc -i def -i xyz
	 *
	 * このような情報が定義されてる場合にたとえば
	 * next(0, "-i") なら "abc" が返却され
	 * next(1, "-i") なら "def" が返却されます.
	 *
	 * @param no 取得番目番号を設定します.
	 * @param names 対象のヘッダ名を設定します.
	 * @return String 文字列が返却されます.
	 */
	public String next(int no, String... names) {
		final int len = names.length;
		if(len == 1 && isNumeric(names[0])) {
			final int pos = Integer.parseInt(names[0]);
			if(pos >= 0 && pos < args.length) {
				return args[pos];
			}
			return null;
		}
		int cnt = 0;
		final int lenJ = args.length - 1;
		for(int i = 0; i < len; i ++) {
			for (int j = 0; j < lenJ; j++) {
				if (names[i].equals(args[j])) {
					if(no <= cnt) {
						return args[j + 1];
					}
					cnt ++;
				}
			}
		}
		return null;
	}

	/**
	 * 指定ヘッダ名を指定して、そのヘッダ名が存在するかチェックします.
	 *
	 * @param names
	 * @return boolean
	 */
	public boolean isValue(String... names) {
		final int len = names.length;
		final int lenJ = args.length;
		for(int i = 0; i < len; i ++) {
			if(isNumeric(names[i])) {
				final int no = Integer.parseInt(names[i]);
				if(no >= 0 && no < args.length) {
					return true;
				}
			} else {
				for (int j = 0; j < lenJ; j++) {
					if (names[i].equals(args[j])) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 最初のパラメータを取得.
	 * @return
	 */
	public String getFirst() {
		if(args.length == 0) {
			return "";
		}
		return args[0];
	}

	/**
	 * 一番うしろのパラメータを取得.
	 * @return
	 */
	public String getLast() {
		if(args.length == 0) {
			return "";
		}
		return args[args.length - 1];
	}

	/**
	 * パラメータ数を取得.
	 * @return
	 */
	public int size() {
		return args.length;
	}
}

