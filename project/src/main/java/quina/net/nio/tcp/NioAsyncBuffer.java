package quina.net.nio.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import quina.net.nio.tcp.NioAtomicValues.Bool;
import quina.util.AtomicNumber;

/**
 * Atomicにデータ受信を行うためのNio受信用バッファ.
 */
public class NioAsyncBuffer {
	// データ管理.
	private final Queue<byte[]> buffer = new ConcurrentLinkedQueue<byte[]>();
	// 現在のデータ長.
	private final AtomicNumber bufferLength = new AtomicNumber(0);
	// 現在読み込み中のバイナリ情報.
	private byte[] topBuffer = null;

	// OutputStream利用時のクローズ処理呼び出しフラグ.
	private final Bool isCloseOutputStream = new Bool(false);

	// クローズ処理実行フラグ.
	private final Bool closeFlag = new Bool(false);

	/**
	 * クローズ処理.
	 */
	public void close() {
		isCloseOutputStream.set(true);
		closeFlag.set(true);
		topBuffer = null;
		buffer.clear();
		bufferLength.set(0);
	}

	/**
	 * クローズしたかチェック.
	 * @return boolean trueの場合、クローズしています.
	 */
	public boolean isClose() {
		return closeFlag.get();
	}

	// クローズ確認.
	protected void checkClose() {
		if(closeFlag.get()) {
			throw new NioException("It's already closed.");
		}
	}

	/**
	 * 現在の格納バイナリ長を取得.
	 *
	 * @return
	 */
	public int size() {
		checkClose();
		return bufferLength.get();
	}

	/**
	 * 書き込み処理.
	 *
	 * @param buf
	 */
	public void write(ByteBuffer buf) {
		checkClose();
		final int bufLen = buf.remaining();
		if (bufLen <= 0) {
			return;
		}
		final byte[] bin = new byte[bufLen];
		buf.get(bin);
		buffer.offer(bin);
		bufferLength.add(bin.length);
	}

	/**
	 * 書き込み処理.
	 *
	 * @param b
	 */
	public void write(byte[] b) {
		write(b, 0, b.length);
	}

	/**
	 * 書き込み処理.
	 *
	 * @param b
	 * @param len
	 */
	public void write(byte[] b, int len) {
		write(b, 0, len);
	}

	/**
	 * 書き込み処理.
	 *
	 * @param b
	 * @param off
	 * @param len
	 */
	public void write(byte[] b, int off, int len) {
		checkClose();
		final byte[] bin = new byte[len];
		System.arraycopy(b, off, bin, 0, len);
		buffer.offer(bin);
		bufferLength.add(bin.length);
	}

	/**
	 * 読み込み処理.
	 *
	 * @param b
	 * @return
	 */
	public int read(byte[] b) {
		return read(b, 0, b.length);
	}

	/**
	 * 読み込み処理.
	 *
	 * @param b
	 * @param len
	 * @return
	 */
	public int read(byte[] b, int len) {
		return read(b, 0, len);
	}

