// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.engine;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.marw.fifteenknots.model.RaceModel;


/**
 * Defines the requirements of an object that builds a race model.
 *
 * @author Martin Weber
 */
public interface RaceModelBuilder {

  /**
   * Builds a new race model.
   *
   * @throws FileNotFoundException
   *         if one of the specified files cannot be found
   * @throws IOException
   *         If an I/O error occurs
   */
  public RaceModel buildModel() throws FileNotFoundException, IOException;
}
