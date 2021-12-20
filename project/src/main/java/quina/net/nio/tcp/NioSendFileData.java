package quina.net.nio.tcp;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import quina.util.AtomicNumber;

/**
 * Nio送信ファイルデータ.
 * 指定ファイルを送信します.
 */
public class NioSendFileData extends AbstractNioSendData {
	/** 送信Bodyデータ **/
	private FileChannel channel;

	/** 送信ファイル名. **/
	private String fileName;

	/** オブジェクトコピーカウント. **/
	private AtomicNumber copyCount;

	/**
	 * コンストラクタ.
	 * @param fileName Body送信対象のファイル名を設定します.
	 * @exception IOException I/O例外.
	 */
	public NioSendFileData(String fileName)
		throws IOException {
		File fp = new File(fileName);
		init(false, fp, fp.length());
	}

	/**
	 * コンストラクタ.
	 * @param fileName Body送信対象のファイル名を設定します.
	 * @param length 対象のファイルサイズを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioSendFileData(String fileName, long length)
		throws IOException {
		init(false, new File(fileName), length);
	}

	/**
	 * コンストラクタ.
	 * @param fileName Body送信対象のファイル名を設定します.
	 * @exception IOException I/O例外.
	 */
	public NioSendFileData(File file)
		throws IOException {
		init(false, file, file.length());
	}

	/**
	 * コンストラクタ.
	 * @param fileName Body送信対象のファイル名を設定します.
	 * @exception IOException I/O例外.
	 */
	public NioSendFileData(boolean endToDeleteFlag, String fileName)
		throws IOException {
		File fp = new File(fileName);
		init(endToDeleteFlag, fp, fp.length());
	}

	/**
	 * コンストラクタ.
	 * @param fileName Body送信対象のファイル名を設定します.
	 * @param length 対象のファイルサイズを設定します.
	 * @exception IOException I/O例外.
	 */
	public NioSendFileData(boolean endToDeleteFlag, String fileName, long length)
		throws IOException {
		init(endToDeleteFlag, new File(fileName), length);
	}

	/**
	 * コンストラクタ.
	 * @param fileName Body送信対象のファイル名を設定します.
	 * @exception IOException I/O例外.
	 */
	public NioSendFileData(boolean endToDeleteFlag, File file)
		throws IOException {
		init(endToDeleteFlag, file, file.length());
	}

	/**
	 * コンストラクタ.
	 * 元のオブジェクトをコピーする場合はこの処理を呼び出します.
	 *
	 * @param length 対象のデータ長を設定します.
	 * @param fileName 対象のファイル名を設定します.
	 * @exception IOException I/O例外.
	 */
	private NioSendFileData(long length, String fileName, AtomicNumber copyCount)
		throws IOException {
		this.fileName = fileName;
		this.length = length;
		this.position = 0L;
		this.closeFlag = false;
		this.copyCount = copyCount;
		this.channel = openChannel(fileName);

		// コピーカウントを１インクリメント.
		if(copyCount != null) {
			this.copyCount.inc();
		}
	}

	/**
	 * 初期処理.
	 * @param fileName Body送信対象のファイル名を設定します.
	 * @param length 対象のファイルサイズを設定します.
	 * @exception IOException I/O例外.
	 */
	private final void init(boolean endToDeleteFlag, File file, long length)
		throws IOException {
		check();
		if(!file.exists() || !file.canRead()) {
			throw new IOException("The specified file'"
				+ fileName + "'does not exist or you do not have read permission.");
		}
		String fpath = file.getCanonicalPath();
		this.fileName = fpath;
		this.length = length;
		this.position = 0L;
		this.closeFlag = false;
		this.channel = openChannel(fpath);
		this.copyCount = endToDeleteFlag ? new AtomicNumber(1) : null;
	}

	/** チャネルをオープンして読み込み可能にする. **/
	private final FileChannel openChannel(String fpath) throws IOException {
		return FileChannel.open(Paths.get(fpath), StandardOpenOption.READ);
	}

	/**
	 * クローズ処理.
	 * @exception IOException
	 */
	@Override
	public void close() throws IOException {
		super.close();
		if(channel != null) {
			try {
				channel.close();
			} catch(Exception e) {}
			channel = null;
			// 処理後にファイルを削除する場合.
			if(copyCount != null) {
				// copyCountを１デクリメント.
				if(copyCount.dec() <= 0) {
					// コピーオブジェクトを含めた物全てがクローズ
					// された場合は、一時ファイルを削除する.
					deleteFile();
				}
			}
		}
	}

	// ファイル削除処理.
	private final void deleteFile() {
		// 本来は DELETE_ON_CLOSE を利用するが
		// この送信処理系は「複数送信先」が想定されるので
		// 手動で削除するように変更.
		for(int i = 0; i < NioConstants.DELETE_RETRY; i ++) {
			try {
				// クローズ後削除処理.
				if(Files.deleteIfExists(Paths.get(fileName))) {
					return;
				}
			} catch(Exception e) {}
			try {
				Thread.sleep(5L);
			} catch(Exception e) {}
		}
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
		try {
			return new NioSendFileData(length, fileName, copyCount);
		} catch(Exception e) {
			throw new NioException(e);
		}
	}

	/**
	 * データ取得.
	 * @param buf 対象のByteBufferを設定します.
	 * @return int 読み込まれたデータ数が返却されます.
	 *             -1 の場合EOFに達しました.
	 * @exception IOException I/O例外.
	 */
	@Override
	public int read(ByteBuffer buf) throws IOException {
		check();
		if(!buf.hasRemaining()) {
			return 0;
		}
		// ファイル内容を読み込む.
		final int readLen = channel.read(buf);
		// EOF.
		if(readLen <= 0) {
			return -1;
		}
		position += (long)readLen;
		return (int)readLen;
	}

	/**
	 * ファイル名を取得.
	 * @return String ファイル名が返却されます.
	 */
	public String getFileName() {
		check();
		return fileName;
	}

	/**
	 * 送信終了後にファイルを削除するかチェック.
	 * @return boolean trueの場合は削除されます.
	 */
	public boolean isEndToDelete() {
		check();
		return copyCount != null;
	}

	@Override
	public String toString() {
		check();
		return new StringBuilder("[FileBody]")
			.append(" fileName: \"").append(fileName).append("\"")
			.append(", position: ").append(position)
			.append(", length: ").append(length)
			.append(", isEndToDelete: ").append(isEndToDelete())
			.toString();
	}
}
