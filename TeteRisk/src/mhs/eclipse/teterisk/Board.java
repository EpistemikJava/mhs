/* ***************************************************************************************
 
   Mark Sattolo (epistemik@gmail.com)
 -----------------------------------------------
  
  mhs.eclipse.teterisk.Board.java
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Hashtable;

import javax.swing.JPanel;

/**
 * A rectangular Tetris board containing a grid of colored squares.<br>
 * The board is constrained on both sides and at the bottom. 
 * There is no constraint at the top of the board,
 * although colors assigned to positions above the board are not saved.
 * 
 * @version 1.1
 * @author Mark Sattolo - based on code by <a href="mailto:per@percederberg.net">Per Cederberg</a>
 */
class Board extends JPanel
{
  /**
   * Create a new board with the specified size, initially empty.
   * 
   * @param wd - the width of the board (in squares)
   * @param ht - the height of the board (in squares)
   * @param dbg - enable debug mode
   */
  public Board( int wd, int ht, boolean dbg )
  {
    debugMode = dbg ;

    width = wd ;
    height = ht ;
    
    brdInsets = new Insets( 0, 0, 0, 0 );
    matrix = new Color[ ht ][ wd ];
    sqrSize = new Dimension( 0, 0 );
    
    bufferRect = new Rectangle();
    updateRect = new Rectangle();
    
    lighterColors = new Hashtable<>();
    darkerColors = new Hashtable<>();
    
    clear();

  }// CONSTRUCTOR

  /*
   *      M E T H O D S
   *      
   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

  /** Set the background and message color.  */
  void init()
  {
    setBackground( Configuration.getColor( "board.background", "#000000" ) );
    messageColor = Configuration.getColor( "board.message", "#ffffff" );

  }// init()

  /**
   * Check if a specified square is empty, i.e. it is not marked with a color.<br>
   * If the square is outside the board, false will be returned in all cases except 
   * when the square is directly above the board.
   * 
   * @param horz - the horizontal position (0 <= x < width)
   * @param vert - the vertical position (0 <= y < height)
   * 
   * @return true if the square is empty, false otherwise
   */
  boolean isSquareEmpty( int horz, int vert )
  {
    if( horz < 0 || horz >= width || vert < 0 || vert >= height )
    {
      return horz >= 0 && horz < width && vert < 0 ;
    }

    return matrix[vert][horz] == null ;
  }

  /**
   * Check if a specified line is empty, i.e. only contains empty squares.<br>
   * If the line is outside the board, false will always be returned.
   * 
   * @param vert - the vertical position (0 <= y < height)
   * 
   * @return true if the whole line is empty, false otherwise
   */
  boolean isLineEmpty( int vert )
  {
    if( vert < 0 || vert >= height )
    {
      return false ;
    }

    for( int x = 0 ; x < width ; x++ )
    {
      if( matrix[vert][x] != null )
      {
        return false ;
      }
    }
    return true ;
  }

  /**
   * Check if a specified line is full, i.e. contains no empty squares.<br>
   * If the line is outside the board, true will always be returned.
   * 
   * @param vert - the vertical position (0 <= y < height)
   * 
   * @return true if the whole line is full, false otherwise
   */
  boolean isLineFull( int vert )
  {
    if( vert < 0 || vert >= height )
    {
      return true ;
    }

    for( int x = 0 ; x < width ; x++ )
    {
      if( matrix[vert][x] == null )
      {
        return false ;
      }
    }
    return true ;
  }

  /**
   * @return true if there are full lines on the board, false otherwise
   */
  boolean hasFullLines()
  {
    for( int y = height - 1 ; y >= 0 ; y-- )
    {
      if( isLineFull( y ) )
      {
        return true ;
      }
    }
    return false ;
  }

  /**
   * @return the board height in squares
   */
  int getBoardHeight() { return height ;}

  /**
   * @return the board width in squares
   */
  int getBoardWidth() { return width ;}

  /**
   * @return the number of lines removed since the last clear call
   */
  int getRemovedLines() { return removedLines ;}

  /**
   * Return the color of an individual square on the board.<br>
   * If the square is empty or outside the board, null will be returned.
   * 
   * @param horz - the horizontal position (0 <= x < width)
   * @param vert - the vertical position (0 <= y < height)
   * 
   * @return the square color, or null for none
   */
  Color getSquareColor( int horz, int vert )
  {
    if( horz < 0 || horz >= width || vert < 0 || vert >= height )
    {
      return null ;
    }

    return matrix[vert][horz];
  }

