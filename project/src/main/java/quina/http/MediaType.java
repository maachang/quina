package quina.http;

/**
 * よく使うMimeTypeを定義します.
 */
public enum MediaType {
	/** 不明. **/
	UNKNOWN("application/octet-stream", false)
	/** バイナリ. **/
	,OCTET_STREAM("application/octet-stream", false)
	/** フォームデータ. **/
	,FORM_DATA("application/x-www-form-urlencoded", true)
	/** プレーンテキスト. **/
	,TXT("text/plain", true)
	/** プレーンテキスト. **/
	,TEXT("text/plain", true)
	/** HTML. **/
	,HTM("text/html", true)
	/** HTML. **/
	,HTML("text/html", true)
	/** XML. **/
	,XML("text/xml", true)
	/** RSS. **/
	,RSS("application/rss+xm", true)
	/** XHTML. **/
	,XHTML("application/xhtml+xml", true)
	/** CSS. **/
	,CSS("text/css", true)
	/** JavaScript. **/
	,JS("text/javascript", true)
	/** JSON. **/
	,JSON("application/json", true)
	/** ZIP. **/
	,ZIP("application/zip", false)
	/** TAR. **/
	,TAR("application/x-tar", false)
	/** GZIP. **/
	,GZ("application/gzip", false)
	/** PDF. **/
	,PDF("application/pdf", false)
	/** MS-Word. **/
	,WORD("application/msword", false)
	/** MS-Word. **/
	,DOC("application/msword", false)
	/** MS-Excel. **/
	,EXCEL("application/vnd.ms-excelapplication/pdf", false)
	/** MS-Excel. **/
	,XLS("application/vnd.ms-excelapplication/pdf", false)
	/** MS-PowerPoint. **/
	,POWERPOINT("application/ppt", false)
	/** MS-PowerPoint. **/
	,PPT("application/ppt", false)
	/** BitMap. **/
	,BMP("image/x-bmp", false)
	/** Gif. **/
	,GIF("image/gif", false)
	/** Jpeg. **/
	,JPEG("image/jpeg", false)
	/** Jpeg. **/
	,JPG("image/jpeg", false)
	/** Png. **/
	,PNG("image/png", false)
	/** Tiff. **/
	,TIF("image/tiff", false)
	/** Tiff. **/
	,TIFF("image/tiff", false)
	/** Icon. **/
	,ICO("image/vnd.microsoft.icon", false)
	/** Flash. **/
	,SWF("application/x-shockwave-flash", false)
	/** Flash. **/
	,Flash("application/x-shockwave-flash", false)
	/** Avi. **/
	,AVI("video/x-msvideo", false)
	/** Mpeg. **/
	,MPG("video/mpeg", false)
	/** Mpeg. **/
	,MPEG("video/mpeg", false)
	/** Mp4. **/
	,MP4("video/mp4", false)
	/** Webm. **/
	,WEBM("video/webm", false)
	/** QuickTime. **/
	,MOV("video/quicktime", false)
	/** Mp3. **/
	,MP3("audio/mpeg", false)
	/** Ogg. **/
	,OGG("audio/ogg", false)
	/** M4a. **/
	,M4A("audio/aac", false)
	/** Wav. **/
	,WAV("audio/wav", false)
	/** Midi. **/
	,MID("audio/midi", false)
	/** Midi. **/
	,MIDI("audio/midi", false)
	;

	private String mimeType;
	private boolean charsetFlag;

	private MediaType(String mimeType, boolean charsetFlag) {
		this.mimeType = mimeType;
		this.charsetFlag = charsetFlag;
	}

	/**
	 * MimeTypeを取得.
	 * @return String MimeTypeが返却されます.
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * 文字コードの設定が可能かチェック.
	 * @return boolean trueの場合は文字コード設定可能です.
	 */
	public boolean isCharset() {
		return charsetFlag;
	}

	@Override
	public String toString() {
		return mimeType;
	}
}
