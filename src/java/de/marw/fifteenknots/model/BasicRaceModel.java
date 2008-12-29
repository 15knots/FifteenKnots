// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.model;

import java.util.List;


/**
 * A data model for the renderer that contains just {@link BasicCruise}s.
 *
 * @author Martin Weber
 */
public class BasicRaceModel implements RaceModel {

  private List< ? extends Cruise> cruises;

  public BasicRaceModel() {}

  public List< ? extends Cruise> getCruises() {
    return cruises;
  }

  /**
   * Sets the cruises.
   */
  public void setCruises( List< ? extends Cruise> cruises) {
    if (cruises == null) {
      throw new NullPointerException( "cruises");
    }
    this.cruises= cruises;
  }

}