  /**
   * Change the color of an individual square on the board.<br>
   * The square will be marked as in need of a repaint,
   * but the graphical component will NOT be repainted until {@link #update()} is called.
   * 
   * @param horz - the horizontal position (0 <= x < width)
   * @param vert - the vertical position (0 <= y < height)
   * @param clr - the new square color, or null for empty
   */
  void setSquareColor( int horz, int vert, Color clr )
  {
    if( horz < 0 || horz >= width || vert < 0 || vert >= height )
    {
      return ;
    }
    
    matrix[vert][horz] = clr ;
    invalidateSquare( horz, vert );
  }

  /**
   * Set a message to display on the square board.<br>
   * <b>This should ONLY be used when the board is NOT being used for active drawing,
   * as it slows down the drawing considerably.</b>
   * 
   * @param msg - a message to display, or null to remove a previous message
   */
  void setMessage( String msg )
  {
    message = msg ;
    redrawAll();
  }

  /**
   * Clear the board, i.e. removes all the colored squares.<br>
   * Also, the number of removed lines will be reset to zero, and the component will be repainted.
   */
  void clear()
  {
    removedLines = 0 ;
    for( int y = 0 ; y < height ; y++ )
    {
      for( int x = 0 ; x < width ; x++ )
      {
        this.matrix[y][x] = null ;
      }
    }
    redrawAll();
    
  }// clear()

  /**
   * Remove all full lines. All lines above a removed line will be moved down one step,
   * and a new empty line will be added at the top. After removing all full lines, the component will be repainted.
   * 
   * @return number of lines removed
   * 
   * @see #hasFullLines()
   */
  int removeFullLines()
  {
    boolean repaint = false ;
    int base = removedLines ;

    // remove full lines
    for( int y=height-1; y >= 0 ; y-- )
    {
      if( isLineFull(y) )
      {
        removeLine( y );
        removedLines++ ;
        y++ ;
        repaint = true ;
      }
    }

    // repaint if necessary
    if( repaint )
    {
      redrawAll();
    }
    
    int result = removedLines - base ;
    if( debugMode )
      System.out.println( "Removed " + result + " lines." );
    
    return result ;
    
  }// removeFullLines()

  /**
   * Remove a single line. All lines above are moved down one step, and a new empty line
   * is added at the top. No repainting will be done after removing the line.
   * 
   * @param vert - the vertical position (0 <= y < height)
   */
  private void removeLine( int vert )
  {
    int y = vert;
    if( y < 0 || y >= height )
    {
      return ;
    }

    for( ; y > 0 ; y-- )
    {
      for( int x = 0 ; x < width ; x++ )
      {
        matrix[y][x] = matrix[y - 1][x];
      }
    }

    for( int x = 0 ; x < width ; x++ )
    {
      matrix[0][x] = null ;
    }
    
  }// removeLine()
  
  /**
   * Updates the graphical component.<br>
   * Any squares previously changed will be repainted by this method.
   */
  void update()
  {
    redraw();
  }
  
  /**
   * @return true as this component is double buffered
   */
  public boolean isDoubleBuffered()
  { return true ;}
  
  /**
   * @return the preferred size of this component
   */
  public Dimension getPreferredSize()
  {
    return new Dimension( width * SQUARE_SIDE_LENGTH_PX, height * SQUARE_SIDE_LENGTH_PX );
  }
  
  /**
   * @return the minimum size of this component
   */
  public Dimension getMinimumSize()
  { return getPreferredSize(); }
  
  /**
   * @return the maximum size of this component
   */
  public Dimension getMaximumSize()
  { return getPreferredSize(); }
  
  /*
   *      G R A P H I C S
   * 
   * = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = 
   */
  
  /**
   * Add a square to the set of squares in need of redrawing.
   * 
   * @param horz - the horizontal position (0 <= x < width)
   * @param vert - the vertical position (0 <= y < height)
   */
  private void invalidateSquare( int horz, int vert )
  {
    if( updated )
    {
      updated = false ;
      updateRect.x = horz ;
      updateRect.y = vert ;
      updateRect.width = 0 ;
      updateRect.height = 0 ;
    }
    else
    {
      if( horz < updateRect.x )
      {
        updateRect.width += updateRect.x - horz ;
        updateRect.x = horz ;
      }
      else if( horz > updateRect.x + updateRect.width )
      {
        updateRect.width = horz - updateRect.x;
      }
      if( vert < updateRect.y )
      {
        updateRect.height += updateRect.y - vert ;
        updateRect.y = vert ;
      }
      else if( vert > updateRect.y + updateRect.height )
      {
        updateRect.height = vert - updateRect.y;
      }
    }
  }

  /**
   * Redraw all invalid squares.<br>
   * If no squares have been marked as in need of redrawing, no redrawing will occur.
   */
  private void redraw()
  {
    Graphics page ;
    
    if( !updated )
    {
      updated = true ;
      page = getGraphics();
      if( page == null )
        return ;
      
      page.setClip( brdInsets.left + updateRect.x * sqrSize.width, brdInsets.top + updateRect.y * sqrSize.height,
                    (updateRect.width + 1) * sqrSize.width, (updateRect.height + 1) * sqrSize.height );
      paint( page );
    }
  }

