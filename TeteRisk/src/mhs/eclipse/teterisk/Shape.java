/* ***************************************************************************************
 
   Mark Sattolo (epistemik@gmail.com)
 -----------------------------------------------
  
  mhs.eclipse.teterisk.Shape.java
  Eclipse version created Jan 6, 2012
  git version created Apr 26, 2014
  
  This work is free software; you can redistribute it and/or modify it under the terms
  of the GNU General Public License as published by the Free Software Foundation;
  either version 2 of the License, or (at your option) any later version.
  
  This work is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  See the GNU General Public License for more details.
  
  Copyright (c) 2012-14 Mark Sattolo.  All rights reserved.
  
***************************************************************************************** */

package mhs.eclipse.teterisk;

import java.awt.Color;

/**
 * A class representing a Tetris shape. Each Shape consists of four connected
 * squares in one of seven possible configurations. A Shape may be rotated in 90
 * degree steps and has sideways and downwards mobility.
 * <p>
 * Each Shape instance can have two states -- either attached to a Board or not.
 * When attached, all move and rotation operations are checked so that collisions do not
 * occur with other squares on the board. When not attached, any rotation can be made (and
 * will be kept when attached to a new Board).
 * 
 * @version 1.2
 * @author Mark Sattolo - based on code by <a href="mailto:per@percederberg.net">Per Cederberg</a>
 */
public abstract class Shape
{
 /*
  *    M E T H O D S
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
   
  /**
   * Create a new Shape.<br>
   * The Shape will not be attached to a Board and default position and orientation will be assigned.
   * 
   * @param dbg - enable debug mode
   */
  public Shape( boolean dbg )
  {
    debugMode = dbg ;
    
    // initialize default variables
    board = null ;
    xPos = yPos = 0 ;
    orientation = 0 ;
    
  }// CONSTRUCTOR
  
  /**
   * Checks if this shape is attached to a board.
   * 
   * @return true if the shape is already attached, or false otherwise
   */
  boolean isAttached()
  {
    return board != null ;
  }
  
  /**
   * Attaches the shape to a specified {@link #board}.<br>
   * The shape will be drawn either at the absolute top of the board,
   * with only the bottom line visible, or centered onto the board.
   * In both cases, the squares on the new board are checked for collisions.
   * If the squares are already occupied, this method returns false and no attachment is made.
   * <p>
   * The horizontal and vertical coordinates will be reset for the shape, when centering
   * the shape on the new board. The shape orientation (rotation) will be kept, however.
   * If the shape was previously attached to another board, it will be detached from that
   * board before attaching to the new board.
   * 
   * @param brd - the board to attach to
   * @param center - the centered position flag
   * 
   * @return true if the shape can be attached, false otherwise
   */
  boolean attach( Board brd, boolean center )
  {
    int $newX , $newY ;
    
    // check for previous attachment
    if( isAttached() )
    {
      detach();
    }
    
    // reset position (for correct controls)
    xPos = yPos = 0 ;
    
    // calculate position
    $newX = brd.getBoardWidth() / 2 ;
    if( center )
    {
      $newY = brd.getBoardHeight() / 2 ;
    }
    else
    {
      $newY = 0 ;
      for( int i=0; i < shapeX.length; i++ )
      {
        if( getRelativeY(i, orientation) - $newY > 0 )
        {
          $newY = -getRelativeY( i, orientation );
        }
      }
    }
    
    // check position
    board = brd ;
    if( !canMoveTo($newX, $newY, orientation) )
    {
      board = null ;
      return false ;
    }
    
    // draw shape
    xPos = $newX ;
    yPos = $newY ;
    paint( typeColor );
    board.update();
    
    if( debugMode && board.height > Game.PREVIEW_BOARD_LENGTH )
      System.out.println( "Attached a " + names[type] + " shape." );

    return true ;
    
  }// attach()
  
  /**
   * Detaches this shape from its board. The shape will not be removed from the
   * board by this operation, resulting in the shape being left intact.
   */
  void detach()
  {
    board = null ;
  }
  
  /**
   * If the shape isn't attached to a board, false will be returned.
   * 
   * @return true if the shape is fully visible, false otherwise
   */
  boolean isFullyVisible()
  {
    if( !isAttached() )
    {
      return false ;
    }
    for( int i=0; i < shapeX.length; i++ )
    {
      if( yPos + getRelativeY(i, orientation) < 0 )
      {
        return false ;
      }
    }
    return true ;
    
  }// isFullyVisible()
  
  /**
   * If this method returns true, the moveDown() or the moveAllWayDown() methods should have no effect.
   * If no board is attached, this method will return true.
   * 
   * @return true if the shape has landed, false otherwise
   */
  boolean hasLanded()
  {
    return( !isAttached() || !canMoveTo(xPos, yPos + 1, orientation) );
  }
  
