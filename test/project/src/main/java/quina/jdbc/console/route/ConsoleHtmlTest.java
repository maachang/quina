package quina.jdbc.console.route;

import quina.annotation.Switch;
import quina.annotation.cdi.Inject;
import quina.annotation.component.ResponseSwitch;
import quina.annotation.route.FilePath;
import quina.component.file.FileComponent;
import quina.http.Request;
import quina.http.Response;
import quina.jdbc.console.JDBCConsoleService;

/**
 * qunaJdbc用コンソール用HTMLファイルのI/O用FileComponent.
 * 
 * 一時的に利用(Native化では、Resource内にセット).
 * 
 */
//以下@Routerにコメントがある時は、ルーター登録されない.
//@Route("/quina/jdbc/console/public/*")
@FilePath("./console/")
@ResponseSwitch(cache=Switch.Off)
public class ConsoleHtmlTest extends FileComponent {
	
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
