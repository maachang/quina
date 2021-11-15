package quina.jdbc.console.route;

import quina.annotation.Switch;
import quina.annotation.component.ResponseSwitch;
import quina.annotation.route.FilePath;
import quina.annotation.route.Route;
import quina.component.file.FileComponent;

/**
 * qunaJdbc用コンソール用HTMLファイルのI/O用FileComponent.
 * 
 * 一時的に利用(Native化では、Resource内にセット予定).
 */
@Route("/quina/jdbc/console/*")
@FilePath("./console/")
@ResponseSwitch(cache=Switch.Off)
public class ConsoleHtml extends FileComponent {
}
