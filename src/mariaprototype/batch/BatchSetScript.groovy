/**
 * 
 */
package mariaprototype.batch

import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.OptionBuilder
import org.apache.commons.cli.Options

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.GnuParser

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService

/**
 * Run a set of batch parameter files each as a separate process.
 * 
 * @author arcabrer
 *
 */
public class BatchSetScript{
	private Options options
	
	private int processes = 1
	
	// private String batchMainClass = "repast.simphony.batch.BatchMain";
	private String batchMainClass = "mariaprototype.batch.TerminatingBatchMain";
	
	private String javapath = "java"
	private String classpath = "bin:bin-groovy:lib/*:lib/generic/*:../repastS/bin:../repastS/lib/*"
	private String winclasspath = "bin;bin-groovy;lib/*;lib/windows/*;../repastS/bin;../repastS/lib/*"
	private String vmargs = "-Xss10M -Xms1100M -Xmx1100M"
	private String repastargs = ""
	private String resourcedir
	
	public BatchSetScript() {
		options = new Options()
		
		Option help = OptionBuilder.withArgName("help")
		.hasArg()
		.withDescription("show this help message")
		.withLongOpt("help")
		.create("h");
		
		Option processes = OptionBuilder.withArgName("number")
		.hasArg()
		.withDescription("use this number of simulataneous processes, defaults to " + processes.toString())
		.create("processes");
		
		Option javapath = OptionBuilder.withArgName("path-to-java")
		.hasArg()
		.withDescription("location of java binary, if not on path")
		.create("javapath");
		
		Option classpath = OptionBuilder.withArgName("classpath")
		.hasArg()
		.withDescription("classpath to all required binaries and libraries, paths separated by colons")
		.withLongOpt("classpath")
		.create("cp");
		
		Option vmargs = OptionBuilder.withArgName("arguments")
		.hasArg()
		.withDescription("pass these arguments to the VM")
		.create("vmargs");
		
		Option repastargs = OptionBuilder.withArgName("arguments")
		.hasArg()
		.withDescription("pass these arguments to the repast, not including the parameter or resource files")
		.create("repastargs");
		
		Option resourcedir = OptionBuilder.withArgName("resourcedir")
		.hasArg()
		.withDescription("use this resource directory")
		.create("resourcedir");
		
		Option windows = OptionBuilder
		.withDescription("set if on windows platform")
		.create("windows");
		
		Option recursive = OptionBuilder
		.withDescription("set to true if on windows platform")
		.withLongOpt("recursive")
		.create("R");
		
		options.addOption(help)
		options.addOption(javapath)
		options.addOption(classpath)
		options.addOption(vmargs)
		options.addOption(repastargs)
		options.addOption(resourcedir)
		options.addOption(processes)
		options.addOption(windows)
		options.addOption(recursive)
	}
	
	private void showHelp() {
		HelpFormatter formatter = new HelpFormatter();
		String header = "Start a set of parameter files, each as separate processes.\n";
		formatter.printHelp(BatchSetScript.class.getName() + " [options] parameter-files", header, options, "");
	}
	
	private void runBatch(String javaPath, String classPath, String vmArgs, String repastArgs, String paramFile, String resourceFile) {
		// execute process
		String commandLine = javaPath + " -cp " + classPath + " " + vmArgs + " " + batchMainClass + " " + repastArgs + " -params " + paramFile + " " + resourceFile
		
		println "Running: " + commandLine
		
		Process proc = commandLine.execute()
		proc.consumeProcessOutputStream System.out
		proc.consumeProcessErrorStream System.err
		proc.waitFor()
	}
	
	private void run(String[] args) {
		CommandLineParser parser = new GnuParser()
		try {
			CommandLine line = parser.parse(options, args)
			if (line.hasOption("h") || args.length < 1) {
				showHelp();
				System.exit(0);
			}
			
			if (line.hasOption("processes")) {
				int processesSuggestion = line.getOptionValue("processes").toInteger()
				if (processesSuggestion >= 0)
					processes = processesSuggestion
			}
			
			println "Processes: " + processes
			
			if (line.hasOption("windows")) {
				classpath = winclasspath
			}
			
			if (line.hasOption("javapath")) {
				javapath = line.getOptionValue("javapath")
			}
			
			if (line.hasOption("cp")) {
				classpath = line.getOptionValue("cp")
			}
			
			if (line.hasOption("vmargs")) {
				vmargs = line.getOptionValue("vmargs")
			}
			
			if (line.hasOption("repastargs")) {
				repastargs = line.getOptionValue("repastargs")
			}
			
			if (line.hasOption("resourcedir")) {
				resourcedir = line.getOptionValue("resourcedir")
			}
			
			ExecutorService pool = Executors.newFixedThreadPool(processes)
			Closure defer = { c -> pool.submit(c as Callable) }
			
			List paramFiles = line.getArgList()
			for (String paramFilename : paramFiles) {
				// run process launcher in new thread
				//Object lock = locks.take()
				File paramFile = new File(paramFilename)
				
				if (paramFile.isDirectory()) {
					if (line.hasOption("R")) {
						paramFile.eachFileRecurse { File path -> defer { runBatch(javapath, classpath, vmargs, repastargs, path.path, resourcedir) } }
					} else {
						paramFile.eachFile { File path -> defer { runBatch(javapath, classpath, vmargs, repastargs, path.path, resourcedir) } }
					}
				} else if (paramFile.isFile()) {
					def job = defer { runBatch(javapath, classpath, vmargs, repastargs, paramFile.path, resourcedir) }
				}
				
			}
			
			pool.shutdown()
		} catch (Exception ex) {
			ex.printStackTrace();
			showHelp();
			throw ex;
		}
	}
	
	public static void main(String[] args) {
		new BatchSetScript().run(args)
	}
	
	
	
	
}
