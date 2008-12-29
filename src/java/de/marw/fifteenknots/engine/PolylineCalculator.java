// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.engine;

import java.util.Iterator;
import java.util.concurrent.Callable;

import de.marw.fifteenknots.model.PolyLine;
import de.marw.fifteenknots.model.SpeedCruise;
import de.marw.fifteenknots.nmeareader.TrackEvent;


/**
 * Enriches a {@code SpeedCruise} object with the calulated {@link PolyLine}s.
 * The calculated lines encode the boat's speed as a color.
 *
 * @author Martin Weber
 */
public class PolylineCalculator implements Callable<Object> {

  private final SpeedCruise cruise;

  private final SpeedColorEncoder colorEncoder;

  /**
   * Constructs a new object with the specified boat and zero files to read in.
   * No model factory will be set.
   *
   * @param cruise
   *        the cruise for which the speeds should be calculated.
   */
  public PolylineCalculator( SpeedCruise cruise, SpeedColorEncoder colorEncoder) {
    if (cruise == null) {
      throw new NullPointerException( "cruise");
    }
    this.cruise= cruise;
    if (colorEncoder == null) {
      throw new NullPointerException( "colorEncoder");
    }
    this.colorEncoder= colorEncoder;
  }

  /**
   * Calulates the {@link PolyLine}s from the cruise's
   * {@linkplain SpeedCruise#getTrackpoints() track points} and stores these
   * values in the cruise object.
   *
   * @return always {@code null}
   */
  public Object call() {
    int lastColorIdx= -1;
    PolylineImpl polyline= null;
    TrackEvent trackPoint= null;
    for (Iterator<TrackEvent> iterator= cruise.getTrackpoints().iterator(); iterator
      .hasNext();) {
      trackPoint= iterator.next();
      if (trackPoint.getSpeed() == null)
	continue; // TODO berechnen!
      int colorIdx= colorEncoder.getEncodedColorIndex( trackPoint.getSpeed());
      if (colorIdx != lastColorIdx) {
	// close last polyline...
	if (polyline != null) {
	  polyline.getSegments().add( trackPoint);
	}
	// start new polyline...
	polyline= new PolylineImpl( colorIdx);
	cruise.getPolyLines().add( polyline);
	lastColorIdx= colorIdx;
      }
      polyline.getSegments().add( trackPoint);
    }
    // close last polyline...
    if (polyline != null) {
      if (trackPoint != null) {
	polyline.getSegments().add( trackPoint);
      }
      cruise.getPolyLines().add( polyline);
    }
    return null;

  }
}