	/**
	 * 読み込み処理.
	 *
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	public int read(byte[] b, int off, int len) {
		checkClose();
		int bufLen, etcLen;
		int ret = 0;
		byte[] etcBuf;
		byte[] buf = null;
		// 前回読み込み中のTOPデータが存在する場合.
		buf = topBuffer;
		topBuffer = null;
		if (buf != null) {
			if (len < (bufLen = buf.length)) {
				etcLen = len;
				System.arraycopy(buf, 0, b, off, etcLen);
				etcBuf = new byte[bufLen - etcLen];
				System.arraycopy(buf, etcLen, etcBuf, 0, bufLen - etcLen);
				topBuffer = etcBuf;
				ret = etcLen;
				bufferLength.remove(etcLen);
				return ret;
			}
			System.arraycopy(buf, 0, b, off, bufLen);
			off += bufLen;
			ret = bufLen;
			bufferLength.remove(bufLen);
			if (len <= ret) {
				return ret;
			}
		}
		// 断片化されたバイナリを結合.
		while ((buf = buffer.poll()) != null) {
			if (len < ret + (bufLen = buf.length)) {
				// １つの塊のデータの読み込みに対して、残りが発生する場合.
				// 次の処理で読み込む.
				etcLen = len - ret;
				System.arraycopy(buf, 0, b, off, etcLen);
				etcBuf = new byte[bufLen - etcLen];
				System.arraycopy(buf, etcLen, etcBuf, 0, bufLen - etcLen);
				topBuffer = etcBuf;
				ret += etcLen;
				bufferLength.remove(etcLen);
				return ret;
			}
			System.arraycopy(buf, 0, b, off, bufLen);
			off += bufLen;
			ret += bufLen;
			bufferLength.remove(bufLen);
			if (len <= ret) {
				return ret;
			}
		}
		return ret;
	}

	/**
	 * スキップ.
	 *
	 * @param len
	 * @return
	 */
	public int skip(int len) {
		checkClose();
		int bufLen, etcLen;
		int ret = 0;
		byte[] etcBuf;
		byte[] buf = null;

		// 前回読み込み中のTOPデータが存在する場合.
		buf = topBuffer;
		topBuffer = null;
		if (buf != null) {
			if (len < (bufLen = buf.length)) {
				etcLen = len;
				etcBuf = new byte[bufLen - etcLen];
				System.arraycopy(buf, etcLen, etcBuf, 0, bufLen - etcLen);
				topBuffer = etcBuf;
				ret = etcLen;
				bufferLength.remove(etcLen);
				return ret;
			}
			ret = bufLen;
			bufferLength.remove(bufLen);
			if (len <= ret) {
				return ret;
			}
		}
		// 断片化されたバイナリを結合.
		while ((buf = buffer.poll()) != null) {
			if (len < ret + (bufLen = buf.length)) {
				// １つの塊のデータの読み込みに対して、残りが発生する場合.
				// 次の処理で読み込む.
				etcLen = len - ret;
				etcBuf = new byte[bufLen - etcLen];
				System.arraycopy(buf, etcLen, etcBuf, 0, bufLen - etcLen);
				topBuffer = etcBuf;
				ret += etcLen;
				bufferLength.remove(etcLen);
				return ret;
			}
			ret += bufLen;
			bufferLength.remove(bufLen);
			if (len <= ret) {
				return ret;
			}
		}
		return ret;
	}

	/**
	 * バイナリ検索.
	 *
	 * @param index
	 * @return
	 */
	public int indexOf(byte[] index) {
		return indexOf(index, 0);
	}

