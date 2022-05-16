package quina.compile.cdi;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import quina.compile.QuinaCTConstants;
import quina.compile.QuinaCTParams;
import quina.compile.QuinaCTUtil;
import quina.compile.cdi.annotation.proxy.AnnotationProxyScopedConstants;
import quina.compile.cdi.annotation.proxy.ProxyField;
import quina.compile.cdi.annotation.proxy.ProxyInitialSetting;
import quina.compile.cdi.annotation.proxy.ProxyInjectMethod;
import quina.compile.cdi.annotation.proxy.ProxyOverride;
import quina.exception.QuinaException;
import quina.util.CheckReflection;
import quina.util.StringUtil;
import quina.util.collection.js.JsArray;
import quina.util.collection.js.JsObject;

/**
 * proxy_AutoProxySmple.java.smjを利用した
 * Proxyオブジェクトの出力処理.
 */
public class CdiOutputJavaProxySrc {
	
	// 利用smple名.
	private static final String SMPLE_NAME =
		"proxy_AutoProxySmple.java";
	
	/**
	 * 抽出したRoute定義されたComponentをJavaファイルに出力.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws IOException I/O例外.
	 * @throws ClassNotFoundException クラス非存在例外.
	 */
	public static final void proxyScoped(
		String outSourceDirectory, QuinaCTParams params)
		throws Exception {
		
		final List<String> prxList = params.prxList;
		final int len = prxList == null ? 0 : prxList.size();
		
		// 自動生成するProxyオブジェクトのパッケージディレクトリを削除.
		CdiRemoveFileOrDir.removeProxyDirectory(outSourceDirectory);
		
		// ProxyScopedオブジェクトが存在しない場合.
		if(len <= 0) {
			return;
		}
		
		// Proxyオブジェクトのパッケージディレクトリ名を生成.
		String outDir = outCdiProxyDirectory(outSourceDirectory);
		
		// 自動生成オブジェクトの作成.
		for(int i = 0; i < len; i ++) {
			outputOneAutoProxyClass(outDir, prxList.get(i), params);
		}
	}
	
	/**
	 * １つの抽出したProxyScoped定義されたオブジェクトをJavaファイルに出力.
	 * @param outDir proxyオブジェクトを出力するディレクトリ名が設定されます.
	 * @param clazzName 対象のクラス名を設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws Exception 例外.
	 */
	private static final void outputOneAutoProxyClass(
		String outDir, String clazzName,QuinaCTParams params)
		throws Exception {
		
		// publicClassで空のPublicコンストラクタが存在するかチェック.
		Class<?> clazz = QuinaCTUtil.getClass(clazzName, params.cl);
		QuinaCTUtil.checkPublicClass(clazz);
		
		// このオブジェクトが abstract class 定義であるかチェック.
		if(!isAbstractClass(clazz)) {
			throw new QuinaException(
				"The ProxyScoped class must be an Abstract Class.");
		}
		
		// proxyFieldアノテーションが定義されているフィールド群を取得.
		Field prxField = getProxyField(clazz);
		
		// ProxyInitialSettingアノテーションが定義されているメソッドを取得.
		Method initSetMethod = getProxyInitialSetting(clazz);
		
		// ProxyInjectMethodアノテーションが定義されているメソッドを取得.
		Method injectMethod = getProxyInjectMethod(clazz);
		
		// ProxyOverrideアノテーションが設定されてるMethod群を取得.
		final List<Method> overrideList = new ArrayList<Method>();
		getProxyOverride(overrideList, clazz);
		
		// ProxyFieldのクラスがProxyScopedの継承元と一致しない場合.
		if(!CheckReflection.isInstanceof(clazz, prxField.getType())) {
			throw new QuinaException(
				"The type of ProxyField does not match the inheritance "+
				"source of ProxyScoped.");
		}
		
		// 出力先Proxyクラス名を生成.
		final String thisClassName = getProxyClassName(clazzName);
		
		// jsObject.
		JsObject jsObject = new JsObject();
		
		// [js-smple]継承元クラス名をセット.
		jsObject.putObject("srcClass", clazzName);
		
		// [js-smple]生成するProxyクラス名をセット.
		jsObject.putObject("thisClass", thisClassName);
		
		// [js-smple]initメソッドのパラメータ設定定義をセット.
		jsObject.putObject("initMethod",
			createProxyInitialSettingParams(initSetMethod));
		
		// [js-smple]Overrideメソッド群をセット.
		JsArray overriteJsArray = new JsArray();
		jsObject.putObject("proxyMethodList", overriteJsArray);
		Method[] methods = prxField.getType().getMethods();
		final int len = methods == null ? 0 : methods.length;
		for(int i = 0; i < len; i ++) {
			// @ProxyOverride条件は自動ソースで出力しない.
			if(isProxyOverride(overrideList, methods[i])) {
				continue;
			}
			// 未定義のメソッドを自動生成.
			overriteJsArray.addObject(
				createProxyOverrideMethod(methods[i], prxField, injectMethod)
			);
		}
		
		// 書き込みProxyクラス名のJavaファイル名を取得.
		final String outFileName = thisClassName + ".java";
		
		// 対象ProxyのJavaファイル出力.
		// js用のパラメータはJsArrayで囲う必要がある.
		// args = [{....}];
		CdiOutputJsSmpleOut.executeSmpleToOutputJavaFile(
			outDir, SMPLE_NAME,
			new JsArray(jsObject), outFileName);
	}
	
