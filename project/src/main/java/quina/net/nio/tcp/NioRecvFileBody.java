package quina.net.nio.tcp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 大きなデータの受信処理.
 *
 * 大きなデータは一旦テンポラリファイルを作成してデータ受信します.
 * また、この一時ファイルはクローズ後に自動的に削除されます.
 */
public class NioRecvFileBody implements NioRecvBody {
	// 先頭ランダムファイル名
	private static final String NAME_HEAD = "swrcv-";

	// ランダムファイル名拡張子.
	private static final String NAME_EXTENSION = ".tmp";

	/** ディレクトリ名. **/
	private String directoryName = null;

	/** ファイル名. **/
	private String fileName = null;

	/** フルパス名. **/
	private String fullPath = null;

	/** 現在書き込み中のファイルサイズ. **/
	private long[] nowFileSize = new long[] {0L};

	/** 最大書き込みサイズ. **/
	private long length = -1L;

	/** 書き込み用ByteBuffer. **/
	private ByteBuffer writeBuffer = null;

	/** 書き込み専用ファイルチャネル. **/
	private FileChannel channel = null;

	/** InputStream. **/
	private InputStream input = null;

	/**
	 * コンストラクタ.
	 * @exception IOException I/O例外.
	 */
	public NioRecvFileBody()
		throws IOException {
		this(NioConstants.getTcpBodyFileDir(), NioRand.get(),
			NioConstants.getTempFileNameLength());
	}

	/**
	 * コンストラクタ.
	 * @param baseDir データを格納するディレクトリ名を設定します.
	 * @exception IOException I/O例外.
	 */
	public NioRecvFileBody(String baseDir)
		throws IOException {
		this(baseDir, NioRand.get(),
			NioConstants.getTempFileNameLength());
	}

	/**
	 * コンストラクタ.
	 * @param randNameLength テンポラリファイル名を生成する長さを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioRecvFileBody(int randNameLength)
		throws IOException {
		this(NioConstants.getTcpBodyFileDir(), NioRand.get(),
			randNameLength);
	}

	/**
	 * コンストラクタ.
	 * @param baseDir データを格納するディレクトリ名を設定します.
	 * @param randNameLength テンポラリファイル名を生成する長さを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioRecvFileBody(String baseDir, int randNameLength)
		throws IOException {
		this(NioConstants.getTcpBodyFileDir(), NioRand.get(),
			randNameLength);
	}

	/**
	 * コンストラクタ.
	 * @param rand テンポラリファイル名を生成するランダムオブジェクトを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioRecvFileBody(NioRand rand)
		throws IOException {
		this(NioConstants.getTcpBodyFileDir(), rand,
			NioConstants.getTempFileNameLength());
	}

	/**
	 * コンストラクタ.
	 * @param baseDir データを格納するディレクトリ名を設定します.
	 * @param rand テンポラリファイル名を生成するランダムオブジェクトを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioRecvFileBody(String baseDir, NioRand rand)
		throws IOException {
		this(baseDir, rand, NioConstants.getTempFileNameLength());
	}

	/**
	 * コンストラクタ.
	 * @param rand テンポラリファイル名を生成するランダムオブジェクトを設定します.
	 * @param randNameLength テンポラリファイル名を生成する長さを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioRecvFileBody(NioRand rand, int randNameLength)
		throws IOException {
		this(NioConstants.getTcpBodyFileDir(), rand, randNameLength);
	}

	/**
	 * コンストラクタ.
	 * @param baseDir データを格納するディレクトリ名を設定します.
	 * @param rand テンポラリファイル名を生成するランダムオブジェクトを設定します.
	 * @param randNameLength テンポラリファイル名を生成する長さを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioRecvFileBody(String baseDir, NioRand rand, int randNameLength)
		throws IOException {
		// テンポラリ名の長さをチェック.
		if(randNameLength < NioConstants.MIN_TMP_NAME_COUNT) {
			randNameLength = NioConstants.MIN_TMP_NAME_COUNT;
		}
		// ディレクトリ名の最後にセパレータが無い場合はセット.
		if (!baseDir.endsWith(File.pathSeparator)) {
			baseDir += File.pathSeparator;
		}
		try {
			// 指定ディレクトリに一意のファイルを作成.
			String name = NioUtil.createRandomFile(
				rand, baseDir, NAME_HEAD, NAME_EXTENSION, randNameLength);
			// フルパス取得.
			String fpath = baseDir + name + NAME_EXTENSION;
			// 対象の書き込み用ファイルチャネルを作成.
			FileChannel ch = FileChannel.open(
				Paths.get(fpath),
				StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			// メンバー変数に設定.
			directoryName = baseDir;
			fileName = name;
			fullPath = fpath;
			nowFileSize[0] = 0L;
			length = -1L;
			channel = ch;
			writeBuffer = ByteBuffer.allocateDirect(NioConstants.getByteBufferLength());
			input = null;
		} catch (Exception e) {
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
		if(nowFileSize[0] != 0L) {
			// 書き込みファイルが存在する場合ファイル削除.
			try {
				Files.deleteIfExists(Paths.get(fullPath));
			} catch(Exception e) {}
		}
		directoryName = null;
		fileName = null;
		nowFileSize[0] = 0L;
		length = -1L;
	}

	// クローズ済みかチェック.
	protected void checkClose() {
		if(fileName == null) {
			throw new NioException("It is already closed.");
		}
	}

	// 書き込みクローズ済みかチェック.
	protected void checkWriteEnd() {
		if(channel == null) {
			throw new NioException("It is already write closed.");
		}
	}

	/**
	 * 書き込み処理.
	 * @param tmpBuf
	 * @param rbuf
	 * @return
	 * @throws IOException
	 */
	public int write(byte[] tmpBuf, NioBuffer rbuf) throws IOException {
		checkWrite();
		if(tmpBuf == null) {
			tmpBuf = new byte[writeBuffer.capacity()];
		}
		int len;
		int ret = 0;
		while((len = rbuf.read(tmpBuf)) != 0) {
			ret += _write(tmpBuf, 0, len);
		}
		nowFileSize[0] += (long)ret;
		return ret;
	}

