/* ***************************************************************************************
 
   Mark Sattolo (epistemik@gmail.com)
 -----------------------------------------------
 $File: //depot/Eclipse/Java/workspace/Pseudokeu/src/mhs/pseudokeu/Square.java $
 $Revision: #13 $
 $Change: 175 $
 $DateTime: 2012/02/25 09:39:04 $
 -----------------------------------------------
 
  mhs.latinsquare.Square.java
  Eclipse version created on Jan 14, 2008, 20h33
 
*************************************************************************************** */

package mhs.pseudokeu;

import java.awt.Color;
import java.util.logging.Level;

/**
 *  Used by {@link Square} <var>type</var> to define different types of Squares
 *  and to centralize various useful {@link Square} properties
 *  @author Mark Sattolo
 */
enum SqrTypes
{
  /* the different TYPES of Square */
  OPEN   ( 0, Color.gray            ), // blank OR temp
  GUESS  ( 1, Color.blue.darker()   ),
  FIXED  ( 2, Color.black           ),
  
  /* modes - just used to get the color */
  TEMP   ( 3, new Color( 150, 60, 30 )      ), // brown
  ACTIVE ( 4, Color.white                   ),
  BAD    ( 5, Color.red.darker()            ), // used for both 'conflicting' and 'wrong' Square display
  SOLVED ( 6, Color.green.darker().darker() );
  
  static final int
                  BLANK_VAL = OPEN.getKey(),
                  NUM_TYPES = FIXED.getKey() + 1 ;
  
  /** Max value for a temp Square - highest 3 digits = 987 */
  static final int MAX_TEMP_VAL = 987 ;
  
  private final int key ;
  private final Color color ;
  
  SqrTypes( int i, Color c ) { key = i; color = c; }
  int getKey() { return key; }
  Color getColor() { return color; }
}

//====================================================================================

/**
 *  The data structure behind every <code>Square</code> in the <code>Grid</code> <br>
 *  - {@link Grid} uses a 2D array of <code>Squares</code>
 *  @author Mark Sattolo
 *  @version $Revision: #13 $
 */
class Square
{
 /*
  *        C O N S T R U C T O R S
  ***************************************************************************************/
    
  /**
   *  default Constructor 
   *  @param homeGrid - enclosing {@link Grid}
   */
  public Square( final Grid homeGrid )
  {
    grid = homeGrid ;
    gridLength = grid.getLength() ;
    logger = Grid.logger ;
    
    chainColor = Grid.NO_COLOR ;
    nGrpSqrsWithVal = new int[ gridLength + 1 ];
    excludeVals = new boolean[ gridLength + 1 ]; // all false at start
  }
  
 /*
  *            M E T H O D S
  ***************************************************************************************/
  
 // ===========================================================================================================
 //                          I N T E R F A C E
 // ===========================================================================================================
  
  /**
   * set the references to each {@link Square}'s {@link Group}s
   *
   * @param row - my {@link Row}
   * @param col - my {@link Col}
   * @param zone - my {@link Zone}
   */
  void setGroups( final Row row, final Col col, final Zone zone )
  {
    // each Square is in a particular row, col, and zone
    myRow  = row ;
    myCol  = col ;
    myZone = zone ;
  
  }// Square.setGroups()
  
  /**
   * Set {@link SqrTypes#FIXED} for this {@link Square}
   * @see Grid#activateGame
   */
  void setFixed()
  {
    type = SqrTypes.FIXED ;// GAME ACTIVATION ONLY
    
    myZone.adjustTypeCounts( SqrTypes.GUESS, SqrTypes.FIXED );
     myRow.adjustTypeCounts( SqrTypes.GUESS, SqrTypes.FIXED );
     myCol.adjustTypeCounts( SqrTypes.GUESS, SqrTypes.FIXED );
    
    logger.severe( "Sqr " + strGridPosn() );
    
  }// Square.setFixed()
  
