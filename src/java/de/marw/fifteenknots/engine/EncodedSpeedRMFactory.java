// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.engine;

import java.util.List;

import de.marw.fifteenknots.model.Boat;
import de.marw.fifteenknots.model.Cruise;
import de.marw.fifteenknots.model.EncodedSpeedRaceModel;
import de.marw.fifteenknots.model.RaceModel;
import de.marw.fifteenknots.model.SpeedCruise;
import de.marw.fifteenknots.nmeareader.TrackEvent;


/**
 * Factory implementation for objects of a race model with speed encodings.
 *
 * @see EncodedSpeedRaceModel
 * @author Martin Weber
 */
public class EncodedSpeedRMFactory implements RaceModelFactory {

  /**
   * {@inheritDoc}
   *
   * @return a {@link EncodedSpeedRaceModel} object.
   */
  public RaceModel createRaceModel() {
    return new EncodedSpeedRaceModel();
  }

  /**
   * {@inheritDoc}
   *
   * @return a {@link SpeedCruise} object.
   */
  public Cruise createCruise( Boat boat, List<TrackEvent> track) {
    return new SpeedCruise( boat, track);
  }

}
