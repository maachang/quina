package quina.http;

import java.io.IOException;
import java.util.Map;

import quina.net.nio.tcp.NioRecvBody;
import quina.net.nio.tcp.NioRecvFileBody;
import quina.net.nio.tcp.NioRecvMemBody;
import quina.util.Alphabet;
import quina.util.Json;
import quina.util.StringUtil;
import quina.util.collection.BinarySearchMap;

/**
 * Http解析処理.
 */
public class HttpAnalysis {
	private HttpAnalysis() {}

	/**
	 * urlのみを取得.
	 * /a/b/c/hoge.json?a=100&b=200
	 * などの条件の場合、
	 * /a/b/c/hoge.jsonだけを取得します.
	 * @param url
	 * @return
	 */
	public static final String getUrl(String url) {
		int p = url.indexOf("?");
		if(p == -1) {
			return url;
		}
		return url.substring(0, p);
	}

	/**
	 * requestからパラメータ情報を取得.
	 * @param req
	 * @param custom
	 * @return
	 */
	public static final Params convertParams(Request req, HttpCustomPostParams custom) {
		Params params = null;
		final Method method = req.getMethod();
		if (method == Method.GET) {
			if(req.getUrl().length() != req.getBaseUrl().length()) {
				params = getParams(req.getBaseUrl(), req.getUrl().length() + 1);
			}
		} else if (method == Method.POST || method == Method.PUT ||
			method == Method.PATCH || method == Method.DELETE) {
			params = postParams(req, custom);
		}
		return params;
	}

	/**
	 * GETパラメータを取得.
	 * @param url
	 * @return
	 */
	public static final Params getParams(String url){
		int p = url.indexOf("?");
		if (p != -1) {
			return HttpAnalysis.getParams(url, p + 1);
		}
		return null;
	}

	/**
	 * POSTパラメータを取得.
	 * @param req
	 * @param custom
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Params postParams(Request req, HttpCustomPostParams custom) {
		final long contentLength = req.getContentLength();
		// Bodyデータが存在しない場合.
		if(contentLength == 0) {
			return null;
		// 大容量ファイルの受け取りの場合は、パラメータ解析を行わない.
		} else if (HttpConstants.getMaxRecvMemoryBodyLength() < contentLength) {
			return null;
		}
		// 文字列変換.
		String v = req.getString();
		// Body内容がJSON形式の場合.
		String contentType = req.getHeader().get("Content-Type");
		if (contentType.indexOf("application/json") == 0) {
			Object o = Json.decode(v);
			// デコード結果を返却.
			if(o != null && o instanceof BinarySearchMap) {
				// 形式がMap形式の場合はそのまま返却.
				return new Params(((BinarySearchMap)o).getIndexMap());
			}
			// Map形式でない場合は[value]をキー名でMap形式で返却する.
			return new Params("value", o);
		// Body内容がPOST形式の場合.
		} else if ("application/x-www-form-urlencoded".equals(contentType)) {
			return HttpAnalysis.getParams(v, 0);
		// Body内容のカスタム変換が存在する場合.
		} else if(custom != null) {
			Object o = custom.postParams(req);
			// デコード結果を返却.
			if(o != null) {
				if(o instanceof Map) {
					if(o instanceof BinarySearchMap) {
						// Paramsオブジェクトの場合.
						if(o instanceof Params) {
							return (Params)o;
						}
						// 形式がBinarySearchMap形式の場合はそのまま返却.
						return new Params(((BinarySearchMap)o).getIndexMap());
					} else {
						// それ以外のMap形式の場合.
						return new Params((Map)o);
					}
				}
			}
			// Map形式でない場合は[value]をキー名でMap形式で返却する.
			return new Params("value", o);

		}
		// 変換条件が存在しない場合.
		return null;
	}

	/**
	 * パラメータ変換処理. POSTのデータおよび、GETのデータを解析します.
	 *
	 * @param body
	 *            対象のBody情報を設定します.
	 * @param cset
	 *            対象のキャラクタセットを設定します.
	 * @param pos
	 *            対象のポジションを設定します.
	 * @return Params 変換結果を返却します.
	 */
	public static final Params getParams(String body, int pos) {
		return getParams(body, null, pos);
	}

	/**
	 * パラメータ変換処理. POSTのデータおよび、GETのデータを解析します.
	 *
	 * @param body
	 *            対象のBody情報を設定します.
	 * @param cset
	 *            対象のキャラクタセットを設定します.
	 * @param pos
	 *            対象のポジションを設定します.
	 * @return Params 変換結果を返却します.
	 */
	public static final Params getParams(
		String body, String cset, int pos) {
		if(cset == null || cset.isEmpty()) {
			cset = HttpConstants.getCharset();
		}
		// パラメータバイナリを解析.
		int p, n;
		String k;
		int b = pos;
		Params ret = new Params();
		while (true) {
			if ((n = body.indexOf("&", b)) == -1) {
				k = body.substring(b);
				if ((p = k.indexOf("=")) == -1) {
					break;
				}
				if (k.indexOf("%") != -1) {
					ret.put(StringUtil.urlDecode(k.substring(0, p), cset),
							StringUtil.urlDecode(k.substring(p + 1), cset));
				} else {
					ret.put(k.substring(0, p), k.substring(p + 1));
				}
				break;
			}
			k = body.substring(b, n);
			if ((p = k.indexOf("=")) == -1) {
				b = n + 1;
				continue;
			}
			if (k.indexOf("%") != -1) {
				ret.put(StringUtil.urlDecode(k.substring(0, p), cset),
					StringUtil.urlDecode(k.substring(p + 1), cset));
			} else {
				ret.put(k.substring(0, p), k.substring(p + 1));
			}
			b = n + 1;
		}
		return ret;
	}

