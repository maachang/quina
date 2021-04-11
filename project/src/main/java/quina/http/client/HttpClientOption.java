package quina.http.client;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import quina.http.Header;
import quina.http.HttpConstants;
import quina.http.HttpSendChunkedData;
import quina.http.HttpSendHeader;
import quina.http.Method;
import quina.json.Json;
import quina.net.nio.tcp.NioSendData;
import quina.net.nio.tcp.NioSendFileData;
import quina.net.nio.tcp.NioSendInputStreamData;
import quina.net.nio.tcp.NioSendMemData;
import quina.util.Alphabet;
import quina.util.StringUtil;

/**
 * HttpClientOption.
 */
public class HttpClientOption {
	private Method method = Method.GET;
	private RedirectMode redirect = RedirectMode.follow;
	private HttpSendHeader headers = null;
	private HttpSendHeader fixHeaders = null;
	private NioSendData body = null;
	private String formData = null;
	private String referrer = null;
	private String charset = null;
	private boolean jsonFlag = false;
	private boolean fixFlag = false;

	/**
	 * オブジェクトを破棄.
	 */
	public void destroy() {
		if(body != null) {
			try {
				body.close();
			} catch(Exception e) {}
		}
		headers = null;
		fixHeaders = null;
		referrer = null;
		charset = null;
	}

	/**
	 * 情報を確定する.
	 */
	public void fix() {
		if(fixFlag) {
			return;
		}
		// MethodがGETやDELETEに対してBodyが設定されている場合は
		// エラー返却.
		if(body != null &&
			(Method.GET == method || Method.DELETE == method)) {
			throw new HttpClientException(
				"Body cannot be set when Method is " + method + ".");
		// MethodがPOSTに対してBodyが設定されてない場合は
		// エラー返却.
		} else if(body == null && Method.POST == method) {
			// ただしformDataが存在する場合はエラーにしない.
			if(formData == null || formData.isEmpty()) {
				throw new HttpClientException(
					"Body is not set for the method POST.");
			}
		}
		// ヘッダー情報が設定されていない場合.
		if(headers == null) {
			headers = new HttpSendHeader();
			fixHeaders = new HttpSendHeader();
		} else {
			// fix後のヘッダを生成.
			fixHeaders = headers.copy();
		}
		// リダイレクトモードが設定されていない場合.
		if(redirect == null) {
			redirect = RedirectMode.follow;
		}
		// リファラーが設定されていない場合.
		if(referrer == null || referrer.isEmpty()) {
			referrer = null;
		// リファラーが設定されている場合.
		} else {
			fixHeaders.put("Referer", referrer);
		}
		// JSONモードでの送信の場合.
		if(jsonFlag) {
			fixHeaders.put("Content-Type", HttpConstants.MIME_TYPE_JSON);
		}
		// Formデータが存在する場合.
		if(formData != null && !formData.isEmpty()) {
			// メソッドがGETかDELETE以外の場合.
			if(Method.GET != method && Method.DELETE != method) {
				// Body変換してコンテンツタイプにフォームデータ設定.
				setBody(formData, charset);
				fixHeaders.put("Content-Type",
					"application/x-www-form-urlencoded");
				formData = null;
			}
		}
		// コンテンツタイプが設定されていて、キャラクターセットが
		// 設定されている場合.
		String s = fixHeaders.get("Content-Type");
		if(charset != null && !charset.isEmpty() &&
			s != null && !s.isEmpty()) {
			if(Alphabet.indexOf(s, "charset") == -1) {
				s += ";charset=" + charset;
				fixHeaders.put("Content-Type", s);
			}
		}
		// bodyが存在する場合.
		if(body != null) {
			// Body情報がチャンク送信の場合.
			if(body instanceof HttpSendChunkedData) {
				// チャング送信条件をセット.
				fixHeaders.put("Transfer-Encoding", "chunked");
				fixHeaders.remove("Content-Length");
			// Body情報がチャンク送信でない場合.
			// コンテンツ長が設定されていない場合は設定する.
			} else if(headers.containsKey("Content-Length")) {
				fixHeaders.put("Content-Length", "" + body.length());
			}
		}
		// fix完了.
		fixFlag = true;
	}

	/**
	 * Fix状態をキャンセル.
	 */
	public void cancelFix() {
		fixFlag = false;
	}

	/**
	 * Fixされているかチェック.
	 * @return boolean trueの場合Fixしています.
	 */
	public boolean isFix() {
		return fixFlag;
	}

