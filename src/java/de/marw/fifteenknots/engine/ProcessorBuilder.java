// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.engine;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.marw.fifteenknots.main.BoatOptions;
import de.marw.fifteenknots.main.Options;
import de.marw.fifteenknots.model.EncodedSpeedRaceModel;
import de.marw.fifteenknots.model.ICruise;
import de.marw.fifteenknots.model.IPolyLine;
import de.marw.fifteenknots.nmeareader.TrackEvent;
import de.marw.fifteenknots.render.ARGBToABRGMethod;
import de.marw.fifteenknots.render.MillisToDateMethod;
import de.marw.fifteenknots.render.TemplateRenderer;
import freemarker.template.TemplateException;


/**
 * Builds the processing chain from the commandline arguments.
 * 
 * @author Martin Weber
 */
public class ProcessorBuilder
{

  private final Options options;

  private IProcessor processor;

  /**
   * @param options
   *        the parsed commandline arguments
   */
  public ProcessorBuilder( Options options)
  {
    if (options == null) {
      throw new NullPointerException( "options");
    }
    this.options= options;

  }

  /**
   * @throws IOException
   */
  public IProcessor getProcessor() throws IOException
  {
    if (processor == null) {
      processor= createProcessor();

    }
    return processor;
  }

  /**
   * @return
   * @throws IOException
   *         if the output file could not be created
   */
  private IProcessor createProcessor() throws IOException
  {

    // create output writer for template engine...
    Writer writer;
    {
      OutputStream out;
      if (options.getOutputFileName() != null) {
        File file= new File( options.getOutputFileName());
        file.createNewFile();
        out= new BufferedOutputStream( new FileOutputStream( file));
      }
      else {
        out= System.out;
      }
      writer= new OutputStreamWriter( out, "UTF-8");
    }
    return new KMLProcessor( writer, options);
  }

  // //////////////////////////////////////////////////////////////////
  // inner classes
  // //////////////////////////////////////////////////////////////////
  /**
   * A Processor that produces output in the KML-format (Google Earth)
   * 
   * @author Martin Weber
   */
  private static class KMLProcessor implements IProcessor
  {
    private final Writer writer;

    private final Options options;

    /**
     * @param writer
     */
    public KMLProcessor( Writer writer, Options options)
    {
      if (options == null) {
        throw new NullPointerException( "options");
      }
      this.writer= writer;
      if (options == null) {
        throw new NullPointerException( "options");
      }
      this.options= options;

    }

    /**
     * @throws FileNotFoundException
     *         if one of the specified files cannot be found
     * @throws IOException
     *         If an I/O error occurs
     * @see de.marw.fifteenknots.engine.IProcessor#process()
     */
    public void process() throws FileNotFoundException, IOException
    {
      // wait until all boats have produced their tracks...
      List<Cruise> cruises= gatherCruises( options.getBoats());
      // compute speed values in tracks, if not present...
      // TODO

      SpeedColorEncoder sce= createColorEncoder( cruises);
      // calculate Polylines of speed levels for all boats...
      for (ICruise cruise : cruises) {
        int lastColorIdx= -1;
        PolylineImpl polyline= null;
        TrackEvent trackPoint= null;
        for (Iterator<TrackEvent> iterator= cruise.getTrackpoints().iterator(); iterator
          .hasNext();) {
          trackPoint= iterator.next();
          if (trackPoint.getSpeed() == null)
            continue; // TODO
          int colorIdx= sce.getEncodedColorIndex( trackPoint.getSpeed());
          if (colorIdx != lastColorIdx) {
            // close last polyline...
            if (polyline != null) {
              polyline.getSegments().add( trackPoint);
            }
            // start new polyline...
            sce.encodeSpeed( trackPoint.getSpeed());
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

      }

      EncodedSpeedRaceModel raceModel=
        new EncodedSpeedRaceModel( cruises, new SpeedEncoding( sce.getRanges()));
      // render the output...
      Map<String, Object> model= new HashMap<String, Object>();
      model.put( "race", raceModel);
      // add conversion method to be invoked by Freemarker
      model.put( "millisToDate", new MillisToDateMethod());
      model.put( "toABGRhex", new ARGBToABRGMethod());
      TemplateRenderer renderer=
        new TemplateRenderer( writer, "speed-colored.kml.ftl");
      try {
        renderer.process( model);
      }
      catch (TemplateException ex) {
        // TODO Auto-generated catch block
        ex.printStackTrace();
      }
    }

    /**
     * @param cruises
     */
    private SpeedColorEncoder createColorEncoder( List<Cruise> cruises)
    {
      {
        // determine min and max speed per cruise...
        List<Callable<Object>> workers=
          new ArrayList<Callable<Object>>( cruises.size());

        // create workers..
        for (Cruise cruise : cruises) {
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
              throw (RuntimeException) ex.getCause();
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

      // determine overall min and max speed...
      float speedMin= Float.MAX_VALUE;
      float speedMax= Float.MIN_VALUE;
      for (Cruise cruise : cruises) {
        speedMin= Math.min( speedMin, cruise.getSpeedMin());
        speedMax= Math.max( speedMax, cruise.getSpeedMax());
      }
      return new SpeedColorEncoder( options.getColorCount(), speedMin, speedMax);
    }

    /**
     * Reads all input files and waits until all boats have produced their
     * tracks...
     * 
     * @return A list of {@link ICruise}s representing the boats, in the same
     *         sequential order as produced by the iterator for the given
     *         {@link BoatOptions} list.
     * @throws FileNotFoundException
     *         if one of the specified files cannot be found
     * @throws IOException
     *         If an I/O error occurs
     */
    private List<Cruise> gatherCruises( List<BoatOptions> boats)
      throws FileNotFoundException, IOException
    {
      ArrayList<Callable<Cruise>> workers=
        new ArrayList<Callable<Cruise>>( boats.size());
      List<Cruise> cruises= new ArrayList<Cruise>( boats.size());

      // create workers and returned list..
      for (BoatOptions boatOptions : boats) {
        workers.add( new CruiseGenerator( boatOptions));
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
      return cruises;

    }

    /**
     * @author Martin Weber
     */
    private static final class PolylineImpl implements IPolyLine
    {
      private final List<TrackEvent> segments= new ArrayList<TrackEvent>();

      private final int color;

      /**
       * @param colorIdx
       *        the color index for display of this polyline.
       */
      public PolylineImpl( int colorIdx)
      {
        this.color= colorIdx;
      }

      public int getColorIndex()
      {
        return color;
      }

      public List<TrackEvent> getSegments()
      {
        return segments;
      }
    }

  }// KMLProcessor

}
