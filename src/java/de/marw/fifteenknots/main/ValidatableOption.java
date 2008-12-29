// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.main;

/**
 * @author Martin Weber
 */
public interface ValidatableOption
{

  /**
   * 
   */
  void validate() throws OptionValidationException;

}