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
	private PromiseAction action;

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
		PromiseAction action, PromiseFromEndCall call) {
		this.action = action;
		this.destroyFlag = false;
		this.call = call;
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
			call.call(action);
		} catch(Exception e) {
			// エラーの場合リジェクト.
			action.reject(e);
		}
		return true;
	}
}
