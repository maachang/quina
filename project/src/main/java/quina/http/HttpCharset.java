package quina.http;

/**
 * 基本的なHTTPでの文字コードを設定します.
 */
public enum HttpCharset {
	/** 不明. **/
	UNKNOWN("iso-8859-1")
	/** ISO-8859-1. **/
	,ISO_8859_1("iso-8859-1")
	/** UTF8. **/
	,UTF8("utf-8")
	/** UTF16. **/
	,UTF16("utf-16")
	/** UTF32. **/
	,UTF32("utf-32")
	/** EUC-JP **/
	,EUC_JP("euc-jp")
	/** Shift_Jis. **/
	,SHIFT_JIS("shift_jis")
	/** JIS. **/
	,JIS("iso-2022-jp")
	/** Shift_Jis(CP932). **/
	,CP932("cp-932")
	/** Shift_Jis(MS932). **/
	,MS932("ms-932")
	/** Shift_Jis(Windows-31J). **/
	,WINDOWS_31J("windows-31j")
	;

	private String charset;
	private HttpCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * 文字コードを出力.
	 * @return String 文字コードが返却されます.
	 */
	public String getCharset() {
		return charset;
	}

	@Override
	public String toString() {
		return charset;
	}
}
