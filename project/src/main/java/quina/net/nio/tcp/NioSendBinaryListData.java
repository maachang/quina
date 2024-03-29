package quina.net.nio.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * 複数のNio送信バイナリデータ.
 */
public class NioSendBinaryListData extends AbstractNioSendData {

	// １つの送信要素.
	private static final class Element {
		private int pos;
		private int len;
		private byte[] binary;

		/**
		 * コンストラクタ.
		 * @param binary
		 * @param off
		 * @param len
		 */
		public Element(byte[] binary, int off, int len) {
			this.binary = binary;
			this.pos = off;
			this.len = len;
		}

		/**
		 * バイナリデータ読み込み.
		 * @param buf
		 * @return
		 * @throws IOException
		 */
		public int read(ByteBuffer buf) throws IOException {
			int sendLen = (int)(len - pos);
			sendLen = buf.remaining() > sendLen ? sendLen : buf.remaining();
			// 送信データが設定可能な場合.
			if(sendLen > 0) {
				buf.put(binary, pos, sendLen);
				pos += sendLen;
			// 送信データが設定できない場合.
			} else {
				return -1;
			}
			return sendLen;
		}

		/**
		 * 現在のポジション位置を取得.
		 * @return
		 */
		public int getPosition() {
			return pos;
		}

		/**
		 * 対象の長さを取得.
		 * @return
		 */
		public int getLength() {
			return len;
		}

		/**
		 * value内容を文字列変換.
		 * @param buf 対象のStringBuilderを設定します.
		 * @param charset バイナリから文字に変換する文字コードを設定します.
		 */
		public void valueToString(StringBuilder buf, String charset) {
			if(charset == null) {
				charset = "UTF8";
			}
			try {
				buf.append("position: ").append(pos);
				buf.append(" length: ").append(len);
				buf.append(" value: {").append(new String(binary, charset)).append("}");
			} catch(Exception e) {}
		}
	}

	// バイナリ格納リスト.
	private LinkedList<Element> list = new LinkedList<Element>();

	/**
	 * コンストラクタ.
	 */
	public NioSendBinaryListData() {
	}

	/**
	 * コンストラクタ.
	 * @param b
	 */
	public NioSendBinaryListData(byte[] b) {
		this(b, 0, b.length);
	}

	/**
	 * コンストラクタ.
	 * @param b
	 * @param off
	 * @param len
	 */
	public NioSendBinaryListData(byte[] b, int off, int len) {
		final Element em = new Element(b, off, len);
		// 一番最後にデータセット.
		list.offer(em);
		length = (long)len;
	}

	/**
	 * 処理前チェック.
	 */
	@Override
	protected void check() {
		super.check();
	}

	/**
	 * クローズ処理.
	 * @exception IOException
	 */
	@Override
	public void close() throws IOException {
		super.close();
		list.clear();
	}

	/**
	 * NioSendDataをコピー.
	 * この処理は「複数先に同じものを送信したい場合」に利用します.
	 * @return NioSendData コピーされたNioSendDataが返却されます.
	 */
	@Override
	public NioSendData copy() {
		check();
		Element em;
		NioSendBinaryListData ret = new NioSendBinaryListData();
		int len = list.size();
		for(int i = 0; i < len; i ++) {
			em = list.get(i);
			ret.offer(em.binary, em.getPosition(), em.getLength());
		}
		return ret;
	}

	/**
	 * 次に送信するバイナリを設定.
	 * @param b
	 * @return
	 */
	public NioSendBinaryListData offer(byte[] b) {
		return this.offer(b, 0, b.length);
	}

	/**
	 * 次に送信するバイナリを設定.
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	public NioSendBinaryListData offer(byte[] b, int off, int len) {
		check();
		final Element em = new Element(b, off, len);
		// 一番最後にデータセット.
		list.offer(em);
		length += (long)len;
		return this;
	}

	/**
	 * データ取得.
	 * @param buf 対象のByteBufferを設定します.
	 * @return int 読み込まれたデータ数が返却されます.
	 *             -1 の場合EOFに達しました.
	 */
	@Override
	public int read(ByteBuffer buf) throws IOException {
		check();
		// 送信データが存在しない場合.
		if(list.size() == 0) {
			// EOF.
			return -1;
		}
		int rlen;
		Element em;
		int ret = 0;
		// bufが満タンになるか、送信データが存在しなくなるまで
		// 処理する.
		while(buf.hasRemaining()) {
			// 今回の送信データを取得.
			if((em = list.peek()) == null) {
				// 無い場合は処理しない.
				break;
			}
			// データ読み込み.
			rlen = em.read(buf);
			// 今回の送信データがEOFの場合.
			if(rlen == -1) {
				// 今回の送信データを削除.
				list.remove();
			} else {
				// 今回の送信データ長をセット.
				ret += rlen;
			}
		}
		return ret;
	}

	/**
	 * value内容を文字列に変換.
	 * @param charset バイナリから文字変換する文字コードを設定します.
	 * @return String value内容が文字列で返却されます.
	 */
	public String valueToString(String charset) {
		int len = list.size();
		StringBuilder buf = new StringBuilder();
		buf.append("[BinaryListBody]: ").append(len);
		for(int i = 0; i < len; i ++) {
			buf.append("\n");
			list.get(i).valueToString(buf, charset);
		}
		return buf.toString();
	}

	@Override
	public String toString() {
		check();
		return new StringBuilder("[BinaryListBody]")
			.append(" list: ").append(list.size())
			.append(" position: ").append(position)
			.append(", length: ").append(length)
			.toString();
	}
}
