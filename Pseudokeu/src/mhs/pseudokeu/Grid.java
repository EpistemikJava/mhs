/* ***************************************************************************************
 
   Mark Sattolo (epistemik@gmail.com)
 -----------------------------------------------
 $File: //depot/Eclipse/Java/workspace/Pseudokeu/src/mhs/pseudokeu/Grid.java $
 $Revision: #14 $
 $Change: 174 $
 $DateTime: 2012/02/21 20:28:23 $
 -----------------------------------------------
 
  mhs.latinsquare.LatinSquareGrid.java
  Eclipse version created on Nov 30, 2007, 10:51 PM
  git version created Mar 8, 2014
 
*************************************************************************************** */

package mhs.pseudokeu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

import mhs.pseudokeu.Loader.SavedGame;

/**
 * The grid containing 9 {@link Col}umns, 9 {@link Row}s, and 9 {@link Zone}s, each filled with 9 {@link Square}s
 *
 * @author Mark Sattolo
 * @version 8.1.1
 * 
 * @see JPanel
 * @see MouseListener
 * @see KeyListener
 */
public class Grid extends JPanel implements MouseListener, KeyListener
{
  /*
   *            I N N E R    C L A S S E S
  *************************************************************************************************************/
  
  /**
   *  Handles the information needed to Undo & Redo value assignments to {@link Square}s
   *  
   *  @author Mark Sattolo
   *  @see #undoVector
   */
  class UndoMatrix
  {
    /** Position in {@link #undoVector}  */
    private int index ;
    
    /** Previous temp status  */
    private boolean oldTemp ;
    /** New temp status  */
    private boolean newTemp ;
    
    /**
     *  Position of the {@link Square} whose value was changed <br>
     *  x = row & y = col
     *  
     *  @see Point
     */
    private Point sqrLocation ;
    
    /**
     *  Previous and New values in the changed {@link Square} <br>
     *  x = old & y = new
     *  
     *  @see Point
     */
    private Point values ;
    
    /**
     *  CONSTRUCTOR - new {@link #sqrLocation} & {@link #values} are created ;
     *   - other fields are set from parameters <br>
     *   
     *  @param indx - {@link #index}
     *  @param old - {@link #oldTemp}
     *  @param newtmp - {@link #newTemp}
     */
    UndoMatrix( int indx, boolean old, boolean newtmp )
    {
      index = indx ;
      oldTemp = old ;
      newTemp = newtmp ;
      sqrLocation = new Point();
      values = new Point();
    }
    
    /** Reset all fields to default values  */
    void reset()
    {
      oldTemp = newTemp = false ;
      sqrLocation.x = sqrLocation.y = 0 ;
      values.x = values.y = 0 ;
    }
    
    /**
     *  Set {@link #sqrLocation}
     *  
     *  @param row - assigned to sqrLocation.x
     *  @param col - assigned to sqrLocation.y
     */
    void setLocation( int row, int col )
    {
      sqrLocation.x = row ;
      sqrLocation.y = col ;
    }
    
    /**
     *  Set {@link #values}
     *  
     *  @param oldVal - assigned to values.x
     *  @param newVal - assigned to values.y
     */
    void setValues( int oldVal, int newVal )
    {
      values.x = oldVal ;
      values.y = newVal ;
    }
    
    /** @return {@link #index}  */
    int getIndex()  { return index ; }
    /** @return <var>x</var> field of {@link #sqrLocation}  */
    int getRow()    { return sqrLocation.x ; }
    /** @return <var>y</var> field of {@link #sqrLocation}  */
    int getCol()    { return sqrLocation.y ; }
    /** @return <var>x</var> field of {@link #values}  */
    int getOldVal() { return values.x ; }
    /** @return <var>y</var> field of {@link #values}  */
    int getNewVal() { return values.y ; }
    
    /**
     *  @param old - true to get {@link #oldTemp} & false to get {@link #newTemp}
     *  @return {@link #oldTemp} OR {@link #newTemp}
     */
    boolean getTemp( boolean old )
    {
      if( old ) return oldTemp ;
       
      return newTemp ;
    }
    
    /**
     *  Show the fields of {@link UndoMatrix}
     *  
     *  @param level - {@link Level} to display at
     *  @param msg - additional info
     */
    void display( Level level, String msg )
    {
      logger.appendln( msg + "Sqr " + sqrs2dArray[sqrLocation.x][sqrLocation.y].strGridPosn()
                       + " ; Old Val = " + values.x + ( oldTemp ? "/T" : "" )
                       + " ; New Val = " + values.y + ( newTemp ? "/T" : "" ) );
      
      //logger.send( level );
    }
  
  }/* INNER CLASS UndoMatrix */
  
 //============================================================================================================
  
  /**
   *  Handles the information needed to use Color Chains for Solving
   *  
   *  @author Mark Sattolo
   *  
   *  @see Grid#colorChain
   *  @see Grid#findColorChainVal
   */
  class ColorChain
  {
    static final int MAX_SQRS = Launcher.INITIAL_GRID_LENGTH + 1 ;
    
    /** my index in {@link Grid#colorChain}[]  */
    private final int myColor ;
    
    /** keep track of duplicate Groups  */
    private boolean duplicate ;
    
    private boolean[] myRows ;
    private boolean[] myCols ;
    private boolean[] myZones ;
    
    /** store my Squares  */
    private Vector<Square> sqrs ;
    
    /**
     *  CONSTRUCTOR - create new row, col, zone, Square arrays
     *  @param clr - assign to {@link #myColor}
     */
    ColorChain( int clr )
    {
      if( (clr <= NO_COLOR) || (clr >=  NUM_COLOR_CHAINS) )
      {
        logger.warning( "BAD Color index received!" );
        myColor = 0 ;
      }
      else
        myColor = clr ;
      
      myRows = new boolean[ MAX_SQRS ] ;
      myCols = new boolean[ MAX_SQRS ] ;
      myZones = new boolean[ MAX_SQRS ] ;
      
      sqrs = new Vector<>( MAX_SQRS );
    }
    
    /** Reset all mutable fields to default values  */
    void reset()
    {
      duplicate = false ;
      
      for( int i=0; i < MAX_SQRS; i++ )
      {
        myRows[i] = false ;
        myCols[i] = false ;
        myZones[i] = false ;
      }
      
      sqrs.removeAllElements() ;
      
    }// ColorChain.reset()
    
    /** Add a {@link Square} to {@link #sqrs}
     * @param sqr - the Square
     * @return success or failure
     * @see Grid#buildColorChain
     */
    boolean add( Square sqr )
    {
      if( sqrs.size() >= MAX_SQRS )
      {
        logger.warning( "colorChain[" + chainName[myColor] + "]: FULL!" );
        return false ;
      }
      
      if( sqr.inColorChain() )
      {
        logger.warning( "Sqr " + sqr.strGridPosn() + " is ALREADY in A Color Chain!" );
        return false ;
      }
      
      logger.info( "colorChain[" + chainName[myColor] + "]: ADDING Sqr " + sqr.strGridPosn() );
      
      int currentRow  = sqr.getRowIndex();
      int currentCol  = sqr.getColIndex();
      int currentZone = sqr.getZoneIndex();
      
      if( myRows[currentRow] )
        duplicate = true ;
      else
        myRows[currentRow] = true ;
      
      if( myCols[currentCol] )
        duplicate = true ;
      else
        myCols[currentCol] = true ;
      
      if( myZones[currentZone] )
        duplicate = true ;
      else
        myZones[currentZone] = true ;
      
      sqr.putInChain( myColor );
      sqrs.add( sqr );
      
      return true ;
      
    }// ColorChain.add()
    
    /** @return {@link #duplicate} */
    boolean checkForDuplicateGrps() { return duplicate ; }
    
    /** Set EACH of my {@link Square}s to a single possible value
     *  @param v - value to set  */
    void setSqrsToPossibleVal( int v )
    {
      for( Square s : sqrs )
      {
        for( int u=1; u <= gridLength; u++ )
        {
          if( (u != v) && s.canBeVal(u) )
            s.removePossibleVal( u );
        }
      }
      
    }// ColorChain.setSqrsToVal()
    
    /** @return {@link #myColor}  */
    int getColor()  { return myColor ; }
    /** @return number of Squares  */
    int getSize()  { return sqrs.size() ; }
    
    /** @param i - index
     * @return {@link #myRows}[i]  */
    boolean getRow(int i) { return myRows[i] ; }
    /** @param i - index
     * @return {@link #myCols}[i]  */
    boolean getCol(int i) { return myCols[i] ; }
    /** @param i - index 
     * @return {@link #myZones}[i]  */
    boolean getZone(int i) { return myZones[i] ; }
    
    /**
     *  Show the fields of {@link ColorChain}
     *  @param level - {@link Level} to display at
     *  @param msg - additional info
     */
    void display( Level level, String msg )
    {
      logger.append( "\n colorChain[" + chainName[myColor] + "]: " + sqrs.size() + " Sqrs (" + msg + "): " );
      
      for( Square s : sqrs )
        logger.append( s.strGridPosn() + "  " );
      
      logger.append( "\n" );
      logger.send( level );
    }
    
  }/* INNER CLASS ColorChain */
 
 /*
  *             C O N S T R U C T O R S
  *************************************************************************************************************/
  
  /**
   *  USUAL Constructor <br>
   *  - called by {@link Launcher#createGrid}
   *  
   *  @param frame - reference to the enclosing {@link Launcher}
   */
  public Grid( final Launcher frame )
  {
    if( frame == null )
    {
      System.err.println( "Grid Constructor: passed a null Launcher!!??" );
      System.exit( this.hashCode() );
    }
    
    gameview = frame ;
    logger = Launcher.logger ;
    
    // TODO: offer choice of grid length... 16 Squares per side?
    gridLength = Launcher.INITIAL_GRID_LENGTH ;
    
    totalSqrs = gridLength * gridLength ;
    
    undoVector = new Vector<>( totalSqrs, gridLength );
    
    autoSolvedSqrs = new boolean[gridLength][gridLength];
    
    colorChain = new ColorChain[ NUM_COLOR_CHAINS ];
    
    zoneLength = (int)Math.round( Math.sqrt(gridLength) );
    logger.logInit( myname() + ".zoneLength = " + zoneLength );
    
    tempValue = SqrTypes.BLANK_VAL ;
    
    initSqrsAndGroups();
    
    initFonts();
    
    changeSize( gameview.getSqrLength() );
    
    initLocation();
    
    activate();
    
    LogControl.showLoggers();
    LogControl.checkLogging();
    
  }// Grid CONSTRUCTOR
  
 /*
  *              M E T H O D S
  *************************************************************************************************************/
 
 // ===========================================================================================================
 //                          I N I T I A L I Z A T I O N
 // ===========================================================================================================
 
  /**
   *  Initialization of the <code>Grid</code> {@link Row}s, {@link Col}s, {@link Zone}s & {@link Square}s <br>
   *  - called by {@link #Grid(Launcher)}
   */
  private final void initSqrsAndGroups()
  {
    logger.logInit();
    
    // create each Square in the Grid
    int i, j ;
    sqrs2dArray = new Square[ gridLength ][ gridLength ];
    for( i=0; i < gridLength; i++ )
      for( j=0; j < gridLength; j++ )
        sqrs2dArray[i][j] = new Square( this );
    
    // Create each Group - Group constructor will set the Group's references to its Squares
    zones = new Zone[ gridLength ];
     rows = new Row[ gridLength ];
     cols = new Col[ gridLength ];
    for( i=0; i < gridLength; i++ )
    {
      zones[i] = new Zone( this, i, (i/zoneLength)*zoneLength, (i%zoneLength)*zoneLength );
       rows[i] = new  Row( this, i, i, 0 );
       cols[i] = new  Col( this, i, 0, i );
    }
    
    // Now that the Groups are set, we can tell each Square which Groups it is a part of
    for( i=0; i < gridLength; i++ )
      for( j=0; j < gridLength; j++ )
        sqrs2dArray[i][j].setGroups( rows[i], cols[j], zones[zoneLength*(i/zoneLength) + (j/zoneLength)] );
    
  }// Grid.initSqrsAndGroups()
  
  /**
   *  Initialization of the {@link Grid}'s {@link Font}s<br>
   *  - called by {@link #Grid(Launcher)}
   */
  private final static void initFonts()
  {
    logger.logInit();
    
    Font_Sqr_Sm       = new Font( strREG_TYPEFACE, Font.BOLD  , Launcher.FONT_SIZE_SM );
    Font_Temp_Sm      = new Font( strREG_TYPEFACE, Font.BOLD  , Launcher.FONT_SIZE_XS - 6 );
    Font_Conflicts_Sm = new Font( strBIG_TYPEFACE, Font.PLAIN , Launcher.FONT_SIZE_XS );
    
    Font_Sqr_Md       = new Font( strREG_TYPEFACE, Font.BOLD  , Launcher.FONT_SIZE_MD );
    Font_Temp_Md      = new Font( strREG_TYPEFACE, Font.BOLD  , Launcher.FONT_SIZE_XS );
    Font_Conflicts_Md = new Font( strBIG_TYPEFACE, Font.PLAIN , Launcher.FONT_SIZE_SM );
    
    Font_Sqr_Lg       = new Font( strBIG_TYPEFACE, Font.BOLD  , Launcher.FONT_SIZE_LG );
    Font_Temp_Lg      = new Font( strREG_TYPEFACE, Font.BOLD  , Launcher.FONT_SIZE_SM );
    Font_Conflicts_Lg = new Font( strBIG_TYPEFACE, Font.PLAIN , Launcher.FONT_SIZE_MD );
    
  }// Grid.initFonts()
  
