package quina.annotation.resource;

/**
 * Resourceファイルのコピーモード.
 * 
 * たとえば対象のディレクトリ内のファイルをJavaパッケージに
 * リソースファイルとして、コピーしたい場合は以下のように
 * 実装します.
 * 
 * ＠BuildResponse(src="./console",
 *   srcMode=ResourceMode.Directory,
 *   dest="quina.jdbc.console.resource",
 *   destMode=ResourceMode.JavaPackage)
 * public String[] copyConsoleByDirectoryToJavaPackage() {
 *   return new String[] {
 *     "login.html",
 *     "console.html",
 *     "base.js",
 *     "console.js",
 *     "console.css"
 *   };
 * }
 * 
 * これにより "./console" ディレクトリから、
 * "quina.jdbc.console.resource"のJavaソースの
 * パッケージに対して、ファイルをコピー実行します.
 * 
 */
public enum ResourceMode {
	/**
	 * ディレクトリ.
	 */
	Directory("directory", false, false, false),
	/**
	 * Javaパッケージ.
	 */
	JavaPackage("java", true, true, false),
	/**
	 * クラスパッケージ.
	 */
	ClassPackage("class", true, false, true)
	;
	
	private String name;
	private boolean packageMode;
	private boolean javaPackageFlag;
	private boolean classPackageFlag;
	
	/**
	 * コンストラクタ.
	 * @param name
	 * @param packageMode
	 * @param javaPackageFlag
	 * @param classPackageFlag
	 */
	private ResourceMode(
		String name, boolean packageMode, boolean javaPackageFlag,
		boolean classPackageFlag) {
		this.name = name;
		this.packageMode = packageMode;
		this.javaPackageFlag = javaPackageFlag;
		this.classPackageFlag = classPackageFlag;
	}
	
	/**
	 * 名前を取得.
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * パッケージモードの場合.
	 * @return
	 */
	public boolean isPackage() {
		return packageMode;
	}
	
	/**
	 * JavaPackageの場合.
	 * @return
	 */
	public boolean isJavaPackage() {
		return javaPackageFlag;
	}
	
	/**
	 * ClassPackageの場合.
	 * @return
	 */
	public boolean isClassPackage() {
		return classPackageFlag;
	}
}