	// Fixしている場合はエラー.
	private void checkFix() {
		if(fixFlag) {
			throw new HttpClientException("I have already fixed it.");
		}
	}

	/**
	 * HttpMethodを取得.
	 * @return
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * HttpMethodを設定.
	 * @param method
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setMethod(Method method) {
		checkFix();
		// メソッドが設定されてない場合.
		if(method == null) {
			// GETをセット.
			method = Method.GET;
		// 対応していないメソッドが設定された場合.
		} else if(Method.HEAD == method ||
			Method.OPTIONS == method ||
			Method.TRACE == method) {
			throw new HttpClientException(
				"The configured Http method is not supported: " + method);
		}
		// methodがGETの場合.
		if(Method.GET == method) {
			// 設定されているBodyを削除.
			if(this.body != null) {
				try {
					this.body.close();
				} catch(Exception e) {}
			}
			// ヘッダからBody関連の条件を削除.
			if(headers != null) {
				headers.remove("Content-Length");
				headers.remove("Transfer-Encoding");
			}
		}
		this.method = method;
		return this;
	}

	/**
	 * HttpMethodを設定.
	 * @param method
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setMethod(String method) {
		checkFix();
		return setMethod(Method.get(method));
	}

	/**
	 * HttpHeaderを取得.
	 * @return
	 */
	public Header getHeaders() {
		if(fixFlag) {
			return fixHeaders;
		}
		return headers;
	}

	/**
	 * HttpHeaderを設定.
	 * @param headers
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setHeaders(Object... headers) {
		checkFix();
		this.headers = new HttpSendHeader(headers);
		return this;
	}

	/**
	 * HttpHeaderを設定.
	 * @param headers
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setHeaders(Header headers) {
		checkFix();
		if(headers instanceof HttpSendHeader) {
			this.headers = (HttpSendHeader)headers;
		} else {
			this.headers = new HttpSendHeader(headers);
		}
		return this;
	}

	/**
	 * HttpHeaderを設定.
	 * @param headers
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setHeaders(Map<String, String> headers) {
		checkFix();
		this.headers = new HttpSendHeader(headers);
		return this;
	}

	/**
	 * Body情報を取得.
	 * @return
	 */
	public NioSendData getBody() {
		return body;
	}

	/**
	 * Body情報が存在するかチェック.
	 * @return
	 */
	public boolean isBody() {
		return body != null;
	}

	/**
	 * FormData情報を取得.
	 * @return
	 */
	public String getFormData() {
		return formData;
	}

	/**
	 * FormData情報が存在するかチェック.
	 * @return
	 */
	public boolean isFormData() {
		return formData != null && !formData.isEmpty();
	}

	// 現在のBody情報が存在する場合はクローズする.
	private void nowBodyByClose() {
		if(this.body != null) {
			try {
				this.body.close();
			} catch(Exception e) {}
		}
		this.body = null;
		this.formData = null;
		this.charset = null;
		this.jsonFlag = false;
	}

