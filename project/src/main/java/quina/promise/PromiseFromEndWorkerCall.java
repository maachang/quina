package quina.promise;

import quina.worker.QuinaWorkerCall;
import quina.worker.QuinaWorkerConstants;

/**
 * Promise開始・終了時のコール実行に対するワーカー要素.
 */
final class PromiseFromEndWorkerCall extends QuinaWorkerCall {
	/**
	 * PromiseAction情報.
	 */
	private PromiseActionImpl action;

	/**
	 * 実行情報.
	 */
	private PromiseFromEndCall fromEndCall;

	/**
	 * コンストラクタ.
	 * @param action PromiseAction情報を設定します.
	 * @param call 実行対象のコールオブジェクトを設定します.
	 */
	protected PromiseFromEndWorkerCall(
		PromiseActionImpl action, PromiseFromEndCall call) {
		this.action = action;
		this.fromEndCall = call;
	}

	/**
	 * ワーカー要素用のユニークIDを取得.
	 * @return int ユニークIDを取得します.
	 */
	@Override
	public int getId() {
		return QuinaWorkerConstants.PROMISE_WORKER_CALL_ID;
	}

	/**
	 * 要素を破棄.
	 * @param no 対象のスレッドNoを設定します.
	 */
	@Override
	public void destroy(int no) {
		
	}

	/**
	 * 要素が既に破棄されているかチェック.
	 * @param no 対象のスレッドNoを設定します.
	 * @return boolean [true]の場合は既に破棄されています.
	 */
	@Override
	public boolean isDestroy(int no) {
		return false;
	}

	/**
	 * 対象要素の実行時の呼び出し.
	 * @param no 対象のスレッドNoを設定します.
	 * @return boolean falseの場合実行処理は失敗しました.
	 */
	@Override
	public boolean executeCall(int no) {
		// 実行処理.
		try {
			// コール実行前のresolveやreject
			// 呼び出し累計回数を取得.
			final long count = action.getResolveRejectCount();
			fromEndCall.call(action);
			// resolveやrejectが呼ばれてない場合は
			// 強制的に空のresolve呼び出し.
			if(count == action.getResolveRejectCount()) {
				action.resolve();
			}
		} catch(Exception e) {
			// エラーの場合リジェクト.
			action.reject(e);
		}
		return true;
	}
}
