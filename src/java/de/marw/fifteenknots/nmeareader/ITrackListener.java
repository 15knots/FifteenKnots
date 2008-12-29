// $Id$
/*
 * Copyright 2008 by Martin Weber
 */

package de.marw.fifteenknots.nmeareader;

import java.util.EventListener;

/**
 * Defines the requirements of an object that is interested in listening to
 * track events.
 * 
 * @author Martin Weber
 * @see TrackEvent
 */
public interface ITrackListener extends EventListener
{

  /**
   * Notified when an new track event ocuured.
   * 
   * @param evt
   *        the track event that occurred.
   */
  void trackPoint( TrackEvent evt);

}
