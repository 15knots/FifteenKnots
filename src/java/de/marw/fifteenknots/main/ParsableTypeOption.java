// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.main;

/**
 * Defines the requirements of an object that is capable of parsing command line
 * output type options.
 * 
 * @author Martin Weber
 */
interface ParsableTypeOption
{
  /**
   * Gets a brief help text suitable to be displayed with the 'help' commandline
   * option.
   */
  public String getDescription();

  /**
   * Gets a human readable help text suitable to be displayed with the 'help'
   * commandline option.
   * 
   * @return the text or {@code null}, if nothing should be printed.
   */
  public String getUsage();

  /**
   * Gets whether this object matches the specified command line argument. In
   * other words: Returns whether this object is responsible for parsing the
   * specified argument, including an arbitrary number of subsequent arguments.
   * 
   * @param typeArg
   *        the command line argument from main() that follows the type option.
   * @return {@code true} if this object an parse the argument.
   */
  public boolean matchesOption( String typeArg);

  /**
   * @return
   */
  public CmdlineKit createCmdlineKit();
}
