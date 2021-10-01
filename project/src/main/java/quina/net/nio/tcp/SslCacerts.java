package quina.net.nio.tcp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import quina.exception.QuinaException;
import quina.util.FileUtil;

/**
 * トラストストアの証明書
 * (SSLの認証局証明書管理ファイル)を取得.
 */
public class SslCacerts {
	private static final String CACERTS_FILE = "cacerts";
	public static final char[] TRUST_PASSWORD = "changeit".toCharArray();

	// ファイル存在.
	private static final boolean isFile(String name) {
		final File file = new File(name);
		return (file.exists() && !file.isDirectory());
	}

	// フルパス変換.
	private static final String getFullPath(String path) {
		try {
			char c;
			path = new File(path).getCanonicalPath();
			final int len = path.length();
			StringBuilder buf = new StringBuilder(len + 2);
			if(!path.startsWith("/")) {
				buf.append("/");
			} else if(path.indexOf("\\") == -1) {
				return path;
			}
			for(int i = 0; i < len; i++) {
				c = path.charAt(i);
				if(c == '\\') {
					buf.append("/");
				} else {
					buf.append(c);
				}
			}
			return buf.toString();
		} catch(Exception e) {
			throw new NioException(e);
		}
	}

	// JREの「cacerts」ファイルパスを取得.
	private static final String getJavaHomeCacertsPath() {
		// JAVA_HOMEが設定されている場合.
		final String javaHome = System.getenv("JAVA_HOME");
		if (javaHome != null && !javaHome.isEmpty()) {
			final String sp = System.getProperty("file.separator");
			// 通常VMとしてチェック.
			String ret = new StringBuilder(javaHome)
				.append(sp).append("jre")
				.append(sp).append("lib")
				.append(sp).append("security")
				.append(sp).append(CACERTS_FILE)
				.toString();
			if(isFile(ret)) {
				return ret;
			}
			// graalvmの場合.
			ret = new StringBuilder(javaHome)
				.append(sp).append("lib")
				.append(sp).append("security")
				.append(sp).append(CACERTS_FILE)
				.toString();
			if (isFile(ret)) {
				return ret;
			}
		}
		return null;
	}

	/**
	 * コンフィグディレクトリ名を取得.
	 * @return String コンフィグディレクトリ名が返却されます.
	 */
	public static final String getConfigDirectory() {
		try {
			String ret;
			String[] check;
			// コンフィグシステムプロパティから取得.
			check = new String[] {
				"config.dir",
				"config.directory",
				"config.folder",
				"config.path",
				"config"
			};
			for(int i = 0; i < check.length; i ++) {
				ret = System.getProperty(check[i]);
				if(ret != null && isFile(ret + "/" + CACERTS_FILE)) {
					return getFullPath(ret + "/" + CACERTS_FILE);
				}
			}
			// config関連ディレクトリ配下に存在する場合.
			check = new String[] {
				"./conf/" + CACERTS_FILE,
				"./config/" + CACERTS_FILE
			};
			for(int i = 0; i < check.length; i ++) {
				if(FileUtil.isFile(check[i])) {
					return getFullPath(check[i]);
				}
			}
			// カレントディレクトリに存在する場合.
			if(FileUtil.isFile("./" + CACERTS_FILE)) {
				return getFullPath("./" + CACERTS_FILE);
			}
			return null;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}

	// システムプロパティから取得.
	private static final String getCacertsBySystemProperty() {
		String o;
		String[] check;
		// システムプロパティから取得.
		check = new String[] {
			"http.cacerts",
			"net.cacerts",
			"ssl.cacerts",
			"cacerts"
		};
		for(int i = 0; i < check.length; i ++) {
			o = System.getProperty(check[i]);
			if(o != null) {
				if(FileUtil.isFile(o)) {
					return getFullPath(o);
				}
			}
		}
		return null;
	}

	/**
	 * SSLの認証局証明書管理ファイル
	 * cacertsの内容を取得.
	 * @return
	 */
	public static final byte[] get() {
		InputStream in = null;
		// JAVA_HOMEが存在する場合は、jreのcacertsファイルを
		// 読み込む.
		String changeitFile = getJavaHomeCacertsPath();
		if (changeitFile != null) {
			try {
				in = new BufferedInputStream(
					new FileInputStream(changeitFile));
			} catch(Exception e) {
				in = null;
			}
		}
		// 読み込めない場合はConfigディレクトリ群を読み込む.
		if(in == null) {
			// configディレクトリを取得.
			changeitFile = getConfigDirectory();
			// configディレクトリが存在する場合.
			if(changeitFile != null) {
				try {
					in = new BufferedInputStream(
						new FileInputStream(changeitFile));
				} catch(Exception e) {
					in = null;
				}
			}
		}
		// configディレクトリが見つからない場合.
		if(in == null) {
			// SystemPropertyから取得.
			changeitFile = getCacertsBySystemProperty();
			if(changeitFile != null) {
				try {
					in = new BufferedInputStream(
						new FileInputStream(changeitFile));
				} catch(Exception e) {
					in = null;
				}
			}
		}
		// cacertsのInputStreamが存在しない場合はエラー.
		if(in == null) {
			throw new NioException("Failed to load '" +
				CACERTS_FILE + "' file.");
		}
		// InputStreamを読み込む.
		byte[] ret = null;
		try {
			int len;
			final byte[] b = new byte[1024];
			NioBuffer buf = new NioBuffer(1024);
			while((len = in.read(b)) != -1) {
				buf.write(b, 0, len);
			}
			in.close();
			in = null;
			ret = buf.toByteArray();
			buf.close();
			buf = null;
		} catch (NioException ne) {
			throw ne;
		} catch (Exception e) {
			throw new NioException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
		return ret;
	}
}