  /**
   * Moves the shape one step to the left. If such a move is not possible with respect to
   * the board, nothing is done. The board will be changed as the shape
   * moves, clearing the previous cells. If no board is attached, nothing is done.
   */
  void moveLeft()
  {
    if( isAttached() && canMoveTo(xPos - 1, yPos, orientation) )
    {
      paint( null );
      xPos-- ;
      paint( typeColor );
      board.update();
    }
  }
  
  /**
   * Moves the shape one step to the right. If such a move is not possible with respect
   * to the board, nothing is done. The board will be changed as the shape
   * moves, clearing the previous cells. If no board is attached, nothing is done.
   */
  void moveRight()
  {
    if( isAttached() && canMoveTo(xPos + 1, yPos, orientation) )
    {
      paint( null );
      xPos++ ;
      paint( typeColor );
      board.update();
    }
  }
  
  /**
   * Move the shape one step down. If such a move is not possible with respect to the
   * board, nothing is done. The board will be changed as the shape moves,
   * clearing the previous cells. If no board is attached, nothing is done.
   */
  void moveDown()
  {
    if( isAttached() && canMoveTo(xPos, yPos + 1, orientation) )
    {
      paint( null );
      yPos++ ;
      paint( typeColor );
      board.update();
    }
  }
  
  /**
   * Move the shape all the way down.<br>
   * The limits of the move are either the board bottom, or squares not being empty.
   * If no move is possible with respect to the board, nothing is done. The board will be changed as the shape moves,
   * clearing the previous cells. If no board is attached, nothing is done.
   */
  void moveAllWayDown()
  {
    int y = yPos ;
    
    // check for board
    if( !isAttached() )
    {
      return ;
    }
    
    // find lowest position
    while( canMoveTo(xPos, y+1, orientation) )
    {
      y++ ;
    }
    // back up one line so user can move piece left or right just before reaching the bottom
    y-- ;
    
    // update
    if( y != yPos )
    {
      paint( null );
      yPos = y ;
      paint( typeColor );
      board.update();
    }
  }// moveAllWayDown()
  
  /**
   * @return the current shape rotation (orientation)
   */
  int getRotation() { return orientation ;}
  
  /**
   * Set the shape rotation (orientation). If the desired rotation is not possible with
   * respect to the board, nothing is done. The board will be changed as the
   * shape moves, clearing the previous cells. If no board is attached, the rotation is performed directly.
   * 
   * @param rotation - the new shape orientation
   */
  void setRotation( int rotation )
  {
    // check new orientation against number of possible orientations
    int newOrientation = rotation % numOrientations ;
    
    // check new position
    if( !isAttached() )
    {
      orientation = newOrientation ;
    }
    else
      if( canMoveTo(xPos, yPos, newOrientation) )
      {
        // clear the squares in the current location of the shape
        paint( null );
        orientation = newOrientation ;
        // paint the new position of the shape
        paint( typeColor );
        
        board.update();
      }
  }// setRotation()
  
  /**
   * Rotate the shape randomly.
   * If such a rotation is not possible with respect to the board, nothing is done.
   * The board will be changed as the shape moves, clearing the previous cells.
   * If no board is attached, the rotation is performed directly.
   */
  void rotateRandom()
  {
    setRotation( (int)(Math.random() * 4.0) );
  }
  
  /**
   * Rotate the shape clockwise.
   * If such a rotation is not possible with respect to the board, nothing is done.
   * The board will be changed as the shape moves, clearing the previous cells.
   * If no board is attached, the rotation is performed directly.
   */
  void rotateClockwise()
  {
    if( numOrientations == 1 )
    {
      return ;
    }
    
    setRotation( orientation + 1 );
  }

  /**
   * Rotate the shape counter-clockwise.
   * If such a rotation is not possible with respect to the board, nothing is done.
   * The board will be changed as the shape moves, clearing the previous cells.
   * If no board is attached, the rotation is performed directly.
   */
  void rotateCounterClockwise()
  {
    if( numOrientations == 1 )
    {
      return ;
    }
    
    setRotation( orientation + 3 );
  }
  
  /**
   * Check if a specified pair of (square) coordinates is inside the shape.
   * 
   * @param horz - the horizontal position
   * @param vert - the vertical position
   * 
   * @return true if the coordinates are inside the shape, false otherwise
   */
  private boolean isInside( int horz, int vert )
  {
    for( int i=0; i < shapeX.length ; i++ )
    {
      if( horz == xPos + getRelativeX(i, orientation)
          && vert == yPos + getRelativeY(i, orientation) )
      {
        return true ;
      }
    }
    return false ;
    
  }// isInside()
  