  /**
   *  Have a new {@link #value} in this {@link Square} <br>
   *  - start the <em>'Set Value Chain'</em> to update the relevant variables in this Square, its {@link Group}s,
   *  its <b>Group Squares</b>, and the <code>Groups</code> of each <b>Group Square</b> <br>
   *  - called by {@link Grid#activateGame} OR {@link Grid#newValue}
   *  @param val - the new <var>value</var>
   *  @param tempMode - temp status when new value was entered
   */
  void newValue( final int val, final boolean tempMode )
  {
    // DO NOTHING if both are Blank OR both value & temp mode are unchanged
    if( ( (val == SqrTypes.BLANK_VAL) && (value == SqrTypes.BLANK_VAL) )
        || ( (val == value) && (temp == tempMode) ) )
    {
      logger.fine( "DO NOTHING... current val = " + value + ( temp ? "/T" : "" )
                   + " & new val = " + val + ( tempMode ? "/T" : "" )+ " // current type = " + type );
      return ;
    }
    
    logger.info( "Sqr " + strGridPosn() + " : Set value to '" + val + "'" );
    
    /*  >> the 'Set Value Chain'
     *  When a user changes the value of a Square, there are several updates that need to be done 
     *  to keep the application aware of the state of the Grid in order to be able to SOLVE the current game:
     *  1. Update the (fields of the) active Square itself
     */
    
    // store the old and new values
    int $oldVal = value , $newVal = val ;
    
    // set the value
    value = val ;
    
    SqrTypes $oldType = type ;
    // modify the type based on the current mode and submitted value
    adjustType( tempMode );
    // clear the 'wrong' state
    wrong = false ;
    
    /* CHANGE TEMP VALUES TO BLANK HERE THEN NO NEED FOR TEMP HANDLING IN ANY FURTHER METHODS  */
    if( $oldType == SqrTypes.OPEN )
      $oldVal = SqrTypes.BLANK_VAL ;
    else
      if( isOpen() ) $newVal = SqrTypes.BLANK_VAL ;
    
    // NO changes needed if both old and new are OPEN
    if( isGuess() || ($oldType == SqrTypes.GUESS) )
    {
      adjustConflict( $oldVal, $newVal );
      
      resetPossibleVals();
      
      /* 2. Update the Groups of the active Square
       * 3. Update the other Squares in the active Groups - i.e. the "Group Squares" of the active Square  */ 
      myZone.changedSqr( $oldVal, $newVal, $oldType, type );
       myRow.changedSqr( $oldVal, $newVal, $oldType, type );
       myCol.changedSqr( $oldVal, $newVal, $oldType, type );
         
      /* 4. Update the Groups of the Group Squares - i.e. the "Group Square Groups"  */
      grid.setSqrsCanBeVal();
      
      /* >> FINISHED the 'Set Value Chain'  */
    }
  
  }// Square.newValue()
  
  /**
   *  A {@link Square} in one of my {@link Group}s has a new <var>value</var> <br>
   *  Must increment OR decrement {@link #nGrpSqrsWithVal} for the old and new values,
   *  then adjust {@link #conflicting} and reset {@link #possibleVals} <br>
   *  - called by {@link Group#notifySqrsOfValChange} or {@link Zone#notifySqrsOfValChange}
   *  @param oldVal - old <var>value</var> of the changed Square
   *  @param newVal - new <var>value</var> of the changed Square
   *  @see #adjustConflict
   *  @see #resetPossibleVals
   */
  void adjustGroupSqrCounts( final int oldVal, final int newVal )
  {
    logger.fine( "Sqr " + strGridPosn() + " : '" + oldVal + "' -> '" + newVal + "'" );
    
    if( oldVal != SqrTypes.BLANK_VAL )
      --nGrpSqrsWithVal[ oldVal ];
    
    if( newVal != SqrTypes.BLANK_VAL )
      ++nGrpSqrsWithVal[ newVal ];
    
    // must call this AFTER adjusting nGroupSqrsWithVal[] above
    adjustConflict( oldVal, newVal );
    
    // reset possible values AFTER nGroupSqrsWithVal[] has been adjusted
    resetPossibleVals();
    
  }// Square.adjustGroupSqrCounts()
  
  /** @return value of {@link #nGrpSqrsWithVal} at the specified index
   *  @param index - into array  */
  int numGrpSqrsWithVal( final int index ) { return nGrpSqrsWithVal[index] ;}

  /** @return {@link #possibleVals}  */
  int getPossibleVals() { return possibleVals ;}
  
  /** @return {@link #nPossibleVals}  */
  int numPossibleVals() { return nPossibleVals ;}
  
