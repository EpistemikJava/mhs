/* ***************************************************************************************
 
   Mark Sattolo (epistemik@gmail.com)
 -----------------------------------------------------------------
 $File: //depot/Eclipse/Java/workspace/Pseudokeu/src/mhs/pseudokeu/Solver.java $
 $Revision: #10 $
 $Change: 174 $
 $DateTime: 2012/02/21 20:28:23 $
 -----------------------------------------------------------------
 
  Eclipse version created on Oct 22, 2009, 13:27 PM
  git version created Mar 8, 2014
 
*************************************************************************************** */

package mhs.pseudokeu;

import java.util.Date;
import java.util.logging.Level;

import mhs.pseudokeu.Loader.SavedGame;

/**
 * Solve Sudoku puzzles using the Knuth 'Dancing Links' strategy <br>
 * Port to Java & modifications (c) 2009 - 2014 by Mark Sattolo <br>
 * 
 * Original C code by Xi Chen <br> 
 * @see <a href="http://cgi.cse.unsw.edu.au/~xche635/dlx_sodoku">Dancing Links</a>
 * 
 * @author Mark Sattolo
 * @version 8.1.1
 */
class Solver
{
 /*
  *            I N N E R    C L A S S E S
  *************************************************************************************************************/
   
  /**
   * Internal class to hold the node info
   */
  class DlxNode
  {
    DlxNode()
    {
      cType = 'X' ;
      nID = 0 ;
    }
    
    void set( char name, int num )
    {
      cType = name ;
      nID = num ;
    }
    
    // references to other nodes in the matrix
    DlxNode header;
    DlxNode left;
    DlxNode right;
    DlxNode up;
    DlxNode down;
    
    // identify each node
    char cType;
    int  nID;
    
  } /* Inner class DlxNode  */
  
 /*
  *            C O N S T R U C T O R S
  *************************************************************************************************************/

  /**
   *  Only Constructor
   *  
   *  @param len - number of {@link Square}s along each side of the grid
   */
  public Solver( final int len )
  {
    gridLen = len ;
    nSqrs = gridLen * gridLen ;
    nCols = nSqrs * 4 ;
    nRows = (nSqrs * gridLen) + 1 ;
    
    sqrOffset = 0 ;
    rowOffset = nSqrs ;
    colOffset = nSqrs * 2 ;
    boxOffset = nSqrs * 3 ;
    
    nodeMatrix = new DlxNode[ nCols ][ nRows ];
    for( int i=0; i < nCols; i++ )
      for( int j=0; j < nRows; j++ )
        nodeMatrix[i][j] = new DlxNode();
    
    // contains references only
    arRowHdrs = new DlxNode[nRows] ;
    
    rootNode = new DlxNode();
    
    constraintMatrix = new int[nCols][nRows];
    
    arResults = new int[nSqrs] ;
  }
  
 /*
  *            M E T H O D S
  *************************************************************************************************************/
  
 // ===========================================================================================================
 //                          I N T E R F A C E
 // ===========================================================================================================
  
  /**
   *  Find and return the solution to the submitted {@link SavedGame}
   *  
   *  @param puzzle - from {@link Grid}
   *  @param solution - empty game for the results
   *  
   *  @return elapsed time in msecs
   *  
   *  @see Grid#activateGame
   */
  public static long getSolution( final SavedGame puzzle, SavedGame solution )
  {
    //TODO: Create a new SavedGame in this method and RETURN it
    
    logger = Launcher.logger ;
    
    if( puzzle == null || solution == null )
    {
      logger.severe( "Passed a null SavedGame!!??" );
      return( -1 );
    }
    
    // make sure this is running on a Worker thread
    logger.info( "Running on " + Thread.currentThread() );
    
    Solver $dlx = new Solver( puzzle.getLength() );
    
    $dlx.buildConstraintMatrix();
    $dlx.buildNodeMatrix();
    
    $dlx.loadGame( puzzle );
    
    Date $startDate = new Date();
    $dlx.search( 0 );
    Date $endDate = new Date();
    
    searchTime = $endDate.getTime() - $startDate.getTime();
    logger.info( "Search time was " + searchTime + " msecs" );
    
    $dlx.loadResults( solution );
    
    if( ! $dlx.checkSolution(solution) )
    {
      logger.warning( "PROBLEM WITH SOLUTION!!" );
      return( -1 * searchTime );
    }
    
    return searchTime ;
    
  }// getSolution()
  
