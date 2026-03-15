
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

  private List<VehicleOptions> vehicles= new ArrayList<VehicleOptions>();

  public void addVehicle( VehicleOptions vehicle)
  {
    vehicles.add( vehicle);
    vehicle.setNumber( vehicles.size());
  }

  /**
   * Gets the vehicles property.
   *
   * @return the current vehicles property.
   */
  public List<VehicleOptions> getVehicles()
  {
    return this.vehicles;
  }

  public void validate() throws OptionValidationException
  {
    for (ValidatableOption vehicle : vehicles) {
      vehicle.validate();
    }
  }

}