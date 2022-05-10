package quina.compile.cdi;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;

import org.graalvm.polyglot.proxy.Proxy;
import org.graalvm.polyglot.proxy.ProxyArray;

import quina.exception.QuinaException;
import quina.smple.SmpleAnalysis;
import quina.util.FileUtil;
import quina.util.ResourceUtil;

/**
 * [JS]smpleテンプレート実行.
 */
public class CdiOutputJsSmpleOut {
	// [JS]SMPLEのリソース場所.
	protected static final String SMPLE_RESOURCE_PACKAGE =
		"quina/resources/compile/";
	
	// [JS]SMPLEの拡張子.
	protected static final String SMPLE_JS_EXTENSION = ".smj";
	
	/**
	 * [JS]smpleJSを実行してJavaFileを出力.
	 * @param outDir 出力先ディレクトリ名を設定します.
	 * @param smpleName 対象のSmpleテンプレートファイル名を設定します.
	 * @param jsParam JsのArrayオブジェクトを設定します.
	 * @param outJavaName 出力先Javaファイル名を設定します.
	 *                    空かnullの場合はsmpleNameが対象となります.
	 * @return boolean trueの場合、出力に成功しました.
	 */
	public static final boolean executeSmpleToOutputJavaFile(
		String outDir, String smpleName, ProxyArray jsParam,
		String outJavaName) {
		// 出力条件が存在する場合のみ出力.
		if(jsParam.getSize() > 0L) {
			// outJavaNameが存在しない場合.
			if(outJavaName == null || outJavaName.isEmpty()) {
				// smpleNameを対象とする.
				outJavaName = smpleName;
			}
			// 出力先を生成.
			final String outFileName = outDir + "/" + outJavaName;
			
			// smpleコンパイルファイルを読み込む.
			String smple = getSmpleJs(smpleName, outJavaName);
			
			// smpleを実行する.
			smple = resultSmple(smple, jsParam);
			
			// ソース出力先ディレクトリを作成.
			mkdirs(outDir);
			
			// 処理結果を出力.
			outputSmple(outFileName, smple);
			return true;
		}
		// 出力条件が存在しない.
		return false;
	}
	
	// [JS]Java出力[js]Smple名を設定してSmpleJSスクリプト取得.
	private static final String getSmpleJs(String name, String outJavaName) {
		String smpleName = SMPLE_RESOURCE_PACKAGE +
			name + SMPLE_JS_EXTENSION;
		String smple = null;
		if(name.equals(outJavaName)) {
			System.out.println("> " + smpleName);
		} else {
			System.out.println("> " + smpleName + ": " + outJavaName);
		}
		try {
			smple = ResourceUtil.getString(smpleName);
			return SmpleAnalysis.compileJs(smple);
		} catch(QuinaException qe) {
			errorSmpleJs(smple);
			throw qe;
		} catch(Exception e) {
			errorSmpleJs(smple);
			throw new QuinaException(e);
		}
	}
	
	// [JS]SmpleJSを実行して処理結果を取得.
	private static final String resultSmple(String smple, Proxy proxy) {
		try {
			return SmpleAnalysis.executeJsSmple(smple, proxy);
		} catch(QuinaException qe) {
			errorSmpleJs(smple);
			throw qe;
		} catch(Exception e) {
			errorSmpleJs(smple);
			throw new QuinaException(e);
		}
	}
	
	// [JS]smpleError.
	private static final void errorSmpleJs(String smple) {
		if(smple == null) {
			return;
		}
		BufferedReader r = null;
		try {
			System.out.println("> error script: ");
			String s;
			int line = 1;
			r = new BufferedReader(new StringReader(smple));
			while((s = r.readLine()) != null) {
				System.out.println(printLine(line) + ":  " + s);
				line ++;
			}
		} catch(Exception e) {
			throw new QuinaException(e);
		} finally {
			try {
				r.close();
			} catch(Exception e) {}
		}
	}
	
	// 出力先のディレクトリ作成.
	private static final void mkdirs(String name) {
		try {
			//FileUtil.mkdirs(name);
			new File(name).mkdirs();
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
	
	
	// ライン番号を出力.
	private static final String printLine(int no) {
		return "00000".substring(String.valueOf(no).length()) + no;
	}
	
	// smpleコンパイル結果をファイル出力.
	private static final void outputSmple(String outFileName, String smple) {
		try {
			FileUtil.setFileString(true, outFileName, smple, "UTF8");
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
}