 // end INTERFACE
 
 // ===========================================================================================================
 //                            P R I V A T E
 // ===========================================================================================================
  
  private int dataLeft ( final int i ) { return i - 1 < 0 ? nCols - 1 : i - 1; }
  private int dataRight( final int i ) { return (i + 1) % nCols; }
  private int dataUp   ( final int i ) { return i - 1 < 0 ? nRows - 1 : i - 1; }
  private int dataDown ( final int i ) { return (i + 1) % nRows; }
  
  /**
   * Build the Constraint Matrix
   */
  private void buildConstraintMatrix()
  {
    int $index, $valOffset ;
    
    for( int r=0; r < gridLen; r++ )
    {
      for( int c=0; c < gridLen; c++ )
      {
        for( int v=0; v < gridLen; v++ )
        {
          $index = getIndex( v, r, c );
          $valOffset = v * gridLen ;
          
          // Constraint 1: Only 1 number per Square
          constraintMatrix[sqrOffset + getSqr(r,c)][$index] = 1;
          
          // Constraint 2: Only 1 of each value per Row
          constraintMatrix[rowOffset + r + $valOffset][$index] = 1;
          
          // Constraint 3: Only 1 of each value per Column
          constraintMatrix[colOffset + c + $valOffset][$index] = 1;
          
          // Constraint 4: Only 1 of each value per 3x3 Box
          constraintMatrix[boxOffset + getBox(r,c) + $valOffset][$index] = 1;
        }
      }
    }
    
    for( int i=0; i < nCols; i++ )
    {
      constraintMatrix[i][nRows - 1] = 2;
    }
    
  }// buildConstraintMatrix()
  
  /**
   * Set all the references for the Node Matrix
   */
  private void buildNodeMatrix()
  {
    int $shift ;
    
    // Build a toroidal linkedlist matrix according to the constraints data
    for( int c = 0; c < nCols; c++ )
    {
      for( int r = 0; r < nRows; r++ )
      {
        // find the non-empty nodes in each direction to set the pointers
        if( constraintMatrix[c][r] != 0 )
        {
          // Left pointer
          $shift = c;
          do
          {
            $shift = dataLeft( $shift );
          }
          while( constraintMatrix[$shift][r] == 0 );
          
          nodeMatrix[c][r].left = nodeMatrix[$shift][r];
          
          // Right pointer
          $shift = c;
          do
          {
            $shift = dataRight( $shift );
          }
          while( constraintMatrix[$shift][r] == 0 );
          
          nodeMatrix[c][r].right = nodeMatrix[$shift][r];
          
          // Up pointer
          $shift = r;
          do
          {
            $shift = dataUp( $shift );
          }
          while( constraintMatrix[c][$shift] == 0 );
          
          nodeMatrix[c][r].up = nodeMatrix[c][$shift];
          
          // Down pointer
          $shift = r;
          do
          {
            $shift = dataDown( $shift );
          }
          while( constraintMatrix[c][$shift] == 0 );
          
          nodeMatrix[c][r].down = nodeMatrix[c][$shift];
          
          // Header pointer
          nodeMatrix[c][r].header = nodeMatrix[c][nRows - 1];
          nodeMatrix[c][r].nID = r ;
          
          // Row Headers
          arRowHdrs[r] = nodeMatrix[c][r];
          
        }// if( constraintMatrix[c][r] != 0 )
        
      }// for( Rows )
      
      // Columns
      nodeMatrix[c][nRows - 1].set( 'C', c );

    }// for( Cols )
    
    // set root
    rootNode.cType = '/' ;
    rootNode.left = nodeMatrix[nCols - 1][nRows - 1];
    rootNode.right = nodeMatrix[0][nRows - 1];
    
    // insert root
    nodeMatrix[nCols - 1][nRows - 1].right = rootNode;
    nodeMatrix[0][nRows - 1].left = rootNode;
    
    // set Row boundaries
    for( int i=0; i < rowOffset; i++ )
    {
      nodeMatrix[i + sqrOffset][nRows - 1].set( 'S', i );
      nodeMatrix[i + rowOffset][nRows - 1].set( 'R', i );
      nodeMatrix[i + colOffset][nRows - 1].set( 'C', i );
      nodeMatrix[i + boxOffset][nRows - 1].set( 'B', i );
    }
    
  }// buildNodeMatrix()
  
