// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.model;



/**
 * A data model for the renderer that contains tracks for various vehicles and
 * information on vehicles' speeds for encoding tracks as a colored line.
 *
 * @author Martin Weber
 */
public class EncodedSpeedRaceModel extends BasicRaceModel {

  private SpeedEncoding speedEncoding;

  public final SpeedEncoding getSpeedEncoding() {
    return speedEncoding;
  }

  /**
   * Sets the speedEncoding property.
   */
  public final void setSpeedEncoding( SpeedEncoding speedEncoding) {
    this.speedEncoding= speedEncoding;
  }
}
