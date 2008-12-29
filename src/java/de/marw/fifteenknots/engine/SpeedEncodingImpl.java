// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.engine;

import java.util.List;

import de.marw.fifteenknots.model.SpeedRange;
import de.marw.fifteenknots.model.ISpeedEncoding;


/**
 * @author Martin Weber
 */
public class SpeedEncodingImpl implements ISpeedEncoding
{

  private List<SpeedRange> ranges;

  /**
   * @param ranges
   */
  public SpeedEncodingImpl( List<SpeedRange> ranges)
  {
    this.ranges= ranges;
  }

  /*-
   * @see de.marw.fifteenknots.model.ISpeedEncoding#getRanges()
   */
  public List<SpeedRange> getRanges()
  {
    return ranges;
  }

}
