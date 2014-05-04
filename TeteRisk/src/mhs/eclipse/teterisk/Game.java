/* ***************************************************************************************
 
   Mark Sattolo (epistemik@gmail.com)
 -----------------------------------------------
  
  mhs.eclipse.teterisk.Game.java
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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * A Tetris game.<br>
 * This class controls all events in the game and handles all the game logic.<br>
 * The game is started through user interaction with the graphical game component provided by this class.
 * 
 * @version 1.1
 * @author Mark Sattolo - based on code by <a href="mailto:per@percederberg.net">Per Cederberg</a>
 */
class Game extends JFrame
{
  /**
   * Create a new TeteRisk game with default width and height
   * @param debug - enable debug mode
   */
  public Game( boolean debug )
  {
    this( DEFAULT_WIDTH, DEFAULT_HEIGHT, debug );
    
  }// Game CONSTRUCTOR with DEFAULT SIZE
  
  /**
   * Create a new TeteRisk game of the specified size.
   * 
   * @param width - the width of the board (in squares)
   * @param height - the height of the board (in squares)
   * @param debug - enable debug mode
   */
  public Game( int width, int height, boolean debug )
  {
    debugMode = debug ;
    
    System.out.println( (debug ? "In" : "NOT in" ) + " DEBUG Mode." );

    board = new Board( width, height, debug );
    board.setMessage( "Press Start" );
    
    previewBoard = new Board( PREVIEW_BOARD_LENGTH, PREVIEW_BOARD_LENGTH, false );
    
    createShapes( debug );
    
    gameThread = new GameThread();
    
    setDefaultCloseOperation( EXIT_ON_CLOSE );
    
    setTitle( "TeteRisk" );
    
    setFocusable( true );
    setResizable( false );
    
  }// Game CONSTRUCTOR with ALL PARAMETERS
  
 /*
  *    M E T H O D S
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
  
  /**
   * Start the game in stand-alone mode.
   * 
   * @param args - from the command line
   */
  public static void main( final String[] args )
  {
    System.out.println( "Main() STARTED ON " + Thread.currentThread() );
    
    SwingUtilities.invokeLater
    (
      new Runnable()
      {
        public void run()
        {
          try
          {
            System.out.println( "new Runnable.run() STARTED ON " + Thread.currentThread() );
            
            // Set System L&F
            UIManager.setLookAndFeel
            (
            //  "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"
            //  "com.sun.java.swing.plaf.motif.MotifLookAndFeel"
            //  "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
            //  UIManager.getSystemLookAndFeelClassName()
                UIManager.getCrossPlatformLookAndFeelClassName()
            );
          }
          catch( Exception e )
          {
            e.printStackTrace();
          }
          
          new Game( (args.length > 0 ? true : false) ).go();

        }// run()
      }
    );// SwingUtilities.invokeLater()
    
    System.out.println( "Main() ENDED on " + Thread.currentThread() );
    
  }// main()
  
  /**
   * Set up the Game and show it on screen.
   */
  private void go()
  {
    add( getGameContainer() );
    pack();
    
    // add frame window listener
    addWindowListener( new WindowAdapter()
    {
      public void windowClosing( WindowEvent wevt )
      {
        System.out.println( "run() EXITED from " + Thread.currentThread() );
        System.exit( 0 );
      }
    } );
    
    // centered
    setLocationRelativeTo( null );
    
    // show frame (and start game)
    setVisible( true );
    
    // so will respond to key event 'P' to start the game
    gameContainer.requestFocus();
    
  }// Game.go()
  
  /**
   * Kill the game running thread and do necessary clean-up.
   * After calling this method, no further methods in this class should be called,
   * nor should any {@link #gameThread} returned earlier be trusted.
   */
  void quit()
  {
    gameThread = null ;
  }
  
  /**
   * @return {@link #gameContainer}
   */
  private Component getGameContainer()
  {
    if( gameContainer == null )
      gameContainer = new GameContainer();
    
    return gameContainer ;
    
  }// Game.getGameContainer()
  
