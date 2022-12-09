package quina.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import quina.util.collection.js.JsWrapperList;

public class QuinaJsTest {
	
	public static final void main(String[] args) {
		Context ctx = Context.newBuilder()
			// これをつけることで、putMemberのJavaオブジェクトに
			// アクセスできる.
			.allowAllAccess(true)
			.build();
		
		Value value = ctx.getBindings("js");
		
		/*
		
		// MapをJsのObjectで表現する場合.
		Map<String, Object> map = new HashMap<>();
		map.put("hoge", "moge");
		
		value.putMember("xyz", map);
		value.putMember("hoge", ProxyObject.fromMap(map));
		
		// ListをJsのArrayで表現する場合.
		List<Object> list = new ArrayList<>();
		list.add("abc");
		value.putMember("list", ProxyArray.fromList(list));
		
		System.out.println("# toJava(Map)");
		
		// putMemberでMapを直接した場合の呼び出し.
		// ProxyObject.fromMapの場合はエラーとなる.
		ctx.eval("js", "print(xyz.get('hoge'))");
		
		System.out.println("\n# ProxyObject or ProxyArray");
		
		// ProxyObject.fromMapや
		// ProxyArray.fromListの場合の呼び出し.
		ctx.eval("js",
			"print(hoge.hoge);"
			+ "print(list[0]);"
			+ "print(list.length);"
		);
		
		System.out.println("\n# return js");
		
		// 戻り値の取得サンプル.
		// (function() { return "hoge" })();
		// 的な形で返却結果が取得できるか。
		Value v = ctx.eval("js",
			"(function() {'use strict';\n" +
			"return 'hoge_' + hoge.hoge;\n" +
			"\n})();\n"
		);
		System.out.println(v.asString());
		*/
		
		/*
		// パラメータ.
		Object params = JsArray.of(
			JsObject.of(
				"name", "Hoge",
				"fields", JsArray.of(
					"abc",
					"def"
				)
			)
			,
			JsObject.of(
				"name", "Xyz",
				"fields", JsArray.of(
					"ryo",
					"kaori"
				)
			)
		);
		
		System.out.println("params: " + params);
		
		value.putMember("args", params);
		ctx.eval("js",
			"print(args);"
			+ "print(args.length);"
			+ "print(args[0]);"
			+ "print(args[0].name);"
			+ "print(args[0].fields);"
			+ "print(args[0].fields.length);"
		);
		*/
		
		List<Object> list = new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		list.add(map);
		List<Object> list2 = new ArrayList<>();
		map.put("hoge", "moge");
		map.put("list", list2);
		list2.add("xyz");
		
		value.putMember("args", JsWrapperList.from(list));
		ctx.eval("js",
			"print(args.length);"
			+"print(args[0].hoge);"
			+"print(args[0].list.length);"
			+"print(args[0].list[0]);"
		);
		
	}
}