	/**
	 * バイナリ検索.
	 *
	 * @param index
	 * @param pos
	 * @return
	 */
	public int indexOf(byte[] index, int pos) {
		checkClose();
		int i, j, bufLen, startPos, off;
		int bp, np;
		int cnt = 0;
		int eqCnt = 0;
		byte[] buf = null;
		Object[] array = buffer.toArray();
		final int len = array.length;
		final byte top = index[0];
		final int indexLen = index.length;
		bp = 0;
		off = 0;
		// posの位置を算出.
		if (pos > 0) {
			// データサイズを超えている場合.
			if (pos >= size()) {
				return -1;
			}
			// topBufferが存在する場合.
			if (topBuffer != null) {
				bp = -1;
				bufLen = topBuffer.length;
				if (bufLen > pos) {
					off = pos;
					cnt = pos;
				} else {
					cnt += bufLen;
					bp = 0;
				}
			}
			// topBufferが存在しないか、topBuffer内の範囲でoffが確定できなかった場合.
			if (off == 0) {
				// posの位置まで移動.
				for (int p = 0; p < len; p++) {
					bufLen = ((byte[]) array[bp]).length;
					if (cnt + bufLen > pos) {
						off = pos - cnt;
						break;
					}
					cnt += bufLen;
					bp++;
				}
				cnt = pos;
			}
		} else if (topBuffer != null) {
			// topBufferが存在する場合開始位置を、-1にする.
			bp = -1;
		}
		// toArrayで配列化された内容を元に、検索.
		for (; bp < len; bp++) {
			buf = (bp == -1) ? topBuffer : (byte[]) array[bp];
			bufLen = buf.length;
			for (i = off; i < bufLen; i++) {
				// 先頭のindex条件が一致.
				if (top == buf[i]) {
					// indexデータ数が１つの場合.
					if (indexLen == 1) {
						return cnt;
					}
					eqCnt = 0;
					startPos = i;

					// 跨ったデータのチェック用.
					for (np = bp; np < len; np++) {
						buf = (np == -1) ? topBuffer : (byte[]) array[np];
						bufLen = buf.length;
						// 一致するかチェックする.
						for (j = startPos; j < bufLen; j++) {
							if (index[eqCnt++] != buf[j]) {
								eqCnt = -1;
								break;
							} else if (eqCnt >= indexLen) {
								break;
							}
						}
						// 不一致.
						if (eqCnt == -1) {
							// 元に戻す.
							buf = (bp == -1) ? topBuffer : (byte[]) array[bp];
							bufLen = buf.length;
							break;
						}
						// 一致.
						else if (eqCnt == indexLen) {
							// 一致条件を返却.
							return cnt;
						}
						// 次のまたがった情報を取得.
						startPos = 0;
					}
				}
				cnt++;
			}
			off = 0;
		}
		return -1;
	}

	/**
	 * InputStreamを取得.
	 * @return InputStream InputStreamが返却されます.
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException {
		return new AsyncBufferInputStream(this);
	}

	/**
	 * OutputStreamを取得.
	 * @return OutputStreamが返却されます.
	 * @throws IOException
	 */
	public OutputStream getOutputStream() throws IOException {
		return new AsyncBufferOutputStream(this);
	}

	// InputStream.
	private static final class AsyncBufferInputStream extends InputStream {
		private NioAsyncBuffer buffer;
		/**
		 * コンストラクタ.
		 * @param buffer 対象のByteArrayBufferを設定します.
		 * @exception IOException I/O例外.
		 */
		public AsyncBufferInputStream(NioAsyncBuffer buffer)
			throws IOException {
			if(buffer == null) {
				throw new NullPointerException();
			} else if(buffer.isClose()) {
				throw new IOException("NioAsyncBuffer is already closed.");
			}
			this.buffer = buffer;
		}

		/**
		 * 情報クローズ.
		 *
		 * @exception IOException
		 *                例外.
		 */
		@Override
		public void close() throws IOException {
			this.buffer = null;
		}

		// クローズ確認.
		private void checkClose() {
			if(this.buffer == null || this.buffer.closeFlag.get()) {
				throw new NioException("It's already closed.");
			}
		}

		// read() 用のバイナリ.
		private final byte[] b1 = new byte[1];

		/**
		 * 情報の取得.
		 *
		 * @return int １バイトのバイナリの内容が返却されます.
		 *             -1 の場合 EOF に達しました.
		 * @exception IOException
		 *                例外.
		 */
		@Override
		public int read() throws IOException {
			checkClose();
			byte[] b = b1;
			int res = this.buffer.read(b, 0, 1);
			if(res <= 0) {
				if(this.buffer.isCloseOutputStream.get()) {
					return -1;
				}
				return 0;
			}
			return (b[0] & 0x000000ff);
		}

		/**
		 * 情報の取得.
		 *
		 * @param buf
		 *            対象のバッファ情報を設定します.
		 * @return int 取得された情報長が返却されます.
		 * @exception IOException
		 *                例外.
		 */
		@Override
		public int read(byte b[]) throws IOException {
			checkClose();
			final int res = this.buffer.read(b, 0, b.length);
			if(res <= 0) {
				if(this.buffer.isCloseOutputStream.get()) {
					return -1;
				}
				return 0;
			}
			return res;
		}

