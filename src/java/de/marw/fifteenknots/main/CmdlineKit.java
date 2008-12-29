// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.main;

import de.marw.fifteenknots.engine.IProcessor;


/**
 * Parses the commandline arguments for a specific type and provides the data
 * processor.
 *
 * @author Martin Weber
 */
interface CmdlineKit {

  /**
   * Parses the commandline arguments.
   *
   * @param args
   *        command line arguments from main().
   * @param firstArgIdx
   *        index of the first argument to parse.
   * @return the number of elements in args that were consumed.
   * @throws OptionValidationException
   *         if an invalid command line option was detected.
   */
  int parseOptions( String[] args, int firstArgIdx)
    throws OptionValidationException;

  /**
   * Creates the data processor.
   *
   * @param globalOptions
   *        parsed global commandline options
   */
  IProcessor createProcessor( Options globalOptions);

}
