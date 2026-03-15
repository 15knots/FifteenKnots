// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.engine;

import java.util.List;

import de.marw.fifteenknots.model.BasicCruise;
import de.marw.fifteenknots.model.BasicRaceModel;
import de.marw.fifteenknots.model.Vehicle;
import de.marw.fifteenknots.model.Cruise;
import de.marw.fifteenknots.model.RaceModel;
import de.marw.fifteenknots.nmeareader.TrackEvent;


/**
 * Factory implementation for objects of a basic race model.
 *
 * @see BasicRaceModel
 * @author Martin Weber
 */
public class BasicRMFactory implements RaceModelFactory {

  /**
   * {@inheritDoc}
   *
   * @return a {@link BasicRaceModel} object.
   */
  public RaceModel createRaceModel() {
    return new BasicRaceModel();
  }

  /**
   * {@inheritDoc}
   *
   * @return a {@link BasicCruise} object.
   */
  public Cruise createCruise( Vehicle vehicle, List<TrackEvent> track) {
    return new BasicCruise( vehicle, track);
  }

}
