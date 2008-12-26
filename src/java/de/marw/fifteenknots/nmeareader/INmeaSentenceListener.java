// $Header$
/*
 * Copyright 2008 by Martin Weber
 */

package de.marw.fifteenknots.nmeareader;

import java.util.EventListener;


/**
 * Defines the requirement for an object that gets notified when certain
 * information is detected in a NMEA sentence. In order to keep a timeline of
 * detected information, the methods that receive timing information will be
 * invoked prior to any other methods. If more than one piece of information is
 * detected in a NMEA sentence, the appropriate listener methods will be invoked
 * in a well defined order as follows:
 * <ol>
 * <li>{@link #timeChanged(Object, long)}</li>
 * <li>{@link #dateChanged(Object, long)}</li>
 * <li>{@link #fixChanged(Object, Boolean)}</li>
 * <li>{@link #positionChanged(Object, Position2D)},
 * {@link #speedChanged(Object, float)}, {@link #bearingChanged(Object, float)}
 * in no specific order</li>
 * </ol>
 * 
 * @author Martin Weber
 */
public interface INmeaSentenceListener extends EventListener
{

  /**
   * Invoked, when the GPS derived date changed.
   * 
   * @param source
   *        the source of the event.
   * @param date
   *        number of milliseconds since the standard base time known as
   *        "the epoch", namely January 1, 1970, 00:00:00 GMT. The value will
   *        always represent the first millisecond of the day, that is 00:00.0
   *        GMT.
   */
  void dateChanged( Object source, long date);

  /**
   * Invoked when the GPS derived time of the day changed.
   * 
   * @param source
   *        the source of the event.
   * @param timeOfDay
   *        number of milliseconds since the first millisecond of the day, that
   *        is 00:00.0 GMT.
   */
  void timeChanged( Object source, long timeOfDay);

  /**
   * Invoked when the GPS satellite fix changed.
   * 
   * @param source
   *        the source of the event.
   * @param fix
   *        {@code Boolean.TRUE} if a satellite fix was detected, {@code
   *        Boolean.FALSE} if the satellite fix was lost or <code>null</code> if
   *        the sentence contained invalid data for the fix state.
   */
  void fixChanged( Object source, Boolean fix);

  /**
   * Invoked when the position changed.
   * 
   * @param source
   *        the source of the event.
   * @param pos
   *        the new position.
   */
  void positionChanged( Object source, Position2D pos);

  /**
   * Invoked when the speed changed.
   * 
   * @param source
   *        the source of the event.
   * @param speed
   *        the new speed.
   */
  void speedChanged( Object source, float speed);

  /**
   * Invoked when the bearing changed.
   * 
   * @param source
   *        the source of the event.
   * @param bearing
   *        the new bearing
   */
  void bearingChanged( Object source, float bearing);

}
