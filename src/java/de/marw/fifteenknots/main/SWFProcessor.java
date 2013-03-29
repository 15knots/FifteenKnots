// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.main;

import java.io.File;
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
import java.util.zip.DataFormatException;

import com.flagstone.transform.Background;
import com.flagstone.transform.EnableDebugger2;
import com.flagstone.transform.Movie;
import com.flagstone.transform.MovieHeader;
import com.flagstone.transform.Place2;
import com.flagstone.transform.PlaceType;
import com.flagstone.transform.ShowFrame;
import com.flagstone.transform.datatype.Bounds;
import com.flagstone.transform.datatype.Color;
import com.flagstone.transform.datatype.CoordTransform;
import com.flagstone.transform.datatype.WebPalette;
import com.flagstone.transform.fillstyle.SolidFill;
import com.flagstone.transform.linestyle.LineStyle1;
import com.flagstone.transform.shape.DefineShape2;
import com.flagstone.transform.shape.ShapeTag;
import com.flagstone.transform.util.movie.Layer;
import com.flagstone.transform.util.shape.Canvas;

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
  public SWFProcessor(Options globalOptions, String outputFileName) {
    if (globalOptions == null) {
      throw new NullPointerException("options");
    }
    this.options = globalOptions;
    this.outputFileName = outputFileName;
  }

  /**
   * @throws FileNotFoundException
   *         if one of the specified files cannot be found
   * @throws IOException
   *         If an I/O error occurs
   * @see de.marw.fifteenknots.engine.IProcessor#process()
   */
  public void process() throws FileNotFoundException, IOException {
    RaceModelBuilder builder = new BasicRaceModelBuilder(options);

    RaceModel raceModel = builder.buildModel();
    getMinimumBoundingBox(raceModel);

    // render the output...
// renderer.process( model);
    Movie movie = createMovie(raceModel);
    try {
      movie.encodeToFile(new File(outputFileName));
    } catch (DataFormatException ex) {
      // TODO introduce somthing like ProcessorExecption as a wrapper
      throw new RuntimeException(ex);
    }
  }

  /**
   * Computes the minimum bounding box of the race.
   *
   * @param raceModel
   *        the race of the boats.
   * @return an array containing the corner positions of the minimum bounding
   *         box
   */
  public static Position2D[] getMinimumBoundingBox(RaceModel raceModel) {
    final List<? extends Cruise> cruises = raceModel.getCruises();
    List<List<Position2D>> hulls = getConvexHulls(cruises);
    // merge convex hulls..
    List<Position2D> points = new ArrayList<Position2D>(10 * cruises.size());
    for (List<Position2D> hull : hulls) {
      for (Position2D point : hull) {
	points.add(point);
      }
    }
    return MBBCalculator.mbbSpherical(points);
  }

  /**
   * Gets all convex hulls of the cruises.
   *
   * @return A list of convex hulls, one for each cruise.
   */
  private static List<List<Position2D>> getConvexHulls(
      List<? extends Cruise> cruises) {
    final int size = cruises.size();
    final List<List<Position2D>> hulls = new ArrayList<List<Position2D>>(
	cruises.size());
    if (size == 1) {
      // optimization for a single boat
      hulls.add(QuickHull.quickHullOfTrack(cruises.get(0).getTrackpoints()));
      return hulls;
    } else if (size == 0) {
      return hulls;
    }

    // create workers and returned list..
    ArrayList<Callable<List<Position2D>>> workers = new ArrayList<Callable<List<Position2D>>>(
	size);
    for (Cruise boatOptions : cruises) {
      workers.add(new QuickHullCalculator(boatOptions.getTrackpoints()));
    }

    // start workers and wait for all to finish
    ExecutorService e = ThreadPoolExecutorService.getService();
    try {
      List<Future<List<Position2D>>> workerResults = e.invokeAll(workers);
      for (Future<List<Position2D>> result : workerResults) {
	try {
	  // throws the exception if one occurred during the invocation
	  List<Position2D> hull = result.get(0, TimeUnit.MILLISECONDS);
	  hulls.add(hull);
	} catch (ExecutionException ex) {
	  // raise exception that occured in worker
	  final Throwable cause = ex.getCause();
	  if (cause instanceof RuntimeException) {
	    throw (RuntimeException) cause;
	  } else if (cause instanceof Error) {
	    throw (Error) cause;
	  }
	} catch (CancellationException ignore) {
	} catch (TimeoutException ignore) {
	}
      }
    } catch (InterruptedException ignore) {
      // ignore and finish
    }
    return hulls;

  }

  private Movie createMovie(RaceModel raceModel) {
    int height = 10000;
    int width = height * 16 / 9;
    int fontSize = 240;

    int uid = 1;

    Movie movie = new Movie();
    final MovieHeader header = new MovieHeader();
    header.setFrameSize(new Bounds(0, 0, width, height));
    header.setFrameRate(25.0f);
    movie.add(header);
    movie.add(new EnableDebugger2("15kts"));

    movie.add(new Background(WebPalette.DARK_BLUE.color()));

    ArrayList<Layer> layers = new ArrayList<Layer>();
    // one layer per boat...
    final int BOATS = 22;
    ArrayList<ShapeTag> boatShapes = new ArrayList<ShapeTag>();

    SpeedColorEncoder colorEncoder = new SpeedColorEncoder(BOATS, 0.0f, BOATS);
    for (int boat = 0; boat < BOATS; boat++) {
      Layer layer = new Layer(boat);
      layers.add(layer);
      ShapeTag boatShape = createBoatShape(uid++,
	  colorEncoder.encodeSpeed(Float.valueOf(boat)));
      movie.add(boatShape);
      boatShapes.add(boatShape);
    }
    // place boats...
    for (int boat = 0; boat < BOATS; boat++) {
      final ShapeTag bs = boatShapes.get(boat);
      CoordTransform position = CoordTransform.translate(600 + 90 * boat, 600);
      movie.add(new Place2().setType(PlaceType.NEW).setLayer(boat + 1)
	  .setIdentifier(bs.getIdentifier()).setTransform(position));
    }
    // move boats...
    for (int f = 0; f < 25 * 10; f++) {
      for (int boat = 0; boat < BOATS; boat++) {
	CoordTransform position = CoordTransform.translate(f * 20 + (boat + 1)
	    * 160, (boat + 1) * 180);
	CoordTransform orientation = CoordTransform.rotate(45 + f * 5
	    * (boat % 2 == 0 ? 1 : -1));
	CoordTransform transform = new CoordTransform(CoordTransform.product(
	    position.getMatrix(), orientation.getMatrix()));
	final ShapeTag bs = boatShapes.get(boat);

	movie.add(new Place2().setType(PlaceType.MODIFY).setLayer(boat + 1)
	    .setIdentifier(bs.getIdentifier()).setTransform(transform));
      }
      movie.add(ShowFrame.getInstance());
    }

//    {
//      // Add STOP action
//      ArrayList<Action> actions = new ArrayList<Action>();
//      actions.add(Action.Stop());
//      movie.add(new DoAction(actions));
//    }
//    // frame where actions will be executed
//    movie.add(ShowFrame.getInstance());

    return movie;
  }

  private ShapeTag createBoatShape(int identifier, java.awt.Color color) {

    /*
     * Define the outline...
     */
    int width = 194;
    int length = 505;
    int mast = 303; // pos of mast from aft

    // start_*: first line to draw starts here
    final int start_x = width / 2, start_y = 0;
    Canvas path = new Canvas();

    path.setLineStyle(new LineStyle1(20, WebPalette.WHITE.color()));
    path.setFillStyle(new SolidFill(new Color(color.getRed(), color.getGreen(),
	color.getBlue())));
    path.move(start_x, start_y);
    path.curve(0, -2 * length, -start_x, start_y);
    path.line(-width / 2, mast);
    path.line(width / 2, mast);
    path.line(start_x, start_y);
    path.close();

    DefineShape2 shape = path.defineShape(identifier);
    return shape;
  }
}