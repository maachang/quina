package quina.compile.cdi;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import quina.compile.QuinaCTConstants;
import quina.compile.QuinaCTParams;
import quina.compile.QuinaCTUtil;
import quina.exception.QuinaException;
import quina.util.collection.js.JsArray;
import quina.util.collection.js.JsObject;

public final class CdiOutputJavaSrc {
	private CdiOutputJavaSrc() {}
	
	// 対象のクラスがPublic定義で空のpublicコンストラクタが
	// 利用可能かチェック.
	private static final boolean isPublicClass(
		String clazzName, QuinaCTParams params)
		throws ClassNotFoundException {
		return isPublicClass(QuinaCTUtil.getClass(clazzName, params.cl));

	}
	
	// 対象のクラスがPublic定義で空のpublicコンストラクタが
	// 利用可能かチェック.
	private static final boolean isPublicClass(Class<?> c) {
		if(QuinaCTConstants.isDefineAnnotation(c) ||
			QuinaCTConstants.isProxyAnnotation(c)) {	
			QuinaCTUtil.checkPublicClass(c);
			return true;
		}
		return false;
	}
	
	// 対象フィールドがCdiFieldでfinal定義でないかチェック.
	private static final boolean isCdiField(Class<?> c, Field f) {
		// 何らかのアノテーション定義が存在する場合.
		if(f.getAnnotations().length > 0) {
			// フィールドが final 定義の場合エラー.
			if(Modifier.isFinal(f.getModifiers())) {
				throw new QuinaException(
					"The specified field (class: " +
					c.getName() + " field: " + f.getName() +
					") is the final definition while the Cdi " +
					"injection annotation definition. ");
			}
			return true;
		}
		return false;
	}
	
	// CDIディレクトリ名を取得.
	private static final String outCdiDirectory(String outSourceDirectory) {
		return outSourceDirectory + "/" +
			QuinaCTConstants.CDI_DIRECTORY_NAME;
	}
	
	/**
	 * 抽出したRoute定義されたComponentをJavaファイルに出力.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws IOException I/O例外.
	 * @throws ClassNotFoundException クラス非存在例外.
	 */
	public static final void routerScoped(
		String outSourceDirectory, QuinaCTParams params)
		throws IOException, ClassNotFoundException {
		// JSパラメータ.
		JsArray jsParam = new JsArray();
		
		// 通常Componentの出力.
		String clazzName;
		int len = params.routeList.size();
		for(int i = 0; i < len; i ++) {
			clazzName = params.routeList.get(i);
			// pubilcのクラス定義のみ対象とする.
			if(isPublicClass(clazzName, params)) {
				// routeメソッド追加定義を行う.
				jsParam.addObject(
					new JsObject(
						"method", "route"
						,"name", clazzName
					)
				);
			}
		}
		
		// AnyComponentの出力.
		if(params.any != null) {
			// pubilcのクラス定義のみ対象とする.
			if(isPublicClass(params.any, params)) {
				// anyメソッド追加定義を行う.
				jsParam.addObject(
					new JsObject(
						"method", "any"
						,"name", params.any
					)
				);
			}
		}
		
		// ErrorComponentの出力.
		Class<?> c;
		len = params.errList.size();
		for(int i = 0; i < len; i ++) {
			clazzName = params.errList.get(i);
			c = QuinaCTUtil.getClass(clazzName, params);
			// pubilcのクラス定義のみ対象とする.
			if(isPublicClass(c)) {
				// anyメソッド追加定義を行う.
				jsParam.addObject(
					new JsObject(
						"method", "error"
						,"name", clazzName
					)
				);
			}
		}
		
		// [LoadRouter.java]のJavaファイルを出力.
		CdiOutputJsSmpleOut.executeSmpleToOutputJavaFile(
			outCdiDirectory(outSourceDirectory),
			QuinaCTConstants.AUTO_ROUTE_SOURCE_NAME,
			jsParam, null);
	}
	
	/**
	 * 抽出したServiceScoped定義されたオブジェクトをJavaファイルに出力.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws IOException I/O例外.
	 * @throws ClassNotFoundException クラス非存在例外.
	 */
	public static final void serviceScoped(
		String outSourceDirectory, QuinaCTParams params)
		throws IOException, ClassNotFoundException {
		
		// JSパラメータ.
		JsArray jsParam = new JsArray();
		
		// ServiceScopedクラス名を取得.
		String clazzName;
		int len = params.cdiList.size();
		for(int i = 0; i < len; i ++) {
			clazzName = params.cdiList.get(i);
			// pubilcのクラス定義のみ対象とする.
			if(isPublicClass(clazzName, params)) {
				jsParam.addObject(clazzName);
			}
		}
		
		// [LoadCdiService.java]のJavaファイルを出力.
		CdiOutputJsSmpleOut.executeSmpleToOutputJavaFile(
			outCdiDirectory(outSourceDirectory),
			QuinaCTConstants.CDI_SERVICE_SOURCE_NAME,
			jsParam, null);
	}
	
	/**
	 * 抽出したQuinaServiceScoped定義されたオブジェクトをJavaファイルに出力.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws IOException I/O例外.
	 * @throws ClassNotFoundException クラス非存在例外.
	 */
	public static final void quinaServiceScoped(
		String outSourceDirectory, QuinaCTParams params)
		throws IOException, ClassNotFoundException {
		
		// JSパラメータ.
		JsArray jsParam = new JsArray();
		
		String clazzName;
		int len = params.qsrvList.size();
		for(int i = 0; i < len; i ++) {
			clazzName = params.qsrvList.get(i);
			// pubilcのクラス定義のみ対象とする.
			if(isPublicClass(clazzName, params)) {
				jsParam.addObject(clazzName);
			}
		}
		
		// [LoadQuinaService.java]のJavaファイルを出力.
		CdiOutputJsSmpleOut.executeSmpleToOutputJavaFile(
			outCdiDirectory(outSourceDirectory),
			QuinaCTConstants.QUINA_SERVICE_SOURCE_NAME,
			jsParam, null);
	}
	
