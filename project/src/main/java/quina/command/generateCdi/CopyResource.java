package quina.command.generateCdi;

import quina.annotation.resource.AnnotationResource;
import quina.annotation.resource.ExecuteCopyResource;

/**
 * Resource情報をコピーします.
 */
public class CopyResource {
	private CopyResource() {}
	
	/**
	 * ResourceScope定義されていて、クラス指定が
	 * 行われてた場合はそれが、クラスローダ内に
	 * ローディングされていて、その中のBuildResource
	 * アノテーションが定義されてるMethod群を実行します.
	 * @param c 対象のクラスを設定します.
	 * @param javaSourceDir javaSourceDirを設定します.
	 * @param clazzDir clazzDirを設定します.
	 * @return boolean trueの場合実行出来ました.
	 */
	public static final boolean executeExecuteStep(
		Class<?> c, String javaSourceDir, String clazzDir) {
		ExecuteCopyResource step =
			AnnotationResource.resourceScoped(c);
		if(step != null) {
			step.execute(javaSourceDir, clazzDir);
			return true;
		}
		return false;
	}
}
