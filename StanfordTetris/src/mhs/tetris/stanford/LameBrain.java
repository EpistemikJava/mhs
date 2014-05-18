/* ***************************************************************************************
 * 
 *  Mark Sattolo (epistemik@gmail.com) 
 * -----------------------------------------------
 * $File: //depot/Eclipse/Java/workspace/StanfordTetris/src/mhs/tetris/stanford/LameBrain.java $
 * $Revision: #4 $ 
 * $Change: 166 $ 
 * $DateTime: 2012/01/02 22:14:27 $
 * -----------------------------------------------
 * 
 * mhs.tetris.stanford.LameBrain.java 
 * Eclipse version created on Jan 2, 2012
 * 
 * ***************************************************************************************
 */

package mhs.tetris.stanford ;

/**
 * A simple Brain implementation. bestMove() iterates through all the possible x values
 * and rotations to play a particular piece (there are only around 10-30 ways to play a piece).
 * <br>
 * For each play, it uses the rateBoard() message to rate how good the resulting board is
 * and it just remembers the play with the lowest score. Undo() is used to back-out each
 * play before trying the next. To experiment with writing your own brain -- just subclass
 * off LameBrain and override rateBoard().
 */
class LameBrain implements Brain
{
 /*
  *    M E T H O D S
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
  
  /**
   * Given a piece and a board, returns a move object that represents the best play for
   * that piece, or returns null if no play is possible. 
   * See the Brain interface for details.
   */
  public Brain.Move bestMove( Board board, Piece piece, int limitHeight )
  {
    Brain.Move $move = new Brain.Move();
    Piece $bestPiece = null;
    Piece $current = piece;
    
    double $bestScore = 1e20;
    int $bestX = 0;
    int $bestY = 0;
    
    // loop through all the rotations
    while( true )
    {
      final int $yBound = limitHeight - $current.getHeight() + 1;
      final int $xBound = board.getWidth() - $current.getWidth() + 1;
      
      // For current rotation, try all the possible columns
      for( int x = 0; x < $xBound; x++ )
      {
        int y = board.dropHeight( $current, x );
        if( y < $yBound )
        { // piece does not stick up too far
          int $result = board.place( $current, x, y );
          if( $result <= Board.PLACE_ROW_FILLED )
          {
            if( $result == Board.PLACE_ROW_FILLED )
              board.clearRows();
            
            double $score = rateBoard( board );
            if( $score < $bestScore )
            {
              $bestScore = $score;
              $bestX = x;
              $bestY = y;
              $bestPiece = $current;
            }
          }
          
          board.undo(); // back out of that play, loop around for the next
        }
      }
      
      $current = $current.nextRotation();
      if( $current == piece )
        break; // break if back to original rotation
    }
    
    if( $bestPiece == null )
      return null ; // could not find a play at all!
    
    $move.x = $bestX;
    $move.y = $bestY;
    $move.piece = $bestPiece;
    $move.score = $bestScore;
    
    return $move ;
    
  }// bestMove()
  
  /**
   * A simple brain function. Given a board, produce a number that rates that board
   * position -- larger numbers for worse boards. This version just counts the height and
   * the number of "holes" in the board. See Tetris-Architecture.html for brain ideas.
   * @param board - in play
   * @return rating
   */
  double rateBoard( Board board )
  {
    final int $width = board.getWidth();
    final int $maxHeight = board.getMaxHeight();
    
    int $sumHeight = 0;
    int $holes = 0;
    
    // Count the holes, and sum up the heights
    for( int x = 0; x < $width; x++ )
    {
      final int $colHeight = board.getColumnHeight( x );
      $sumHeight += $colHeight;
      
      int y = $colHeight - 2; // addr of first possible hole
      
      while( y >= 0 )
      {
        if( !board.getGrid( x, y ) )
        {
          $holes++;
        }
        y--;
      }
    }
    
    double $avgHeight = ( (double)$sumHeight ) / $width;
    
    // Add up the counts to make an overall score
    // The weights, 8, 40, etc., are just made up numbers that appear to work
    return ( 8 * $maxHeight + 40 * $avgHeight + 1.25 * $holes );
    
  }// rateBoard()

}// class LameBrain
