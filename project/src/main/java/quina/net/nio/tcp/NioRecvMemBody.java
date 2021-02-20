package quina.net.nio.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * メモリでのデータ受信処理.
 *
 * メモリ上でデータを管理するので「大きなサイズ」の場合は
 * OutputFileBodyを使ってください.
 */
public class NioRecvMemBody implements NioRecvBody {
	/** Nio受信バッファ. **/
	protected NioBuffer buffer;

	/** 最大データ長. **/
	private long length = -1L;

	/** InputStream. **/
	private InputStream input = null;

	/** exitWriteFlag. **/
	private boolean exitWriteFlag = false;

	/**
	 * コンストラクタ.
	 */
	public NioRecvMemBody() {
		this.buffer = new NioBuffer();
		this.length = -1L;
		this.exitWriteFlag = false;
	}

	/**
	 * コンストラクタ.
	 * @param buf Nio受信バッファを設定します.
	 */
	public NioRecvMemBody(NioBuffer buf) {
		try {
			this.buffer = buf;
			this.length = -1L;
			this.exitWriteFlag = false;
		} catch(NioException ne) {
			throw ne;
		} catch(Exception e) {
			throw new NioException(e);
		}
	}


	/**
	 * オブジェクトのクローズ.
	 * @exception IOException
	 */
	@Override
	public void close() throws IOException {
		exitWrite();
		if(input != null) {
			try {
				input.close();
			} catch(Exception e) {}
			input = null;
		}
		buffer = null;
		length = -1L;
		exitWriteFlag = false;
	}

	// クローズ済みかチェック.
	protected void checkClose() {
		if(buffer == null) {
			throw new NioException("It is already closed.");
		}
	}

	// 書き込みクローズ済みかチェック.
	protected void checkWriteEnd() {
		if(exitWriteFlag) {
			throw new NioException("It is already write closed.");
		}
	}

	/**
	 * binaryで書き込み.
	 * @param bin
	 * @return
	 * @throws IOException
	 */
	@Override
	public int write(byte[] bin)
		throws IOException {
		return write(bin, 0, bin.length);
	}

	/**
	 * binaryで書き込み.
	 * @param bin
	 * @param len
	 * @return
	 * @throws IOException
	 */
	@Override
	public int write(byte[] bin, int len)
		throws IOException {
		return write(bin, 0, len);
	}

	/**
	 * binaryで書き込み.
	 * @param bin
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 */
	@Override
	public int write(byte[] bin, int off, int len)
		throws IOException {
		checkClose();
		checkWriteEnd();
		// 書き込み元のByteBufferデータが存在しない場合.
		if(len <= 0) {
			return 0; // データ書き込み出来ない.
		}
		buffer.write(bin, off, len);
		return len;
	}

	/**
	 * ByteBufferで書き込み.
	 * ByteBufferにはremainingが無い場合は書き込みしません.
	 * @param buf
	 * @return
	 * @throws IOException
	 */
	@Override
	public int write(ByteBuffer buf)
		throws IOException {
		checkClose();
		checkWriteEnd();
		// 書き込み元のByteBufferデータが存在しない場合.
		if(!buf.hasRemaining()) {
			return 0; // データ書き込み出来ない.
		}
		int ret = buf.remaining();
		buffer.write(buf);
		return ret;
	}

	/**
	 * 現在の受信データ長を取得.
	 * @return
	 */
	@Override
	public long remaining() {
		checkClose();
		return (long)buffer.size();
	}

	/**
	 * 受信データが存在するかチェック.
	 * @return boolan true の場合受信情報は存在します.
	 */
	public boolean hasRemaining() {
		checkClose();
		return remaining() != 0L;
	}

	/**
	 * 書き込み処理を終了.
	 */
	@Override
	public void exitWrite() {
		checkClose();
		// 全体の長さを更新.
		length = buffer.size();
		exitWriteFlag = true;
	}

	/**
	 * 書き込み終了処理が行われたかチェック.
	 * @return boolean [true]の場合、書き込みは終了しています.
	 */
	@Override
	public boolean isExitWrite() {
		checkClose();
		return exitWriteFlag;
	}

	/**
	 * 受信される予定のBody取得データ長を取得.
	 * @return long -1Lの場合は[exitWrite()]を呼び出されず長さが確定していません.
	 */
	@Override
	public long getLength() {
		checkClose();
		return length;
	}

	/**
	 * 読み込みInputStreamを作成.
	 * また、このInputStreamはクローズすると自動的に削除されます.
	 * @return InputStream InputStreamが返却されます.
	 * @exception IOException I/O例外.
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		// 書き込みチャネルが閉じていない場合は閉じる.
		if(!exitWriteFlag) {
			exitWrite();
		}
		// 既にInputStream作成済み.
		else if(input != null) {
			// 作成した内容を返却.
			return input;
		}
		// InputStreamを作成して返却.
		input = buffer.getInputStream();
		return input;
	}

	/**
	 * バイナリを取得.
	 * @return byte[] バイナリが返却されます.
	 */
	public byte[] toByteArray() {
		// 書き込みチャネルが閉じていない場合は閉じる.
		if(!exitWriteFlag) {
			exitWrite();
		}
		NioBuffer b = buffer;
		buffer = null;
		return b.toByteArray();
	}
}
