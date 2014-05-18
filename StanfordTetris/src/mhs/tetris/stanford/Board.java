/* ***************************************************************************************
 * 
 *  Mark Sattolo (epistemik@gmail.com) 
 * -----------------------------------------------
 * $File: //depot/Eclipse/Java/workspace/StanfordTetris/src/mhs/tetris/stanford/Board.java $
 * $Revision: #3 $ 
 * $Change: 166 $ 
 * $DateTime: 2012/01/02 22:14:27 $
 * -----------------------------------------------
 * 
 * mhs.tetris.stanford.Board.java 
 * Eclipse version created on Jan 2, 2012
 * 
 * ***************************************************************************************
 */

package mhs.tetris.stanford;

import java.awt.*;

/**
 * Represents a Tetris board -- essentially a 2-d grid of booleans. Supports tetris pieces
 * and row clearning. Has an "undo" feature that allows clients to add and remove pieces
 * efficiently. Does not do any drawing or have any idea of pixels. Intead, just
 * represents the abtsract 2-d board. See Tetris-Architecture.html for an overview.
 * 
 * This is the starter file version -- a few simple things are filled in already
 * 
 * @author Nick Parlante
 * @version 1.0, Mar 1, 2001
 */
final class Board
{
  /**
   * Creates an empty board of the given width and height measured in blocks.
   * @param aWidth in blocks
   * @param aHeight in blocks
   */
  Board( int aWidth, int aHeight )
  {
    width = aWidth;
    height = aHeight;

    grid = new boolean[ width ][ height ];
    widths = new int[ height ];
    heights = new int[ width ];

    xgrid = new boolean[ width ][ height ];
    xwidths = new int[ height ];
    xheights = new int[ width ];

    committed = true;
    
  }// CONSTRUCTOR

 /*
  *    M E T H O D S
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
     
  /** @return the width of the board in blocks  */
  int getWidth() { return width ;}

  /** @return the height of the board in blocks  */
  int getHeight() { return height ;}

  /** @return the max column height present in the board. For an empty board this is 0.  */
  int getMaxHeight()
  {
    int $max = 0;
    for( int i = 0; i < width; i++ )
    {
      if( heights[i] > $max )
        $max = heights[i];
    }
    return $max;
  }

  /** Checks the board for internal consistency -- used for debugging.  */
  void sanityCheck()
  {
    if( DEBUG )
    {
      // consistency check the board state
      // check widths[]
      int $temp = 0;
      for( int i = 0; i < height; i++ )
      {
        for( int j = 0; j < width; j++ )
        {
          if( grid[j][i] )
            $temp++;
        }
        if( $temp != widths[i] )
          throw new RuntimeException( "Widths[] array is CrAzY!" );
        $temp = 0;
      }
      
      // check heights[]
      $temp = 0;
      int $max = 0;
      for( int i = 0; i < width; i++ )
      {
        for( int j = height - 1; j >= 0; j-- )
        {
          if( grid[i][j] )
          {
            $temp = j + 1;
            if( j + 1 > $max )
              $max = j + 1;
            break;
          }
        }
        if( $temp != heights[i] )
        {
          throw new RuntimeException( "Heights[] array is CrAzY!" );
        }
        $temp = 0;
      }
      
      // check max height
      if( getMaxHeight() != $max )
      {
        throw new RuntimeException( "HeightMax is CRAzY!!!!" );
      }
    }
  }// sanityCheck()
  
  /**
   * Given a piece and an x, returns the y value where the piece would come to rest 
   * if it were dropped straight down at that x.
   * <br>
   * Implementation: use the skirt and the col heights to compute this fast -- O(skirt length).
   * 
   * @param piece to drop
   * @param x - location to drop
   * @return landing location
   */
  int dropHeight( Piece piece, int x )
  {
    int $high = 10000;
    int $result = 0;
    int $temp;
    int i;
    int[] $skirtAr = piece.getSkirt();
    for( i = x; i < x + piece.getWidth(); i++ )
    {
      $temp = $skirtAr[i - x] - heights[i];
      if( $temp < $high )
      {
        $high = $temp;
        $result = i;
      }
    }
    return heights[$result] - $skirtAr[$result - x];
    
  }// dropHeight()
  
  /**
   * The height is 0 if the column contains no blocks.
   * @param x - position to get height
   * @return the height of the given column -- i.e. the y value of the highest block + 1.
   */
  int getColumnHeight( int x ) { return heights[x] ;}
  
  /** @param y - postion to get width
   *  @return the number of filled blocks in the given row  */
  int getRowWidth( int y ) { return widths[y] ;}
  
  /**
   * Blocks outside of the valid width/height area always return true.
   * @param x - position in board
   * @param y  - position in board
   * @return true if the given block is filled in the board.
   */
  final boolean getGrid( int x, int y )
  {
    if( x < 0 || x > width )
      return true;
    if( y < 0 || y > height )
      return true;
    return grid[x][y];
    
  }// getGrid()
  
