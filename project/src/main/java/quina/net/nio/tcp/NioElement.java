package quina.net.nio.tcp;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import quina.net.nio.tcp.NioAtomicValues.Bool;
import quina.net.nio.tcp.NioAtomicValues.Number32;
import quina.net.nio.tcp.NioAtomicValues.Number64;
import quina.util.Flag;

/**
 * Nio要素.
 */
public class NioElement implements Closeable {
	/**
	 * 割り当てられてないワーカースレッド.
	 */
	public static final int NON_WORKER_NO = -1;
	
	// コネクションフラグ.
	protected final Flag connectionFlag = new Flag(false);
	protected final Number32 ops = new Number32(SelectionKey.OP_READ);
	protected final Number32 workerNo = new Number32(NON_WORKER_NO);
	protected final Bool regIoTimeout = new Bool(false);
	protected final Number64 ioTime = new Number64(
		System.currentTimeMillis());
	protected final Bool sendFlag = new Bool(false);
	protected NioSelector selector;
	protected SelectionKey key;
	protected InetSocketAddress access;
	protected NioBuffer buffer = null;
	protected LinkedList<NioSendData> sendDataList = null;
	protected NioSendLess less = null;

	protected Object object = null;

	/**
	 * コンストラクタ.
	 */
	public NioElement() {
	}

