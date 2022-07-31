package quina.net.nio.tcp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import quina.compile.cdi.annotation.AnnotationCdiConstants;
import quina.exception.QuinaException;
import quina.util.FileUtil;
import quina.util.Flag;

/**
 * トラストストアの証明書の取得・管理.
 */
public class SslCacerts {
	/**
	 * ルートCA証明書ファイル名.
	 */
	public static final String CACERTS_FILE = "cacerts";
	
	/**
	 * ルートCA証明書のパスワード.
	 */
	public static final char[] TRUST_PASSWORD = "changeit".toCharArray();
	
	/**
	 * リソース先のSslのCacertsファイル格納パッケージ名.
	 */
	public static final String RESOURCE_PACKAGE_NAME =
		AnnotationCdiConstants.CDI_PACKAGE_NAME + ".ssl";
	
	/**
	 * リソース先のSslのCacertsファイル名.
	 */
	public static final String RESOURCE_CACERTS_FILE =
			RESOURCE_PACKAGE_NAME + "." + CACERTS_FILE;

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
	
	/**
	 * パッケージ名をリソースパスに変換.
	 * @param packageNameFile xxx.yyy.Zzz のようなパッケージ名+リソースファイル名を設定します.
	 * @return String /xxx/yyy/Zzz のような リソースパスに変換されます.
	 */
	public static final String convertPackageNameToResourcePath(
		String packageNameFile) {
		StringBuilder buf = new StringBuilder("/");
		char c;
		final int len = packageNameFile.length();
		for(int i = 0; i < len; i ++) {
			c = packageNameFile.charAt(i);
			if(c == '.') {
				buf.append("/");
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}

	/**
	 * $JAVA_HOME環境変数の「cacerts」ファイルパスを取得.
	 * @return String ファイルパスが返却されます.
	 */
	public static final String getJavaHomeCacertsPath() {
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
				return getFullPath(ret);
			}
			// graalvmの場合.
			ret = new StringBuilder(javaHome)
				.append(sp).append("lib")
				.append(sp).append("security")
				.append(sp).append(CACERTS_FILE)
				.toString();
			if (isFile(ret)) {
				return getFullPath(ret);
			}
		}
		return null;
	}

	/**
	 * コンフィグディレクトリ名から「cacerts」ファイルパスを取得.
	 * @return String ファイルパスが返却されます.
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

	/**
	 * システムプロパティから「cacerts」ファイルパスを取得.
	 * @return String ファイルパスが返却されます.
	 */
	public static final String getCacertsBySystemProperty() {
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
	 * リソースパスからInputStreamを取得.
	 * @return InputStream InputStreamが返却されます.
	 */
	public static final InputStream getResourceToInputStream() {
		// カレントのクラスローダーを取得.
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream ret = null;
		try {
			ret = cl.getResourceAsStream(
				convertPackageNameToResourcePath(RESOURCE_CACERTS_FILE));
		} catch(Exception e) {
			ret = null;
		}
		return ret;
	}
	
	/**
	 * 指定ファイルパスからInputStreamを取得.
	 * @param path 対象のファイルパスを設定します.
	 * @return InputStream InputStreamが返却されます.
	 */
	public static final InputStream getFileNameToInputStream(String path) {
		InputStream ret = null;
		if(path != null && !path.isEmpty()) {
			try {
				ret = new BufferedInputStream(
					new FileInputStream(path));
			} catch(Exception e) {
				ret = null;
			}
		}
		return ret;
	}
	
	/**
	 * SSLの認証局証明書管理ファイル「cacerts」InputStreamを取得.
	 * @return InputStream 「cacerts」InputStreamが返却されます.
	 */
	public static final InputStream getInputStream() {
		InputStream in = null;
		// JAVA_HOMEが存在する場合は、jreのcacertsファイルを
		// 読み込む.
		String changeitFile = getJavaHomeCacertsPath();
		if (changeitFile != null) {
			in = getFileNameToInputStream(changeitFile);
		}
		
		// 読み込めない場合はConfigディレクトリ群を読み込む.
		if(in == null) {
			// configディレクトリを取得.
			changeitFile = getConfigDirectory();
			// configディレクトリが存在する場合.
			if(changeitFile != null) {
				in = getFileNameToInputStream(changeitFile);
			}
		}
		
		// 読み込めない場合はSystemPropertyから取得.
		if(in == null) {
			// SystemPropertyから取得.
			changeitFile = getCacertsBySystemProperty();
			if(changeitFile != null) {
				in = getFileNameToInputStream(changeitFile);
			}
		}
		
		// 読み込めない場合はリソースパスから取得.
		if(in == null) {
			// リソースパスから取得.
			in = getResourceToInputStream();
		}
		// cacertsのInputStreamが存在しない場合はエラー.
		if(in == null) {
			throw new NioException("Failed to load '" +
				CACERTS_FILE + "' file.");
		}
		return in;
	}
	
	/**
	 * InputStreamからバイナリ情報を取得.
	 * @param in InputStreamを設定します.
	 * @return byte[] バイナリが返却されます.
	 */
	public static final byte[] getBinary(InputStream in) {
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
	
	// SocketFactory生成用情報.
	private static final Object sslSync = new Object();
	private static Flag sslFactoryFlag = new Flag(false);
	private static SSLSocketFactory sslFactory = null;

	/**
	 * SSLSocketFactory作成.
	 * @return SSLSocketFactory SSLSocketFactoryが返却されます.
	 */
	public static final SSLSocketFactory getSSLSocketFactory() {
		if (!sslFactoryFlag.get()) {
			synchronized (sslSync) {
				if(!sslFactoryFlag.get()) {
					InputStream in = null;
					try {
						// キーストアを生成.
						in = getInputStream();
						KeyStore keyStore = KeyStore.getInstance("JKS");
						keyStore.load(in, SslCacerts.TRUST_PASSWORD);
						in.close();
						in = null;
						
						// トラストストアマネージャを生成.
						final TrustManagerFactory tmf =
							TrustManagerFactory.getInstance("SunX509");
						tmf.init(keyStore);
						keyStore = null;
						
						// SSLソケットを生成する
						final SSLContext ctx = SSLContext.getInstance("TLS");
						ctx.init(null, tmf.getTrustManagers(), null);
						SSLSocketFactory s = ctx.getSocketFactory();
						
						// SSLSocketFactoryをセット.
						sslFactory = s;
						sslFactoryFlag.set(true);
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
				}
			}
		}
		return sslFactory;
	}
}