  /**
   *  Remove an entry from {@link #possibleVals} and add this entry to {@link #excludeVals} <br>
   *  - called by Solving methods
   *  @param val - value to remove
   *  @return success or failure
   */
  boolean removePossibleVal( final int val )
  {
    // need to set the exclude val even though this may not be a possible val
    excludeVals[val] = true ;
    
    // Only Open Squares have valid possible values
    if( ! isOpen() )
      return false ;
    
    boolean $result = false ;
    
    logger.append( "\t\t\t Sqr " + strGridPosn()
                    + " : possibleVals == " + Helper.displaySetBits(possibleVals, gridLength, " ")
                   /* + "/ " + Integer.toBinaryString(possibleVals)*/ + "/ n." + nPossibleVals );
    
    int $val = ( 1 << val );
    if( ($val|possibleVals) == possibleVals )
    {
      possibleVals -= $val ;
      nPossibleVals-- ;
      logger.appendln( " -> NOW == " + Helper.displaySetBits(possibleVals, gridLength, " ")
                       /* + "/ " + Integer.toBinaryString(possibleVals)*/ + "/ n." + nPossibleVals );
      
      removeSqrFromGrpVal( val );
      
      $result = true ;
    }
    else
        logger.appendln( " -> NO CHANGE" );
    
    return $result ;
    
  }// Square.removePossibleVal()
  
  /**
   *  Find my only possible value <br>
   *  - called by {@link Grid#findSqrSingle}
   *  @return success or failure
   */
  boolean findSingleVal()
  {
    if( ! isOpen() )
      return false ;
    
    logger.append( "Sqr " + strGridPosn() );
    
    boolean $result = false ;
    if( nPossibleVals == 1 )
    {
      solvedValue = Integer.numberOfTrailingZeros( possibleVals );
      logger.append( ": Found Single val '" + solvedValue + "'" );
      $result = true ;
    }
    
    logger.send( $result ? Level.INFO : Level.FINER );
    return $result ;
    
  }// Square.findSingleVal()
  
  /**
   *  Is the parameter one of my possible values? <br>
   *  @param val - value to check
   *  @return success or failure
   */
  boolean canBeVal( int val )
  {
    if( ((1 << val) | possibleVals) == possibleVals )
      return true ;
    
    return false ;
    
  }// Square.canBeVal()
  
  /** @return value of {@link #excludeVals} at the specified index
   *  @param index - into the array  */
  boolean getExcludeVal( final int index ) { return excludeVals[index] ;}
  
  /**
   * Set the chain color
   * @param color - color to set
   * @return previous color  */
  int putInChain( final int color )
  {
    int $old = chainColor ;
    chainColor = color ;
    return $old ;
  }
  
  /**
   *  Reset all mutable fields to default values
   *  - called by {@link #Square(Grid)} and {@link Grid#clear}
   */
  final void clear()
  {
    for( int v=0; v <= gridLength; v++ )
    {
      nGrpSqrsWithVal[v] = 0 ;
      excludeVals[v] = false ;
    }
    
    type  = SqrTypes.OPEN ;
    value = SqrTypes.BLANK_VAL ;
    
    // clear ALL modes
    active = temp = autoSolved = conflicting = wrong = false ;
    
    // clear Solving parameters
    solvedValue = SqrTypes.BLANK_VAL ;
    chainColor = Grid.NO_COLOR ;
    
  }// Square.clear()
  
  /**
   * What is my <var>type</var>?
   * @return {@link SqrTypes} {@link #type}
   */
  SqrTypes getType() { return type ;}
  
  /**
   * What is my {@link #value}?
   * @return int with <var>value</var>
   */
  int getValue() { return value ;}
  
  /**
   * Set {@link #autoSolved} for this {@link Square}
   * @see Grid#setAutoSolvedSqr
   */
  void setAutoSolved()
  {
    autoSolved = true ; // CANNOT undo 'SOLVED' status
    
    logger.info( "Sqr " + strGridPosn() + ": autoSolved = " + this.autoSolved );
    
  }// Square.setAutoSolved()
  
  /**
   * Set {@link #wrong} for this {@link Square}
   * @param state - WRONG or NOT
   * @see Grid#findWrongGuess
   */
  void setWrong( final boolean state )
  {
    wrong = state ;
    
    logger.info( "Sqr " + strGridPosn() + ": wrong = " + state + "\n" );
    
  }// Square.setWrong()
  
