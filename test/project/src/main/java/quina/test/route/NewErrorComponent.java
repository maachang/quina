package quina.test.route;

import quina.annotation.route.ErrorRoute;
import quina.component.ErrorComponent;
import quina.http.Request;
import quina.http.server.response.NormalResponse;

@ErrorRoute
public class NewErrorComponent implements ErrorComponent {

	@Override
	public void call(int state, boolean restful, Request req, NormalResponse res, Throwable e) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

}
