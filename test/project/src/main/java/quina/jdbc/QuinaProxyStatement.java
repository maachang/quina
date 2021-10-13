package quina.jdbc;

import java.sql.Statement;

import quina.annotation.proxy.ProxyField;
import quina.annotation.proxy.ProxyScoped;

/**
 * QinaProxyStatement.
 */
@ProxyScoped
public abstract class QuinaProxyStatement
	extends AbstractQuinaProxyStatement
	implements Statement {
	
	// 元のStatement.
	@ProxyField
	protected Statement statement;
	
	@Override
	protected Statement getStatement() {
		return statement;
	}
	
	@Override
	protected void setStatement(Statement s) {
		statement = s;
	}
}
