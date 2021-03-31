package quina.promise;

/**
 * Promise開始・終了時のコール実行に対するワーカー要素.
 */
final class PromiseFromEndWorker implements PromiseWorkerCall {
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
	protected PromiseFromEndWorker(
		PromiseActionImpl action, PromiseFromEndCall call) {
		this.action = action;
		this.fromEndCall = call;
	}

	/**
	 * ワーカースレッドでの実行処理.
	 */
	@Override
	public void call() {
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
	}
}
