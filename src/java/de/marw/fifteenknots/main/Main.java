// $Header$
/*
 * Copyright 2008 by Martin Weber
 */

package de.marw.fifteenknots.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;

import de.marw.fifteenknots.engine.IProcessor;


/**
 * @author Martin Weber
 */
public class Main {

  /**  */
  private static final String PROGNAME= "15kts";

  private CmdlineKit kit;

  private Options options= new Options();

  /**
   * Command line evaluation modes:
   * <ul>
   * <li>all file name arguments get associated to a single anonymous boat</li>
   * <li>option <code>-boat</code> starts association of options and file name
   * aguments to a boat.</li>
   * <li>(optional) option <code>-XXX</code> associates each file to an
   * anonymous boat</li>
   * </ul>
   *
   * @param args
   *        command line arguments from main().
   * @throws OptionValidationException
   *         if an invalid command line option was detected.
   */
  public Main( String[] args) throws OptionValidationException {

    int i;
    for (i= 0; i < args.length;) {
      String arg= args[i];
      if (arg.startsWith( "-")) {
	i+= parseGlobalOption( args, i, options);
      }
      else {
	break; // done with option args
      }
    }

    if (i < args.length) {
      // remaining args are file names, associate these with an anonymous boat
      BoatOptions boatOption= new BoatOptions();
      i+= parseBoatOptions( args, i, options, boatOption);
      options.addBoat( boatOption);
    }
    // validate options...
    if (kit == null) {
      throw new OptionValidationException( "no output type specified");
    }
    options.validate();
  }

  public static void main( String[] args) {
    if (args.length == 0) {
      usage();
      System.exit( 1);
    }

    try {
      Main prog= new Main( args);
      prog.run();
    }
    catch (OptionValidationException ex) {
      String format= "{0}: {1}";
      System.err.println( MessageFormat.format( format, PROGNAME, ex
	.getLocalizedMessage()));
      usage();
      System.exit( 1);
    }
    catch (IOException ex) {
      // TODO Auto-generated catch block
      ex.printStackTrace();
      System.exit( 1);
    }
    System.exit( 0);
  }

  /**
   * @throws FileNotFoundException
   * @throws IOException
   */
  private void run() throws FileNotFoundException, IOException {
    // create the processing chain...
    IProcessor processor= kit.createProcessor( options);
    processor.process();
  }

  /**
   * Parses one of the global commandline arguments.
   *
   * @param args
   *        command line arguments from main().
   * @param firstArgIdx
   *        index of the first argument to parse.
   * @param options
   *        the parsing results.
   * @return the number of elements in args that were consumed.
   * @throws OptionValidationException
   *         if an invalid command line option was detected.
   */
  private int parseGlobalOption( String[] args, final int firstArgIdx,
    Options options) throws OptionValidationException {
    final String arg= args[firstArgIdx];
    int consumed= 0;
    if (arg.equals( "-h") || arg.equals( "-help")) {
      usage();
      System.exit( 0);
    }
    else {
      if (arg.equals( "-t") || arg.equals( "-type")) {
	final String typeArg= getRequiredArg( args, firstArgIdx);
	if (kit != null) {
	  String format= "option specified twice -- {0} ";
	  throw new OptionValidationException( MessageFormat.format( format,
	    arg.substring( 1)));
	}
	for (OutputType type : OutputType.values()) {
	  if (type.matchesOption( typeArg)) {
	    kit= type.createCmdlineKit();
	    consumed+= 2;
	    // parse
	    consumed+= kit.parseOptions( args, firstArgIdx + consumed);
	    break;
	  }
	}
      }
      else if (arg.equals( "-boat")) {
	BoatOptions boatOption= new BoatOptions();
	consumed=
	  1 + parseBoatOptions( args, firstArgIdx + 1, options, boatOption);
	options.addBoat( boatOption);
      }
      else {
	String format= "unknown option -- {0}";
	throw new OptionValidationException( MessageFormat.format( format, arg
	  .substring( 1)));
      }

    }
    return consumed;
  }

  /**
   * Parses the commandline arguments for a boat.
   *
   * @param args
   *        command line arguments from main().
   * @param firstArgIdx
   *        index of the first argument to parse.
   * @param options
   *        commandline options already parsed.
   * @param boatOption
   *        the parsing results.
   * @return the number of elements in args that were consumed.
   * @throws OptionValidationException
   *         if an invalid command line option was detected.
   */
  private static int parseBoatOptions( String[] args, int firstArgIdx,
    Options options, BoatOptions boatOption) throws OptionValidationException {

    int i;
    for (i= firstArgIdx; i < args.length; i++) {
      final String arg= args[i];
      if (arg.equals( "-name")) {
	boatOption.setName( getRequiredArg( args, firstArgIdx));
	i++;
      }
      else if (arg.startsWith( "-")) {
	// unknown option
	break;
      }
      else {
	// arg is an input file
	boatOption.getFileNames().add( arg);
      }
    }
    return i - firstArgIdx;
  }

  /**
   * Prints usage synopsis.
   */
  private static void usage() {
    String format=
      "Usage:"
	+ "\n  {0} [-h] -t <type> [<type options>] <file> [<file>...]"
	+ "\n  {0} [-h] -t <type> [<type options>] -boat <boat options> <file> [<file>...] [-boat ...]"
	+ "\nOptions:" + "\n  -h -help:        print help and exit"
	+ "\n  -t -type <type>: set output file type"
	+ "\n  -boat:           all following arguments refer to a new boat"
	+ "\nBoat options: [-name <text>]"
	+ "\n  -name <text>:    sets boat name";
    StringBuilder typeUsages= new StringBuilder();
    typeUsages.append( "\nOutput types:");
    for (OutputType type : OutputType.values()) {
      typeUsages.append( "\n ").append( type.getDescription());
      final String usage= type.getUsage();
      if (usage != null)
	typeUsages.append( "\n  Usage:\n   ").append( usage);
    }
    System.out.println( MessageFormat.format( format, PROGNAME));
    System.out.println( typeUsages);
  }

  /**
   * @param args
   *        command line arguments from main().
   * @param firstArgIdx
   *        index of the first argument to parse.
   * @return the required argument
   * @throws OptionValidationException
   *         if an invalid command line option was detected.
   */
  public static String getRequiredArg( String[] args, final int firstArgIdx)
    throws OptionValidationException {
    if (firstArgIdx + 1 >= args.length
      || args[firstArgIdx + 1].startsWith( "-")) {
      String format= "option {0} requires an argument";
      throw new OptionValidationException( MessageFormat.format( format,
	args[firstArgIdx]));
    }
    final String arg= args[firstArgIdx + 1];
    return arg;
  }
}
