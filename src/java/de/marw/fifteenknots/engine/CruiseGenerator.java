// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.marw.fifteenknots.main.BoatOptions;
import de.marw.fifteenknots.model.Boat;
import de.marw.fifteenknots.model.ICruise;
import de.marw.fifteenknots.nmeareader.ITrackListener;
import de.marw.fifteenknots.nmeareader.NmeaParser;
import de.marw.fifteenknots.nmeareader.TrackEvent;


/**
 * Reads all files specified in the passed in {@code BoatOptions} and returns a
 * {@link ICruise} object from the {@link #call()} method.
 * 
 * @author Martin Weber
 */
class CruiseGenerator implements Callable<Cruise>
{
  private BoatOptions options;

  /**
   * @param options
   */
  public CruiseGenerator( BoatOptions options)
  {
    this.options= options;
  }

  /**
   * Reads files and gathers track events.
   * 
   * @throws FileNotFoundException
   *         if the specified file cannot be found
   * @throws IOException
   *         If an I/O error occurs
   * @return all track events, sorted by time stamp.
   */
  public Cruise call() throws FileNotFoundException, IOException
  {
    Vector<TrackEvent> trackBuffer= new Vector<TrackEvent>( 60 * 60, 5 * 60);
    TrackBufferAppender bufferAppender= new TrackBufferAppender( trackBuffer);

    final List<String> fileNames= options.getFileNames();
    final int fileCnt= fileNames.size();
    ArrayList<Callable<Object>> workers=
      new ArrayList<Callable<Object>>( fileCnt);
    // create workers..
    for (int i= 0; i < fileCnt; i++) {
      workers.add( new InputFileWorker( fileNames.get( i), bufferAppender));
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
          throw (IOException) ex.getCause();
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

    if (fileCnt > 1) {
      Collections.sort( trackBuffer, new ByDateComparator());
    }
    final Boat boat= new Boat( options.getNumber());
    boat.setName( options.getName());
    return new Cruise( boat, trackBuffer);
  }

  // //////////////////////////////////////////////////////////////////
  // inner classes
  // //////////////////////////////////////////////////////////////////
  /**
   * Appends {@link TrackEvent}s to a buffer.
   * 
   * @author Martin Weber
   */
  private static class TrackBufferAppender implements ITrackListener
  {

    /** shared track buffer */
    private Vector<TrackEvent> buffer;

    /**
     * @param buffer
     *        the shared buffer to fill with track events.
     */
    public TrackBufferAppender( Vector<TrackEvent> buffer)
    {
      if (buffer == null) {
        throw new NullPointerException( "buffer");
      }
      this.buffer= buffer;
    }

    /*-
     * @see de.marw.fifteenknots.nmeareader.ITrackListener#trackPoint(de.marw.fifteenknots.nmeareader.TrackEvent)
     */
    public void trackPoint( TrackEvent evt)
    {
      buffer.add( evt);
    }

  }// TrackBufferAppender

  private static class InputFileWorker implements Callable<Object>
  {
    private NmeaParser parser;

    /**
     * @param fileName
     * @param trackListener
     * @throws FileNotFoundException
     *         if the specified file cannot be found
     */
    public InputFileWorker( String fileName, ITrackListener trackListener)
      throws FileNotFoundException
    {
      if (fileName == null) {
        throw new NullPointerException( "fileName");
      }
      if (trackListener == null) {
        throw new NullPointerException( "trackListener");
      }

      FileInputStream stream= new FileInputStream( fileName);
      parser= new NmeaParser( stream, fileName);
      parser.addTrackListener( trackListener);
    }

    /**
     * Parses the input file and appends events to the buffer.
     * 
     * @return always {@code null}
     * @throws IOException
     *         If an I/O error occurs
     */
    public Object call() throws IOException
    {
      parser.parse();
      return null;
    }

  }// InputFileWorker

  private static class ByDateComparator implements Comparator<TrackEvent>
  {

    /*-
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare( TrackEvent o1, TrackEvent o2)
    {
      return (int) (o1.getDate() - o2.getDate());
    }

  } // ByDateComparator
}