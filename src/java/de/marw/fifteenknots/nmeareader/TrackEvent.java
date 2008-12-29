// $Id$
/*
 * Copyright 2008 by Martin Weber
 */

package de.marw.fifteenknots.nmeareader;

import java.util.EventObject;
import java.util.Formatter;


/**
 * <code>TrackEvent</code> is used to notify interested parties that the
 * position has changed in the event source. At the minimum, instances will
 * contain the position on earth and the time when that position was reached.
 *
 * @author Martin Weber
 */
public class TrackEvent extends EventObject
{
  private static final long serialVersionUID= -395784838463065170L;

  private long date;

  private Position2D position;

  private Float speed;

  private Float bearing;

  /**
   * @param source
   *        The object on which the Event initially occurred.
   * @param date
   *        number of milliseconds since the standard base time known as
   *        "the epoch", namely January 1, 1970, 00:00:00 GMT.
   * @param position
   * @param speed
   * @param bearing
   * @throws IllegalArgumentException
   *         if source or position is null.
   */
  public TrackEvent( Object source, long date, Position2D position,
    Float speed, Float bearing)
  {
    super( source);
    if (position == null)
      throw new IllegalArgumentException( "position");
    setDate( date);
    this.position= position;
    this.speed= speed;
    this.bearing= bearing;
  }

  /**
   * @return The date.
   */
  public synchronized long getDate()
  {
    return this.date;
  }

  /**
   * @param date
   *        the date to set
   */
  public synchronized void setDate( long date)
  {
    if (date < 0) {
      throw new IllegalArgumentException( "date less than zero");
    }
    this.date= date;
  }

  /**
   * @return The position.
   */
  public synchronized Position2D getPosition()
  {
    return this.position;
  }

  /**
   * @return The speed or <code>null</code>.
   */
  public synchronized Float getSpeed()
  {
    return this.speed;
  }

  /**
   * @param speed
   *        the speed to set
   */
  public synchronized void setSpeed( Float speed)
  {
    this.speed= speed;
  }

  /**
   * @return The bearing or <code>null</code>.
   */
  public synchronized Float getBearing()
  {
    return this.bearing;
  }

  /**
   * @param bearing
   *        the bearing to set
   */
  public synchronized void setBearing( Float bearing)
  {
    this.bearing= bearing;
  }

  /**
   * @see java.util.EventObject#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder sb= new StringBuilder();
    // Send all output to the Appendable object sb
    Formatter formatter= new Formatter( sb);
    formatter.format( "[%tF %<tT.%<tL, %s", getDate(), getPosition());
    if (getSpeed() != null)
      formatter.format( ", %fkts", getSpeed());
    if (getBearing() != null)
      formatter.format( ", %f°", getBearing());
    sb.append( ']');
    return sb.toString();
  }

}