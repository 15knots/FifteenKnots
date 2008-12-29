// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.engine;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import de.marw.fifteenknots.model.SpeedEncoding;
import de.marw.fifteenknots.model.SpeedRange;


/**
 * Provides colors for boat speed values.
 *
 * @author Martin Weber
 */
public class SpeedColorEncoder {

  private final float[] speeds;

  private final Color[] colors;

  /** speed ranges, lazily initialized */
  private volatile SpeedEncoding speedEncoding;

  /**
   * @param colorCount
   *        the number of colors available for encoding.
   * @param speedMin
   *        the minimum speed that should be encoded.
   * @param speedMax
   *        the maximum speed that should be encoded.
   */
  public SpeedColorEncoder( int colorCount, float speedMin, float speedMax) {
    speeds= new float[colorCount];
    colors= new Color[speeds.length];

    // populate encoding tables...
    // 128 stufen für menschliches Auge unterscheidbar.
    // Gegeben sei ein Algorithmus, der ein Grauwertbild liefert . Die
    // Intensitäten liegen zwischen 0 und 2^24-1. Wie würden sie das Bild
    // farblich kodieren, so dass der angegebene Intensitätsbereich farblich
    // abgebildet wird? Dabei sollen hohe Intensitätswerte warmen (roten) Farben
    // und niedrige kalten (blauen) Farben entsprechen.
    // Hierzu kann eine Konvertierung in das HSV-System verwendet werden,
    // wobei der Wertebereich auf H (Hue) abgebildet wird

    final float stepSize= (speedMax - speedMin) / (colorCount - 1);
    final float hueSize= 240 / colorCount;
    // compute overall speed levels...
    for (int i= 0; i < speeds.length; i++) {
      speeds[i]= speedMin + stepSize * i;
      final float[] rgb= convertHSVtoRGB( 240 - hueSize * i, 1.0f, 1.0f);
      colors[i]= new Color( rgb[0], rgb[1], rgb[2], 1.0f);
    }
  }

  /**
   * Gets the color that represents the specified speed.
   *
   * @param speed
   *        the speed greater or equal than zero.
   * @return the speed, encoded as a Color object.
   */
  public Color encodeSpeed( Float speed) {
    int idx= getEncodedColorIndex( speed);
    return colors[idx];
  }

  /**
   * Gets an index for the color that represents the specified speed.
   *
   * @param speed
   *        the speed greater or equal than zero.
   * @return the index that identifies the color encoded speed. The returned
   *         value will be greater or equal than zero and less than the number
   *         of colors provided in the constructor.
   */
  public int getEncodedColorIndex( Float speed) {
    int idx= Arrays.binarySearch( speeds, speed);
    if (idx >= 0) {
      if (idx >= speeds.length) {
	// above highest speed;
	idx= speeds.length - 1;
      }
    }
    else { // not in table
      idx= -idx - 2;
    }
    return idx;
  }

  /**
   * Gets the color encoded speed ranges.
   */
  public SpeedEncoding getSpeedEncoding() {
    if (speedEncoding == null) { // variable ref initialization is atomic
      synchronized (this ) {
	if (speedEncoding == null) {
	  speedEncoding= createSpeedEncoding();
	}
      }
    }
    return speedEncoding;
  }

  /**
   */
  private SpeedEncoding createSpeedEncoding() {
    ArrayList<SpeedRange> spList= new ArrayList<SpeedRange>( speeds.length);
    for (int i= 0; i < speeds.length; i++) {
      SpeedRange es= new SpeedRange( speeds[i], i + 1 < speeds.length
	? speeds[i + 1] : speeds[i], colors[i]);
      spList.add( es);
    }
    spList.trimToSize();
    return new SpeedEncodingImpl( spList);
  }

  /**
   * Change an HSV color to RGB color. We don't bother converting the alpha as
   * that stays the same regardless of color space.
   *<p>
   * The RGB<->HSV color space conversions have been taken from Foley & van Dam
   * <i>Computer Graphics Principles and Practice, 2nd Edition</i>, Addison
   * Wesley, 1990.
   * <p/>
   *
   * @param h
   *        The Hue component of the color [0..369], relative to the Red axis
   *        with red at angle 0, green at 2Pi/3, blue at 4Pi/3 and red again at
   *        2Pi.
   * @param s
   *        The Saturation component of the color. Saturation is the depth or
   *        purity of the color and is measured as a radial distance from the
   *        central axis with value between 0 at the center to 1 at the outer
   *        surface.
   * @param v
   *        The v component of the color
   * @return An array to return the RGB colour values in
   */
  public static float[] convertHSVtoRGB( float h, float s, float v) {

    float r= 0;
    float g= 0;
    float b= 0;

    if (s == 0) {
      // this color in on the black white center line <=> h = UNDEFINED
      if (Float.isNaN( h)) {
	// Achromatic color, there is no hue
	r= v;
	g= v;
	b= v;
      }
      else {
	throw new IllegalArgumentException(
	  "Invalid h (it has a value) value when s is zero");
      }
    }
    else {
      h%= 360; // 360 is equiv to 0

      // h is now in [0,6)
      h= h / 60;

      int i= (int) h;
      float f= h - i; // f is fractional part of h
      float p= v * (1 - s);
      float q= v * (1 - (s * f));
      float t= v * (1 - (s * (1 - f)));

      switch (i) {
	case 0:
	  r= v;
	  g= t;
	  b= p;
	break;

	case 1:
	  r= q;
	  g= v;
	  b= p;
	break;

	case 2:
	  r= p;
	  g= v;
	  b= t;
	break;

	case 3:
	  r= p;
	  g= q;
	  b= v;
	break;

	case 4:
	  r= t;
	  g= p;
	  b= v;
	break;

	case 5:
	  r= v;
	  g= p;
	  b= q;
	break;
      }
    }

    // now assign everything....
    return new float[] { r, g, b };
  }
}
