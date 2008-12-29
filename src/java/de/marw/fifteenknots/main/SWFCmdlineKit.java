// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.main;

import de.marw.fifteenknots.engine.IProcessor;


/**
 * A commandline kit implementation for the SWF output format (Adobe shockwave).
 *
 * @author Martin Weber
 */
class SWFCmdlineKit implements CmdlineKit {

  private String outputFileName;

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

  /*-
   * @see de.marw.fifteenknots.main.CmdlineKit#createProcessor(de.marw.fifteenknots.main.Options)
   */
  public IProcessor createProcessor( Options globalOptions) {
    return new SWFProcessor( globalOptions, outputFileName);
  }

}