  /**
   *  Calculate the position to place the {@link Grid} in {@link Launcher#myLayeredPane} in {@link Launcher#myContentPane}<br>
   *  - called by {@link #Grid(Launcher)}
   *  
   *  @see java.awt.Component#setLocation
   */
  private final void initLocation()
  {
    // check the dimensions of the Game Frame
    logger.appendln( "Game Frame: width = " + gameview.getWidth() + " & height = " + gameview.getHeight() );
    
    // check the dimensions of the Layered Pane
    paneWidth  = gameview.myLayeredPane.getWidth();
    paneHeight = gameview.myLayeredPane.getHeight();
    logger.appendln( "Game Layered Pane: width = " + paneWidth + " & height = " + paneHeight );
    
    // check the dimensions of the Content Pane
    paneWidth  = gameview.myContentPane.getWidth();
    paneHeight = gameview.myContentPane.getHeight();
    logger.appendln( "Game Content Pane: width = " + paneWidth + " & height = " + paneHeight );
    
    // get the dimensions of the space available for the Grid
    int spaceHt = paneHeight - gameview.topEnclosingPanel.getHeight() - gameview.botBtnPanel.getHeight();
    logger.appendln( "Grid Space: width = " + paneWidth + " & height = " + spaceHt );
    
    // calculate the starting position to have the Grid centered
    xLocation = (paneWidth - pxSize)/2 ;
    yLocation = (spaceHt - pxSize)/2 ;
    logger.logInit( "Grid: xLocation = " + xLocation + " & yLocation = " + yLocation );
    
    // check the parent
    java.awt.Container parent = getParent();
    logger.appendln( "Grid: Parent is " + ( parent == null ? "null" : parent.toString() ) );
    
    // centre the Grid in the available space
    setLocation( xLocation, yLocation );
    
    logger.send( Level.FINE );
    
  }// Grid.initLocation()
  
  /**
   *  Add listeners and show the {@link Grid} <br>
   *  - called by {@link #Grid(Launcher)}
   */
  private final void activate()
  {
    logger.logInit();
    
    addMouseListener( this );
    addKeyListener( this );
    
    setBackground( Launcher.COLOR_GAME_BKGRND );
    setFocusable( true );
    setVisible( true );
  
  }// Grid.activate()
  
 // ===========================================================================================================
 //                             I N T E R F A C E
 // ===========================================================================================================
  
  /**
   * Load {@link #sqrs2dArray} with values from a {@link Loader.SavedGame} then get the solution from Solver<br>
   * - called by {@link Launcher#newGame}
   * 
   * @param loader - {@link Loader} to use
   * @param diff - level
   * @param sel - index of selected {@link Loader.SavedGame}, OR a value LT zero to obtain a random saved game
   * 
   * @return name of loaded game
   * @see #getSolutionGame
   */
  String activateGame( final Loader loader, final int diff, final int sel )
  {
    if( loader == null )
    {
      logger.severe( "Passed a null Loader!!??" );
      return null ;
    }
    
    logger.config( (new Date()).toString() );
    
    int $newval ;
    
    // get the requested game or choose a random game
    activeGameIndex = (sel < 0) ? (int)Math.round( Math.random() * (loader.getNumLoadedGames(diff) - 1) ) : sel ;
    
    activeGame = loader.getSavedGame( diff, activeGameIndex );
    if( activeGame == null )
    {
      logger.warning( "loader.getSavedGame() DID NOT WORK !! ??" );
      return null ;
    }
    
    // set the current game in the Grid
    for( int row=0; row < gridLength; row++ )
      for( int col=0; col < gridLength; col++ )
      {
        activeSqr = sqrs2dArray[row][col] ;
        activeSqr.setActive( false );
        
        $newval = activeGame.getValue( row, col );
        if( $newval != SqrTypes.BLANK_VAL )
        {
          activeSqr.newValue( $newval, false );
          activeSqr.setFixed();
        }
      }
    
    // clear all the Undo fields that were set - these are not valid for FIXED Squares
    clearUndo();
    
    activeSqr = null ;
    setDefaultSqr();
    
    gameview.updateSqrsMesg( numBlankSqrs );
    
    /* get the solution  */
    getSolutionGame( loader );
    
    return activeGame.getName();
  
  }// Grid.activateGame()
  
  /**
   *  See if the {@link Solver} thread has returned <br>
   *  
   *  @return done or not
   *  
   *  @see #createSolveWorker
   *  @see #savedSolnReady
   */
  boolean solveWorkerDone()
  {
    if( savedSolnReady )
      return true ;
    
    return false ;
    
  }// Grid.solveWorkerDone()
  
  /**
   *  Change the {@link Grid} size to accommodate a new {@link Square} size <br>
   *  - also adjusts the Font
   *  
   *  @param size - the new value
   *  
   *  @see Launcher.GameSizeFrame#confirm
   *  @see java.awt.Component#setSize
   *  @see javax.swing.JComponent#setFont
   */
  final void changeSize( final int size )
  {
    logger.info( "Current Sqr size = " + pxSqrSize + "; New Sqr size = " + size );
    
    if( size == pxSqrSize )
      return ;
    
    pxSqrSize = size ;
    pxSize = gridLength * pxSqrSize ;
    logger.info( "Grid size = " + pxSize + " x " + pxSize );
    
    setSize( pxSize, pxSize );
    
    // set appropriate fonts
    if( size == Launcher.SQUARE_SIZE_MD )
    {
         guessFont = Font_Sqr_Md ;
      conflictFont = Font_Conflicts_Md ;
          tempFont = Font_Temp_Md ;
    }
    else if( size == Launcher.SQUARE_SIZE_LG )
    {
         guessFont = Font_Sqr_Lg ;
      conflictFont = Font_Conflicts_Lg ;
          tempFont = Font_Temp_Lg ;
    }
    else // default: should only ever be SQUARE_SIZE_SM
    {
         guessFont = Font_Sqr_Sm ;
      conflictFont = Font_Conflicts_Sm ;
          tempFont = Font_Temp_Sm ;
    }
    
    setFont( guessFont );
    
  }// Grid.changeSize()
  
  /**
   *  Set the Sqr indices for each value in each Group <br>
   *  - called by {@link Square#newValue}
   *  
   *  @see Group#setSqrsCanBeVal
   */
  void setSqrsCanBeVal()
  {
    for( int i=0; i < gridLength; i++ )
    {
      zones[i].setSqrsCanBeVal();
       rows[i].setSqrsCanBeVal();
       cols[i].setSqrsCanBeVal();
    }
    
  }// Grid.setSqrsCanBeVal()
  
  /**
   *  modify {@link #numBlankSqrs}
   *  
   *  @param diff - the +ve or -ve amount to increment/decrement
   *  @see Square#adjustType
   */
  void incBlankCount( final int diff )
  {
    logger.config( numBlankSqrs + " >> " + (numBlankSqrs + diff) + " ; numConflicts == " + nConflicts );
    
    numBlankSqrs += diff ;
    
    gameview.updateSqrsMesg( numBlankSqrs );
  
  }// Grid.incBlankCount()
  
  /**
   *  Reset all {@link Grid} fields whose value depends on the current loaded game and user actions
   *  
   *  @see Launcher#reset
   */
  final void clear()
  {
    for( int i=0; i < gridLength; i++ )
    {
      zones[i].clear();
       rows[i].clear();
       cols[i].clear();
      
      // clear Squares and AutoSolve record
      for( int j=0; j < gridLength; j++ )
      {
        sqrs2dArray[i][j].clear();
        autoSolvedSqrs[i][j] = false ;
      }
    }
    
    if( activeGame != null ) 
      activeGame = null ;
    if( savedSolution != null )
      savedSolution = null ;
    
    defaultSqr = sqrs2dArray[0][0] ;
    
    numBlankSqrs = totalSqrs ;
    nConflicts = 0 ;
    
    tempValue = SqrTypes.BLANK_VAL ;
    tempMode = false ;
    
    clearUndo();
    savedSolnReady = false ;
    
    showKeyStrokes = showMouseActions = false ;
    
  }// Grid.clear()
  
  /**
   * Reverse the most recent value assignment to a {@link Square}
   * 
   * @see #addNewUndoEntry
   * @see Launcher#undoLastEntry
   */
  void undoLastValue()
  {
    if( undoPtr > 0 )
    {
      // decrement undoPtr
      undoPtr-- ;
      
      // set the previous Square to Active
      setActiveSqr( undoVector.elementAt(undoPtr).getRow(), undoVector.elementAt(undoPtr).getCol() );
      
      // set previous Square in UndoVector to Old value and mode
      getActiveSqr().newValue( undoVector.elementAt(undoPtr).getOldVal(), undoVector.elementAt(undoPtr).getTemp(true) );
      
      // discard any remaining "undone" entries if Square is Solved
      if( getActiveSqr().isAutoSolved() )
      {
        undoVector.setSize( totalEntries = undoPtr );
        gameview.incSolveCount( DECREASE );
        undoMode = false ;
      }
      else
          undoMode = true ;
      
      // enable Redo action unless was a Solved Square
      gameview.enableRedo( undoMode );
    }
    
    if( undoPtr == 0 )
      gameview.enableUndo( false );
    
    requestFocusInWindow();
    
    logger.info( "undoPtr = " + undoPtr + " & solvedSqr = " + (!undoMode) );
  
  }// Grid.undoLastValue()
  
  /**
   * Reverse the most recent UNDO action
   * 
   * @return success or failure
   * 
   * @see #undoLastValue
   * @see Launcher#redoLastUndoAction
   */
  boolean redoLastUndo()
  {
    // check if in Undo mode
    if( ! undoMode )
    {
      logger.info( "NOT ENABLED" );
      return false ;
    }
    
    // ensure the current entry is the Active Square
    setActiveSqr( undoVector.elementAt(undoPtr).getRow(), undoVector.elementAt(undoPtr).getCol() );
    
    // set this Square to the New value
    getActiveSqr().newValue( undoVector.elementAt(undoPtr).getNewVal(), undoVector.elementAt(undoPtr).getTemp(false) );
    
    // increment undoPtr and check Undo button
    if( ++undoPtr > 0 )
      gameview.enableUndo( true );
    
    // can continue until at last entry in Vector
    if( undoPtr == totalEntries )
    {
      undoMode = false ;
      gameview.enableRedo( undoMode );
    }
    
    logger.info( "Available to Redo: " + (totalEntries - undoPtr) );
    
    requestFocusInWindow();
    return true ;
    
  }// Grid.redoLastUndo()
  
