$smple {
  // name.
  name: abc.def.Exsample,
  // packages.
  packages: [
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
  values: [
    $smple.beans.UserInfo userInfo,
    List<String> list,
    Map<String\, Object> options,
  ]
}
name: ${userInfo.getName()}
age: ${userInfo.getAge()}
comment: ${userInfo.getComment()}

<%for(int i = 0; i < list.size(); i ++) {%>
list[${i}]: ${list.get(i)}
<%}%>

<%if(options.containsKey("hoge")) {%>
map["hoge"]: ${options.get("hoge")}
<%}%>