/* ***************************************************************************************
 
   Mark Sattolo (epistemik@gmail.com)
 -----------------------------------------------
 $File: //depot/Eclipse/Java/workspace/Turing/src/mhs/turing/Turing3_2.java $
 $Revision: #3 $
 $Change: 180 $
 $DateTime: 2012/05/19 06:12:55 $
 -----------------------------------------------
 
  mhs.turing.Turing3_2.java
  Created May 11, 2012
 
*************************************************************************************** */

package mhs.turing ;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Java implementation of the Turing machine described in "On Computable Numbers (1936)", section 3.II,
 * which generates a sequence of 0's followed by an increasing number of 1's, from 0 to infinity,
 * i.e. 001011011101111011111...
 * 
 * @author mhsatto
 * @version 1.4.3
 * 
 */
public class Turing3_2
{
  final static int DEFAULT_TAPE_SIZE = 1024 ;
  final static int     MAX_TAPE_SIZE = 1024 * 16 ;
  final static int     MIN_TAPE_SIZE =   64 ;
  
  final static int DEFAULT_DELAY_MS = 2000 ;
  final static int     MIN_DELAY_MS =    5 ;
  
  /** tape symbol */
  final static int nSCHWA = -101 ,
                   nBLANK =    0 ,
                   nZERO  =   10 ,
                   nONE   =   11 ,
                   nX     =   99 ;
  
  /** machine state */
  final static int  STATE_BEGIN   = 0 ,
                    STATE_PRINT_X = 1 ,
                    STATE_ERASE_X = 2 ,
                    STATE_PRINT_0 = 3 ,
                    STATE_PRINT_1 = 4 ;
  
  /** state names for display */
  static final String[] STR_STATES = { "STATE_BEGIN", "STATE_PRINT_X", "STATE_ERASE_X", "STATE_PRINT_0", "STATE_PRINT_1" };
  
  /** use an array as a substitute for the infinite tape */
  private int[] ar_tape ;
  /** need a finite size for our array */
  private int tape_size ;
  
  /** current state of the machine */
  private int state ;
  /** current position on the tape */
  private int position ;
  
  /** control whether each step is displayed */
  private boolean show_steps ;
  /** user-determined delay, in milliseconds, between each step display */
  private int step_delay ;
  
  /**
   * create the class and enter setup()
   * 
   * @param args - from command line
   */
  public static void main( String[] args )
  {
    Turing3_2 turing = new Turing3_2();
    turing.setup( args );
    turing.generate();
  }
  
  /**
   * initialize, then generate the number sequence on the tape
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
      "\n -s [int] to have each step of the algorithm displayed, with a 2 second delay between steps," +
      "\n    > the optional argument will specify an alternate delay between each step, in milliseconds.\n" );
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
    }
    
    /* use -s [delay] to show steps and optionally specify a delay interval by entering an integer argument */
    int requested_delay = MIN_DELAY_MS ;
    if( options.has("s") )
    {
      show_steps = true ;
      
      if( options.hasArgument("s") )
      {
        requested_delay = Integer.valueOf( (String)options.valueOf("s") );
        if( requested_delay < 0 )
        {
          System.out.println( "\n\t>>> You must specify a NON-NEGATIVE INTEGER for the step delay value! <<<" );
          show_steps = false ;
        }
      }
    }
    step_delay = ( requested_delay < 0 ? DEFAULT_DELAY_MS : requested_delay );
    
    /* use -t <tape_size> to request a particular array (tape) size */
    int requested_size = MIN_TAPE_SIZE ;
    if( options.has("t") )
    {
      requested_size = Integer.valueOf( (String)options.valueOf("t") );
      if( requested_size < 0 )
      {
        System.out.println( "\n\t>>> You must specify a NON-NEGATIVE INTEGER for the tape size! <<<\n" );
      }
      
      // prevent unreasonably large size...
      if( requested_size > MAX_TAPE_SIZE )
      {
        requested_size = DEFAULT_TAPE_SIZE ;
        System.out.println( "\n\t>>> MAXIMUM VALUE for tape size is " + MAX_TAPE_SIZE + "! <<<" );
      }
    }
    tape_size = ( requested_size <= 0 ? DEFAULT_TAPE_SIZE : requested_size );
    
    ar_tape = new int[ tape_size ];
    