	/**
	 * 書き込み処理.
	 * @param tmpBuf
	 * @param buf
	 * @return
	 * @throws IOException
	 */
	public int write(byte[] tmpBuf, NioRecvMemBody body) throws IOException {
		checkWrite();
		if(tmpBuf == null) {
			tmpBuf = new byte[writeBuffer.capacity()];
		}
		int len;
		int ret = 0;
		NioBuffer bbuf = body.buffer;
		while((len = bbuf.read(tmpBuf)) != 0) {
			ret += _write(tmpBuf, 0, len);
		}
		nowFileSize[0] += (long)ret;
		return ret;
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
		checkWrite();
		return _write(bin, off, len);
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
		checkWrite();
		// 書き込み元のByteBufferデータが存在しない場合.
		if(!buf.hasRemaining()) {
			return 0; // データ書き込み出来ない.
		}
		return _write(buf);
	}

	// 書き込みチェック.
	private final void checkWrite()
		throws IOException {
		checkClose();
		checkWriteEnd();
		// 書き込み先のチャネルがクローズ.
		if(channel == null) {
			throw new IOException("The write object is already closed.");
		}
	}

	// １つのバイナリを書き込み.
	private final int _write(byte[] b, int off, int len)
		throws IOException {
		int wlen;
		int ret = 0;
		final int wcap = writeBuffer.capacity();
		while(off < len) {
			wlen = len - off;
			if(wlen > wcap) {
				wlen = wcap;
			}
			writeBuffer.clear();
			writeBuffer.put(b, off, wlen);
			writeBuffer.flip();
			wlen = _write(writeBuffer);
			off += wlen;
			ret += wlen;
		}
		return ret;
	}

	// 1つのByteBufferを書き込み.
	private final int _write(ByteBuffer buf)
		throws IOException {
		int ret = channel.write(buf);
		nowFileSize[0] += (long)ret;
		return ret;
	}

	/**
	 * 現在の受信データ長を取得.
	 * @return
	 */
	@Override
	public long remaining() {
		checkClose();
		return nowFileSize[0];
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
		if(channel != null) {
			try {
				channel.close();
			} catch(Exception e) {}
			channel = null;
			writeBuffer = null;
			length = nowFileSize[0];
		}
	}

	/**
	 * 書き込み終了処理が行われたかチェック.
	 * @return boolean [true]の場合、書き込みは終了しています.
	 */
	@Override
	public boolean isExitWrite() {
		checkClose();
		return channel == null;
	}

	/**
	 * ディレクトリ名を取得.
	 * @return String ディレクトリ名が返却されます.
	 */
	public String directoryName() {
		checkClose();
		return directoryName;
	}

	/**
	 * ファイル名を取得.
	 * @return String ファイル名が返却されます.
	 */
	public String fileName() {
		checkClose();
		return fileName + NAME_EXTENSION;
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
		if(channel != null) {
			exitWrite();
		}
		// 既にInputStream作成済み.
		else if(input != null) {
			// 作成した内容を返却.
			return input;
		}
		// InputStreamを作成して返却.
		InputStream in = Files.newInputStream(
			Paths.get(fullPath),
			StandardOpenOption.DELETE_ON_CLOSE);
		input = new NioFileBodyInputStream(
			new BufferedInputStream(in), nowFileSize);
		return input;
	}

	// NioRecvFileBody用のInputStream.
	private static final class NioFileBodyInputStream extends InputStream {
		private InputStream in;
		private long[] nowFileSize;
		protected NioFileBodyInputStream(InputStream in, long[] nowFileSize)
			throws IOException {
			if(in == null) {
				throw new NullPointerException();
			}
			this.in = in;
			this.nowFileSize = nowFileSize;
		}
		public void close() throws IOException {
			if(in != null) {
				in.close();
				in = null;
			}
			nowFileSize[0] = 0L;
		}
		private void checkClose() throws IOException {
			if(isClose()) {
				throw new IOException("It's already closed.");
			}
		}
		public boolean isClose() {
			return this.in == null;
		}
		public int read() throws IOException {
			checkClose();
			int ret = in.read();
			if(ret != -1) {
				nowFileSize[0] --;
			}
			return ret;
		}
		public int read(byte b[]) throws IOException {
			return read(b, 0, b.length);
		}
		public int read(byte b[], int off, int len)
			throws IOException {
			int ret = in.read(b, off, len);
			if(ret != -1) {
				nowFileSize[0] -= ret;
			}
			return ret;
		}
		public long skip(long n) throws IOException {
			checkClose();
			long ret = in.skip(n);
			if(ret > 0L) {
				nowFileSize[0] -= ret;
			}
			return ret;
		}
		public int available() throws IOException {
			checkClose();
			return in.available();
		}
	}
}
