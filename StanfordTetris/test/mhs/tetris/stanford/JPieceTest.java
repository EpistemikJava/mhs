/* ***************************************************************************************
 * 
 *  Mark Sattolo (epistemik@gmail.com)
 * -----------------------------------------------
 * $File: //depot/Eclipse/Java/workspace/StanfordTetris/test/mhs/tetris/stanford/JPieceTest.java $
 * $Revision: #2 $ 
 * $Change: 165 $ 
 * $DateTime: 2012/01/02 13:10:31 $
 * -----------------------------------------------
 * 
 * mhs.tetris.stanford.JPieceTest.java 
 * Eclipse version created on Jan 2, 2012
 * 
 * ***************************************************************************************
 */

package mhs.tetris.stanford;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * Debugging client for the Piece class. 
 * The JPieceTest component draws all the rotations of a tetris piece. 
 * JPieceTest.main() creates a frame with one JPieceTest for each of the 7 standard tetris pieces.
 * 
 * This is the starter file version -- The outer shell is done.
 * You need to complete paintComponent() and drawPiece().
 */
class JPieceTest extends JComponent
{
  public JPieceTest( Piece piece, int width, int height )
  {
    super();
    
    setPreferredSize( new Dimension(width, height) );
    
    root = piece;
    
  }// CONSTRUCTOR

  /** Draws the rotations from left to right. Each piece goes in its own little box.  */
  public void paintComponent( Graphics page )
  {
    Piece $cur = root;
    Rectangle $rect = new Rectangle();
    $rect.y = 0;
    
    if( getWidth() / 4 > getHeight() )
    {
      $rect.width = getHeight();
      $rect.height = $rect.width;
    }
    else
    {
      $rect.width = getWidth() / 4;
      $rect.height = $rect.width;
    }
    
    for( int i = 0; i < 4; i++ )
    {
      $rect.x = i * ( getWidth() / 4 );
      drawPiece( page, $cur, $rect );
      $cur = $cur.nextRotation();
      if( $cur == root )
        break;
    }
    
  }// paintComponent()

  /** Draw the piece inside the given rectangle  */
  private void drawPiece( Graphics page, Piece piece, Rectangle rect )
  {
    // note that the internal piece representation is opposite the screen coords in y
    Point[] $drawAr = piece.getBody();
    
    for( int i = 0; i < $drawAr.length; i++ )
    {
      if( piece.getSkirt()[$drawAr[i].x] == $drawAr[i].y )
      {
        page.setColor( Color.yellow );
      }
      else
      {
        page.setColor( Color.black );
      }
      
      page.fillRect( rect.x + $drawAr[i].x * ( rect.width / 4 ) + 1,
                     rect.y + rect.height - ( $drawAr[i].y + 1 ) * ( rect.height / 4 ) + 1,
                     ( rect.width / 4 ) - 1,
                     ( rect.height / 4 ) - 1 
                   );
    }
    page.setColor( Color.red );
    page.drawString( "w:" + piece.getWidth() + " h:" + piece.getHeight(), rect.x, rect.y + rect.height );
    
  }// drawPiece()

  /**
   * Draws all the pieces by creating a JPieceTest for each piece, and putting them all in a frame.
   * @param args - from command line
   */
  static public void main( String[] args )
  {
    JFrame $frame = new JFrame( "Piece Tester" );
    JComponent $container = (JComponent)$frame.getContentPane();
    
    // Put in a BoxLayout to make a vertical list
    $container.setLayout( new BoxLayout( $container, BoxLayout.Y_AXIS ) );
    
    Piece[] $pieceAr = Piece.getPieces();
    
    for( int i = 0; i < $pieceAr.length; i++ )
    {
      JPieceTest $test = new JPieceTest( $pieceAr[i], 375, 75 );
      $container.add( $test );
    }
    
    // Size the window and show it on screen
    $frame.pack();
    $frame.setVisible( true );
    
    // Quit on window close
    $frame.addWindowListener( new WindowAdapter() {
      public void windowClosing( WindowEvent e )
      {
        System.exit( 0 );
      }
    } );
  }
  
  protected Piece root;
  
  static final int MAX_ROTATIONS = 4;
  
  /** keep the compiler from complaining */
  private static final long serialVersionUID = -1938011970392936777L;

}// class JPieceTest