  /** Redraw the whole component */
  private void redrawAll()
  {
    Graphics page ;
    
    updated = true ;
    page = getGraphics();
    if( page == null )
      return ;
    
    page.setClip( brdInsets.left, brdInsets.top, width * sqrSize.width, height * sqrSize.height );
    paint( page );
  }

  /**
   * Returns a lighter version of the specified color.<br>
   * The lighter color will looked up in a hashtable, making this method fast. 
   * If the color is not found, the lighter color will be calculated and added to the lookup table for later reference.
   * 
   * @param clr - the base color
   * 
   * @return the lighter version of the color
   */
  private Color getLighterColor( Color clr )
  {
    Color $lighter ;
    
    $lighter = lighterColors.get( clr );
    if( $lighter == null )
    {
      $lighter = clr.brighter().brighter();
      lighterColors.put( clr, $lighter );
    }
    return $lighter ;
  }

  /**
   * Returns a darker version of the specified color.<br>
   * The darker color will looked up in a hashtable, making this method fast. 
   * If the color is not found, the darker color will be calculated and added to the lookup table for later reference.
   * 
   * @param clr - the base color
   * 
   * @return the darker version of the color
   */
  private Color getDarkerColor( Color clr )
  {
    Color $darker ;
    
    $darker = darkerColors.get( clr );
    if( $darker == null )
    {
      $darker = clr.darker().darker();
      darkerColors.put( clr, $darker );
    }
    return $darker ;
  }

  /**
   * Paints this component indirectly.<br>
   * The painting is first done to a buffer image, that is then painted directly to the specified graphics context.
   * 
   * @param page - the graphics context to use
   */
  public synchronized void paint( Graphics page )
  {
    Graphics $bufferPage ;
    Rectangle $rect ;
    
    // handle component size change
    if( brdSize == null || !brdSize.equals( getSize() ) )
    {
      brdSize = getSize();
      sqrSize.width = brdSize.width / width ;
      sqrSize.height = brdSize.height / height ;
      if( sqrSize.width <= sqrSize.height )
      {
        sqrSize.height = sqrSize.width ;
      }
      else
      {
        sqrSize.width = sqrSize.height ;
      }
      
      brdInsets.left = ( brdSize.width - width * sqrSize.width ) / 2 ;
      brdInsets.right = brdInsets.left;
      brdInsets.top = 0 ;
      brdInsets.bottom = brdSize.height - height * sqrSize.height ;
      bufferImage = createImage( width * sqrSize.width, height * sqrSize.height );
    }
    
    // paint component in buffer image
    $rect = page.getClipBounds();
    $bufferPage = bufferImage.getGraphics();
    $bufferPage.setClip( $rect.x - brdInsets.left, $rect.y - brdInsets.top, $rect.width, $rect.height );
    paintComponent( $bufferPage );
    
    // paint image buffer
    page.drawImage( bufferImage, brdInsets.left, brdInsets.top, getBackground(), null );
    
  }// paint()

  /**
   * Paints this component directly.<br>
   * All the squares on the board will be painted directly to the specified graphics context.
   * 
   * @param page - the graphics context to use
   */
  protected void paintComponent( Graphics page )
  {
    // paint background
    page.setColor( getBackground() );
    page.fillRect( 0, 0, width * sqrSize.width, height * sqrSize.height );
    
    // paint squares
    for( int y=0; y < height ; y++ )
    {
      for( int x=0; x < width ; x++ )
      {
        if( matrix[y][x] != null )
        {
          paintSquare( page, x, y );
        }
      }
    }
    
    // paint message
    if( message != null )
    {
      paintMessage( page, message );
    }
  }// paintComponent()

  /**
   * Paints a single board square. The specified position must contain a color object.
   * 
   * @param page - the graphics context to use
   * @param horz - the horizontal position (0 <= x < width)
   * @param vert - the vertical position (0 <= y < height)
   */
  private void paintSquare( Graphics page, int horz, int vert )
  {
    Color $color = matrix[vert][horz];
    int $xMin = horz * sqrSize.width ;
    int $yMin = vert * sqrSize.height ;
    int $xMax = $xMin + sqrSize.width - 1 ;
    int $yMax = $yMin + sqrSize.height - 1 ;
    int i ;
    
    // skip drawing if not visible
    bufferRect.x = $xMin ;
    bufferRect.y = $yMin ;
    bufferRect.width = sqrSize.width ;
    bufferRect.height = sqrSize.height ;
    if( !bufferRect.intersects( page.getClipBounds() ) )
    {
      return;
    }
    
    // fill with base color
    page.setColor( $color );
    page.fillRect( $xMin, $yMin, sqrSize.width, sqrSize.height );
    
    // draw brighter lines
    page.setColor( getLighterColor( $color ) );
    for( i=0 ; i < sqrSize.width/10 ; i++ )
    {
      page.drawLine( $xMin + i, $yMin + i, $xMax - i, $yMin + i );
      page.drawLine( $xMin + i, $yMin + i, $xMin + i, $yMax - i );
    }
    
    // draw darker lines
    page.setColor( getDarkerColor( $color ) );
    for( i=0 ; i < sqrSize.width/10 ; i++ )
    {
      page.drawLine( $xMax - i, $yMin + i, $xMax - i, $yMax - i );
      page.drawLine( $xMin + i, $yMax - i, $xMax - i, $yMax - i );
    }
  }// paintSquare()

