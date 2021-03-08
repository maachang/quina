package quina.command.shutdown;

/**
 * コマンド実行.
 */
public class Command {

	// バージョン.
	private static final String VERSION = "0.0.1";

	/**
	 * メイン処理.
	 * @param args
	 * @throws Exception
	 */
	public static final void main(String[] args) throws Exception {
		Command cmd = new Command(args);
		try {
			cmd.execute();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/** プログラム引数の管理. **/
	private Args args;

	/**
	 * コンストラクタ.
	 * @param args
	 */
	private Command(String[] args) {
		this.args = new Args(args);
	}

	/**
	 * ヘルプ情報を表示.
	 */
	private void outHelp() {
		System.out.println("Command to shut down quina.");
		System.out.println();
		System.out.println("With this command, you can shut down the running quina.");
		System.out.println();
		System.out.println("Usage: quinaExit [options]");
		System.out.println(" where options include:");

		System.out.println("  -v [--version]");
		System.out.println("     version information .");
		System.out.println("  -h [--help]");
		System.out.println("     the help contents.");
		System.out.println("  -t [--token] {token}");
		System.out.println("     the token for shutdown.");
		System.out.println("     If not set, the default value will be assigned.");
		System.out.println("  -p [--port] {port}");
		System.out.println("     Set the shutdown port for quina.");
		System.out.println("     If not set, " + ShutdownConstants.getPort() +
			" will be assigned.");
		System.out.println("  -o [--timeout] {timeout}");
		System.out.println("     Set the receive timeout value.");
		System.out.println("     If not set, " + ShutdownConstants.getTimeout() +
			" milliseconds will be assigned.");
		System.out.println("  -r [--retry] {retry}");
		System.out.println("     Set the number of retries.");
		System.out.println("     If not set, " + ShutdownConstants.getRetry() +
			" retries will be assigned.");
		System.out.println();
	}

	/**
	 * コマンド実行.
	 */
	public void execute() {
		// バージョンを表示.
		if(args.isValue("-v", "--version")) {
			System.out.println(VERSION);
			System.exit(0);
			return;
		// ヘルプ内容を表示.
		} else if(args.isValue("-h", "--help")) {
			outHelp();
			System.exit(0);
			return;
		}

		String token = ShutdownConstants.DEFAULT_TOKEN;
		int port = ShutdownConstants.getPort();
		int timeout = ShutdownConstants.getTimeout();
		int retry = ShutdownConstants.getRetry();

		String e = args.get("-t", "--token");
		if(e != null) {
			token = e;
		}
		if((e = args.get("-p", "--port")) != null) {
			if(Args.isNumeric(e)) {
				port = Integer.parseInt(e);
			} else {
				System.err.println("[ERROR] The port number must be set numerically: " + e);
				System.exit(1);
				return;
			}
		}
		if((e = args.get("-o", "--timeout")) != null) {
			if(Args.isNumeric(e)) {
				timeout = Integer.parseInt(e);
			} else {
				System.err.println("[ERROR] The timeout value must be set numerically: " + e);
				System.exit(1);
				return;
			}
		}
		if((e = args.get("-r", "--retry")) != null) {
			if(Args.isNumeric(e)) {
				retry = Integer.parseInt(e);
			} else {
				System.err.println("[ERROR] The number of retries must be set numerically: " + e);
				System.exit(1);
				return;
			}
		}

		// 詳細出力.
		SendShutdown.setVerbose(true);

		System.out.println("Sends a shutdown signal to quina.");
		boolean res = SendShutdown.send(token, port, timeout, retry);
		if(res) {
			System.out.println("You have successfully shut down quina.");
			System.exit(0);
			return;
		} else {
			System.err.println("[ERROR] Failed to shut down quina.");
			System.exit(1);
			return;
		}
	}
}
