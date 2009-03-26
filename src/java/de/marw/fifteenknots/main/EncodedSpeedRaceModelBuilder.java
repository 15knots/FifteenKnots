// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.marw.fifteenknots.engine.EncodedSpeedRMFactory;
import de.marw.fifteenknots.engine.PolylineCalculator;
import de.marw.fifteenknots.engine.RaceModelFactory;
import de.marw.fifteenknots.engine.SpeedColorEncoder;
import de.marw.fifteenknots.engine.SpeedLimitsCalculator;
import de.marw.fifteenknots.engine.ThreadPoolExecutorService;
import de.marw.fifteenknots.model.EncodedSpeedRaceModel;
import de.marw.fifteenknots.model.RaceModel;
import de.marw.fifteenknots.model.SpeedCruise;


/**
 * Builder for a race model that contains {@link SpeedCruise}s and information
 * on boat's speeds for encoding of speed as a colored line.
 *
 * @author Martin Weber
 */
class EncodedSpeedRaceModelBuilder extends BasicRaceModelBuilder {

  private final int colorCount;

  /**
   * @param options
   *        parsed global commandline options
   * @param colorCount
   *        the number of colors to use for visual boat speed coding.
   */
  public EncodedSpeedRaceModelBuilder( Options options, int colorCount) {
    super( options);
    this.colorCount= colorCount;

    setModelFactory( new EncodedSpeedRMFactory());
  }

  /**
   * @throws IllegalStateException
   *         if no {@link #setModelFactory(RaceModelFactory) model factory} has
   *         been set.
   */
  public RaceModel buildModel() throws FileNotFoundException, IOException {
    EncodedSpeedRaceModel model= (EncodedSpeedRaceModel) super.buildModel();
    @SuppressWarnings("unchecked")
    List<SpeedCruise> cruises= (List<SpeedCruise>) model.getCruises();
    // compute speed values in tracks, if not present...
    // TODO

    calcSpeedLimits( cruises);
    SpeedColorEncoder sce= createColorEncoder( cruises);

    // calculate Polylines of speed levels for all boats...
    calcPolyLines( cruises, sce);

    model.setSpeedEncoding( sce.getSpeedEncoding());
    return model;
  }

  /**
   * @param cruises
   * @param sce
   */
  private void calcPolyLines( List<SpeedCruise> cruises, SpeedColorEncoder sce) {
    List<Callable<Object>> workers=
      new ArrayList<Callable<Object>>( cruises.size());

    // create workers..
    for (SpeedCruise cruise : cruises) {
      workers.add( new PolylineCalculator( cruise, sce));
    }

    // start workers and wait for all to finish
    ExecutorService e= ThreadPoolExecutorService.getService();
    try {
      List<Future<Object>> workerResults= e.invokeAll( workers);
      for (Future<Object> result : workerResults) {
	try {
	  // throws the exception if one occurred during the invocation
	  result.get( 0, TimeUnit.MILLISECONDS);
	}
	catch (ExecutionException ex) {
	  // raise exception that occured in worker
	  final Throwable cause= ex.getCause();
	  if (cause instanceof RuntimeException) {
	    throw (RuntimeException) cause;
	  }
	  else if (cause instanceof Error) {
	    throw (Error) cause;
	  }
	}
	catch (CancellationException ignore) {
	}
	catch (TimeoutException ignore) {
	}
      }
    }
    catch (InterruptedException ignore) {
      // ignore and finish
    }

  }

  /**
   * @param cruises
   */
  private SpeedColorEncoder createColorEncoder( List<SpeedCruise> cruises) {
    // determine overall min and max speed...
    float speedMin= Float.MAX_VALUE;
    float speedMax= Float.MIN_VALUE;
    int colors= 1;
    for (SpeedCruise cruise : cruises) {
      speedMin= Math.min( speedMin, cruise.getSpeedMin());
      speedMax= Math.max( speedMax, cruise.getSpeedMax());
      colors= Math.max( colors, cruise.getTrackpoints().size());
    }
    // limit num of colors to a sensible value, to avoid OutOfMemoryError if
    // millions of colors were requested...
    return new SpeedColorEncoder( Math.min( colorCount, colors), speedMin,
      speedMax);
  }

  /**
   * Enriches each of the cruises with information about min and max speed.
   *
   * @param cruises
   * @throws RuntimeException
   */
  private void calcSpeedLimits( List<SpeedCruise> cruises)
    throws RuntimeException {
    // determine min and max speed per cruise...
    List<Callable<Object>> workers=
      new ArrayList<Callable<Object>>( cruises.size());

    // create workers..
    for (SpeedCruise cruise : cruises) {
      workers.add( new SpeedLimitsCalculator( cruise));
    }

    // start workers and wait for all to finish
    ExecutorService e= ThreadPoolExecutorService.getService();
    try {
      List<Future<Object>> workerResults= e.invokeAll( workers);
      for (Future<Object> result : workerResults) {
	try {
	  // throws the exception if one occurred during the invocation
	  result.get( 0, TimeUnit.MILLISECONDS);
	}
	catch (ExecutionException ex) {
	  // raise exception that occured in worker
	  final Throwable cause= ex.getCause();
	  if (cause instanceof RuntimeException) {
	    throw (RuntimeException) cause;
	  }
	  else if (cause instanceof Error) {
	    throw (Error) cause;
	  }
	}
	catch (CancellationException ignore) {
	}
	catch (TimeoutException ignore) {
	}
      }
    }
    catch (InterruptedException ignore) {
      // ignore and finish
    }
  }

}
