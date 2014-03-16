/* ***************************************************************************************
 
   Mark Sattolo (epistemik@gmail.com)
 -----------------------------------------------
  mhs.eclipse.wordjumble.WordJumble.java
  Created March 16, 2014
  original was Jumble.pas
 
*************************************************************************************** */

package mhs.eclipse.wordjumble ;

import java.io.IOException;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Java implementation of ...<br>
 * 
 * @author mhsatto
 * @version 0.1
 * 
 */
public class WordJumble
{
  /**
   * create the machine, process the command line, then start the algorithm
   * 
   * @param args - from command line
   */
  public static void main( String[] args )
  {
    WordJumble jumble = new WordJumble();
    jumble.setup( args );
    jumble.generate();
  }
  
  /**
   * process the command line arguments and initialize the program
   * 
   * @param args - from command line
   */ 
  private void setup( String[] args )
  {
    // -h for help, -s [arg] for steps, -t <arg> for tape size
    OptionParser parser = new OptionParser( "h*s::t:" );
    OptionSet options = parser.parse( args );
    
    /* show help */
    if( options.has("h") )
    {
      System.out.println( 
      "\n Java implementation of ... \n" +
      "\n Usage: java <executable> [-h] [-s [arg]] [-t <arg>] " +
      "\n -h to print this message." +
      "\n -t <int> to specify " +
      "\n -s [int] to have \n" );
/*
      try
      {
        parser.printHelpOn( System.out );
      }
      catch( IOException ioe )
      {
        // TODO Auto-generated catch block
        ioe.printStackTrace();
      }
*/
      System.exit( 0 );
      
    }// -h
    
    /* use -s [delay] to show each step and optionally specify a delay interval by entering an integer argument */
    if( options.has("s") )
    {
          System.out.println( "\n\t>>> MINIMUM value for the step delay. <<<" );
    }// -s
    
    /* use -t <tape_size> to request a particular array (tape) size */
    if( options.has("t") )
    {
        System.out.println( "\n\t>>> MINIMUM value for the tape size. <<<" );
    }// -t
    
  }// setup()
  
  /**
   * 
   */
  private void generate()
  {
    byte[] unjumbled = new byte[1024];
    String jumbled = new String( "" );
    int posn = 1 ;
    
    System.out.println( "Please enter a string to jumble: ");
    try
    {
      System.in.read( unjumbled );
    }
    catch( IOException ie )
    {
      // TODO Auto-generated catch block
      ie.printStackTrace();
    }
    jumble( unjumbled, jumbled, posn );
    
    System.out.println( "PROGRAM ENDED." );
    
  }// generate()
  
  /**
   * 
   */
  private void jumble( byte[] unjumbled, String jumbled, int posn )
  {
    int len, pos2 ;
    char x ;
    len = unjumbled.length ;
    if( len >= posn )
    {
      pos2 = posn + 1 ;
      if( pos2 <= len )
      {
        jumble( unjumbled, jumbled, pos2 );
        x = (char)unjumbled[(posn % len)+1] ;
        delete( unjumbled, (posn % len)+1, 1 );
        jumbled = jumbled + x ;
        jumble( unjumbled, jumbled, posn );
      }
      else
      if( jumbled.length() > 0 )
      {
        System.out.println( jumbled );
      }
    }
  }// jumble()
  
}// class WordJumble