  /**
   * Check if the shape can move to a new position.
   * The current shape position is taken into account when checking for collisions.
   * If a collision is detected, returns false.
   * 
   * @param newX - the new horizontal position
   * @param newY - the new vertical position
   * @param newOrientation - the new orientation (rotation)
   * 
   * @return true if the shape can be moved to the specified position, otherwise false
   */
  private boolean canMoveTo( int newX, int newY, int newOrientation )
  {
    int x, y ;
    
    for( int i=0; i < 4 ; i++ )
    {
      x = newX + getRelativeX( i, newOrientation );
      y = newY + getRelativeY( i, newOrientation );
      if( !isInside(x, y) && !board.isSquareEmpty(x, y) )
      {
        return false ;
      }
    }
    return true ;
    
  }// canMoveTo()
  
  /**
   * Return the relative horizontal position of a specified square.<br>
   * The square will be rotated according to the specified orientation.
   * 
   * @param square - the square to rotate (0-3)
   * @param orient - the orientation to use (0-3)
   * 
   * @return the rotated relative horizontal position
   */
  private int getRelativeX( int square, int orient )
  {
    switch( orient % 4 )
    {
      case 0:
        return shapeX[square];
      case 1:
        return -shapeY[square];
      case 2:
        return -shapeX[square];
      case 3:
        return shapeY[square];
      default:
              throw new IllegalArgumentException( "Shape.getRelativeX(): " + orient ); // should NEVER occur
    }
  }// getRelativeX()
  
  /**
   * Rotate the relative vertical position of a specified square.<br>
   * The square will be rotated according to the specified orientation.
   * 
   * @param square - the square to rotate (0-3)
   * @param orient - the orientation to use (0-3)
   * 
   * @return the rotated relative vertical position
   */
  private int getRelativeY( int square, int orient )
  {
    switch( orient % 4 )
    {
      case 0:
        return shapeY[square];
      case 1:
        return shapeX[square];
      case 2:
        return -shapeY[square];
      case 3:
        return -shapeX[square];
      default:
              throw new IllegalArgumentException( "Shape.getRelativeY(): " + orient ); // should NEVER occur
    }
  }// getRelativeY()
  
  /**
   * Paint the shape on the board with the specified color.
   * 
   * @param clr - the color to paint with, or null for clearing
   */
  private void paint( Color clr )
  {
    int x, y ;
    
    for( int i=0; i < shapeX.length ; i++ )
    {
      x = xPos + getRelativeX( i, orientation );
      y = yPos + getRelativeY( i, orientation );
      board.setSquareColor( x, y, clr );
    }
  }// paint()
  
 /*
  *    F I E L D S
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
  
  /** Enable or disable debug actions */
  boolean debugMode ;

  /** The board to which the shape is attached. If this variable is set to null, the shape is not attached. */
  private Board board ;
  
  /** The horizontal shape position on the board.<br>
   *  This value has no meaning when the shape is not attached to a board. */
  private int xPos ;
  
  /** The vertical shape position on the board.<br>
   *  This value has no meaning when the shape is not attached to a board. */
  private int yPos ;
  
  /**
   * The shape's current orientation (rotation)<br>
   * A value between 0 and 3, that must also be less than the {@link #numOrientations} value
   */
  private int orientation ;
  
  /**
   * The number of unique orientations.<br>
   * This is used to reduce the number of possible rotations for some shapes, such as the square shape.
   * If this value is not used, the square shape will be allowed to rotate around one of its squares,
   * which gives an erroneous effect.
   * 
   * @see #orientation
   */
  int numOrientations ;
  
  /**
   * The horizontal coordinates of each square in the shape.<br>
   * Each entry in the array is for one of the four squares making up each shape.<br>
   * The coordinates are relative to the current position and orientation.
   */
  int[] shapeX = new int[ 4 ];
  
  /**
   * The vertical coordinates of each square in the shape.<br>
   * Each entry in the array is for one of the four squares making up each shape.<br>
   * The coordinates are relative to the current position and orientation.
   */
  int[] shapeY = new int[ 4 ];
  
  /** The shape type */
  int type ;
  
  /** The shape color */
  Color typeColor ;
  
  /** shape identifier */
  static final int
                    SQUARE = 0 ,
                      LINE = 1 ,
                   S_SHAPE = 2 ,
                   Z_SHAPE = 3 ,
                     GAMMA = 4 ,
                   L_SHAPE = 5 ,
                  TRIANGLE = 6 ;

  /** shape names  */
  static final String[] names = { "SQUARE", "LINE", "S", "Z", "GAMMA", "L", "TRIANGLE" };
  
