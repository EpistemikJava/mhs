package mhs.eclipse.wordjumble;

import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to implement a recursive word jumbling algorithm<br>
 *  - initial implementation imported from remote
 *  
 * @author mhsatto
 * @version 0.3
 */
public class Jumble
{
  static Logger jumbleLogger ;
  
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
    
    System.out.println( "\n*** PROGRAM ENDED ***" );
    
  }// main()
  
  /**
   * Process the command line then get user input if necessary
   * 
   * @param ar_str - command line args
   */
  private static String setup( final String[] ar_str )
  {
    jumbleLogger.info( "" );
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
    StringBuilder alpha = new StringBuilder();
    
    char[] mychars = toCheck.toCharArray();
    
    for( char c: mychars )
    {
      if( Character.isLetter(c) )
      {
        alpha.append( c );
      }
    }
    
    return new String( alpha );
  }    
  
  /**
   *  initialize the logger
   */
  private static void setLogger()
  {
    jumbleLogger = Logger.getLogger( Jumble.class.getName() );
    /*
    for( Handler h: jumbleLogger.getHandlers() )
    {
      h.setLevel( Level.ALL );
    }
    /*
    ConsoleHandler jumbleHandler = new ConsoleHandler();
    jumbleHandler.setFormatter( new SimpleFormatter() );
    jumbleLogger.addHandler( jumbleHandler );
    */
    // NEED to set the root ("") Logger's handler 
    Logger.getLogger("").getHandlers()[0].setLevel( Level.ALL );
    // AND the jumble logger itself to Level.ALL to get ALL messages to the Console
    jumbleLogger.setLevel( Level.ALL );
    
    jumbleLogger.finer( "Set up Logger" );
    
  }// setLogger()
  
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
    System.out.println( "Vector capacity == " + vsb.capacity() );
    System.out.println( "Vector size == " + vsb.size() );
    
    jumbler( letters, vsb );
    
    System.out.println( "Final size of Vector: " + vsb.size() );
    System.out.println( "All letter combinations from the submitted string:" );
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
    System.out.println( "Letters arriving at jumbler(): " + sb );
    
    if( sb.length() == 1 )
    {
      vsb.add( sb );
      System.out.println( "Added " + sb + " to Vector." );
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
    System.out.println( "Target arriving at pluck(): " + target );
    
    String head = target.substring( 0, 1 );
    StringBuilder headsb = new StringBuilder( head );
    
    target.deleteCharAt( 0 );
    
    System.out.println( "Letter plucked from target: " + headsb );
    System.out.println( "Letters left after pluck():" + target );
    
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
    System.out.println( "Insert " + head + " to Vector." );
    
    int len, limit = vsb.size();
    System.out.println( "Size of Vector == " + limit );
    
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
