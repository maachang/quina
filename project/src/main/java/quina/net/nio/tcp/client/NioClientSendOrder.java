package quina.net.nio.tcp.client;

import java.nio.channels.SocketChannel;

import quina.net.nio.tcp.NioException;
import quina.net.nio.tcp.NioSendData;

/**
 * NioClient用送信命令.
 */
public final class NioClientSendOrder {
	private SocketChannel channel;
	private NioSendData[] datas;

	/**
	 * コンストラクタ.
	 * @param channel SocketChannelを設定します.
	 * @param datas 送信データオブジェクトを設定します.
	 */
	public NioClientSendOrder(SocketChannel channel, NioSendData... datas) {
		if(channel == null || !channel.isOpen() || !channel.isBlocking()) {
			throw new NioException("The outbound client socket is not set up correctly.");
		} else if(datas == null || datas.length == 0) {
			throw new NioException("The transmission data is not set correctly.");
		}
		this.channel = channel;
		this.datas = datas;
	}

	/**
	 * 情報破棄.
	 * 管理している情報を全てクローズしてnullクリアします.
	 */
	public void destroy() {
		if(channel != null) {
			try {
				channel.close();
			} catch(Exception e) {

			}
			channel = null;
		}
		if(datas != null) {
			int len = datas.length;
			for(int i = 0; i < len; i ++) {
				try {
					datas[i].close();
				} catch(Exception e) {}
				datas[i] = null;
			}
			datas = null;
		}
	}

	/**
	 * 情報クリア.
	 * 管理情報にnullを設定してクリアします.
	 */
	public void clear() {
		channel = null;
		datas = null;
	}

	/**
	 * SocketChannelを取得.
	 * @return
	 */
	public SocketChannel getChannel() {
		return channel;
	}

	/**
	 * 送信Bodyを取得.
	 * @return
	 */
	public NioSendData[] getDatas() {
		return datas;
	}
}
