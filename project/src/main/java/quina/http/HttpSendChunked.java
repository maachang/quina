package quina.http;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import quina.net.nio.tcp.NioConstants;
import quina.net.nio.tcp.NioException;

/**
 * InputStreamに対するChunked送信対応.
 */
public class HttpSendChunked {
	private InputStream in;
	private int srcBufferLength;
	private byte[] buf;
	private int chunkedHeadLength;
	private int position = -1;
	private int limit = 0;
	private boolean endFlag;

	/** クローズフラグ. **/
	private boolean closeFlag = false;

	/**
	 * コンストラクタ.
	 * @param bufLen １度にチャンク送信する単位を設定します.
	 * @param in InputStreamを設定します.
	 */
	public HttpSendChunked(InputStream in) {
		this(NioConstants.getBufferSize(), in);
	}

	/**
	 * コンストラクタ.
	 * @param bufLen １度にチャンク送信する単位を設定します.
	 * @param in InputStreamを設定します.
	 */
	public HttpSendChunked(int bufLen, InputStream in) {
		if(bufLen < NioConstants.getBufferSize()) {
			bufLen = NioConstants.getBufferSize();
		}
		int cheadLen = chunkedHeadLength(bufLen);
		int endLinesLen = HttpConstants.END_LINE_LENGTH * 2;
		this.chunkedHeadLength = cheadLen;
		this.buf = new byte[bufLen + cheadLen + endLinesLen];
		this.in = (in instanceof FileInputStream) ? new BufferedInputStream(in) : in;
		this.srcBufferLength = bufLen;
		this.endFlag = false;
	}

	/**
	 * クローズ処理.
	 */
	public void close() {
		if (in != null) {
			try {
				in.close();
			} catch(Exception e) {}
			in = null;
		}
		closeFlag = true;
		endFlag = true;
		buf = null;
	}

	/**
	 * 処理前チェック.
	 */
	private void check() {
		if(closeFlag) {
			throw new NioException("It is already closed.");
		}
	}

	// 新しいChunkedデータを作成.
	private int readChunked() {
		try {
			// 読み込みデータを取得.
			if((limit = in.read(buf, chunkedHeadLength
				+ HttpConstants.END_LINE_LENGTH, srcBufferLength)) == 0) {
				// 読み込むデータが無い場合は0バイト読み込みを通知.
				return 0;
			}
		} catch(Exception e) {
			throw new HttpException(e);
		}
		// データが存在する場合.
		if (limit > 0) {
			// (今回送信分のデータ長[16進数])¥r¥n
			// (body)
			// ¥r¥n
			position = chunkedWrite(buf, limit, chunkedHeadLength);
			limit += chunkedHeadLength + HttpConstants.END_LINE_LENGTH;
			buf[limit++] = HttpConstants.END_LINE[0];
			buf[limit++] = HttpConstants.END_LINE[1];
			// データが存在しない場合.
		} else if (endFlag) {
			// chunked(0)が送信された.
			return -1;
		} else if (limit < 0) {
			// chunked(0)が送信されていない.
			endFlag = true;
			position = 0;
			limit = 5;
			buf[0] = (byte) '0';
			buf[1] = HttpConstants.END_LINE[0];
			buf[2] = HttpConstants.END_LINE[1];
			buf[3] = HttpConstants.END_LINE[0];
			buf[4] = HttpConstants.END_LINE[1];
		}
		return limit - position;
	}

	/**
	 * 読み込みデータが読み込み完了しているかチェック.
	 * @return boolean trueの場合、読み込みは完了しています.
	 */
	public boolean isEof() {
		return endFlag;
	}

	/**
	 * 読み込み可能なchunkedデータ長を取得.
	 * @return int 読み込み可能なchunkedデータ長を取得.
	 */
	public int length() {
		check();
		int ret;
		if(isEof()) {
			return 0;
		} else if((ret = remaining()) == 0) {
			readChunked();
			return remaining();
		}
		return ret;
	}

