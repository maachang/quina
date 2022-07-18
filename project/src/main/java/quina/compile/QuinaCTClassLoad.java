package quina.compile;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import quina.annotation.AnnotationUtil;
import quina.exception.QuinaException;
import quina.util.collection.BinarySet;

/**
 * 対象プロジェクトのクラスロード.
 */
public class QuinaCTClassLoad {
	
	/**
	 * (abstract)ClassPathハンドラー処理.
	 */
	public static abstract class AbstractClassPathHandler {
		/**
		 * 拡張子群.
		 */
		protected BinarySet<String> extensions;
		
		/**
		 * CompileToolパラメータ.
		 */
		protected QuinaCTParams params;
		
		/**
		 * コンストラクタ.
		 * @param params CompileToolパラメータを設定します.
		 */
		public AbstractClassPathHandler(QuinaCTParams params) {
			this.init();
			this.params = params;
		}
		
		// 初期化処理.
		private void init() {
			final int len;
			final String[] extensionList = targetExtension();
			if(extensionList == null ||
				(len = extensionList.length) == 0) {
				throw new QuinaException(
					"The target extension has not been set.");
			}
			BinarySet<String> list = new BinarySet<String>(len);
			for(int i = 0; i < len; i ++) {
				list.add(extensionList[i], i);
			}
			list.fix();
			this.extensions = list;
		}
		
		/**
		 * 対象名の拡張子が処理対象かチェック.
		 * @param name 対象名を設定します.
		 * @return boolean trueの場合、処理対象の拡張子です.
		 */
		protected boolean isExtension(String name) {
			int p = name.lastIndexOf(".");
			if(p == -1) {
				return false;
			}
			return extensions.search(
				name.substring(p + 1)) != -1;
		}
		
		/**
		 * 対象拡張子のクラス名の登録処理.
		 * QuinaCTClassLoadでは、こちらを呼び出すます
		 *   (addClassNameはここから実行する).
		 * @param params QuinaCTParams QuinaCTParamsが設定されます.
		 * @param className 対象のクラス名を設定します.
		 */
		protected void addClassName(
			QuinaCTParams params,
			String className) {
			int p = className.lastIndexOf(".");
			if(p == -1) {
				return;
			}
			// クラス名と拡張子を分離して
			// addClassNameを実行.
			addClassName(params,
				className.substring(0, p),
				className.substring(p + 1));
		}
		
		/**
		 * CompileToolパラメータを返却.
		 * @return QuinaCTParams CompileToolパラメータが返却されます.
		 */
		protected QuinaCTParams getParams() {
			return params;
		}
		
		/**
		 * ターゲットとなる拡張子群を取得.
		 * @return String ターゲットとなる拡張子群が返却されます.
		 */
		public abstract String[] targetExtension();
		
		/**
		 * 対象拡張子のクラス名の登録処理.
		 * @param params QuinaCTParams QuinaCTParamsが設定されます.
		 * @param className 対象のクラス名が設定されます.
		 * @param extension 対象の拡張子が設定されます.
		 */
		public abstract void addClassName(
			QuinaCTParams params,
			String className, String extension);
	}
	
	/**
	 * クラス群を取得.
	 * @param handle 処理ハンドルを設定します.
	 * @param params 対象のQuinaCompileTool用Paramsを設定します.
	 * @param classDir クラスディレクトリ名を設定します.
	 * @param jarFiles jarファイル名群を設定します.
	 * @throws Exception 例外.
	 */
	public static final void findClassList(AbstractClassPathHandler handle,
		QuinaCTParams params, String classDir, String[] jarFiles)
		throws Exception {
		// クラスディレクトリのクラス一覧を取得.
		if(classDir != null) {
			findClassDirByClassNames(
				handle, params, classDir);
		}
		// jarファイル群からクラス一覧を取得.
		if(jarFiles != null && jarFiles.length > 0) {
			int len = jarFiles.length;
			for(int i = 0; i < len; i ++) {
				findJarByClassNames(
					handle, params, jarFiles[i]);
			}
		}
	}
	
	/**
	 * Classディレクトリからクラス名一覧を取得.
	 * @param handle 処理ハンドルを設定します.
	 * @param params 対象のQuinaCompileTool用Paramsを設定します.
	 * @param classDir クラスディレクトリ名を設定します.
	 * @return int 取得したクラス名一覧数が返却されます.
	 */
	private static final int findClassDirByClassNames(
		AbstractClassPathHandler handle,QuinaCTParams params,
		String classDir) {
		classDir = AnnotationUtil.slashPath(classDir);
		int[] ret = new int[] {0};
		_findClassDirByClassNames(handle, params, ret, "", classDir);
		return ret[0];
	}
	
	// Classディレクトリからクラス名一覧を取得.
	private static final void _findClassDirByClassNames(
		AbstractClassPathHandler handle, QuinaCTParams params,
		int[] count, String packageName,
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
					handle, params, count,
					createPackageFileName(
						packageName, list[i]), target);
			}
			// 処理対象の拡張子の場合.
			if(handle.isExtension(list[i])) {
				// 条件追加.
				handle.addClassName(
					params,
					createPackageFileName(
						packageName, list[i]));
			}
		}
	}
	
	/**
	 * PackageName + fileNameを作成.
	 * @param base ベースのディレクトリ名を設定します.
	 * @param target ターゲット名を設定します.
	 * @return String PackageNameが返却されます.
	 */
	private static final String createPackageFileName(
		String base, String target) {
		if(base == null || base.isEmpty()) {
			return target;
		}
		return base + "." + target;
	}
	
	/**
	 * 指定jarファイルからクラス名一覧を取得.
	 * @param handle 処理ハンドルを設定します.
	 * @param params 対象のQuinaCompileTool用Paramsを設定します.
	 * @param jarFileName jarファイル名を設定します.
	 * @return int 取得したクラス名一覧数が返却されます.
	 * @throws Exception 例外.
	 */
	private static final int findJarByClassNames(
		AbstractClassPathHandler handle, QuinaCTParams params,
		String jarFileName)
		throws Exception {
		if(!jarFileName.toLowerCase().endsWith(".jar")) {
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
				// 処理対象の拡張子の場合.
				} else if(handle.isExtension(name)) {
					// 条件追加.
					handle.addClassName(
						params,
						zipEntryNameByClassPath(name));
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
	 * ZipEntry.getName() をクラスパス形式に変換.
	 *  <例> hoge/moge/Abc.class を hoge.moge.Abc.class に置き換える.
	 * @param fileName ZipEntry.getName()を設定します.
	 * @return String クラスパス変換された内容が返却されます.
	 */
	private static final String zipEntryNameByClassPath(
		String fileName) {
		char c;
		int len = fileName.length();
		StringBuilder buf = new StringBuilder();
		// slash(/)をdot(.)に変換.
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
	 * 指定ディレクトリ以下の指定拡張子のファイル名群を取得.
	 * @param out ファイル名群を格納するリストを設定します.
	 * @param dir 対象のディレクトリ名を設定します.
	 * @param extension 取得対象の拡張子を設定します.
	 * @return int 今回取得したファイル名数が返却されます.
	 */
	private static final int findTargetFiles(
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
}