	// 出力先のProxyディレクトリ名を取得.
	private static final String outCdiProxyDirectory(
		String outSourceDirectory) {
		// quinax/proxyパスを生成.
		return outSourceDirectory + "/" +
			QuinaCTConstants.CDI_PROXY_DIRECTORY_NAME;
	}
	
	// ProxyScopedの自動作成されるクラス名を取得.
	private static final String getProxyClassName(
		String srcClassName) {
		// AutoProxy + "srcClassNameのクラス名"
		return new StringBuilder
			(AnnotationProxyScopedConstants.HEAD_PROXY_CLASS_NAME)
			.append(QuinaCTUtil.getClassNameByCutPackageName(srcClassName))
			.toString();
	}
	
	// 対象のメソッドがPublicかProtected定義の場合.
	private static final boolean isMethodByAvailable(Method m) {
		// 対象メソッドがpublicかpurotectedで継承利用可能かチェック.
		final int n = m.getModifiers();
		return Modifier.isPublic(n) || Modifier.isProtected(n);
	}
	
	// 対象のフィールドがPublicかProtected定義の場合.
	private static final boolean isFieldByAvailable(Field f) {
		// 対象フィールドがpublicかpurotectedで継承利用可能かチェック.
		final int n = f.getModifiers();
		return Modifier.isPublic(n) || Modifier.isProtected(n);
	}
	
	// 対象がAbstractClass定義かチェック.
	private static final boolean isAbstractClass(Class<?> c) {
		// abstractクラスの場合(非インターフェイス).
		final int md = c.getModifiers();
		return Modifier.isAbstract(md) && !Modifier.isInterface(md);
	}
	
	// ProxyFieldアノテーションが設定されてるFieldを取得.
	private static final Field getProxyField(Class<?> c) {
		Field field;
		final List<Field> list = QuinaCTUtil.getFields(c);
		Field ret = null;
		final int len = list == null ? 0 : list.size();
		for(int i = 0; i < len; i ++) {
			field = list.get(i);
			if(field.isAnnotationPresent(ProxyField.class)) {
				if(!isFieldByAvailable(field)) {
					// アクセス出来ないField定義の場合.
					throw new QuinaException(
						"The Field in the @ProxyField definition " +
						"must be public or protected: " + c.getName());
				} else if(ret != null && !CheckReflection.equalsField(ret, field)) {
					// 複数のFieldに設定されてる場合、その内容が最初に取得した
					// 条件と同一でない場合はエラー出力.
					throw new QuinaException(
						"There are multiple @ProxyField definitions: " +
						c.getName());
				}
				ret = field;
			}
		}
		// 必須定義.
		if(ret == null) {
			// ProxyFieldが存在しない場合例外.
			throw new QuinaException(
				"@ProxyField definition does not exist: " + c.getName());
		}
		return ret;
	}
	
