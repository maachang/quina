package quina.promise;

import quina.worker.QuinaWorkerCallHandler;
import quina.worker.QuinaWorkerConstants;

/**
 * PromiseWorkerCallハンドル.
 */
public class PromiseWorkerCallHandler
	extends QuinaWorkerCallHandler {
	
	/**
	 * 紐付けたいQuinaCall.getId()と同じIdを取得.
	 * @return Integer 対象となるQuinaCallのIdが返却されます.
	 */
	@Override
	public Integer targetId() {
		return QuinaWorkerConstants.PROMISE_WORKER_CALL_ID;
	}

}
