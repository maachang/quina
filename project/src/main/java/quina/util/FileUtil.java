package quina.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import quina.exception.QuinaException;

/**
 * ファイルユーティリティ.
 */
public final class FileUtil {
	protected FileUtil() {
	}

	// ファイル時間を取得.
	private static final long _getFileTime(
		int type, String name) throws Exception {
		File fp = new File(name);
		if (fp.exists()) {
			BasicFileAttributes attrs = Files.readAttributes(
				Paths.get(name), BasicFileAttributes.class);
			switch (type) {
			// ファイル作成時間.
			case 0:
				return attrs.creationTime().toMillis();
			// ファイル最終アクセス時間.
			case 1:
				return attrs.lastAccessTime().toMillis();
			// ファイル最終更新時間.
			case 2:
				return attrs.lastModifiedTime().toMillis();
			}
		}
		return -1;
	}

	/**
	 * ファイル名の存在チェック.
	 *
	 * @param name
	 *            対象のファイル名を設定します.
	 * @return boolean [true]の場合、ファイルは存在します.
	 */
	public static final boolean isFile(String name) {
		File file = new File(name);
		return (file.exists() && !file.isDirectory());
	}

	/**
	 * ディレクトリ名の存在チェック.
	 *
	 * @param name
	 *            対象のディレクトリ名を設定します.
	 * @return boolean [true]の場合、ディレクトリは存在します.
	 */
	public static final boolean isDir(String name) {
		File file = new File(name);
		return (file.exists() && file.isDirectory());
	}

	/**
	 * 指定情報が読み込み可能かチェック.
	 *
	 * @param name
	 *            対象のファイル／ディレクトリ名を設定します.
	 * @return boolean [true]の場合、読み込み可能です.
	 */
	public static final boolean isRead(String name) {
		File file = new File(name);
		return (file.exists() && file.canRead());
	}

	/**
	 * 指定情報が書き込み可能かチェック.
	 *
	 * @param name
	 *            対象のファイル／ディレクトリ名を設定します.
	 * @return boolean [true]の場合、書き込み可能です.
	 */
	public static final boolean isWrite(String name) {
		File file = new File(name);
		return (file.exists() && file.canWrite());
	}

	/**
	 * 指定情報が読み書き込み可能かチェック.
	 *
	 * @param name
	 *            対象のファイル／ディレクトリ名を設定します.
	 * @return boolean [true]の場合、読み書き込み可能です.
	 */
	public static final boolean isIO(String name) {
		File file = new File(name);
		return (file.exists() &&
			file.canRead() && file.canWrite());
	}

	/**
	 * 対象のディレクトリを生成.
	 *
	 * @param dirName
	 *            生成対象のディレクトリ名を設定します.
	 * @exception Exception
	 *                例外.
	 */
	public static final void mkdirs(String dir)
		throws Exception {
		File fp = new File(dir);
		if (!fp.mkdirs()) {
			throw new IOException(
				"Failed to create directory (" +
				dir + ").");
		}
	}

	/**
	 * ファイルの長さを取得.
	 *
	 * @param name
	 *            対象のファイル名を設定します.
	 * @return long ファイルの長さが返却されます.
	 *              [-1L]が返却された場合ファイルは存在しません.
	 * @exception Exception
	 *                例外.
	 */
	public static final long getFileLength(String name)
		throws Exception {
		File fp = new File(name);
		return (fp.exists()) ? fp.length() : -1L;
	}

	/**
	 * ファイル生成時間を取得.
	 *
	 * @param name
	 *            対象のファイル名を設定します.
	 * @return long ファイルタイムが返却されます. [-1L]が返却された場合、ファイルは存在しません.
	 * @exception Exception
	 *                例外.
	 */
	public static final long birthtime(String name)
		throws Exception {
		return _getFileTime(0, name);
	}

