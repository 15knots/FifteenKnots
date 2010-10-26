// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.main;

import java.awt.Color;
import java.awt.Font;
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

import com.flagstone.transform.*;
import com.flagstone.transform.util.FSShapeConstructor;
import com.flagstone.transform.util.FSTextConstructor;

import de.marw.fifteenknots.engine.IProcessor;
import de.marw.fifteenknots.engine.MBBCalculator;
import de.marw.fifteenknots.engine.QuickHull;
import de.marw.fifteenknots.engine.QuickHullCalculator;
import de.marw.fifteenknots.engine.RaceModelBuilder;
import de.marw.fifteenknots.engine.SpeedColorEncoder;
import de.marw.fifteenknots.engine.ThreadPoolExecutorService;
import de.marw.fifteenknots.model.Cruise;
import de.marw.fifteenknots.model.RaceModel;
import de.marw.fifteenknots.nmeareader.Position2D;


/**
 * A Processor that produces output in the SWF-format (Adobe shockwave)
 *
 * @author Martin Weber
 */
class SWFProcessor implements IProcessor {

  private final Options options;

  private String outputFileName;

  /**
   * @param globalOptions
   *        parsed global commandline options
   * @param outputFileName
   *        the name of the output file or {@code null}, if output should go to
   *        stdout.
   */
  public SWFProcessor( Options globalOptions, String outputFileName) {
    if (globalOptions == null) {
      throw new NullPointerException( "options");
    }
    this.options= globalOptions;
    this.outputFileName= outputFileName;
  }

  /**
   * @throws FileNotFoundException
   *         if one of the specified files cannot be found
   * @throws IOException
   *         If an I/O error occurs
   * @see de.marw.fifteenknots.engine.IProcessor#process()
   */
  public void process() throws FileNotFoundException, IOException {
    RaceModelBuilder builder= new BasicRaceModelBuilder( options);

    RaceModel raceModel= builder.buildModel();
    getMinimumBoundingBox( raceModel);

    // render the output...
// renderer.process( model);
    FSMovie movie= createMovie( raceModel);
    movie.encodeToFile( outputFileName);
  }

  /**
   * Computes the minimum bounding box of the race.
   *
   * @param raceModel
   *        the race of the boats.
   * @return an array containing the corner positions of the minimum bounding box
   */
  public static Position2D[] getMinimumBoundingBox( RaceModel raceModel) {
    final List< ? extends Cruise> cruises= raceModel.getCruises();
    List<List<Position2D>> hulls= getConvexHulls( cruises);
    // merge convex hulls..
    List<Position2D> points= new ArrayList<Position2D>( 10 * cruises.size());
    for (List<Position2D> hull : hulls) {
      for (Position2D point : hull) {
	points.add( point);
      }
    }
   return MBBCalculator.mbbSpherical( points);
  }

