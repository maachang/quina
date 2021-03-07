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

	public void outHelp() {
		System.out.println("quinaExit varsion " + VERSION);
		System.out.println(" [params]");

		System.out.println("  -v [--version] Display version information .");
		System.out.println("  -h [--help] Display the help contents.");
		System.out.println("  -t [--token] Set the token for shutdown.");
		System.out.println("    If not set, the default value will be assigned.");
		System.out.println("  -p [--port] Set the shutdown port for quina.");
		System.out.println("    If not set, " + ShutdownConstants.getPort() + " will be assigned.");
		System.out.println("  -o [--timeout] Set the receive timeout value.");
		System.out.println("    If not set, " + ShutdownConstants.getTimeout() + " msec will be assigned.");
		System.out.println("  -r [--retry] Set the number of retries.");
		System.out.println("    If not set, " + ShutdownConstants.getRetry() + " times will be assigned.");
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
		e = args.get("-p", "--port");
		if(Args.isNumeric(e)) {
			port = Integer.parseInt(e);
		}
		e = args.get("-o", "--timeout");
		if(Args.isNumeric(e)) {
			timeout = Integer.parseInt(e);
		}
		e = args.get("-r", "--retry");
		if(Args.isNumeric(e)) {
			retry = Integer.parseInt(e);
		}
		System.out.println("send quit exit conection.");
		boolean res = SendShutdown.send(token, port, timeout, retry);
		if(res) {
			System.out.println("You have successfully shut down quina.");
			System.exit(0);
		} else {
			System.err.println("Failed to shut down quina.");
			System.exit(1);
		}
	}
}
