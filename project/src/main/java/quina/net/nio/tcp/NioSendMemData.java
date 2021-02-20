package quina.net.nio.tcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Nio送信バイナリデータ.
 */
public class NioSendMemData extends AbstractNioSendData {
	/** 送信Bodyデータ **/
	private byte[] binary;

	/**
	 * コンストラクタ.
	 * @param binary 送信データを設定します.
	 */
	public NioSendMemData(byte[] binary) {
		this(binary, 0, binary.length);
	}

	/**
	 * コンストラクタ.
	 * @param binary 送信データを設定します.
	 * @param length 送信データ長を設定します.
	 */
	public NioSendMemData(byte[] binary, int length) {
		this(binary, 0, length);
	}

	/**
	 * コンストラクタ.
	 * @param binary 送信データを設定します.
	 * @param offset 対象のオフセット値を設定します.
	 * @param length 送信データ長を設定します.
	 */
	public NioSendMemData(byte[] binary, int offset, int length) {
		this.binary = binary;
		this.length = length;
		this.position = offset;
		this.closeFlag = false;
	}

	/**
	 * コンストラクタ.
	 * @param in 送信データを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioSendMemData(InputStream in)
		throws IOException {
		int len;
		final byte[] buf = new byte[512];
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			while((len = in.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
			in.close();
			in = null;
			byte[] o = out.toByteArray();
			out.close();
			out = null;
			this.binary = o;
			this.length = o.length;
			this.position = 0;
			this.closeFlag = false;
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch(Exception e) {}
			}
			if(out != null) {
				try {
					out.close();
				} catch(Exception e) {}
			}
		}
	}

	/**
	 * クローズ処理.
	 * @exception IOException
	 */
	@Override
	public void close() throws IOException {
		super.close();
	}

	/**
	 * 処理前チェック.
	 */
	@Override
	protected void check() {
		super.check();
	}

	/**
	 * NioSendDataをコピー.
	 * この処理は「複数先に同じものを送信したい場合」に利用します.
	 * @return NioSendData コピーされたNioSendDataが返却されます.
	 */
	@Override
	public NioSendData copy() {
		check();
		return new NioSendMemData(binary, 0, (int)length);
	}

	/**
	 * データ取得.
	 * @param buf 対象のByteBufferを設定します.
	 * @return int 読み込まれたデータ数が返却されます.
	 *             -1 の場合EOFに達しました.
	 */
	@Override
	public int read(ByteBuffer buf) {
		if(!buf.hasRemaining()) {
			return 0;
		}
		int sendLen = (int)(length - position);
		sendLen = buf.remaining() > sendLen ? sendLen : buf.remaining();
		if(sendLen > 0) {
			buf.get(binary, (int)position, sendLen);
			position += (long)sendLen;
		} else {
			return -1;
		}
		return sendLen;
	}

	@Override
	public String toString() {
		check();
		return new StringBuilder("[BinaryBody]")
			.append(" position: ").append(position)
			.append(", length: ").append(length)
			.toString();
	}
}
