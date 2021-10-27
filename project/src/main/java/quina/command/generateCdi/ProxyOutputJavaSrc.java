package quina.command.generateCdi;

import static quina.command.generateCdi.GCdiConstants.PROXY_DIRECTORY_NAME;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import quina.annotation.proxy.AnnotationProxyScopedConstants;
import quina.annotation.proxy.ProxyField;
import quina.annotation.proxy.ProxyInitialSetting;
import quina.annotation.proxy.ProxyInjectMethod;
import quina.annotation.proxy.ProxyOverride;
import quina.exception.QuinaException;
import quina.util.FileUtil;
import quina.util.InstanceOf;
import quina.util.StringUtil;

/**
 * ProxyScoped定義に対する自動ソースコード生成処理.
 */
public class ProxyOutputJavaSrc {
	private ProxyOutputJavaSrc() {}
	
	/**
	 * 抽出したRoute定義されたComponentをJavaファイルに出力.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws IOException I/O例外.
	 * @throws ClassNotFoundException クラス非存在例外.
	 */
	public static final void proxyScoped(String outSourceDirectory,
		GCdiParams params)
		throws Exception {
		final List<String> prxList = params.prxList;
		final int len = prxList == null ? 0 : prxList.size();
		
		// 自動生成するProxyオブジェクトのパッケージディレクトリを削除.
		removeDirectory(outSourceDirectory);
		
		// ProxyScopedオブジェクトが存在しない場合.
		if(len <= 0) {
			return;
		}
		
		// Proxyオブジェクトのパッケージディレクトリを生成.
		createDirectory(outSourceDirectory);
		
		// 自動生成オブジェクトの作成.
		for(int i = 0; i < len; i ++) {
			autoProxyClass(outSourceDirectory, prxList.get(i), params);
		}
	}
	
	// 可変引数用オブジェクト.
	private static final String PROXY_SETTING_ARGS =
		"quina.annotation.proxy.ProxySettingArgs";
	
