package de.marw.fifteenknots.engine;

import java.util.ArrayList;
import java.util.List;

import de.marw.fifteenknots.model.IPolyLine;
import de.marw.fifteenknots.nmeareader.TrackEvent;

/**
 * @author Martin Weber
 */
final class PolylineImpl implements IPolyLine {

  private final List<TrackEvent> segments= new ArrayList<TrackEvent>();

  private final int color;

  /**
   * @param colorIdx
   *        the color index for display of this polyline.
   */
  public PolylineImpl( int colorIdx) {
    this.color= colorIdx;
  }

  public int getColorIndex() {
    return color;
  }

  public List<TrackEvent> getSegments() {
    return segments;
  }
}