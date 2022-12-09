package quina.jdbc.console.route;

import quina.compile.cdi.annotation.Inject;
import quina.component.file.ResourceFileComponent;
import quina.http.Request;
import quina.http.Response;
import quina.jdbc.console.JDBCConsoleService;
import quina.route.annotation.ResourcePackage;
import quina.route.annotation.Route;

/**
 * qunaJdbc用コンソール用HTMLファイルのI/O用
 * ResourceFileComponent経由のアクセスで処理します.
 */
//以下@Routerにコメントがある時は、ルーター登録されない.
@Route("/quina/jdbc/console/*")
@ResourcePackage("quina.jdbc.console.resource")
public class ConsoleHtmlByResource
	extends ResourceFileComponent {
	
	// JDBCコンソールサービス.
	@Inject
	private JDBCConsoleService service;
	
	@Override
	public void call(Request req, Response<?> res) {
		// ipアクセス制御.
		service.checkAccessControll(req, res);
		// 問題なければファイル返却.
		super.call(req, res);
	}
}
