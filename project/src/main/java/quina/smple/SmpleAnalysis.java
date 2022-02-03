package quina.smple;

/**
 * Simple-Template解析.
 */
public class SmpleAnalysis {
	
	/*
	simple template例を以下に示す.
	 
	＜abc.def.Exsample.sml＞
	-------------------------------------------------
	$mple: {
	  // package.
	  package: [
	    java.util.*,
	    java.io.*,
	  ],
	  // beans.
	  beans: {
	    // UserInfo.
	    UserInfo: [
	      String name,
	      int age,
	      String comment,
	    ]
	  },
	  // values.
	  values [
	    $mple.beans.UserInfo userInfo,
	    List<String> list,
	    Map<String, Object> options,
	  ]
	}
	name: ${userInfo.name}
	age: ${userInfo.age}
	comment: ${userInfo.comment}
	
	<%for(int i = 0; i < list.size(); i ++) {%>
	list[${i}]: ${list.get(i)}
	<%}%>
	
	<%if(options.containsKey("hoge") {%>
	map[hoge]: ${options.get("hoge")}
	<%}%>
	-------------------------------------------------
	
	smple JSONを定義することで、Javaのテンプレートに対する
	パラメータ定義を行う事が出来る.
	
	またコメントなどの設定は Json内のみ定義が可能.
	コメントは (半角)[／／]  [／＊ ＊／] [＃] のみ.
	
	基本的にsmpleでのテンプレート定義は、テンプレート部分と
	プログラム実行部分に分離される.
	
	
	定義された smple定義のテンプレートは 条件にもよるが
	たとえばJava実行用のテンプレート定義された場合は、以下の
	ように定義される.
	  ●javaテンプレート展開:
	    javaリソースの場合、以下のJavaオブジェクト定義となる.
	     ＜例＞abc/def/exsample.sml(リソース格納のSmpleテンプレート
	           ファイル)
	     展開先
	      quinax.smple.abc_def_Exsample.java
	    
	  ●json定義説明:
	    $mple.package以下の定義は、対象がJavaの場合においてテンプレート
	    をJava変換する際のPackage定義を行う必要がある.
	    
	    $mple.beans以下のBena定義ファイルは以下のように静的クラスで
	    定義される.
	     ＜例＞$mple.beans.UserInfo
	     展開先
	      quinax.smple.abc_Exsample$UserInfo.class
	    これらのbeansは対象オブジェクトのパラメータとして利用することが
	    出来る.
	    またこの時のbean定義はそれぞれgetter/setter定義され、呼び出し定義と
	    して${}の場合のみgetterを省略(abcならgetterでbean.getAbc()だが、
	    この場合は${bean.abc}で[bean.getAbc()]と内部変換されて利用可能)
	    (ただし<% %>は通常通り<%= bean.getAbc() %>の定義となる).
	    
	    $mple.values定義で$mple.beans定義のオブジェクトの場合を利用する
	    場合は、以下のように定義する.
	     ＞ $mple.beans.UserInfo userInfo
	    このように定義することで、対象のuserInfo定義を利用することができる.
	    
	    また、他のsmple定義のbeanを利用したい場合は別途quina.smple.beans定義
	    を利用する必要がある.
	    
	    $mple.values以下の定義は、以下のquina.smple.Smpleインターフェイス
	    に則ったテンプレート実行に対するパラメータを定義.
	
	またテンプレートの解析に関して java 形式の解析と javascript形式が
	存在するが、graalvm の native-image で利用する場合はJavaの利用のみ
	となる.
	  ※理由として、native変換でjavascirptを利用するとアホみたいな
	    リソース(ファイル容量)とコンパイル時間を要するので、
	    javascript向けのsmpleは利用推奨しない.
	*/
	
	// smple定義用のJsonシンボル.
	private static final String JSON_SIMBLE = "$mple";
	
	/**
	 * smpleコンパイル実行.
	 * @param template テンプレート情報を設定します.
	 * @return String smpleコンパイル結果が返却されます.
	 */
	public static final String compile(String template) {
		return null;
		
		
		
	}
	
	// $mple: { ... } の開始から終端までのポジションを取得.
	private static final int[] jsonPos(String template, int off) {
		return null;
	}
	
	// $mple: { を検索.
	private static final int firstJsonPos(String template, int off) {
		return -1;
	}
	
	
}
