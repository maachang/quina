package quina.net.nio.tcp;

import java.io.IOException;

/**
 * NioSendData基礎実装.
 */
abstract class AbstractNioSendData implements NioSendData {
	/** 送信データ長. **/
	protected long length;

	/** データポジション. **/
	protected long position;

	/** クローズフラグ. **/
	protected boolean closeFlag;

	/**
	 * クローズ処理.
	 * @exception IOException
	 */
	@Override
	public void close() throws IOException {
		closeFlag = true;
	}

	/**
	 * 処理前チェック.
	 */
	protected void check() {
		if(closeFlag) {
			throw new NioException("It is already closed.");
		}
	}

	/**
	 * NioSendData全体のデータ長を取得.
	 * @return long NioSendData全体のデータ長が返却されます.
	 */
	@Override
	public long length() {
		check();
		return length;
	}

	/**
	 * 残りデータ長を取得.
	 * @return long 残りのデータ長が返却されます.
	 */
	@Override
	public long remaining() {
		check();
		return length - position;
	}

	/**
	 * 残りデータが存在するかチェック.
	 * @return boolean true の場合残りデータがあります.
	 */
	@Override
	public boolean hasRemaining() {
		check();
		return remaining() > 0L;
	}

	/**
	 * 情報が空かチェック.
	 * @return boolean true の場合空です.
	 */
	@Override
	public boolean isEmpty() {
		check();
		return remaining() == 0L;
	}

	/**
	 * 送信完了かチェック.
	 * @return boolean true の場合送信完了です.
	 */
	@Override
	public boolean isSendEnd() {
		check();
		return remaining() == 0L;
	}
}
