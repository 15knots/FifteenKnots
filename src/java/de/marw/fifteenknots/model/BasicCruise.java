// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.model;

import java.util.List;

import de.marw.fifteenknots.engine.SphericalUtil;
import de.marw.fifteenknots.nmeareader.Position2D;
import de.marw.fifteenknots.nmeareader.TrackEvent;


/**
 * A cruise of a vehicle. Contains vehicle metadata and the list of track points. None
 * of the track points in the list is guaranteed to hold information about
 * {@link TrackEvent#getSpeed() speed} or {@link TrackEvent#getBearing()
 * bearing}, only {@link TrackEvent#getDate() date} and
 * {@link TrackEvent#getPosition() position} is provided.
 *
 * @author Martin Weber
 */
public class BasicCruise implements Cruise
{
  private final List<TrackEvent> track;

  private final Vehicle vehicle;

  private UnitLength cachedLength;

  /**
   * Constructs a cruise object with the specified vehicle and track points.
   */
  public BasicCruise( Vehicle vehicle, List<TrackEvent> track)
  {
    if (track == null) {
      throw new NullPointerException( "track");
    }
    this.track= track;
    if (vehicle == null) {
      throw new NullPointerException( "vehicle");
    }
    this.vehicle= vehicle;
  }

  /**
   * Gets the metadata about the vehicle.
   */
  @Override
  public Vehicle getVehicle()
  {
    return vehicle;
  }

  /**
   * Gets all track points recorded for this cruise.
   *
   * @return all track points ordered ascending by time.
   */
  @Override
  public List<TrackEvent> getTrackpoints()
  {
    return track;
  }

  @Override
  public UnitLength getTotalLength() {
    if (cachedLength == null) {
      List<Position2D> posns = track.stream().map(TrackEvent::getPosition)
          .toList();
      double length = SphericalUtil.computeLength(posns);
      cachedLength = new UnitLength(length / 1000, "km");
    }
    return cachedLength;
  }
}
