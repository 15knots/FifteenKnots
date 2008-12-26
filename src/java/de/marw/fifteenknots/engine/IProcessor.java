// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.engine;

import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * @author Martin Weber
 */
public interface IProcessor
{

  /**
   * @throws FileNotFoundException
   *         if one of the specified files cannot be found
   * @throws IOException
   *         If an I/O error occurs
   */
  void process() throws FileNotFoundException, IOException;

}