	/**
	 * ContentTypeに設定されている文字コードを取得する.
	 * @param contentType Httpヘッダのコンテンツタイプを設定します.
	 * @return String 設定されている文字コードが返却されます.
	 *                存在しない場合は[null]が返却されます.
	 */
	public static final String contentTypeToCharset(String contentType) {
		int p = Alphabet.indexOf(contentType, ";charset=");
		if (p == -1) {
			p = Alphabet.indexOf(contentType, " charset=");
			if (p == -1) {
				return null;
			}
		}
		int b = p + 9;
		p = contentType.indexOf(";", b);
		if (p == -1) {
			p = contentType.length();
		}
		return contentType.substring(b, p).trim();
	}

	/**
	 * 受信データをBody情報に設定します.
	 * @param tmpBuf 一時的に受け取るバイナリを設定します.
	 * @param element 対象のHttp要素を設定します.
	 * @param contentLength コンテンツ長を設定します.
	 * @param recvBin 今回受信されたバイナリ情報が設定されます.
	 * @return boolean trueの場合、Bodyの受信が完了しました.
	 * @exception IOException I/O例外.
	 */
	public static final boolean receiveBody(
		byte[] tmpBuf, HttpElement element, long contentLength, byte[] recvBin)
		throws IOException {
		HttpElementState state = element.getState();
		// 既に受信終了の場合.
		if(state == HttpElementState.STATE_END_RECV) {
			return true;
		}
		NioRecvBody body;
		HttpReceiveChunked chunked;
		// 受信Bodyデータが作成されていない場合.
		if(state == HttpElementState.STATE_END_RECV_HTTP_HEADER) {
			// chunked受信の場合.
			if(contentLength == -1L) {
				// chunkedにはnio要素のnioBufferを利用する.
				chunked = new HttpReceiveChunked(element.getBuffer());
				element.setReceiveChunked(chunked);
				// nioElementで管理しているnioBufferをクリア.
				element.clearBuffer();
				body = new NioRecvMemBody();
				// cunkedに今回のデータを書き込む.
				if(recvBin != null) {
					chunked.write(recvBin);
					recvBin = null;
				}
				// chunked受信.
				element.setState(state = HttpElementState.STATE_RECV_CHUNKED_BODY);
			// contentLength分受信.
			} else {
				// メモリで受信ができないサイズの場合.
				if(contentLength > HttpConstants.getMaxRecvMemoryBodyLength()) {
					// ファイルでBody受信する.
					body = new NioRecvFileBody();
					/// nioElementのnioBufferを書き込む.
					((NioRecvFileBody)body).write(tmpBuf, element.getBuffer());
					// nioElementで管理しているnioBufferをクリア.
					element.clearBuffer();
				// メモリで受信できるサイズの場合.
				} else {
					// メモリ受信にはnio要素のnioBufferを利用する.
					body = new NioRecvMemBody(element.getBuffer());
					// nioElementで管理しているnioBufferをクリア.
					element.clearBuffer();
				}
				// bodyに今回の受信データを書き込む.
				if(recvBin != null) {
					body.write(recvBin, 0, recvBin.length);
					recvBin = null;
				}
				// contentLength受信.
				element.setState(state = HttpElementState.STATE_RECV_BODY);
			}
			// nioElementにbodyをセット.
			element.setReceiveBody(body);
			chunked = null; body = null;
		}
		// chunked受信の場合.
		if(state == HttpElementState.STATE_RECV_CHUNKED_BODY) {
			// 受信処理.
			chunked = element.getReceiveChunked();
			body = element.getReceiveBody();
			// cunkedに今回のデータを書き込む.
			if(recvBin != null) {
				chunked.write(recvBin);
			}
			// chunkedからBodyデータを読み込む.
			int len = chunked.read(tmpBuf);
			if(len > 0) {
				// bodyに書き込み.
				body.write(tmpBuf, 0, len);
				// メモリ受信の時に受信容量が超えた場合.
				if(body instanceof NioRecvMemBody &&
					body.remaining() > HttpConstants.getMaxRecvMemoryBodyLength()) {
					// メモリ受信からファイル受信に置き換える.
					NioRecvFileBody fbody = new NioRecvFileBody();
					fbody.write(tmpBuf, (NioRecvMemBody)body);
					body.close();
					element.setReceiveBody(fbody);
					body = fbody;
				}
			}
			// 受信が完了した場合.
			if(chunked.isEof()) {
				chunked.clear();
				element.setReceiveChunked(null);
				element.clearBuffer();
				element.setState(state = HttpElementState.STATE_END_RECV);
				body.exitWrite();
			}
			chunked = null; body = null;
		// contentLength分受信.
		} else if(state == HttpElementState.STATE_RECV_BODY) {
			body = element.getReceiveBody();
			// bodyに今回のデータを書き込む.
			if(recvBin != null) {
				body.write(recvBin, 0, recvBin.length);
				recvBin = null;
			}
			// body受信完了の場合.
			if(contentLength == body.remaining()) {
				element.clearBuffer();
				element.setState(state = HttpElementState.STATE_END_RECV);
				body.exitWrite();
			}
			body = null;
		// chunked受信,contentLength受信以外の場合はエラー.
		} else {
			throw new HttpException("Unnatural reception status: " + state.getName());
		}
		// 受信完了の場合はtrue返却.
		return state == HttpElementState.STATE_END_RECV;
	}
}