  /**
   *  Find a new possible value in the current Grid and set it <br>
   *  - called by {@link Launcher#revealSquare}
   *  
   *  @see Launcher#toggleAutoSolve
   *  @see #findGrpSingle
   *  @see #findSqrSingle
   *  @see #findGridSingle
   *  @see #findLockedVals
   *  @see #findPairs
   *  @see #findTriples
   *  @see #findQuads
   *  @see #findRectads
   *  @see #findElad
   *  @see #findHexad
   *  @see #findTetrads
   *  @see #findColorChainVal
   *  @see #getValFromSolution
   */
  void solveOneSqr()
  {
    boolean
             $moreSolving = true  ,
            $foundLockVal = false ,
               $foundPair = false ,
             $foundTriple = false ,
               $foundQuad = false ,
             $foundRectad = false ,
               $foundElad = false ,
              $foundHexad = false ,
             $foundTetrad = false  ;
      // multi-LockedValues
      // multi-Colors
      // Octad (Jellyfish)
      // Forcing Chains
    
    tempMode = false ;
    
    while( $moreSolving )
    {
      // the easiest type of Solving
      // FIND a Square which has ONLY 1 possible value because ALL the other Squares
      // in one of its Groups CANNOT be that value
      // aka "Hidden Single"
      if( findGrpSingle() )
        return ;
      logger.info( "NO MORE Group Singles can be found in this game state." );
      
      // the other basic type of Solving
      // FIND a Square which can ONLY be 1 value because ALL the other 
      // possible values are represented in at least 1 of its Group Squares
      // aka "Naked Single"
      if( findSqrSingle() )
        return ;
      logger.info( "NO MORE Square Singles can be found in this game state." );
      
      // FIND a Value which is already in the Grid 8 times,
      // and so there is ONLY 1 possible Square for the final position
      if( findGridSingle() )
        return ;
      logger.info( "NO MORE Grid Singles can be found in this game state." );
      
      // a particular possible value in a zone is restricted to one row OR col 
      //  - thus that value can be eliminated as a possibility from the other Squares in that row or col
      // OR a particular possible value in a row or col is restricted to one zone
      //  - thus that value can be eliminated as a possibility from the other Squares in that zone
      // FIND locked values in Groups [aka "Locked Candidates"]
      // - if successful, try other Solution algorithms again
      if( ! $foundLockVal )
      {
        $foundLockVal = $moreSolving = findLockedVals();
        if( $foundLockVal )
        {
          $foundPair = $foundTriple = $foundQuad = $foundRectad = $foundElad = $foundHexad = $foundTetrad = false ;
          continue ;
        }
      }
      logger.info( "NO MORE Locked Vals can be found in this game state." );
      
      // find Pairs in Squares or Groups and remove excluded values from the appropriate Squares
      // - if successful, try other Solution algorithms again
      if( ! $foundPair )
      {
        $foundPair = $moreSolving = findPairs();
        if( $foundPair )
        {
          $foundLockVal = $foundTriple = $foundQuad = $foundRectad = $foundElad = $foundHexad = $foundTetrad = false ;
          continue ;
        }
      }
      logger.info( "NO MORE Pairs can be found in this game state." );
      
      // find Triples in Squares or Groups and remove excluded values from the appropriate Squares
      // - if successful, try other Solution algorithms again
      if( ! $foundTriple )
      {
        $foundTriple = $moreSolving = findTriples();
        if( $foundTriple )
        {
          $foundLockVal = $foundPair = $foundQuad = $foundRectad = $foundElad = $foundHexad = $foundTetrad = false ;
          continue ;
        }
      }
      logger.info( "NO MORE Triples can be found in this game state." );
      
      // find Rectad in Grid and remove possible vals from the appropriate Squares
      // - if successful, try other Solution algorithms again
      if( ! $foundRectad )
      {
        $foundRectad = $moreSolving = findRectads();
        if( $foundRectad )
        {
          $foundLockVal = $foundPair = $foundTriple = $foundQuad = $foundElad = $foundHexad = $foundTetrad = false ;
          continue ;
        }
      }
      logger.info( "NO MORE Rectads can be found in this game state." );
      
      // find a Tetrad in the Grid and remove possible vals from the appropriate Squares
      // - if successful, try other Solution algorithms again
      if( ! $foundTetrad )
      {
        $foundTetrad = $moreSolving = findTetrads();
        if( $foundTetrad )
        {
          $foundLockVal = $foundPair = $foundTriple = $foundQuad = $foundRectad = $foundElad = $foundHexad = false ;
          continue ;
        }
      }
      logger.info( "NO MORE Tetrads can be found in this game state." );
      
      // find Quads in Squares or Groups and remove excluded values from the appropriate Squares
      // - if successful, try other Solution algorithms again
      if( ! $foundQuad )
      {
        $foundQuad = $moreSolving = findQuads();
        if( $foundQuad )
        {
          $foundLockVal = $foundPair = $foundTriple = $foundRectad = $foundElad = $foundHexad = $foundTetrad = false ;
          continue ;
        }
      }
      logger.info( "NO MORE Quads can be found in this game state." );
      
      // find Elad in Grid and remove possible vals from the appropriate Squares
      // - if successful, try other Solution algorithms again
      if( ! $foundElad )
      {
        $foundElad = $moreSolving = findElad();
        if( $foundElad )
        {
          $foundLockVal = $foundPair = $foundTriple = $foundQuad = $foundRectad = $foundHexad = $foundTetrad = false ;
          continue ;
        }
      }
      logger.info( "NO MORE Elads can be found in this game state." );
      
      // find Hexad in Grid and remove possible vals from the appropriate Squares
      // - if successful, try other Solution algorithms again
      if( ! $foundHexad )
      {
        $foundHexad = $moreSolving = findHexad();
        if( $foundHexad )
        {
          $foundLockVal = $foundPair = $foundTriple = $foundQuad = $foundRectad = $foundElad = $foundTetrad = false ;
          continue ;
        }
      }
      logger.info( "NO MORE Hexads can be found in this game state." );
      
      // find Color Chains in the Grid and remove possible vals from the appropriate Squares
      // - ALWAYS check for new color chains if reach this point in the solution set
      if( findColorChainVal() )
      {
        $moreSolving = true ;
        $foundLockVal = $foundPair = $foundTriple = $foundQuad 
        = $foundRectad = $foundElad = $foundHexad = $foundTetrad = false ;
        continue ;
      }
      logger.info( "NO MORE Color Chain Values can be found in this game state." );
      
      /* get a new value directly from the Solution SavedGame  */
      if( getValFromSolution() )
        return ;
      
    }// end while
    
    // NO MORE Solving to be done - turn OFF Solve
    gameview.toggleAutoSolve();
    
  }// Grid.solveOneSqr()
  
  /**
   *  Find and show a WRONG guess, if any, in the current Grid
   *  
   *  @return success or failure
   *  @see Launcher#revealWrongGuess
   */
  boolean findWrongGuess()
  {
    // do NOT have a proper Solution...
    if( ! savedSolnGood )
    {
      logger.warning( "Solution is NOT GOOD!" );
      return false ;
    }
    
    logger.info( "Reveal a WRONG guess, if any" );
    
    Square $target ;
    int $soln ;
    for( int i=0; i < gridLength; i++ )
      for( int j=0; j < gridLength; j++ )
      {
        $target = sqrs2dArray[i][j] ;
        if( ! $target.isFixed() )
        {
          $soln = savedSolution.getValue( i, j );
          if( $target.isGuess() && ($target.getValue() != $soln) )
          {
            setActiveSqr( $target.getRowIndex(), $target.getColIndex() );
            logger.info( "Found a WRONG guess at " + $target.strGridPosn() );
            getActiveSqr().setWrong( true );
            
            requestFocusInWindow();
            return true ;
          }
        }
      }
    
    return false ;
    
  }// Grid.findWrongGuess()
  
  /**
   *  Any {@link Square}s with conflicting <var>values</var>?<br>
   *  
   *  @return boolean indicating if {@link #nConflicts} is GT zero
   *  
   *  @see #incConflicts
   *  @see Launcher#setConflicts
   *  @see Square#adjustConflict
   */
  boolean hasConflicts()
  {
    logger.fine( "numConflicts == " + nConflicts );
    return( nConflicts > 0 );
  
  }// Grid.hasConflicts()
  
  /** 
   *  Increment (or decrement) {@link #nConflicts}
   *  
   *  @param inc - the amount to increment, +ve or -ve 
   *  @param sqr - {@link Square} that made the call
   *  
   *  @see #hasConflicts
   */
  void incConflicts( final int inc, final Square sqr )
  {
    if( sqr == null )
    {
      logger.severe( "Passed a null Square!!??" );
      return ;
    }
    
    nConflicts += inc ;
    logger.severe( ( inc >= INCREASE ? "INCREASE" : "DECREASE" ) + " to '" + nConflicts + "' for Sqr " + sqr.strGridPosn() );
    
    gameview.setConflicts( false );
  
  }// Grid.incConflicts()
  
  /** @return int {@link #gridLength}  */
  int getLength()
  { return gridLength; }
  
  /** @return number of {@link Square}s with values (FIXED or GUESS)  */
  int getNumValues()
  { return( totalSqrs - numBlankSqrs ); }
  
  /** @return {@link #totalSqrs}  */
  int getTotalSqrs()
  { return totalSqrs ;}

  /** @return {@link #zoneLength}  */
  int getZoneLength()
  { return zoneLength ;}

  /**
   *  Check if {@link #activeSqr} is valid and return it, 
   *  or {@link #defaultSqr} if <em>activeSqr</em> is <b>INVALID</b>
   *  
   *  @return {@link Square} {@link #activeSqr} OR {@link #defaultSqr}
   *  @see #setDefaultSqr
   */
  Square getActiveSqr()
  {
    if( activeSqr == null )
    {
      logger.log( showMouseActions ? Level.SEVERE : Level.FINE, "Active Sqr is NULL !" );
      
      return defaultSqr ;
    }
    
    return activeSqr ;
    
  }// Grid.getActiveSqr()
  
  /** @return {@link #undoMode}  */
  boolean inUndoMode()
  { return undoMode ; }
  
  /** @return boolean indicating if {@link #totalEntries} GT zero  */
  boolean hasEntries()
  { return( totalEntries > 0 ); }
  
  /** @return boolean indicating if any ACTIVE entries, i.e. NOT undone  */
  boolean hasActiveEntries()
  { return( (totalEntries > 0) && (undoPtr > 0) ); }
  
  /** @return active {@link Col}  */
  Col activeCol()
  { return getActiveSqr().getCol(); }
  
  /** @return active {@link Row}  */
  Row activeRow()
  { return getActiveSqr().getRow(); }
  
  /** @return active {@link Zone}  */
  Zone activeZone()
  { return getActiveSqr().getZone(); }
 
 // end INTERFACE
 // ===========================================================================================================
 //                              I N P U T
 // ===========================================================================================================
  
  /**
   *  Check key input for an Arrow key press <br>
   *  - called by {@link #keyPressed}
   *  
   *  @param keycode - the input
   *  @return success or failure
   */
  private boolean processArrowKeys( final int keycode )
  {
    int $activeRow = activeSqr.getRowIndex(),
        $activeCol = activeSqr.getColIndex();
    int $newRow = $activeRow,
        $newCol = $activeCol ;
    
    switch( keycode )
    {
      case KeyEvent.VK_UP:
        do { $newRow = ( ($newRow == 0) ? gridLength-1 : $newRow-1 ); }
          while( sqrs2dArray[$newRow][$newCol].isFixed() );
        break ;
      case KeyEvent.VK_DOWN:
        do{ $newRow = ( ($newRow == gridLength-1) ? 0 : $newRow+1 ); }
          while( sqrs2dArray[$newRow][$newCol].isFixed() );
        break ;
      case KeyEvent.VK_LEFT:
        do{ $newCol = ( ($newCol == 0) ? gridLength-1 : $newCol-1 ); }
          while( sqrs2dArray[$newRow][$newCol].isFixed() );
        break ;
      case KeyEvent.VK_RIGHT:
        do{ $newCol = ( ($newCol == gridLength-1) ? 0 : $newCol+1 ); }
          while( sqrs2dArray[$newRow][$newCol].isFixed() );
    }
    
    if( ($newRow != $activeRow) || ($newCol != $activeCol) )
    {
      setActiveSqr( $newRow, $newCol );

      // clear previous temp value
      tempValue = SqrTypes.BLANK_VAL ;

      logger.finer( KeyEvent.getKeyText( keycode ) + ": New Sqr == " + activeSqr.strGridPosn() );
      return true ;
    }
    
    return false ;
    
  }// Grid.processArrowKeys()
  
  /**
   *  Process a new {@link Square} input event <br>
   *  - called by {@link #keyPressed} or {@link #keyTyped}
   *  
   *  @param kevt - the input event
   *  @see #newValue
   */
  private void processInput( final KeyEvent kevt )
  {
    if( kevt == null )
    {
      logger.severe( "Passed a null KeyEvent!!??" );
      return ;
    }
    
    logger.finer( "Event = " + kevt.toString() );
    
    // seems to work, but getKeyChar() is supposedly NOT reliable in KeyEvents from keyPressed() ?
    char   $input = kevt.getKeyChar();
    int $intInput = $input ;
    
    if( showKeyStrokes )
      logger.severe( "Key typed = '" + $input + "'; int value = '" + $intInput
                     + "' // Temp is" + (tempMode ? " " : " NOT ") + "Active" );
    
    newValue( $intInput - BASE_KEYCODE_INDEX );
    
  }// Grid.processInput()
  
  /**
   *  Process a new {@link Square} value <br>
   *  - called by {@link #processInput} or {@link #setAutoSolvedSqr}
   *  
   *  @param newval - the new value
   *  @return success or failure
   *  
   *  @see #handleTempMode
   *  @see #addNewUndoEntry
   *  @see Square#newValue
   */
  private boolean newValue( final int newval )
  {
    // make sure we have an appropriate Square
    if( getActiveSqr().isFixed() )
      return false ;
    
    /*
       make sure we have a proper value
       e.g. If the Grid has focus, then menu keystrokes like Alt-x, Alt-h, etc
            end up here with inappropriate values like 72, 56 ...
       - just ignore any of these
    */
    if( (newval < SqrTypes.BLANK_VAL) || (newval > gridLength) )
    {
      logger.warning( "* New value '" + newval + "' OUT OF RANGE *" );
      return false ;
    }
    
    // got a digit & this is NOT a Fixed Square
    logger.info( "New Square value == " + newval );
    
    int $val = newval ;
    
    // handle temp mode - keep track of up to 3 digits
    if( tempMode )
      $val = handleTempMode( $val );
    else
        tempValue = SqrTypes.BLANK_VAL ;// reset
    
    // keep track of assignments for the Undo/Redo functionality
    addNewUndoEntry( $val );
    
    /* PROCESS THE ENTRY */
    getActiveSqr().newValue( $val, tempMode );
    
    // Game over?
    if( (numBlankSqrs == 0) && (nConflicts == 0) )
    {
      getActiveSqr().setActive( false );
      gameview.gameOver();
    }
    
    repaint();
    
    return true ;
    
  }// Grid.newValue()
  
  /**
   * Handle the multiple digits that can be displayed by a {@link Square} in 'temp' mode
   * 
   * @param inVal - new digit typed in a {@link Square}
   * @return up to {@link SqrTypes#MAX_TEMP_VAL}
   * 
   * @see #newValue
   */
  private int handleTempMode( final int inVal )
  {
    int $base = gridLength + 1 ;
    int $outVal = inVal ;
    
    if( inVal == SqrTypes.BLANK_VAL )
    {
      tempValue = SqrTypes.BLANK_VAL ; // start over with next temp digit
    }
    else
    {
      if( tempValue == SqrTypes.BLANK_VAL ) // i.e. first temp digit
        tempValue = inVal ;
      else if( tempValue >= ($base * $base) ) // already 3 digits - replace the 3rd digit
          tempValue = ( (tempValue/$base) * $base ) + inVal ;
        else // already have 1 or 2 digits - add next digit to the end
            tempValue = ( tempValue * $base ) + inVal ;
      
      $outVal = tempValue ;
    }
    
    logger.info( "Temp value is now '" + $outVal + "'" );
    
    if( $outVal > SqrTypes.MAX_TEMP_VAL )
    {
      logger.warning( "Temp Val exceeded Max!" );
      $outVal = SqrTypes.MAX_TEMP_VAL ;
    }
    
    return $outVal ;
  
  }// Grid.handleTempMode()
  
