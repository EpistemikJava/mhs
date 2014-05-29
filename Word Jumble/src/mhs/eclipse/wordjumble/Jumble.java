/* **************************************************************************************
 
   Mark Sattolo (epistemik@gmail.com)
 -----------------------------------------------
  mhs.eclipse.wordjumble.Jumble.java
  Created March 30, 2014
  original was mhs.eclipse.wordjumble.WordJumble.java
  
*************************************************************************************** */

package mhs.eclipse.wordjumble;

import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to implement a recursive word jumbling algorithm<br>
 *  - initial work imported from remote
 *  
 * @author mhsatto
 * @version 1.0
 */
public class Jumble
{
  /** Logger for the class */
  static Logger jumbleLogger ;
  
  /** Overall Level to log at */
  static Level jumbleLevel = Level.INFO ;
  
  /**
   * MAIN
   * 
   * @param args - from command line
   */
  public static void main( String[] args )
  {
    Jumble.setLogger();
    
    String letters ;

    // process the command line and get properly formatted user input
    letters = setup( args );

    // do the jumble
    Jumble j1 = new Jumble();
    j1.go( letters );
    
    jumbleLogger.info( "*** PROGRAM ENDED ***" );
    
  }// main()
  
  /**
   *  initialize the logger
   */
  private static void setLogger()
  {
    jumbleLogger = Logger.getLogger( Jumble.class.getName() );
    
    // NEED to set the root ("") Logger's handler
    Logger.getLogger("").getHandlers()[0].setLevel( jumbleLevel );
    // AND the jumble logger itself to Level.ALL to get ALL messages to the Console
    jumbleLogger.setLevel( jumbleLevel );
    
    jumbleLogger.log( jumbleLevel, "Logging Level is " + jumbleLevel.toString() );
    
    jumbleLogger.fine( "Set up Logger" );
    
  }// setLogger()
  
  /**
   * Process the command line then get user input if necessary
   * 
   * @param ar_str - command line args
   */
  private static String setup( final String[] ar_str )
  {
    jumbleLogger.entering( Jumble.class.getName(), "setup()" );
    
    byte[] enter ;
    String entry, result ;
    
    if( ar_str.length < 1 ) // need to ask the user to enter some letters
    {
      enter = new byte[1024];
      System.out.print( "Please enter a string to jumble: ");
      try
      {
        System.in.read( enter );
      }
      catch( IOException ie )
      {
        ie.printStackTrace();
        System.exit( 11 );
      }
      
      entry = new String( enter );
    }
    else // some letters were entered on the command line
    {
      entry = new String( ar_str[0] );
    }
    
    // check input for alphabetic only
    result = checkAlphabetic( entry );
    
    // make sure there are still some letters
    if( result.length() <= 0 )
    {
      System.out.println( "You must enter ONLY alphabetic characters to jumble." );
      System.exit( 12 );
    }
    
    jumbleLogger.exiting( Jumble.class.getName(), "setup()" );
    
    return result.trim();
    
  }// setup()
  
  /**
   * @param toCheck - to check
   */
  private static String checkAlphabetic( String toCheck )
  {
    jumbleLogger.entering( Jumble.class.getName(), "checkAlphabetic()" );
    
    StringBuilder alpha = new StringBuilder();
    
    char[] mychars = toCheck.toCharArray();
    
    for( char c: mychars )
    {
      if( Character.isLetter(c) )
      {
        alpha.append( c );
      }
    }
    
    jumbleLogger.exiting( Jumble.class.getName(), "checkAlphabetic()" );
    
    return new String( alpha );
  }    
  
  /**
   * Prep the String with the submitted letters then jumble.
   * 
   * @param str - letters from the user
   */
  private void go( final String str )
  {
    StringBuilder letters = new StringBuilder( str );
    jumbleLogger.info( "letters arriving: " + letters );
    
    Vector<StringBuilder> vsb = new Vector<>( 32, 8 );
    jumbleLogger.finer( "Vector capacity == " + vsb.capacity() );
    jumbleLogger.finer( "Vector size == " + vsb.size() );
    
    jumbler( letters, vsb );
    
    jumbleLogger.info( "Final size of Vector: " + vsb.size() );
    System.out.println( "All combinations from the submitted letters:" );
    for( StringBuilder sb : vsb )
    {
      System.out.println( sb );
    }
    
  }// go()
  
  /**
   * recursive method to find all letter combinations from a selection of letters
   * 
   * @param sb - the letters to jumble
   * @param vsb - a vector to store the letter combinations
   */
  private void jumbler( StringBuilder sb, Vector<StringBuilder> vsb )
  {
    jumbleLogger.fine( "Letters arriving: " + sb );
    
    if( sb.length() == 1 )
    {
      vsb.add( sb );
      jumbleLogger.fine( "Added " + sb + " to Vector." );
    }
    else
    {
      StringBuilder head = pluck( sb );
      
      // recursive call
      jumbler( sb, vsb );
      
      insert( head, vsb );
    }
    
  }// jumbler()
  
  /**
   * Remove the first letter from a StringBuilder and return it as a StringBuilder
   * 
   * @param target - letter which will be 'plucked'
   * @return StringBuilder of the initial letter
   */
  private StringBuilder pluck( StringBuilder target )
  {
    jumbleLogger.fine( "Target arriving: " + target );
    
    String head = target.substring( 0, 1 );
    StringBuilder headsb = new StringBuilder( head );
    
    target.deleteCharAt( 0 );
    
    jumbleLogger.fine( "Letter plucked from target: " + headsb );
    jumbleLogger.fine( "Letters left after pluck():" + target );
    
    return headsb ;
    
  }// pluck()
  
  /**
   * Insert the head letter into each position of each StringBuilder in the Vector
   * 
   * @param head - letter to insert
   * @param vsb - current collection of letter combinations
   */
  private void insert( final StringBuilder head, Vector<StringBuilder> vsb )
  {
    jumbleLogger.fine( "Insert " + head + " to Vector." );
    
    int len, limit = vsb.size();
    jumbleLogger.finer( "Size of Vector == " + limit );
    
    StringBuilder elem, newsb ;
    
    // process all the StringBuilders in the Vector
    for( int j = 0; j < limit; j++ )
    {
      elem = vsb.elementAt( j );
      len = elem.length();
      // insert the head letter into each position of the target StringBuilder
      for( int i = 0; i <= len; i++ )
      {
        newsb = new StringBuilder( elem );
        newsb.insert( i, head );
        vsb.addElement( newsb );
      }
    }
    vsb.addElement( head );
    
  }// insert()
  
}// class Jumble
