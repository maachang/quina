package quina.test;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.graalvm.polyglot.Context;

public class QuinaJsTest {
	
	public static final void main(String[] args) {
		Context ctx = Context.newBuilder()
			// これをつけることで、putMemberのJavaオブジェクトに
			// アクセスできる.
			.allowAllAccess(true)
			.build();
		Map<String, String> map =
			new HashMap<> ();
		map.put("hoge", "moge");
		ctx.getBindings("js").putMember("hoge", map);
		
		ctx.eval("js", "print(hoge.get('hoge'))");
		ByteArrayOutputStream bo;
		
	}
}