  /**
   * Keep track of value assignments in order to support Undo & Redo actions
   * 
   * @param newVal - new value of the Active Square
   * 
   * @see #undoVector
   * @see #newValue
   */
  private void addNewUndoEntry( final int newVal )
  {
    /* CANNOT call this method from Square.newValue() as Undo & Redo use that method for their actions...
     * Have to CHECK the old & new values/temp modes the same as is done in Square.newValue() so 
     * that Undo Vector entries will get Undone & Redone PROPERLY     */
    
    Square $activeSqr = getActiveSqr();
    int $oldVal = $activeSqr.getValue();
    boolean $tempState = $activeSqr.isTemp();
    
    // DO NOTHING if old & new value are both Blank OR if old & new value/mode are both the SAME
    if( ( (newVal == SqrTypes.BLANK_VAL) && ($oldVal == SqrTypes.BLANK_VAL) )
        || ( (newVal == $oldVal) && ($tempState == tempMode) ) )
    {
      logger.fine( "DO NOTHING... current val = " + $oldVal + ( $tempState ? "/T" : "" )
                   + " & new val = " + newVal + ( tempMode ? "/T" : "" ) + " // current type = " + $activeSqr.getType() );
      return ;
    }
    
    // discard any remaining "undone" entries if type in a new value
    if( undoMode )
    {
      undoVector.setSize( undoPtr );
      undoMode = false ;
    }
    
    undoVector.add( new UndoMatrix(undoPtr, $activeSqr.isTemp(), (newVal == SqrTypes.BLANK_VAL) ? false : tempMode) );
    undoVector.elementAt( undoPtr ).setLocation( $activeSqr.getRowIndex(), $activeSqr.getColIndex() );
    undoVector.elementAt( undoPtr ).setValues( $oldVal, newVal );
    
    undoPtr++ ;
    totalEntries = undoPtr ;
    
    gameview.enableAutoSolve( totalEntries >= 1 );
    gameview.enableUndo( true );
    gameview.enableRedo( false );
    
    logger.config( "totalEntries == " + totalEntries );
  
  }// Grid.addNewUndoEntry()
  
 // ===========================================================================================================
 //                              S O L V I N G
 // ===========================================================================================================
  
  /**
   *  Use the {@link Loader} to create a new SavedGame and start the {@link #solveWorker} thread <br>
   *  - called by {@link #activateGame}
   *  
   *  @param loader - to load games
   *  
   *  @return success or failure
   *  @see #createSolveWorker
   */
  private final boolean getSolutionGame( final Loader loader )
  {
    if( loader == null )
    {
      logger.severe( "Passed a null Loader!!??" );
      return false ;
    }

    if( activeGame == null )
    {
      logger.warning( "Active SavedGame not constructed!" );
      return false ;
    }
    
    savedSolution = loader.new SavedGame( activeGame.getLength(), "SOLUTION_" + activeGame.getName(),
                                          activeGame.getDifficulty() );
    
    if( savedSolution == null )
    {
      logger.warning( "Solution SavedGame not constructed!" );
      return false ;
    }
    
    logger.info( savedSolution.getName() );
    
    // it may take several seconds, so get the solution in a separate thread 
    createSolveWorker();
    solveWorker.execute();
    
    return true ;
  
  }// Grid.getSolutionGame()
  
  /**
   *  Call {@link Loader} to load games from file or jar to {@link Loader.SavedGame}s <br>
   *  - called by {@link #activateGame}
   *  
   *  @see Solver#getSolution
   *  @see #solveWorker
   *  @see #savedSolnReady
   */
  private final void createSolveWorker()
  {
    logger.logInit();
    
    /* get the solution  */
    solveWorker = new SwingWorker<Long, Void>()
    {
      @Override
      public Long doInBackground()
      {
        savedSolnTime = Long.valueOf( Solver.getSolution(activeGame, savedSolution) );
        return savedSolnTime ;
      }
      
      @Override
      public void done()
      {
        savedSolnGood = savedSolnTime > 0 ;
        savedSolnReady = true ;
      }
      
    };// new SwingWorker
    
  }// Grid.createSolveWorker()
  
  /**
   *  Find a Group that has a missing value which can ONLY go in 1 of its open Squares,
   *  as the other Squares in this Group have that value in a Group Square,
   *  then set this Square to that value [aka "Hidden Single"]<br>
   *  - called by {@link #solveOneSqr}
   *  
   *  @return success or failure
   *  @see Group#findSingleSqrForVal
   */
  private boolean findGrpSingle()
  {
    logger.info( myname() );
    
    Square $sqr ;
    
    for( int i=0; i < gridLength; i++ )
    {
      $sqr = zones[i].findSingleSqrForVal();
      if( $sqr == null )
      {
        $sqr = rows[i].findSingleSqrForVal();
        if( $sqr == null )
          $sqr = cols[i].findSingleSqrForVal();
      }
      
      if( $sqr != null )
        return setAutoSolvedSqr( $sqr );
      
    }// for( groups )
    
    return false ;
    
  }// Grid.findGrpSingle()
  
  /**
   *  Find a Square that has only 1 possible value because of the values
   *  in Group Squares and set this Square to that value [aka "Naked Single"]<br>
   *  - called by {@link #solveOneSqr}
   *  
   *  @return success or failure
   *  @see Square#findSingleVal
   */
  private boolean findSqrSingle()
  {
    logger.info( myname() );
    
    for( Square[] r : sqrs2dArray )
      for( Square s : r )
        if( s.findSingleVal() )
          return setAutoSolvedSqr( s );
    
    return false ;
    
  }// Grid.findSqrSingle()
  
  /**
   *  Find a value that is already in (gridLength - 1) Grid locations, so there is only 1 possible
   *  Square remaining which can have the last token of that value <br>
   *  - called by {@link #solveOneSqr}
   *  
   *  @return success or failure
   */
  private boolean findGridSingle()
  {
    logger.info( myname() );
    
    int $count, $posn=0 ;
    for( int v=1; v <= gridLength; v++ )
    {
      $count = 0 ;
      for( Row r : rows )
        if( r.nSqrsWithVal[v] == 0 )
          $posn = r.getPosn() ;
        else
            $count++ ;
      
      if( $count == gridLength - 1 )
      {
        logger.info( ">> Value " + v + " is in ALL rows except row[" + $posn + "]" );
        for( Square s : sqrs2dArray[$posn] )
          if( s.isOpen() && (s.numGrpSqrsWithVal(v) == 0) )
          {
            s.solvedValue = v ;
            return setAutoSolvedSqr( s );
          }
      }
    }
    
    return false ;
    
  }// Grid.findGridSingle()
  
  /**
   *  Find any locked values [aka "Locked Candidates"] in Groups and then re-check for any Solvable Squares <br>
   *  - called by {@link #solveOneSqr} OR {@link Launcher#setGridDebugKeyMap} Ctrl-V keystroke
   *  
   *  @return success or failure
   *  @see Group#findLockedVals
   */
  boolean findLockedVals()
  {
    boolean $result=false, $resZone=false, $resRow=false, $resCol=false ;
    
    for( int i=0; i < gridLength; i++ )
    {
      $resZone = zones[i].findLockedVals();
      $resRow  =  rows[i].findLockedVals();
      $resCol  =  cols[i].findLockedVals();
      
      if( ! $result )
        $result = ( $resZone || $resRow || $resCol );
    }
    
    //logger.info( "result == " + result );
    return $result ;
    
  }// Grid.findLockedVals()
  
  /**
   *  Find any Square or Group Pairs and then re-check for any Solvable Squares <br>
   *  - called by {@link #solveOneSqr}
   *  
   *  @return success or failure
   */
  private boolean findPairs()
  {
    boolean $zsp, $rsp, $csp, $zgp, $rgp, $cgp, $result=false ;
    
    for( int i=0; i < gridLength; i++ )
    {
      $zsp = zones[i].findSqrPairs();
      $rsp =  rows[i].findSqrPairs();
      $csp =  cols[i].findSqrPairs();
      
      $zgp = zones[i].findGrpPairs();
      $rgp =  rows[i].findGrpPairs();
      $cgp =  cols[i].findGrpPairs();
      
      if( ! $result )
        $result = ( $zsp || $rsp || $csp || $zgp || $rgp || $cgp );
    }
    
    return $result ;
    
  }// Grid.findPairs()
  
  /**
   *  Find any Square or Group Triples and then re-check for any Solvable Squares <br>
   *  - called by {@link #solveOneSqr}
   *  
   *  @return success or failure
   */
  private boolean findTriples()
  {
    boolean $zst, $rst, $cst, $zgt, $rgt, $cgt, $result=false ;
    
    for( int i=0; i < gridLength; i++ )
    {
      $zst = zones[i].findSqrTriples();
      $rst =  rows[i].findSqrTriples();
      $cst =  cols[i].findSqrTriples();
      
      $zgt = zones[i].findGrpTriples();
      $rgt =  rows[i].findGrpTriples();
      $cgt =  cols[i].findGrpTriples();
      
      if( ! $result )
        $result = ( $zst || $rst || $cst || $zgt || $rgt || $cgt );
    }
    
    return $result ;
    
  }// Grid.findTriples()
  
  /**
   *  Find any Square or Group Quads and then re-check for any Solvable Squares <br>
   *  - called by {@link #solveOneSqr}
   *  
   *  @return success or failure
   */
  private boolean findQuads()
  {
    boolean $zsqd, $rsqd, $csqd, $zgqd, $rgqd, $cgqd, $result=false ;
    
    for( int i=0; i < gridLength; i++ )
    {
      $zsqd = zones[i].findSqrQuads();
      $rsqd =  rows[i].findSqrQuads();
      $csqd =  cols[i].findSqrQuads();
      
      $zgqd = zones[i].findGrpQuads();
      $rgqd =  rows[i].findGrpQuads();
      $cgqd =  cols[i].findGrpQuads();
      
      if( ! $result )
        $result = ( $zsqd || $rsqd || $csqd || $zgqd || $rgqd || $cgqd );
    }
    
    return $result ;
    
  }// Grid.findQuads()
  
  /**
   *  Two rows OR cols each have only 2 Sqrs where a certain value can go,
   *  and these 2 Sqrs are also in matching cols/rows,
   *  thus can REMOVE that value as a possibility from ALL other open Sqrs
   *  in the matching cols/rows [aka "X-wing"]<br>
   *  
   *  <b>Find</b> any Rectads in the Grid and process the values.<br>
   *  - called by {@link #solveOneSqr}
   *  
   *  @return success or failure
   */
  private boolean findRectads()
  {
    int $g1, $g2 ;
    boolean $res1, $res2, $result=false ;
    
    for( int v=1; v <= gridLength; v++ )
    {
      for( int i=0; i < gridLength; i++ )
      {
        logger.append( "Check val '" + v + "' in row & col [" + i + "]" );
        
        // CHECK ROWS
        if( rows[i].nSqrsCanBeVal[v] == 2 )
        {
          for( int j=i+1; j < gridLength ; j++ )
            if( rows[j].sqrsCanBeVal[v] == rows[i].sqrsCanBeVal[v] )
            {
              $g1 = Helper.getBitPosn( rows[i].sqrsCanBeVal[v], 1 );
              $g2 = Helper.getBitPosn( rows[i].sqrsCanBeVal[v], 2 );
              
              logger.appendln( "\n\t FOUND a rectad in rows " + i + "," + j + " at cols " + $g1 + "," + $g2 );
              
              $res1 = cols[$g1].processRectadVal( v, i, j );
              $res2 = cols[$g2].processRectadVal( v, i, j );
              
              if( ! $result )
                $result = $res1 || $res2 ;
            }
        }// rows
        
        // CHECK COLS
        if( cols[i].nSqrsCanBeVal[v] == 2 )
        {
          for( int k=i+1; k < gridLength ; k++ )
            if( cols[k].sqrsCanBeVal[v] == cols[i].sqrsCanBeVal[v] )
            {
              $g1 = Helper.getBitPosn( cols[i].sqrsCanBeVal[v], 1 );
              $g2 = Helper.getBitPosn( cols[i].sqrsCanBeVal[v], 2 );
              
              logger.appendln( "\n\t FOUND a rectad in cols " + i + "," + k + " at rows " + $g1 + "," + $g2 );
              
              $res1 = rows[$g1].processRectadVal( v, i, k );
              $res2 = rows[$g2].processRectadVal( v, i, k );
              
              if( ! $result )
                $result = $res1 || $res2 ;
            }
        }// cols
        
        logger.send( Level.INFO );
        
      }// initial row/col
    }// values
    
    return $result ;
    
  }// Grid.findRectads()
  