	/**
	 * 読み込みバッファの残りデータ数を取得.
	 * @return int 読み込みバッファの残りデータ数が返却されます.
	 */
	public int remaining() {
		check();
		if(isEof() || limit <= 0) {
			return 0;
		}
		return limit - position;
	}

	/**
	 * 読み込みバッファの残りが存在しないかチェック.
	 * @return
	 */
	public boolean hasRemaining() {
		check();
		return remaining() != 0;
	}

	/**
	 * データ読み込み.
	 * @param out 読み込み先データを設定します.
	 * @return int 読み込まれたデータ長が返却されます.
	 */
	public int read(byte[] out) {
		return read(out, 0, out.length);
	}

	/**
	 * データ読み込み.
	 * @param out 読み込み先データを設定します.
	 * @param off 読み込み先データオフセット値を設定します.
	 * @param len 読み込み先データ長を設定します.
	 * @return int 読み込まれたデータ長が返却されます.
	 */
	public int read(byte[] out, int off, int len) {
		check();
		int rem, readLen;
		int ret = 0;
		// 読み込み先データに情報が設定されるか
		// 読み込み元データが無くなるまで読み取ります.
		while(true) {
			// 現在のバッファが読み込み完了の場合.
			if((rem = remaining()) == 0) {
				// 新しく読み込み処理を行う.
				if((readLen = readChunked()) <= 0) {
					// 読み込み先データに読み込まれたデータが存在する場合.
					if(ret > 0) {
						// これまでの読み込み長を返却.
						return ret;
					}
					// 0の場合は読み込みデータなし
					// -1の場合はEOF.
					return readLen;
				}
				// remainingの情報を再更新.
				continue;
			}
			// バッファの残りの方が読み込み先データ長より多い場合.
			if(rem > len) {
				System.arraycopy(buf, position, out, off, len);
				position += len;
				return ret + len;
			// バッファの残りの方が読み込み先データ長より少ない場合.
			} else {
				System.arraycopy(buf, position, out, off, rem);
				position += rem;
				off += rem;
				len -= rem;
				ret += rem;
				continue;
			}
		}
	}

	// chunkedヘッダ長を取得.
	private static final int chunkedHeadLength(int len) {
		int headLen = len;
		int ret = 0;
		while (true) {
			ret ++;
			headLen = headLen >> 4;
			if (headLen == 0) {
				break;
			}
		}
		return ret;
	}

	// chunkedデータ付与.
	private static final int chunkedWrite(byte[] out, int len, int chunkedLength) {
		int position = chunkedLength - chunkedHeadLength(len);
		int off = position;
		int shift = 0;
		for (; off < chunkedLength; off++, shift += 4) {
			switch ((len & (0x0f << shift)) >> shift) {
			case 0:
				out[off] = (byte) ('0');
				break;
			case 1:
				out[off] = (byte) ('1');
				break;
			case 2:
				out[off] = (byte) ('2');
				break;
			case 3:
				out[off] = (byte) ('3');
				break;
			case 4:
				out[off] = (byte) ('4');
				break;
			case 5:
				out[off] = (byte) ('5');
				break;
			case 6:
				out[off] = (byte) ('6');
				break;
			case 7:
				out[off] = (byte) ('7');
				break;
			case 8:
				out[off] = (byte) ('8');
				break;
			case 9:
				out[off] = (byte) ('9');
				break;
			case 10:
				out[off] = (byte) ('a');
				break;
			case 11:
				out[off] = (byte) ('b');
				break;
			case 12:
				out[off] = (byte) ('c');
				break;
			case 13:
				out[off] = (byte) ('d');
				break;
			case 14:
				out[off] = (byte) ('e');
				break;
			case 15:
				out[off] = (byte) ('f');
				break;
			}
		}
		out[chunkedLength] = HttpConstants.END_LINE[0];
		out[chunkedLength + 1] = HttpConstants.END_LINE[1];
		return position;
	}
}
