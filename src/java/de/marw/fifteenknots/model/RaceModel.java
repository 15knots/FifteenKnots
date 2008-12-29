// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.model;

import java.util.List;


/**
 * A data model for the renderer that contains tracks for various boats.<br>
 * All implemtations of zhis interface must provide a public no-argument
 * constructor.
 *
 * @author Martin Weber
 */
public interface RaceModel {

  /**
   * Gets the cruises of all boats in the race.
   */
  public List< ? extends Cruise> getCruises();

}
