package quina.compile;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import quina.annotation.AnnotationUtil;
import quina.compile.cdi.annotation.proxy.AnnotationProxyScopedConstants;
import quina.exception.QuinaException;

/**
 * GenerateCdiユーティリティ.
 */
public class QuinaCTUtil {
	private QuinaCTUtil() {}
	
	/**
	 * 指定annotationが対象クラスに一致するか継承している
	 * アノテーションであるかチェック.
	 * 
	 * <例>
	 * ＠CdiScoped
	 * ＠Target(ElementType.TYPE)
	 * ＠Retention(RetentionPolicy.RUNTIME)
	 * public ＠interface TestesAnnotation {}
	 * 
	 * ＠TestesAnnotation
	 * public class Hoge {}
	 * 
	 * boolean res = isAnnotation(Hoge.class, CdiScoped.class);
	 * System.out.println(res);
	 * 
	 * > true
	 * 
	 * このような場合、TestesAnnotationアノテーションに対して@CdiScopedを継承
	 * しているとみなします.
	 * @param c 対象のクラスを設定します.
	 * @param at 対象のアノテーションクラスを設定します.
	 */
	@SuppressWarnings("rawtypes")
	public static final boolean isAnnotation(Class c, Class at) {
		return _isAnnotation(0, c, at);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final boolean _isAnnotation(int count, Class c, Class at) {
		// 対象クラスに指定アノテーションが含まれてる場合.
		if(c.isAnnotationPresent(at)) {
			return true;
		// 対象アノテーションの１つ下のアノテーション定義を参照する.
		} else if(count >= 1) {
			return false;
		}
		// 対象クラスに定義されてるアノテーション一覧を取得.
		Annotation[] list = c.getAnnotations();
		int len = list == null ? 0 : list.length;
		for(int i = 0; i < len; i ++) {
			// このアノテーションに指定アノテーションが含まれてるか.
			if(_isAnnotation(count + 1, list[i].annotationType(), at)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 指定文字列以下の場合スペースをセットして表示をあわせる.
	 * @param src 表示対象の文字列を設定します.
	 * @param fooder 文字列のフッダーを設定します.
	 * @param max 表示対象の最大文字列を設定します.
	 * @return String 文字列が返却されます.
	 */
	public static final String toSpaceString(
		String src, String fooder, int max) {
		int len = src.length();
		if(len > max) {
			return src + fooder;
		}
		final int spaceLen = max - len;
		final StringBuilder ret = new StringBuilder(src);
		for(int i = 0; i < spaceLen; i ++) {
			ret.append(" ");
		}
		return ret.append(fooder).toString();
	}
	
	/**
	 * 指定Classディレクトリとjarクラスを読み込むクラローダーを作成.
	 * @param classDir クラスディレクトリを設定します.
	 * @param jarFileNames jarファイル名群を設定します.
	 * @return ClassLoader クラスローダーが返却されます.
	 * @throws Exception 例外.
	 */
	public static final ClassLoader createClassLoader(
		String classDir, String... jarFileNames)
		throws Exception {
		if(classDir == null &&
			(jarFileNames == null || jarFileNames.length == 0)) {
			throw new QuinaException(
				"Neither the class directory nor the jar file is set.");
		}
		int offset = 0;
		// Jarファイル名数を取得.
		int len = jarFileNames == null ? 0 : jarFileNames.length;
		// ClassLoaderに取り込むURL配列を生成.
		URL[] urls = new URL[(classDir != null ? 1 : 0) + len];
		// クラスディレクトリが設定されている場合.
		if(classDir != null) {
			urls[0] = new File(classDir + "/").toURI().toURL();
			offset += 1;
		}
		// jarファイル名群をセット.
		for(int i = 0; i < len; i ++) {
			urls[i + offset] = new File(jarFileNames[i]).toURI().toURL();
		}
		return new URLClassLoader(urls,
			Thread.currentThread().getContextClassLoader());
	}
	
	/**
	 * クラスをローディング.
	 * @param name クラス名を設定します.
	 * @param params GenerateCdiのパラメータを設定します.
	 * @return Class<?> クラスオブジェクトが返却されます.
	 * @throws ClassNotFoundException クラス読み込み例外.
	 */
	public static final Class<?> getClass(String name, QuinaCTParams params)
		throws ClassNotFoundException {
		return getClass(name, params.cl);
	}
	
	/**
	 * クラスをローディング.
	 * @param name クラス名を設定します.
	 * @param cl ClassLoaderを設定します.
	 * @return Class<?> クラスオブジェクトが返却されます.
	 * @throws ClassNotFoundException クラス読み込み例外.
	 */
	public static final Class<?> getClass(String name, ClassLoader cl)
		throws ClassNotFoundException {
		return Class.forName(name, true, cl);
	}
	
	/**
	 * クラスからインスタンスを生成.
	 * @param c 対象のクラスを設定します.
	 * @return Object 生成されたオブジェクトが返却されます.
	 * @throws Exception 例外.
	 */
	public static final Object newInstance(Class<?> c)
		throws Exception {
		// クラスのインスタンスを生成.
		final Constructor<?> cons = c.getDeclaredConstructor();
		cons.setAccessible(true);
		return cons.newInstance();
	}
	
	/**
	 * パッケージ名をディレクトリ名に変換.
	 * @param name パッケージ名を設定します.
	 * @return String ディレクトリ名が返却されます.
	 */
	public static final String packageNameToDirectory(String name) {
		char c;
		StringBuilder buf = new StringBuilder();
		int len = name.length();
		for(int i = 0; i < len; i ++) {
			c = name.charAt(i);
			if(c == '.') {
				buf.append('/');
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}
	
	/**
	 * PackageNameを作成.
	 * @param base ベースのディレクトリ名を設定します.
	 * @param target ターゲット名を設定します.
	 * @return String PackageNameが返却されます.
	 */
	public static final String createPackageName(String base, String target) {
		if(base == null || base.isEmpty()) {
			return target;
		}
		return base + "." + target;
	}
	
	/**
	 * パッケージ名＋クラスファイルでClass名を取得.
	 * @param packageName パッケージ名を設定します.
	 * @param fileName 拡張子が .class のファイル名を設定します.
	 * @return String Class名が返却されます.
	 */
	public static final String createClassName(
		String packageName, String fileName) {
		if(fileName.endsWith(".class")) {
			fileName = fileName.substring(0, fileName.length() - 6);
		} else if(fileName.endsWith(".properties")) {
			fileName = fileName.substring(0, fileName.length() - 11);
		}
		if(packageName.isEmpty()) {
			return fileName;
		}
		return packageName + "." + fileName;
	}
	
	/**
	 * パッケージ名を除外したクラス名を取得.
	 * @param className 対象のパッケージ名＋クラス名を設定します.
	 * @return String クラス名が返却されます.
	 */
	public static final String getClassNameByCutPackageName(
		String className) {
		int p = className.lastIndexOf(".");
		if(p == -1) {
			return className;
		}
		return className.substring(p + 1);
	}
	
	/**
	 * ProxyScopedの自動作成されるクラス名を取得.
	 * @param srcClassName ProxyScopedアノテーションが定義されてる
	 *                     パッケージ名＋クラス名を設定します.
	 * @return String 自動作成されるクラス名が返却されます.
	 */
	public static final String getAutoProxyClassName(
		String srcClassName) {
		return new StringBuilder(
			AnnotationProxyScopedConstants.OUTPUT_AUTO_SOURCE_PROXY_PACKAGE_NAME)
			.append(".")
			.append(AnnotationProxyScopedConstants.HEAD_PROXY_CLASS_NAME)
			.append(getClassNameByCutPackageName(srcClassName))
			.toString();
	}
	
	/**
	 * クラス群を取得.
	 * @params 対象のQuinaCompileTool用Paramsを設定します.
	 * @param classDir クラスディレクトリ名を設定します.
	 * @param jarFiles jarファイル名群を設定します.
	 * @return List<String> クラス名一覧を格納するリストを設定します
	 * @throws Exception 例外.
	 */
	public static final List<String> findClassList(
		QuinaCTParams params, String classDir, String[] jarFiles)
		throws Exception {
		// クラス一覧を取得.
		List<String> ret = new ArrayList<String>();
		// クラスディレクトリのクラス一覧を取得.
		if(classDir != null) {
			QuinaCTUtil.findClassDirByClassNames(ret, params, classDir);
		}
		// jarファイル群からクラス一覧を取得.
		if(jarFiles != null && jarFiles.length > 0) {
			int len = jarFiles.length;
			for(int i = 0; i < len; i ++) {
				QuinaCTUtil.findJarByClassNames(ret, params, jarFiles[i]);
			}
		}
		return ret;
	}
	
	/**
	 * Classディレクトリからクラス名一覧を取得.
	 * @param out クラス名一覧を格納するリストを設定します.
	 * @param params 対象のQuinaCompileTool用Paramsを設定します.
	 * @param classDir クラスディレクトリ名を設定します.
	 * @return int 取得したクラス名一覧数が返却されます.
	 */
	public static final int findClassDirByClassNames(
		List<String> out, QuinaCTParams params, String classDir) {
		if(out == null) {
			throw new QuinaException(
				"The list object to store the result is not set.");
		}
		classDir = AnnotationUtil.slashPath(classDir);
		int[] ret = new int[] {0};
		_findClassDirByClassNames(out, params, ret, "", classDir);
		return ret[0];
	}
	
	// Classディレクトリからクラス名一覧を取得.
	private static final void _findClassDirByClassNames(
		List<String> out, QuinaCTParams params, int[] count, String packageName,
		String classDir) {
		String target;
		File f = new File(classDir);
		String[] list = f.list();
		int len = list != null ? list.length : 0;
		for(int i = 0; i < len; i ++) {
			target = classDir + "/" + list[i];
			// ディレクトリの場合.
			if(new File(target).isDirectory()) {
				// 今回のディレクトリで再帰処理.
				_findClassDirByClassNames(
					out, params, count, createPackageName(
						packageName, list[i]), target);
			// クラスファイルの場合.
			} else if(list[i].endsWith(".class")) {
				out.add(createClassName(packageName, list[i]));
				count[0] ++;
			// リソースファイルの場合.
			} else if(list[i].endsWith(".properties") &&
				params.registerResourceItemFlag) {
				params.regResourceList.add(
					createClassName(packageName, list[i]));
			}
		}
	}
	
	// ZipEntry.getName() をクラス名に変換.
	private static final String zipEntryNameByClassName(String fileName) {
		char c;
		int len;
		StringBuilder buf = new StringBuilder();
		if(fileName.endsWith(".class")) {
			// .class を除外た文字列で実行.
			len = fileName.length() - 6;
		} else if(fileName.endsWith(".properties")) {
			// .properties を除外た文字列で実行.
			len = fileName.length() - 11;
		} else {
			len = fileName.length();
		}
		for(int i = 0; i < len; i ++) {
			c = fileName.charAt(i);
			if(c == '/') {
				buf.append('.');
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}
	
	/**
	 * 指定jarファイルからクラス名一覧を取得.
	 * @param out クラス名一覧を格納するリストを設定します.
	 * @param params 対象のQuinaCompileTool用Paramsを設定します.
	 * @param jarFileName jarファイル名を設定します.
	 * @return int 取得したクラス名一覧数が返却されます.
	 * @throws Exception 例外.
	 */
	public static final int findJarByClassNames(
		List<String> out, QuinaCTParams params, String jarFileName)
		throws Exception {
		if(out == null) {
			throw new QuinaException(
				"The list object to store the result is not set.");
		} else if(!jarFileName.toLowerCase().endsWith(".jar")) {
			throw new QuinaException("The specified file \"" +
				jarFileName + "\" is not a jar file.");
		}
		int ret = 0;
		ZipFile zip = null;
		try {
			String name;
			zip = new ZipFile(jarFileName);
			ZipEntry entry;
			Enumeration<?> em = zip.entries();
			while(em.hasMoreElements()) {
				entry = (ZipEntry)em.nextElement();
				name = entry.getName();
				entry = null;
				// META-INF 以下は無視.
				if(name.startsWith("META-INF/")) {
					continue;
				// クラスファイルの場合.
				} else if(name.endsWith(".class")) {
					out.add(zipEntryNameByClassName(name));
					ret ++;
				// リソースファイルの場合.
				} else if(name.endsWith(".properties") &&
					params.registerResourceItemFlag) {
					params.regResourceList.add(zipEntryNameByClassName(name));
				}
			}
			zip.close();
			zip = null;
		} finally {
			if(zip != null) {
				try {
					zip.close();
				} catch(Exception e) {}
			}
		}
		return ret;
	}

	/**
	 * 指定ディレクトリ以下の指定拡張子のファイル名群を取得.
	 * @param out ファイル名群を格納するリストを設定します.
	 * @param dir 対象のディレクトリ名を設定します.
	 * @param extension 拡張子を設定します.
	 * @return int 今回取得したファイル名数が返却されます.
	 */
	public static final int findTargetFiles(
		List<String> out, String dir, String extension) {
		if(out == null) {
			throw new QuinaException(
				"The list object to store the result is not set.");
		} else if(dir == null || dir.isEmpty()) {
			throw new QuinaException("No directory name is specified.");
		} else if(extension == null || extension.isEmpty()) {
			throw new QuinaException("The extension is not specified.");
		} else if(!extension.startsWith(".")) {
			extension = "." + extension;
		}
		dir = AnnotationUtil.slashPath(dir);
		int[] ret = new int[] {0};
		_findTargetFiles(out, ret, dir, extension);
		return ret[0];
	}
	
	// 指定ディレクトリ以下の指定拡張子のファイル名群を取得.
	private static final void _findTargetFiles(
		List<String> out, int[] count, String dir, String extension) {
		String target;
		File f = new File(dir);
		String[] list = f.list();
		int len = list != null ? list.length : 0;
		for(int i = 0; i < len; i ++) {
			target = dir + "/" + list[i];
			// ディレクトリの場合.
			if(new File(target).isDirectory()) {
				// 今回のディレクトリで再帰処理.
				_findTargetFiles(out, count, target, extension);
			// 指定拡張子のファイルの場合.
			} else if(list[i].endsWith(extension)) {
				out.add(target);
				count[0] ++;
			}
		}
	}
	
	/**
	 * 指定ディレクトリ以下のJarファイル名を取得.
	 * @param dirs 対象のディレクトリ名群を設定します
	 * @return String[] 検出されたJarファイル名群が返却されます.
	 */
	public static final String[] findJarFiles(String... dirs) {
		if(dirs == null || dirs.length == 0) {
			return new String[0];
		}
		List<String> list = new ArrayList<String>();
		int len = dirs.length;
		for(int i = 0; i < len; i ++) {
			findTargetFiles(list, dirs[i], ".jar");
		}
		len = list.size();
		String[] ret = new String[len];
		for(int i = 0; i < len; i ++) {
			ret[i] = list.get(i);
		}
		return ret;
	}
	
	/**
	 * 対象のクラスがPublic定義で空のpublicコンストラクタが
	 * 利用可能かチェック.
	 * @param clazzName
	 * @param params
	 * @throws ClassNotFoundException
	 */
	public static final void checkPublicClass(
		String clazzName, QuinaCTParams params)
		throws ClassNotFoundException {
		// 対象のクラスをロード.
		final Class<?> c = QuinaCTUtil.getClass(clazzName, params.cl);
		checkPublicClass(c);
	}
	
	/**
	 * 対象のクラスがPublic定義で空のpublicコンストラクタが
	 * 利用可能かチェック.
	 * @param c
	 */
	public static final void checkPublicClass(Class<?> c) {
		// クラス定義がPublic定義の場合.
		if(Modifier.isPublic(c.getModifiers())) {
			try {
				// 引数の無いコンストラクタが存在して、それが
				// public 定義かチェック.
				Constructor<?> csr = c.getConstructor();
				if(Modifier.isPublic(csr.getModifiers())) {
					// 対象コンストラクタがPublicの場合.
					return;
				}
			} catch(NoSuchMethodException mse) {
			}
		}
		// クラス定義がpublicでなく、空のpublic
		// コンストラクタが存在しない場合.
		throw new QuinaException(
			"An empty Public constructor for the specified " +
			"class \"" + c.getName() +
			"\" is not defined. ");
	}
	
	/**
	 * 対象リフレクションオブジェクトのアクセス修飾子を取得します.
	 * @param o 取得したいClass, Constructor, Method, Fieldのどれかを
	 *          設定します.
	 * @return String 対象のアクセス修飾子が返却されます.
	 */
	public static final String getAccessModifier(Member o) {
		int mod = o.getModifiers();
		if(Modifier.isPublic(mod)) {
			return "public";
		} else if(Modifier.isProtected(mod)) {
			return "protected";
		}
		return "private";
	}
	
	/**
	 * 指定クラスのMethod群を取得.
	 * @param c 対象のクラスを設定します.
	 * @return List<Method> superClassにさかのぼってMethod情報を取得します.
	 */
	public static final List<Method> getMethods(Class<?> c) {
		int len;
		Method[] methods;
		List<Method> ret = new ArrayList<Method>();
		while(c != null) {
			methods = c.getDeclaredMethods();
			len = methods.length;
			for(int i = 0; i < len; i ++) {
				ret.add(methods[i]);
			}
			methods = null;
			c = c.getSuperclass();
		}
		return ret;
	}
	
	/**
	 * 指定クラスのField群を取得.
	 * @param c 対象のクラスを設定します.
	 * @return List<Field> superClassにさかのぼってField情報を取得します.
	 */
	public static List<Field> getFields(Class<?> c) {
		int len;
		Field[] fields;
		List<Field> ret = new ArrayList<Field>();
		while(c != null) {
			fields = c.getDeclaredFields();
			len = fields.length;
			for(int i = 0; i < len; i ++) {
				ret.add(fields[i]);
			}
			fields = null;
			c = c.getSuperclass();
		}
		return ret;
	}
	
	/**
	 * クラス名からメソッド名変換.
	 * @param clazz クラス名を設定します.
	 * @return String メソッド名に変換された名前が返却されます.
	 */
	public static final String convertClassByMethodName(String clazz) {
		char c;
		final StringBuilder buf = new StringBuilder();
		final int len = clazz.length();
		for(int i = 0; i < len; i ++) {
			if((c = clazz.charAt(i)) == '.' || c == '/') {
				buf.append("_");
			} else {
				buf.append(clazz.charAt(i));
			}
		}
		return buf.toString();
	}
	

}