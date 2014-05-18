/* ***************************************************************************************
 * 
 *  Mark Sattolo (epistemik@gmail.com) 
 * -----------------------------------------------
 * $File: //depot/Eclipse/Java/workspace/StanfordTetris/src/mhs/tetris/stanford/Piece.java $
 * $Revision: #4 $ 
 * $Change: 166 $ 
 * $DateTime: 2012/01/02 22:14:27 $
 * -----------------------------------------------
 * 
 * mhs.tetris.stanford.Piece.java 
 * Eclipse version created on Jan 2, 2012
 * 
 * ***************************************************************************************
 */

package mhs.tetris.stanford;

import java.awt.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * An immutable representation of a tetris piece in a particular rotation. Each piece is
 * defined by the blocks that make up its body. See the Tetris-Architecture.html for an overview.
 * <br>
 * Typical client looks like:
 * <pre>
 * Piece[] pieces = Piece.getPieces(); // the array of base pieces
 * 
 * Piece p = pieces[0]; // get piece 0
 * 
 * int width = p.getWidth(); // get its width
 * 
 * Piece next = p.nextRotation(); // get the next rotation of piece 0
 * </pre>
 * 
 * @author Mark Sattolo, based on original code by Nick Parlante
 * @version $Revision: #4 $
 */
final class Piece
{
  /*
   * Implementation notes: -The starter code specs out a few simple things, but leaves the
   * key algorithms for you. -Store the body as a Point[] array -The ivars in the Point
   * class are .x and .y -Do not assume there are 4 points in the body -- use array.length
   * to keep the code general
   */

  /**
   * Defines a new piece given the Points that make up its body. Makes its own copy of the
   * array and the Points inside it. Does not set up the rotations.
   * 
   * This constructor is PRIVATE -- if a client wants a piece object, they must use
   * Piece.getPieces().
   */
  private Piece( Point[] points )
  {
    body = new Point[ points.length ];
    for( int i = 0; i < points.length; i++ )
    {
      body[i] = new Point();
      body[i] = points[i];
    }
    
  }// CONSTRUCTOR

 /*
  *    M E T H O D S
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
     
  /** @return the width of the piece measured in blocks  */
  int getWidth() { return width ;}

  /** @return the height of the piece measured in blocks  */
  int getHeight() { return height ;}

  /** @return a pointer to the piece's body.
   *  The caller should not modify this array.  */
  Point[] getBody() { return body ;}

  /**
   * For each x value across the piece, the skirt gives the lowest y value in the body.
   * This useful for computing where the piece will land. 
   * The caller should not modify this array.
   * @return a pointer to the piece's skirt. 
   */
  int[] getSkirt() { return skirt ;}

  /**
   * Implementation: The Piece class pre-computes all the rotations once. 
   * This method just hops from one pre-computed rotation to the next in constant time.
   * @return a piece that is 90 degrees counter-clockwise rotated from the receiver.
   */
  Piece nextRotation() { return next ;}

  /**
   * Interestingly, this is not the same as having exactly the same body arrays, since the
   * points may not be in the same order in the bodies. 
   * Used internally to detect if two rotations are effectively the same.
   * @return true if two pieces are the same -- their bodies contain the same points.
   */
  public boolean equals( Object obj )
  {
    // standard equals() technique 1
    if( obj == this )
      return true ;
    
    // standard equals() technique 2
    // (null will be false)
    if( !( obj instanceof Piece ) )
      return false ;
    
    Piece $pce = (Piece)obj ;
    
    if( ($pce).body.length != this.body.length )
      return false;
    
    // your code
    Collection<Point> $setA = new HashSet<>();
    Collection<Point> $setB = new HashSet<>();
    
    for( int i = 0; i < this.body.length; i++ )
    {
      $setA.add( ($pce).body[i] );
      $setB.add( this.body[i] );
    }
    return( $setA.equals($setB) );
    
  }// equals()
  
