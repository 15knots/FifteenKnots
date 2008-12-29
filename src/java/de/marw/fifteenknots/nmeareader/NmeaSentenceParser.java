// $Id$
/*
 * Copyright 2008 by Martin Weber
 */

package de.marw.fifteenknots.nmeareader;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.swing.event.EventListenerList;


/**
 * A parser for NMEA 0183 sentences that sends events when certain data are
 * detected. The parser is able to decode the following NMEA sentence types:
 * GPRMC, GPGSA and GPGSV.
 * 
 * @author Martin Weber
 */
public class NmeaSentenceParser
{

  /**
   * the source used in the events to send.
   */
  private Object eventSource;

  private static final TimeZone UTC_TIME_ZONE= TimeZone.getTimeZone( "UTC");

  /**
   * last date in milliseconds detected
   */
  private long lastDate= -1;

  /**
   * last time in milliseconds of day deteced
   */
  private long lastTimeOfDay= -1;

  // listeners
  private EventListenerList listenerList= new EventListenerList();

  private Boolean lastFix;

  private Position2D lastPosition;

  /**
   * last speed in knots detected. Float.MAX_VALUE is used to indicate 'no value
   * yet', that is safe since that is faster than the speed of light.
   */
  private float lastSpeed= Float.MAX_VALUE;

  private float lastBearing= Float.MAX_VALUE;

  private final Calendar cal= new GregorianCalendar( UTC_TIME_ZONE);

  // private PositionReceivedListener PositionReceived;
  //
  // private DateTimeChangedListener DateTimeChanged;
  //
  // private BearingReceivedListener BearingReceived;
  //
  // private SpeedReceivedListener SpeedReceived;
  //
  // private FixObtainedListener FixObtained;
  //
  // private FixLostListener FixLost;
  //
  // private SatelliteReceivedListener SatelliteReceived;
  //
  // private HDOPReceivedListener HDOPReceived;
  //
  // private VDOPReceivedListener VDOPReceived;
  //
  // private PDOPReceivedListener PDOPReceived;

  /**
   * Contruct a new instance that uses the specified object as the source of the
   * events to send.
   */
  public NmeaSentenceParser( Object source)
  {
    this.eventSource= source;
  }

  /**
   * Processes information from the GPS receiver
   * 
   * @param sentence
   * @return <code>true</code> if the sentence was recognized, otherwise
   *         <code>false</code>.
   */
  public boolean parse( String sentence)
  {
    // Discard the sentence if its checksum does not match our
    // calculated checksum
    if ( !isValid( sentence))
      return false;
    // Look at the first word to decide where to go next
    final String[] words= sentence.split( ",|\\*");
    final String type= words[0];

    if ("$GPGGA".equals( type)) {
      // A "essential fix data" sentence was received
      return parseGPGGA( words);
    }
    else if ("$GPRMC".equals( type)) {
      // A "Recommended Minimum" sentence was found
      return parseGPRMC( words);
    }
    // else if ("$GPGSV".equals( type)) {
    // // A "Satellites in View" sentence was received
    // return parseGPGSV( words);
    // }
    else if ("$GPGSA".equals( type)) {
      // "Overall Satellite data"
      return parseGPGSA( words);
    }
    else {
      // Indicate that the sentence was not recognized
      return false;
    }
  }

