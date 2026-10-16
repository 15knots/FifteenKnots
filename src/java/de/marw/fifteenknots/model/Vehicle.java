// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.model;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Metadata of a vehicle.
 *
 * @author Martin Weber
 */
public class Vehicle
{
  private final int index;
  private final List<String> fileNames;

  private String name;


  /**
   * Creates a new vehicle with the index
   *
   * @param index
   * @param inputFileNames
   */
  public Vehicle( int index, List<String> inputFileNames)
  {
    this.index= index;
    Objects.requireNonNull(inputFileNames);
    this.fileNames = inputFileNames.stream().map(s -> {
      return new File(s).getName();
    }).collect(Collectors.toList());
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

  /** Gets the names of the NMEA input files of this vehicle.
   */
  public List<String> getFileNames(){
    return fileNames;
  }
}
