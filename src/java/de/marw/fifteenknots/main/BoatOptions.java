
package de.marw.fifteenknots.main;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Parsed commandline options for a single boat.
 * 
 * @author Martin Weber
 */
public class BoatOptions
{
  /** the number of the boat by position on commandline */
  private int number;

  private String name;

  private List<String> fileNames= new ArrayList<String>();

  /**
   * Gets the number property.
   * 
   * @return the current number property.
   */
  public int getNumber()
  {
    return this.number;
  }

  /**
   * Gets the name of the boat.
   * 
   * @return the boat name or {@code null}, if no name was specified.
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * Gets the fileNames property.
   * 
   * @return the current fileNames property.
   */
  public List<String> getFileNames()
  {
    return this.fileNames;
  }

  /**
   * Sets the number property.
   * 
   * @see BoatOptions#number
   */
  void setNumber( int number)
  {
    this.number= number;
  }

  /**
   * Sets the name property.
   * 
   * @see BoatOptions#name
   */
  void setName( String name)
  {
    this.name= name;
  }

  /**
   * 
   */
  public void validate() throws OptionValidationException
  {
    if (fileNames.isEmpty()) {
      String format= "No input fileNames given for boat number {0,number,integer}";
      throw new OptionValidationException( MessageFormat
        .format( format, number));
    }
  }

}