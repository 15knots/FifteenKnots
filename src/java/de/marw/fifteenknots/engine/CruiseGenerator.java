// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import de.marw.fifteenknots.model.Vehicle;
import de.marw.fifteenknots.model.Cruise;
import de.marw.fifteenknots.nmeareader.TrackEvent;


/**
 * Reads all files for a {@code Vehicle}, extracts the the {@link TrackEvent track
 * points} and returns the {@link Cruise} object from the {@link #call()}
 * method.
 *
 * @author Martin Weber
 */
public class CruiseGenerator implements Callable<Cruise> {

  private final Vehicle vehicle;

  private final TrackGenerator trackGenerator;

  private RaceModelFactory modelFactory;

  /**
   * Constructs a new object with the specified vehicle and zero files to read in.
   * No model factory will be set.
   *
   * @param vehicle
   *        the vehicle for which the cruise will be generated.
   * @param modelFactory
   *        the model factory used to create race model objects.
   * @see #setModelFactory(RaceModelFactory)
   * @see #addFileName(String)
   */
  public CruiseGenerator( Vehicle vehicle, RaceModelFactory modelFactory) {
    if (vehicle == null) {
      throw new NullPointerException( "vehicle");
    }
    this.vehicle= vehicle;
    this.modelFactory= modelFactory;
    this.trackGenerator= new TrackGenerator();
  }

  /**
   * Gets the factory for objects of the race model.
   *
   * @return the current model factory or {@code null}, if none has been set.
   */
  public RaceModelFactory getModelFactory() {
    return this.modelFactory;
  }

  /**
   * Sets the model factory used to create race model objects.
   */
  public void setModelFactory( RaceModelFactory modelFactory) {
    this.modelFactory= modelFactory;
  }

  /**
   * Adds all specified input file names to be read.
   *
   * @see CruiseGenerator#addFileName(String)
   */
  public void addFileNames( Collection<String> fileNames) {
    trackGenerator.addFileNames( fileNames);
  }

  /**
   * Adds the specified input file name to be read.
   *
   * @see CruiseGenerator#addFileNames(Collection)
   */
  public void addFileName( String fileName) {
    trackGenerator.addFileName( fileName);
  }

  /**
   * Reads all input files and gathers track events.
   *
   * @return all track events, sorted by time stamp.
   * @throws FileNotFoundException
   *         if the specified file cannot be found
   * @throws IOException
   *         If an I/O error occurs
   * @throws IllegalStateException
   *         if no {@link #setModelFactory(RaceModelFactory) model factory} has
   *         been set.
   */
  public Cruise call() throws FileNotFoundException, IOException {
    if (modelFactory == null)
      throw new IllegalStateException( "model factory not set");
    final List<TrackEvent> track= trackGenerator.generate();
    return modelFactory.createCruise( vehicle, track);
  }
}