  /** @return {@link #type} == {@link SqrTypes#OPEN}  */
  boolean isOpen() { return type == SqrTypes.OPEN ;}
  
  /** @return {@link #type} == {@link SqrTypes#GUESS}  */
  boolean isGuess() { return type == SqrTypes.GUESS ;}
  
  /** @return {@link #type} == {@link SqrTypes#GUESS}  */
  boolean isFixed() { return type == SqrTypes.FIXED ;}
  
  /**
   * This {@link Square} is currently selected
   * @return boolean <var>active</var>
   */
  boolean isActive() { return active ;}
  
  /**
   * This {@link Square} has just been selected or de-selected
   * @param val - value to set
   * @return success or failure
   */
  boolean setActive( final boolean val )
  {
    if( type == SqrTypes.FIXED )
    {
      logger.warning( "! Trying to activate a FIXED Square: " + strGridPosn() );
      return false ;
    }
    
    active = val ;
    return true ;
  }
  
  /**
   * This {@link Square} has a <var>value</var> the same as at least one 
   * other {@link Square} in one of its {@link Group}s
   * @return boolean {@link #conflicting}
   */
  boolean isConflicting() { return conflicting ;}
  
  /** @return {@link #temp}  */
  boolean isTemp() { return temp ; }
  
  /** @return {@link #autoSolved}  */
  boolean isAutoSolved() { return autoSolved ; }
  
  /** @return {@link #wrong}  */
  boolean isWrong() { return wrong ; }
  
  /** @return whether {@link #chainColor} is set  */
  boolean inColorChain() { return chainColor > Grid.NO_COLOR ; }
  
  /**
   * Which {@link Zone} am I in?
   * @return {@link #myZone}
   */
  Zone getZone() { return myZone ;}
  
  /**
   * What is my {@link Zone} index?
   * @return <var>posnIndex</var> of {@link #myZone}
   */
  int getZoneIndex() { return myZone.getPosn() ;}
  
  /**
   * Which {@link Row} am I in?
   * @return <CODE>Row</CODE> {@link #myRow}
   */
  Row getRow() { return myRow ;}
  
  /**
   * What is my {@link Row} index?
   * @return <var>posnIndex</var> of {@link #myRow}
   */
  int getRowIndex() { return myRow.getPosn() ;}
  
  /**
   * Which {@link Col} am I in?
   * @return <CODE>Col</CODE> {@link #myRow}
   */
  Col getCol() { return myCol ;}
  
  /**
   * What is my {@link Col} index?
   * @return <var>posnIndex</var> of {@link #myCol}
   */
  int getColIndex() { return myCol.getPosn() ;}
  
 // end INTERFACE
 // ===========================================================================================================
 //                            P R I V A T E
 // ===========================================================================================================
  
  /**
   * Update my {@link #type} according to mode or new {@link #value} <br>
   * <b> !! value has ALREADY been updated !! </b><br>
   * - called by {@link #newValue}
   * @param tempMode - temp status when type changed
   * @see SqrTypes
   */
  private void adjustType( final boolean tempMode )
  {
    // Once FIXED, remain that way
    if( type == SqrTypes.FIXED )
      return ;
    
    SqrTypes $oldType = type ;
    if( tempMode || (value == SqrTypes.BLANK_VAL) || (value > gridLength) )
    {
      type = SqrTypes.OPEN ;
      if( $oldType == SqrTypes.GUESS ) grid.incBlankCount( 1 );
    }
    else
      {
        type = SqrTypes.GUESS ;
        if( $oldType == SqrTypes.OPEN ) grid.incBlankCount( -1 );
      }
    
    temp = (value == SqrTypes.BLANK_VAL) ? false : tempMode ;
  
  }// Square.adjustType()
  
