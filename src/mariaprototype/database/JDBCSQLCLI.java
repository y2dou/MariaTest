package mariaprototype.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

/**
 * <p>Stand-alone process allowing a single SQL statement to be sent by command line to a JDBC database.</p>
 * 
 * <p>Help for this process can be printed by running the program with no arguments.</p>
 * 
 * @author Raymond Cabrera
 *
 */
public class JDBCSQLCLI {
	private Options options;
	
	@SuppressWarnings("static-access")
	public JDBCSQLCLI() {
		options = new Options();
		
		Option help = new Option("help", "print this message");
		
		Option url = OptionBuilder.withArgName("JDBC url")
		.hasArg()
		.withDescription("use the database specified by this JDBC url, such as jdbc:hsqldb:hsql://localhost/xdb")
		.create("url");

		Option driver = OptionBuilder.withArgName("driver")
		.hasArg()
		.withDescription("use this database driver, such as org.hsqldb.jdbcDriver")
		.create("driver");
		
		Option username = OptionBuilder.withArgName("username")
		.hasArg()
		.withDescription("use this user name to log into the database")
		.create("user");
		
		Option password = OptionBuilder.withArgName("password")
		.hasArg()
		.withDescription("use this password to log into the database")
		.create("pass");
		
		Option query = OptionBuilder
		.withDescription("specifies if the SQL statement should print a result set")
		.create("query");
		
		options.addOption(help);
		options.addOption(url);
		options.addOption(driver);
		options.addOption(query);
		options.addOption(username);
		options.addOption(password);
	}
	
	public void run(String[] args) throws Exception {
		String user = "sa";
		String pass = "";
		
		String url;
		String driver;
		
		CommandLineParser parser = new GnuParser();
		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("help") || args.length < 1) {
				showHelp();
				System.exit(0);
			}
			
			if (!line.hasOption("url")) {
				System.err.println("-url parameter missing.\n");
				showHelp();
				System.exit(1);
			}
			
			if (line.hasOption("driver")) {
				driver = line.getOptionValue("driver");
				Class.forName(driver);
			}
			
			if (line.hasOption("user")) {
				user = line.getOptionValue("user");
			}
			
			if (line.hasOption("pass")) {
				pass = line.getOptionValue("pass");
			}
			
			url = line.getOptionValue("url");
			driver = line.getOptionValue("driver");
			
			Connection conn = DriverManager.getConnection(url, user, pass);
			
			String statement = args[args.length - 1];
			if (line.hasOption("query")) {
				Statement s = conn.createStatement();
				ResultSet rs = s.executeQuery(statement);
				rs.toString();
			} else {
				Statement s = conn.createStatement();
				s.executeUpdate(statement);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			showHelp();
			throw ex;
		}
	}
	
	private void showHelp() {
		HelpFormatter formatter = new HelpFormatter();
		String header = "Send a single SQL statement to a JDBC database and optionally print a ResultSet.\n";
		formatter.printHelp(JDBCSQLCLI.class.getName() + " [options] SQL-statement", header, options, "");
	}
	
	public static void main(String[] args) throws Exception {
		new JDBCSQLCLI().run(args);
	}
}
