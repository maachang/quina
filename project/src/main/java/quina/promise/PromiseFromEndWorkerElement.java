package quina.promise;

import java.io.IOException;

import quina.net.nio.tcp.worker.WorkerElement;

/**
 * Promise開始・終了時のコール実行に対するワーカー要素.
 */
final class PromiseFromEndWorkerElement implements WorkerElement {
	/**
	 * 破棄呼び出しフラグ.
	 */
	private boolean destroyFlag;

	/**
	 * PromiseAction情報.
	 */
	private AbstractPromiseAction<?> action;

	/**
	 * 実行情報.
	 */
	private PromiseFromEndCall call;

	/**
	 * コンストラクタ.
	 * @param action PromiseAction情報を設定します.
	 * @param call 実行対象のコールオブジェクトを設定します.
	 */
	protected PromiseFromEndWorkerElement(
		AbstractPromiseAction<?> action, PromiseFromEndCall call) {
		this.action = action;
		this.call = call;
		this.destroyFlag = false;
	}

	/**
	 * クローズ処理.
	 * @exception IOException IO例外.
	 */
	@Override
	public void close() throws IOException {
		this.action = null;
		this.call = null;
		destroy();
	}

	/**
	 * オブジェクト破棄.
	 */
	@Override
	public void destroy() {
		destroyFlag = true;
	}

	/**
	 * ワーカースレッドでの実行処理.
	 * @param o ワーカーIDが設定されます.
	 * @return trueの場合正常に処理されました.
	 */
	@Override
	public boolean isDestroy() {
		return destroyFlag;
	}

	@Override
	public boolean call(Object o) {
		// 実行処理.
		try {
			// コール実行前のresolveやreject
			// 呼び出し累計回数を取得.
			final long count = action.getResolveRejectCount();
			call.call(action);
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