  /**
   * Interprets a $GPRMC message
   * 
   * @param sentence
   *        the NMEA line divided into words
   * @return <code>true</code> if the sentence was recognized, otherwise
   *         <code>false</code>.
   */
  private boolean parseGPRMC( String[] sentence)
  {
    final String[] words= sentence;
    // time of day
    if (words.length >= 1 && words[1].length() >= 6) {
      long time= parseTimeOfDay( words[1]);
      // notify listener
      fireTimeChanged( time);
    }
    // Do we have enough values to parse satellite-derived date?
    if (words.length >= 9 && words[9].length() >= 6) {
      int day= Integer.valueOf( words[9].substring( 0, 2));
      int month= Integer.valueOf( words[9].substring( 2, 4));
      int year= Integer.valueOf( words[9].substring( 4, 6));
      cal.clear();
      cal.set( Calendar.DAY_OF_MONTH, day);
      cal.set( Calendar.MONTH, month - 1);
      cal.set( Calendar.YEAR, year + 2000);
      long date= cal.getTime().getTime();
      // notify listener
      fireDateChanged( date);
    }

    // Does the device currently have a satellite fix?
    if (words.length >= 2 && words[2].length() > 0) {
      Boolean fix= null;
      if ("A".equals( words[2])) {
        // got fix
        fix= Boolean.TRUE;
      }
      else if ("V".equals( words[2])) {
        fix= Boolean.FALSE;
      }
      else {
        // garbled value, should we complain here??
      }

      fireFixChanged( fix);
    }

    if (words.length >= 6 && words[3].length() > 0 && words[4].length() > 0
      && words[5].length() > 0 && words[6].length() > 0) {
      // Extract latitude and longitude
      double latitude= parseLatitude( words[3], words[4]);
      double longitude= parseLongitude( words[5], words[6]);
      Position2D pos= new Position2D( latitude, longitude);
      // notify listener
      firePositionChanged( pos);
    }
    // Do we have enough information to extract the current speed?
    if (words.length >= 7 && words[7].length() > 0) {
      // Yes. Parse the speed (knots)
      float speed= Float.valueOf( words[7]);
      // notify listener
      fireSpeedChanged( speed);
    }
    // Do we have enough information to extract bearing?
    if (words.length >= 8 && words[8].length() > 0) {
      // Indicate that the sentence was recognized
      float bearing= Float.valueOf( words[8]);
      // notify listener
      fireBearingChanged( bearing);
    }
    // Indicate that the sentence was recognized
    return true;
  }

  // Interprets a "essential fix data" NMEA sentence
  private boolean parseGPGGA( String[] sentence)
  {
    final String[] words= sentence;
    // Do we have enough values to parse satellite-derived time?
    if (words.length >= 1 && words[1].length() >= 6) {
      long time= parseTimeOfDay( words[1]);
      // notify listener
      fireTimeChanged( time);
    }
    // Does the device currently have a satellite fix?
    if (words.length >= 7 && words[6].length() > 0) {
      Boolean fix= null;
      if ("1".equals( words[6])) {
        // got fix
        fix= Boolean.TRUE;
      }
      else if ("2".equals( words[6])) {
        // got fix
        fix= Boolean.TRUE;
      }
      else {
        fix= Boolean.FALSE;
      }
      fireFixChanged( fix);
    }

    if (words.length >= 6 && words[2].length() > 0 && words[3].length() > 0
      && words[4].length() > 0 && words[5].length() > 0) {
      // Extract latitude and longitude
      double latitude= parseLatitude( words[2], words[3]);
      double longitude= parseLongitude( words[4], words[5]);
      Position2D pos= new Position2D( latitude, longitude);
      // notify listener
      firePositionChanged( pos);
    }

    return true;
  }

  // Interprets a "Satellites in View" NMEA sentence
  private boolean parseGPGSV( String[] sentence)
  {
    final String[] words= sentence;
    // Each sentence contains four blocks of satellite information.
    // Read each block and report each satellite's information
    for (int sat= 0; sat < 4; sat++) {
      // Does the sentence have enough words to analyze?
      final int offset= 4 + sat * 4;
      if (words.length > offset + 3) {
        // Yes. Proceed with analyzing the block.
        // Does it contain any information?
        if (words[offset].length() > 0 && words[offset + 1].length() > 0
          && words[offset + 2].length() > 0 && words[offset + 3].length() > 0) {
          // Yes. Extract satellite information and report it
          int pseudoRandomCode= 0;
          int azimuth= 0;
          int elevation= 0;
          int signalToNoiseRatio= 0;
          pseudoRandomCode= Integer.valueOf( words[offset]);
          elevation= Integer.valueOf( words[offset + 1]);
          azimuth= Integer.valueOf( words[offset + 2]);
          signalToNoiseRatio= Integer.valueOf( words[offset + 3]);
          // Notify of this satellite's information
          // if (SatelliteReceived != null)
          // SatelliteReceived( pseudoRandomCode, azimuth, elevation,
          // signalToNoiseRatio);
        }
      }
    }
    // Indicate that the sentence was recognized
    return true;
  }

