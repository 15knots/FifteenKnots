// $Id$
/*
 * Copyright 2008 by Martin Weber
 */

package de.marw.fifteenknots.model;

import java.util.List;

import de.marw.fifteenknots.nmeareader.TrackEvent;


/**
 * Represents the cruise of a boat.
 *
 * @author Martin Weber
 */
public interface Cruise {

  /**
   * Gets metadata about the boat.
   */
  public Boat getBoat();

  /**
   * Gets all track points recorded for this cruise.
   *
   * @return all track points ordered ascending by time.
   */
  public List<TrackEvent> getTrackpoints();
}
