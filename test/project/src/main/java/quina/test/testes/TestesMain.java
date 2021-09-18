package quina.test.testes;

import java.lang.annotation.Annotation;

import quina.annotation.cdi.CdiScoped;

public class TestesMain {
	public static final void main(String[] args) {
		Class<?> c = TestesInfo.class;
		boolean res = isAnnotation(0, c, CdiScoped.class);
		System.out.println("res: " + res);
		

	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final boolean isAnnotation(int count, Class c, Class at) {
		// 対象クラスに指定アノテーションが含まれてる場合.
		if(c.isAnnotationPresent(at)) {
			return true;
		// 対象アノテーションの１つ下のアノテーション定義を参照する.
		} else if(count >= 1) {
			return false;
		}
		// 対象クラスに定義されてるアノテーション一覧を取得.
		Annotation[] list = c.getAnnotations();
		int len = list == null ? 0 : list.length;
		for(int i = 0; i < len; i ++) {
			// このアノテーションに指定アノテーションが含まれてるか.
			if(isAnnotation(count + 1, list[i].annotationType(), at)) {
				return true;
			}
		}
		return false;
	}


}
