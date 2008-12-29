// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.engine;

/**
 * Abstract base implementation of a {@code RaceModelBuilder} that holds the
 * model factory.
 *
 * @author Martin Weber
 */
public abstract class AbstractRaceModelBuilder implements RaceModelBuilder {

  /** the factory for objects of the race model. */
  private RaceModelFactory modelFactory;

  /**
   * Gets the factory for objects of the race model.
   *
   * @return the current model factory or {@code null}, if none has been set.
   */
  public final RaceModelFactory getModelFactory() {
    return this.modelFactory;
  }

  /**
   * Sets the model factory used to create race model objects.
   */
  public final void setModelFactory( RaceModelFactory modelFactory) {
    this.modelFactory= modelFactory;
  }

}
