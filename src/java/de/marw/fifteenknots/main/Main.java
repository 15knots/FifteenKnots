// $Header$
/*
 * Copyright 2008 by Martin Weber
 */

package de.marw.fifteenknots.main;

import java.io.IOException;
import java.text.MessageFormat;

import de.marw.fifteenknots.engine.ProcessorBuilder;


/**
 * @author Martin Weber
 */
public class Main
{
  /**  */
  private static final String PROGNAME= "15kts";

  /**
   * Command line evaluation modes:
   * <ul>
   * <li>all file name arguments get associated to a single anonymous boat</li>
   * <li>option <code>-boat</code> starts association of options and file name
   * aguments to a boat.</li>
   * <li>(optional) option <code>-XXX</code> associates each file to an
   * anonymous boat</li>
   * </ul>
   */
  public static void main( String[] args)
  {
    if (args.length == 0) {
      usage_exit();
    }
    Options options= new Options();

    int i;
    for (i= 0; i < args.length;) {
      String arg= args[i];
      if (arg.startsWith( "-")) {
        int consumed= parseGlobalOption( args, i, options);
        if (consumed == 0) {
          String format= "{0}: unknown option -- {1} ";
          System.err.print( MessageFormat.format( format, PROGNAME, arg
            .substring( 1)));
          System.exit( 1);
        }
        i+= consumed;
      }
      else {
        break; // done with option args
      }
    }

    if (i < args.length) {
      // remaining args are file names, associate these with an anonymous boat
      BoatOptions boatOption= new BoatOptions();
      i+= parseBoatArgs( args, i, options, boatOption);
      options.addBoat( boatOption);
    }
    // validate options...
    try {
      options.validate();
    }
    catch (OptionValidationException ex) {
      String format= "{0}: {1} ";
      System.err.print( MessageFormat.format( format, PROGNAME, ex
        .getLocalizedMessage()));
      System.exit( 1);
    }
    
    // create the processing chain...
    ProcessorBuilder builder= new ProcessorBuilder(options);
    try {
      builder.getProcessor().process();
    }
    catch (IOException ex) {
      // TODO Auto-generated catch block
      ex.printStackTrace();
    }
    System.exit( 0);
  }

  /**
   * @param args
   * @param firstArgIdx
   * @param options
   * @return the number of elements in args that were consumed.
   */
  private static int parseGlobalOption( String[] args, final int firstArgIdx,
    Options options)
  {
    final String arg= args[firstArgIdx];
    int consumed= 0;
    if (arg.equals( "-h") || arg.equals( "-help")) {
      // options.help= true;
      // return 1;
      usage();
      System.exit( 0);
    }
    else if (arg.equals( "-o") || arg.equals( "-output")) {
      options.setOutputFileName( getRequiredArgOrDie( args, firstArgIdx));
      consumed= 2;
    }
    else if (arg.equals( "-boat")) {
      BoatOptions boatOption= new BoatOptions();
      consumed= 1 + parseBoatArgs( args, firstArgIdx + 1, options, boatOption);
      options.addBoat( boatOption);
    }
    return consumed;
  }

  /**
   * Parses the commandlin arguments for a boat.
   * 
   * @param args
   * @param firstArgIdx
   * @param options
   * @param boatOption
   * @return the number of elements in args that were consumed.
   */
  private static int parseBoatArgs( String[] args, int firstArgIdx,
    Options options, BoatOptions boatOption)
  {

    int i;
    for (i= firstArgIdx; i < args.length; i++) {
      final String arg= args[i];
      if (arg.equals( "-name")) {
        boatOption.setName( getRequiredArgOrDie( args, firstArgIdx));
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
   * Print usage and exit.
   */
  private static void usage_exit()
  {
    usage();
    System.exit( 1);
  }

  /**
   * Prints usage synopsis.
   */
  private static void usage()
  {
    String format=
      "Usage:\n" + "  {0} <global options> <file> [<file>...]\n"
        + "  {0} <global options> -boat <options> <file> [<file>...]\n"
        + " Global options: [-h] [-o <file>]\n"
        + "  -h -help:          print help and exit\n"
        + "  -o -output <file>: output file name\n"
        + " Options: [-name <text>]\n" + "  -name <text>: sets boat name"
    // + " -color: "
    ;
    System.out.println( MessageFormat.format( format, PROGNAME));
  }

  /**
   * @param args
   * @param firstArgIdx
   * @return the required argument
   */
  private static String getRequiredArgOrDie( String[] args,
    final int firstArgIdx)
  {
    if (firstArgIdx + 1 >= args.length
      || args[firstArgIdx + 1].startsWith( "-")) {
      String format= "{0}: option {1} requires an argument";
      System.err.print( MessageFormat.format( format, PROGNAME,
        args[firstArgIdx]));
      System.exit( 1);
    }
    String arg= args[firstArgIdx + 1];
    return arg;
  }
}
