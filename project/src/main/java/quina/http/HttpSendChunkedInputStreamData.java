package quina.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import quina.net.nio.tcp.NioException;
import quina.net.nio.tcp.NioSendData;

/**
 * HttpChunked送信用のInputStream送信
 */
public class HttpSendChunkedInputStreamData implements NioSendData {
	/** InputStream. **/
	private InputStream in = null;
	/** テンポラリバイナリ. **/
	private byte[] tmpBuf = null;
	/** 送信完了フラグ. **/
	private boolean endSendFlag = false;
	/** クローズフラグ. **/
	private boolean closeFlag = false;

	/**
	 * コンストラクタ.
	 * @param in InputStreamを設定します.
	 */
	public HttpSendChunkedInputStreamData(InputStream in) {
		this.in = in;
		this.endSendFlag = false;
		this.closeFlag = false;
	}

	/**
	 * クローズ処理.
	 * @exception IOException
	 */
	@Override
	public void close() throws IOException {
		closeFlag = true;
		if(in != null) {
			try {
				in.close();
			} catch(Exception e) {}
			in = null;
		}
		tmpBuf = null;
		endSendFlag = false;
	}


	/**
	 * 処理前チェック.
	 */
	private void check() {
		if(closeFlag) {
			throw new NioException("It is already closed.");
		}
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
				if(len < 0) {
					endSendFlag = true;
				}
				return len;
			}
			buf.put(tmpBuf, 0, len);
			return len;
		} else {
			return 0;
		}
	}

	/**
	 * NioSendData全体のデータ長を取得.
	 * @return long NioSendData全体のデータ長が返却されます.
	 */
	@Override
	public long length() {
		check();
		try {
			return (long)in.available();
		} catch(Exception e) {
			throw new NioException(e);
		}
	}

	/**
	 * 残りデータ長を取得.
	 * @return long 残りのデータ長が返却されます.
	 */
	@Override
	public long remaining() {
		check();
		return endSendFlag ? 0L : length();
	}

	/**
	 * 残りデータが存在するかチェック.
	 * @return boolean true の場合残りデータがあります.
	 */
	@Override
	public boolean hasRemaining() {
		check();
		return !endSendFlag;
	}

	/**
	 * 情報が空かチェック.
	 * @return boolean true の場合空です.
	 */
	@Override
	public boolean isEmpty() {
		check();
		return endSendFlag;
	}

	@Override
	public String toString() {
		check();
		return new StringBuilder("[ChunkedInputStreamBody]")
			.append(" endSendFlag: ").append(endSendFlag)
			.append(", closeFlag: ").append(closeFlag)
			.toString();
	}

}