  /**
   *  A Group contains 2 Sqrs with Pairs in the x,y + y,z pattern,
   *  and in a separate Group of one of these Sqrs is a Sqr with an x,z Pair,
   *  thus can REMOVE the x possibility from any open Sqr at the intersection row/cols
   *  of the x,y and x,z Sqrs [aka "XY-wing"]<br>
   *  
   *  <b>Find</b> an Elad in the Grid and process the value.<br>
   *  - called by {@link #solveOneSqr}
   *  
   *  @return success or failure
   */
  private boolean findElad()
  {
    int $bits2, $bits3, $res1, $res2, $val=0 ;
     Row $row ;
     Col $col ;
    Zone $zone ;
    Square $target = null ;
    boolean $result = false ;
    
    logger.info( "\n Checking..." );
    
    // Search all Squares for possVals == 2, e.g. 'ab'
    loop:
    for( Square[] r : sqrs2dArray )
      for( Square $s1 : r )
        if( $s1.numPossibleVals() == 2 )
        {
          logger.appendln( "Check possible Sqr " + $s1.strGridPosn()
                           + " / PV == " + Helper.displaySetBits($s1.getPossibleVals(), gridLength, "") );
          // if found, search group squares for possVals == 'ac' OR 'bc'
           $row = $s1.getRow();
           $col = $s1.getCol();
          $zone = $s1.getZone();
          
          // CHECK ROW FOR SQR 2
          for( Square $s2r : $row.mySqrs )
            if( ($s2r.numPossibleVals() == 2) && ($s2r != $s1) )
            {
              logger.appendln( "\t Check possible Row Sqr2 " + $s2r.strGridPosn()
                               + " / PV == " + Helper.displaySetBits($s2r.getPossibleVals(), gridLength, "") );
              $res1 = $s1.getPossibleVals() | $s2r.getPossibleVals() ;
              if( Helper.numSetBits($res1, gridLength) == 3 )
              {
                $bits2 = $s2r.getPossibleVals() ^ $res1 ;
                $bits3 =  $s1.getPossibleVals() ^ $res1 ;
                $res2 = $bits2 | $bits3 ;
                $val = Helper.getBitPosn( $bits3, 1 );
                
                // if found, e.g. 'ac', search the 2 non-'ac' groups for 'bc'
                // check col for Sqr 3
                for( Square $s3c : $col.mySqrs )
                  if( ($s3c.getPossibleVals() == $res2) && ($s3c != $s1) )
                  {
                    logger.appendln( "\t\t ! FOUND Col Sqr3 " + $s3c.strGridPosn()
                                     + " / PV == " + Helper.displaySetBits($s3c.getPossibleVals(), gridLength, "") );
                    $target = sqrs2dArray[$s3c.getRowIndex()][$s2r.getColIndex()];
                    break loop ;
                  }
                // check zone for Sqr 3
                for( Square $s3z : $zone.mySqrs )
                  if( ($s3z.getPossibleVals() == $res2) && ($s3z.getRow() != $row) )
                  {
                    logger.appendln( "\t\t ! FOUND Zone Sqr3 " + $s3z.strGridPosn()
                                     + " / PV == " + Helper.displaySetBits($s3z.getPossibleVals(), gridLength, "") );
                    $target = sqrs2dArray[$s3z.getRowIndex()][$s2r.getColIndex()];
                    break loop ;
                  }
              }
            }
          
          // CHECK COL FOR SQR 2
          for( Square $s2c : $col.mySqrs )
            if( ($s2c.numPossibleVals() == 2) && ($s2c != $s1) )
            {
              logger.appendln( "\t Check possible Col Sqr2 " + $s2c.strGridPosn()
                               + " / PV == " + Helper.displaySetBits($s2c.getPossibleVals(), gridLength, "") );
              $res1 = $s1.getPossibleVals() | $s2c.getPossibleVals() ;
              if( Helper.numSetBits($res1, gridLength) == 3 )
              {
                $bits2 = $s2c.getPossibleVals() ^ $res1 ;
                $bits3 =  $s1.getPossibleVals() ^ $res1 ;
                $res2 = $bits2 | $bits3 ;
                $val = Helper.getBitPosn( $bits3, 1 );
                
                // if found, e.g. 'ac', search the 2 non-'ac' groups for 'bc'
                // check row for Sqr 3
                for( Square $s3r : $row.mySqrs )
                  if( ($s3r.getPossibleVals() == $res2) && ($s3r != $s1) )
                  {
                    logger.appendln( "\t\t ! FOUND Row Sqr3 " + $s3r.strGridPosn()
                                     + " / PV == " + Helper.displaySetBits($s3r.getPossibleVals(), gridLength, "") );
                    $target = sqrs2dArray[$s2c.getRowIndex()][$s3r.getColIndex()];
                    break loop ;
                  }
                // check zone for Sqr 3
                for( Square $s3z : $zone.mySqrs )
                  if( ($s3z.getPossibleVals() == $res2) && ($s3z.getCol() != $col) )
                  {
                    logger.appendln( "\t\t ! FOUND Zone Sqr3 " + $s3z.strGridPosn()
                                     + " / PV == " + Helper.displaySetBits($s3z.getPossibleVals(), gridLength, "") );
                    $target = sqrs2dArray[$s2c.getRowIndex()][$s3z.getColIndex()];
                    break loop ;
                  }
              }
            }
          
        }// found a possible Square
    // end loop
    
    // if found, can eliminate possible val 'c' in the Square at the row/col intersection
    //           of 'ac' and 'bc' that does NOT share a row OR col with 'ab'
    if( $target != null )
    {
      logger.appendln( "\t\t Remove possible Val '" + $val + "' from Sqr " + $target.strGridPosn() );
      $result = $target.removePossibleVal( $val );
    }
    
    logger.send( Level.INFO );
    return $result ;
    
  }// Grid.findElad()
  
  /**
   *  Three rows OR cols each have a Pair or Triple for a particular value,
   *  and these overlap in the SAME three cols/rows,
   *  thus can REMOVE ALL instances of this value in the other open Sqrs 
   *  of the overlapping cols/rows [aka "Swordfish"]<br>
   *  
   *  <b>Find</b> a Hexad in the Grid and process the value.<br>
   *  - called by {@link #solveOneSqr}
   *  
   *  @return success or failure
   */
  private boolean findHexad()
  {
    int $res1, $res2 ;
    int $g1, $g2, $g3 ;
    boolean $tmpRes1, $tmpRes2, $tmpRes3, $result=false ;
    
    loop:
    for( int v=1; v <= gridLength; v++ )
    {
      logger.appendln( "Check val '" + v + "'" );
      for( int i=0; i < gridLength; i++ )
      {
        logger.appendln( "\tin row & col [" + i + "]" );
        
        // CHECK ROWS
        if( (rows[i].nSqrsCanBeVal[v] == 2) || (rows[i].nSqrsCanBeVal[v] == 3) )
        {
          for( int j=i+1; j < gridLength; j++ )
            if( (rows[j].nSqrsCanBeVal[v] == 2) || (rows[j].nSqrsCanBeVal[v] == 3) )
            {
              $res1 = rows[i].sqrsCanBeVal[v] | rows[j].sqrsCanBeVal[v] ;
              
              // make sure we have exactly 3 Squares
              if( Helper.numSetBits($res1, gridLength) != 3 )
                continue ;
              
              for( int k=j+1; k < gridLength; k++ )
                if( (rows[k].nSqrsCanBeVal[v] == 2) || (rows[k].nSqrsCanBeVal[v] == 3) )
                {
                  $res2 = $res1 | rows[k].sqrsCanBeVal[v] ;
                  if( $res2 == $res1 )
                  {
                    $g1 = Helper.getBitPosn( $res1, 1 );
                    $g2 = Helper.getBitPosn( $res1, 2 );
                    $g3 = Helper.getBitPosn( $res1, 3 );
                    
                    logger.appendln( "\t FOUND a hexad in rows " + i + "," + j + "," + k
                        + " at cols " + $g1 + "," + $g2 + "," + $g3 );
                    
                    $tmpRes1 = cols[$g1].processHexadVal( v, i, j, k );
                    $tmpRes2 = cols[$g2].processHexadVal( v, i, j, k );
                    $tmpRes3 = cols[$g3].processHexadVal( v, i, j, k );
                    
                    if( ! $result )
                      $result = $tmpRes1 || $tmpRes2 || $tmpRes3 ;
                    
                    if( $result )
                      break loop ;
                  }
                }
            }
        }// rows
        
        // CHECK COLS
        if( (cols[i].nSqrsCanBeVal[v] == 2) || (cols[i].nSqrsCanBeVal[v] == 3) )
        {
          for( int j=i+1; j < gridLength ; j++ )
            if( (cols[j].nSqrsCanBeVal[v] == 2) || (cols[j].nSqrsCanBeVal[v] == 3) )
            {
              $res1 = cols[i].sqrsCanBeVal[v] | cols[j].sqrsCanBeVal[v] ;
              
              // make sure we have exactly 3 Squares
              if( Helper.numSetBits($res1, gridLength) != 3 )
                continue ;
              
              for( int k=j+1; k < gridLength; k++ )
                if( (cols[k].nSqrsCanBeVal[v] == 2) || (cols[k].nSqrsCanBeVal[v] == 3) )
                {
                  $res2 = $res1 | cols[k].sqrsCanBeVal[v] ;
                  if( $res2 == $res1 )
                  {
                    $g1 = Helper.getBitPosn( $res1, 1 );
                    $g2 = Helper.getBitPosn( $res1, 2 );
                    $g3 = Helper.getBitPosn( $res1, 3 );
                    
                    logger.appendln( "\t FOUND a hexad in cols " + i + "," + j + "," + k
                                     + " at rows " + $g1 + "," + $g2 + "," + $g3 );
                    
                    $tmpRes1 = rows[$g1].processHexadVal( v, i, j, k );
                    $tmpRes2 = rows[$g2].processHexadVal( v, i, j, k );
                    $tmpRes3 = rows[$g3].processHexadVal( v, i, j, k );
                    
                    if( ! $result )
                      $result = $tmpRes1 || $tmpRes2 || $tmpRes3 ;
                    
                    if( $result )
                      break loop ;
                  }
                }
            }
        }// cols
        
      }// initial row/col
    }// values
    
    logger.send( Level.INFO );
    return $result ;
    
  }// Grid.findHexad()
  
  /**
   *  CANNOT have a "rectangle" in two Groups where each corner has the same two possible values,
   *  as the placement of the two different values would be equally likely,
   *  and thus the solution to that game would NOT be unique. [aka "Unique Rectangles" requirement]<br>
   *  
   *  <b>Find</b> any Tetrads [aka "Deadly Rectangles"] in the Grid and process the values <br>
   *  - called by {@link #solveOneSqr}
   *  
   *  @return success or failure
   */
  private boolean findTetrads()
  {
    Square $foundSqr ;
    int $foundRow, $foundCol ;
    boolean $tmpRes1, $tmpRes2, $result=false ;
    
    logger.info( "\n Checking for tetrads..." );
    
    // #1 for value v, check Sqrs for possVals of 2
    for( Square[] r : sqrs2dArray )
      for( Square $s1 : r )
      {
        if( $s1.numPossibleVals() == 2 )
          // #2 if found, check Sqrs in Zone for matching possVals and common row or col
          for( Square $s2 : $s1.getZone().mySqrs )
            if( ($s2 != $s1) && ($s2.getPossibleVals() == $s1.getPossibleVals()) )
            {
              // check Row
              if( $s2.getRow() == $s1.getRow() )
              {
                logger.info( "Check possible Tetrad pair at " + $s1.strGridPosn() + " & " + $s2.strGridPosn() );
                
                // #3a if found, check its col for Sqr with matching possVals
                for( Square $s3 : $s1.getCol().mySqrs )
                  if( ($s3 != $s1) && ($s3.getPossibleVals() == $s1.getPossibleVals()) )
                  // #4a if found, can REMOVE these 2 possible values from the Sqr at the intersection of this row & col
                  {
                    logger.info( " >> Found a matching Sqr at " + $s3.strGridPosn() );
                    $foundRow = $s3.getRowIndex();
                    $foundCol = $s2.getColIndex();
                    $foundSqr = sqrs2dArray[$foundRow][$foundCol];
                    logger.info( "\t >> Can REMOVE [" + Helper.displaySetBits($s3.getPossibleVals(), gridLength, "")
                                 +  "] from " + $foundSqr.strGridPosn() );
                    
                    $tmpRes1 = $foundSqr.removePossibleVal( Helper.getBitPosn($s1.getPossibleVals(), 1) );
                    $tmpRes2 = $foundSqr.removePossibleVal( Helper.getBitPosn($s1.getPossibleVals(), 2) );
                    logger.send( Level.INFO );
                    
                    if( ! $result )
                      $result = $tmpRes1 || $tmpRes2 ;
                  }
              }
              // check Col
              if( $s2.getCol() == $s1.getCol() )
              {
                logger.info( "Check possible Tetrad pair at " + $s1.strGridPosn() + " & " + $s2.strGridPosn() );
                
                // #3b if found, check its row for Sqr with matching possVals
                for( Square $s3 : $s1.getRow().mySqrs )
                  if( ($s3 != $s1) && ($s3.getPossibleVals() == $s1.getPossibleVals()) )
                  // #4b if found, can REMOVE these 2 possible values from the Sqr at the intersection of this row & col
                  {
                    logger.info( " >> Found a matching Sqr at " + $s3.strGridPosn() );
                    $foundRow = $s2.getRowIndex();
                    $foundCol = $s3.getColIndex();
                    $foundSqr = sqrs2dArray[$foundRow][$foundCol];
                    logger.info( "\t >> Can REMOVE [" + Helper.displaySetBits($s3.getPossibleVals(), gridLength, "")
                                 +  "] from " + $foundSqr.strGridPosn() );
                    
                    $tmpRes1 = $foundSqr.removePossibleVal( Helper.getBitPosn($s1.getPossibleVals(), 1) );
                    $tmpRes2 = $foundSqr.removePossibleVal( Helper.getBitPosn($s1.getPossibleVals(), 2) );
                    logger.send( Level.INFO );
                    
                    if( ! $result )
                      $result = $tmpRes1 || $tmpRes2 ;
                  }
              }
            }// s2 matches s1
      }// check Sqrs
    
    return $result ;
    
  }// Grid.findTetrads()
  