  /**
   * Handle a game start event. Both the main and preview boards will be reset,
   * and all other game parameters will be reset. Finally, the game thread will be launched.
   */
  private void handleStart()
  {
    System.out.println( "Game STARTED on " + Thread.currentThread() );
    
    // reset score and level
    level = 1 ;
    level_score = 0 ;
    total_score = 0 ;
    
    // reset shapes
    shape = null ;
    nextShape = randomShape();
    nextShape.rotateRandom();
    nextRotation = nextShape.getRotation();
    
    // reset components
    board.setMessage( null );
    board.clear();
    previewBoard.clear();
    
    handleLevelModification();
    handleScoreModification();
    gameContainer.button.setText( "Pause" );
    
    // start game thread
    gameThread.reset();
    
  }// Game.handleStart()
  
  /** 
   * Handle a game over event. This will stop the game thread, reset all shapes and print a "Game Over" message.
   */
  private void handleGameOver()
  {
    System.out.println( "Game ENDED on " + Thread.currentThread() );

    // handle game thread
    gameThread.setGameOver();
    
    // reset shapes
    if( shape != null )
    {
      shape.detach();
    }
    shape = null ;
    
    if( nextShape != null )
    {
      nextShape.detach();
    }
    nextShape = null ;
    
    // handle components
    board.setMessage( "Game Over" );
    gameContainer.button.setText( "Start" );
    
  }// Game.handleGameOver()
  
  /**
   * Handle a game pause event.<br>
   * This will pause the game thread and print a pause message on the game board.
   */
  private void handlePause()
  {
    System.out.println( "Game PAUSED on " + Thread.currentThread() );

    gameThread.setPaused( true );
    board.setMessage( "Paused" );
    gameContainer.button.setText( "Resume" );
    
  }// Game.handlePause()
  
  /** Handle a game resume event. This will resume the game thread and remove any messages on the game board.  */
  private void handleResume()
  {
    System.out.println( "Game RESUMED on " + Thread.currentThread() );

    board.setMessage( null );
    gameContainer.button.setText( "Pause" );
    gameThread.setPaused( false );
    
  }// Game.handleResume()
  
  /** Handle a level modification event. This will modify the level label and adjust the thread speed.  */
  private void handleLevelModification()
  {
    gameContainer.levelLabel.setText( "Level: " + level );
  }
  
  /** Handle a score modification event. This will modify the score label.  */
  private void handleScoreModification()
  {
    gameContainer.scoreLabel.setText( "Score: " + total_score );
  }
  
  /** Handle a velocity modification event. This will modify the velocity label.  */
  private void handleVelocityModification()
  {
    gameContainer.velocityLabel.setText( "Velocity: " + gameThread.getVelocity() );
  }
  
  /**
   * Create the seven possible shapes
   * 
   * @param debug - determine if in debug mode
   */
  private void createShapes( boolean debug )
  {
    shapes[Shape.SQUARE_SHAPE]   = new Shape( Shape.SQUARE_SHAPE,   debugMode );
    shapes[Shape.LINE_SHAPE]     = new Shape( Shape.LINE_SHAPE,     debugMode );
    shapes[Shape.S_SHAPE]        = new Shape( Shape.S_SHAPE,        debugMode );
    shapes[Shape.Z_SHAPE]        = new Shape( Shape.Z_SHAPE,        debugMode );
    shapes[Shape.GAMMA_SHAPE]    = new Shape( Shape.GAMMA_SHAPE,    debugMode );
    shapes[Shape.L_SHAPE]        = new Shape( Shape.L_SHAPE,        debugMode );
    shapes[Shape.TRIANGLE_SHAPE] = new Shape( Shape.TRIANGLE_SHAPE, debugMode );
  }

  /**
   * Handle a shape-start event. This will move the next shape to the current shape
   * position, while also creating a new preview shape. If the shape cannot be
   * introduced onto the game board, a game over event will be launched.
   */
  private void handleShapeStart()
  {
    int rotation;
    
    // move next shape to current
    shape = nextShape ;
    moveLock = false ;
    rotation = nextRotation ;
    
    nextShape = randomShape();
    nextShape.rotateRandom();
    nextRotation = nextShape.getRotation();
    
    // handle shape preview
    if( preview )
    {
      previewBoard.clear();
      nextShape.attach( previewBoard, true );
      nextShape.detach();
    }
    
    // attach shape to game board
    shape.setRotation( rotation );
    if( !shape.attach( board, false ) )
    {
      previewBoard.clear();
      shape.attach( previewBoard, true );
      shape.detach();
      
      handleGameOver();
    }
  }// Game.handleShapeStart()
  
