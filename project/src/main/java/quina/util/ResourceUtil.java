package quina.util;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import quina.exception.QuinaException;

/**
 * javaのResourceファイルユーティリティ.
 */
public final class ResourceUtil {
	private ResourceUtil() {}
	
	/**
	 * javaのリソースファイルが存在するかチェック.
	 * @param resourceName Javaのリソースファイル名を設定します.
	 * @return boolean trueの場合、存在します.
	 */
	public static final boolean isFile(
		String name) {
		return Thread.currentThread()
			.getContextClassLoader()
			.getResource(name) != null;
	}
	
	/**
	 * javaのリソースファイルのInputStreamを取得.
	 * @param name Javaのリソースファイル名を設定します.
	 * @return InputStream InputStreamが返却されます.
	 */
	public static final InputStream getInputStream(
		String name) {
		if(!isFile(name)) {
			throw new QuinaException(
				"Specified resource file: " +
				name + " does not exist ");
		}
		return Thread
			.currentThread()
			.getContextClassLoader()
			.getResourceAsStream(name);
	}
	
	/**
	 * javaのリソースファイル内容を取得.
	 *
	 * @param name Javaのリソースファイル名を設定します.
	 * @return byte[] バイナリ情報が返却されます.
	 * @exception Exception 例外.
	 */
	public static final byte[] getFile(String name)
		throws Exception {
		return FileUtil.getBinary(getInputStream(name));
	}

	/**
	 * javaのリソースファイル内容を文字列で取得.
	 *
	 * @param name Javaのリソースファイル名を設定します.
	 * @return String 文字列情報が返却されます.
	 * @exception Exception 例外.
	 */
	public static final String getString(String name)
		throws Exception {
		return getString(name, "UTF8");
	}
	/**
	 * javaのリソースファイル内容を文字列で取得.
	 *
	 * @param name Javaのリソースファイル名を設定します.
	 * @param charset 対象のキャラクタセットを設定します.
	 * @return String 文字列情報が返却されます.
	 * @exception Exception 例外.
	 */
	public static final String getString(
		String name, String charset)
		throws Exception {
		return FileUtil.getString(getInputStream(name), charset);

	}
	
	/**
	 * リソースファイルを、対象ファイルにコピーする.
	 * @param src リソースファイルパスを設定します.
	 * @param dest コピー先のファイル名を設定します.
	 * @throws Exception
	 */
	public static final void rcpy(String src, String dest)
		throws Exception {
		int len;
		byte[] bin = new byte[4096];
		OutputStream out = null;
		InputStream in = null;
		try {
			in = getInputStream(src);
			out = new FileOutputStream(dest);
			while ((len = in.read(bin)) != -1) {
				out.write(bin, 0, len);
			}
			in.close();
			in = null;
			out.close();
			out = null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
				try {
					out.close();
				} catch (Exception e) {
				}
			}
		}
	}
}
