// $Id$
/*
 * Copyright 2008 by Martin Weber
 */

package de.marw.fifteenknots.nmeareader;

/**
 * A position on earth with the time when that position was reached.
 * 
 * @author weber
 */
public class TimedPosition extends Position2D
{
  /**
   * @param date
   *        number of milliseconds since the standard base time known as
   *        "the epoch", namely January 1, 1970, 00:00:00 GMT.
   * @param position
   *        the position.
   */
  public TimedPosition( long date, Position2D position)
  {
    this( date, position.getLatitude(), position.getLongitude());
  }

  /**
   * @param date
   *        number of milliseconds since the standard base time known as
   *        "the epoch", namely January 1, 1970, 00:00:00 GMT.
   * @param latitude
   *        latitude in degrees, where positive values denote the northern
   *        hemisphere.
   * @param longitude
   *        longitude in degrees, where positive values denote the eastern
   *        hemisphere.
   */
  public TimedPosition( long date, double latitude, double longitude)
  {
    super( longitude, latitude);
    setDate( date);
  }

  /**
   * number of milliseconds since the standard base time known as "the epoch",
   * namely January 1, 1970, 00:00:00 GMT.
   */
  private long date;

  /**
   * @return The date.
   */
  public long getDate()
  {
    return this.date;
  }

  /**
   * @param date
   *        the date to set
   */
  public void setDate( long date)
  {
    if (date < 0) {
      throw new IllegalArgumentException( "date less than zero");
    }
    this.date= date;
  }

}