	// ProxyInitialSettingアノテーションが設定されてるMethodを取得.
	private static final Method getProxyInitialSetting(Class<?> c) {
		Method method;
		final List<Method> list = QuinaCTUtil.getMethods(c);
		Method ret = null;
		final int len = list == null ? 0 : list.size();
		for(int i = 0; i < len; i ++) {
			method = list.get(i);
			if(method.isAnnotationPresent(ProxyInitialSetting.class)) {
				if(!isMethodByAvailable(method)) {
					// アクセス出来ないMethod定義の場合.
					throw new QuinaException(
						"The Method in the @ProxyInitialSetting definition " +
						"must be public or protected: " + c.getName());
				} else if(ret != null && !CheckReflection.equalsMethod(ret, method)) {
					// 複数のMethodに設定されてる場合、その内容が最初に取得した
					// 条件と同一でない場合はエラー出力.
					throw new QuinaException(
						"There are multiple @ProxyInitialSetting definitions: " +
						c.getName());
				}
				ret = method;
			}
		}
		// 必須定義.
		if(ret == null) {
			// ProxyInitialSettingアノテーションが存在しない場合例外.
			throw new QuinaException(
				"@ProxyInitialSetting definition does not exist: " + c.getName());
		}
		return ret;
	}
	
	// ProxyInjectMethodアノテーションが設定されてるMethodを取得.
	private static final Method getProxyInjectMethod(Class<?> c) {
		Method method;
		final List<Method> list = QuinaCTUtil.getMethods(c);
		Method ret = null;
		final int len = list == null ? 0 : list.size();
		for(int i = 0; i < len; i ++) {
			method = list.get(i);
			if(method.isAnnotationPresent(ProxyInjectMethod.class)) {
				if(!isMethodByAvailable(method)) {
					// アクセス出来ないMethod定義の場合.
					throw new QuinaException(
						"The Method in the @ProxyInjectMethod definition " +
						"must be public or protected: " + c.getName());
				} else if(ret != null && !CheckReflection.equalsMethod(ret, method)) {
					// 複数のMethodに設定されてる場合、その内容が最初に取得した
					// 条件と同一でない場合はエラー出力.
					throw new QuinaException(
						"There are multiple @ProxyInjectMethod definitions: " +
						c.getName());
				}
				ret = method;
			}
		}
		if(ret != null && ret.getParameterCount() != 0) {
			// 対象のメソッドのパラメータは空である必要がある.
			throw new QuinaException(
				"@ProxyInjectMethod must be an empty parameter definition: " +
				c.getName());
		}
		return ret;
	}
	
	// ProxyOverrideアノテーションが設定されてるMethod群を取得.
	private static final int getProxyOverride(List<Method> out, Class<?> c) {
		Method method;
		final List<Method> list = QuinaCTUtil.getMethods(c);
		out.clear();
		final int len = list == null ? 0 : list.size();
		for(int i = 0; i < len; i ++) {
			method = list.get(i);
			if(method.isAnnotationPresent(ProxyOverride.class)) {
				if(!isMethodByAvailable(method)) {
					// アクセス出来ないMethod定義の場合.
					throw new QuinaException(
						"The Method in the @ProxyOverride definition " +
						"must be public or protected: " + c.getName());
				}
				out.add(method);
			}
		}
		// １つも設定されていない場合.
		if(out.size() <= 0) {
			// ProxyOverrideアノテーションが存在しない場合例外.
			throw new QuinaException(
				"@ProxyOverride definition does not exist: " + c.getName());
		}
		return out.size();
	}
	