	/**
	 * クローズ処理.
	 * @exception IOException
	 */
	@Override
	public void close() throws IOException {
		connectionFlag.set(false);
		selector = null;
		access = null;
		if (less != null) {
			less.clear();
			less = null;
		}
		NioSendData in;
		if(sendDataList != null) {
			while (!sendDataList.isEmpty()) {
				in = sendDataList.pop();
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
		if (buffer != null) {
			buffer.clear();
			buffer = null;
		}
		if (key != null) {
			key.attach(null);
			NioUtil.destroyKey(key);
			key = null;
		}
		if(object != null) {
			// Closeableオブジェクトの場合.
			if(object instanceof Closeable) {
				try {
					((Closeable)object).close();
				} catch(Exception e) {}
			// AuthCloseableオブジェクトの場合.
			} else if(object instanceof AutoCloseable) {
				try {
					((AutoCloseable)object).close();
				} catch(Exception e) {}
			}
			object = null;
		}
	}

	/**
	 * 要素が有効かチェック.
	 *
	 * @return boolean [true]の場合、接続中です.
	 */
	public boolean isConnection() {
		return connectionFlag.get();
	}

	/**
	 * 対象要素と、対象Socket情報を、セレクタに登録.
	 *
	 * @param sc 登録先のセレクタを設定します.
	 * @param ch 対象のソケットチャネルを設定します.
	 * @param op 対象の処理モードを設定します.
	 * @return SelectionKey 生成されたSelectionKeyを返却します.
	 * @exception Exception 例外.
	 */
	public SelectionKey registor(NioSelector sc, SocketChannel ch, int op)
		throws Exception {
		if(!this.connectionFlag.setToGetBefore(true)) {
			SelectionKey ret = sc.register(ch, op, this);
			this.key = ret;
			this.selector = sc;
			this.access = (InetSocketAddress) ch.getRemoteAddress();
			return ret;
		}
		return null;
	}

	/**
	 * SelectedKeyを取得.
	 *
	 * @return SelectionKey SelectionKeyが返却されます.
	 */
	public SelectionKey getKey() {
		return key;
	}

	/**
	 * Selectorを取得.
	 *
	 * @return NioSelector Selectorが返却されます.
	 */
	public NioSelector getSelector() {
		return selector;
	}

	/**
	 * Nioバッファをクリア.
	 */
	public void clearBuffer() {
		buffer = null;
	}

	/**
	 * Nioバッファにデータが存在するかチェック.
	 * @return boolean [true]の場合、Nioバッファにデータは存在します.
	 */
	public boolean isBuffer() {
		return buffer != null && !buffer.isEmpty();
	}

	/**
	 * Nioバッファを取得.
	 *
	 * @return HttpReadBuffer Nioバッファが返却されます.
	 */
	public NioBuffer getBuffer() {
		if(buffer == null) {
			buffer = new NioBuffer();
		}
		return buffer;
	}

	/**
	 * SendLessオブジェクトを取得.
	 *
	 * @return SendLess オブジェクトが返却されます.
	 */
	public NioSendLess getSendLess() {
		if(less == null) {
			less = new NioSendLess();
		}
		return less;
	}

	/**
	 * 書き込み開始を行う.
	 *
	 * @throws IOException
	 */
	public NioElement startWrite() throws IOException {
		this.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		return this;
	}

	/**
	 * SendDataオブジェクトを設定.
	 *
	 * @param in
	 *            対象の送信データを設定します.
	 */
	public NioElement setSendData(NioSendData... in) {
		final int len = in == null ? 0 : in.length;
		if(len > 0) {
			sendFlag.set(true);
			if(sendDataList == null) {
				sendDataList = new LinkedList<NioSendData>();
			}
			for(int i = 0; i < len; i ++) {
				sendDataList.offer(in[i]);
			}
		}
		return this;
	}

	/**
	 * 送信データが存在するかチェック.
	 * @return boolean [true]の場合、送信データが存在します.
	 */
	public boolean isSendData() {
		//return sendDataList != null && !sendDataList.isEmpty();
		return sendFlag.get();
	}

	/**
	 * SendDataオブジェクトを取得.
	 *
	 * @return NioSendData オブジェクトが返却されます.
	 */
	public NioSendData getSendData() {
		if (sendDataList != null && !sendDataList.isEmpty()) {
			return sendDataList.peek();
		}
		return null;
	}

	/**
	 * 現在のSendDataオブジェクトを取得して削除.
	 *
	 * @return NioSendData オブジェクトが返却されます.
	 */
	public NioSendData removeSendData() {
		if (sendDataList != null && !sendDataList.isEmpty()) {
			NioSendData ret = sendDataList.pop();
			if(sendDataList.isEmpty()) {
				sendDataList = null;
			}
			return ret;
		}
		return null;
	}
	
	/**
	 * IoTimeout監視を登録.
	 * @return boolean true の場合既に登録されています.
	 */
	public boolean regIoTimeout() {
		return regIoTimeout.setToGetBefore(true);
	}
	
	/**
	 * IoTimeout監視が登録されてるか取得.
	 * @return boolean true の場合既に登録されています.
	 */
	public boolean isRegIoTimeout() {
		return regIoTimeout.get();
	}
	
	/**
	 * I/Oタイムアウト値を設定.
	 */
	public void setIoTimeout() {
		ioTime.set(System.currentTimeMillis());
	}
	
	/**
	 * I/Oタイムアウト値を取得.
	 * @return long I/Oタイムアウト値を取得.
	 */
	public long getIoTimeout() {
		return ioTime.get();
	}
	
	/**
	 * interOpsの変更.
	 *
	 * @param ops
	 *            対象のOpsを設定します.
	 * @exception IOException
	 *                I/O例外.
	 */
	public NioElement interestOps(int ops)
		throws IOException {
		this.ops.set(ops);
		key.interestOps(ops);
		selector.wakeup();
		return this;
	}

	/**
	 * 現在のinterOpsを取得.
	 *
	 * @return int 対象のOpsが返却されます.
	 */
	public int interestOps() {
		return ops.get();
	}

	/**
	 * アクセス先の接続情報を取得.
	 *
	 * @return InetSocketAddress リモートアクセス情報が返却されます.
	 */
	public InetSocketAddress getRemoteAddress() {
		return access;
	}

	/**
	 * ワーカースレッドの番号を取得.
	 * @return int -1の場合は未決定です.
	 */
	public int getWorkerNo() {
		return workerNo.get();
	}

	/**
	 * ワーカースレッドの番号を設定.
	 * @param no ワーカースレッドの番号が設定されます.
	 * @return
	 */
	public NioElement setWorkerNo(int no) {
		workerNo.set(no);
		return this;
	}
	
	/**
	 * オブジェクトを設定.
	 *
	 * このオブジェクトの属性が Closeable か AutoCloseable の場合
	 * NioElementがクローズ処理の時に、同時にクローズ処理を行います.
	 * @param o
	 * @return
	 */
	public NioElement attach(Object o) {
		object = o;
		return this;
	}

	/**
	 * オブジェクトの取得.
	 * @return
	 */
	public Object attachment() {
		return object;
	}
}