  /**
   * Gets all convex hulls of the cruises.
   *
   * @return A list of convex hulls, one for each cruise.
   */
  private static List<List<Position2D>> getConvexHulls( List< ? extends Cruise> cruises) {
    final int size= cruises.size();
    final List<List<Position2D>> hulls=
      new ArrayList<List<Position2D>>( cruises.size());
    if (size == 1) {
      // optimization for a single boat
      hulls.add( QuickHull.quickHullOfTrack( cruises.get( 0).getTrackpoints()));
      return hulls;
    }
    else if (size == 0) {
      return hulls;
    }

    // create workers and returned list..
    ArrayList<Callable<List<Position2D>>> workers=
      new ArrayList<Callable<List<Position2D>>>( size);
    for (Cruise boatOptions : cruises) {
      workers.add( new QuickHullCalculator( boatOptions.getTrackpoints()));
    }

    // start workers and wait for all to finish
    ExecutorService e= ThreadPoolExecutorService.getService();
    try {
      List<Future<List<Position2D>>> workerResults= e.invokeAll( workers);
      for (Future<List<Position2D>> result : workerResults) {
	try {
	  // throws the exception if one occurred during the invocation
	  List<Position2D> hull= result.get( 0, TimeUnit.MILLISECONDS);
	  hulls.add( hull);
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
    return hulls;

  }

  private FSMovie createMovie( RaceModel raceModel) {
    int height= 10000;
    int width= height * 5 / 4;
    int fontSize= 240;

    FSMovie movie= new FSMovie();
    movie.add( new FSEnableDebugger2( ""));

    String txt= "The quick, brown, fox jumped over the lazy dog.";
    char[] characters= txt.toCharArray();
    java.util.Arrays.sort( characters);

    FSTextConstructor constructor=
      new FSTextConstructor( movie.newIdentifier(), new Font( "Arial",
	Font.PLAIN, 1));
    constructor.willDisplay( characters);

    FSDefineFont2 definition= constructor.defineFont();
    movie.add( definition);

    movie.setFrameSize( new FSBounds( 0, 0, width, height));
    movie.setFrameRate( 24.0f);
    movie.add( new FSSetBackgroundColor( FSColorTable.darkblue()));
// movie.add( new FSPlaceObject2( text.getIdentifier(), 1, 0, 0));
// movie.add( new FSShowFrame());
    ArrayList<FSLayer> layers= new ArrayList<FSLayer>();
    // one layer per boat...
    final int BOATS= 30;

    SpeedColorEncoder colorEncoder= new SpeedColorEncoder( BOATS, 0.0f, BOATS);
    for (int boat= 0; boat < BOATS; boat++) {
      FSLayer layer= new FSLayer( boat + 1);
      layers.add( layer);
      FSDefineObject boatShape=
	createBoatShape( movie.newIdentifier(), colorEncoder.encodeSpeed( Float
	  .valueOf( boat)));
      FSDefineShape3 text=
	constructor.defineShape( movie.newIdentifier(), txt, fontSize,
	  FSColorTable.green());
// layer.select( text);
      layer.select( boatShape);
    }
    // place boat
    for (int boat= 0; boat < 1; boat++) {
      FSLayer layer= layers.get( boat);
      layer.move( 600, 600);
      layer.show();
    }
    // move boats...
    for (int f= 0; f < 73; f++) {
      for (int boat= 0; boat < BOATS; boat++) {
	FSLayer layer= layers.get( boat);
	FSCoordTransform transform= new FSCoordTransform();
// transform.translate( f * 100, f * 100);
	transform.translate( f * 20 + (boat + 1) * 60, (boat + 1) * 80);
	transform.rotate( f * 5.0 * (boat % 2 == 0
	  ? 1 : -1));
	layer.change( transform);
	layer.show();
      }
    }

    movie.add( FSLayer.merge( layers));
    {
      // Add STOP action
      ArrayList<FSAction> actions= new ArrayList<FSAction>();
      actions.add( FSAction.Stop());
      movie.add( new FSDoAction( actions));
    }
    // frame where actions will be executed
    movie.add( FSShowFrame.getInstance());

    return movie;
  }

  private FSDefineObject createBoatShape( int identifier, Color color) {

// ArrayList<FSLineStyle> lineStyles= new ArrayList<FSLineStyle>();
// ArrayList<FSFillStyle> fillStyles= new ArrayList<FSFillStyle>();

    /*
     * Define the outline...
     */
    int width= 194;
    int length= 505;
    int mast= 303; // pos of mast from aft

    // start_*: first line to draw starts here
    final int start_x= width / 2, start_y= 0;
    FSShapeConstructor path= new FSShapeConstructor();
    path.add( new FSSolidLine( 20, FSColorTable.white()));
    path.add( new FSSolidFill( new FSColor( color.getRed(), color.getGreen(),
      color.getBlue())));
    path.selectLineStyle( 0);
    path.selectFillStyle( 0);
    path.move( start_x, start_y);
    path.curve( 0, -2 * length, -start_x, start_y);
    path.line( -width / 2, mast);
    path.line( width / 2, mast);
    path.line( start_x, start_y);
    path.closePath();
    // draw cross
// path.newPath();
// path.move( start_x, start_y);
// path.selectFillStyle( 0);
// path.line( -start_x, start_y);

    FSDefineShape2 shape= path.defineShape( identifier);
    return shape;
  }
}