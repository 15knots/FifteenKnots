// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.render;

import java.util.Date;
import java.util.List;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;


/**
 * Converts a {@code long} value into a Date object for use by Freemarker.
 * 
 * @author Martin Weber
 */
public class MillisToDateMethod implements TemplateMethodModelEx
{

  /*-
   * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
   */
  @SuppressWarnings("unchecked")
  public Date exec( List arguments) throws TemplateModelException
  {
    final Object arg= arguments.get( 0);
    if ( !(arg instanceof SimpleNumber)) {
      throw new TemplateModelException(
        "Parameter to must be a long integer.");
    }
    return new Date( ((SimpleNumber) arg).getAsNumber().longValue());
  }
}
