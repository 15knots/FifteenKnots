// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.main;

/**
 * @author Martin Weber
 */
class OptionValidationException extends Exception
{

  /**  */
  private static final long serialVersionUID = -5375451039185285921L;

  /**
   * @param message
   * @param cause
   */
  public OptionValidationException( String message, Throwable cause)
  {
    super( message, cause);
  }

  /**
   * @param message
   */
  public OptionValidationException( String message)
  {
    super( message);
  }

}
