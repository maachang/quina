package quina.annotation;

/**
 * Annotationユーティリティ.
 */
public class AnnotationUtil {
	private AnnotationUtil() {}
	
	/**
	 * Pathに￥が存在する場合は / に変換.
	 * @param path
	 * @return
	 */
	public static final String slashPath(String path) {
		if(path == null || path.isEmpty()) {
			return path;
		}
		char c;
		StringBuilder buf = new StringBuilder();
		int len = path.length();
		for(int i = 0; i < len; i ++) {
			c = path.charAt(i);
			if(c == '\\') {
				buf.append('/');
			} else {
				buf.append(c);
			}
		}
		path = buf.toString();
		if(path.endsWith("/")) {
			return path.substring(0, path.length() - 1);
		}
		return path;
	}
}