	/**
	 * 最終アクセス時間を取得.
	 *
	 * @param name
	 *            対象のファイル名を設定します.
	 * @return long ファイルタイムが返却されます. [-1L]が返却された場合、ファイルは存在しません.
	 * @exception Exception
	 *                例外.
	 */
	public static final long atime(String name)
		throws Exception {
		return _getFileTime(1, name);
	}

	/**
	 * 最終更新時間を取得.
	 *
	 * @param name
	 *            対象のファイル名を設定します.
	 * @return long ファイルタイムが返却されます. [-1L]が返却された場合、ファイルは存在しません.
	 * @exception Exception
	 *                例外.
	 */
	public static final long mtime(String name)
		throws Exception {
		return _getFileTime(2, name);
	}

	/**
	 * ファイル名のフルパスを取得.
	 *
	 * @param name
	 *            対象のファイル名を設定します.
	 * @return String フルパス名が返却されます.
	 * @exception Exception
	 *                例外.
	 */
	public static final String getFullPath(String name)
		throws Exception {
		char c;
		name = new File(name).getCanonicalPath();
		final int len = name.length();
		StringBuilder buf = new StringBuilder();
		if(!name.startsWith("/")) {
			buf.append("/");
		} else if(name.indexOf("\\") == -1) {
			return name;
		}
		for(int i = 0; i < len; i++) {
			c = name.charAt(i);
			if(c == '\\') {
				buf.append("/");
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}

	/**
	 * 対象パスのファイル名のみ取得.
	 *
	 * @param path
	 *            対象のパスを設定します.
	 * @return String ファイル名が返却されます.
	 */
	public static final String getFileName(String path) {
		int p = path.lastIndexOf("/");
		if (p == -1) {
			p = path.lastIndexOf("\\");
		}
		if (p == -1) {
			return path;
		}
		return path.substring(p + 1);
	}

	/**
	 * 対象パスのディレクトリ名のみ取得.
	 *
	 * @param name
	 * @return
	 */
	public static final String getDirectoryName(String name) {
		String f = FileUtil.getFileName(name);
		return name.substring(0, name.length() - f.length());
	}

	/**
	 * ファイル内容を取得.
	 *
	 * @param name 対象のファイル名を設定します.
	 * @return byte[] バイナリ情報が返却されます.
	 * @exception Exception 例外.
	 */
	public static final byte[] getFile(String name)
		throws Exception {
		return getBinary(new FileInputStream(name));
	}

	/**
	 * ファイル内容を取得.
	 *
	 * @param name 対象のファイル名を設定します.
	 * @return String 文字列情報が返却されます.
	 * @exception Exception 例外.
	 */
	public static final String getFileString(String name)
		throws Exception {
		return getFileString(name, "UTF8");
	}
	
	/**
	 * ファイル内容を取得.
	 *
	 * @param name
	 *            対象のファイル名を設定します.
	 * @param charset
	 *            対象のキャラクタセットを設定します.
	 * @return String 文字列情報が返却されます.
	 * @exception Exception
	 *                例外.
	 */
	public static final String getFileString(
		String name, String charset)
		throws Exception {
		return getString(new FileInputStream(name), charset);
	}
	
	/**
	 * InputStream内容を取得.
	 *
	 * @param in 対象のInputStreamを設定します.
	 * @return byte[] バイナリ情報が返却されます.
	 * @exception Exception 例外.
	 */
	public static final byte[] getBinary(InputStream in)
		throws Exception {
		InputStream buf = null;
		ByteArrayOutputStream bo = null;
		try {
			int len;
			byte[] b = new byte[1024];
			bo = new ByteArrayOutputStream();
			buf = new BufferedInputStream(in);
			while (true) {
				if ((len = buf.read(b)) <= 0) {
					if (len <= -1) {
						break;
					}
					continue;
				}
				bo.write(b, 0, len);
			}
			buf.close();
			buf = null;
			byte[] ret = bo.toByteArray();
			bo.close();
			bo = null;
			return ret;
		} finally {
			if (buf != null) {
				try {
					buf.close();
				} catch (Exception t) {
				}
			}
			buf = null;
			if (bo != null) {
				bo.close();
			}
		}
	}
	
	/**
	 * InputStream内容を取得.
	 *
	 * @param in 対象のInputStreamを設定します.
	 * @param charset 対象のキャラクタセットを設定します.
	 * @return String 文字列情報が返却されます.
	 * @exception Exception 例外.
	 */
	public static final String getString(
		InputStream in, String charset)
		throws Exception {
		String ret = null;
		CharArrayWriter ca = null;
		Reader buf = null;
		try {
			if(charset == null || charset.isEmpty()) {
				charset = "UTF8";
			}
			int len;
			char[] tmp = new char[1024];
			ca = new CharArrayWriter();
			buf = new BufferedReader(
				new InputStreamReader(
					in, charset));
			while ((len = buf.read(tmp, 0, 512)) > 0) {
				ca.write(tmp, 0, len);
			}
			ret = ca.toString();
			ca.close();
			ca = null;
			buf.close();
			buf = null;
		} finally {
			if (buf != null) {
				try {
					buf.close();
				} catch (Exception t) {
				}
			}
			if (ca != null) {
				try {
					ca.close();
				} catch (Exception t) {
				}
			}
			buf = null;
			ca = null;
		}
		return ret;
	}

	/**
	 * バイナリをファイル出力.
	 *
	 * @param newFile
	 *            [true]の場合、新規でファイル出力します.
	 * @param name
	 *            ファイル名を設定します.
	 * @param binary
	 *            出力対象のバイナリを設定します.
	 * @exception Exception
	 *                例外.
	 */
	public static final void setFile(
		boolean newFile, String name, byte[] binary)
		throws Exception {
		if (binary == null) {
			throw new IOException("There is no binary to output.");
		}
		BufferedOutputStream buf = new BufferedOutputStream(
			new FileOutputStream(name, !newFile));
		try {
			buf.write(binary);
			buf.flush();
			buf.close();
			buf = null;
		} finally {
			if (buf != null) {
				try {
					buf.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * 文字情報をファイル出力.
	 *
	 * @param newFile
	 *            [true]の場合、新規でファイル出力します.
	 * @param name
	 *            ファイル名を設定します.
	 * @param value
	 *            出力対象の文字列を設定します.
	 * @param charset
	 *            対象のキャラクタセットを設定します.
	 *            nullの場合は、UTF8が設定されます.
	 * @exception Exception
	 *                例外.
	 */
	public static final void setFileString(
		boolean newFile, String name, String value, String charset)
			throws Exception {
		if (value == null) {
			throw new IOException(
				"There is no target string information for output.");
		}
		BufferedWriter buf = new BufferedWriter(
			new OutputStreamWriter(new FileOutputStream(name, !newFile),
				(charset == null) ? "UTF8" : charset));
		try {
			buf.write(value, 0, value.length());
			buf.flush();
			buf.close();
			buf = null;
		} finally {
			if (buf != null) {
				try {
					buf.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * 指定ファイルorフォルダを削除.
	 *
	 * @param name
	 *            対象のファイルorフォルダ名を設定します.
	 * @return boolean 削除結果が返されます.
	 * @exception 例外.
	 */
	public static final void removeFile(String name)
		throws Exception {
		_deleteFileOrDirectory(name);
	}
	
	/**
	 * 指定フォルダ内のファイルとフォルダを全削除.
	 *
	 * @param name
	 *            削除対象のフォルダ名かファイル名を設定します.
	 * @throws Exception
	 *             例外.
	 */
	public static final void delete(String name)
		throws Exception {
		_delete(name);
	}
	
	// 指定内のフォルダが空でない場合は中身も削除して削除処理.
	private static final void _delete(String name)
		throws Exception {
		if (isFile(name)) {
			_deleteFileOrDirectory(name);
		} else {
			String[] list = list(name);
			if (list != null && list.length > 0) {
				if (!name.endsWith("/")) {
					name = name + "/";
				}
				String path;
				int len = list.length;
				for (int i = 0; i < len; i++) {
					path = name + list[i];
					_delete(path);
				}
			}
			_deleteFileOrDirectory(name);
		}
	}
	
	
	// ファイル及びディレクトリの削除.
	private static final void _deleteFileOrDirectory(
		String name)
		throws Exception {
		Files.delete(Paths.get(name));
	}

	/**
	 * ファイルリストを取得.
	 *
	 * @param name
	 *            対象のフォルダ名を設定します.
	 * @return String[] ファイルリストが返却されます.
	 * @exception Exception
	 *                例外.
	 */
	public static final String[] list(String name)
		throws Exception {
		File fp = new File(name);
		return (fp.exists()) ?
			fp.list() : new String[0];
	}

	/**
	 * ファイル、フォルダの移動.
	 *
	 * @param src
	 *            移動元のファイル名を設定します.
	 * @param dest
	 *            移動先のファイル名を設定します.
	 * @throws Exception
	 *             例外.
	 */
	public static final void move(String src, String dest)
		throws Exception {
		Files.move(Paths.get(src), Paths.get(dest));
	}

	// ファイルのコピー.
	private static final void _copyFile(String src, String dest)
		throws Exception {
		Files.copy(Paths.get(src), Paths.get(dest),
			StandardCopyOption.COPY_ATTRIBUTES,
			StandardCopyOption.REPLACE_EXISTING);
	}

	// コピー処理.
	public static final void _copy(String src, String dest)
		throws Exception {
		if (isFile(src)) {
			_copyFile(src, dest);
		} else {
			String[] list = list(src);
			if (list != null && list.length > 0) {
				if (!src.endsWith("/")) {
					src = src + "/";
				}
				if (!dest.endsWith("/")) {
					dest = dest + "/";
				}
				int len = list.length;
				for (int i = 0; i < len; i++) {
					if (isDir(src)) {
						if (!isDir(dest + list[i])) {
							mkdirs(dest + list[i]);
						}
					}
					_copy(src + list[i], dest + list[i]);
				}
			}
		}
	}

	/**
	 * ファイル・フォルダのコピー処理.
	 *
	 * @param src
	 *            コピー元のファイル、フォルダを設定します.
	 * @param dest
	 *            コピー先のファイル、フォルダを設定します.
	 * @throws Exception
	 *             例外.
	 */
	public static final void copy(String src, String dest)
		throws Exception {
		_copy(getFullPath(src), getFullPath(dest));
	}
	
	/**
	 * 取り込み判別を行うインターフェイス.
	 */
	@FunctionalInterface
	public static interface TakeIn {
		/**
		 * 判別処理.
		 * @param fileName 対象のファイル名を設定します.
		 * @return boolean 取り込み対象の場合は true返却.
		 */
		public boolean determine(String fileName);
	}
	
	/**
	 * 対象ディレクトリ以下を走査して、取り込み判別を元に
	 * ファイル名を取り込む.
	 * @param directory 対象のディレクトリ名を設定します.
	 * @param takeIn 取り込み判別を設定します.
	 * @return List<String> 取り込まれたファイル名が返却されます.
	 */
	public static final List<String> takeInFile(
		String directory, TakeIn takeIn) {
		List<String> ret = new ArrayList<String>();
		try {
			_takeInFile(ret, takeIn, getFullPath(directory));
			return ret;
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
	
	// 対象ディレクトリ以下を走査して、取り込み判別を元に
	// ファイル名を取り込む.
	private static final void _takeInFile(
		List<String> out, TakeIn takeIn, String target)
		throws Exception {
		if (isFile(target)) {
			if(takeIn.determine(target.toLowerCase())) {
				out.add(target);
			}
		} else {
			String[] list = list(target);
			if (list != null && list.length > 0) {
				if (!target.endsWith("/")) {
					target = target + "/";
				}
				int len = list.length;
				for (int i = 0; i < len; i++) {
					_takeInFile(out, takeIn, target + list[i]);
				}
			}
		}

	}
}