  /**
   *  Find any Color Chain values and then re-check for any Solvable Squares <br>
   *  = TF chaining, reductio ad absurdum <br>
   *  - called by {@link #solveOneSqr}
   *  
   *  @return success or failure
   */
  private boolean findColorChainVal()
  {
    boolean $haveChain, $chainInVal, $haveSoln=false ;
    
    if( ! colorChainInit )
    {
      numActiveColors = 2 ;
      for( int i=0; i < numActiveColors; i++ )
        colorChain[i] = new ColorChain( i );
      
      colorChainInit = true ;
      logger.info( "INIT " + numActiveColors + " Color Chains" );
    }
    
    for( int v=1; v <= gridLength; v++ )
    {
      logger.info( "\n\t\t Check val '" + v + "'" );
      
      $haveChain = $chainInVal = false ;
      
      // Search Squares
      loop:
      for( Square[] r : sqrs2dArray )
        for( Square s : r )
          // check for possible value 'v'
          if( s.isOpen() && s.canBeVal(v) )
          {
            // only need Sqrs that HAVEN'T already been checked
            if( s.inColorChain() )
              continue ;
            
            logger.info( "\n\t Check possible Sqr " + s.strGridPosn()
                         + " / PV == " + Helper.displaySetBits(s.getPossibleVals(), gridLength, "") );
            
            // clear the chains but DO NOT reset Squares until FINISHED with this value
            resetColorChains( false );
            
            // #1 - if found, check for Groups with 2 possible values
            $haveChain = buildColorChain( PINK_CHAIN, v, s );
            
            if( $haveChain )
            {
              $chainInVal = true ;
              
              $haveSoln = checkColorChains( v );
              
              if( $haveSoln )
                break loop ;
            }
          }
      // end loop
      
      if( $chainInVal )
      {
        // reset Chains & Squares for this value
        resetColorChains( true );
        
        if( $haveSoln )
          break ;
      }
    }
    
    logger.send( Level.INFO );
    return $haveSoln ;
    
  }// Grid.findColorChainVal()
  
  /**
   *  Check the parameter {@link Square} for inclusion in a Color Chain <br>
   *  
   *  @param chainColor - current Color Chain level
   *  @param val - value to check
   *  @param sqr - Square to check
   *  
   *  @return success or failure
   *  
   *  @see #findColorChainVal
   *  @see Group#findColorSqr
   */
  boolean buildColorChain( final int chainColor, final int val, final Square sqr )
  {
    if( sqr == null )
    {
      logger.severe( "Passed a null Square!!??" );
      return false ;
    }
    
    logger.info( "Check Sqr " + sqr.strGridPosn() + " for inclusion in colorChain["
                 + (chainColor == PINK_CHAIN ? "PINK" : "BLUE") + "]" );
    
     Row r = sqr.getRow();
     Col c = sqr.getCol();
    Zone z = sqr.getZone();
    
    boolean $rowRes  = ( r.nSqrsCanBeVal[val] == 2 );
    boolean $colRes  = ( c.nSqrsCanBeVal[val] == 2 );
    boolean $zoneRes = ( z.nSqrsCanBeVal[val] == 2 );
    
    // #2 - if found, place this Sqr in the appropriate ColorChain
    if( $rowRes || $colRes || $zoneRes )
    {
      logger.info( "\t Sqr " + sqr.strGridPosn() + " has a Color-eligible Group" );
      colorChain[ chainColor % numActiveColors ].add( sqr );
    }
    
    // #3 - and place Grp Sqrs with possible vals that include 'v' in other/next ColorChain
    if( $rowRes )
    {
      logger.info( "\t >> possible matching Sqr is in " + r.myPosn() );
      r.findColorSqr( chainColor+1, val );
    }
    
    if( $colRes )
    {
      logger.info( "\t >> possible matching Sqr is in " + c.myPosn() );
      c.findColorSqr( chainColor+1, val );
    }
    
    if( $zoneRes )
    {
      logger.info( "\t >> possible matching Sqr is in " + z.myPosn() );
      z.findColorSqr( chainColor+1, val );
    }
    
    return( $rowRes || $colRes || $zoneRes );
    
  }// Grid.buildColorChain()
  
  /**
   *  Check the Color Chains for values that can be assigned or excluded <br>
   *  - called by {@link #findColorChainVal}
   *  
   *  @param val - current value
   *  @return success or failure
   *  
   *  @see #colorChain
   */
  private boolean checkColorChains( final int val )
  {
    for( int i=0; i < numActiveColors; i++ )
      colorChain[i].display( Level.INFO, "" );
    
    // if any Group of a Square is also represented by another Square in this Color
    // >> ALL Squares of the OTHER Color must be value 'v'
    if( colorChain[PINK_CHAIN].checkForDuplicateGrps() )
    {
      logger.info( "DUPLICATE Groups in Pink Chain >> ALL Sqrs in Blue Chain MUST BE val '" + val + "'" );
      // leave v as the ONLY POSSIBLE val in the Blue Chain Squares
      // - these Squares will then easily be solved by findSqrSingle()
      colorChain[BLUE_CHAIN].setSqrsToPossibleVal( val );
      return true ;
    }
    
    if( colorChain[BLUE_CHAIN].checkForDuplicateGrps() )
    {
      logger.info( "DUPLICATE Groups in Blue Chain >> ALL Sqrs in Pink Chain MUST BE val '" + val + "'" );
      // leave v as the ONLY POSSIBLE val in the Pink Chain Squares
      // - these Squares will then easily be solved by findSqrSingle()
      colorChain[PINK_CHAIN].setSqrsToPossibleVal( val );
      return true ;
    }
    
    // if ANY Squares at the INTERSECTION of a row/col of 2 Squares from DIFFERENT Colors
    // >> EXCLUDE 'v' from these Squares
    boolean $intxVal=false, $result=false ;
    
    for( int cc=0; cc < numActiveColors; cc++ )
      for( int i=0; i < ColorChain.MAX_SQRS; i++ )
        if( colorChain[cc].getRow(i) )
          for( int j=0; j < ColorChain.MAX_SQRS; j++ )
            if( colorChain[ (cc+1) % numActiveColors ].getCol(j) )
            {
              if( (!sqrs2dArray[i][j].inColorChain()) && sqrs2dArray[i][j].isOpen() )
              {
                logger.info( "\t Look for an INTERSECTION Square at " + sqrs2dArray[i][j].strGridPosn() );
                $intxVal = sqrs2dArray[i][j].removePossibleVal( val );
                logger.send( Level.INFO );
              }
              
              if( ! $result )
                $result = $intxVal ;
            }
    
    return $result ;
    
  }// Grid.checkColorChains()
  
  /**
   *  Reset ALL mutable Color Chain fields to default values <br>
   *  - called by {@link #findColorChainVal}
   *  
   *  @param doSqrs - reset individual {@link Square}s too
   *  @see #colorChain
   */
  private void resetColorChains( final boolean doSqrs )
  {
    for( int i=0; i < numActiveColors; i++ )
      colorChain[i].reset();
    
    if( doSqrs )
      for( Square[] r : sqrs2dArray )
        for( Square s : r )
          if( s.inColorChain() )
            s.putInChain( NO_COLOR );
    
  }// Grid.resetColorChains()
  
  /**
   *  Get a new value directly from the Solution {@link SavedGame} <br>
   *  - called by {@link #solveOneSqr}
   *  
   *  @return whether or not there are still more values to find in the current game
   *  @see #savedSolution
   */
  private boolean getValFromSolution()
  {
    logger.info( "Obtain a value directly from the Solution.\n" );
    
    // should NOT still be finding a solution when reach this point...
    // - but just in case, give it a few more attempts here anyway
    int $attempts = 0 ;
    while( (!solveWorkerDone()) && ($attempts < 8) )
    {
      try
      {
        Thread.sleep( gameview.getSolveDelay()/3 );
      }
      catch( InterruptedException ie )
      {
        logger.info( ie.toString() );
      }
      $attempts++ ;
    }
    
    // something seriously wrong in this case
    if( ! solveWorkerDone() )
    {
      logger.warning( "Solution is NOT available!! ??" );
      return false ;
    }
    
    // DID NOT receive a proper Solution...
    if( ! savedSolnGood )
    {
      logger.warning( "Solution is NOT GOOD!" );
      return false ;
    }
    
    // this loop will also fix any incorrect values in Squares
    int $soln ;
    loop:
    for( int i=0; i < gridLength; i++ )
      for( int j=0; j < gridLength; j++ )
      {
        if( ! sqrs2dArray[i][j].isFixed() )
        {
          $soln = savedSolution.getValue( i, j );
          if( sqrs2dArray[i][j].getValue() != $soln )
          {
            sqrs2dArray[i][j].solvedValue = $soln ;
            setAutoSolvedSqr( sqrs2dArray[i][j] );
            break loop ;
          }
        }
      }
    
    return gameview.isRunning() ;
    
  }// Grid.getValFromSolution()
  
  /**
   *  Set the required fields when solving a {@link Square} <br>
   *  - value is in {@link Square#solvedValue}
   *  
   *  @param sqr - Square to set
   *  @return success or failure
   */
  private boolean setAutoSolvedSqr( Square sqr )
  {
    if( sqr == null )
    {
      logger.severe( "Passed a null Square!!??" );
      return false ;
    }
    
    if( sqr.isFixed() )
    {
      logger.warning( "! Trying to solve a FIXED Square: " + sqr.strGridPosn() );
      return false ;
    }
    
    if( (sqr.solvedValue < 1) || (sqr.solvedValue > gridLength) )
    {
      logger.warning( "Sqr " + sqr.strGridPosn() + " has an INVALID solvedValue: '" + sqr.solvedValue + "' !" );
      return false ;
    }
    
    logger.info( "Sqr " + sqr.strGridPosn() + ": val = " + sqr.solvedValue );
    
    setActiveSqr( sqr.getRowIndex(), sqr.getColIndex() );
    autoSolvedSqrs[ sqr.getRowIndex() ][ sqr.getColIndex() ] = true ;
    
    // Enter the AutoSolve value in the grid
    if( newValue(sqr.solvedValue) )
    {
      sqr.setAutoSolved();
      
      boolean $focusRequest = requestFocusInWindow();
      logger.fine( "requestFocusInWindow() = " + $focusRequest );
      
      return true ;
    }
    
    return false ;
    
  }// Grid.setAutoSolvedSqr()
  
  /**
   *  Activate the Square in the given row and col
   *  
   *  @param row - of new Active {@link Square}
   *  @param col - of new Active {@link Square}
   */
  private void setActiveSqr( final int row, final int col )
  {
    if( sqrs2dArray[row][col].isFixed() )
    {
      logger.warning( "! Trying to activate a FIXED Square: " + sqrs2dArray[row][col].strGridPosn() );
      return ;
    }
    
    // de-activate the previous Active Square, if any
    getActiveSqr().setActive( false );
    
    // set the current active Square
    sqrs2dArray[row][col].setActive( true );
    activeSqr = sqrs2dArray[row][col] ;
    
  }// Grid.setActiveSqr()
  
  /**
   * Set a {@link Square} reference for when the active Square is not valid
   */
  private void setDefaultSqr()
  {
    // first Open Square will be the default
    for( Square[] r : sqrs2dArray )
      for( Square s : r )
        if( s.isOpen() )
        {
          defaultSqr = s ;
          return ;
        }
    
  }// Grid.setDefaultSqr()
  
  /**
   * Reset all the Undo/Redo fields
   */
  private final void clearUndo()
  {
    undoVector.clear();
    undoMode = false ;
    undoPtr = 0 ;
    totalEntries = 0 ;
    
  }// Grid.clearUndo()
  
 // ===========================================================================================================
 //                            A C T I O N    E V E N T S
 // ===========================================================================================================
 
   /**
    * Set the active {@link Square}, repaint the {@link Grid} and get Keyboard focus
    * 
    * @param mevt - {@link MouseEvent}
    * @see MouseListener#mouseClicked
    */
   public void mouseClicked( final MouseEvent mevt )
   {
     if( ! gameview.isRunning() )
       return ;
     
     int $col = mevt.getX() / pxSqrSize ; // horizontal co-ordinate
     int $row = mevt.getY() / pxSqrSize ; // vertical co-ordinate
     
     logger.log( showMouseActions ? Level.SEVERE : Level.FINER, "Clicked on Sqr " + sqrs2dArray[$row][$col].strGridPosn() );
     
     // make sure we are in an appropriate Square
     if( ($row < gridLength) && ($col < gridLength) )
     {
       setActiveSqr( $row, $col );
       
       // clear previous temp value
       tempValue = SqrTypes.BLANK_VAL ;
       
       repaint();
     }
     
     // get Keyboard focus for the Grid
     boolean $haveFocus = isFocusOwner();
     if( ! $haveFocus )
       $haveFocus = requestFocusInWindow();
     
     if( ! $haveFocus )
       logger.severe( " *** Grid DID NOT get Keyboard Focus! ***" );
     
   }// Grid.mouseClicked()
   