  /**
   * Cover a Row
   * 
   * @param colNode - node in a column
   */
  private void cover( DlxNode colNode )
  {
    if( colNode == null )
    {
      logger.severe( "Passed a null DlxNode!!??" );
      return ;
    }
    
    // remove this column from the matrix
    colNode.right.left = colNode.left;
    colNode.left.right = colNode.right;
    
    // remove all rows with this column
    for( DlxNode $downNode = colNode.down; $downNode != colNode; $downNode = $downNode.down )
    {
      for( DlxNode $rightNode = $downNode.right; $rightNode != $downNode; $rightNode = $rightNode.right )
      {
        $rightNode.up.down = $rightNode.down;
        $rightNode.down.up = $rightNode.up;
      }
    }
    
  }// cover()
    
  /**
   * UnCover a Row
   * 
   * @param colNode - node in a column
   */
  private void unCover( DlxNode colNode )
  {
    if( colNode == null )
    {
      logger.severe( "Passed a null DlxNode!!??" );
      return ;
    }
    
    // restore the rows
    for( DlxNode $upNode = colNode.up; $upNode != colNode; $upNode = $upNode.up )
    {
      for( DlxNode $leftNode = $upNode.left; $leftNode != $upNode; $leftNode = $leftNode.left )
      {
        $leftNode.up.down = $leftNode;
        $leftNode.down.up = $leftNode;
      }
    }
    
    // restore the column
    colNode.right.left = colNode;
    colNode.left.right = colNode;
    
  }// unCover()
  
  /**
   *  Insert the value and location of a {@link DlxNode} in the results <br>
   *  - called by {@link #loadGame}
   *  
   *  @param val = 0 - 8
   *  @param row = 0 - 8
   *  @param col = 0 - 8
   */
  private void includeNode( final int val, final int row, final int col )
  {
    int index = getIndex( val, row, col );
      logger.fine( " (index = " + index + ")" );
    
    // remove from the matrix
    DlxNode $hdrNode = arRowHdrs[index] ;
    cover( $hdrNode.header );
    for( DlxNode $rightNode = $hdrNode.right; $rightNode != $hdrNode; $rightNode = $rightNode.right )
    {
      cover( $rightNode.header );
    }
    
    // insert in the results array
    arResults[nResults++] = index ;
    
    nInitialVals++ ;
    
  }// includeNode()
  
  /**
   * extract data from the given 3-digit index number in the format [Val(0-8)|Row(0-8)|Col(0-8)] 
   * @param n - index
   * @return value
   */
  int getVal( final int n )
  { return n / nSqrs ; }
  
  /**
   * extract data from the given 3-digit index number in the format [Val(0-8)|Row(0-8)|Col(0-8)] 
   * @param n - index
   * @return value
   */
  int getRow( final int n )
  { return (n / gridLen) % gridLen ; }
  
  /**
   * extract data from the given 3-digit index number in the format [Val(0-8)|Row(0-8)|Col(0-8)] 
   * @param n - index
   * @return value
   */
  int getCol( final int n )
  { return n % gridLen ; }
  
  /**
   * find the required index from row and col info 
   * @param r - row info
   * @param c - col info
   * @return index
   */
  int getBox( final int r, final int c )
  { return( ((r/3) * 3) + (c/3) ); }
  
  /**
   * find the required index from row and col info 
   * @param r - row info
   * @param c - col info
   * @return index
   */
  int getSqr( final int r, final int c )
  { return( (r * gridLen) + c ); }
  
  /**
   *  Get the 3-digit integer index from the parameter info <br>
   *  - max value = (8 * 81) + (8 * 9) + 8 = 648 + 72 + 8 = 728
   *  
   *  @param val = 0 - 8
   *  @param row = 0 - 8 
   *  @param col = 0 - 8
   *  
   *  @return index
   */
  int getIndex( final int val, final int row, final int col )
  { return (val * nSqrs) + (row * gridLen) + col; }
  