	/**
	 * フォームデータを設定.
	 * @param formData フォームデータを設定します.
	 * @param charset 文字コードを設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	@SuppressWarnings("rawtypes")
	public HttpClientOption setFormData(Map formData) {
		return setFormData(formData, null);
	}

	/**
	 * フォームデータを設定.
	 * @param formData フォームデータを設定します.
	 * @param charset 文字コードを設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HttpClientOption setFormData(Map formData, String charset) {
		if(formData == null || formData.size() <= 0) {
			throw new HttpClientException("Form data is not set.");
		} else if(charset == null || charset.isEmpty()) {
			charset = HttpConstants.getCharset();
		}
		checkFix();
		Entry e;
		Object v;
		String k;
		int cnt = 0;
		StringBuilder buf = new StringBuilder();
		Iterator<Entry> it = formData.entrySet().iterator();
		while (it.hasNext()) {
			e = it.next();
			k = e.getKey().toString();
			v = e.getValue();
			if (cnt ++ != 0)
				buf.append("&");
			buf.append(k).append("=");
			if (v != null && ((String) (v = v.toString())).length() > 0) {
				buf.append(StringUtil.urlEncode((String) v, charset));
			}
		}
		nowBodyByClose();
		this.formData = buf.toString();
		this.charset = charset;
		return this;
	}


	/**
	 * Body情報を設定.
	 * @param body バイナリ情報を設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setBody(byte[] body) {
		if(body == null || body.length == 0) {
			throw new HttpClientException("body information is not set.");
		}
		checkFix();
		nowBodyByClose();
		this.body = new NioSendMemData(body);
		return this;
	}

	/**
	 * Body情報を設定.
	 * @param body 文字列を設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setBody(String body) {
		return this.setBody(body, null);
	}

	/**
	 * Body情報を設定.
	 * @param body 文字列を設定します.
	 * @param charset バイナリに変換する文字コードを設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setBody(String body, String charset) {
		if(body == null || body.isEmpty()) {
			throw new HttpClientException("body information is not set.");
		} else if(charset == null || charset.isEmpty()) {
			charset = HttpConstants.getCharset();
		}
		checkFix();
		NioSendData ns = null;
		try {
			ns = new NioSendMemData(body.getBytes(charset));
		} catch(Exception e) {
			throw new HttpClientException(e);
		}
		nowBodyByClose();
		this.body = ns;
		this.charset = charset;
		return this;
	}

	/**
	 * Body情報にJson情報をセット.
	 * @param json 送信するオブジェクトを設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setJson(Object json) {
		return setJson(json, null);
	}

	/**
	 * Body情報にJson情報をセット.
	 * @param json 送信するオブジェクトを設定します.
	 * @param charset バイナリに変換する文字コードを設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setJson(Object json, String charset) {
		if(json == null) {
			throw new HttpClientException("body information is not set.");
		} else if(charset == null || charset.isEmpty()) {
			charset = HttpConstants.getCharset();
		}
		checkFix();
		String body = Json.encode(json);
		json = null;
		NioSendData ns = null;
		try {
			ns = new NioSendMemData(body.getBytes(charset));
		} catch(Exception e) {
			throw new HttpClientException(e);
		}
		nowBodyByClose();
		this.body = ns;
		this.charset = charset;
		this.jsonFlag = true;
		return this;
	}

	/**
	 * Body情報を設定.
	 * この処理ではチャング送信でBodyを送信します.
	 * @param body InputStreamを設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setBody(InputStream body) {
		if(body == null) {
			throw new HttpClientException("body information is not set.");
		}
		checkFix();
		try {
			nowBodyByClose();
			this.body = new HttpSendChunkedData(body);
		} catch(Exception e) {
			throw new HttpClientException(e);
		}
		return this;
	}

	/**
	 * Body情報を設定.
	 * @param body InputStreamを設定します.
	 * @param length Bodyの長さを設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setBody(InputStream body, long length) {
		if(body == null) {
			throw new HttpClientException("body information is not set.");
		}
		checkFix();
		nowBodyByClose();
		this.body = new NioSendInputStreamData(body, length);
		return this;
	}

	/**
	 * ファイル送信用のBody情報を設定.
	 * @param name ファイル名を設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setFile(String name) {
		if(name == null || name.isEmpty()) {
			throw new HttpClientException("The file name to be sent is not set.");
		}
		checkFix();
		NioSendData ns = null;
		try {
			ns = new NioSendFileData(name);
		} catch(Exception e) {
			throw new HttpClientException(e);
		}
		nowBodyByClose();
		this.body = ns;
		return this;
	}

	/**
	 * 設定された文字コードを取得.
	 * @return String 文字コードが返却されます.
	 *                nullの場合は設定されていません.
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * リファラーを取得.
	 * @return String リファラーURLを設定します.
	 */
	public String getReferrer() {
		return referrer;
	}

	/**
	 * リファラーを設定.
	 * @param referrer リファラーを設定します.
	 *                 [null]及び空文字を設定した場合はリファラーは設定されません.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setReferrer(String referrer) {
		checkFix();
		if(referrer == null || referrer.isEmpty()) {
			this.referrer = null;
		} else {
			this.referrer = referrer;
		}
		return this;
	}

	/**
	 * リダイレクトモードを取得.
	 * @return RedirectMode リダイレクトモードが返却されます.
	 */
	public RedirectMode getRedirect() {
		return redirect;
	}

	/**
	 * リダイレクトモードを設定.
	 * @param redirect リダイレクトモードを設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setRedirect(RedirectMode redirect) {
		checkFix();
		this.redirect = redirect;
		return this;
	}

	/**
	 * リダイレクトモードを設定.
	 * @param redirect リダイレクトモードを文字列で設定します.
	 * @return HttpClientOption オブジェクトが返却されます.
	 */
	public HttpClientOption setRedirect(String redirect) {
		checkFix();
		this.redirect = RedirectMode.get(redirect);
		return this;
	}
}