    state = STATE_BEGIN ;
    position = 0 ;
  }
  
  /**
   * the algorithm: move to the proper position on the "tape", create or erase a symbol, then set the next state.
   */
  private void generate()
  {
    int location ;
    int step = 0 ;
    
    System.out.println( "\n Size of tape array is " + ar_tape.length + "\n" );

    /* we don't have an infinite tape -- continue until we move past the end of the array */
    do 
    {
      step++ ;
      switch( state )
      {
        case STATE_BEGIN:
          set( nSCHWA ); move_right();
          set( nSCHWA ); move_right();
          set( nZERO );
          move_right( 2 );
          set( nZERO );
          move_left( 2 );
          state = STATE_PRINT_X ;
          break;
          
        case STATE_PRINT_X:
          location = ar_tape[position] ;
          if( location == nONE )
          {
            move_right();
            set( nX );
            move_left( 3 );
          }
          else if( location == nZERO )
          {
            state = STATE_PRINT_1 ;
          }
          break;
          
        case STATE_ERASE_X:
          location = ar_tape[position] ;
          if( location == nX )
          {
            erase();
            move_right();
            state = STATE_PRINT_1 ;
          }
          else if( location == nSCHWA )
          {
            move_right();
            state = STATE_PRINT_0 ;
          }
          else if( location == nBLANK )
          {
            move_left( 2 );
          }
          break ;
          
        case STATE_PRINT_0:
          location = ar_tape[position] ;
          if( location == nBLANK )
          {
            set( nZERO );
            move_left( 2 );
            state = STATE_PRINT_X ;
          }
          else
          {
            move_right( 2 );
          }
          break;
          
        case STATE_PRINT_1:
          location = ar_tape[position] ;
          if( location == nBLANK )
          {
            set( nONE );
            move_left();
            state = STATE_ERASE_X ;
          }
          else // if( (location == nZERO) || (location == nONE) )
          {
            move_right( 2 );
          }
          break;
          
        default: throw new IllegalStateException( "\n\t>> Current state is '" + state + "'?!" );
      }
      
      // see the number sequence and machine state after each step
      if( show_steps )
        show_step( step );
    }
    while( position < tape_size );
  }
  
  /**
   * set the specified symbol on the tape at the current position
   * 
   * @param i - symbol to set
   */
  private void set( int i )
  {
    ar_tape[position] = i ;
  }
  
  /** erase the symbol at the current position  */
  private void erase()
  {
    ar_tape[position] = nBLANK ;
  }
  
  /** move right on the tape by one square  */
  private void move_right()
  {
    move_right( 1 );
  }
  
  /**
   * move right by the specified number of squares
   * - not in Turing's description but more convenient
   * 
   * @param count - number of squares to move to the right
   */
  private void move_right( int count )
  {
    for( int i=0; i < count; i++ )
      position++ ;
    
    /* end program if position moves beyond the end of the array */
    if( position >= tape_size )
    {
      end();
    }
    
  }
  
  /** move left on the tape by one square  */
  private void move_left()
  {
    move_left( 1 );
  }
  
  /**
   * move left by the specified number of squares
   * - not in Turing's description but more convenient
   * 
   * @param count - number of squares to move to the left
   */
  private void move_left( int count )
  {
    for( int i=0; i < count; i++ )
      position-- ;
    
    // return to 0 if move before the beginning of the array
    if( position < 0 )
      position = 0 ;
  }
  
  /**
   * print info for the user, display the sequence of symbols on the tape, and exit. 
   */
  private void end()
  {
    if( ! show_steps )
    {
      // print the tape using the default character to display a blank square
      printTape();
    }
    
    System.out.println();
    System.exit( 0 );
  }
  
  /**
   *  display the tape with a space printed for each blank square
   */
  private void printTape()
  {
    printTape( " " );
  }
  
  /**
   * display the sequence of symbols on the tape to stdout 
   * 
   * @param blank - character to use to display a blank square on the tape
   */
  private void printTape( String blank )
  {
    for( int symbol : ar_tape )
    {
      switch( symbol )
      {
        case nBLANK:
          System.out.print( blank ); break ;
          
        case nSCHWA:
          System.out.print( "@" ); break ;
          
        case nX:
          System.out.print( "x" ); break ;
          
        case nZERO:
          // start a new line before each zero
          System.out.println();
          System.out.print( "0" ); break ;
          
        case nONE:
          System.out.print( "1" ); break ;
          
        default: throw new IllegalStateException( "\n\t>> Current symbol is '" + symbol + "'?!" );
      }
    }
    System.out.println();
  }

  /**
   * display the number sequence and machine state at a particular point in the program
   *
   * @param step - current place in the series of instructions
   */
  private void show_step( int step )
  {
    System.out.println( "Step #" + step + " - State = " + STR_STATES[state] + " - Position is " + position );
    
    // use "-" for blank squares to see each step more clearly
    printTape( "-" );
    
    // pause to allow easier inspection of each step
    try
    {
      Thread.sleep( step_delay ); // milliseconds
    }
    catch( InterruptedException ie )
    {
      ie.printStackTrace();
    }
  }
  
}// class Turing3_2
