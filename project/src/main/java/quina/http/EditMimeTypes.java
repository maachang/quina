package quina.http;

import quina.util.collection.IndexMap;

/**
 * 編集可能なMimeType定義群.
 */
public class EditMimeTypes implements MimeTypes {
	/** 不明なMimeType. **/
	public static final String UNKNONW_MIME_TYPE = "application/octet-stream";

	/** 拡張子に対するMimeType定義. **/
	private final IndexMap<String, String> extensionToMimeTypes = new IndexMap<String, String>();

	/** 文字コードが付与可能なMimeType定義. **/
	private final IndexMap<String, Boolean> appendCharsetToMimeTypes = new IndexMap<String, Boolean>();

	// デフォルト内容の初期化.
	private static final void init(IndexMap<String, String> mime, IndexMap<String, Boolean> charset) {
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
	public EditMimeTypes() {
		init(extensionToMimeTypes, appendCharsetToMimeTypes);
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


}
