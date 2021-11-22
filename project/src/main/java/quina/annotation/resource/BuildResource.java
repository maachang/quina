package quina.annotation.resource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ファイルのコピーを行うBuildアノテーション.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BuildResource {
	/**
	 * コピー元リソース名.
	 */
	public String src();
	
	/**
	 * コピー元リソース.
	 */
	public ResourceMode srcMode() default ResourceMode.JavaPackage;
	
	/**
	 * コピー先リソース名.
	 */
	public String dest();
	
	/**
	 * コピー先リソース.
	 */
	public ResourceMode destMode() default ResourceMode.ClassPackage;
	
}
