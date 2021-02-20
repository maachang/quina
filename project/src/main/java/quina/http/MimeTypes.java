package quina.http;

/**
 * MimeType定義群.
 */
public interface MimeTypes {

	/**
	 * 拡張子を指定してMimeTypeを取得.
	 * @param extension 拡張子を設定します.
	 * @return String mimeTypeが返却されます.
	 */
	public String getMimeType(String extension);

	/**
	 * 対象のmimeTypeにcharsetが追加されるかチェック.
	 * @param mime mimeTypeを設定します.
	 * @return boolean trueの場合、charsetが追加可能です.
	 */
	public boolean isAppendCharset(String mime);
}
