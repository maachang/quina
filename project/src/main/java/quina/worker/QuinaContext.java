package quina.worker;

public interface QuinaContext {
	/**
	 * 対象のコンテキストをコピー.
	 * @return HttpContext コピーされたContextが返却されます.
	 */
	public QuinaContext copy();
}