  // Interprets a "Fixed Satellites and DOP" NMEA sentence
  private boolean parseGPGSA( String[] sentence)
  {
    final String[] words= sentence;
    // Update the DOP values
    if (words.length >= 15 && words[15].length() > 0) {
      // if (PDOPReceived != null)
      // PDOPReceived( Double.valueOf( words[15]));
    }
    if (words.length >= 16 && words[16].length() > 0) {
      // if (HDOPReceived != null)
      // HDOPReceived( Double.valueOf( words[16]));
    }
    if (words.length >= 17 && words[17].length() > 0) {
      // if (VDOPReceived != null)
      // VDOPReceived( Double.valueOf( words[17]));
    }
    return true;
  }

  /**
   * @param degrees
   * @param hemisphere
   * @return
   */
  private double parseLongitude( final String degrees, final String hemisphere)
  {
    // hours
    double longitude= Integer.valueOf( degrees.substring( 0, 3));
    // minutes
    longitude+= Double.valueOf( degrees.substring( 3)) / 60.0;
    // hemisphere
    if ("W".equals( hemisphere)) {
      longitude*= -1.0;
    }
    return longitude;
  }

  /**
   * @param degrees
   * @param hemisphere
   * @return
   */
  private double parseLatitude( final String degrees, final String hemisphere)
  {
    // hours
    double latitude= Integer.valueOf( degrees.substring( 0, 2));
    // minutes
    latitude+= (Double.valueOf( degrees.substring( 2)) / 60.0);
    // hemisphere
    if ("S".equals( hemisphere)) {
      latitude*= -1.0;
    }
    return latitude;
  }

  /**
   * @param word
   * @return
   */
  private long parseTimeOfDay( final String word)
  {
    // Extract hours, minutes, seconds and milliseconds
    int hours= Integer.valueOf( word.substring( 0, 2));
    int minutes= Integer.valueOf( word.substring( 2, 4));
    int seconds= Integer.valueOf( word.substring( 4, 6));
    // Extract milliseconds if it is available
    int milliseconds= 0;
    if (word.length() > 7) {
      milliseconds= (int) (Float.valueOf( word.substring( 6)) * 1000F);
    }
    cal.clear();
    cal.set( Calendar.HOUR_OF_DAY, hours);
    cal.set( Calendar.MINUTE, minutes);
    cal.set( Calendar.SECOND, seconds);
    cal.set( Calendar.MILLISECOND, milliseconds);
    long time= cal.getTime().getTime();
    return time;
  }

  // Returns True if a sentence's checksum matches the
  // calculated checksum
  public boolean isValid( String sentence)
  {
    // Compare the characters after the asterisk to the calculation
    final int index= sentence.indexOf( "*");
    if (index == -1)
      return false; // no '*'
    return sentence.substring( index + 1, index + 3).equalsIgnoreCase(
      getChecksum( sentence));
  }

  /**
   * Calculates the checksum for a sentence
   * 
   * @param sentence
   * @return the checksum as a String containing two hexadecimal characters
   */
  public String getChecksum( String sentence)
  {
    // Loop through all chars to get a checksum
    byte checksum= 0;
    for (int i= 0; i < sentence.length(); i++) {
      char character= sentence.charAt( i);
      if (character == '$') {
        // Ignore the dollar sign
      }
      else if (character == '*') {
        // Stop processing before the asterisk
        break;
      }
      else {
        // XOR the checksum with this character's value
        checksum^= (byte) character;
      }
    }
    // Return the checksum formatted as a two-character hexadecimal
    return Integer.toString( checksum, 16);
  }

