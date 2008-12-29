// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.model;



/**
 * A data model for the renderer that contains tracks for various boats and
 * information on boats' speeds for encoding tracks as a colored line.
 *
 * @author Martin Weber
 */
public class EncodedSpeedRaceModel extends BasicRaceModel {

  private ISpeedEncoding speedEncoding;

  public final ISpeedEncoding getSpeedEncoding() {
    return speedEncoding;
  }

  /**
   * Sets the speedEncoding property.
   */
  public final void setSpeedEncoding( ISpeedEncoding speedEncoding) {
    this.speedEncoding= speedEncoding;
  }
}
