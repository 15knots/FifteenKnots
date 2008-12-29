// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.main;

import java.text.MessageFormat;

import de.marw.fifteenknots.engine.IProcessor;


/**
 * A commandline kit implementation for the KML output format (Google Earth).
 *
 * @author Martin Weber
 */
class KMLCmdlineKit implements CmdlineKit {

  private String outputFileName;

  private int colorCount= 120;

  /**
   *
   */
  public KMLCmdlineKit() {}

  /*-
   * @see de.marw.fifteenknots.main.CmdlineKit#parseOptions(java.lang.String[], int)
   */
  public int parseOptions( String[] args, int firstArgIdx)
    throws OptionValidationException {
    int consumed= 0;
    for (int i= firstArgIdx; i < args.length; i+= consumed) {
      final String arg= args[i];
      if (arg.equals( "-o") || arg.equals( "-output")) {
	setOutputFileName( Main.getRequiredArg( args, i));
	consumed+= 2;
      }
      else if (arg.equals( "-c") || arg.equals( "-colors")) {
	final String colors_s= Main.getRequiredArg( args, i);
	try {
	  // convert to positive int
	  Integer colors= Integer.decode( colors_s);
	  int colorCnt= colors.intValue();
	  if (colorCnt < 0) {
	    String format= "Negative number -- {0} ";
	    throw new OptionValidationException( MessageFormat.format( format,
	      colors));
	  }
	  setColorCount( colorCnt);
	  consumed+= 2;
	}
	catch (NumberFormatException ex) {
	  String format= "Invalid number -- {0} ";
	  throw new OptionValidationException( MessageFormat.format( format,
	    colors_s));
	}
      }

      else {
	// unknown option
	break;
      }
    }
    return consumed;
  }

  /**
   * Gets the outputFileName property.
   *
   * @return the current outputFileName property.
   */
  public String getOutputFileName() {
    return this.outputFileName;
  }

  /**
   * Sets the name of the output file property.
   */
  private void setOutputFileName( String outputFile) {
    this.outputFileName= outputFile;
  }

  /**
   * Gets the number of colors to use for visual boat speed coding.
   *
   * @return a value grater than zero.
   */
  public int getColorCount() {
    return colorCount;
  }

  /**
   * Sets the colorCount property.
   *
   * @see KMLCmdlineKit#colorCount
   */
  private void setColorCount( int colorCount) {
    this.colorCount= colorCount;
  }

  /*-
   * @see de.marw.fifteenknots.main.CmdlineKit#createProcessor(de.marw.fifteenknots.main.Options)
   */
  public IProcessor createProcessor( Options globalOptions) {
    return new KMLProcessor( globalOptions, outputFileName, colorCount);
  }

}
