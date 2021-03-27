package quina.validate;

import quina.QuinaException;
import quina.util.StringUtil;

/**
 * １つのValidate要素.
 */
final class VElement {
	private boolean headerFlag;
	private String column;
	private String headerColumn;
	private VT type;
	private VCheckElements vlist;

	/**
	 * コンストラクタ.
	 * @param column
	 * @param type
	 * @param validate
	 */
	public VElement(String column, String type, String validate) {
		this(column, VT.getStringByVType(type), validate);
	}

	/**
	 * コンストラクタ.
	 * @param column
	 * @param type
	 * @param validate
	 */
	public VElement(String column, VT type, String validate) {
		VCheckElements lst = new VCheckElements(type, validate);
		this.column = headerColumnNames(column);
		this.type = type;
		this.vlist = lst;
		// HTTPヘッダ名の場合.
		if(!this.column.equals(column)) {
			this.headerFlag = true;
			this.headerColumn = column;
		// パラメータから取得する場合.
		} else {
			this.headerFlag = false;
			this.headerColumn = null;
		}
	}

	// カラム名がヘッダ情報の場合変換.
	private static final String headerColumnNames(String column) {
		if (column.startsWith("X-")) {
			try {
				// カラム名の[X-]を取り、-を抜いて、最初の文字を小文字に変換.
				// [X-Test-Code] -> testCode.
				column = StringUtil.changeString(column.substring(2), "-", "");
				column = column.substring(0, 1).toLowerCase() + column.substring(1);
			} catch (Exception e) {
				throw new QuinaException(500, "Failed to get header information '" + column + ".'", e);
			}
		}
		return column;
	}

	/**
	 * Validate処理.
	 * @param value
	 * @return
	 */
	public Object validate(Object value) {
		return vlist.check(type, column, value);
	}

	/**
	 * HTTPヘッダから取得する場合.
	 * @return
	 */
	public boolean isHeader() {
		return headerFlag;
	}

	/**
	 * パラメータ名を取得.
	 * @return
	 */
	public String getColumn() {
		return column;
	}

	/**
	 * Httpヘッダパラメータ名を取得.
	 * @return
	 */
	public String getHeaderColumn() {
		return headerColumn;
	}

	/**
	 * VTypeを取得.
	 * @return
	 */
	public VT getType() {
		return type;
	}

	/**
	 * VCheckListを取得.
	 * @return
	 */
	public VCheckElements getVCheckList() {
		return vlist;
	}
}
