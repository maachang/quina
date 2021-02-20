package quina.net.nio.tcp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import quina.net.nio.tcp.NioAtomicValues.Bool;

/**
 * トラストストアの証明書
 * (SSLの認証局証明書管理ファイル)を取得.
 */
public class SslCacerts {
	public static final char[] TRUST_PASSWORD = "changeit".toCharArray();
	private static byte[] cacerts = null;
	private static final Object sync = new Object();
	private static final Bool cacertsFlag = new Bool(false);

	/**
	 * SSLの認証局証明書管理ファイル[cacerts]の内容を取得.
	 * @return
	 */
	public static final byte[] get() {
		if (!cacertsFlag.get()) {
			synchronized (sync) {
				if(!cacertsFlag.get()) {
					InputStream in = null;
					// カレントパスにあるcacertsを読み込む.
					if (isFile("./cacerts")) {
						try {
							in = new BufferedInputStream(
								new FileInputStream("./cacerts"));
						} catch(Exception e) {
							in = null;
						}
					}
					// JAVA_HOME環境変数が存在する場合はそちらを優先して読み込む.
					try {
						final String javaHome = System.getenv("JAVA_HOME");
						if (javaHome != null && !javaHome.isEmpty()) {
							final String sp = System.getProperty("file.separator");
							// 通常javaで読み込む.
							String changeitFile = new StringBuilder(javaHome)
								.append(sp).append("jre")
								.append(sp).append("lib")
								.append(sp).append("security")
								.append(sp).append("cacerts")
								.toString();
							if (isFile(changeitFile)) {
								try {
									in = new BufferedInputStream(new FileInputStream(changeitFile));
								} catch(Exception e) {
									in = null;
								}
							}
							if(in == null) {
								// graalvmの場合.
								changeitFile = new StringBuilder(javaHome)
									.append(sp).append("lib")
									.append(sp).append("security")
									.append(sp).append("cacerts")
									.toString();
								if (isFile(changeitFile)) {
									try {
										in = new BufferedInputStream(new FileInputStream(changeitFile));
									} catch(Exception ee) {
										in = null;
									}
								}
							}
						}
						// それでも読み込み失敗の場合は組み込まれたcacertsを読み込む.
						if(in == null) {
							try {
								in = new BufferedInputStream(
									SslCacerts.class.getResourceAsStream("cacerts"));
							} catch(Exception e) {
								in = null;
							}
						}
						// cacertsが存在しない場合はエラー.
						if(in == null) {
							throw new NioException("Failed to load 'cacerts'.");
						}
						int len;
						final byte[] b = new byte[1024];
						NioBuffer buf = new NioBuffer(1024);
						while((len = in.read(b)) != -1) {
							buf.write(b, 0, len);
						}
						in.close();
						in = null;
						cacerts = buf.toByteArray();
						cacertsFlag.set(true);
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
				}
			}
		}
		return cacerts;
	}

	// ファイル存在.
	private static final boolean isFile(String name) {
		final File file = new File(name);
		return (file.exists() && !file.isDirectory());
	}
}
