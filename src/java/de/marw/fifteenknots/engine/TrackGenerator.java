// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.marw.fifteenknots.nmeareader.ITrackListener;
import de.marw.fifteenknots.nmeareader.NmeaParser;
import de.marw.fifteenknots.nmeareader.TrackEvent;


/**
 * Reads all input files for a {@code Boat}, extracts the the {@link TrackEvent
 * track points} and returns the {@link TrackEvent track points} from the
 * {@link #generate()} method.
 *
 * @author Martin Weber
 */
public class TrackGenerator
{
  private Set<String> fileNames;

  /**
   * Constructs a new object with zero files to read in.
   *
   * @see #addFileName(String)
   */
  public TrackGenerator()
  {
    fileNames= new HashSet<String>( 2);
  }

  /**
   * Adds all specified input file names to be read.
   *
   * @see TrackGenerator#addFileName(String)
   */
  public void addFileNames( Collection<String> fileNames)
  {
    this.fileNames.addAll( fileNames);
  }

  /**
   * Adds the specified input file name to be read.
   *
   * @see TrackGenerator#addFileNames(Collection)
   */
  public void addFileName( String fileName)
  {
    fileNames.add( fileName);
  }

  // /**
  // * Gets the file names that will be read.
  // *
  // * @return the current fileNames property.
  // */
  // public Set<String> getFileNames()
  // {
  // return fileNames;
  // }

  /**
   * Reads all input files and gathers track events.
   *
   * @throws FileNotFoundException
   *         if the specified file cannot be found
   * @throws IOException
   *         If an I/O error occurs
   * @return all track events, sorted by time stamp.
   * @see #addFileNames(Collection)
   */
  public List<TrackEvent> generate() throws FileNotFoundException, IOException
  {
    Vector<TrackEvent> trackBuffer= new Vector<TrackEvent>( 60 * 60, 5 * 60);
    TrackBufferAppender bufferAppender= new TrackBufferAppender( trackBuffer);

    final int fileCnt= fileNames.size();
    ArrayList<Callable<Object>> workers=
      new ArrayList<Callable<Object>>( fileCnt);
    // create workers..
    for (String fileName : fileNames) {
      workers.add( new InputFileWorker( fileName, bufferAppender));
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

    if (fileCnt > 1) {
      Collections.sort( trackBuffer, new ByDateComparator());
    }
    return trackBuffer;
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