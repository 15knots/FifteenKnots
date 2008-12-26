// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Provides a shared instance of an {@link ExecutorService}.
 * 
 * @author Martin Weber
 */
public class ThreadPoolExecutorService
{

  /** singleton instance */
  private static ThreadPoolExecutorService instance;

  private ExecutorService es= Executors.newCachedThreadPool();

  /**
   * Singleton constructor
   */
  private ThreadPoolExecutorService()
  {}

  /**
   * Gets the shared instance of an {@link ExecutorService}.
   */
  public static ExecutorService getService()
  {
    return getInstance().es;
  }

  /**
   * Gets or creates the singleton instance.
   */
  private static synchronized ThreadPoolExecutorService getInstance()
  {
    if (instance == null) {
      instance= new ThreadPoolExecutorService();
    }
    return instance;
  }
}
