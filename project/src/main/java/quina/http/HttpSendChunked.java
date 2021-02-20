package quina.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * chunked 送信処理用InputStream.
 */
public class HttpSendChunked extends InputStream {
	private int bufLen;
	private byte[] buf;
	private InputStream in;
	private int chunkedHeadLength;
	private int position = -1;
	private int limit;
	private boolean endFlag;

	public HttpSendChunked(int bufLen, InputStream in) {
		this.bufLen = bufLen;
		this.chunkedHeadLength = chunkedHeadLength(bufLen);
		this.buf = new byte[bufLen + chunkedHeadLength + (HttpConstants.END_LINE_LENGTH * 2)];
		this.in = (in instanceof BufferedInputStream) ? in : new BufferedInputStream(in);
		this.endFlag = false;
	}

	@Override
	public void close() throws IOException {
		if (in != null) {
			in.close();
			in = null;
		}
		buf = null;
	}

	@Override
	public int read() throws IOException {
		// バッファが存在しない場合取得.
		if (position >= limit || position == -1) {
			limit = in.read(buf, chunkedHeadLength
				+ HttpConstants.END_LINE_LENGTH, bufLen);
			// データが存在する場合.
			if (limit >= 0) {
				// (今回送信分のデータ長[16進数])¥r¥n
				// (body)
				// ¥r¥n
				position = chunkedWrite(buf, limit, chunkedHeadLength);
				limit += chunkedHeadLength + HttpConstants.END_LINE_LENGTH;
				buf[limit++] = HttpConstants.END_LINE[0];
				buf[limit++] = HttpConstants.END_LINE[1];
				// デーtが存在しない場合.
			} else if (endFlag) {
				// chunked(0)が送信された.
				return -1;
			} else {
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
		}
		return buf[position++] & 0x00ff;
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	// chunkedヘッダ長を取得.
	private static final int chunkedHeadLength(int len) {
		int ret = 0;
		while (true) {
			ret ++;
			if ((len = len >> 4) == 0) {
				break;
			}
		}
		return ret;
	}

	// chunked出力.
	private static final int chunkedWrite(byte[] out, int len, int chunkedLength) throws IOException {
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