  /**
   * Paints a board message. The message will be drawn at the center of the component.
   * 
   * @param page - the graphics context to use
   * @param msg  - the string message
   */
  private void paintMessage( Graphics page, String msg )
  {
    int $fontWidth ;
    int $offset ;
    int x , y ;
    
    // find string font width
    page.setFont( new Font( "SansSerif", Font.BOLD, sqrSize.width + 4 ) );
    $fontWidth = page.getFontMetrics().stringWidth( msg );
    
    // find centered position
    x = ( width * sqrSize.width - $fontWidth ) / 2 ;
    y = height * sqrSize.height / 2 ;
    
    // draw black version of the string
    $offset = sqrSize.width / 10 ;
    page.setColor( Color.black );
    page.drawString( msg, x - $offset, y - $offset );
    page.drawString( msg, x - $offset, y );
    page.drawString( msg, x - $offset, y - $offset );
    page.drawString( msg, x, y - $offset );
    page.drawString( msg, x, y + $offset );
    page.drawString( msg, x + $offset, y - $offset );
    page.drawString( msg, x + $offset, y );
    page.drawString( msg, x + $offset, y + $offset );
    
    // draw white version of the string
    page.setColor( messageColor );
    page.drawString( msg, x, y );
    
  }// paintMessage()

  /*
   *      F I E L D S 
   *   
   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

  /** default side length */
  static final int SQUARE_SIDE_LENGTH_PX = 30 ;
  
  /** Enable or disable debug actions.  */
  boolean debugMode ;
  
  /** The board width (in squares) */
  int width = 0 ;
  
  /** The board height (in squares) */
  int height = 0 ;
  
  /**
   * The board color matrix. This matrix (or grid) contains a color entry for each square
   * in the board. The matrix is indexed by the vertical, and then the horizontal coordinate.
   */
  private Color[][] matrix ;
  
  /**
   * An optional board message.<br>
   * The board message can be set at any time, printing it on top of the board.
   */
  private String message ;

  /**
   * The number of lines removed.<br>
   * This counter is increased each time a line is removed from the board.
   */
  private int removedLines = 0 ;

  /**
   * The component size. If the component has been resized, that will be detected when the
   * paint method executes. If this value is set to null, the component dimensions are unknown.
   */
  private Dimension brdSize ;

  /**
   * The component's {@link java.awt.Insets}.
   * The Insets values are used to create a border around the board to compensate for a skewed aspect ratio.
   * If the component has been resized, the Insets values will be recalculated when the paint method executes.
   */
  private Insets brdInsets ;

  /**
   * The square size in pixels. This value is updated when the component's size is
   * changed, i.e. when the {@link #brdSize} variable is modified.
   */
  private Dimension sqrSize ;

  /**
   * An image used for double buffering. The board is first painted onto this image, and
   * that image is then painted onto the real surface in order to avoid making the drawing
   * process visible to the user. This image is recreated each time the component size changes.
   */
  private Image bufferImage ;

  /**
   * A clip boundary buffer rectangle. This rectangle is used when calculating the clip
   * boundaries, in order to avoid allocating a new clip rectangle for each board square.
   */
  private Rectangle bufferRect ;

  /** The board message color */
  private Color messageColor = Color.white ;

  /**
   * A lookup table containing lighter versions of the colors.<br>
   * This table is used to avoid calculating the lighter versions of the colors for each and every square drawn.
   */
  private Hashtable<Color, Color> lighterColors ;

  /**
   * A lookup table containing darker versions of the colors.<br>
   * This table is used to avoid calculating the darker versions of the colors for each and every square drawn.
   */
  private Hashtable<Color, Color> darkerColors ;

  /** A flag set when the component has been updated */
  private boolean updated = true ;

  /**
   * A bounding box of the squares to update. The coordinates used in the rectangle refers to the square matrix.
   */
  private Rectangle updateRect ;

  /** generated */
  static final long serialVersionUID = 145079248878994429L ;

}// class Board
