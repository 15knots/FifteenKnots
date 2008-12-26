// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.model;

import java.awt.Color;


/**
 * A speed range encoded as a color.
 * 
 * @author Martin Weber
 */
public class SpeedRange
{

  private float lowerLimit;
  private float upperLimit;
  private Color color;

  /**
   * @param lowerLimit
   * @param upperLimit
   * @param color
   */
  public SpeedRange( float lowerLimit, float upperLimit, Color color)
  {
    this.lowerLimit= lowerLimit;
    this.upperLimit= upperLimit;
    this.color= color;
  }

  /**
   * Gets the lowest speed included in this speed range. For all speed values
   * <tt>x</tt> in this range:<br>
   * <tt>{@linkplain #getLowerLimit()} &le; x &lt; {@linkplain #getUpperLimit()}</tt>
   */
  public float getLowerLimit()
  {
    return lowerLimit;
  }

  /**
   * Gets the highest speed <em>not included</em> in this speed range. For all
   * speed values <tt>x</tt> in this range:<br>
   * <tt>{@linkplain #getLowerLimit()} &le; x &lt; {@linkplain #getUpperLimit()}</tt>
   */
  public float getUpperLimit()
  {
    return upperLimit;
  }

  /**
   * Get the color used to diplay any speed value in this range.
   */
  public Color getColor()
  {
    return color;
  }
}
