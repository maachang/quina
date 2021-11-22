package quina.annotation.resource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import quina.exception.QuinaException;
import quina.util.FileUtil;
import quina.util.StringUtil;
import quina.util.collection.IndexKeyValueList;
import quina.util.collection.ObjectList;

/**
 * Resourceファイルコピー処理.
 */
public class ExecuteCopyResource {
	// 対象クラス.
	private Class<?> clazz;
	// 対象メソッド.
	private IndexKeyValueList<String, Method> methods =
		new IndexKeyValueList<String, Method>();
	
	/**
	 * コンストラクタ.
	 * @param clazz 対象クラスを設定します.
	 * @param list 対象BuildStep実行メソッドを設定します.
	 */
	public ExecuteCopyResource(
		Class<?> clazz, ObjectList<Method> list) {
		this.clazz = clazz;
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			methods.put(
				list.get(i).getName(), list.get(i));
		}
	}
	
	/**
	 * NativeBuildStepメソッド群を実行.
	 * @param javaPackageDir
	 * @param classPackageDir
	 */
	public void execute(String javaPackageDir, String classPackageDir) {
		Method m;
		Object o = null;
		String[] files = null;
		BuildResource rb;
		String src;
		String dest;
		if(!javaPackageDir.endsWith("/")) {
			javaPackageDir += "/";
		}
		if(!classPackageDir.endsWith("/")) {
			classPackageDir += "/";
		}
		try {
			final int len = methods.size();
			for(int i = 0; i < len; i ++) {
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
				rb = m.getAnnotation(BuildResource.class);
				src = rb.src();
				dest = rb.dest();
				if(rb.srcMode().isPackage()) {
					src = getPackageNameToDirectoryName(src);
				}
				if(rb.destMode().isPackage()) {
					dest = getPackageNameToDirectoryName(dest);
				}
				copyFiles(rb, javaPackageDir, classPackageDir,
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
		BuildResource rb, String javaPackageDir, String classPackageDir,
		String src, String dest, String[] files) {
		String srcDir, destDir, srcFile, destFile;
		if(rb.srcMode().isJavaPackage()) {
			srcDir = javaPackageDir + src;
		} else if(rb.srcMode().isClassPackage()) {
			srcDir = classPackageDir + src;
		} else {
			srcDir = src;
		}
		if(!FileUtil.isDir(srcDir)) {
			throw new QuinaException(
				"The copy source directory \"" +
				srcDir + "\" does not exist. ");
		}
		if(!srcDir.endsWith("/")) {
			srcDir += "/";
		}
		if(rb.destMode().isJavaPackage()) {
			destDir = javaPackageDir + dest;
		} else if(rb.destMode().isClassPackage()) {
			destDir = classPackageDir + dest;
		} else {
			destDir = dest;
		}
		if(!destDir.endsWith("/")) {
			destDir += "/";
		}
		if(!FileUtil.isDir(destDir)) {
			try {
				FileUtil.mkdirs(destDir);
			} catch(Exception e) {
				throw new QuinaException(e);
			}
		}
		final int len = files.length;
		for(int i = 0; i < len; i ++) {
			srcFile = srcDir + files[i];
			if(!FileUtil.isFile(srcFile)) {
				throw new QuinaException(
					"The copy source file \"" +
					srcFile + "\" does not exist. ");
			}
		}
		try {
			for(int i = 0; i < len; i ++) {
				srcFile = srcDir + files[i];
				destFile = destDir + files[i];
				FileUtil.copy(srcFile, destFile);
			}
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
}
