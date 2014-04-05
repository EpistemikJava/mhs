package mhs.eclipse.wordjumble;

import java.util.Vector;

/**
 * Class to implement a recursive word jumbling algorithm<br>
 *  - initial implementation imported from remote
 *  
 * @author mhsatto
 * @version 0.2
 */
public class Testing2
{
  /**
   * MAIN
   * 
   * @param args - from command line
   */
  public static void main( String[] args )
  {
    // get the letters from the command line
    if( args.length < 1 )
    {
      System.out.println( "You must enter some letters on the command line." );
      System.exit( 1 );
    }

    // run the test
    Testing2 t2 = new Testing2();
    t2.go( args[0] );
    
    System.out.println( "\n PROGRAM ENDED" );

  }// main()

  /**
   * Prep the String with the submitted letters then jumble.
   * 
   * @param str - submitted letters from command line
   */
  private void go( final String str )
  {
    StringBuilder letters = new StringBuilder( str );
    System.out.println( "letters arriving in go():" + letters );
    
    Vector<StringBuilder> vsb = new Vector<>( 32, 8 );
    System.out.println( "vsb capacity = " + vsb.capacity() );
    System.out.println( "vsb size = " + vsb.size() );
    
    jumble( letters, vsb );
    
    System.out.println( "Final size of vsb: " + vsb.size() );
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
  private void jumble( StringBuilder sb, Vector<StringBuilder> vsb )
  {
    System.out.println( "Letters arriving at jumble(): " + sb );
    if( sb.length() == 1 )
    {
      vsb.add( sb );
      System.out.println( "Added " + sb + " to vsb." );
    }
    else
    {
      StringBuilder head = pluck( sb );
      
      // recursive call
      jumble( sb, vsb );
      
      insert( head, vsb );
    }
    
  }// jumble()

  /**
   * Remove the first letter from a StringBuilder and return it as a StringBuilder
   * 
   * @param target - StringBuilder which will be 'plucked'
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
    System.out.println( "Insert " + head + " to vsb." );
    
    int len, limit = vsb.size();
    System.out.println( "Size of vsb == " + limit );
    
    StringBuilder elem, newsb;
    
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
  
}// class Testing2
