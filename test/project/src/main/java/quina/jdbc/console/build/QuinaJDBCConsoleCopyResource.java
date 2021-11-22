package quina.jdbc.console.build;

import quina.annotation.resource.BuildResource;
import quina.annotation.resource.ResourceMode;
import quina.annotation.resource.ResourceScoped;

/**
 * Quina jdbc console コンテンツをclassに
 * コピー処理を行う.
 */
@ResourceScoped
public class QuinaJDBCConsoleCopyResource {
	// copy file list.
	private static final String[] COPY_LIST = new String[] {
		"login.html",
		"console.html",
		"base.js",
		"console.js",
		"console.css"
	};

	/**
	 * javaPackageからclassPackageに
	 * console用コンテンツをコピー.
	 * @return
	 */
	@BuildResource(
		src="quina.jdbc.console.resource",
		srcMode=ResourceMode.JavaPackage,
		dest="quina.jdbc.console.resource",
		destMode=ResourceMode.ClassPackage)
	public String[] copyResource() {
		return COPY_LIST;
	}
}