	/**
	 * 抽出したCdiInjectField定義されたオブジェクトをJavaファイルに出力.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws IOException I/O例外.
	 * @throws ClassNotFoundException クラス非存在例外.
	 */
	public static final void cdiInjectField(
		String outSourceDirectory, QuinaCTParams params)
		throws IOException, ClassNotFoundException {
		
		// JSパラメータ.
		JsArray jsParam = new JsArray();
		
		Class<?> c;
		String clazzName;
		int len = params.injFdList.size();
		for(int i = 0; i < len; i ++) {
			clazzName = params.injFdList.get(i);
			c = QuinaCTUtil.getClass(clazzName, params);
			
			// subClassを含むFieldをすべて取得.
			final List<Field> list = QuinaCTUtil.getFields(c);
			final int lenJ = list.size();
			
			// フィールド定義を生成.
			JsArray fieldsList = new JsArray();
			for(int j = 0; j < lenJ; j ++) {
				// Cdiアノテーションがあってfinal定義でない場合.
				if(isCdiField(c, list.get(j))) {
					// フィールド定義にフィールド内容をセット.
					fieldsList.addObject(
						new JsObject(
							"name", list.get(j).getName()
							,"isStatic", Modifier.isStatic(
								list.get(j).getModifiers())
						)
					);
				}
			}
			
			// 出力するフィールド定義が存在する場合.
			if(fieldsList.objectSize() > 0) {
				jsParam.addObject(
					new JsObject(
						"name", clazzName
						,"fields", fieldsList
					)
				);
			}
		}
		
		// [LoadCdiInjectField.java]のJavaファイルを出力.
		CdiOutputJsSmpleOut.executeSmpleToOutputJavaFile(
			outCdiDirectory(outSourceDirectory),
			QuinaCTConstants.CDI_INJECT_FIELD_SOURCE_NAME,
			jsParam, null);
	}
	
	/**
	 * 抽出したCdiHandleScoped定義されたオブジェクトをJavaファイルに出力.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws IOException I/O例外.
	 * @throws ClassNotFoundException クラス非存在例外.
	 */
	public static final void cdiHandle(
		String outSourceDirectory, QuinaCTParams params)
		throws IOException, ClassNotFoundException {
		
		// JSパラメータ.
		JsArray jsParam = new JsArray();
		
		String clazzName;
		final int len = params.hndList.size();
		for(int i = 0; i < len; i ++) {
			clazzName = params.hndList.get(i);
			// pubilcのクラス定義のみ対象とする.
			if(isPublicClass(clazzName, params)) {
				jsParam.addObject(clazzName);
			}
		}
		
		// [LoadCdiAnnotationHandle.java]のJavaファイルを出力.
		CdiOutputJsSmpleOut.executeSmpleToOutputJavaFile(
			outCdiDirectory(outSourceDirectory),
			QuinaCTConstants.CDI_HANDLE_SOURCE_NAME,
			jsParam, null);
	}
	
	/**
	 * 抽出したProxyScoped定義されたオブジェクトをJavaファイルに出力.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws IOException I/O例外.
	 * @throws ClassNotFoundException クラス非存在例外.
	 */
	public static final void proxyScoped(String outSourceDirectory,
		QuinaCTParams params)
		throws IOException, ClassNotFoundException {
		
		// JSパラメータ.
		JsArray jsParam = new JsArray();
		
		String clazzName;
		final int len = params.prxList.size();
		for(int i = 0; i < len; i ++) {
			clazzName = params.prxList.get(i);
			// pubilcのクラス定義のみ対象とする.
			if(isPublicClass(clazzName, params)) {
				jsParam.addObject(
					new JsObject(
						"name", clazzName
						,"proxyName", QuinaCTUtil.getAutoProxyClassName(
							clazzName)
					)
				);
			}
		}
		
		// [LoadProxyScoped.java]のJavaファイルを出力.
		CdiOutputJsSmpleOut.executeSmpleToOutputJavaFile(
			outCdiDirectory(outSourceDirectory),
			QuinaCTConstants.CDI_PROXY_SCOPED_SOURCE_NAME,
			jsParam, null);
	}
	
	
	/**
	 * 抽出したQuinaLoopScoped定義されたオブジェクトをJavaファイルに出力.
	 * @param outSourceDirectory 出力先ディレクトリを設定します.
	 * @param params GenerateGciパラメータを設定します.
	 * @throws IOException I/O例外.
	 * @throws ClassNotFoundException クラス非存在例外.
	 */
	public static final void quinaLoopScoped(
		String outSourceDirectory, QuinaCTParams params)
		throws IOException, ClassNotFoundException {
		
		// JSパラメータ.
		JsArray jsParam = new JsArray();
		
		String clazzName;
		final int len = params.loopList.size();
		for(int i = 0; i < len; i ++) {
			clazzName = params.loopList.get(i);
			// pubilcのクラス定義のみ対象とする.
			if(isPublicClass(clazzName, params)) {
				jsParam.addObject(clazzName);
			}
		}
		
		// [LoadQuinaLoopElement.java]のJavaファイルを出力.
		CdiOutputJsSmpleOut.executeSmpleToOutputJavaFile(
			outCdiDirectory(outSourceDirectory),
			QuinaCTConstants.QUINA_LOOP_SCOPED_SOURCE_NAME,
			jsParam, null);
	}

}