	/**
	 * 抽出したProxyScoped定義されたオブジェクトをJavaファイルに出力.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @param clazzName 対象のクラス名を設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws IOException I/O例外.
	 * @throws ClassNotFoundException クラス非存在例外.
	 */
	private static final void autoProxyClass(String outSourceDirectory,
		String clazzName,GCdiParams params)
		throws IOException, ClassNotFoundException {
		String outDir = outSourceDirectory + "/" + PROXY_DIRECTORY_NAME;
		
		// publicClassで空のPublicコンストラクタが存在するかチェック.
		Class<?> clazz = GCdiUtil.getClass(clazzName, params.cl);
		GCdiUtil.checkPublicClass(clazz);
		
		// このオブジェクトが abstract class 定義であるかチェック.
		if(!isAbstractClass(clazz)) {
			throw new QuinaException(
				"The ProxyScoped class must be an Abstract Class.");
		}
		
		// ProxyScoped定義を取得.
		List<Method> overrideList = new ArrayList<Method>();
		Field prxField = getProxyField(clazz);
		Method initSetMethod = getProxyInitialSetting(clazz);
		Method injectMethod = getProxyInjectMethod(clazz);
		getProxyOverride(overrideList, clazz);
		
		// ProxyFieldのクラスがProxyScopedの継承元と一致しない場合.
		if(!InstanceOf.isInstanceof(clazz, prxField.getType())) {
			throw new QuinaException(
				"The type of ProxyField does not match the inheritance "+
				"source of ProxyScoped.");
		}
		
		// ソース出力先ディレクトリを作成.
		new File(outDir).mkdirs();
		
		int i, len;
		String nextArgs;
		Class<?>[] methodParams;
		Method[] methods;
		
		// 出力先ファイル名を生成.
		String autoClassName = getProxyClassName(clazzName);
		String outFileName = outDir + "/" + autoClassName + ".java";
		BufferedWriter w = null;
		try {
			// ソースコードを出力.
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName)));
			println(w, 0, "package " +
				AnnotationProxyScopedConstants.OUTPUT_AUTO_SOURCE_PROXY_PACKAGE_NAME + ";");
			println(w, 0, "");
			println(w, 0, "/**");
			println(w, 0, " * ProxyScoped ProxyClass automatically generated based on the");
			println(w, 0, " * annotation definition class \"" + clazzName + "\".");
			println(w, 0, " */");
			println(w, 0, "@SuppressWarnings({ \"unchecked\", \"rawtypes\", \"deprecation\" })");
			println(w, 0, "public final class " + autoClassName);
			println(w, 1, "extends " + clazzName+ " {");
			println(w, 1, "");
			
			// initialSettingを生成.
			println(w, 1, "/**");
			println(w, 1, " * Set the required parameters.");
			println(w, 1, " * @param args Set the parameters.");
			println(w, 1, " */");
			println(w, 1, "public final void " +
				AnnotationProxyScopedConstants.INITIAL_SETTING_METHOD +
				"(" + PROXY_SETTING_ARGS +" args) {");
			println(w, 2, "try {");
			if(initSetMethod != null) {
				println(w, 3, "super." + initSetMethod.getName() + "(");
				methodParams = initSetMethod.getParameterTypes();
				len = methodParams == null ? 0 : methodParams.length;
				nextArgs = "";
				for(i = 0; i < len; i ++) {
					println(w, 4, nextArgs +
						"(" + StringUtil.getClassName(
							methodParams[i]) +
						")args.getArgs(" + i + ")");
					nextArgs = ",";
				}
				println(w, 3, ");");
				methodParams = null;
				initSetMethod = null;
			}
			println(w, 2, "} catch(quina.exception.QuinaException qe) {");
			println(w, 3, "throw qe;");
			println(w, 2, "} catch(Exception e) {");
			println(w, 3, "throw new quina.exception.QuinaException(e);");
			println(w, 2, "}");
			
			println(w, 1, "}");
			println(w, 1, "");
			
			// 残りのメソッド群を生成.
			methods = prxField.getType().getMethods();
			len = methods == null ? 0 : methods.length;
			for(i = 0; i < len; i ++) {
				// @ProxyOverride条件は自動ソースで出力しない.
				if(isProxyOverride(overrideList, methods[i])) {
					continue;
				}
				// 未定義のメソッドを自動生成.
				println(w, 0, getMethodOut(
					1, methods[i], prxField, injectMethod));
			}
			// オブジェクト終端.
			println(w, 0, "}");
			
			w.close();
			w = null;
			
		} finally {
			if(w != null) {
				try {
					w.close();
				} catch(Exception e) {}
			}
		}
	}
	
	// 指定Methodオブジェクトのメソッド名定義
	private static final String getMethodOut(
		int space, Method method, Field prxField, Method injectMethod) {
		StringBuilder buf = new StringBuilder();
		// メソッド名定義.
		print(buf, space).append("@Override\n");
		print(buf, space)
			.append(GCdiUtil.getAccessModifier(method))
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
			buf.append("\n");
			print(buf, space + 1);
			if(i != 0) {
				buf.append(",");
			}
			buf.append(StringUtil.getClassName(list[i]))
				.append(" arg").append(i);
		}
		if(paramsLen > 0) {
			buf.append("\n");
			print(buf, space).append(")");
		} else {
			buf.append(")");
		}
		// 例外が存在する場合は例外出力.
		Class<?>[] exceptions = method.getExceptionTypes();
		if(exceptions != null && exceptions.length > 0) {
			if(paramsLen > 0) {
				buf.append(" ");
			} else {
				buf.append("\n");
				print(buf, space + 1);
			}
			buf.append("throws ");
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
			print(buf, space + 1)
				.append("super.")
				.append(injectMethod.getName())
				.append("();\n");
		}
		// 既存の処理内容を呼び出す.
		print(buf, space + 1);
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
		print(buf, space).append("}\n");
		
		// 処理返却.
		return buf.toString();
	}
	
	// 出力処理.
	private static final void println(Writer w, int tab, String... s)
		throws IOException {
		StringUtil.println(w, tab, s);
	}
	
	// StringBuilderにインデントをセット.
	private static final StringBuilder print(StringBuilder buf, int tab, String... s) {
		return StringUtil.print(buf, tab, s);
	}
	
	// 出力先のパッケージの削除処理.
	public static final void removeDirectory(String outSourceDirectory)
		throws Exception {
		try {
			FileUtil.delete(
				outSourceDirectory + "/" +PROXY_DIRECTORY_NAME);
		} catch(Exception e) {
		}
	}
	
	// 出力先のパッケージディレクトリ生成処理.
	private static final void createDirectory(String outSourceDirectory)
		throws Exception {
		FileUtil.mkdirs(
			outSourceDirectory + "/" +PROXY_DIRECTORY_NAME);
	}
	
	// ProxyScopedの自動作成されるクラス名を取得.
	private static final String getProxyClassName(
		String srcClassName) {
		return new StringBuilder
			(AnnotationProxyScopedConstants.HEAD_PROXY_CLASS_NAME)
			.append(GCdiUtil.getClassNameByCutPackageName(srcClassName))
			.toString();
	}
	
	// 対象のメソッドがPublicかProtected定義の場合.
	private static final boolean isPublicProtectedMethod(Method m) {
		int n = m.getModifiers();
		return Modifier.isPublic(n) || Modifier.isProtected(n);
	}
	
	// 対象のメソッドがPublicかProtected定義の場合.
	private static final boolean isPublicProtectedField(Field f) {
		int n = f.getModifiers();
		return Modifier.isPublic(n) || Modifier.isProtected(n);
	}
	
	// 対象がAbstractClass定義かチェック.
	private static final boolean isAbstractClass(Class<?> c) {
		int md = c.getModifiers();
		if(Modifier.isAbstract(md) && !Modifier.isInterface(md)) {
			return true;
		}
		return false;
	}
	
	// ProxyFieldアノテーションが設定されてるFieldを取得.
	private static final Field getProxyField(Class<?> c) {
		Field field;
		final List<Field> list = GCdiUtil.getFields(c);
		Field ret = null;
		final int len = list == null ? 0 : list.size();
		for(int i = 0; i < len; i ++) {
			field = list.get(i);
			if(field.isAnnotationPresent(ProxyField.class)) {
				if(!isPublicProtectedField(field)) {
					// アクセス出来ないField定義の場合.
					throw new QuinaException(
						"The Field in the @ProxyField definition " +
						"must be public or protected: " + c.getName());
				} else if(ret != null && !InstanceOf.equalsField(ret, field)) {
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
		final List<Method> list = GCdiUtil.getMethods(c);
		Method ret = null;
		final int len = list == null ? 0 : list.size();
		for(int i = 0; i < len; i ++) {
			method = list.get(i);
			if(method.isAnnotationPresent(ProxyInitialSetting.class)) {
				if(!isPublicProtectedMethod(method)) {
					// アクセス出来ないMethod定義の場合.
					throw new QuinaException(
						"The Method in the @ProxyInitialSetting definition " +
						"must be public or protected: " + c.getName());
				} else if(ret != null && !InstanceOf.equalsMethod(ret, method)) {
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
		final List<Method> list = GCdiUtil.getMethods(c);
		Method ret = null;
		final int len = list == null ? 0 : list.size();
		for(int i = 0; i < len; i ++) {
			method = list.get(i);
			if(method.isAnnotationPresent(ProxyInjectMethod.class)) {
				if(!isPublicProtectedMethod(method)) {
					// アクセス出来ないMethod定義の場合.
					throw new QuinaException(
						"The Method in the @ProxyInjectMethod definition " +
						"must be public or protected: " + c.getName());
				} else if(ret != null && !InstanceOf.equalsMethod(ret, method)) {
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
	
	// ProxyInitialSettingアノテーションが設定されてるMethodを取得.
	private static final int getProxyOverride(List<Method> out, Class<?> c) {
		Method method;
		final List<Method> list = GCdiUtil.getMethods(c);
		out.clear();
		final int len = list == null ? 0 : list.size();
		for(int i = 0; i < len; i ++) {
			method = list.get(i);
			if(method.isAnnotationPresent(ProxyOverride.class)) {
				if(!isPublicProtectedMethod(method)) {
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
			if(InstanceOf.equalsMethod(method, list.get(i))) {
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
				if(InstanceOf.isInstanceof(src, tarList[j])) {
					return true;
				}
			}
		}
		return false;
	}
}
