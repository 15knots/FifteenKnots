// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.model;

/**
 * Metadata of a vehicle.
 * 
 * @author Martin Weber
 */
public class Vehicle
{
  private final int index;

  private String name;

  /**
   * Creates a new vehicle with the index
   * 
   * @param index
   */
  public Vehicle( int index)
  {
    this.index= index;
  }

  /**
   * Gets the name of the vehicle.
   * 
   * @return the vehicle name or {@code null}, if no name was specified.
   */
  public String getName()
  {
    return name;
  }

  /**
   * Sets the name of the vehicle.
   */
  public void setName( String name)
  {
    this.name= name;
  }

  /**
   * Gets the index of this vehicle in the list of the vehicles that were specified on
   * the commandline.
   * 
   * @return an int greater or equals tha zero.
   */
  public int getIndex()
  {
    return index;
  }
}
