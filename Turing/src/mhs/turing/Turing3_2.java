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
  git version created Mar 3, 2014
 
*************************************************************************************** */

package mhs.turing;

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
public class Turing3_2 {
    /** current state of the machine */
    private int state;
    /** current position on the 'tape' */
    private int position;

    /** use an array as a substitute for the <em>infinite</em> tape */
    private int[] ar_tape;
    /** size of the 'tape' array */
    private int tape_size;

    /** number of 'squares' available on the 'tape' */
    final static int DEFAULT_TAPE_SIZE = 256;
    /** MAXIMUM number of 'squares' available on the 'tape' */
    final static int MAX_TAPE_SIZE = 1024 * 16;
    /** MINIMUM number of 'squares' available on the 'tape' */
    final static int MIN_TAPE_SIZE = 16;

    /** determine whether each step is displayed */
    private boolean show_steps;
    /** delay, in milliseconds, between each step display */
    private int step_delay;

    /** if displaying each step of the algorithm, DEFAULT delay (in msec) between each step */
    final static int DEFAULT_DELAY_MS = 2000;
    /** if displaying each step of the algorithm, MINIMUM delay (in msec) between each step */
    final static int MIN_DELAY_MS = 5;
    /** if displaying each step of the algorithm, MAXIMUM delay (in msec) between each step */
    final static int MAX_DELAY_MS = 1000 * 60 * 60;

    //@formatter:off
    /** tape symbol */
    final static int nBLANK = 0, // nBLANK = 0 so tape array is initialized by default to all blanks
                     nZERO  = 1,
                     nONE   = 2,
                     nX     = 3,
                     nSCHWA = 4;

    /** symbols to display */
    static String[] STR_SYMBOLS = { " ", "0", "1", "x", "@" };

    /** machine state */
    final static int STATE_BEGIN   = 0,
                     STATE_PRINT_X = 1,
                     STATE_ERASE_X = 2,
                     STATE_PRINT_0 = 3,
                     STATE_PRINT_1 = 4;
    //@formatter:on

    /** state names for display */
    final static String[] STR_STATES = { "STATE_BEGIN", "STATE_PRINT_X", "STATE_ERASE_X", "STATE_PRINT_0", "STATE_PRINT_1" };

    /**
     * create the machine, process the command line, then start the algorithm
     * 
     * @param args - from command line
     */
    public static void main(String[] args) {
        Turing3_2 turing = new Turing3_2();
        turing.setup(args);
        turing.generate();
    }

    /**
     * process the command line arguments and initialize the program
     * 
     * @param args - from command line
     */
    private void setup(String[] args) {
        // -h for help, -s [arg] for steps, -t <arg> for tape size
        OptionParser parser = new OptionParser("h*s::t:");
        OptionSet options = parser.parse(args);

        /* show help */
        if(options.has("h")) {
            System.out.println("\n Java implementation of the Turing machine described in 'On Computable Numbers' (1936), section 3.II,"
                            + "\n which generates a sequence of 0's followed by an increasing number of 1's, from 0 to infinity,"
                            + "\n i.e. 001011011101111011111... \n"
                            + "\n Usage: java <executable> [-h] [-s [arg]] [-t <arg>] "
                            + "\n -h to print this message."
                            + "\n -t <int> to specify the size of the tape array (within reason)."
                            + "\n -s [int] to have each step of the algorithm displayed, with a 2-second delay between steps,"
                            + "\n    > the optional argument sets an alternate delay between each step, in milliseconds.\n");
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
            System.exit(0);

        }// -h

        step_delay = DEFAULT_DELAY_MS;

        /* use -s [delay] to show each step and optionally specify a delay interval by entering an integer argument */
        if(options.has("s")) {
            show_steps = true;

            // use "-" for blank squares to see each step more clearly
            STR_SYMBOLS[nBLANK] = "-";

            if(options.hasArgument("s")) {
                step_delay = Integer.valueOf((String) options.valueOf("s"));
                if(step_delay < MIN_DELAY_MS) {
                    System.out.println("\n\t>>> MINIMUM value for the step delay is " + MIN_DELAY_MS + ". <<<");
                    step_delay = MIN_DELAY_MS;
                } else if(step_delay > MAX_DELAY_MS) {
                    System.out.println("\n\t>>> MAXIMUM value for the step delay is " + MAX_DELAY_MS + ". <<<");
                    step_delay = MAX_DELAY_MS;
                }
            }
        }// -s

        tape_size = DEFAULT_TAPE_SIZE;

        /* use -t <tape_size> to request a particular array (tape) size */
        if(options.has("t")) {
            tape_size = Integer.valueOf((String) options.valueOf("t"));
            if(tape_size < MIN_TAPE_SIZE) {
                tape_size = MIN_TAPE_SIZE;
                System.out.println("\n\t>>> MINIMUM value for the tape size is " + MIN_TAPE_SIZE + ". <<<");
            } else if(tape_size > MAX_TAPE_SIZE) {
                tape_size = MAX_TAPE_SIZE;
                System.out.println("\n\t>>> MAXIMUM value for the tape size is " + MAX_TAPE_SIZE + ". <<<");
            }
        }// -t

        ar_tape = new int[tape_size];

        state = STATE_BEGIN;
        position = 0;

    }// setup()

