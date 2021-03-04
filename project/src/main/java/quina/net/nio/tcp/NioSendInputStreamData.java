package quina.net.nio.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * InputStreamによるNio送信データ.
 */
public class NioSendInputStreamData extends AbstractNioSendData {
	/** InputStream. **/
	private InputStream in;
	/** テンポラリバイナリ. **/
	private byte[] tmpBuf = null;

	/**
	 * コンストラクタ.
	 * @param in InputStreamを設定します.
	 * @param length データ長を設定します.
	 */
	public NioSendInputStreamData(InputStream in, long length) {
		this.in = in;
		this.length = length;
		this.position = 0;
		this.closeFlag = false;
	}

	/**
	 * クローズ処理.
	 * @exception IOException
	 */
	@Override
	public void close() throws IOException {
		super.close();
		if(in != null) {
			try {
				in.close();
			} catch(Exception e) {}
			in = null;
		}
		tmpBuf = null;
	}

	/**
	 * NioSendDataをコピー.
	 * この処理は「複数先に同じものを送信したい場合」に利用します.
	 * @return NioSendData コピーされたNioSendDataが返却されます.
	 */
	@Override
	public NioSendData copy() {
		check();
		throw new NioException("InputStream transmissions cannot be duplicated.");
	}

	/**
	 * データ取得.
	 * @param buf 対象のByteBufferを設定します.
	 * @return int 読み込まれたデータ数が返却されます.
	 *             -1 の場合EOFに達しました.
	 */
	@Override
	public int read(ByteBuffer buf) {
		check();
		if(!buf.hasRemaining()) {
			return 0;
		}
		if(tmpBuf == null) {
			tmpBuf = new byte[buf.capacity()];
		}
		int sendLen = buf.remaining();
		if(sendLen > 0) {
			int len;
			try {
				len = in.read(tmpBuf, 0, sendLen);
			} catch(Exception e) {
				throw new NioException(e);
			}
			if(len <= 0) {
				return len;
			}
			buf.put(tmpBuf, 0, len);
			position += (long)len;
			return len;
		} else {
			return 0;
		}
	}

	@Override
	public String toString() {
		check();
		return new StringBuilder("[InputStreamBody]")
			.append(" position: ").append(position)
			.append(", length: ").append(length)
			.toString();
	}
}
