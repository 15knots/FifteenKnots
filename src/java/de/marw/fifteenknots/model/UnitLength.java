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
 * Represents the length of a cruise along with it length unit.
 *
 * @author Martin Weber
 */
public class UnitLength {
  private final double length;
  private final String unit;

  /**
   * @param length
   *        the length of a cruise, measured in the unit given
   * @param unit
   *        the measurement unit of the length
   */
  public UnitLength(double length, String unit) {
    this.length = length;
    this.unit = unit;
  }

  /**
   * Gets the length, measured in the unit given.
   *
   * @see #getUnit
   */
  public double getLength() {
    return length;
  }

  /**
   * Gets the measurement unit of the length as a String.
   */
  public String getUnit() {
    return unit;
  }
}