  /**
   *  Load a game to the node matrix
   *  
   *  @param game - {@link SavedGame} to load
   */
  private void loadGame( final SavedGame game )
  {
    if( game == null )
    {
      logger.severe( "Passed a null SavedGame!!??" );
      return ;
    }
    
    int $val ;
    int $len = game.getLength();
    for( int $row=0; $row < $len; $row++ )
      for( int $col=0; $col < $len; $col++ )
      {
        $val = game.getValue( $row, $col );
        if( $val != SqrTypes.BLANK_VAL )
          includeNode( $val-1, $row, $col );
      }
    
  }// loadGame()
  
  /**
   *  Recursively search the node matrix for a solution
   *  
   *  @param nodeCount - number of nodes
   */
  private void search( final int nodeCount )
  {
    loops++ ;
    
    if( ( (rootNode.left == rootNode) && (rootNode.right == rootNode) ) || (nodeCount == (nSqrs - nInitialVals)) )
    {
      // valid solution!
      logger.info( "\n ---- SOLUTION FOUND! ---- \n (" + loops + " search loops)" );
      displayResults( Level.INFO );
      
      done = true ;
      return;
    }
    
    if( (nodeCount < gridLen) || (update < nodeCount) || (loops % 50 == 0) )
    {
      logger.fine( " search( " + nodeCount + " ) / loops = " + loops );
      displayResults( Level.FINE );
      update = nodeCount;
    }
    
    DlxNode $nextNode = rootNode.right ;
    // remove this column
    cover( $nextNode );
    
    int $val, $row, $col ;
    DlxNode $rightNode, $downNode ;
    for( $downNode = $nextNode.down; ($downNode != $nextNode) && !done; $downNode = $downNode.down )
    {
      // try this node
      arResults[nResults++] = $downNode.nID;
      
      $val = getVal( $downNode.nID );
      $row = getRow( $downNode.nID );
      $col = getCol( $downNode.nID );
      logger.fine( " >> try " +($val+1)+ " at r" +($row+1)+ "c" +($col+1)+ " (node " +(nodeCount+1)+ ")" );
      
      for( $rightNode = $downNode.right; $rightNode != $downNode; $rightNode = $rightNode.right )
      {
        // remove any other columns in this column's rows
        cover( $rightNode.header );
      }
      
      /* recursively try the next node  */
      search( nodeCount + 1 );
      
      if( ! done )
      {
        // Ok, that node didn't work
        logger.fine( "Ok, " + ($val+1) + " at r" + ($row+1) + "c" + ($col+1) + " (node " + (nodeCount+1) + ") didn't work..." );
        
        for( $rightNode = $downNode.right; $rightNode != $downNode; $rightNode = $rightNode.right )
        {
          unCover( $rightNode.header );
        }
        
        arResults[--nResults] = 0;
      }
    }// for loop
    
    logger.fine( " >> Finished search(" + nodeCount + "): next$node = " + $nextNode.cType + $nextNode.nID
                  + "; down$Node = " + $downNode.cType + $downNode.nID );
    
    if( ! done )
      unCover( $nextNode );
    else
        logger.fine( ">> DONE!" );
    
  }// search()
  
  /**
   *  Transfer the results matrix to a SavedGame
   *  
   *  @param game - {@link SavedGame} to load
   */
  private void loadResults( SavedGame game )
  {
    if( game == null )
    {
      logger.severe( "Passed a null SavedGame!!??" );
      return ;
    }
    
    // put the current results in a SavedGame
    for( int n = 0; n < nResults; n++ )
      game.setValue( getRow(arResults[n]), getCol(arResults[n]), getVal(arResults[n])+1 );
    
  }// loadResults()
  