    /**
     * run the algorithm:<br>
     * - check the current state<br>
     * - check the current position on the "tape"<br>
     * - create or erase a symbol if necessary<br>
     * - move to a different position on the "tape" if necessary<br>
     * - set the next state<br>
     */
    private void generate() {
        int location;
        int step = 0;

        System.out.println("\n Size of tape array is " + ar_tape.length);
        System.out.println(show_steps ? " Step delay is " + step_delay + "\n" : ".");

        // initial state
        begin();

        /* we don't have an infinite tape -- continue until we move past the end of the array */
        do {
            step++;
            location = ar_tape[position];

            if(show_steps) show_step(step);

            switch(state) {
            case STATE_PRINT_X:
                if(location == nONE) {
                    move_right();
                    set(nX);
                    move_left(3);
                } else if(location == nZERO) {
                    state = STATE_PRINT_1;
                }
                break;

            case STATE_ERASE_X:
                if(location == nX) {
                    erase();
                    move_right();
                    state = STATE_PRINT_1;
                } else if(location == nSCHWA) {
                    move_right();
                    state = STATE_PRINT_0;
                } else if(location == nBLANK) {
                    move_left(2);
                }
                break;

            case STATE_PRINT_0:
                if(location == nBLANK) {
                    set(nZERO);
                    move_left(2);
                    state = STATE_PRINT_X;
                } else {
                    move_right(2);
                }
                break;

            case STATE_PRINT_1:
                if(location == nBLANK) {
                    set(nONE);
                    move_left();
                    state = STATE_ERASE_X;
                } else // if( (location == nZERO) || (location == nONE) )
                {
                    move_right(2);
                }
                break;

            default:
                throw new IllegalStateException("\n\t>> Current state is '" + state + "'?!");
            }// switch

        }// do
        while(position < tape_size);

        end();

    }// generate()

    /**
     * the actions of the initial state of the algorithm -- NEVER return to this state again
     */
    private void begin() {
        if(state != STATE_BEGIN) return;

        if(show_steps) show_step(0);

        set(nSCHWA);
        move_right();
        set(nSCHWA);
        move_right();

        set(nZERO);
        move_right(2);
        set(nZERO);
        move_left(2);
        state = STATE_PRINT_X;
    }

    /**
     * set the specified symbol on the tape at the current position
     * 
     * @param i - symbol to set
     */
    private void set(int i) {
        ar_tape[position] = i;
    }

    /** erase the symbol at the current position */
    private void erase() {
        ar_tape[position] = nBLANK;
    }

    /** move right on the tape by one square */
    private void move_right() {
        move_right(1);
    }

    /**
     * move right by the specified number of squares - not in Turing's description but more convenient
     * 
     * @param count - number of squares to move to the right
     */
    private void move_right(int count) {
        position += count;

        /* end program when position moves beyond the end of the array */
        if(position >= tape_size) {
            System.out.println("Position is " + position + ".");
            end();
        }
    }

    /** move left on the tape by one square */
    private void move_left() {
        move_left(1);
    }

    /**
     * move left by the specified number of squares - not in Turing's description but more convenient
     * 
     * @param count - number of squares to move to the left
     */
    private void move_left(int count) {
        position -= count;

        /* return to 0 if move before the start of the array -- SHOULD NEVER HAPPEN */
        if(position < 0) {
            System.out.println("WARNING: Position: [" + position + "] is less than 0 !");
            position = 0;
        }
    }

    /**
     * display the sequence of symbols on the tape, then exit.
     */
    private void end() {
        if(!show_steps) {
            printTape();
        }

        System.out.println("\n == DONE ==");
        System.exit(0);
    }

    /**
     * display the sequence of symbols on the tape
     */
    private void printTape() {
        for(int posn : ar_tape) {
            printSymbol(posn, true);
        }
        System.out.println("E");
    }

    /**
     * display the symbol used for different types of <code>position</code> on the tape to stdout
     * 
     * @param posn - position on the tape to display
     * @param newline - new line starting at each 'zero'
     */
    private void printSymbol(int posn, boolean newline) {
        switch(posn) {
        case nBLANK:
            System.out.print(STR_SYMBOLS[nBLANK]);
            break;

        case nSCHWA:
            System.out.print(STR_SYMBOLS[nSCHWA]);
            break;

        case nX:
            System.out.print(STR_SYMBOLS[nX]);
            break;

        case nZERO:
            if(newline) System.out.println();
            System.out.print(STR_SYMBOLS[nZERO]);
            break;

        case nONE:
            System.out.print(STR_SYMBOLS[nONE]);
            break;

        default:
            throw new IllegalStateException("\n\t>> Current symbol is '" + posn + "'?!");
        }
    }

    /**
     * display the step sequence and machine state at a particular point in the program
     *
     * @param step - current count in the series of instructions
     */
    private void show_step(int step) {
        System.out.print("Step #" + step + " - State = " + STR_STATES[state] + " - Position is " + position + "[");
        printSymbol(ar_tape[position], false);
        System.out.println("]");

        printTape();

        // pause to allow easier inspection of each step
        try {
            Thread.sleep(step_delay); // milliseconds
        } catch(InterruptedException ie) {
            ie.printStackTrace();
        }
    }

}// class Turing3_2