  /**
   * Handle a shape-landed event.
   * This will check that the shape is completely visible, or a game-over event will be launched.
   * The Board will be checked for full lines, and any found will be removed.
   * If no full lines could be removed, a shape start event is launched directly.
   */
  private void handleShapeLanded()
  {
    // check if shape did not land safely
    if( ! shape.isFullyVisible() )
    {
      handleGameOver();
      return ;
    }
    
    shape.detach();
    shape = null ;
    
    // check for full lines
    if( board.hasFullLines() )
    {
      setScore( board.removeFullLines() );
    }

  }// Game.handleShapeLanded()
  
  /**
   * Adjust the score based on current level and number of lines removed in a turn
   */
  private void setScore( int lines )
  {
    int extra = (BASE_SCORE + level) * lines * lines ;
    
    level_score += extra ;
    total_score += extra ;
    
    handleScoreModification();
    
    setLevel();
    
  }// Game.setScore()
  
  /**
   * Adjust the level if the score changes
   */
  private void setLevel()
  {
    if( level_score >= (BASE_LEVEL_SCORE + (BASE_LEVEL_MULTIPLIER * level)) )
    {
      level++ ;
      level_score = 0 ;
      
      gameThread.adjustVelocity();
      
      handleLevelModification();
    }
  }// Game.handleStart()
  
  /**
   * Handle a timer event. This will normally move the shape down one step, but when a
   * shape has landed or isn't ready other events will be launched. This method is
   * synchronized to avoid race conditions with other asynchronous events (keyboard and mouse).
   */
  private synchronized void handleTimer()
  {
    if( shape == null )
    {
      handleShapeStart();
    }
    else if( shape.hasLanded() )
    {
      handleShapeLanded();
    }
    else
    {
      shape.moveDown();
    }
  }// Game.handleTimer()
  
  /**
   * Handle a button press event. This will launch different events depending on the
   * state of the game, as the button semantics change as the game changes. This method is
   * synchronized to avoid race conditions with other asynchronous events (timer and keyboard).
   */
  private synchronized void handleButtonPressed()
  {
    if( nextShape == null )
    {
      handleStart();
    }
    else if( gameThread.isPaused() )
    {
      handleResume();
    }
    else
    {
      handlePause();
    }
  }// Game.handleButtonPressed()
  
  /**
   * Handle a keyboard event. This will result in different actions being taken,
   * depending on the key pressed. In some cases, other events will be launched. This
   * method is synchronized to avoid race conditions with other asynchronous events (timer and mouse).
   * 
   * @param kevt - the key event
   */
  private synchronized void handleKeyEvent( KeyEvent kevt )
  {
    int key = kevt.getKeyCode();
    
    /*/
    if( debugMode )
      System.out.println( "Key '" + KeyEvent.getKeyText(key) + "' was pressed." );
    //*/
    
    // handle start, pause and resume
    if( key == KeyEvent.VK_P )
    {
      handleButtonPressed();
      return ;
    }
    
    // don't proceed if stopped or paused
    if( shape == null || moveLock || gameThread.isPaused() )
    {
      return ;
    }
    
    // handle remaining key events
    switch( key )
    {
      case KeyEvent.VK_LEFT:
        shape.moveLeft();
        break ;
        
      case KeyEvent.VK_RIGHT:
        shape.moveRight();
        break ;
        
      case KeyEvent.VK_DOWN:
        moveLock = true ;
        shape.moveAllWayDown();
        moveLock = false ;
        break ;
        
      case KeyEvent.VK_UP:
      case KeyEvent.VK_SPACE:
        if( kevt.isControlDown() )
          shape.rotateRandom();
        else if( kevt.isShiftDown() )
          shape.rotateClockwise();
        else
            shape.rotateCounterClockwise();
        break ;
        
      case KeyEvent.VK_S:
        if( level < 9 )
        {
          level++ ;
          handleLevelModification();
        }
        break ;
        
      case KeyEvent.VK_N:
        preview = !preview ;
        if( preview && shape != nextShape )
        {
          nextShape.attach( previewBoard, true );
          nextShape.detach();
        }
        else
            previewBoard.clear();
        break ;
    }
  }// Game.handleKeyEvent()
  
