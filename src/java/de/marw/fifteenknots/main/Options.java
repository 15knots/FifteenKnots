
package de.marw.fifteenknots.main;

import java.util.ArrayList;
import java.util.List;


/**
 * Parsed commandline options.
 *
 * @author Martin Weber
 */
class Options
{

  private List<BoatOptions> boats= new ArrayList<BoatOptions>();

  public void addBoat( BoatOptions boat)
  {
    boats.add( boat);
    boat.setNumber( boats.size());
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
    for (ValidatableOption boat : boats) {
      boat.validate();
    }
  }

}