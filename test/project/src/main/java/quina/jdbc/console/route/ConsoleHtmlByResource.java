package quina.jdbc.console.route;

import quina.annotation.cdi.Inject;
import quina.annotation.route.ResourcePackage;
import quina.annotation.route.Route;
import quina.component.file.ResourceFileComponent;
import quina.http.Request;
import quina.http.Response;
import quina.jdbc.console.JDBCConsoleService;

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
		service.checkAccessControll(req);
		// 問題なければファイル返却.
		super.call(req, res);
	}
}