   /**
    * Terminate 'Active' status & reset temp value if mouse exited the {@link Grid}
    * 
    * @param mevt - {@link MouseEvent}
    * @see MouseListener#mouseExited
    */
   public void mouseExited( final MouseEvent mevt )
   {
     if( activeGame == null )
       return ;
     
     // turn OFF 'Active' if focus left the grid
     getActiveSqr().setActive( false );
     activeSqr = null ;
     if( showMouseActions )
       logger.severe( "getActiveSqr()[" + getActiveSqr().strGridPosn() + "].setActive( false )" );
     
     // reset temp
     tempValue = SqrTypes.BLANK_VAL ;
     tempMode = false ;
     typeLock = false ;
     
     repaint();
     
   }// Grid.mouseExited()
   
   /* Must implement these inherited abstract methods of interface MouseListener
      even though do not need their functionality */
   /** @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
    *  @param me - {@link MouseEvent}  */
   public void mouseEntered( MouseEvent me ) { }
   /** @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
    *  @param me - {@link MouseEvent}  */
   public void mousePressed( MouseEvent me ) { }
   /** @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
    *  @param me - {@link MouseEvent}  */
   public void mouseReleased( MouseEvent me ) { }
   
   /* Must implement these inherited abstract methods of interface KeyListener */

   /**
    *  Deal with {@link KeyEvent}s that are not handled properly in {@link #keyTyped}
    *  
    *  @param kevt - {@link KeyEvent}
    *  
    *  @see #processInput
    *  @see KeyListener#keyPressed
    */
   public void keyPressed( final KeyEvent kevt )
   {
     if( kevt == null )
     {
       logger.severe( "Passed a null KeyEvent!!??" );
       return ;
     }
     
     if( (!gameview.isRunning()) || (activeSqr == null) )
       return ;
     
     int $code = kevt.getKeyCode();
     int $mods = kevt.getModifiers();
     int $locn = kevt.getKeyLocation();
     
     if( showKeyStrokes )
     {
       logger.severe
       (
         "Key: '" + KeyEvent.getKeyText( $code ) + "'"
         + " // Mods: '" + KeyEvent.getKeyModifiersText( $mods ) + "'"
         + " // Locn: "  + ( ($locn == KeyEvent.KEY_LOCATION_NUMPAD) ? "Numpad" 
                             : ( $locn == KeyEvent.KEY_LOCATION_STANDARD ? "Standard" : "Other(" + $locn + ")" ) )
        );
     }
     
     // arrow keys move the active Square
     if( kevt.isActionKey() )
     {
       if( processArrowKeys($code) )
         repaint();
       
       return ;
     }
     
     // ignore Control events
     if( kevt.isControlDown() )
       return ;
     
     /* SET TEMP MODE */
     tempMode = kevt.isAltDown();
     
     // have to handle Alt+Numpad Events here
     // - these produce a keyTyped() KeyEvent in MS Windows, but with a modified numeric value
     //   e.g. Alt+Numpad-5 produces '9827', Alt+Numpad-234 produces '937'
     if( tempMode && ($locn == KeyEvent.KEY_LOCATION_NUMPAD) )
     {
       logger.appendln( "Handle Alt+Numpad KeyEvent" );
       
       // Set typeLock to ignore the next keyTyped Event
       // - except for Alt+Numpad-0 Events, which do NOT produce a keyTyped Event in MS Windows
       if( (!typeLock) && ($code != KeyEvent.VK_NUMPAD0) )
       {
         typeLock = true ;
         logger.append( "Alt+Numpad (not 0) Event - ACTIVATE TypeLock" );
       }
       
       logger.send( Level.FINE );
       
       processInput( kevt );
     }
     
   }// Grid.keyPressed()
   
   /**
    *  For DEBUG info
    *  
    *  @param kevt - {@link KeyEvent}
    *  @see KeyListener#keyReleased
    */
   public void keyReleased( final KeyEvent kevt )
   {
     if( kevt == null )
     {
       logger.severe( "Passed a null KeyEvent!!??" );
       return ;
     }
     
     logger.append( "Key: '" + KeyEvent.getKeyText( kevt.getKeyCode() ) + "'"
                    + " // Mod: '" + KeyEvent.getKeyModifiersText( kevt.getModifiers() ) + "'" );

     logger.send( showKeyStrokes ? Level.FINE : Level.FINER );
     
   }// Grid.keyReleased()
   
   /**
    * Send the typed value for processing
    * 
    * @param kevt - {@link KeyEvent}
    * 
    * @see #processInput
    * @see KeyListener#keyTyped
    */
   public void keyTyped( final KeyEvent kevt )
   {
     if( kevt == null )
     {
       logger.severe( "Passed a null KeyEvent!!??" );
       return ;
     }
     
     if( (!gameview.isRunning()) || (activeSqr == null) || kevt.isControlDown() )
       return ;                                            // ignore Control events
     
     // this event is from an Alt+Numpad keyPress, which we want to ignore here 
     // as it was already handled in Grid.keyPressed() 
     // - must reset so the next GOOD keyTyped() Event will be processed
     if( typeLock )
     {
       typeLock = false ;
       logger.appendln( "\t\t DE-ACTIVATE TypeLock" );
       return ;
     }
     
     logger.log( showKeyStrokes ? Level.SEVERE : Level.FINE,
                 "tempMode NO" + (tempMode ? "W" : "T") + " Active: Key '" + kevt.getKeyChar() + "'" );
     
     processInput( kevt );
     
   }// Grid.keyTyped()
 
 // ===========================================================================================================
 //                        G R A P H I C S    E V E N T S
 // ===========================================================================================================
 
  // for debugging
  static int pci = 0 ;
  
  /**
   *  Refresh the graphical representation of the {@link Grid}<br>
   *  - Swing painting guidelines instruct to override this <b>instead of</b> <em>paint()</em> <br>
   *  
   *  - called by {@link java.awt.Component#repaint}<br>
   *  - invokes {@link #paintSquare}<br>
   *  
   *  @param page - {@link Graphics} reference
   *  
   *  @see javax.swing.JComponent#paintComponent
   */
  @Override
  protected void paintComponent( Graphics page )
  {
    // just to see how often this gets called
    logger.finer( "Called " + (++pci) + " times" );
    
    if( page == null )
    {
      logger.severe( " *** PROBLEM: Graphics page is NULL! ***" );
      return ;
    }
    
    for( int $row = 0; $row < gridLength; $row++ )
      for( int $col = 0; $col < gridLength; $col++ )
      {
        paintSquare( $col * pxSqrSize, $row * pxSqrSize, sqrs2dArray[$row][$col], page );
      }
  
  }// Grid.paintComponent()
  
  /**
   *  Draw individual {@link Square}s according to the value and type, i.e. whether FIXED, GUESS or OPEN <br>
   *  - called by {@link #paintComponent}
   *  - invokes {@link #paintBlankSquare}<br>
   *  
   *  @param horiz - <i>x</i> coordinate of the upper left corner
   *  @param vert  - <i>y</i> coordinate of the upper left corner
   *  @param sqr   - {@link Square} to paint
   *  @param page  - {@link Graphics} reference
   */
  private void paintSquare( final int horiz, final int vert, final Square sqr, Graphics page )
  {
    if( page == null )
    {
      logger.severe( " *** PROBLEM: Graphics page is NULL! ***" );
      return ;
    }
    
    if( sqr == null )
    {
      logger.severe( "Passed a null Square!!??" );
      return ;
    }
    
    paintValue = sqr.getValue();
    paintString = String.valueOf( paintValue );
    
    logger.finest( " horizontal co-ord = " + horiz + "; vertical co-ord = " + vert
                   + "; type = " + sqr.getType() + "; value = " + paintString );
    
    // paint the background
    paintBlankSquare( horiz, vert, sqr, page );
    
    if( paintValue == SqrTypes.BLANK_VAL )
      return ;
    
    paintFont = page.getFont();
    // use Font metrics to center properly
    paintX = page.getFontMetrics( paintFont ).stringWidth( paintString );
    paintY = paintFont.getSize();
    
    // set the Color and Font
    if( sqr.isTemp() )
    {
      page.setColor( SqrTypes.TEMP.getColor() );
      page.setFont( tempFont );
      paintFont = page.getFont();
      paintX = page.getFontMetrics( paintFont ).stringWidth( paintString );
      paintY = paintFont.getSize();
    }
    else if( sqr.isAutoSolved() )
    {
      page.setColor( SqrTypes.SOLVED.getColor() );
    }
    else if( sqr.isWrong() )
    {
      page.setColor( SqrTypes.BAD.getColor() );
    }
    else
        page.setColor( sqr.getType().getColor() );
    
    // paint in the value
    page.drawString( paintString, horiz + (pxSqrSize-paintX)/2, vert + (pxSqrSize+paintY)/2 );
    
    // if 'showConflict' is selected, add a '?' in CONFLICTING Squares
    if( gameview.showingConflicts() && sqr.isConflicting() )
    {
      page.setColor( SqrTypes.BAD.getColor() );
      page.setFont( conflictFont );
      page.drawString( "?", horiz + 2, vert + conflictFont.getSize() );
    }
    
    // restore the regular Font
    if( ! page.getFont().equals(guessFont) )
      page.setFont( guessFont );
    
  }// Grid.paintSquare()
  
  /**
   *  Draw a blank {@link Square} with edge hilites <br>
   *  - called by {@link #paintSquare}
   *  
   *  @param horiz - <i>x</i> coordinate of the upper left corner
   *  @param vert  - <i>y</i> coordinate of the upper left corner
   *  @param sqr     - {@link Square} to draw
   *  @param page  - {@link Graphics} reference
   */
  private void paintBlankSquare( final int horiz, final int vert, final Square sqr, Graphics page )
  {
    if( page == null )
    {
      logger.severe( " *** PROBLEM: Graphics page is NULL! ***" );
      return ;
    }
    
    if( sqr == null )
    {
      logger.severe( "Passed a null Square!!??" );
      return ;
    }
    
    logger.finest( "Sqr " + sqr.strGridPosn() );
    
    if( sqr.isActive() )
      page.setColor( SqrTypes.ACTIVE.getColor() );
    else
        page.setColor( Zone.COLORS[sqr.getZoneIndex()] );
    
    page.fillRect( horiz, vert, pxSqrSize, pxSqrSize );
    
    page.setColor( COLOR_SHADOW );
    page.drawLine( horiz            , vert+pxSqrSize-1, horiz+pxSqrSize  , vert+pxSqrSize-1 );// bottom
    page.drawLine( horiz+pxSqrSize-1, vert            , horiz+pxSqrSize-1, vert+pxSqrSize   );// right
    
    page.setColor( COLOR_HILITE );
    page.drawLine( horiz, vert, horiz+pxSqrSize-1, vert             ); // top
    page.drawLine( horiz, vert, horiz            , vert+pxSqrSize-1 ); // left
  
  }// Grid.paintBlankSquare()
  
 // ===========================================================================================================
 //                             D E B U G    C O D E
 // ===========================================================================================================
  
  String myname() { return getClass().getSimpleName(); }
  
  /**
   * Toggle keystroke information display
   */
  void toggleKeystrokes()
  {
    showKeyStrokes = !showKeyStrokes ;
    logger.warning( "showKeyStrokes is NO" + (showKeyStrokes ? "W" : "T") + " Active!" );
  }
  
  /**
   * Toggle mouseclick information display
   */
  void toggleMouseclicks()
  {
    showMouseActions = !showMouseActions ;
    logger.warning( "showMouseclicks is NO" + (showMouseActions ? "W" : "T") + " Active!" );
  }
  
  /**
   *  Set each {@link Square}'s possible values as a temp value <br>
   *  - called by {@link Launcher#setGridDebugKeyMap}
   *  
   *  @see Square#setPossValsAsTempVal
   */
  void setPossibleValsAsTempVals()
  {
    logger.info("");
    
    for( Square[] r : sqrs2dArray )
      for( Square s : r )
        s.setPossValsAsTempVal();
    
    // ?? NEED TO SET THESE IN THE UNDO VECTOR ??
    
    tempMode = false ;
    validate();
    gameview.repaint();
  
  }// Grid.setPossibleValsAsTempVals()
  
  /**
   *  Clear ALL temp value {@link Square}s <br>
   *  - called by {@link Launcher#setGridDebugKeyMap}
   */
  void clearAllTempVals()
  {
    logger.info("");
    
    for( Square[] r : sqrs2dArray )
      for( Square s : r )
        if( s.isTemp() )
          s.newValue( SqrTypes.BLANK_VAL, false );
    
    tempMode = false ;
    validate();
    gameview.repaint();
  
  }// Grid.clearAllTempVals()
  
