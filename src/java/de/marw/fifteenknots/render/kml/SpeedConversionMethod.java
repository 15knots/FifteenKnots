/*******************************************************************************
 * Copyright (c) 2026 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package de.marw.fifteenknots.render.kml;

import java.util.List;
import java.util.Objects;

import de.marw.fifteenknots.model.SpeedUnitConverter;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * Converts a speed value for use by Freemarker.

 * @author Martin Weber
 */
public class SpeedConversionMethod implements TemplateMethodModelEx {
  private final SpeedUnitConverter speedUnitConverter;

  /**
   *
   */
  public SpeedConversionMethod(SpeedUnitConverter speedUnitConverter) {
    this.speedUnitConverter = Objects.requireNonNull(speedUnitConverter);
  }

  @Override
  public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
    final Object arg = arguments.get(0);
    if (!(arg instanceof SimpleNumber)) {
      throw new TemplateModelException("Parameter to must be a float.");
    }

    return Float.valueOf(speedUnitConverter.convert(((SimpleNumber) arg).getAsNumber().floatValue()));
  }
}
