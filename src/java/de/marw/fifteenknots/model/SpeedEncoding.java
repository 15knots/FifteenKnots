// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.model;

import java.util.List;


/**
 * Holds all speed ranges that are encoded as a color.
 *
 * @author Martin Weber
 */
public interface SpeedEncoding {

  /**
   * Gets the speed ranges.
   */
  public List<SpeedRange> getRanges();
}
