// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.model;

import java.util.List;


/**
 * A data model for the renderer that contains tracks for various boats and
 * information on boat's speeds for encoding spped as a colered line.
 * 
 * @author Martin Weber
 */
public class EncodedSpeedRaceModel
{

  private List<? extends ICruise> cruises;

  private ISpeedEncoding speedEncoding;

  /**
   * @param cruises
   * @param speedEncoding
   */
  public EncodedSpeedRaceModel( List<? extends ICruise> cruises,
    ISpeedEncoding speedEncoding)
  {
    this.cruises= cruises;
    this.speedEncoding= speedEncoding;
  }

  public List<? extends ICruise> getCruises()
  {
    return cruises;
  }

  public ISpeedEncoding getSpeedEncoding()
  {
    return speedEncoding;
  }
}
