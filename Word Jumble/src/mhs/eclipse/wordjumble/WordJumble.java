/* ***************************************************************************************
 
   Mark Sattolo (epistemik@gmail.com)
 -----------------------------------------------
  mhs.eclipse.wordjumble.WordJumble.java
  Created March 16, 2014
  original was Jumble.pas
 
*************************************************************************************** */

package mhs.eclipse.wordjumble ;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Java implementation of the Turing machine described in "On Computable Numbers (1936)", section 3.II,<br>
 * which generates a sequence of 0's followed by an increasing number of 1's, from 0 to infinity,<br>
 * i.e. 001011011101111011111...<br>
 * See also <i>The Annotated Turing</i> by <b>Charles Petzold</b>; Chapter 5, pp.85-94.
 * 
 * @author mhsatto
 * @version 1.4.5
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
      "\n Java implementation of the Turing machine described in 'On Computable Numbers' (1936), section 3.II," +
      "\n which generates a sequence of 0's followed by an increasing number of 1's, from 0 to infinity," +
      "\n i.e. 001011011101111011111... \n" +
      "\n Usage: java <executable> [-h] [-s [arg]] [-t <arg>] " +
      "\n -h to print this message." +
      "\n -t <int> to specify the size of the tape array (within reason)." +
      "\n -s [int] to have each step of the algorithm displayed, with a 2-second delay between steps," +
      "\n    > the optional argument sets an alternate delay between each step, in milliseconds.\n" );
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
  
  
}
/*
program  WordJumble ;

  procedure jumble( unjumbled, jumbled: string; posn: integer) ;
    var
      len, pos2: integer ;
      x: char ;
    BEGIN
      len := length(unjumbled) ;
      if len >= posn then
        BEGIN
          pos2 := posn + 1 ;
          if pos2 <= len then
            jumble(unjumbled, jumbled, pos2) ;
          x := unjumbled[(posn mod len)+1] ;
          delete(unjumbled, (posn mod len)+1, 1) ;
          jumbled := jumbled + x ;
          jumble(unjumbled, jumbled, posn)
        END
      else
      if length(jumbled) > 0 then
        writeln(jumbled) ;
    END;
  { private proc jumble }

  var
    unjumbled, jumbled: string ;
    posn: integer ;

  BEGIN
    write('Please enter a string to jumble: ');
    readln(unjumbled) ;
    jumbled := '' ;
    posn := 1 ;
    jumble(unjumbled, jumbled, posn) ;
   
    writeln( 'PROGRAM ENDED.' )
  END.
*/
