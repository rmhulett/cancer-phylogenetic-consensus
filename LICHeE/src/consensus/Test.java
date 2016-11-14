/*
 * TODO(Reyna) modeled on code by viq
*/


package consensus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import consensus.PHYTree;

public class Test {
	protected static final Logger logger = Logger.getLogger("test");

	public static void testBuild(Args args) {
		PHYTree tree = new PHYTree();
		PHYNode root = new PHYNode(0);
		PHYNode leaf = new PHYNode(0,1);

		SampleProfile profile = new SampleProfile("1");
		SNVEntry entry = new SNVEntry("test snv entry", 0);
		ArrayList<SNVEntry> entries = new ArrayList<SNVEntry>();
		entries.add(entry);
		PHYNode internal = new PHYNode(profile, entries, 2);

		tree.addNode(root);
		tree.addNode(internal);
		tree.addNode(leaf);
		tree.addEdge(root, internal);
		tree.addEdge(internal, leaf);

		System.out.println(tree);
	}

	private static final String TREES_TXT_FILE_EXTENSION = ".trees.txt";
	public static void main(String[] args) {
		Options options = new Options(); 
		// Commands
		options.addOption("build", false, "Construct the sample lineage trees");
		
		// Input/Output/Display
		options.addOption("i", true, "Input file path [required]");
		options.addOption("o", true, "Output file path (default: input file with suffix .trees.txt)");
	
		options.addOption("v", "verbose", false, "Verbose mode");
		options.addOption("h", "help", false, "Print usage");
		
		// display order
		ArrayList<Option> optionsList = new ArrayList<Option>();
		optionsList.add(options.getOption("build"));

		optionsList.add(options.getOption("i"));
		optionsList.add(options.getOption("o"));
		optionsList.add(options.getOption("v"));
		optionsList.add(options.getOption("h"));
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmdLine = null;
		HelpFormatter hf = new HelpFormatter();
		hf.setOptionComparator(new OptionComarator<Option>(optionsList));
		try {
			cmdLine = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			hf.printHelp("lichee", options);
			System.exit(-1);
		}
		
		// Set-up input args
		Args params = new Args();	
		if(cmdLine.hasOption("i")) {
			params.inputFileName = cmdLine.getOptionValue("i");
		} else {
			System.out.println("Required parameter: input file path [-i]");
			hf.printHelp("lichee", options);
			System.exit(-1);
		}
		if(cmdLine.hasOption("o")) {
			params.outputFileName = cmdLine.getOptionValue("o");	
		} else {
			params.outputFileName = params.inputFileName + TREES_TXT_FILE_EXTENSION;
		}
		if(cmdLine.hasOption("h")) {
			new HelpFormatter().printHelp(" ", options);
		}
		// logger
		ConsoleHandler h = new ConsoleHandler();
		h.setFormatter(new LogFormatter());
		h.setLevel(Level.INFO);
		logger.setLevel(Level.INFO);
		if(cmdLine.hasOption("v")) {
			h.setLevel(Level.FINEST);
			logger.setLevel(Level.FINEST);
		}
		logger.addHandler(h);
		logger.setUseParentHandlers(false);
		
		if(cmdLine.hasOption("build")) {
			testBuild(params);
			
		} else {
			new HelpFormatter().printHelp("lichee", options);
			System.exit(-1);
		}
	}
	
	protected static class Args {
		// --- 'build' command ---
		String inputFileName;
		String outputFileName;
	}

	protected static class LogFormatter extends Formatter {
		public String format(LogRecord rec) {
			return rec.getMessage() + "\r\n";
		}
	}
	
	protected static class OptionComarator<T extends Option> implements Comparator<T> {
	    protected ArrayList<Option> orderedOptions;
	    public OptionComarator(ArrayList<Option> options) {
	    	orderedOptions = options;
	    }
	    public int compare(T o1, T o2) {
	        return orderedOptions.indexOf(o1) - orderedOptions.indexOf(o2);
	    }
	}
}