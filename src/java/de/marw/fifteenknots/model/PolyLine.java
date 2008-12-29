// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.model;

import java.util.List;

import de.marw.fifteenknots.nmeareader.TrackEvent;


/**
 * A continuous line composed of one or more line segments.
 *
 * @author Martin Weber
 */
public interface PolyLine
{
  /**
   * Gets the color index for display of this polyline.
   */
  public int getColorIndex();

  /**
   * Gets the end points of each segment.
   */
  public List<TrackEvent> getSegments();
}
