package quina.textanalysis;

import quina.exception.QuinaException;

/**
 * テキストスクリプト.
 */
public class TextScript {
	//テキストスクリプト.
	protected String script;
	
	//テキストスクリプト長.
	protected int scriptLength;
	
	// 現在読み込み中のポジション.
	protected int position;
	
	// ポジションバックアップ.
	protected int backupPosition;
	
	/**
	 * コンストラクタ.
	 */
	protected TextScript() {}
	
	/**
	 * コンストラクタ.
	 * @param script テキストスクリプトを設定します.
	 */
	public TextScript(String script) {
		create(script);
	}
	
	/**
	 * 初期化処理.
	 * @param script テキストスクリプトを設定します.
	 * @return TextScript このオブジェクトが返却されます.
	 */
	public TextScript create(String script) {
		if(script == null || script.isEmpty()) {
			throw new QuinaException("The script is not set.");
		}
		script = convertEnter(script);
		this.script = script;
		this.scriptLength = script.length();
		clearPosition();
		return this;
	}
	
	/**
	 * 改行コードを"￥ｎ"に統一.
	 * この処理を実施する場合は、この処理を呼び出す必要があります.
	 * @param script テキストスクリプトを設定します.
	 * @return String 変換された内容が返却されます.
	 */
	protected static final String convertEnter(String script) {
		char c;
		final int len = script.length();
		final StringBuilder buf = new StringBuilder(len);
		for(int i = 0; i < len; i ++) {
			c = script.charAt(i);
			if(c == '\r') {
				if(i + 1 >= len ||
					script.charAt(i + 1) != '\n') {
					buf.append('\n');
				}
				continue;
			}
			buf.append(c);
		}
		return buf.toString();
	}
	
	/**
	 * 終端の場合エラー.
	 */
	protected void checkEOF() {
		if(!isGet()) {
			throw new QuinaException(
				"It's already the end of the text script.");
		}
	}
	
	/**
	 * 現在位置の情報が読み込み可能かチェック.
	 * @return trueの場合、読み込み可能です.
	 */
	public boolean isGet() {
		return isGet(1);
	}
	
	/**
	 * 現在位置の情報が読み込み可能かチェック.
	 * @param addLen 長さを設定します.
	 *               マイナスを設定した場合エラーが発生します.
	 * @return boolean trueの場合終端です.
	 */
	public boolean isGet(int addLen) {
		if(addLen < 0) {
			throw new QuinaException(
				"The position to advance is set as an integer: " +
					addLen);
		}
		return position + addLen <= scriptLength;
	}

	
	/**
	 * 現在位置が終端かチェック.
	 * @return boolean trueの場合終端です.
	 */
	public boolean isEOF() {
		return !isGet();
	}
	
	/**
	 * 指定位置を含んで終端かチェック.
	 * @param addLen この数字を足した場合終端になるかの
	 *               数字を設定します.
	 *               マイナスを設定した場合エラーが発生します.
	 * @return boolean trueの場合終端です.
	 */
	public boolean isEOF(int addLen) {
		return !isGet(addLen);
	}
	
	/**
	 * ポジションを１つ進める.
	 * @return trueの場合終端ではありません.
	 */
	public boolean next() {
		position += 1;
		return isGet();
	}
	
	/**
	 * ポジションを指定数進める.
	 * @params addPos 進めるポジション数を設定します.
	 *                進めるポジションは整数である必要があります.
	 * @return trueの場合終端ではありません.
	 */
	public boolean next(int addPos) {
		// 前に戻る設定は出来ない.
		if(addPos < 0) {
			throw new QuinaException(
				"The position to advance is set as an integer: " +
				addPos);
		}
		position += addPos;
		return isGet();
	}
	
	public boolean before() {
		if(position > 0) {
			position --;
			return true;
		}
		position = 0;
		return false;
	}
	
