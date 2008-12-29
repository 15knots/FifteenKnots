// $Header$
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

import de.marw.fifteenknots.engine.AbstractRaceModelBuilder;
import de.marw.fifteenknots.engine.BasicRMFactory;
import de.marw.fifteenknots.engine.CruiseGenerator;
import de.marw.fifteenknots.engine.RaceModelFactory;
import de.marw.fifteenknots.engine.ThreadPoolExecutorService;
import de.marw.fifteenknots.model.BasicRaceModel;
import de.marw.fifteenknots.model.Boat;
import de.marw.fifteenknots.model.Cruise;
import de.marw.fifteenknots.model.RaceModel;


/**
 * Builder for a basic race model that contains just [@link BasicCruise}s.
 *
 * @author Martin Weber
 */
class BasicRaceModelBuilder extends AbstractRaceModelBuilder {

  private final Options options;

  /**
   * @param options
   *        parsed global commandline options
   */
  public BasicRaceModelBuilder( Options options) {
    if (options == null) {
      throw new NullPointerException( "options");
    }
    this.options= options;
    setModelFactory( new BasicRMFactory());
  }

  /**
   * Gets the options property.
   *
   * @return the current options property.
   */
  public final Options getOptions() {
    return this.options;
  }

  /**
   * @throws IllegalStateException
   *         if no {@link #setModelFactory(RaceModelFactory) model factory} has
   *         been set.
   */
  public RaceModel buildModel() throws FileNotFoundException, IOException {
    // wait until all boats have produced their tracks...
    List<Cruise> cruises= gatherCruises( options.getBoats());
    BasicRaceModel model= (BasicRaceModel) getModelFactory().createRaceModel();
    model.setCruises( cruises);
    return model;
  }

  /**
   * Reads all input files and waits until all boats have produced their tracks.
   *
   * @return A list of {@link Cruise}s representing the boats, in the same
   *         sequential order as produced by the iterator for the given
   *         {@link BoatOptions} list.
   * @throws FileNotFoundException
   *         if one of the specified files cannot be found
   * @throws IOException
   *         If an I/O error occurs
   */
  private List<Cruise> gatherCruises( List<BoatOptions> boats)
    throws FileNotFoundException, IOException {
    ArrayList<Callable<Cruise>> workers=
      new ArrayList<Callable<Cruise>>( boats.size());
    List<Cruise> cruises= new ArrayList<Cruise>( boats.size());

    // create workers and returned list..
    for (BoatOptions boatOptions : boats) {
      Boat boat= new Boat( boatOptions.getNumber());
      boat.setName( boatOptions.getName());
      final CruiseGenerator cg= new CruiseGenerator( boat, getModelFactory());
      cg.addFileNames( boatOptions.getFileNames());
      workers.add( cg);
    }

    // start workers and wait for all to finish
    ExecutorService e= ThreadPoolExecutorService.getService();
    try {
      List<Future<Cruise>> workerResults= e.invokeAll( workers);
      for (Future<Cruise> result : workerResults) {
	try {
	  // throws the exception if one occurred during the invocation
	  Cruise cruise= result.get( 0, TimeUnit.MILLISECONDS);
	  cruises.add( cruise);
	}
	catch (ExecutionException ex) {
	  // raise exception that occured in worker
	  final Throwable cause= ex.getCause();
	  if (cause instanceof IOException) {
	    throw (IOException) cause;
	  }
	  else if (cause instanceof RuntimeException) {
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
    return cruises;

  }

}
