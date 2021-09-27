package quina;

import quina.exception.QuinaException;
import quina.http.server.HttpServerContext;
import quina.logger.Log;
import quina.logger.LogFactory;
import quina.worker.QuinaWorkerCall;
import quina.worker.QuinaWorkerHandler;

/**
 * 共通のQuinaWorkerハンドル実装.
 * 
 * 基本的にHttpContextに対する対応処理を行います
 */
class QuinaWorkerHandlerImpl
	extends QuinaWorkerHandler {
	
	// ログオブジェクト.
	protected final Log log = LogFactory.getInstance().get();
	
	/**
	 * ワーカーマネージャ初期化時の呼び出し処理。
	 * @param len ワーカースレッド数が設定されます.
	 */
	@Override
	public void initWorkerCall(int len) {
		if(log.isInfoEnabled()) {
			log.info("### initThread(ThreadNo: "
				+ len + ")");
		}
	}

	/**
	 * １つのワーカスレッド開始時の呼び出し.
	 * @param no ワーカースレッドNoが設定されます.
	 */
	@Override
	public void startThreadCall(int no) {
		if(log.isDebugEnabled()) {
			log.debug("### startThread(ThreadNo: "
				+ no + ")");
		}
	}

	/**
	 * １つのワーカスレッド終了時の呼び出し.
	 * @param no ワーカースレッドNoが設定されます.
	 */
	@Override
	public void endThreadCall(int no) {
		if(log.isDebugEnabled()) {
			log.debug("### endThread(ThreadNo: "
				+ no + ")");
		}
	}
	
	// コンテキストをクリア.
	private static final void clearContext(QuinaWorkerCall em) {
		// クリア.
		em.setContext(null);
		HttpServerContext.clear();
	}
	
	/**
	 * Workerに対してPush実行される時の呼び出し.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 */
	@Override
	public void pushCall(int no, QuinaWorkerCall em) {
		// HttpServerContextをワーカーコールにセット.
		HttpServerContext ctx = HttpServerContext.get();
		if(ctx != null) {
			em.setContext(ctx);
			ctx = null;
		}
	}
	
	/**
	 * 対象要素の開始時の呼び出し.
	 * この処理はQuinaWorkerElement開始時に必ず呼び出されます.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 */
	@Override
	public void startCommonCall(int no, QuinaWorkerCall em) {
		// ワーカーコール要素が無効な場合.
		if(em.isDestroy(no)) {
			// Contextクリア.
			clearContext(em);
			return;
		}
		// workerCallからContextを取得.
		HttpServerContext ctx = 
			(HttpServerContext)em.getContext();
		
		// workerCallのコンテキストが存在する場合.
		if(ctx != null) {
			// このスレッドのコンテキストとして設定.
			HttpServerContext.set(ctx);
			// スレッドスコープ開始.
			ctx.startThreadScope();
		}
	}
	
	/**
	 * 対象要素の終了時の呼び出し.
	 * この処理はQuinaWorkerElement終了時に必ず呼び出されます.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 */
	@Override
	public void endCommonCall(int no, QuinaWorkerCall em) {
		// ワーカーコール要素が無効な場合.
		if(em.isDestroy(no)) {
			// Contextクリア.
			clearContext(em);
			return;
		}
		// workerCallに存在するContextを取得.
		HttpServerContext ctx =
			(HttpServerContext)em.getContext();
		// workerCallのコンテキストが存在しないで
		if(ctx == null) {
			// このスレッドのコンテキストを取得.
			HttpServerContext thisCtx =
				HttpServerContext.get();
			// このスレッドのコンテキストが存在する場合.
			if(thisCtx != null) {
				// １つのスレッドスコープを終了.
				thisCtx.exitThreadScope();
			}
		// workerCallのコンテキストが存在する場合.
		} else if(ctx != null) {
			// １つのスレッドスコープを終了.
			ctx.exitThreadScope();
		}
		// このスレッドのコンテキストをクリア.
		HttpServerContext.clear();
	}
	
	/**
	 * 要素を破棄.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 */
	@Override
	public void destroy(int no, QuinaWorkerCall em) {
		throw new QuinaException(
			"Target processing \"destroy\" is not supported.");
	}

	/**
	 * 要素が既に破棄されているかチェック.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 * @return boolean [true]の場合は既に破棄されています.
	 */
	@Override
	public boolean isDestroy(int no, QuinaWorkerCall em) {
		throw new QuinaException(
			"Target processing \"isDestroy\" is not supported.");
	}
	
	/**
	 * 対象要素の開始時の呼び出し.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 */
	@Override
	public void startCall(int no, QuinaWorkerCall em) {
		throw new QuinaException(
			"Target processing \"startCall\" is not supported.");
	}
	
	/**
	 * 対象要素の終了時の呼び出し.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 */
	@Override
	public void endCall(int no, QuinaWorkerCall em) {
		throw new QuinaException(
			"Target processing \"endCall\" is not supported.");
	}
	
	/**
	 * 対象要素の実行時の呼び出し.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 * @return boolean falseの場合実行処理は失敗しました.
	 */
	@Override
	public boolean executeCall(int no, QuinaWorkerCall em) {
		throw new QuinaException(
			"Target processing \"executeCall\" is not supported.");
	}
	
	/**
	 * 対象要素実行時でエラーが発生した場合の呼び出し.
	 * @param no ワーカースレッドNoが設定されます.
	 * @param em QuinaWorkerElementを設定します.
	 * @param t 例外(Throwable)が設定されます.
	 */
	@Override
	public void errorCall(
		int no, QuinaWorkerCall em, Throwable t) {
		em.errorCall(no, t);
	}
}