  /**
   * @return a random shape from the shapes array, NOT initialized
   */
  private Shape randomShape()
  {
    return shapes[ (int)( Math.random() * shapes.length ) ];
  }
  
 /*
  *      I N N E R   C L A S S E S
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
  
  /**
   * A {@link java.awt.Container} for all the game components
   */
  private class GameContainer extends Container
  {
    /**
     * Create a new game container. All the components will be added to this
     */
    public GameContainer()
    {
      super();
      initComponents();
      
    }// CONSTRUCTOR
    
    /*
     *    M E T H O D S
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    
    /**
     * Paint the game component.<br>
     * This method is overridden from the default implementation in order to set the correct background color.
     * 
     * @param page - the graphics context to use
     */
    public void paint( Graphics page )
    {
      Rectangle $rect = page.getClipBounds();
      
      if( size == null || !size.equals( getSize() ) )
      {
        size = getSize();
        resizeComponents();
      }
      page.setColor( getBackground() );
      page.fillRect( $rect.x, $rect.y, $rect.width, $rect.height );
      super.paint( page );
      
    }// GameContainer.paint()
    
    /**
     * Initialize all the components and place them in the container
     */
    private void initComponents()
    {
      GridBagConstraints c;
      
      // set layout manager and background
      setLayout( new GridBagLayout() );
      setBackground( Configuration.getColor( "background", "#d4d0c8" ) );
      
      // add game board
      board.init();
      c = new GridBagConstraints();
      c.gridx = 0;
      c.gridy = 0;
      c.gridheight = 5;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.fill = GridBagConstraints.BOTH;
      add( board, c );
      
      // add next shape board
      previewBoard.init();
      c = new GridBagConstraints();
      c.gridx = 1;
      c.gridy = 0;
      c.weightx = 0.2;
      c.weighty = 0.18;
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets( 5, 15, 5, 15 );
      add( previewBoard, c );
      
      // add score label
      scoreLabel.setForeground( Configuration.getColor( "label", "#000000" ) );
      c = new GridBagConstraints();
      c.gridx = 1;
      c.gridy = 1;
      c.weightx = 0.2;
      c.weighty = 0.05;
      c.anchor = GridBagConstraints.NORTH ;
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets( 0, 15, 0, 15 );
      add( scoreLabel, c );
      
      // add level label
      levelLabel.setForeground( Configuration.getColor( "label", "#000000" ) );
      c = new GridBagConstraints();
      c.gridx = 1;
      c.gridy = 2;
      c.weightx = 0.2;
      c.weighty = 0.05;
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets( 0, 15, 0, 15 );
      add( levelLabel, c );
      
      // add sleep label
      velocityLabel.setForeground( Configuration.getColor( "label", "#000000" ) );
      c = new GridBagConstraints();
      c.gridx = 1;
      c.gridy = 3;
      c.weightx = 0.2;
      c.weighty = 0.05;
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets( 0, 10, 5, 10 );
      add( velocityLabel, c );
      
      // add start/pause/resume button
      button.setBackground( Configuration.getColor( "button", "#d4d0c8" ) );
      c = new GridBagConstraints();
      c.gridx = 1;
      c.gridy = 4;
      c.weightx = 0.3;
      c.weighty = 1.0;
      c.anchor = GridBagConstraints.NORTH;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets( 15, 15, 15, 15 );
      add( button, c );
      
      // add event handling
      enableEvents( KeyEvent.KEY_EVENT_MASK );
      addKeyListener( new KeyAdapter()
      {
        public void keyPressed( KeyEvent kevt )
        {
          handleKeyEvent( kevt );
        }
      } );
      
      button.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent aevt )
        {
          handleButtonPressed();
          gameContainer.requestFocus();
        }
      } );
      
    }// GameContainer.initComponents()
    
    /**
     * Resizes all the static components and invalidates the current layout
     */
    private void resizeComponents()
    {
      Dimension $size = scoreLabel.getSize();
      Font $font;
      int $unitSize;
      
      // calculate the unit size
      $size = board.getSize();
      $size.width /= board.getBoardWidth();
      $size.height /= board.getBoardHeight();
      $unitSize = $size.width > $size.height ? $size.height : $size.width ;
      
      // adjust font sizes
      $font = new Font( "SansSerif", Font.BOLD, 3 + (int)( $unitSize / 1.8 ) );
      scoreLabel.setFont( $font );
      levelLabel.setFont( $font );
      velocityLabel.setFont( $font );
      $font = new Font( "SansSerif", Font.PLAIN, 2 + $unitSize / 2 );
      button.setFont( $font );
      
      // invalidate layout
      scoreLabel.invalidate();
      levelLabel.invalidate();
      velocityLabel.invalidate();
      button.invalidate();
      
    }// GameContainer.resizeComponents()
    
    /*
     *    F I E L D S
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    
    /**
     * The component size. If the component has been resized, that will be detected when
     * the paint method executes. If this value is set to null, the component dimensions are unknown.
     */
    private Dimension size = null ;
    
    /** The score label  */
    private JLabel scoreLabel = new JLabel( "Score: 0" );
    
    /** The level label  */
    private JLabel levelLabel = new JLabel( "Level: 1" );
    
    /** A label to display the velocity -- determined by the {@link GameThread#sleepTime}  */
    private JLabel velocityLabel = new JLabel( "Velocity: " );
    
    /** The generic button  */
    private JButton button = new JButton( "Start" );
    
    /** generated */
    private static final long serialVersionUID = 4347035043865113879L;

  }/* inner class GameContainer */
  
  /**
   * The game thread makes sure that the timer events are launched appropriately, making the current shape fall.
   * This thread can be reused across games, but should be set to paused state when no game is running.
   */
  private class GameThread extends Thread
  {
    /**
     * CONSTRUCTOR - creates a new game thread with default values
     */
    public GameThread()
    { }
    
  /*
   *    M E T H O D S
   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
        
    /** Resets the game thread. This will adjust the speed and start the game thread if not previously started.  */
    void reset()
    {
      adjustVelocity();

      removePointsAmount = PAUSED_POINTS_TO_REMOVE ;
      setPaused( false );
      
      if( !isAlive() )
      {
        start();
      }
    }// GameThread.reset()
    
    /**
     * @return true if the thread is paused, false otherwise
     */
    boolean isPaused()
    { return paused ;}
    
    /**
     * @param pause - the new paused flag value
     */
    void setPaused( boolean pause )
    {
      paused = pause ;
      if( paused )
        removePointsFlag = 1 ;
      
    }// GameThread.setPaused()
    
    /** Game is over -- waiting for user to press 'Start' to begin again.  */
    void setGameOver()
    {
      paused = true ;
      removePointsAmount = 0 ; // do not remove points from displayed final score
    }
    
    /**
     * Adjust the game velocity according to the current level.<br>
     * The sleep time is decreased as the level increases.
     * A level above ten (10) doesn't have any further effect.
     */
    void adjustVelocity()
    {
      if( sleepTime > MIN_SLEEP_TIME_MS )
        sleepTime = BASE_SLEEP_TIME_MS - ( level * MIN_SLEEP_TIME_MS );
      
      velocity = ( board.getHeight() * 10 ) / sleepTime ;
      
      handleVelocityModification();
      
    }// GameThread.adjustVelocity()
    
    /**
     * @return <var>velocity</var>
     */
    int getVelocity()
    { return velocity ;}
    
    /**
     * Run the game thread
     */
    public void run()
    {
      while( gameThread == this )
      {
        // make the time step
        handleTimer();
        
        // sleep for some time
        try
        {
          Thread.sleep( sleepTime );
        }
        catch( InterruptedException ignore )
        {
          // do nothing
        }
        
        // sleep if PAUSED or MINIMIZED
        while( gameThread == this && (paused || Game.this.getExtendedState() == ICONIFIED) )
        {
          try
          {
            Thread.sleep( PAUSED_SLEEP_TIME_MS );
          }
          catch( InterruptedException ignore )
          {
            // do nothing
          }
          
          // decrease the score slightly for each pause interval, unless minimized/iconified
          if( Game.this.getExtendedState() == NORMAL && total_score > 0 )
          {
            if( removePointsFlag % PAUSED_REMOVE_POINTS_REDUCTION_FACTOR == 0 )
            {
              total_score -= removePointsAmount ;
              handleScoreModification();
            }
            // increment the flag
            removePointsFlag++ ;
          }
          
        }// PAUSED or MINIMIZED
      }
    }// GameThread.run()
    
    /*
     *    F I E L D S
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    
    /** The base number of milliseconds to sleep between each move of a game piece.  */
    final static int BASE_SLEEP_TIME_MS = 500 ;
    
    /** Minimum number of milliseconds to sleep, also the difference in sleep time between adjacent levels.  */
    final static int MIN_SLEEP_TIME_MS = 50 ;
    
    /** The number of milliseconds to sleep while paused before re-checking the game state.  */
    final static int PAUSED_SLEEP_TIME_MS = 1000 ;
    
    /** While paused, only remove points one time per this value.  */
    final static int PAUSED_REMOVE_POINTS_REDUCTION_FACTOR = 4 ;
    
    /** Number of points to remove during each pause -- modified by {@link #PAUSED_REMOVE_POINTS_REDUCTION_FACTOR}.  */
    final static int PAUSED_POINTS_TO_REMOVE = 1 ;
    
    /**
     * The number of milliseconds to sleep between each move.
     * This number is reduced with each increase in {@link Game#level}
     * 
     * @see #velocity
     */
    private int sleepTime = BASE_SLEEP_TIME_MS ;
    
    /** The game pause flag. This flag is set to true when the game is paused.  */
    private boolean paused = true ;
    
    /** A flag to determine if points should be removed during a pause interval.  */
    private int removePointsFlag ;
    
    /** Number of points to remove during a pause interval.  */
    private int removePointsAmount ;
    
    /** The velocity of the moving pieces - determined by {@link #sleepTime}.  */
    private int velocity ;
    
  }/* inner class GameThread */
  
 /*
  *    F I E L D S
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
  
  /** default Board size */
  final static int  DEFAULT_WIDTH  = 16 ,
                    DEFAULT_HEIGHT = 24 ;
  
  /** default Score setting */
  final static int
                             BASE_SCORE =   9 ,
                       BASE_LEVEL_SCORE = 200 ,
                  BASE_LEVEL_MULTIPLIER =  50 ;

  static final int PREVIEW_BOARD_LENGTH = 5 ;
  
  /** Enable or disable debug actions.  */
  private boolean debugMode ;

  /** The main board. Used for the game itself.  */
  private Board board ;
  
  /** The preview board. Used to display a preview of the shapes.  */
  private Board previewBoard ;
  
  /** The current shape - updated as the game progresses.  */
  private Shape shape ;
  
  /** The next shape.  */
  private Shape nextShape ;
  
  /**
   * The shapes used on both boards.<br>
   * All shapes are re-utilized in order to avoid creating new objects while the game is running.<br>
   * Special care has to be taken when the preview shape and the current shape refer to the same object.
   */
  private Shape[] shapes = new Shape[7] ;
  
  /**
   * The {@link Game.GameContainer} that holds the game components.<br>
   * This {@link java.awt.Container} is created on the first call to {@link #getGameContainer()}.
   */
  private GameContainer gameContainer ;
  
  /**
   * The thread that runs the game.<br>
   * When this variable is set to null, the game thread will terminate.
   */
  private GameThread gameThread ;
  
  /** 
   * The level increases as points are accumulated.
   * Each succeeding level has an increased velocity and rewards more points for removal of lines.
   */
  private int level ;
  
  /** The current total number of points.  */
  private int total_score ;
  
  /** The number of points scored at the current level.  */
  private int level_score ;
  
  /** The rotation of the next shape.  */
  private int nextRotation ;
  
  /** The shape preview flag. If this flag is set, the shape will be shown in the shape preview board.  */
  private boolean preview = true ;
  
  /**
   * The move lock flag. If this flag is set, the current shape cannot be moved. This
   * flag is set when a shape is moved all the way down, and reset when a new shape is displayed.
   */
  private boolean moveLock = false ;

  /** Generated  */
  @SuppressWarnings( "unused" )
  private static final long serialVersionUID = 3099704049228659953L ;

}// class Game