		/**
		 * 情報の取得.
		 *
		 * @param buf
		 *            対象のバッファ情報を設定します.
		 * @param off
		 *            対象のオフセット値を設定します.
		 * @param len
		 *            対象の長さを設定します.
		 * @return int 取得された情報長が返却されます.
		 * @exception IOException
		 *                例外.
		 */
		@Override
		public int read(byte b[], int off, int len)
			throws IOException {
			checkClose();
			final int res = this.buffer.read(b, off, len);
			if(res <= 0) {
				if(this.buffer.isCloseOutputStream.get()) {
					return -1;
				}
				return 0;
			}
			return res;
		}

		/**
		 * データスキップ.
		 *
		 * @parma len スキップするデータ長を設定します.
		 * @return long 実際にスキップされた数が返却されます.
		 *             [-1L]が返却された場合、オブジェクトはクローズしています.
		 */
		@Override
		public long skip(long n) throws IOException {
			checkClose();
			final long res = this.skip(n);
			if(res <= 0L) {
				if(this.buffer.isCloseOutputStream.get()) {
					return -1L;
				}
				return 0L;
			}
			return res;
		}

		/**
		 * 読み取り可能なデータ数を取得.
		 * @return int 読み取り可能データ数が返却されます.
		 */
		@Override
		public int available() throws IOException {
			checkClose();
			return this.buffer.size();
		}
	}

	// OutputStream.
	private static final class AsyncBufferOutputStream extends OutputStream {
		private NioAsyncBuffer buffer;

		/**
		 * コンストラクタ.
		 * @param buffer 対象のByteArrayBufferを設定します.
		 * @exception IOException I/O例外.
		 */
		public AsyncBufferOutputStream(NioAsyncBuffer buffer)
			throws IOException {
			if(buffer == null) {
				throw new NullPointerException();
			} else if(buffer.isClose()) {
				throw new IOException("NioAsyncBuffer is already closed.");
			}
			this.buffer = buffer;
		}

		/**
		 * 情報クローズ.
		 *
		 * @exception IOException
		 *                例外.
		 */
		@Override
		public void close() throws IOException {
			if(this.buffer != null) {
				this.buffer.isCloseOutputStream.set(true);
			}
			this.buffer = null;
		}

		// クローズ確認.
		private void checkClose() {
			if(this.buffer == null || this.buffer.closeFlag.get()) {
				throw new NioException("It's already closed.");
			}
		}

		/**
		 * フラッシュ.
		 *
		 * @exception IOException
		 *                例外.
		 */
		@Override
		public void flush() throws IOException {
			checkClose();
		}

		// read() 用のバイナリ.
		private final byte[] b1 = new byte[1];

		/**
		 * データセット.
		 *
		 * @param b
		 *            対象のバイナリ情報を設定します.
		 * @exception IOException
		 *                例外.
		 */
		@Override
		public void write(int b) throws IOException {
			checkClose();
			b1[0] = (byte)b;
			this.buffer.write(b1);
		}

		/**
		 * データセット.
		 *
		 * @param bin
		 *            対象のバイナリを設定します.
		 * @exception IOException
		 *                例外.
		 */
		@Override
		public void write(byte[] bin) throws IOException {
			checkClose();
			this.buffer.write(bin, 0, bin.length);
		}

		/**
		 * データセット.
		 *
		 * @param bin
		 *            対象のバイナリを設定します.
		 * @param off
		 *            対象のオフセット値を設定します.
		 * @param len
		 *            対象のデータ長を設定します.
		 * @exception IOException
		 *                例外.
		 */
		@Override
		public void write(byte[] bin, int off, int len)
			throws IOException {
			checkClose();
			this.buffer.write(bin, off, len);
		}
	}
}
