package quina.http;

import java.io.Closeable;
import java.io.InputStream;

import quina.net.nio.tcp.NioBuffer;

/**
 * Httpリクエスト.
 */
public interface Request extends Closeable {

	/**
	 * Httpメソッドを取得.
	 * @return Method Httpメソッドを取得します.
	 */
	public Method getMethod();

	/**
	 * リクエストされたURLを取得.
	 * @return String URLが返却されます.
	 */
	public String getUrl();

	/**
	 * リクエストされた元のURLを取得.
	 * getUrl()とは違い、GETパラメータ(?xxx=yyy&zzz=xyz)が含まれます.
	 * @return String 変換されてない元のURLが返却されます.
	 */
	public String getBaseUrl();

	/**
	 * HTTPバージョンを取得.
	 * @return String HTTPバージョンが返却されます.
	 */
	public String getVersion();

	/**
	 * Bodyデータの長さを取得.
	 * @return long Bodyデータ長が返却されます.
	 */
	public long getContentLength();

	/**
	 * Httpヘッダ情報を取得.
	 * @return Header Httpヘッダが返却されます.
	 */
	public Header getHeader();

	/**
	 * Body情報を取得.
	 * @return InputStream Body情報が返却されます.
	 */
	public InputStream getInputStream();

	/**
	 * Body情報を取得.
	 * @return byte[] Body情報が返却されます.
	 */
	default byte[] getBytes() {
		int len;
		final byte[] bin = new byte[1024];
		InputStream in = getInputStream();
		if(in != null) {
			try {
				NioBuffer buf = new NioBuffer(1024);
				while((len = in.read(bin)) != -1) {
					buf.write(bin, 0, len);
				}
				in.close(); in = null;
				byte[] ret = buf.toByteArray();
				buf.close();
				return ret;
			} catch(Exception e) {
				throw new HttpException(e);
			} finally {
				if(in != null) {
					try {
						in.close();
					} catch(Exception e) {}
				}
			}
		}
		return null;
	}

	/**
	 * Body情報を取得.
	 * @return String Body情報が返却されます.
	 */
	default String getString() {
		return getString(null);
	}

	/**
	 * Body情報を取得.
	 * @param charset 対象の文字列を設定します.
	 * @return String Body情報が返却されます.
	 */
	default String getString(String charset) {
		final byte[] b = getBytes();
		if(b != null) {
			try {
				// Body変換用の文字コードが設定されていない場合.
				if(charset == null) {
					// Content-Typeから文字コードを取得.
					charset = HttpAnalysis.contentTypeToCharset(
						getHeader().get("Content-Type"));
					if(charset == null) {
						// 存在しない場合は、デフォルト条件を設定します.
						charset = HttpConstants.getCharset();
					}
				}
				// 文字列変換.
				return new String(b, charset);
			} catch(Exception e) {
				throw new HttpException(e);
			}
		}
		return null;
	}

	/**
	 * パラメータを取得.
	 * @return Params パラメータが返却されます.
	 */
	public Params getParams();
}
