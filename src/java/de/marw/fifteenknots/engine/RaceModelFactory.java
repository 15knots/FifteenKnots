// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.engine;

import java.util.List;

import de.marw.fifteenknots.model.Vehicle;
import de.marw.fifteenknots.model.Cruise;
import de.marw.fifteenknots.model.RaceModel;
import de.marw.fifteenknots.nmeareader.TrackEvent;


/**
 * A factory for the objects that make up a race model.
 *
 * @author Martin Weber
 */
public interface RaceModelFactory {

  /**
   * Creates a new, empty {@link RaceModel} object.
   */
  public RaceModel createRaceModel();

  /**
   * Creates a new {@link Cruise} object for the specified vehicle containing the specified
   * track points.
   *
   * @param vehicle
   *        the vehicle in the cruise.
   * @param track
   *        the track point of the vehicle.
   */
  public Cruise createCruise( Vehicle vehicle, List<TrackEvent> track);

}
