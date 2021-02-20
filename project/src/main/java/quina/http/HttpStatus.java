package quina.http;

/**
 * HttpStatus.
 */
public enum HttpStatus {
	/**
	 * リクエスト処理中ステータス.
	 */
	Continue(100, "Continue"),
	/**
	 * サーバによって更新ヘッダのプロトコルが切り替えられました.
	 */
	SwitchingProtocols(101, "Switching Protocols"),

	/**
	 * 要求は正常に終了しました.
	 */
	OK(200, "Ok"),
	/**
	 * 要求は満足され、新規リソースが作成されました.
	 */
	Created(201, "Created"),
	/**
	 * 処理するために要求が受け付けられましたが、その処理は完了していません.
	 */
	Accepted(202, "Accepted"),
	/**
	 * エンティティヘッダに返されたメタ情報は、元のサーバから入手できる完全なセットではありません.
	 */
	NonAuthoritativeInformation(203, "Non-Authoritative Information"),
	/**
	 * サーバは要求を処理しましたが、送り返す新規の情報がありません.
	 */
	NoContent(204, "No Content"),
	/**
	 * 要求は完了しました。クライアント プログラムは要求の送信元であるドキュメント
	 * ビューをリセットして、ユーザが次の入力操作をできるようにする必要があります
	 */
	ResetContent(205, "Reset Content"),
	/**
	 * サーバによってリソースの GET 要求の一部が処理されました.
	 */
	PartialContent(206, "Partial Content"),

	/**
	 * サーバから何を返すか判断できませんでした.
	 */
	MultipleChoices(300, "Multiple Choices"),
	/**
	 * 要求された情報が Location ヘッダで指定される URI に移動したことを示します。
	 * このステータスを受信したときの既定のアクションは、応答に関連付けられている
	 * Location ヘッダの追跡です。元の要求メソッドが POST の場合、リダイレクトされた
	 * 要求は GET メソッドを使用します
	 */
	MovedPermanently(301, "Moved Permanently"),
	/**
	 * 要求された情報が Location ヘッダで指定される URI にあることを示します。
	 * このステータスを受信したときの既定のアクションは、応答に関連付けられている
	 * Location ヘッダの追跡です。元の要求メソッドが POST の場合、リダイレクトされた
	 * 要求は GET メソッドを使用します
	 */
	MovedTemporarily(302, "Moved Temporarily"),
	/**
	 * POST の結果として、Location ヘッダで指定された URI にクライアントを自動的に
	 * リダイレクトします。Location ヘッダで指定されるリソースへの要求は、GET で行います
	 */
	SeeOther(303, "See Other"),
	/**
	 * クライアントのキャッシュされたコピーが最新のものであることを示します。
	 * リソースの内容は転送されません
	 */
	NotModified(304, "Not Modified"),
	/**
	 * 要求が Location ヘッダで指定される URI でプロキシ
	 * サーバを使用する必要があることを示します
	 */
	UseProxy(305, "Use Proxy"),
	/**
	 * 要求された情報が Location ヘッダで指定される URI にあることを示します。
	 * このステータスを受信したときの既定のアクションは、応答に関連付けられている
	 * Location ヘッダの追跡です。元の要求メソッドが POST の場合、リダイレクトされた
	 * 要求も POST メソッドを使用します
	 */
	TemporaryRedirect(307, "Temporary Redirect"),

	/**
	 * 無効な要求です
	 */
	BadRequest(400, "Bad Request"),
	/**
	 * 要求されたリソースには、ユーザの認証が必要です.
	 */
	AuthorizationRequired(401, "Authorization Required"),
	/**
	 * 支払いが必要です
	 */
	PaymentRequired(402, "Payment Required"),
	/**
	 * 要求はサーバで解読されましたが、その処理は拒否されました
	 */
	Forbidden(403, "Forbidden"),
	/**
	 * 要求されたリソースがサーバに存在していないことを示します
	 */
	NotFound(404, "Not Found"),
	/**
	 * 要求メソッド (POST または GET) が要求リソースで許可されていないことを示します
	 */
	MethodNotAllowed(405, "Method Not Allowed"),
	/**
	 * クライアントが Accept ヘッダでリソースの利用可能な任意の表現を受け入れないことを
	 * 指定していることを示します
	 */
	NotAcceptable(406, "Not Acceptable"),
	/**
	 * プロキシによる認証が必要です
	 */
	ProxyAuthenticationRequired(407, "Proxy Authentication Required"),
	/**
	 * 要求待ちでサーバがタイムアウトしました
	 */
	RequestTimeout(408, "Request Time-out"),
	/**
	 * リソースの現在の状態と矛盾するため、要求は完了できませんでした。
	 * 詳しい情報を再度送信する必要があります
	 */
	Conflict(409, "Conflict"),
	/**
	 * 要求されたリソースはサーバにありません。転送先アドレスは不明です
	 */
	Gone(410, "Gone"),
	/**
	 * 内容の長さが定義されていない要求の受け入れをサーバが拒否しました
	 */
	LengthRequired(411, "Length Required"),
	/**
	 * 要求の 1 つ以上のヘッダフィールドにある事前条件がサーバでテストされ、
	 * 不正と判定されました
	 */
	PreconditionFailed(412, "Precondition Failed"),
	/**
	 * 要求が大きすぎて、サーバで処理できないことを示します
	 */
	RequestEntityTooLarge(413, "Request Entity Too Large"),
	/**
	 * 要求された URI が長すぎます
	 */
	RequestUriTooLarge(414, "Request-URI Too Large"),
	/**
	 * サポートされていないメディアの種類です
	 */
	UnsupportedMediaType(415, "Unsupported Media Type"),
	/**
	 * 要求された範囲内にありません
	 */
	RequestedRangeNotSatisfiable(416, "Requested range not satisfiable"),
	/**
	 * サーバが Expectヘッダで指定された要求を満たすことができないことを示します
	 */
	ExpectationFaile(417, "Expectation Faile"),