  public synchronized void addListener( INmeaSentenceListener listener)
  {
    listenerList.add( INmeaSentenceListener.class, listener);
  }

  public synchronized void removeListener( INmeaSentenceListener listener)
  {
    listenerList.remove( INmeaSentenceListener.class, listener);
  }

  /**
   * Notifies all listeners that have registered interest for notification on
   * this event type.
   * 
   * @param bearing
   */
  private void fireBearingChanged( float bearing)
  {
    if (Float.compare( lastBearing, bearing) != 0) {
      lastBearing= bearing;
      // Guaranteed to return a non-null array
      Object[] listeners= listenerList.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i= listeners.length - 2; i >= 0; i-= 2) {
        if (listeners[i] == INmeaSentenceListener.class) {
          ((INmeaSentenceListener) listeners[i + 1]).bearingChanged(
            eventSource, bearing);
        }
      }
    }
  }

  /**
   * Notifies all listeners that have registered interest for notification on
   * this event type.
   * 
   * @param speed
   */
  private void fireSpeedChanged( float speed)
  {
    if (Float.compare( lastSpeed, speed) != 0) {
      lastSpeed= speed;
      // Guaranteed to return a non-null array
      Object[] listeners= listenerList.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i= listeners.length - 2; i >= 0; i-= 2) {
        if (listeners[i] == INmeaSentenceListener.class) {
          ((INmeaSentenceListener) listeners[i + 1]).speedChanged( eventSource,
            speed);
        }
      }
    }
  }

  /**
   * Notifies all listeners that have registered interest for notification on
   * this event type.
   * 
   * @param pos
   */
  private void firePositionChanged( Position2D pos)
  {
    if ( !pos.equals( lastPosition)) {
      lastPosition= pos;
      // Guaranteed to return a non-null array
      Object[] listeners= listenerList.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i= listeners.length - 2; i >= 0; i-= 2) {
        if (listeners[i] == INmeaSentenceListener.class) {
          ((INmeaSentenceListener) listeners[i + 1]).positionChanged(
            eventSource, pos);
        }
      }
    }
  }

  /**
   * Notifies all listeners that have registered interest for notification on
   * this event type.
   * 
   * @param fix
   */
  private void fireFixChanged( Boolean fix)
  {
    if (fix != null && !fix.equals( lastFix)) {
      // notify listener
      lastFix= fix;
      // Guaranteed to return a non-null array
      Object[] listeners= listenerList.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i= listeners.length - 2; i >= 0; i-= 2) {
        if (listeners[i] == INmeaSentenceListener.class) {
          ((INmeaSentenceListener) listeners[i + 1]).fixChanged( eventSource,
            fix);
        }
      }

    }
  }

  /**
   * Notifies all listeners that have registered interest for notification on
   * this event type.
   * 
   * @param time
   */
  private void fireTimeChanged( long time)
  {
    if (time != lastTimeOfDay) {
      lastTimeOfDay= time;
      // Guaranteed to return a non-null array
      Object[] listeners= listenerList.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i= listeners.length - 2; i >= 0; i-= 2) {
        if (listeners[i] == INmeaSentenceListener.class) {
          ((INmeaSentenceListener) listeners[i + 1]).timeChanged( eventSource,
            time);
        }
      }
    }
  }

  /**
   * Notifies all listeners that have registered interest for notification on
   * this event type.
   * 
   * @param date
   */
  private void fireDateChanged( long date)
  {
    if (date != lastDate) {
      lastDate= date;
      lastTimeOfDay= -1; // invalidate to get event fired
      // Guaranteed to return a non-null array
      Object[] listeners= listenerList.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i= listeners.length - 2; i >= 0; i-= 2) {
        if (listeners[i] == INmeaSentenceListener.class) {
          ((INmeaSentenceListener) listeners[i + 1]).dateChanged( eventSource,
            date);
        }
      }
    }
  }
}