  /**
   *  Check a proposed solution
   *  
   *  @param game to check
   *  @return success or failure 
   */
  private boolean checkSolution( final SavedGame game )
  {
    if( game == null )
    {
      logger.severe( "Passed a null SavedGame!!??" );
      return false ;
    }
    
    boolean $solnOK = true ;
    int $val, $count ;
    int $box1, $box2 ;
    int $boxLength = (int)Math.round( Math.sqrt(gridLen) );
    
    logger.appendln();
    loop:
    for( int r=0; r < gridLen; r++ )
      for( int c=0; c < gridLen; c++ )
      {
        $count = 0 ;
        $val = game.getValue( r, c );
        logger.appendln( " Check Sqr r" + r + "c" + c + "; val == " + $val );
        for( int k=0; k < gridLen; k++ )
        {
          logger.appendln( "\t k == " + k );
          // check row
          if( k != c  )
          {
            if( game.getValue(r,k) == $val )
            {
              logger.appendln( ">> PROBLEM at Sqr R" + r + "c" + k + "; val == " + $val );
              $solnOK = false ;
              break loop ;
            }
            logger.appendln( "\t\t OK at Sqr R" + r + "c" + k );
            $count++ ;
          }
          // check col
          if( k != r )
          {
            if( game.getValue(k,c) == $val )
            {
              logger.appendln( ">> PROBLEM at Sqr r" + k + "C" + c + "; val == " + $val );
              $solnOK = false ;
              break loop ;
            }
            logger.appendln( "\t\t OK at Sqr r" + k + "C" + c );
            $count++ ;
          }
          // check box
          $box1 = ( (r/$boxLength) * $boxLength ) + ( k / $boxLength );
          $box2 = ( (c/$boxLength) * $boxLength ) + ( k % $boxLength );
          if( ($box1 != r) && ($box2 != c) )
          {
            if( game.getValue($box1,$box2) == $val )
            {
              logger.appendln( ">> PROBLEM at Sqr r" + $box1 + "c" + $box2 + "; val == " + $val );
              $solnOK = false ;
              break loop ;
            }
            logger.appendln( "\t\t OK at Sqr r" + $box1 + "c" + $box2 );
            $count++ ;
          }
        }// for( k = 0-8 )
        
        if( $count < 20 ) logger.appendln( "!! Count PROBLEM at Sqr r" + r + "c" + c );
        
      }// for( col = 0-8 )
    
    if( $solnOK )
    {
      logger.send( Level.FINER );
      logger.info( ">> Solution OK!" );
    }
    else
    {
      logger.send( Level.INFO );
      logger.severe( "!! Solution NOT OK ??" );
    }
    
    return $solnOK ;
    
  }// checkSolution()
  
 // ===========================================================================================================
 //                             D E B U G    C O D E
 // ===========================================================================================================
   
  String myname() { return getClass().getSimpleName(); }
  
  /**
   *  Display a puzzle
   *  
   *  @param lev - Level to display at
   */
  private void displayResults( final Level lev )
  {
    Level level = lev ;
    
    if( lev == null )
    {
      logger.severe( "Passed a null Level!!??" );
      level = LogControl.DEFAULT_LEVEL ;
    }
    
    int r, c, i ;
    int[][] $tempAr = new int[gridLen][gridLen] ;
    
    for( r = 0; r < gridLen; r++ )
      for( c = 0; c < gridLen; c++ )
        $tempAr[r][c] = -1 ;
    
    // put the current results in a temp array
    for( r = 0; r < nResults; r++ )
      $tempAr[ getRow(arResults[r]) ][ getCol(arResults[r]) ] = getVal( arResults[r] );
    
    logger.appendln();
    for( r = 0; r < gridLen; r++ )
    {
      logger.append( "\t" );
      for( c = 0; c < gridLen; c++ )
      {
        if( (r > 0) && (r % 3 == 0) && (c == 0) ) // horizontal lines
        {
          for( i = 0; i < 5; i++ )
            logger.append( "----" );
          logger.append( "---\n\t" );
        }
        
        if( $tempAr[r][c] >= 0 )
          logger.append( String.valueOf($tempAr[r][c]+1) ); // value
        else
          logger.append( "." ); // blank
        
        logger.append( c % 3 == 2 ? " | " : " " ); // vertical lines
        
      }
      logger.appendln();
    }
    logger.send( level );
    
  }// displayResults()
  
 /*
  *            F I E L D S
  *************************************************************************************************************/
  
  private final int gridLen ,
                      nSqrs ,
                      nCols ,
                      nRows ;
  
  private final int sqrOffset ,
                    rowOffset ,
                    colOffset ,
                    boxOffset ;
  
  private int nInitialVals ;
  
  private DlxNode     rootNode ;
  private DlxNode[]   arRowHdrs ;
  private DlxNode[][] nodeMatrix ;
  
  private int     nResults ;
  private int[]   arResults ;
  private int[][] constraintMatrix ;
  
  private int loops ;
  private int update ;
  private boolean done ;
  
  private static long searchTime ;
  
  private static PskLogger logger ;
  
}// class Solver