  /**
   *  Set or unset if this {@link Square} has a duplicate <var>value</var> <br>
   *  - called by {@link #newValue} OR {@link #adjustGroupSqrCounts}
   *  @param oldval - old <var>value</var> of the changed {@link Square}
   *  @param newval - new <var>value</var> of the changed {@link Square}
   *  @see Launcher#setConflicts
   *  @see Grid#hasConflicts
   */
  private void adjustConflict( final int oldval, final int newval )
  {
    logger.fine( "Sqr " + strGridPosn() + " : for '" + oldval + "' -> '" + newval + "'" );
    
    int $diff = 0 ;
    
    if( active )
    {
      // lose any conflict with the old value
      if( nGrpSqrsWithVal[oldval] > 0 )
        $diff-- ;
      
      // may still conflict with the new value
      if( nGrpSqrsWithVal[newval] >= 1 )
        $diff++ ;
    
    }
    else // updating Group Squares
      if( isGuess() )
      {
        // may no longer be conflicting if no other Group Squares with oldVal 
        if( (value == oldval) && (nGrpSqrsWithVal[oldval] == 0) )
          $diff-- ;
        
        // may be newly conflicting if no other Group Squares with newVal 
        if( (value == newval) && (nGrpSqrsWithVal[newval] == 1) )
          $diff++ ;
      }
    
    if( $diff != 0 )
    {
      conflicting = ($diff > 0) ? true : false ;
      grid.incConflicts( $diff, this );
    }
    
  }// Square.adjustConflict()
  
  /**
   *  Reset my possible values fields <br>
   *  - called by {@link #newValue} and {@link #adjustGroupSqrCounts}
   *  @return {@link #nPossibleVals}
   *  @see #nGrpSqrsWithVal
   *  @see #excludeVals
   */
  private int resetPossibleVals()
  {
    if( ! isOpen() )
    {
      possibleVals = ( 1 << value );
      return( nPossibleVals = 1 );
    }
    
    possibleVals = 0 ;
    nPossibleVals = 0 ;
    for( int v=1; v <= gridLength; v++ )
      if( (nGrpSqrsWithVal[v] == 0) && (!excludeVals[v]) )
      {
        possibleVals += ( 1 << v );
        nPossibleVals++ ;
      }
    
    logger.fine( "Sqr " + strGridPosn() + ": possibleVals == " + Helper.displaySetBits(possibleVals, gridLength, " ")
                 + "/ " + Integer.toBinaryString(possibleVals) + " / n." + nPossibleVals );
    
    /* test */
    if( Launcher.DEBUG )
    {
      int vals = (1 << 3) + (1 << 4) + (1 << 7) + (1 << 9) ;
      if( (possibleVals|vals) == vals )
        logger.finer( "FOUND vals 3,4,7,9 ! (vals == " + vals + "/" + Integer.toBinaryString(vals) );
    }
    
    return nPossibleVals ;
    
  }// Square.resetPossibleVals()
  
  /**
   *  Remove an entry from each of my Group's possible values <br>
   *  - called by {@link #removePossibleVal}
   *  @param val - value to remove
   *  @see Group#removeSqrFromVal
   */
  private void removeSqrFromGrpVal( final int val )
  {
    // update each of my Groups
    myZone.removeSqrFromVal( this, val );
     myRow.removeSqrFromVal( this, val );
     myCol.removeSqrFromVal( this, val );
    
  }// Square.removeSqrFromGrpVal()
  
 // ===========================================================================================================
 //                            D E B U G    C O D E
 // ===========================================================================================================
  
  String myname() { return getClass().getSimpleName(); }
  
  /**
   * What is my position in the {@link Grid} ?
   * @return position in {@link Row} and {@link Col}
   */ 
  String strGridPosn()
  {
    return( 'r' + Integer.toString(myRow.getPosn()) + 'c' + Integer.toString(myCol.getPosn()) );
  }
  
  /**
   *  Set my possible values (if 3 or fewer) as a temp value
   *  @return the calculated int
   *  @see Grid#setPossibleValsAsTempVals
   */
  int setPossValsAsTempVal()
  {
    if( (!isOpen()) || (nPossibleVals > 3) )
      return SqrTypes.BLANK_VAL ;
    
    int v, $pv = possibleVals ;
    int $tempVal=0, $count=0, $base=10 ;
    
    for( v=1; v <= gridLength; v++ )
    {
      $pv >>= 1 ;
      if( Integer.numberOfTrailingZeros($pv) == 0 )
      {
        if( $count == 0 )
        {
          $tempVal = v ;
          $count++ ;
        }
        else
          if( ($count == 1) || ($count == 2) )
          {
            $tempVal = ($tempVal * $base) + v ;
            $count++ ;
          }
          else
              logger.warning( "Problem with setPossValsAsTempVal()!" );
      }
    }
    
    if( $count > 0 )
      newValue( $tempVal, true );
    
    return $tempVal ;
  
  }// Square.setPossValsAsTempVal()
  