  /**
   * Attempts to add the body of a piece to the board. Copies the piece blocks into the
   * board grid. Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED for a
   * regular placement that causes at least one row to be filled.
   * <p>
   * Error cases: If part of the piece would fall out of bounds, the placement does not
   * change the board at all, and PLACE_OUT_BOUNDS is returned. If the placement is "bad"
   * --interfering with existing blocks in the grid -- then the placement is halted
   * partially complete and PLACE_BAD is returned. An undo() will remove the bad placement.
   * 
   * @param piece to place
   * @param x - horizontal position
   * @param y - vertical position
   * @return - placed location
   */
  int place( Piece piece, int x, int y )
  {
    Point[] $bodyAr = piece.getBody();
    int $putX;
    int $putY;
    boolean $rowFilled = false;
    
    if( !committed )
                    throw new RuntimeException( "Trying to place on uncommited board!!!" );
    
    committed = false;
    backup();
    
    // might as well do all the error checking at once, 
    // since we must leave the board unchanged if we've got an out of bounds error 
    // - this has the added bonus of not altering the board on a bad place
    for( int i = 0; i < $bodyAr.length; i++ )
    {
      $putX = x + $bodyAr[i].x;
      $putY = y + $bodyAr[i].y;
      if( $putX < 0 || $putX >= width )
        return PLACE_OUT_BOUNDS;
      if( $putY < 0 || $putY >= height )
        return PLACE_OUT_BOUNDS;
      if( grid[$putX][$putY] )
        return PLACE_BAD;
    }
    
    for( int i = 0; i < $bodyAr.length; i++ )
    {
      $putX = x + $bodyAr[i].x;
      $putY = y + $bodyAr[i].y;
      
      grid[$putX][$putY] = true;
      widths[$putY]++;
      if( $putY + 1 > heights[$putX] )
        heights[$putX] = $putY + 1;
      
      if( widths[$putY] >= width )
        $rowFilled = true;
    }
    
    sanityCheck();
    
    if( $rowFilled )
      return PLACE_ROW_FILLED;
    
    return PLACE_OK;
    
  }// place()
  
  /**
   * Deletes rows that are filled all the way across, moving things above down.<br>
   * <b>Implementation:</b> This is complicated. Ideally, you want to copy each row down to its
   * correct location in one pass. Note that more than one row may be filled.
   * 
   * @return true if any row clearing happened.
   */
  boolean clearRows()
  {
    boolean $cleared = false;
    
    if( committed )
      backup(); // not calling immediately after place
    committed = false;
    
    for( int i = 0; i < height; i++ )
    {
      if( widths[i] >= width )
      {
        $cleared = true;
        
        for( int j = 0; j < width; j++ )
        {
          System.arraycopy( grid[j], i + 1, grid[j], i, height - 1 - i );
          grid[j][height - 1] = false;
        }
        System.arraycopy( widths, i + 1, widths, i, height - 1 - i );
        widths[height - 1] = 0;
        i--;
      }
    }
    
    if( $cleared )
      recalcHeights();
    
    sanityCheck();
    return $cleared;
    
  }// clearRows()
  
  private void recalcHeights()
  {
    int j;
    boolean $sent = false;
    
    for( int i = 0; i < width; i++ )
    {
      $sent = false;
      for( j = height - 1; j >= 0; j-- )
      {
        if( grid[i][j] && !$sent )
        {
          heights[i] = j + 1;
          $sent = true;
        }
      }
      if( !$sent )
        heights[i] = 0;
    }
    
  }// recalcHeights()
  
  /**
   * If a place() happens, optionally followed by a clearRows(), a subsequent undo()
   * reverts the board to its state before the place(). If the conditions for undo() are
   * not met, such as calling undo() twice in a row, then the second undo() does nothing.
   * See the overview docs.
   */
  void undo()
  {
    if( !committed )
    {
      int[] $tempAr;
      boolean[][] $temp2dAr;
      
      $tempAr = widths;
      widths = xwidths;
      xwidths = $tempAr;
      
      $tempAr = heights;
      heights = xheights;
      xheights = $tempAr;
      
      $temp2dAr = grid;
      grid = xgrid;
      xgrid = $temp2dAr;
      
      /*
       * for(int i = 0; i < width; i++) { System.arraycopy(xgrid[i],0,grid[i],0,height); }
       * System.arraycopy(xwidths,0,widths,0,height);
       * System.arraycopy(xheights,0,heights,0,width);
       */
      committed = true;
    }
    sanityCheck();
    
  }// undo()

  private void backup()
  {
    for( int i = 0; i < width; i++ )
    {
      System.arraycopy( grid[i], 0, xgrid[i], 0, height );
    }
    System.arraycopy( widths, 0, xwidths, 0, height );
    System.arraycopy( heights, 0, xheights, 0, width );
    
  }// backup()

  /** Puts the board in the committed state. See the overview docs.  */
  void commit()
  {
    if( !committed )
    {
      /*
       * for(int i = 0; i < width; i++) { System.arraycopy(grid[i],0,xgrid[i],0,height); }
       * System.arraycopy(widths,0,xwidths,0,height);
       * System.arraycopy(heights,0,xheights,0,width);
       */
      committed = true;
    }
  }// commit()
  
 /*
  *    F I E L D S
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    
  private int width;
  private int height;

  private int[] widths;
  private int[] heights;

  private boolean[][] grid;
  private boolean[][] xgrid;

  private int[] xwidths;
  private int[] xheights;
/*
  private int scol; // start col for backup

  private int ecol; // end col for backup

  private boolean fast_undo = true;
*/
  private boolean committed;
  private boolean DEBUG = false;

  static final int
                  PLACE_OK = 0 ,
          PLACE_ROW_FILLED = 1 ,
          PLACE_OUT_BOUNDS = 2 ,
                 PLACE_BAD = 3 ;

}// class Board
