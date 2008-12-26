
package de.marw.fifteenknots.main;

import java.util.ArrayList;
import java.util.List;


/**
 * Parsed commandline options.
 * 
 * @author Martin Weber
 */
public class Options
{
  private String outputFile;

  private List<BoatOptions> boats= new ArrayList<BoatOptions>();

  public void addBoat( BoatOptions boat)
  {
    boats.add( boat);
    boat.setNumber( boats.size());
  }

  /**
   * Gets the outputFile property.
   * 
   * @return the current outputFile property.
   */
  public String getOutputFileName()
  {
    System.err.println();
    return this.outputFile;
  }

  /**
   * Gets the boats property.
   * 
   * @return the current boats property.
   */
  public List<BoatOptions> getBoats()
  {
    return this.boats;
  }

  public void validate() throws OptionValidationException
  {
    for (BoatOptions boat : boats) {
      boat.validate();
    }
  }

  /**
   * Sets the name of the output file property.
   * 
   * @see Options#outputFile
   */
  void setOutputFileName( String outputFile)
  {
    this.outputFile= outputFile;
  }

  /**
   * Gets the number of colors to use for visual boat speed coding.
   * 
   * @return a value grater than zero.
   */
  public int getColorCount()
  {
     return 120;
  }
}