package quina.annotation.resource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import quina.exception.QuinaException;
import quina.util.FileUtil;
import quina.util.StringUtil;
import quina.util.collection.IndexKeyValueList;
import quina.util.collection.ObjectList;

/**
 * Resourceファイルコピー処理.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ExecuteCopyResource {
	// 対象クラス.
	private Class clazz;
	// 対象メソッド.
	private IndexKeyValueList<String, Method> methods =
		new IndexKeyValueList<String, Method>();
	
	/**
	 * コンストラクタ.
	 * @param clazz 対象クラスを設定します.
	 * @param list 対象BuildStep実行メソッドを設定します.
	 */
	public ExecuteCopyResource(
		Class clazz, ObjectList<Method> list) {
		this.clazz = clazz;
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			methods.put(
				list.get(i).getName(), list.get(i));
		}
	}
	
	/**
	 * NativeBuildStepメソッド群を実行.
	 * @param out 処理結果のコピー元、先の内容が返却されます.
	 *            0: srcMode, 1: srcFile, 2: destMode, 3: destFile, ...
	 *            の順で設定されます.
	 * @param javaSourcesDir Javaソースディレクトリを設定します.
	 * @param classDir クラス出力先ディレクトリを設定します.
	 */
	public void execute(List<String> out, String javaSourceDir, String classDir) {
		Method m;
		Object o = null;
		String[] files = null;
		BuildResource rb;
		String src;
		String dest;
		if(!javaSourceDir.endsWith("/")) {
			javaSourceDir += "/";
		}
		if(!classDir.endsWith("/")) {
			classDir += "/";
		}
		try {
			final int len = methods.size();
			for(int i = 0; i < len; i ++) {
				// メソッドを実行して転送対象のファイル群を取得.
				m = methods.valueAt(i);
				m.setAccessible(true);
				if(Modifier.isStatic(m.getModifiers())) {
					files = (String[])m.invoke(null);
				} else {
					if(o == null) {
						o = clazz.getDeclaredConstructor()
							.newInstance();
					}
					files = (String[])m.invoke(o);
				}
				// コピー元とコピー先を取得・整頓.
				rb = m.getAnnotation(BuildResource.class);
				src = rb.src();
				dest = rb.dest();
				// srcがパッケージ系.
				if(rb.srcMode().isPackage()) {
					// パッケー名のディレクトリ名変換.
					src = getPackageNameToDirectoryName(src);
				}
				// destがパッケージ系.
				if(rb.destMode().isPackage()) {
					// パッケー名のディレクトリ名変換.
					dest = getPackageNameToDirectoryName(dest);
				}
				// コピー処理.
				copyFiles(out, rb, javaSourceDir, classDir,
					src, dest, files);
			}
		} catch(InvocationTargetException ite) {
			throw new QuinaException(ite.getCause());
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
	
	// パッケージ名をディレクトリ名に変換.
	private static final String getPackageNameToDirectoryName(
		String packageName) {
		return StringUtil.changeString(packageName, ".", "/");
	}
	
	// ファイル群のコピー.
	private static final void copyFiles(
		List<String> out, BuildResource rb, String javaSourceDir,
		String classDir, String src, String dest, String[] files) {
		String srcDir, destDir, srcFile, destFile;
		// srcがJavaソースパッケージの場合.
		if(rb.srcMode().isJavaPackage()) {
			srcDir = javaSourceDir + src;
		// srcがClassソースパッケージの場合.
		} else if(rb.srcMode().isClassPackage()) {
			srcDir = classDir + src;
		// srcがファイルディレクトリの場合.
		} else {
			srcDir = src;
		}
		// srcディレクトリが見つからない場合.
		if(!FileUtil.isDir(srcDir)) {
			throw new QuinaException(
				"The copy source directory \"" +
				srcDir + "\" does not exist. ");
		}
		// srcディレクトリの終端をセット.
		if(!srcDir.endsWith("/")) {
			srcDir += "/";
		}
		// destディレクトリがJavaパッケージの場合.
		if(rb.destMode().isJavaPackage()) {
			destDir = javaSourceDir + dest;
		// destディレクトリがClassパッケージの場合.
		} else if(rb.destMode().isClassPackage()) {
			destDir = classDir + dest;
		// destディレクトリがファイルディレクトリの場合.
		} else {
			destDir = dest;
		}
		// destディレクトリの終端をセット.
		if(!destDir.endsWith("/")) {
			destDir += "/";
		}
		// destディレクトリが存在しない場合
		// 作成する.
		if(!FileUtil.isDir(destDir)) {
			try {
				FileUtil.mkdirs(destDir);
			} catch(Exception e) {
				throw new QuinaException(e);
			}
		}
		// コピーするファイルの存在確認を行う.
		final int len = files.length;
		for(int i = 0; i < len; i ++) {
			srcFile = srcDir + files[i];
			if(!FileUtil.isFile(srcFile)) {
				throw new QuinaException(
					"The copy source file \"" +
					srcFile + "\" does not exist. ");
			}
		}
		// リソースファイルのコピー処理.
		try {
			for(int i = 0; i < len; i ++) {
				srcFile = srcDir + files[i];
				destFile = destDir + files[i];
				// コピー元と先を保存.
				if(out != null) {
					out.add(rb.srcMode().toString());
					out.add(srcFile);
					out.add(rb.destMode().toString());
					out.add(destFile);
				}
				FileUtil.copy(srcFile, destFile);
			}
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
}