	// 対象メソッドがProxyOverride定義であるかチェック.
	private static final boolean isProxyOverride(List<Method> list, Method method) {
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			if(CheckReflection.equalsMethod(method, list.get(i))) {
				return true;
			}
		}
		return false;
	}
	
	// InjectMethodが設定可能かチェック.
	private static final boolean isInjectMethod(
		Method injectMethod, Method targetMethod) {
		if(injectMethod == null) {
			return false;
		}
		Class<?>[] imList = injectMethod.getExceptionTypes();
		int imLen = imList.length;
		// InjectMethodの例外が設定されて無い場合.
		if(imLen <= 0) {
			return true;
		}
		Class<?>[] tarList = targetMethod.getExceptionTypes();
		int tarLen = tarList.length;
		
		// ２つを比較してInjectMethodの例外出力が
		// 対象のメソッドで利用可能な場合.
		Class<?> src;
		for(int i = 0; i < imLen; i ++) {
			src = imList[i];
			for(int j = 0; j < tarLen; j ++) {
				// Exceptionの場合、継承元より継承先が優先される.
				if(CheckReflection.isInstanceof(src, tarList[j])) {
					return true;
				}
			}
		}
		return false;
	}
	
	// 初期設定の呼び出し処理プログラムの生成.
	private static final String createProxyInitialSettingParams(
		Method initSetMethod) {
		StringBuilder buf = new StringBuilder();
		if(initSetMethod != null) {
			final Class<?>[] methodParams = initSetMethod.getParameterTypes();
			final int len = methodParams == null ? 0 : methodParams.length;
			buf.append("super.").append(initSetMethod.getName())
				.append("(").append("\n");
			for(int i = 0; i < len; i ++) {
				if(i != 0) {
					buf.append(",");
				}
				buf.append("(")
					.append(StringUtil.getClassName(methodParams[i]))
					.append(")args.getArgs(").append(i).append(")\n");
			}
			buf.append(");\n");
		}
		return buf.toString();
	}
	
	// 指定ProxyOverrideMethodオブジェクトのメソッドプログラムを生成.
	private static final String createProxyOverrideMethod(
		Method method, Field prxField, Method injectMethod) {
		StringBuilder buf = new StringBuilder();
		// メソッド名定義.
		buf.append(QuinaCTUtil.getAccessModifier(method))
			.append(" ")
			.append(StringUtil.getClassName(
				method.getReturnType()))
			.append(" ")
			.append(method.getName())
			.append("(");
		
		// メソッドの引数名を定義.
		final Class<?>[] list = method.getParameterTypes();
		final int paramsLen = list == null ? 0 : list.length;
		for(int i = 0; i < paramsLen; i ++) {
			if(i != 0) {
				buf.append(", ");
			}
			buf.append(StringUtil.getClassName(list[i]))
				.append(" arg").append(i);
		}
		// メソッドの引数定義終了.
		buf.append(")");
		// 例外が存在する場合は例外出力.
		Class<?>[] exceptions = method.getExceptionTypes();
		if(exceptions != null && exceptions.length > 0) {
			buf.append(" throws ");
			int len = exceptions.length;
			for(int i = 0; i < len; i ++) {
				if(i != 0) {
					buf.append(", ");
				}
				buf.append(StringUtil.getClassName(
					exceptions[i]));
			}
		}
		buf.append(" {\n");
		
		// injectMethodが存在する場合.
		if(isInjectMethod(injectMethod, method)) {
			buf.append("super.")
				.append(injectMethod.getName())
				.append("();\n");
		}
		// 返却が必要な場合.
		if(method.getReturnType() != Void.TYPE) {
			buf.append("return ");
		}
		// ProxyFieldに対する当該メソッド実行.
		buf.append(prxField.getName())
			.append(".")
			.append(method.getName())
			.append("(");
		// 引数をセット.
		for(int i = 0; i < paramsLen; i ++) {
			if(i != 0) {
				buf.append(", ");
			}
			buf.append("arg").append(i);
		}
		// 呼び出し終了.
		buf.append(");\n");
		// メソッド実装終端.
		buf.append("}\n");
		
		// 処理返却.
		return buf.toString();
	}
}
