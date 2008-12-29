// $Id$
/*
 * Copyright 2008 by Martin Weber
 */

package de.marw.fifteenknots.nmeareader;

/**
 * A two dimensional position on earth, contains no altitude.
 * 
 * @author Martin Weber
 */
public class Position2D
{

  /**
   * latitude in degrees, where positive values denote the northern hemisphere.
   */
  private double latitude;

  /**
   * longitude in degrees, where positive values denote the eastern hemisphere.
   */
  private double longitude;

  /**
   * @param latitude
   *        latitude in degrees, where positive values denote the northern
   *        hemisphere.
   * @param longitude
   *        longitude in degrees, where positive values denote the eastern
   *        hemisphere.
   */
  public Position2D( double latitude, double longitude)
  {
    this.latitude= latitude;
    this.longitude= longitude;
  }

  /**
   * @return The latitude.
   */
  public double getLatitude()
  {
    return this.latitude;
  }

  /**
   * @param latitude
   *        the latitude to set
   */
  public void setLatitude( double latitude)
  {
    this.latitude= latitude;
  }

  /**
   * @return The longitude.
   */
  public double getLongitude()
  {
    return this.longitude;
  }

  /**
   * @param longitude
   *        the longitude to set
   */
  public void setLongitude( double longitude)
  {
    this.longitude= longitude;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final int prime= 31;
    int result= 1;
    long temp;
    temp= Double.doubleToLongBits( this.latitude);
    result= prime * result + (int) (temp ^ (temp >>> 32));
    temp= Double.doubleToLongBits( this.longitude);
    result= prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if ( !(obj instanceof Position2D))
      return false;
    Position2D other= (Position2D) obj;
    if (Double.doubleToLongBits( this.latitude) != Double
      .doubleToLongBits( other.latitude))
      return false;
    if (Double.doubleToLongBits( this.longitude) != Double
      .doubleToLongBits( other.longitude))
      return false;
    return true;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return "" + latitude + "°, " + longitude + "°";
  }

}