	/**
	 * ポジションを指定数進める.
	 * @params addPos 戻るポジション数を設定します.
	 *                戻るポジションは整数である必要があります.
	 * @return trueの場合終端ではありません.
	 */
	public boolean before(int removePos) {
		// 進む設定は出来ない.
		if(removePos > 0) {
			throw new QuinaException(
				"The position to advance is set as an integer: " +
				removePos);
		}
		position -= removePos;
		if(position < 0) {
			position = 0;
		}
		return isGet();
	}
	
	/**
	 * ポジションを設定.
	 * @return trueの場合終端ではありません.
	 */
	public boolean setPosition(int pos) {
		if(pos < 0) {
			pos = 0;
		}
		this.position = pos;
		return isGet();
	}
	
	/**
	 * ポジションをクリアします.
	 * また、バックアップポジションも同様にクリアされます.
	 * @return TextScript このオブジェクトが返却されます.
	 */
	public TextScript clearPosition() {
		this.position = 0;
		this.backupPosition = -1;
		return this;
	}
	
	/**
	 * 現在のポジションを取得.
	 * @return int 現在のポジションが返却されます.
	 */
	public int getPosition() {
		return position;
	}
	
	/**
	 * テキストスクリプト長を取得.
	 * @return int テキストスクリプト長が返却されます.
	 */
	public int getLength() {
		return scriptLength;
	}
	
	/**
	 * ポジションのバックアップ.
	 * @return TextScript このオブジェクトが返却されます.
	 */
	public TextScript backupPosition() {
		this.backupPosition = this.position;
		return this;
	}
	
	/**
	 * バックアップされたポジションの復元.
	 * @return boolean trueの場合、正しく復元できました.
	 */
	public boolean restorePosition() {
		if(this.backupPosition == -1) {
			return false;
		}
		this.position = backupPosition;
		this.backupPosition = -1;
		return true;
	}
	
	/**
	 * テキストスクリプトを取得.
	 * @return String テキストスクリプトが返却されます.
	 */
	public String getScript() {
		return script;
	}
	
	@Override
	public String toString() {
		return script;
	}
	
	/**
	 * 現在位置の１文字を取得します.
	 * @return char １文字情報が返却されます.
	 */
	public char get() {
		checkEOF();
		return getChar();
	}
	
	/**
	 * １つの文字を取得.
	 * EOFチェック処理を実施しません.
	 * @return char １文字情報が返却されます.
	 */
	protected char getChar() {
		return script.charAt(position);
	}
	
	/**
	 * 前の文字が￥文字か取得.
	 * @return true の場合 前の文字は￥です.
	 */
	public boolean isBeforeYen() {
		if(isEOF()) {
			return false;
		}
		return position == 0 ||
			position >= scriptLength + 1 ? false :
			script.charAt(position - 1) == '\\';
	}
	
	/**
	 * 空文字かチェック.
	 * @return boolean trueの場合は空文字です.
	 */
	public boolean isBlank() {
		if(isEOF()) {
			return false;
		}
		return isBlank(getChar());
	}
	
	/**
	 * 空文字かチェック.
	 * @param c 文字を設定します.
	 * @return boolean trueの場合は空文字です.
	 */
	public static final boolean isBlank(char c) {
		return (c == ' ' || c == '\t' ||
			c == '\r' || c == '\n');
	}
	
	/**
	 * 指定位置の文字が指定文字と一致するかチェック.
	 * @param target 一致する文字を設定します.
	 * @return boolean trueの場合、一致します.
	 */
	public boolean isChar(char target) {
		if(isEOF()) {
			return false;
		}
		return getChar() == target;
	}
	
	
	/**
	 * 開始括弧を取得.
	 * @return int -1の場合見つかりませんでした.
	 *             1 = ( 2 = { 3 = [ となります.
	 */
	public int getStartBrackets() {
		if(isEOF()) {
			return -1;
		}
		char c;
		if((c = getChar()) == '(') {
			return 1;
		} else if(c == '{') {
			return 2;
		} else if(c == '[') {
			return 3;
		}
		return -1;
	}
	
