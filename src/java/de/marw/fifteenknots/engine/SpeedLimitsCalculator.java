// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.engine;

import java.util.concurrent.Callable;

import de.marw.fifteenknots.model.SpeedCruise;
import de.marw.fifteenknots.nmeareader.TrackEvent;


/**
 * Enriches a {@code SpeedCruise} object with the calulated maximum and minimum
 * speed that the boat reached on its cruise.
 *
 * @author Martin Weber
 */
public class SpeedLimitsCalculator implements Callable<Object> {

  private final SpeedCruise cruise;

  /**
   * @param cruise
   *        the cruise for which the speeds should be calculated.
   */
  public SpeedLimitsCalculator( SpeedCruise cruise) {
    if (cruise == null) {
      throw new NullPointerException( "cruise");
    }
    this.cruise= cruise;
  }

  /**
   * Calulates the maximum and minimum speed that a boat reached on its cruise
   * from the cruise's {@linkplain SpeedCruise#getTrackpoints() track points}
   * and stores these values in the cruise object..
   *
   * @return always {@code null}
   */
  public Object call() {
    float speedMin= Float.MAX_VALUE;
    float speedMax= Float.MIN_VALUE;
    for (TrackEvent evt : cruise.getTrackpoints()) {
      final Float speed= evt.getSpeed();
      if (speed != null) {
	float speedF= speed.floatValue();
	if (speedF < speedMin) {
	  speedMin= speedF;
	}
	else if (speedF > speedMax) {
	  speedMax= speedF;
	}
      }
    }
    cruise.setSpeedMax( speedMax);
    cruise.setSpeedMin( speedMin);
    return null;
  }

}
