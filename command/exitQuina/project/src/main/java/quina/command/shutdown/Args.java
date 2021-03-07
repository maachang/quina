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
	 * @param name
	 * @return
	 */
	public String get(String... names) {
		final int len = names.length;
		final int lenJ = args.length - 1;
		for(int i = 0; i < len; i ++) {
			if(isNumeric(names[i])) {
				final int no = Integer.parseInt(names[i]);
				if(no >= 0 && no < args.length) {
					return args[no];
				}
			} else {
				for (int j = 0; j < lenJ; j++) {
					if (names[i].equals(args[j])) {
						return args[j + 1];
					}
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

