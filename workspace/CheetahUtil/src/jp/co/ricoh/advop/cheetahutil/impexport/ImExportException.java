package jp.co.ricoh.advop.cheetahutil.impexport;

/**
 * インポートエクスポートで発生したエラーを表す。<BR>
 */
public class ImExportException extends Exception {
	/** エラーコード */
    private final int code;

	/**
	 * 指定された値を保持するインポートエクスポートで発生したエラーを構築する。<BR>
	 * @param code エラーコード
	 * @param message エラーメッセージ
	 */
	public ImExportException(int code, String message) {
		super(message);
		this.code = code;
	}

	/**
	 * エラーコードを取得する。<BR>
	 * @return エラーコード
	 */
	public int getCode() {
		return code;
	}
}
