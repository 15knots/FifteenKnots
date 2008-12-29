// $Id$
/*
 * Copyright 2008 by Martin Weber
 */

package de.marw.fifteenknots.nmeareader;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.event.EventListenerList;


/**
 * A parser for NMEA 0183 data that sends events when a new timestamp is
 * detected.
 * 
 * @author Martin Weber
 * @see NmeaSentenceParser
 */
public class NmeaParser
{
  private NmeaSentenceParser nmeaSentenceParser;

  private InputStream inputStream;

  private TrackEventMulticaster trackEventMulticaster;

  /**
   * @param inputStream
   *        the stream to parse as NMEA data.
   * @param source
   *        the source used in the events to send.
   * @throws NullPointerException
   *         if stream or source is <code>null</code>.
   */
  public NmeaParser( InputStream inputStream, Object source)
  {
    if (inputStream == null)
      throw new NullPointerException( "inputStream");
    this.inputStream= inputStream;
    nmeaSentenceParser= new NmeaSentenceParser( source);
    this.trackEventMulticaster= new TrackEventMulticaster( source);
    nmeaSentenceParser.addListener( trackEventMulticaster);
  }

  /**
   * Adds a track listener.
   * 
   * @param listener
   */
  public void addTrackListener( ITrackListener listener)
  {
    trackEventMulticaster.addTrackListener( listener);
  }

  /**
   * removes a track listener.
   * 
   * @param listener
   */
  public void removeTrackListener( ITrackListener listener)
  {
    trackEventMulticaster.removeTrackListener( listener);
  }

  /**
   * Parses the characters from the input stream and sends events when certain
   * data are detected.
   * 
   * @throws IOException
   *         If an I/O error occurs
   * @see NmeaSentenceParser#parse(String)
   */
  public void parse() throws IOException
  {
    BufferedReader reader= null;
    try {
      reader= new BufferedReader( new InputStreamReader( inputStream));
      String line;
      while ((line= reader.readLine()) != null) {
        nmeaSentenceParser.parse( line);
      }
    }
    finally {
      trackEventMulticaster.close();
      if (reader != null) {
        reader.close();
      }
    }
  }

  /**
   * @param args
   */
  public static void main( String[] args)
  {
    if (args.length < 1) {
      System.err.println( "Usage:\n" + "file");
      System.exit( 1);
    }
    try {
      FileInputStream stream= new FileInputStream( args[0]);
      final NmeaParser parser= new NmeaParser( stream, new Object());
      parser.addTrackListener( new ITrackListener() {

        public void trackPoint( TrackEvent evt)
        {
          System.out.println( evt.toString());
        }
      });
      parser.parse();
    }
    catch (FileNotFoundException ex) {
      System.err.println( ex.getMessage());
      System.exit( 1);
    }
    catch (IOException ex) {
      System.err.println( ex.getMessage());
      System.exit( 3);
    }
    System.exit( 0);

  }

  // //////////////////////////////////////////////////////////////////
  // inner classes
  // //////////////////////////////////////////////////////////////////

  /**
   * Condenses events from NMEA sentences to {@code TrackEvent}s and sends the
   * to registered listeners. Listens for events from NMEA sentences.
   * 
   * @author Martin Weber
   * @see NmeaSentenceParser
   */
  private static class TrackEventMulticaster implements INmeaSentenceListener,
    Closeable
  {
    /**
     * the source used in the events to send.
     */
    private Object eventSource;

    private EventListenerList listenerList= new EventListenerList();

    /** data buffer fields */
    private long timeOfDay;

    private long date;

    private Position2D pos;

    private Float speed;

    private Float bearing;

    /**
     * Contruct a new instance that uses the specified object as the source of
     * the events to send.
     * 
     * @throws NullPointerException
     *         if source is <code>null</code>.
     */
    public TrackEventMulticaster( Object source)
    {
      if (source == null)
        throw new NullPointerException( "source");
      this.eventSource= source;
    }

    /**
     * @see java.io.Closeable#close()
     */
    public void close() throws IOException
    {
      flush();
    }

    /**
     * Sends the latest event, if any.
     */
    private void flush()
    {
      if (pos != null) {
        TrackEvent evt=
          new TrackEvent( eventSource, date + timeOfDay, pos, speed, bearing);
        fireEvent( evt);
        // clear buffered data
        pos= null;
        speed= null;
        bearing= null;
      }
    }

    /**
     * @see de.marw.fifteenknots.nmeareader.INmeaSentenceListener#timeChanged(java.lang.Object,
     *      long)
     */
    public void timeChanged( Object source, long timeOfDay)
    {
      flush();
      this.timeOfDay= timeOfDay;
    }

    /*
     * @see
     * de.marw.fifteenknots.nmeareader.INmeaSentenceListener#dateChanged(java
     * .lang.Object, long)
     */
    public void dateChanged( Object source, long date)
    {
      this.date= date;
    }

    /**
     * @see de.marw.fifteenknots.nmeareader.INmeaSentenceListener#fixChanged(java.lang.Object,
     *      java.lang.Boolean)
     */
    public void fixChanged( Object source, Boolean fix)
    {}

    /**
     * @see de.marw.fifteenknots.nmeareader.INmeaSentenceListener#positionChanged(java.lang.Object,
     *      de.marw.fifteenknots.nmeareader.Position2D)
     */
    public void positionChanged( Object source, Position2D pos)
    {
      this.pos= pos;
    }

    /**
     * @see de.marw.fifteenknots.nmeareader.INmeaSentenceListener#speedChanged(java.lang.Object,
     *      float)
     */
    public void speedChanged( Object source, float speed)
    {
      this.speed= Float.valueOf( speed);
    }

    /**
     * @see de.marw.fifteenknots.nmeareader.INmeaSentenceListener#bearingChanged(java.lang.Object,
     *      float)
     */
    public void bearingChanged( Object source, float bearing)
    {
      this.bearing= Float.valueOf( bearing);
    }

    /**
     * Adds a track listener.
     * 
     * @param listener
     */
    public void addTrackListener( ITrackListener listener)
    {
      listenerList.add( ITrackListener.class, listener);
    }

    /**
     * removes a track listener.
     * 
     * @param listener
     */
    public void removeTrackListener( ITrackListener listener)
    {
      listenerList.remove( ITrackListener.class, listener);
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type.
     */
    protected void fireEvent( TrackEvent evt)
    {
      // Guaranteed to return a non-null array
      Object[] listeners= listenerList.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i= listeners.length - 2; i >= 0; i-= 2) {
        if (listeners[i] == ITrackListener.class) {
          ((ITrackListener) listeners[i + 1]).trackPoint( evt);
        }
      }
    }
  }
}
