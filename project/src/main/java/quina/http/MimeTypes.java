package quina.http;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import quina.QuinaUtil;
import quina.util.BooleanUtil;
import quina.util.StringUtil;
import quina.util.collection.IndexKeyValueList;

/**
 * 編集可能なMimeType定義群.
 */
public class MimeTypes {
	/** 不明なMimeType. **/
	public static final String UNKNONW_MIME_TYPE = "application/octet-stream";

	/** 拡張子に対するMimeType定義. **/
	private final IndexKeyValueList<String, String> extensionToMimeTypes = new IndexKeyValueList<String, String>();

	/** 文字コードが付与可能なMimeType定義. **/
	private final IndexKeyValueList<String, Boolean> appendCharsetToMimeTypes = new IndexKeyValueList<String, Boolean>();

	// デフォルト内容の初期化.
	private static final void init(IndexKeyValueList<String, String> mime, IndexKeyValueList<String, Boolean> charset) {
		// デフォルトMimeType.
		mime.put("htm", "text/html");
		mime.put("html", "text/html");
		mime.put("htc", "text/x-component");
		mime.put("pdf", "application/pdf");
		mime.put("rtf", "application/rtf");
		mime.put("doc", "application/msword");
		mime.put("xls", "application/vnd.ms-excel");
		mime.put("ppt", "application/ppt");
		mime.put("tsv", "text/tab-separated-values");
		mime.put("csv", "application/octet-stream");
		mime.put("txt", "text/plain");
		mime.put("xml", "text/xml");
		mime.put("xhtml", "application/xhtml+xml");
		mime.put("jar", "application/java-archiver");
		mime.put("sh", "application/x-sh");
		mime.put("shar", "application/x-sh");
		mime.put("tar", "application/x-tar");
		mime.put("z", "application/x-compress");
		mime.put("zip", "application/zip");
		mime.put("bmp", "image/x-bmp");
		mime.put("rle", "image/x-bmp");
		mime.put("dib", "image/x-bmp");
		mime.put("gif", "image/gif");
		mime.put("jpg", "image/jpeg");
		mime.put("jpeg", "image/jpeg");
		mime.put("jpe", "image/jpeg");
		mime.put("jfif", "image/jpeg");
		mime.put("jfi", "image/jpeg");
		mime.put("png", "image/x-png");
		mime.put("tiff", "image/tiff");
		mime.put("tif", "image/tiff");
		mime.put("aiff", "audio/aiff");
		mime.put("aif", "audio/aiff");
		mime.put("au", "audio/basic");
		mime.put("kar", "audio/midi");
		mime.put("midi", "audio/midi");
		mime.put("mid", "audio/midi");
		mime.put("smf", "audio/midi");
		mime.put("wav", "audio/wav");
		mime.put("asf", "video/x-ms-asf");
		mime.put("avi", "vide/x-msvideo");
		mime.put("m1s", "vide/mpeg");
		mime.put("m1v", "vide/mpeg");
		mime.put("m2s", "vide/mpeg");
		mime.put("m2v", "vide/mpeg");
		mime.put("mpeg", "vide/mpeg");
		mime.put("mpg", "vide/mpeg");
		mime.put("mpe", "vide/mpeg");
		mime.put("mpv", "vide/mpeg");
		mime.put("m1a", "audio/mpeg");
		mime.put("m2a", "audio/mpeg");
		mime.put("mp2", "audio/mpeg");
		mime.put("mp3", "audio/mpeg");
		mime.put("ogg", "audio/ogg");
		mime.put("m4a", "audio/aac");
		mime.put("webm", "audio/webm");
		mime.put("moov", "video/quicktime");
		mime.put("mov", "video/quicktime");
		mime.put("qt", "video/quicktime");
		mime.put("rm", "audio/x-pn-realaudio");
		mime.put("swf", "application/x-shockwave-flash");
		mime.put("exe", "application/exe");
		mime.put("pl", "application/x-perl");
		mime.put("ram", "audio/x-pn-realaudio");
		mime.put("js", "text/javascript");
		mime.put("css", "text/css");
		mime.put("ico", "image/x-icon");
		mime.put("manifest", "text/cache-manifest");

		// charset付加できるMimeType.
		charset.put("text/html", true);
		charset.put("text/javascript", true);
		charset.put("text/css", true);
		charset.put("text/plain", true);
		charset.put("text/xml", true);
		charset.put("application/json", true);
		charset.put("application/xhtml+xml", true);
		charset.put("text/x-component", true);
	}

	/**
	 * コンストラクタ.
	 */
	private MimeTypes() {
		init(extensionToMimeTypes, appendCharsetToMimeTypes);
	}

	// シングルトン.
	private static final MimeTypes SNGL = new MimeTypes();

	/**
	 * オブジェクトを取得.
	 * @return
	 */
	public static final MimeTypes getInstance() {
		return SNGL;
	}

	/**
	 * 内容のリセット.
	 */
	public void reset() {
		extensionToMimeTypes.clear();
		appendCharsetToMimeTypes.clear();
		init(extensionToMimeTypes, appendCharsetToMimeTypes);
	}

