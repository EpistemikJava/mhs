/* ***************************************************************************************
 * 
 *  Mark Sattolo (epistemik@gmail.com) 
 * -----------------------------------------------
 * $File: //depot/Eclipse/Java/workspace/StanfordTetris/src/mhs/tetris/stanford/Brain.java $
 * $Revision: #4 $ 
 * $Change: 166 $ 
 * $DateTime: 2012/01/02 22:14:27 $
 * -----------------------------------------------
 * 
 * mhs.tetris.stanford.Brain.java 
 * Eclipse version created on Jan 2, 2012
 * 
 * ***************************************************************************************
 */

package mhs.tetris.stanford ;

/** Play the game without human interaction  */
interface Brain 
{
	/**
	 *  Move is used as a struct to store a single Move
	 *  ("static" here means it does not have a pointer to an
	 *  enclosing Brain object, it's just in the Brain namespace.)
	 */
	static class Move 
	{
		int x;
		int y;
		Piece piece;
		double score;	// lower scores are better
	}
	
	/**
	 * Given a piece and a board, returns a move object that represents
	 *   the best play for that piece, or returns null if no play is possible.
	 * The board should be in the committed state when this is called.
	 * "limitHeight" is the bottom section of the board that where pieces must come to rest -- typically 20.
	 * 
	 * @param board - in play
	 * @param piece - to move
	 * @param limitHeight - from game
	 * @return move 
	*/
	Brain.Move bestMove( Board board, Piece piece, int limitHeight );

}// interface Brain
