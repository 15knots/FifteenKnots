// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.render;

import java.awt.Color;
import java.util.List;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;


/**
 * Converts a {@link Color#getRGB() ARBG value} into a Aplpha,Blue,Green,Red
 * color value as a hexadecimal String for use by Freemarker.
 * 
 * @author Martin Weber
 */
public class ARGBToABRGMethod implements TemplateMethodModelEx
{

  /*-
   * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
   */
  @SuppressWarnings("unchecked")
  public String exec( List arguments) throws TemplateModelException
  {
    final Object arg= arguments.get( 0);
    if ( !(arg instanceof SimpleNumber)) {
      throw new TemplateModelException( "Parameter to must be a integer.");
    }
    final int value= ((SimpleNumber) arg).getAsNumber().intValue();

    int abgr=
      (((value >> 24) & 0xFF) << 24) | (((value >> 16) & 0xFF) << 0)
        | (((value >> 8) & 0xFF) << 8) | ((value & 0xFF) << 16);
    return Integer.toHexString( abgr);
  }
}