	/**
	 * 決められたMimeTypeのJSON形式で、MimeTypeを追加.
	 * @param json 対象のJSON情報を設定します.
	 */
	@SuppressWarnings("unchecked")
	public void setMimeTypes(Map<String, Object> json) {
		//
		// jsonの定義方法
		// {
		//   extension: mimeType // (1)
		//   extension: {mimeType: mimeType, charset: true/false} // (2)
		// }
		// (1)の登録拡張子とmimeTypeを定義する場合です.
		// (2)は基本(1)と同じだが、charset付与可能か否かが設定出来ます.
		//
		Map<String, Object> v;
		Entry<String, Object> e;
		Iterator<Entry<String, Object>> it = json.entrySet().iterator();
		while(it.hasNext()) {
			e = it.next();
			if(e.getValue() instanceof Map) {
				v = (Map<String, Object>)e.getValue();
				if(v.containsKey("mimeType")) {
					if(v.containsKey("charset")) {
						put(e.getKey(),
							StringUtil.parseString(v.get("mimeType")),
							BooleanUtil.parseBoolean(v.get("charset")));
					} else {
						put(e.getKey(),
							StringUtil.parseString(v.get("mimeType")));
					}
				}
			} else {
				put(e.getKey(), StringUtil.parseString(e.getValue()));
			}
		}
	}

	/**
	 * MimeTypeをセット.
	 * @param extension 拡張子を設定します.
	 * @param mime mimeTypeを設定します.
	 * @param appendCharsetFLag このmimeTypeにcharset追加可能な場合はtrueを設定します.
	 */
	public void put(String extension, String mime) {
		put(extension, mime, false);
	}

	/**
	 * MimeTypeをセット.
	 * @param extension 拡張子を設定します.
	 * @param mime mimeTypeを設定します.
	 * @param appendCharsetFLag このmimeTypeにcharset追加可能な場合はtrueを設定します.
	 */
	public void put(String extension, String mime, boolean appendCharsetFLag) {
		if(extension == null || extension.isEmpty()) {
			throw new HttpException("You cannot set an empty extension. ");
		} else if(mime == null || mime.isEmpty()) {
			throw new HttpException("Empty MimeType cannot be set.");
		}
		extensionToMimeTypes.put(extension, mime);
		if(appendCharsetFLag) {
			appendCharsetToMimeTypes.put(mime, true);
		}
	}

	/**
	 * 拡張子を指定して削除.
	 * @param extension 拡張子を設定します.
	 */
	public void removeExtension(String extension) {
		if(extension == null || extension.isEmpty()) {
			return;
		}
		String mime = extensionToMimeTypes.get(extension);
		if(mime != null) {
			extensionToMimeTypes.remove(extension);
			appendCharsetToMimeTypes.remove(mime);
		}
	}

	/**
	 * MimeTypeを指定して削除.
	 * @param mime MimeTypeを設定します.
	 */
	public void removeMimeType(String mime) {
		if(mime == null || mime.isEmpty()) {
			return;
		}
		String extension;
		int len = extensionToMimeTypes.size();
		for(int i = 0; i < len; i ++) {
			if(mime.equals(extensionToMimeTypes.valueAt(i))) {
				extension = extensionToMimeTypes.keyAt(i);
				extensionToMimeTypes.remove(extension);
				appendCharsetToMimeTypes.remove(mime);
			}
		}
	}

	/**
	 * 拡張子を指定してMimeTypeを取得.
	 * @param extension 拡張子を設定します.
	 * @return String mimeTypeが返却されます.
	 */
	public String getMimeType(String extension) {
		if(extension == null || extension.isEmpty()) {
			return null;
		}
		return extensionToMimeTypes.get(extension);
	}

	/**
	 * 対象のmimeTypeにcharsetが追加されるかチェック.
	 * @param mime mimeTypeを設定します.
	 * @return boolean trueの場合、charsetが追加可能です.
	 */
	public boolean isAppendCharset(String mime) {
		if(mime == null || mime.isEmpty()) {
			return false;
		}
		Boolean ret = appendCharsetToMimeTypes.get(mime);
		if(ret == null) {
			return false;
		}
		return ret;
	}

	// 拡張子を取得.
	private static final String getExtension(String path) {
		// "aaa/bbb.ccc" の場合は "ccc"の位置を取得.
		int p = path.lastIndexOf(".");
		if(p == -1) {
			return null;
		}
		final String ret = path.substring(p + 1);
		// "aaa.bb/ccc" のような状況の場合.
		if(ret.indexOf("/") != -1) {
			return null;
		}
		return ret;
	}

	/**
	 * ファイル名を指定してMimeTypeを取得.
	 * @param name
	 * @return
	 */
	public String getMimeTypeFileName(String name) {
		name = getExtension(name);
		if(name != null) {
			return getMimeType(name);
		}
		return null;
	}

	/**
	 * 文字列変換.
	 * @param out StringBuilderを設定します.
	 * @param space データ毎の先頭スペース数を設定します.
	 */
	public void toString(StringBuilder out, int space) {
		Boolean charset;
		String extension, mimeType;
		final int len = extensionToMimeTypes.size();
		for(int i = 0; i < len; i ++) {
			if(i != 0) {
				out.append("\n");
			}
			extension = extensionToMimeTypes.keyAt(i);
			mimeType = extensionToMimeTypes.valueAt(i);
			charset = appendCharsetToMimeTypes.get(mimeType);
			QuinaUtil.setSpace(out, space);
			out.append("extension: ").append(extension)
				.append(", mimeType; ").append(mimeType);
			if(charset != null && charset) {
				out.append(", charset: ").append(charset);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("mimeType: \n");
		toString(buf, 2);
		return buf.toString();
	}
}