  /**
   * display a {@link Square}
   * @param level - {@link Level} to display at
   * @param brief - option to display less information
   * @param info  - extra info
   */
  void display( final Level level, final boolean brief, final String info )
  {
    int v ;
    
    if( !brief ) logger.appendln( info + "\n-----------------------------" );
    
    logger.append( " Sqr " + strGridPosn() + "_z" + myZone.getPosn() 
                   + ( active ? "/A" : "" ) + " = " + value + ( temp ? "/temp" : "" ) + ( autoSolved ? "/SOLVED" : "" )
                   + " (" + ( conflicting ? "/CONFLICT " : "" ) + ( wrong ? "/WRONG " : "" ) + type
                   + "): nPV." + nPossibleVals + " SV." + solvedValue + " PV." + possibleVals );
    
    logger.append( "\n nGrpSqrsWithVal (1-" + gridLength + "): " );
    for( v=1; v <= gridLength; v++ )
      logger.append( v + "|" + nGrpSqrsWithVal[v] + " " );
    
    logger.append( "\n    possibleVals (1-" + gridLength + "): " + Helper.displaySetBits(possibleVals, gridLength, " ") );
    
    logger.append( "\n     excludeVals (1-" + gridLength + "): " );
    for( v=1; v <= gridLength; v++ )
      logger.append( excludeVals[v] ? (v + " ") : "" );
    
    logger.append( "\n-----------------------------------------------------------------" );
    
    if( !brief ) logger.appendln("");
    
    logger.send( level );
  
  }// Square.display()
  
 /*
  *            F I E L D S
  ***************************************************************************************/ 
  
  /** Perforce file version */
  static final String strP4_VERSION = "$Revision: #13 $" ;
  
  /** Logging */
  private static PskLogger logger ;
  
  /** Reference to the enclosing {@link Grid}  */
  private static Grid grid ;
  
  /** Needed to identify temp values  */
  private static int gridLength ;
  
  /** @see SqrTypes
    */
  private SqrTypes type = SqrTypes.OPEN ;
  
  /**
   *  the number I am displaying, if any <br>
   *  - ranges from {@link SqrTypes#BLANK_VAL} to <var>Grid.gridLength</var>
   *  @see Grid#getLength()
   */
  private int value = SqrTypes.BLANK_VAL ;
  
  /**
   *  One of the Solve methods determined that there is only ONE value I can have
   *  @see Grid#solveOneSqr
   */
  int solvedValue = SqrTypes.BLANK_VAL ;
  
  /** Do I have Keyboard Focus?  */
  private boolean active ;
  
  /** Do I have a temp {@link #value}?  */
  private boolean temp ;
  
  /** Did I get my {@link #value} from a Solve method?  */
  private boolean autoSolved ;
  
  /** Do I have a 'conflicting' <var>value</var>,
   *  i.e. one duplicated in another {@link Square} in one of my {@link Group}s?  */
  private boolean conflicting ;
  
  /** Do I have a 'wrong' <var>value</var> in a {@link Square},
   *  i.e. NOT the proper value for the solution?  */
  private boolean wrong ;
  
  /** Which {@link Row} am I in?  */
  private Row myRow ;
  /** Which {@link Col} am I in?  */
  private Col myCol ;
  /** Which {@link Zone} am I in? */
  private Zone myZone ;
  
  /** How many of each possible value are there in my 'Group Squares'? */
  private int[] nGrpSqrsWithVal ;
  
  /**
   *  How many possible values do I have?
   *  @see #possibleVals
   */
  private int nPossibleVals ;
  
  /**
   *  What are my possible values? <br>
   *  - uses bits as value indices
   *  @see #nGrpSqrsWithVal
   *  @see #excludeVals
   */
  private int possibleVals ;
  
  /** Values that have been excluded from my possibles by Solve techniques */
  private boolean[] excludeVals ;
  
  /** need to know if I am in a Color Chain  */
  private int chainColor ;
  
}// Class Square