  /**
   * display each {@link Square} in an Active {@link Group}
   * 
   * @param level - log {@link Level} to display at
   * @param info  - extra text to display
   */
  void displayGrpSqrs( final Level level, final String info )
  {
    Level lev = level ;
    
    if( level == null )
    {
      logger.severe( "Passed a null Level!!??" );
      lev = LogControl.DEFAULT_LEVEL ;
    }
    
    
    logger.log( lev, info + "\n--------------------------" );
    
    for( Square[] r : sqrs2dArray )
      for( Square s : r )
        if( (s.getZone() == activeZone()) || (s.getRow() == activeRow()) || (s.getCol() == activeCol()) )
          s.display( lev, BRIEF, "" );
  
  }// Grid.displayGrpSqrs()
  
  /**
   * display each entry in {@link #undoVector}
   * @param level - log {@link Level} to display at
   * @param info  - extra text to display
   */
  void displayUndoVector( final Level level, final String info )
  {
    Level lev = level ;
    
    if( level == null )
    {
      logger.severe( "Passed a null Level!!??" );
      lev = LogControl.DEFAULT_LEVEL ;
    }
    
    logger.fine( info + "\n--------------------------------------------------------------" );
    
    for( int i=0; i < totalEntries; i++ )
    {
      if( i == undoPtr )
        logger.appendln( "--------------------------------------------------------------" );
      
      logger.append( "UndoMatrix[" + i + "] : " );
      undoVector.elementAt( i ).display( lev, "" );
    }
  
  }// Grid.displayUndoVector()
  
  /**
   * display values of important <var>fields</var> in {@link Grid}
   * 
   * @param level - log {@link Level} to display at
   * @param info  - extra text to display
   * 
   * @see Launcher#setGridDebugKeyMap
   */
  void displayFields( final Level level, final String info )
  {
    Level lev = level ;
    
    if( level == null )
    {
      logger.severe( "Passed a null Level!!??" );
      lev = LogControl.DEFAULT_LEVEL ;
    }
    
    logger.appendln( "\n\t Active Sqr : " + getActiveSqr().strGridPosn() );
    logger.appendln( "\t Current Game : '" + gameview.getGameName() + "'" );
    logger.appendln( "\t numConflicts = " + nConflicts );
    logger.appendln( "\t numBlankSqrs = " + numBlankSqrs );
    logger.appendln( "\t showKeyStrokes = " + showKeyStrokes );
    logger.appendln( "\t showMouseActions = " + showMouseActions );
    logger.appendln( "\t tempValue = " + tempValue );
    logger.appendln( "\t zoneLength = " + zoneLength );
    logger.appendln( "\t gridLength = " + gridLength );
    logger.appendln( "\t totalSqrs = " + totalSqrs );
    logger.appendln( "\t Undo Mode = " + undoMode );
    logger.appendln( "\t undoPtr = " + undoPtr );
    logger.appendln( "\t totalEntries = " + totalEntries );
    
    displayUndoVector( lev, info );
    
    logger.send( lev );
  
  }// Grid.displayFields()
  
  /**
   * display <b>Active</b> {@link Group}s in the {@link Grid}
   * 
   * @param level - log {@link Level} to display at
   * @param brief - display less text
   * @param info  - extra text to display
   */
  void displayActiveGroups( final Level level, final boolean brief, final String info )
  {
    Level lev = level ;
    
    if( level == null )
    {
      logger.severe( "Passed a null Level!!??" );
      lev = LogControl.DEFAULT_LEVEL ;
    }
    
    logger.log( lev, info + "\n---------------------------" );
    
    activeZone().display( lev, brief, info );
     activeRow().display( lev, brief, info );
     activeCol().display( lev, brief, info );
  
  }// Grid.displayActiveGroups()
  
  /**
   * display <u>ALL</u> the {@link Group}s in the {@link Grid}
   * 
   * @param level - log {@link Level} to display at
   * @param brief - display less text
   * @param info  - extra text to display
   */
  void displayAllGroups( final Level level, final boolean brief, final String info )
  {
    Level lev = level ;
    
    if( level == null )
    {
      logger.severe( "Passed a null Level!!??" );
      lev = LogControl.DEFAULT_LEVEL ;
    }
    
    if( !brief )
      logger.log( lev, myname() + ".displayAllGroups(" + info + ")\n-----------------------------" );
    
    displayZones( lev, Grid.BRIEF, info );
    displayRows ( lev, Grid.BRIEF, info );
    displayCols ( lev, Grid.BRIEF, info );
  
  }// Grid.displayAllGroups()
  
  /**
   * display <u>ALL</u> {@link Col}s in the {@link Grid}
   * 
   * @param level - log {@link Level} to display at
   * @param brief - display less text
   * @param info  - extra text to display
   */
  void displayCols( final Level level, final boolean brief, final String info )
  {
    Level lev = level ;
    
    if( level == null )
    {
      logger.severe( "Passed a null Level!!??" );
      lev = LogControl.DEFAULT_LEVEL ;
    }
    
    if( !brief )
      logger.log( lev, myname() + ".displayCols(" + info + "):\n--------------------------------" );
    
    for( Col c: cols )
      c.display( lev, brief, info );
  
  }// Grid.displayCols()
  
  /**
   * display <u>ALL</u> {@link Row}s in the {@link Grid}
   * 
   * @param level - log {@link Level} to display at
   * @param brief - display less text
   * @param info  - extra text to display
   */
  void displayRows( Level level, boolean brief, String info )
  {
    Level lev = level ;
    
    if( level == null )
    {
      logger.severe( "Passed a null Level!!??" );
      lev = LogControl.DEFAULT_LEVEL ;
    }
    
    if( !brief )
      logger.log( lev, myname() + ".displayRows(" + info + "):\n--------------------------------" );
    
    for( Row r: rows )
      r.display( lev, brief, info );
    
  }// Grid.displayRows()
  
  /**
   * display <u>ALL</u> {@link Zone}s in the {@link Grid}
   * 
   * @param level - log {@link Level} to display at
   * @param brief - display less text
   * @param info  - extra text to display
   */
  void displayZones( final Level level, final boolean brief, final String info )
  {
    Level lev = level ;
    
    if( level == null )
    {
      logger.severe( "Passed a null Level!!??" );
      lev = LogControl.DEFAULT_LEVEL ;
    }
    
    if( !brief )
      logger.log( lev, myname() + ".displayZones(" + info + "):\n--------------------------------" );
    
    for( Zone z: zones )
      z.display( lev, brief, info );
    
  }// Grid.displayZones()
 
  /**
   * display the {@link SavedGame} with the current Solution
   */
  void displaySolution()
  {
    // do NOT have a proper Solution...
    if( ! savedSolnGood )
    {
      logger.warning( "Solution is NOT GOOD!" );
      return ;
    }
    
    savedSolution.display( Level.SEVERE );
    
  }// Grid.displaySolution()
 
 /*
  *            F I E L D S
  *************************************************************************************************************/
  
  static final int
                  INCREASE =  1 ,
                  DECREASE = -1 ,
                  BASE_KEYCODE_INDEX = 48 ; // ASCII for character '0'
  
  static final String
                     strREG_TYPEFACE = "Arial"     ,
                     strBIG_TYPEFACE = "SansSerif" ;
  
  static final Color
                     COLOR_HILITE  =  Color.lightGray ,
                     COLOR_SHADOW  =  Color.darkGray ;
  
  static final boolean
                      BRIEF = true  ,
                       FULL = false ;
  
  /** Logging */
  static PskLogger logger ;
  
  /** Display */
  static Font Font_Sqr_Sm, Font_Conflicts_Sm, Font_Temp_Sm,
              Font_Sqr_Md, Font_Conflicts_Md, Font_Temp_Md,
              Font_Sqr_Lg, Font_Conflicts_Lg, Font_Temp_Lg ;
  
  /** Currently selected {@link Font} */
  private Font guessFont, conflictFont, tempFont, paintFont ;
  
  /**
   *  Debug control
   *  @see Launcher#setGridDebugKeyMap
   */
  private boolean showKeyStrokes   ,
                  showMouseActions ;
  
  /** number of remaining blank {@link Square}s (including temp) in the {@link Grid} */
  private int numBlankSqrs ;
  
  /** index of the current loaded game in the {@link Loader}.games array of {@link SavedGame}s */
  private int activeGameIndex ;
  
  /** Store a game obtained from the {@link Loader} */
  private SavedGame activeGame ;
  
  /** Store the current game solution obtained from the {@link Solver} */
  private SavedGame savedSolution ;
  
  // TODO - need this ?
  /** Keep a record of values obtained by AutoSolving  */
  private boolean[][] autoSolvedSqrs ;
  
  /** Used to prevent processing by {@link #keyTyped} of {@link KeyEvent}s
   *  already processed by {@link #keyPressed} */ 
  private boolean typeLock = false ;
  
  /** Used to center the grid in the containing Content Pane */ 
  private int paneWidth, paneHeight, xLocation, yLocation ;
  
  /** number of {@link Square}s on each side of the {@link Grid} */
  private int gridLength ;
  
  /** number of <B>PIXELS</B> on each side of the {@link Grid} */
  private int pxSize ;
  
  /** <b>TOTAL</b> number of {@link Square}s in the {@link Grid} */
  private int totalSqrs ;
  
  /** number of {@link Square}s on each side of a {@link Zone} */
  private int zoneLength ;
  
  /**
   * One of the {@link Grid} sub-sections, each comprising a <var>zoneLength x zoneLength</var> 
   * block of {@link Square}s in {@link #sqrs2dArray} <br>
   * - stored in a {@link Zone}[ {@link #gridLength} ] array
   */
  private Zone[] zones ;
  
  /**
   * One of the {@link Grid} sub-sections, each comprising a row of <var>gridLength</var>
   * {@link Square}s in {@link #sqrs2dArray} <br>
   * - stored in a {@link Row}[ {@link #gridLength} ] array
   */
  private Row[] rows ;
  
  /**
   * One of the {@link Grid} sub-sections, each comprising a column of <var>gridLength</var>
   * {@link Square}s in {@link #sqrs2dArray} <br>
   * - stored in a {@link Col}[ {@link #gridLength} ] array
   */
  private Col[] cols ;
  
  /**
   *  length of sides (in pixels) of each individual {@link Square}
   *  @see Launcher#getSqrLength
   */
  private int pxSqrSize ;
  
  // TODO - need this ?
  /** 2D array of individual {@link Square}s */
  Square[][] sqrs2dArray ;
  
  /**
   *  Reference to the current selected {@link Square}, if any <br>
   *  >> methods should <i>almost</i> &nbsp; <b>ALWAYS</b> use {@link #getActiveSqr} to access this field!
   *  @see #getActiveSqr
   */
  private Square activeSqr ;
  
  /** When need a {@link Square} reference and the active Square is not valid */
  private Square defaultSqr ;
  
  /** Keep track of value assignments to {@link Square}s for Undo & Redo functionality */
  private Vector<UndoMatrix> undoVector ;
  
  /** Keep track of whether there are any value assignments that have been UNDONE  */
  private boolean undoMode = false ;
  
  /** Current position in {@link #undoVector} for Undo/Redo actions  */
  private int undoPtr ;
  
  /**
   *  Separate thread for getting {@link Loader.SavedGame}s from {@link Loader}
   *  @see SwingWorker
   *  @see #createSolveWorker
   */
  private SwingWorker<Long, Void> solveWorker ;
  
  /**
   *  Have Games finished loading?
   *  @see SwingWorker#isDone
   *  @see #solveWorkerDone
   */
  private boolean savedSolnReady = false ;
  
  /**
   *  Did we receive a good Solution from the Solver?
   *  @see SwingWorker#isDone
   */
  private boolean savedSolnGood = false ;
  
  /**
   *  Elapsed time for Solver to find a solution <br>
   *  - returned by {@link #createSolveWorker}
   *  @see SwingWorker#doInBackground
   */
  private Long savedSolnTime ;
  
  /** Are we in 2-Color or 4-Color Solving mode?  */
  private int numActiveColors ;
  
  /** Are the Color Chains initialized yet?  */
  private boolean colorChainInit = false ;
  
  /** Individual Color chains  */
  static final int
                  NO_COLOR = -1 ,
                PINK_CHAIN =  0 ,
                BLUE_CHAIN =  1 ,
               GREEN_CHAIN =  2 ,
               BROWN_CHAIN =  3 ,
          NUM_COLOR_CHAINS =  4 ;
  
  /** Individual Color chain names  */
  static final String[] chainName = { "PINK", "BLUE", "GREEN", "BROWN" };
  
  /**
   *  Use in Solving 
   *  @see ColorChain
   */
  private ColorChain[] colorChain ;
  
  /** Total number of value assignments (GUESSES) - equals number of entries in the {@link #undoVector} */
  private int totalEntries ;
  
  /**
   *  Number of {@link Square}s with conflicting values
   *  @see Square#isConflicting
   */
  private int nConflicts ;
  
  /** Is there a temp value being entered in the Active Square? (Alt key pressed)  */
  private boolean tempMode = false ;
  
  /** Needed to keep track of ongoing temp entries, as Java has no static method variables */
  private int tempValue ;
  
  /**
   *  Used by the painting functionality
   *  @see #paintSquare(int,int,Square,Graphics)
   */
  private int paintValue, paintX, paintY ;
  
  /**
   *  Take the value of a {@link Square} and convert to a {@link String} for painting
   *  @see #paintSquare(int,int,Square,Graphics)
   */
  private String paintString ;
  
  /** reference to the enclosing instance of {@link Launcher} */
  static Launcher gameview ;
  
  /** Just in case...  */
  private static final long serialVersionUID = -6273242223031579498L;

}// Class Grid
