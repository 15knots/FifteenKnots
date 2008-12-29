// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.main;

/**
 * Types of output that can be produced by the program.
 *
 * @author Martin Weber
 */
enum OutputType implements ParsableTypeOption
{
  /** Produce a file in KML format with boat speed encoded as the track's color. */
  KML {

    public boolean matchesOption( String arg)
    {
      return "kml".equals( arg);
    }

    @Override
    public CmdlineKit createCmdlineKit()
    {
      return new KMLCmdlineKit();
    }

    public String getDescription()
    {
      return "kml:\tKML format with boat speed encoded as the track's color";
    }

    public String getUsage()
    {
      return "kml [-c <num>] [-o <file>]" + "\n  Type options:"
        + "\n\t-c -colors <num>:\tnumber of colors to encode speed (default 120)"
        + "\n\t-o -output <file>:\toutput file name (default stdout)";
    }
  },
  /**
   * Produce a file in the flash format with boat animation.
   */
  FLASH {

    public boolean matchesOption( String arg)
    {
      return "flash".equals( arg);
    }

    @Override
    public CmdlineKit createCmdlineKit()
    {
      return new SWFCmdlineKit();
    }

    public String getDescription()
    {
      return "flash:\tflash format with boat animation";
    }

    public String getUsage()
    {
      return "flash -o <dir>" + "\n  Type options:"
        + "\n\t-o -output <dir>:\toutput directory name";
    }
  };

  public CmdlineKit createCmdlineKit()
  {
     throw new UnsupportedOperationException("Not implemented: createCmdlineKit()");
    // TODO Auto-generated method stub since 27.12.2008
  }

}
