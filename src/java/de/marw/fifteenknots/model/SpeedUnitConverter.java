/*******************************************************************************
 * Copyright (c) 2026 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package de.marw.fifteenknots.model;

/**
 * Converts a speed value from the NMEA input (in knots, i.e nautical miles per hour) into a different unit.
 *
 * @author Martin Weber
 */
public interface SpeedUnitConverter {

  /**
   * Gets the name of the unit the speed value is converted to.
   *
   * @return name of the unit e.g. {@code km/h}
   */
  public String getUnitName();

  /**
   * Converts the specified speed value.
   *
   * @param speed
   *        the value from the NMEA input (in knots, i.e nautical miles per hour)
   * @return the speed value converted to the target unit
   */
  public float convert(float speed);
}
