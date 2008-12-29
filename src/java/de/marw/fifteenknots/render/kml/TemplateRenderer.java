// $Id$
/*
 * Copyright 2008 by Martin Weber
 */

package de.marw.fifteenknots.render.kml;

import java.io.IOException;
import java.io.Writer;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;


/**
 * @author weber
 */
public class TemplateRenderer
{
  private Configuration cfg;
  private Writer output;
  private String templateName;
  /**
   * @param output
   *        TODO
   */
  public TemplateRenderer( Writer output, String templateName)
  {
    if (output == null) {
      throw new NullPointerException( "output");
    }
    this.output= output;
    if( templateName == null) {
      throw new NullPointerException("templateName");
    }
    this.templateName= templateName;
    
    /* ------------------------------------------------------------------- */
    /* You usually do it only once in the whole application life-cycle: */

    /* Create and adjust the configuration */
    cfg= new Configuration();
    cfg.setClassForTemplateLoading( getClass(), "templates");
    // cfg.setDirectoryForTemplateLoading(
    // new File("/where/you/store/templates"));
    cfg.setObjectWrapper( new DefaultObjectWrapper());

    /* ------------------------------------------------------------------- */
    /* You usually do these for many times in the application life-cycle: */

    /* Get or create a template */
//    process( output, createDefaultDataModel());
  }

  /**
   * @param dataModel
   * @throws IOException
   * @throws TemplateException
   */
  public void process( Object dataModel)
    throws IOException, TemplateException
  {
    Template temp= cfg.getTemplate( templateName);
    temp.process( dataModel, output);
    output.flush();
  }

}