  /**
   * Returns an array containing the first rotation of each of the 7 standard tetris
   * pieces. The next (counterclockwise) rotation can be obtained from each piece with the
   * {@link #nextRotation()} message. In this way, the client can iterate through all the
   * rotations until eventually getting back to the first rotation. (provided code)
   * @return array with pieces
   */
  static Piece[] getPieces()
  {
    // lazy evaluation -- create array if needed
    if( pieces == null )
    {
      // use pieceRow() to compute all the rotations for each piece
      pieces = new Piece[]
        {
          pieceRow( new Piece( parsePoints( "0 0 0 1 0 2 0 3" ) ) ), // 0
          pieceRow( new Piece( parsePoints( "0 0 0 1 0 2 1 0" ) ) ), // 1
          pieceRow( new Piece( parsePoints( "0 0 1 0 1 1 1 2" ) ) ), // 2
          pieceRow( new Piece( parsePoints( "0 0 1 0 1 1 2 1" ) ) ), // 3
          pieceRow( new Piece( parsePoints( "0 1 1 1 1 0 2 0" ) ) ), // 4
          pieceRow( new Piece( parsePoints( "0 0 0 1 1 0 1 1" ) ) ), // 5
          pieceRow( new Piece( parsePoints( "0 0 1 0 1 1 2 0" ) ) ), // 6
        };
    }
    return pieces ;
    
  }// getPieces()

  /** Given a string of x,y pairs ("0 0	0 1	0 2	1 0"), parses the points into a Point[] array. (Provided code)  */
  private static Point[] parsePoints( String string )
  {
    // could use Arraylist here, but use vector so works on Java 1.1
    Vector<Point> $points = new Vector<>();
    StringTokenizer $tok = new StringTokenizer( string );
    try
    {
      while( $tok.hasMoreTokens() )
      {
        int x = Integer.parseInt( $tok.nextToken() );
        int y = Integer.parseInt( $tok.nextToken() );

        $points.addElement( new Point( x, y ) );
      }
    }
    catch( NumberFormatException e )
    {
      throw new RuntimeException( "Could not parse x,y string:" + string ); // cheap way to do assert
    }
    
    // Make an array out of the Vector
    Point[] $pointAr = new Point[ $points.size() ];
    $points.copyInto( $pointAr );
    
    return $pointAr ;
    
  }// parsePoints() 

  /**
   * Given the "first" rotation of a piece piece, computes all the other rotations and
   * links them all together by their next pointers. Returns the first piece. {@link #nextRotation()}
   * relies on the next pointers to get from one rotation to the next.
   * Internally, uses Piece.equals() to detect when the rotations have gotten us back to
   * the first piece.
   */
  private static Piece pieceRow( Piece root )
  {
    Piece $temp = root;
    Piece $prev = root;
    for( ;; )
    {
      $prev = $temp;
      $prev.setPieceDims();
      $prev.setPieceSkirt();
      $temp = new Piece( $prev.body );
      $temp = $temp.rotatePiece();
      if( !$temp.equals( root ) )
      {
        $prev.next = $temp;
      }
      else
      {
        $prev.next = root;
        break;
      }
    }
    return root;
    
  }// pieceRow()

  private Piece rotatePiece()
  {
    Piece $piece = null;
    Point[] $tempAr = new Point[ body.length ];
    
    // switch x,y to y,x
    for( int i = 0; i < body.length; i++ )
    {
      $tempAr[i] = new Point();
      $tempAr[i].x = body[i].y;
      $tempAr[i].y = body[i].x;
    }
    $piece = new Piece( $tempAr );
    $piece.setPieceDims();

    for( int i = 0; i < $piece.body.length; i++ )
    {
      $tempAr[i].x = ( $piece.width - 1 ) - $piece.body[i].x;
      $tempAr[i].y = $piece.body[i].y;
    }
    $piece = new Piece( $tempAr );
    
    return $piece ;
    
  }// rotatePiece()

  private void setPieceDims()
  {
    int $wmax = -1;
    int $hmax = -1;
    for( int i = 0; i < body.length; i++ )
    {
      if( body[i].x > $wmax )
        $wmax = body[i].x;
      if( body[i].y > $hmax )
        $hmax = body[i].y;
    }
    width = $wmax + 1;
    height = $hmax + 1;
    
  }// setPieceDims()

  private void setPieceSkirt()
  {
    int $wmax = width;
    int $hmax;

    skirt = new int[ $wmax ];

    for( int i = 0; i < $wmax; i++ )
    {
      Point $temp = null;
      $hmax = 10000;
      for( int j = 0; j < body.length; j++ )
      {
        if( body[j].x == i )
        {
          if( body[j].y < $hmax )
          {
            $hmax = body[j].y;
            $temp = body[j];
          }
        }
      }
      skirt[i] = $temp.y;
    }
    
  }// setPieceSkirt()

 /*
  *    F I E L D S
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    
  private Point[] body;

  private Piece next; // "next" rotation
  static private Piece[] pieces; // singleton array of first rotations

  private int[] skirt;

  private int width;
  private int height;

}// class Piece
