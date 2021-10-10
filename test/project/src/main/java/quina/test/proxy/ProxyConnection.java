package quina.test.proxy;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;

import quina.annotation.reflection.ProxyField;
import quina.annotation.reflection.ProxyInitialSetting;
import quina.annotation.reflection.ProxyInjectMethod;
import quina.annotation.reflection.ProxyOverride;
import quina.annotation.reflection.ProxyScoped;
import quina.util.Flag;

@ProxyScoped
public abstract class ProxyConnection
	implements Connection {
	
	@ProxyField
	protected Connection connection;
	
	private Queue<Connection> queue;
	private final Flag closeFlag = new Flag(true);
	
	@ProxyInitialSetting
	protected void setting(Queue<Connection> queue,
		Connection connection) {
		this.queue = queue;
		this.connection = connection;
		this.closeFlag.set(false);
	}
	
	@ProxyOverride
	public void close() throws SQLException {
		if(!closeFlag.setToGetBefore(true)) {
			queue.offer(this);
		}
	}
	
	public void reOpen() {
		closeFlag.set(false);
	}
	
	@ProxyInjectMethod
	public void checkClose() throws SQLException {
		if(closeFlag.get()) {
			throw new SQLException("");
		}
	}
}
