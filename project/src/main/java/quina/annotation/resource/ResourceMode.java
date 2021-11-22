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
	Directory,
	/**
	 * Javaパッケージ.
	 */
	JavaPackage,
	/**
	 * クラスパッケージ.
	 */
	ClassPackage
	;
}
