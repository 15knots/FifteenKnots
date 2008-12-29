// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.model;

/**
 * Metadata of a boat.
 * 
 * @author Martin Weber
 */
public class Boat
{
  private final int index;

  private String name;

  /**
   * Creates a new boat with the index
   * 
   * @param index
   */
  public Boat( int index)
  {
    this.index= index;
  }

  /**
   * Gets the name of the boat.
   * 
   * @return the boat name or {@code null}, if no name was specified.
   */
  public String getName()
  {
    return name;
  }

  /**
   * Sets the name of the boat.
   */
  public void setName( String name)
  {
    this.name= name;
  }

  /**
   * Gets the index of this boat in the list of the boats that were specified on
   * the commandline.
   * 
   * @return an int greater or equals tha zero.
   */
  public int getIndex()
  {
    return index;
  }
}
