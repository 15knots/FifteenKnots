// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.model;

import java.util.List;
import java.util.Objects;


/**
 * A data model for the renderer that contains just {@link BasicCruise}s.
 *
 * @author Martin Weber
 */
public class BasicRaceModel implements RaceModel {

  private List< ? extends Cruise> cruises;
  private SpeedUnitConverter speedUnitConverter;

  public BasicRaceModel() {}

  @Override
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

  public void setSpeedConverter(SpeedUnitConverter speedUnitConverter) {
    this.speedUnitConverter = Objects.requireNonNull( speedUnitConverter);
  }

  @Override
  public SpeedUnitConverter getSpeedConverter() {
    return speedUnitConverter;
  }
}