	/**
	 * 終端括弧を取得.
	 * @return int -1の場合見つかりませんでした.
	 *             1 = ) 2 = } 3 = ] となります.
	 */
	public int getEndBrackets() {
		if(isEOF()) {
			return -1;
		}
		char c;
		if((c = getChar()) == ')') {
			return 1;
		} else if(c == '}') {
			return 2;
		} else if(c == ']') {
			return 3;
		}
		return -1;
	}
	
	/**
	 * 指定文字が一致するかチェック.
	 * @param target 対象位置の文字列に対して一致するかチェック.
	 * @return boolean trueの場合一致しました.
	 */
	public boolean eq(Object o) {
		if(o == null) {
			return false;
		} else if(isEOF()) {
			return false;
		} else if(o instanceof String) {
			String target = (String)o;
			if(target.length() == 0) {
				return false;
			}
			final int len = target.length();
			if(isEOF(len)) {
				return false;
			}
			for(int i = 0; i < len; i ++) {
				if(script.charAt(i + position) !=
					target.charAt(i)) {
					return false;
				}
			}
			return true;
		} else if(o instanceof char[]) {
			final char[] target = (char[])o;
			if(target == null || target.length == 0) {
				return false;
			}
			final int len = target.length;
			if(isEOF(len)) {
				return false;
			}
			for(int i = 0; i < len; i ++) {
				if(script.charAt(i + position) !=
					target[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 指定位置から対象の文字列が一致するかチェック.
	 * @param target 対象位置の文字列に対して一致するかチェック.
	 * @return boolean trueの場合一致しました.
	 */
	public boolean isString(char[] target) {
		if(target == null || target.length == 0) {
			throw new QuinaException(
				"The target argument is empty.");
		}
		return eq(target);
	}
	
	/**
	 * 指定位置から対象の文字列が一致するかチェック.
	 * @param target 対象の文字列を設定します.
	 * @return boolean trueの場合一致しました.
	 */
	public boolean isString(String target) {
		if(target == null || target.isEmpty()) {
			throw new QuinaException(
				"The target argument is empty.");
		}
		return eq(target);
	}
	
	/**
	 * スペース系の文字が見つかるまで移動.
	 * @return boolean trueの場合、正しく移動できました.
	 */
	public boolean moveToBlank() {
		if(isBlank()) {
			return true;
		}
		while(next()) {
			if(isBlank()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * スペース系の文字を読み飛ばす.
	 * @return boolean trueの場合、正しく移動できました.
	 */
	public boolean moveToSkipBlank() {
		if(!isBlank()) {
			return true;
		}
		while(next()) {
			if(!isBlank()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 改行の文字が検出されるまで移動.
	 * 改行が見つかった場合はその改行は読み飛ばしません.
	 * @return boolean trueの場合、正しく移動できました.
	 *                 falseの場合、改行が見つからずに
	 *                 EOFに達しました.
	 */
	public boolean moveToEnter() {
		if(isEOF()) {
			return false;
		} else if(getChar() == '\n') {
			return true;
		}
		while(next()) {
			if(getChar() == '\n') {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 改行の文字を読み飛ばす.
	 * @return boolean trueの場合、正しく移動できました.
	 *                 falseの場合、改行が見つからずに
	 *                 EOFに達しました.
	 */
	public boolean moveToSkipEnter() {
		if(isEOF()) {
			return false;
		} else if(getChar() != '\n') {
			return true;
		}
		while(next()) {
			if(getChar() != '\n') {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * コーテーションの文字列がある場合
	 * コーテーション終端まで移動.
	 * @return boolean trueの場合、正しく移動できました.
	 */
	public boolean moveToBlockQuotation() {
		if(isEOF()) {
			return false;
		}
		char c = getChar();
		if(!isBeforeYen() &&
			(c == '\'' || c =='\"')) {
			int n = c;
			int b = c;
			while(next()) {
				c = getChar();
				if(b != '\\' && c == n) {
					next();
					break;
				}
				b = c;
			}
			return true;
		}
		return false;
	}
	
	// [／／(半角)]のコメント文字列.
	protected static final char[] COMMENT_TWO_SLASH = new char[] {'/', '/'};
	
	/**
	 * [／／(半角)]のコメントがある場合、改行まで移動.
	 * @return boolean trueの場合、正しく移動できました.
	 */
	public boolean moveToLineTwoSlashComment() {
		if(isString(COMMENT_TWO_SLASH)) {
			next(COMMENT_TWO_SLASH.length);
			moveToEnter();
			next();
			return true;
		}
		return false;
	}
	
	// [＃(半角)]のコメント文字.
	protected static final char COMMENT_SHARP = '#';
	
	/**
	 * [＃(半角)]のコメントがある場合、改行まで移動.
	 * @return boolean trueの場合、正しく移動できました.
	 */
	public boolean moveToLineSharpComment() {
		if(isEOF()) {
			return false;
		} else if(getChar() == COMMENT_SHARP) {
			next();
			moveToEnter();
			next();
			return true;
		}
		return false;
	}
	
	// [－－(半角)]のコメント文字列.
	protected static final char[] COMMENT_TWO_HYPHEN = new char[] {'-', '-'};
	
	/**
	 * [－－(半角)]のコメントがある場合、改行まで移動.
	 * @return boolean trueの場合、正しく移動できました.
	 */
	public boolean moveToLineTwoHyphenComment() {
		if(isString(COMMENT_TWO_HYPHEN)) {
			next(COMMENT_TWO_HYPHEN.length);
			moveToEnter();
			next();
			return true;
		}
		return false;
	}
	
	/**
	 * オリジナルな改行コメント.
	 * @param simbol コメントのオリジナルシンボルを設定します.
	 * @return boolean trueの場合、正しく移動できました.
	 */
	public boolean moveToOriginLineComment(String simbol) {
		if(simbol == null || simbol.isEmpty()) {
			return false;
		}
		if(isString(simbol)) {
			next(simbol.length());
			moveToEnter();
			next();
			return true;
		}
		return false;
	}
	
	// ブロックコメント開始.
	protected static final char[] COMMENT_START_BLOCK_COMMENT =
		new char[] { '/', '*'};
	
	// ブロックコメント終了.
	protected static final char[] COMMENT_END_BLOCK_COMMENT =
		new char[] { '*', '/'};
	
	/**
	 * [／＊(半角)] [＊／(半角)]のブロックコメントがある場合に移動.
	 * @return boolean trueの場合、正しく移動できました.
	 */
	public boolean moveToBlockComment() {
		if(isString(COMMENT_START_BLOCK_COMMENT)) {
			next(COMMENT_START_BLOCK_COMMENT.length);
			while(isGet()) {
				if(isString(COMMENT_END_BLOCK_COMMENT)) {
					next(COMMENT_END_BLOCK_COMMENT.length);
					return true;
				}
				next();
			}
			return true;
		}
		return false;
	}
	
	/**
	 * オリジナルなブロックコメントがある場合に移動.
	 * @param start オリジナルな開始ブロックコメントを設定します.
	 * @param end オリジナルな終端ブロックコメントを設定します.
	 * @return boolean trueの場合、正しく移動できました.
	 */
	public boolean moveToOriginBlockComment(
		String start, String end) {
		if(start == null || end == null ||
			start.isEmpty() || end.isEmpty()) {
			return false;
		}
		if(isString(start)) {
			next(start.length());
			while(isGet()) {
				if(isString(end)) {
					next(end.length());
					return true;
				}
				next();
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 全部の条件が連続して一致するかチェックして移動.
	 * たとえば "abc", "def" で定義された場合.
	 *  [abc     def ] の場合、一致する一方
	 *  [abc  xx def ] の場合、一致しません.
	 * つまり、スペース系を除いて出現内容と指定条件が
	 * 一致するかをチェック出来ます.
	 * 
	 * また連続一致している場合は、ポジションが移動し、
	 * 一致しない場合は、ポジションは変わりません.
	 * @param args 文字列群を設定します.
	 * @return boolean trueの場合、連続一致しました.
	 */
	public boolean moveToAllMatch(String... args) {
		if(args == null || args.length == 0) {
			return false;
		}
		String value = null;
		final int backPos = position;
		final int len = args.length;
		for(int i = 0; i < len; i ++) {
			// 前回の条件が存在する場合.
			if(value != null) {
				// 一致文字列分移動.
				next(value.length());
				// スペース等を読み飛ばす.
				moveToSkipBlank();
			}
			// 今回の処理.
			value = args[i];
			// 文字列一致.
			if(!isString(value)) {
				// 一致しない場合は
				// ポジションリセット.
				position = backPos;
				return false;
			}
		}
		// 前回の条件が存在する場合.
		if(value != null) {
			// 一致文字列分移動.
			next(value.length());
		}
		return true;
	}
	
	/**
	 * 括弧を検出した場合に終端位置まで移動.
	 * 括弧の判別は以下が対象となります.
	 *  (){}[]
	 * @param ua ユーザー解析条件を設定します.
	 * @return boolean trueの場合、括弧の開始位置を検出し、
	 *                 終端位置を検出して移動しました.
	 */
	public boolean moveToBracketsByRange(UserAnalysis ua) {
		final int befPos = position;
		// 開始括弧を取得.
		final int br = getStartBrackets();
		// 開始括弧が存在する場合.
		if(br != -1) {
			int brCount = 1;
			// 次の文字に移動.
			next();
			while(!isEOF()) {
				// ユーザー解析で移動.
				// 括弧内の例外条件を読み飛ばす.
				// たとえばコーテーションやコメントなど.
				// 読み飛ばす定義された内容を読み飛ばす.
				if(ua != null) {
					ua.analysis(this);
				}
				// スペース等を読み飛ばす.
				moveToSkipBlank();
				// 括弧開始.
				if(getStartBrackets() == br) {
					brCount ++;
				// 括弧終端.
				} else if(getEndBrackets() == br) {
					brCount --;
					// 最初に検知した括弧の終端の場合.
					if(brCount == 0) {
						// 次に進める.
						next();
						return true;
					}
				}
				// 次に進める.
				next();
			}
		}
		// ポジションを元に戻す.
		position = befPos;
		// 見つからない場合.
		return false;
	}
	
	/**
	 * 現在位置から指定文字列と一致するまで移動.
	 * @param value 対象の文字列を設定します.
	 * @return boolean trueの場合、見つかって移動されました.
	 */
	public boolean moveToIndexOf(String value) {
		if(value == null || value.isBlank()) {
			throw new QuinaException(
				"The value argument is empty.");
		} else if(isEOF()) {
			return false;
		}
		final int p = script.indexOf(value, position);
		if(p == -1) {
			return false;
		}
		this.position = p;
		return true;
	}
	
	/**
	 * 対象位置の文字位置に差し込む.
	 * また、この処理を実行後にポジションやバックアップポジションが
	 * リセットされます.
	 * @param start 開始位置を設定します.
	 * @param end 終了位置を設定します.
	 * @param str 置き換える文字列を設定します.
	 * @return TextScript このオブジェクトが返却されます.
	 */
	public TextScript pluginString(int start, int end, String str) {
		this.script = new StringBuilder(script.substring(0, start))
			.append(str)
			.append(script.substring(end))
			.toString();
		this.scriptLength = script.length();
		clearPosition();
		return this;
	}
	
	/**
	 * 指定範囲の文字列を除外して、除外分を返却します.
	 * また、この処理を実行後にポジションやバックアップポジションが
	 * リセットされます.
	 * @param start 開始位置を設定します.
	 * @return String 除外された文字列が返却されます.
	 */
	public String substring(int start) {
		return substring(start, scriptLength);
	}
	
	/**
	 * 指定範囲の文字列を除外して、除外分を返却します.
	 * また、この処理を実行後にポジションやバックアップポジションが
	 * リセットされます.
	 * @param start 開始位置を設定します.
	 * @param end 終了位置を設定します.
	 * @return String 除外された文字列が返却されます.
	 */
	public String substring(int start, int end) {
		String ret = script.substring(start, end);
		pluginString(start, end, "");
		return ret;
	}
}
