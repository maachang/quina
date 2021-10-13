package quina.annotation.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ProxyScopedアノテーション定義.
 * 
 * Proxyアノテーションでは ProxyClass の
 * 代替え定義を行います.
 * 
 * 理由としてGraalVMのnative-imageでは、ProxyClassを
 * 利用する場合、別途定義が必要になります。
 * 
 * これらの定義をなるべく行わないようにするための
 * 回避措置として、ProxyScopedを利用します.
 * 
 * ＜例＞
 * 
 * ＠ProxyScoped
 * public abstract class ProxyConnection
 *   implements Connection {
 *   
 *   private Queue queue;
 *   
 *   ＠ProxyField
 *   private Connection connection;
 *   
 *   // 初期設定.
 *   ＠ProxyInitalSetting
 *   protected ProxyConnection initialSetting(
 *     Queue queue, Connection connection) {
 *     this.queue = queue;
 *     this.connection = connection;
 *   }
 *   
 *   ＠ProxyOverride
 *   public void close()
 *     throws SQLException {
 *     queue.offer(connection);
 *     connection = null;
 *   }
 *   
 *   public void destroy() {
 *     try {
 *       connection.close();
 *     } catch(Exception e) {}
 *   }
 *   
 *   ＠ProxyInjectMethod
 *   protected void checkClose()
 *     throws SQLException {
 *     if(connection == null) {
 *       throw new SQLException(
 *         "既にクローズ済み.");
 *     }
 *   }
 * }
 * 
 * Queue<Connection> queue =
 *   new ConcurrentLinkedQueue<Connection>();
 * 
 * public Connection getConnection() {
 *   Connection conn = queue.poll();
 *   if(conn != null) {
 *     return conn;
 *   }
 * 
 *   conn = DriverManager.getConnection(....);
 * 
 *   return (Connection)ProxyScopedManager
 *     .getObject(ProxyConnection.class, queue, conn);
 * }
 * 
 * 上記によって Connection.close() で Queueに設定
 * されるプーリングが行わるようになります.
 * 
 * ＠ProxyScopedアノテーション定義先のクラスは
 * 必ず"abstract"で定義する必要があります.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProxyScoped {
}