  /** shape colors */
  //                                beige      pink       blue       magenta    violet     yellow     green
  static final String[] colors = { "#FFD8B1", "#FFB4B4", "#A3D5EE", "#F4ADFF", "#C0B6FA", "#F5F4A7", "#A4D9B6" };
  
}// class Shape

/*
 *  <                                                                                           >  *
 *  <                               S U B C L A S S E S                                         >  *
 *  *********************************************************************************************  *
 */
class Square extends Shape
{
  public Square( boolean debug )
  {
    super( debug );
    
    type = SQUARE ;
    typeColor = Configuration.getColor( names[type], colors[type] );
    numOrientations = 1 ;
    
    // square 1
    shapeX[0] = -1 ;
    shapeY[0] =  0 ;
    // square 2
    shapeX[1] =  0 ;
    shapeY[1] =  0 ;
    // square 3
    shapeX[2] = -1 ;
    shapeY[2] =  1 ;
    // square 4
    shapeX[3] =  0 ;
    shapeY[3] =  1 ;
    
    System.out.println( "Created a " + names[type] + " shape." );
  }
}// class Square

class Line extends Shape
{
  public Line( boolean debug )
  {
    super( debug );
    
    type = LINE ;
    typeColor = Configuration.getColor( names[type], colors[type] );
    numOrientations = 2 ;
    
    shapeX[0] = -2 ;
    shapeY[0] =  0 ;
    shapeX[1] = -1 ;
    shapeY[1] =  0 ;
    shapeX[2] =  0 ;
    shapeY[2] =  0 ;
    shapeX[3] =  1 ;
    shapeY[3] =  0 ;
    
    System.out.println( "Created a " + names[type] + " shape." );
  }
}// class Line

class Sshape extends Shape
{
  public Sshape( boolean debug )
  {
    super( debug );
    
    type = S_SHAPE ;
    typeColor = Configuration.getColor( names[type], colors[type] );
    numOrientations = 2 ;
    
    shapeX[0] =  0 ;
    shapeY[0] =  0 ;
    shapeX[1] =  1 ;
    shapeY[1] =  0 ;
    shapeX[2] = -1 ;
    shapeY[2] =  1 ;
    shapeX[3] =  0 ;
    shapeY[3] =  1 ;
    
    System.out.println( "Created a " + names[type] + " shape." );
  }
}// class Sshape

class Zshape extends Shape
{
  public Zshape( boolean debug )
  {
    super( debug );
    
    type = Z_SHAPE ;
    typeColor = Configuration.getColor( names[type], colors[type] );
    numOrientations = 2 ;
    
    shapeX[0] = -1 ;
    shapeY[0] =  0 ;
    shapeX[1] =  0 ;
    shapeY[1] =  0 ;
    shapeX[2] =  0 ;
    shapeY[2] =  1 ;
    shapeX[3] =  1 ;
    shapeY[3] =  1 ;
    
    System.out.println( "Created a " + names[type] + " shape." );
  }
}// class Zshape

class Gamma extends Shape
{
  public Gamma( boolean debug )
  {
    super( debug );
    
    type = GAMMA ;
    typeColor = Configuration.getColor( names[type], colors[type] );
    numOrientations = 4 ;
    
    shapeX[0] = -1 ;
    shapeY[0] =  0 ;
    shapeX[1] =  0 ;
    shapeY[1] =  0 ;
    shapeX[2] =  1 ;
    shapeY[2] =  0 ;
    shapeX[3] =  1 ;
    shapeY[3] =  1 ;
    
    System.out.println( "Created a " + names[type] + " shape." );
  }
}// class Gamma

class Lshape extends Shape
{
  public Lshape( boolean debug )
  {
    super( debug );
    
    type = L_SHAPE ;
    typeColor = Configuration.getColor( names[type], colors[type] );
    numOrientations = 4 ;
    
    shapeX[0] = -1 ;
    shapeY[0] =  0 ;
    shapeX[1] =  0 ;
    shapeY[1] =  0 ;
    shapeX[2] =  1 ;
    shapeY[2] =  0 ;
    shapeX[3] = -1 ;
    shapeY[3] =  1 ;
    
    System.out.println( "Created a " + names[type] + " shape." );
  }
}// class Lshape

class Triangle extends Shape
{
  public Triangle( boolean debug )
  {
    super( debug );
    
    type = TRIANGLE ;
    typeColor = Configuration.getColor( names[type], colors[type] );
    numOrientations = 4 ;
    
      shapeX[0] = -1 ;
      shapeY[0] =  0 ;
      shapeX[1] =  0 ;
      shapeY[1] =  0 ;
      shapeX[2] =  1 ;
      shapeY[2] =  0 ;
      shapeX[3] =  0 ;
      shapeY[3] =  1 ;
      
      System.out.println( "Created a " + names[type] + " shape." );
  }
}// class Triangle