	/**
	 * サーバで一般的なエラーが発生したことを示します
	 */
	InternalServerError(500, "Internal Server Error"),
	/**
	 * サポートされていないステータスコード
	 */
	NotSupportStatus(500, "Not Support Status"),
	/**
	 * サーバが要求された機能をサポートしていないことを示します
	 */
	NotImplemented(501, "Not Implemented"),
	/**
	 * 中間プロキシサーバが別のプロキシまたは元のサーバから無効な応答を受け取ったことを示します
	 */
	BadGateway(502, "Bad Gateway"),
	/**
	 * 高い負荷または保守のため、サーバを一時的に利用できないことを示します
	 */
	ServiceUnavailable(503, "Service Unavailable"),
	/**
	 * ゲートウェイ待ちで要求がタイムアウトしました
	 */
	GatewayTimeout(504, "Gateway Time-out"),
	/**
	 * サポートされていない HTTP のバージョンです
	 */
	HTTPVersionNotSupported(505, "HTTP Version not supported");

	// Httpステータスコード.
	private int state;
	// メッセージ情報.
	private String message;

	/**
	 * Httpステータスを定義.
	 * @param state Httpステータスコードを設定します.
	 * @param message メッセージ情報を設定します.
	 */
	private HttpStatus(int state, String message) {
		this.state = state;
		this.message = message;
	}

	/**
	 * ステータスコードを取得.
	 * @return int ステータスコードが返却されます.
	 */
	public int getState() {
		return state;
	}

	/**
	 * ステータスメッセージを取得.
	 * @return String ステータスメッセージが返却されます.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * このオブジェクトを文字列として取得.
	 * @return String 文字列が返却されます.
	 */
	@Override
	public String toString() {
		return message;
	}

	/**
	 * HttpStatusを取得.
	 * @param state Httpステータスコードを設定します.
	 * @return HttpStatus HttpStatus列挙体が返却されます.
	 */
	public static final HttpStatus getHttpStatus(int state) {
		switch(state) {
		case 100: return Continue;
		case 101: return SwitchingProtocols;
		case 200: return OK;
		case 201: return Created;
		case 202: return Accepted;
		case 203: return NonAuthoritativeInformation;
		case 204: return NoContent;
		case 205: return ResetContent;
		case 206: return PartialContent;
		case 300: return MultipleChoices;
		case 301: return MovedPermanently;
		case 302: return MovedTemporarily;
		case 303: return SeeOther;
		case 304: return NotModified;
		case 305: return UseProxy;
		case 307: return TemporaryRedirect;
		case 400: return BadRequest;
		case 401: return AuthorizationRequired;
		case 402: return PaymentRequired;
		case 403: return Forbidden;
		case 404: return NotFound;
		case 405: return MethodNotAllowed;
		case 406: return NotAcceptable;
		case 407: return ProxyAuthenticationRequired;
		case 408: return RequestTimeout;
		case 409: return Conflict;
		case 410: return Gone;
		case 411: return LengthRequired;
		case 412: return PreconditionFailed;
		case 413: return RequestEntityTooLarge;
		case 414: return RequestUriTooLarge;
		case 415: return UnsupportedMediaType;
		case 416: return RequestedRangeNotSatisfiable;
		case 417: return ExpectationFaile;
		case 500: return InternalServerError;
		case 501: return NotImplemented;
		case 502: return BadGateway;
		case 503: return ServiceUnavailable;
		case 504: return GatewayTimeout;
		case 505: return HTTPVersionNotSupported;
		}
		return NotSupportStatus;
	}
}
