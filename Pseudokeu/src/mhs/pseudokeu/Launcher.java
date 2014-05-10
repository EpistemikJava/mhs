/* ***************************************************************************************
 
   Mark Sattolo (epistemik@gmail.com)
 -----------------------------------------------
 $File: //depot/Eclipse/Java/workspace/Pseudokeu/src/mhs/pseudokeu/Launcher.java $
 $Revision: #15 $
 $Change: 177 $
 $DateTime: 2012/02/25 11:43:40 $
 -----------------------------------------------
 
  mhs.latinsquare.LatinSquareGame.java
  Eclipse version created on Nov 30, 2007, 10:54 PM
  git version created Mar 8, 2014
 
*************************************************************************************** */

package mhs.pseudokeu;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.* ;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Main Game Frame
 *
 * @author Mark Sattolo
 * @version 8.1.2
 * @see JFrame
 */
public class Launcher extends JFrame
{
 /*
  *            I N N E R    C L A S S E S
  *************************************************************************************************************/
  
  /**
   *  Handles game events - EXCEPT those in the <em>other</em> Inner Classes
   *  
   *  @see java.awt.event.ActionListener
   */
  class GameListener implements ActionListener
  {
    private static final
    String
          strCHANGE_DIFFICULTY_CONFIRM = "Change Difficulty Confirmation" ,
          strEND_GAME_AND_SET_DIFFICULTY = "! End this game & set a NEW level of difficulty?" ,
          strEND_GAME_AND_ADD = "! End this game & ADD a new USER game?" ,
          strCLEAR_AND_SELECT = "! Clear ALL values & Select a Game?" ,
          strCLEAR_AND_LOAD = "! Clear ALL values & Load a NEW game?" ;
    
    /** Component (button, menu, etc) that was activated
     *  @see ActionEvent#getSource
     */
    private Object source;
    
    /** Command that triggered the event
     *  @see ActionEvent#getActionCommand
     */
    private String event ;
    
    /** Result from a Confirmation box
     *  @see JOptionPane#showConfirmDialog
     */
    private int confirm ;
    
    /**
     *  Respond to Game menu and button events <br>
     *  - EXCEPT those for Inner Classes, i.e. {@link GameSizeFrame}, {@link AddGameFrame},
     *    {@link SelectGameFrame}, {@link ChooseDifficultyFrame} & {@link SolveDelayFrame}
     *  @param ae - {@link ActionEvent}
     *  @see ActionListener#actionPerformed
     */
    public void actionPerformed( ActionEvent ae )
    {
      source = ae.getSource();
       event = ae.getActionCommand() + strEVENT ;
      confirm = 0 ;
      
      // CLOCK
      if( source == clock )
      {
        logger.finest( event );
        runClock();
      }
      // SOLVE TIMER SIGNAL
      else if( source == solveTimer )
      {
        logger.finer( event );
        revealSquare();
      }
      // HINT BUTTON
      else if( source == hintButton )
      {
        logger.finer( event );

        confirm = JOptionPane.showConfirmDialog( Launcher.this, "Reveal ONE currently Unknown value?",
                                                 strHint, JOptionPane.YES_NO_OPTION );
        if( confirm == 0 )
        {
          revealSquare();
        }
      }
      // TOGGLE SOLVE TIMER
      else if( source == solveButton )
      {
        logger.info( event );
        toggleAutoSolve();
      }
      // TOGGLE CONFLICTS
      else if( source == conflictsButton )
      {
        logger.info( event );
        setConflicts( true );
      }
      // EXIT
      else if( source == exitMenuItem )
      {
        logger.fine( event );
        confirm = JOptionPane.showConfirmDialog( Launcher.this, (strExitCap + ' ' + GAME_NAME + '?'),
                                                 strExitCap + ' ' + strConfirm, JOptionPane.YES_NO_OPTION );
        if( confirm == 0 )
        {
          halt();
        }
      }
      else // turn OFF Solve for these events
      {
        logger.info( event );
        
        // turn OFF Solve if active
        if( autoSolveActivated() )
          toggleAutoSolve();
        
        // NEW GAME
        if( (source == newGameButton) || (source == newGameMenuItem) )
        {
          if( isRunning()  &&  grid.hasEntries() )
            confirm = JOptionPane.showConfirmDialog( Launcher.this, strCLEAR_AND_LOAD,
                                                     strNewGame + ' ' + strConfirm, JOptionPane.YES_NO_OPTION );
          if( confirm == 0 )
          {
            if( isAddingGame() )
              launchAddGameFrame( false ) ;
            
            newGame( RANDOM_GAME );
          }
        }
        // SELECT GAME
        else if( source == selectGameMenuItem )
        {
          if( isAddingGame()  &&  grid.hasEntries() )
            confirm = JOptionPane.showConfirmDialog( Launcher.this, strCLEAR_AND_SELECT,
                strSelectGame.replace( "...", " " ).replace( "a ", "" ) + strConfirm, JOptionPane.YES_NO_OPTION );
          if( confirm == 0 )
          {
            if( isAddingGame() )
              launchAddGameFrame( false ) ;
            
            launchSelectGameFrame();
          }
        }
        // ADD GAME
        else if( source == addGameMenuItem )
        {
          if( isRunning()  &&  grid.hasEntries() )
            confirm = JOptionPane.showConfirmDialog( Launcher.this, strEND_GAME_AND_ADD,
                strAddGame.replace( "...", " " ).replace( "a ", "" ) + strConfirm, JOptionPane.YES_NO_OPTION );
          if( confirm == 0 )
          {
            // make sure Loader is not still in the worker thread
            if( ! loadWorkerDone() )
              return ;
            
            launchAddGameFrame( true );
          }
        }
        // CHANGE GAME SIZE
        else if( source == sizeMenuItem )
        {
          launchSetSizeFrame();
        }
        // SET SOLVE DELAY
        else if( source == solveDelayMenuItem )
        {
          launchSolveDelayFrame();
        }
        // CHOOSE DIFFICULTY LEVEL
        else if( source == difficultyMenuItem )
        {
          if( isRunning()  &&  grid.hasEntries() )
            confirm = JOptionPane.showConfirmDialog( Launcher.this, strEND_GAME_AND_SET_DIFFICULTY,
                                                     strCHANGE_DIFFICULTY_CONFIRM, JOptionPane.YES_NO_OPTION );
          if( confirm == 0 )
          {
            if( isAddingGame() )
              launchAddGameFrame( false ) ;
            
            launchChooseDifficultyFrame();
          }
        }
        else if( source == revealWrongMenuItem )
        {
          revealWrongGuess();
        }
        else if( source == instructMenuItem )
        {
          launchInstructionBox();
        }
        else if( source == aboutMenuItem )
        {
          launchAboutBox();
        }
        // UNDO
        else if( source == undoButton )
        {
          undoLastEntry();
        }
        // REDO
        else if( source == redoButton )
        {
          redoLastUndoAction();
        }
        
      }// turn OFF Solve
    
    }// GameListener.actionPerformed()

  }/* INNER CLASS GameListener */

 //============================================================================================================
  
  /**
   *  An extended <b>JFrame</b> which allows users to change the size of the Game <br>
   *  - contains radio buttons, action buttons, and implements <b>ActionListener</b>
   *  
   *  @see JFrame
   *  @see ActionListener
   *  @see #launchSetSizeFrame
   */
  class GameSizeFrame extends JFrame implements ActionListener
  {
   /*
    *     FIELDS
    * ========================================================================================= */
    
    /** Just in case...  */
    private static final long serialVersionUID = -7518248412658045164L;
    
    static final String
                       strSmall = "small",
                       strMed   = "Medium" ,
                       strLarge = "LARGE",
                       strTITLE = "Change Game Size" ,
                       HTML_MSG = "<html><br><h1><font color=blue>Game Size:</font></h1></html>" ;
    
    /** identify the source of action events */
    private Object source ;
    
    // default
    private int newSqrSize = SQUARE_SIZE_SM ;
    
    Font myFont ;
    
    JPanel msgPanel, selectPanel, confirmPanel ;
    JLabel msgLabel ;
    
    JButton acceptBtn, cancelBtn ;
    
    ButtonGroup sizeGroup ;
    JRadioButton smSizeRadBtn, mdSizeRadBtn, lgSizeRadBtn ;
    
   /*
    *     METHODS
    * ========================================================================================= */
    
    /** CONSTRUCTOR */
    public GameSizeFrame()
    {
      logger.logInit();
      
      setTitle( strTITLE );
      
      // will close ONLY after clicking the 'Accept' or 'Cancel' buttons
      setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
      
      setMinimumSize( new Dimension(pxMsgFrameWidth, pxMsgFrameWidth / 2) );
      setResizable( false );
      
      getContentPane().setLayout( new GridLayout(3,1,2,2) );
      
      initComponents();
      
      // get Keyboard focus
      if( ! isFocusOwner() )
        requestFocusInWindow();
      
    }// GameSizeFrame Constructor
    
    /** called by {@link #GameSizeFrame} */
    private void initComponents()
    {
      myFont = new Font( "serif", Font.BOLD, getWidth() / 24 );
      
      msgPanel = new JPanel();
      msgPanel.setFocusable( false );
      
      msgLabel = new JLabel( HTML_MSG );
      msgPanel.add( msgLabel );
      
      initRadioButtons();
      initActionButtons();
      
      // insert the components
      getContentPane().add( msgPanel  );
      getContentPane().add( selectPanel  );
      getContentPane().add( confirmPanel );
      
    }// GameSizeFrame.initComponents()
    
    /** called by {@link GameSizeFrame#initComponents} */
    private void initRadioButtons()
    {
      selectPanel = new JPanel( new GridLayout(1,1) );
      
      // use a Box to space out the buttons properly
      Box $selectBox = Box.createHorizontalBox();
      
      sizeGroup = new ButtonGroup();
      
      smSizeRadBtn = new JRadioButton( strSmall, true  );
      mdSizeRadBtn = new JRadioButton( strMed,   false );
      lgSizeRadBtn = new JRadioButton( strLarge, false );
      
      smSizeRadBtn.setFont( myFont );
      mdSizeRadBtn.setFont( myFont );
      lgSizeRadBtn.setFont( myFont );
      
      smSizeRadBtn.setMnemonic( smSizeRadBtn.getText().charAt(0) );
      mdSizeRadBtn.setMnemonic( mdSizeRadBtn.getText().charAt(0) );
      lgSizeRadBtn.setMnemonic( lgSizeRadBtn.getText().charAt(0) );
      
      checkSelectedSize();
      
      lgSizeRadBtn.addActionListener( this );
      smSizeRadBtn.addActionListener( this );
      mdSizeRadBtn.addActionListener( this );
      
      sizeGroup.add( smSizeRadBtn );
      sizeGroup.add( mdSizeRadBtn );
      sizeGroup.add( lgSizeRadBtn );
      
      // glue creates spaces between buttons
      $selectBox.add( Box.createGlue() );
      $selectBox.add( smSizeRadBtn );
      $selectBox.add( Box.createGlue() );
      $selectBox.add( mdSizeRadBtn );
      $selectBox.add( Box.createGlue() );
      $selectBox.add( lgSizeRadBtn );
      $selectBox.add( Box.createGlue() );
      
      selectPanel.add( $selectBox );

    }// GameSizeFrame.initRadioButtons()
    
    /** called by {@link GameSizeFrame#initComponents} */
    private void initActionButtons()
    {
      confirmPanel = new JPanel( new GridLayout(1,1) );

      // use a Box to space out the buttons properly
      Box $confirmBox = Box.createHorizontalBox();
      
      acceptBtn = new JButton( strAccept );
      cancelBtn = new JButton( strCancel );
      
      acceptBtn.setFont( myFont );
      cancelBtn.setFont( myFont );
      
      acceptBtn.setMnemonic( acceptBtn.getText().charAt(0) );
      cancelBtn.setMnemonic( cancelBtn.getText().charAt(0) );
      
      acceptBtn.addActionListener( this );
      cancelBtn.addActionListener( this );
      
      // glue creates spaces between buttons
      $confirmBox.add( Box.createGlue() );
      $confirmBox.add( acceptBtn );
      $confirmBox.add( Box.createGlue() );
      $confirmBox.add( cancelBtn );
      $confirmBox.add( Box.createGlue() );
      
      confirmPanel.add( $confirmBox );
      
    }// GameSizeFrame.initActionButtons()

    /**
     *  @param ae - {@link ActionEvent}
     *  @see ActionListener#actionPerformed
     */
    public void actionPerformed( ActionEvent ae )
    {
      logger.info( ae.getActionCommand() );
      
      source = ae.getSource();
      
      if( source == smSizeRadBtn )
      {
        newSqrSize = SQUARE_SIZE_SM ;
      }
      else if( source == mdSizeRadBtn )
      {
        newSqrSize = SQUARE_SIZE_MD ;
      }
      else if( source == lgSizeRadBtn )
      {
        newSqrSize = SQUARE_SIZE_LG ;
      }
      else if( source == acceptBtn )
      {
        // check the new value
        confirm();
        dispose();
      }
      else if( source == cancelBtn )
      {
        // just close without changing anything
        dispose();
      }
      
    }// GameSizeFrame.actionPerformed()
    
    /**
     *  Check, then reject or accept the new value for {@link #pxSqrLength}
     *  @see #setGameSize
     *  @see Grid#changeSize
     */
    private void confirm()
    {
      logger.info( "Current Sqr length = " + pxSqrLength + "; New Sqr length = " + newSqrSize );
      
      if( pxSqrLength == newSqrSize )
        return ;
      
      pxSqrLength = newSqrSize ;
      
      setGameSize();
      
      grid.changeSize( newSqrSize );
      
      Launcher.this.validate();
      
    }// GameSizeFrame.confirm()
    
    private void checkSelectedSize()
    {
      if( (pxSqrLength != SQUARE_SIZE_SM) && (pxSqrLength != SQUARE_SIZE_MD) && (pxSqrLength != SQUARE_SIZE_LG) )
      {
        logger.warning( "Problem with Square size!" );
        return ;
      }
      smSizeRadBtn.setSelected( pxSqrLength == SQUARE_SIZE_SM );
      
      mdSizeRadBtn.setSelected( pxSqrLength == SQUARE_SIZE_MD );
      
      lgSizeRadBtn.setSelected( pxSqrLength == SQUARE_SIZE_LG );
    }
  
  }/* INNER CLASS GameSizeFrame */
  
 //============================================================================================================
  
  /**
   *  An extended <b>JFrame</b> which allows users to select a particular Game <br>
   *  - contains a Combo box, action buttons, and implements <b>ActionListener</b>
   *  
   *  @see JFrame
   *  @see ActionListener
   *  @see #launchSelectGameFrame
   */
  class SelectGameFrame extends JFrame implements ActionListener, ChangeListener
  {
   /*
    *     FIELDS
    * ========================================================================================= */
    
    /** Just in case...  */
    private static final long serialVersionUID = 4929409014347525410L;
    
    static final String
                       STR_TITLE = "Select a Game" ,
                       HTML_MSG  = "<html><br><h1><font color=blue>Choose a Game Index:</font></h1></html>" ;
    
    /** identify the source of action events */
    private Object source ;
    
    // default
    private int selectedGame, currentMax ;
    
    Font myFont ;
    
    JPanel msgPanel, selectPanel, confirmPanel ;
    JLabel msgLabel ;
    
    JButton acceptBtn, cancelBtn ;
    
    JSpinner spinner ;
    SpinnerNumberModel model ;
    
    
   /*
    *     METHODS
    * ========================================================================================= */
    
    /** CONSTRUCTOR */
    public SelectGameFrame()
    {
      logger.logInit();
      
      setTitle( STR_TITLE );
      
      // will close ONLY after clicking the 'Accept' or 'Cancel' buttons
      setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
      
      setMinimumSize( new Dimension(pxMsgFrameWidth, pxMsgFrameWidth / 2) );
      setResizable( false );
      
      getContentPane().setLayout( new GridLayout(3,1,2,2) );
      
      initComponents();
      
      // get Keyboard focus
      if( ! isFocusOwner() )
        requestFocusInWindow();
      
    }// SelectGameFrame Constructor
    
    /** called by {@link #SelectGameFrame} */
    private void initComponents()
    {
      myFont = new Font( "serif", Font.BOLD, getWidth() / 21 );
      
      msgPanel = new JPanel();
      msgPanel.setFocusable( false );
      
      msgLabel = new JLabel( HTML_MSG );
      msgPanel.add( msgLabel );
      
      initSpinner();
      initActionButtons();
      
      // insert the components
      getContentPane().add( msgPanel  );
      getContentPane().add( selectPanel  );
      getContentPane().add( confirmPanel );
      
    }// SelectGameFrame.initComponents()
    
    /** called by {@link SelectGameFrame#initComponents} */
    private void initSpinner()
    {
      selectPanel = new JPanel();
      
      // set the number model
      model = new SpinnerNumberModel( 1, // initial value
                                      1, // min
                                      MAX_NUM_LOADED_GAMES, // max - will be updated when spinner is called
                                      1  // step
                                     );
      
      spinner = new JSpinner( model );
      
      spinner.setPreferredSize( new Dimension( pxMsgFrameWidth / 3, spinner.getFont().getSize() * 3 ) );
      
      spinner.addChangeListener( this );
      
      selectPanel.add( spinner );
      
    }// SelectGameFrame.initSpinner()
    
    /** called by {@link SelectGameFrame#initComponents} */
    private void initActionButtons()
    {
      confirmPanel = new JPanel( new GridLayout(1,1) );
      
      // use a Box to space out the buttons properly
      Box $confirmBox = Box.createHorizontalBox();
      
      acceptBtn = new JButton( strAccept );
      cancelBtn = new JButton( strCancel );
      
      acceptBtn.setFont( myFont );
      cancelBtn.setFont( myFont );
      
      acceptBtn.setMnemonic( acceptBtn.getText().charAt(0) );
      cancelBtn.setMnemonic( cancelBtn.getText().charAt(0) );
      
      acceptBtn.addActionListener( this );
      cancelBtn.addActionListener( this );
      
      // glue creates spaces between buttons
      $confirmBox.add( Box.createGlue() );
      $confirmBox.add( acceptBtn );
      $confirmBox.add( Box.createGlue() );
      $confirmBox.add( cancelBtn );
      $confirmBox.add( Box.createGlue() );
      
      confirmPanel.add( $confirmBox );
      
    }// SelectGameFrame.initActionButtons()
    
    /**
     *  @param ae - {@link ActionEvent}
     *  @see ActionListener#actionPerformed
     */
    public void actionPerformed( ActionEvent ae )
    {
      logger.info( ae.getActionCommand() );
      
      source = ae.getSource();
      
      if( source == acceptBtn )
      {
        // check the new value
        confirm();
      }
      else if( source == cancelBtn )
      {
        // just close without changing anything
        dispose();
      }
      
    }// SelectGameFrame.actionPerformed()
    
    /**
     *  @param ce - {@link ActionEvent}
     *  @see ActionListener#actionPerformed
     */
    public void stateChanged( ChangeEvent ce )
    {
      source = ce.getSource();
      
      if( source == spinner )
      {
        selectedGame = Integer.parseInt( model.getValue().toString() );
        logger.info( "Spinner changed to get game #" + selectedGame );
      }
      
    }// SelectGameFrame.stateChanged()
    
    /**
     *  Check, then reject or accept the choice for a new game
     *  @see #newGame
     */
    private void confirm()
    {
      if( (selectedGame < 1) || (selectedGame > currentMax) )
        logger.warning( "selectedGame is NOT valid!" );
      else
      {
        String $oldgame = currentGameName ;
        currentGameName = new String( loader.getFileName(difficulty) + Integer.toString(selectedGame) );
        logger.info( "Current Game is '" + $oldgame + "' & New game will be '" + currentGameName + "'" );
        
        newGame( loader.getGameIndex(difficulty, currentGameName) );
        
        Launcher.this.validate();
      }
      
      dispose();
      
    }// SelectGameFrame.confirm()
    
    /** Set the spinner current & maximum values */
    private void setSpinner()
    {
      model.setValue( Integer.valueOf(currentGameNum) );
      
      currentMax = loader.getNumLoadedGames( difficulty );
      model.setMaximum( Integer.valueOf(currentMax) );
    }
  
  }/* INNER CLASS SelectGameFrame */
  
 //============================================================================================================
  
  /**
   *  An extended <b>JFrame</b> which allows users to ADD a Game <br>
   *  - contains action buttons and implements <b>ActionListener</b>
   *  
   *  @see JFrame
   *  @see ActionListener
   *  @see #launchAddGameFrame
   */
  class AddGameFrame extends JFrame implements ActionListener
  {
  /*
    *     FIELDS
    * ========================================================================================= */
    
    /** Just in case...  */
    private static final long serialVersionUID = 1983697817634130734L;
    
    static final String
      STR_ADDGAME   = "Add Game" ,
      STR_ADDANDCLOSE = "Add Game & Close" ,
      HTML_MSG  
       = "<html><br><h1><font color=blue><u>ADD a Game to the USER folder</u></font></h1>" +
           "<ul>" +
           "<li>Insert values in the blank Grid to define a new Game.</li>" +
           "<li>Submitted Games will be checked - <b>NO</b> conflicting values will be allowed.</li>" +
           "<li>Any values NOT in the range <font color=green>1-9</font> will be ignored...</li>" +
           "<li>When finished - click 'Add Game' to check this game and prepare to add another, " +
           "<li>&nbsp;&nbsp;<u>OR</u> click 'Add & Close' to check the game and close this Window.</li>" +
           "<li><font color=red>Be CAREFUL when adding - New Games are <u>NOT</u> " +
             "analyzed to ensure they are Valid - i.e. have a <u>Unique</u> solution!</font></li>" +
         "</ul></html>" ;
    
    /** identify the source of action events */
    private Object source ;
    
    /** ensure that strings & buttons display properly */
    private int pxMsgHt = 300 ,
                pxCfmHt = 120 ,
                pxPrefWidth = 768 ,
                pxWidth ,
                pxHeight ;
    
    Font myFont ;
    
    JPanel msgPanel, confirmPanel ;
    JLabel msgLabel ;
    
    JButton addBtn, addAndCloseBtn, cancelBtn ;
    
   /*
    *     METHODS
    * ========================================================================================= */
    
    /** CONSTRUCTOR */
    public AddGameFrame()
    {
      setTitle( STR_ADDGAME );
      
      // will close ONLY after clicking an Action button
      setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
      
      // make sure frame fits on user's screen
      pxWidth  = Math.min( pxScreenSize.width, pxPrefWidth );
      pxHeight = Math.min( pxScreenSize.height, pxMsgHt + pxCfmHt );
      logger.logInit( "Frame width = " + pxWidth + " & height = " + pxHeight );
      
      setMinimumSize( new Dimension(pxWidth, pxHeight) );
      setResizable( false );
      
      getContentPane().setLayout( new GridLayout(2,1,2,2) );
      
      initComponents();
      
      // get Keyboard focus
      if( ! isFocusOwner() )
        requestFocusInWindow();
      
    }// AddGameFrame Constructor
    
    /** called by {@link #AddGameFrame} */
    private void initComponents()
    {
      myFont = new Font( "serif", Font.BOLD, getWidth() / 24 );
      
      msgPanel = new JPanel();
      msgPanel.setFocusable( false );
      
      msgLabel = new JLabel( HTML_MSG );
      msgPanel.add( msgLabel );
      
      // adjust in case frame height changed because of user screen setting
      msgPanel.setSize( pxWidth, pxHeight * pxMsgHt / (pxMsgHt + pxCfmHt) );
      
      initActionButtons();
      
      logger.logInit( "msgPanel height = " + msgPanel.getHeight() + "; confirmPanel height = " + confirmPanel.getHeight() );
      
      // insert the components
      getContentPane().add( msgPanel  );
      getContentPane().add( confirmPanel );
      
    }// AddGameFrame.initComponents()
    
    /** called by {@link #initComponents} */
    private void initActionButtons()
    {
      confirmPanel = new JPanel( new GridLayout(1,1) );
      
      // use a Box to space out the buttons properly
      Box $confirmBox = Box.createHorizontalBox();
      
      addBtn = new JButton( STR_ADDGAME );
      addAndCloseBtn = new JButton( STR_ADDANDCLOSE );
      cancelBtn = new JButton( strCancel );
      
      addBtn.setFont( myFont );
      addAndCloseBtn.setFont( myFont );
      cancelBtn.setFont( myFont );
      
      addBtn.setMnemonic( addBtn.getText().charAt(0) );
      addAndCloseBtn.setMnemonic( addAndCloseBtn.getText().charAt(1) );
      cancelBtn.setMnemonic( cancelBtn.getText().charAt(0) );
      
      addBtn.addActionListener( this );
      addAndCloseBtn.addActionListener( this );
      cancelBtn.addActionListener( this );
      
      // glue creates spaces between buttons
      $confirmBox.add( Box.createGlue() );
      $confirmBox.add( addBtn );
      $confirmBox.add( Box.createGlue() );
      $confirmBox.add( addAndCloseBtn );
      $confirmBox.add( Box.createGlue() );
      $confirmBox.add( cancelBtn );
      $confirmBox.add( Box.createGlue() );
      
      confirmPanel.add( $confirmBox );

      // adjust in case frame height changed because of user screen setting
      confirmPanel.setSize( pxWidth, pxHeight * pxCfmHt / (pxMsgHt + pxCfmHt) );
      
    }// AddGameFrame.initActionButtons()

    /**
     *  @param ae - {@link ActionEvent}
     *  @see ActionListener#actionPerformed
     */
    public void actionPerformed( ActionEvent ae )
    {
      logger.info( ae.getActionCommand() );
      
      source = ae.getSource();
      
      if( source == addBtn )
      {
        // check the new value and prepare for another added game
        confirm();
      }
      else if( source == addAndCloseBtn )
      {
        // check the new game
        // close if OK, otherwise give user a chance to fix problems
        if( confirm() )
          launchAddGameFrame( false ) ;
      }
      else if( source == cancelBtn )
      {
        // just close without adding anything
        launchAddGameFrame( false ) ;
      }
      
    }// AddGameFrame.actionPerformed()
    
    /**
     * Check, then reject or accept the ADDED game
     * @return Added Game OK or NOT
     * @see #verifyAddedGame
     */
    private boolean confirm()
    {
      if( verifyAddedGame() )
      {
        Launcher.this.validate();
        return true ;
      }
      
      return false ;

    }// AddGameFrame.confirm()
    
  }/* INNER CLASS AddGameFrame */
  
  //===========================================================================================================
   
  /**
   *  An extended <b>JFrame</b> which allows users to select a particular level of difficulty <br>
   *  - contains a Combo box, action buttons, and implements <b>ActionListener</b>
   *  
   *  @see JFrame
   *  @see ActionListener
   *  @see #launchChooseDifficultyFrame
   */
  class ChooseDifficultyFrame extends JFrame implements ActionListener
  {
   /*
    *     FIELDS
    * ========================================================================================= */
    
    /** Just in case...  */
    private static final long serialVersionUID = -2200890974403009644L;
    
    static final String
                 STR_TITLE = "Choose a level of difficulty" ,
                 HTML_MSG  = "<html><br><h1><font color=blue>Choose a level of difficulty:</font></h1></html>" ;
    
    /** identify the source of action events */
    private Object source ;
    
    // default
    private String chosenDifficulty ;
    
    Font myFont ;
    
    JPanel msgPanel, choosePanel, confirmPanel ;
    JLabel msgLabel ;
    
    JButton acceptBtn, cancelBtn ;
    
    /** Java 1.7 makes JComboBox a template type */
    @SuppressWarnings( "rawtypes" )
    JComboBox comboBox ;
    
   /*
    *     METHODS
    * ========================================================================================= */
    
    /** CONSTRUCTOR */
    public ChooseDifficultyFrame()
    {
      logger.logInit();
      
      setTitle( STR_TITLE );
      
      // will close ONLY after clicking the 'Accept' or 'Cancel' buttons
      setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
      
      setMinimumSize( new Dimension(pxMsgFrameWidth, pxMsgFrameWidth / 2) );
      setResizable( false );
      
      getContentPane().setLayout( new GridLayout(3,1,2,2) );
      
      initComponents();
      
      // get Keyboard focus
      if( ! isFocusOwner() )
        requestFocusInWindow();
      
    }// ChooseDifficultyFrame Constructor
    
    /** called by {@link #ChooseDifficultyFrame} */
    private void initComponents()
    {
      myFont = new Font( "serif", Font.BOLD, getWidth() / 21 );
      
      msgPanel = new JPanel();
      msgPanel.setFocusable( false );
      
      msgLabel = new JLabel( HTML_MSG );
      msgPanel.add( msgLabel );
      
      initComboBox();
      initActionButtons();
      
      // insert the components
      getContentPane().add( msgPanel  );
      getContentPane().add( choosePanel  );
      getContentPane().add( confirmPanel );
      
    }// ChooseDifficultyFrame.initComponents()
    
    /** called by {@link #initComponents} */
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private void initComboBox()
    {
      choosePanel = new JPanel();
      
      comboBox = new JComboBox( Loader.STR_DIFF_FOLDERS );
      comboBox.setPreferredSize( new Dimension( pxMsgFrameWidth / 3, comboBox.getFont().getSize() * 3 ) );
      
      comboBox.addActionListener( this );
      
      choosePanel.add( comboBox );
      
    }// ChooseDifficultyFrame.initComboBox()
    
    /** called by {@link #initComponents} */
    private void initActionButtons()
    {
      confirmPanel = new JPanel( new GridLayout(1,1) );
      
      // use a Box to space out the buttons properly
      Box $confirmBox = Box.createHorizontalBox();
      
      acceptBtn = new JButton( strAccept );
      cancelBtn = new JButton( strCancel );
      
      acceptBtn.setFont( myFont );
      cancelBtn.setFont( myFont );
      
      acceptBtn.setMnemonic( acceptBtn.getText().charAt(0) );
      cancelBtn.setMnemonic( cancelBtn.getText().charAt(0) );
      
      acceptBtn.addActionListener( this );
      cancelBtn.addActionListener( this );
      
      // glue creates spaces between buttons
      $confirmBox.add( Box.createGlue() );
      $confirmBox.add( acceptBtn );
      $confirmBox.add( Box.createGlue() );
      $confirmBox.add( cancelBtn );
      $confirmBox.add( Box.createGlue() );
      
      confirmPanel.add( $confirmBox );
      
    }// ChooseDifficultyFrame.initActionButtons()

    /**
     *  @param ae - {@link ActionEvent}
     *  @see ActionListener#actionPerformed
     */
    @SuppressWarnings( "rawtypes" )
    public void actionPerformed( ActionEvent ae )
    {
      logger.info( ae.getActionCommand() );
      
      source = ae.getSource();
      
      if( source == comboBox )
      {
        chosenDifficulty = (String)((JComboBox)source).getSelectedItem();
      }
      else if( source == acceptBtn )
      {
        // check the new value
        confirm( true );
      }
      else if( source == cancelBtn )
      {
        // just close without changing anything
        confirm( false );
      }
      
    }// ChooseDifficultyFrame.actionPerformed()
    
    /**
     *  Check, then reject or accept the choice for a new game
     *  @param check - verify or ignore changes
     *  @see #newGame
     */
    private void confirm( boolean check )
    {
      if( chosenDifficulty == null )
      {
        logger.warning( "chosenDifficulty is null!" );
        return ;
      }
      
      if( check )
      {
        logger.info( "Current difficulty is '" + Loader.STR_DIFF_FOLDERS[difficulty]
                     + "' & New difficulty will be '" + chosenDifficulty + "'" );
        
        if( ! chosenDifficulty.isEmpty() )
        {
          difficulty = loader.getDifficultyIndex( chosenDifficulty );
          newGame( RANDOM_GAME );
          Launcher.this.validate();
        }
      }
      
      dispose();
      
    }// ChooseDifficultyFrame.confirm()
    
    /** Set the combo box selection
     *  @param sel - index to set  */
    private void setComboBoxSelection( int sel ) { comboBox.setSelectedIndex( sel ); }
  
  }/* INNER CLASS ChooseDifficultyFrame */
  
 //============================================================================================================
  
  /**
   *  An extended <b>JFrame</b> which allows users to select the delay interval for revealing Solved Squares<br>
   *  - contains a Combo box, action buttons, and implements <b>ActionListener</b>
   *  
   *  @see JFrame
   *  @see ActionListener
   *  @see #solveTimer
   *  @see #launchSolveDelayFrame
   */
  class SolveDelayFrame extends JFrame implements ActionListener
  {
   /*
    *     FIELDS
    * ========================================================================================= */
    
    /** Just in case...  */
    private static final long serialVersionUID = -7213611964032375294L;
    
    static final String
                 STR_TITLE = "Select the Solve Delay" ,
                 HTML_MSG  = "<html><br><h1><font color=blue>Choose a Delay interval:</font></h1></html>" ;
    
    private String[] selections = { "1 sec", "2 sec", "4 sec", "8 sec", "16 sec",
                                    "24 sec", "32 sec", "48 sec", "64 sec", "96 sec" };
    
    /** identify the source of action events */
    private Object source ;
    
    /** selection index  */
    private int nSelection ;
    
    Font myFont ;
    
    JPanel msgPanel, selectPanel, confirmPanel ;
    JLabel msgLabel ;
    
    JButton acceptBtn, cancelBtn ;
    
    /** Java 1.7 makes JComboBox a template type */
    @SuppressWarnings( "rawtypes" )
    JComboBox comboBox, src ;
    
   /*
    *     METHODS
    * ========================================================================================= */
    
    /** CONSTRUCTOR */
    public SolveDelayFrame()
    {
      logger.logInit();
      
      setTitle( STR_TITLE );
      
      // will close ONLY after clicking the 'Accept' or 'Cancel' buttons
      setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
      
      setMinimumSize( new Dimension(pxMsgFrameWidth, pxMsgFrameWidth / 2) );
      setResizable( false );
      
      getContentPane().setLayout( new GridLayout(3,1,2,2) );
      
      initComponents();
      
      // default
      nSelection = convertMsecToIndex( USER_SOLVE_DELAY_MSEC );
      
      // get Keyboard focus
      if( ! isFocusOwner() )
        requestFocusInWindow();
      
    }// SolveDelayFrame Constructor
    
    /** called by {@link #SolveDelayFrame} */
    private void initComponents()
    {
      myFont = new Font( "serif", Font.BOLD, getWidth() / 21 );
      
      msgPanel = new JPanel();
      msgPanel.setFocusable( false );
      
      msgLabel = new JLabel( HTML_MSG );
      msgPanel.add( msgLabel );
      
      initComboBox();
      initActionButtons();
      
      // insert the components
      getContentPane().add( msgPanel  );
      getContentPane().add( selectPanel  );
      getContentPane().add( confirmPanel );
      
    }// SolveDelayFrame.initComponents()
    
    /** called by {@link #initComponents} */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private void initComboBox()
    {
      selectPanel = new JPanel();
      
      comboBox = new JComboBox( selections );
      comboBox.setSelectedIndex( nSelection );
      comboBox.setPreferredSize( new Dimension( pxMsgFrameWidth / 3, comboBox.getFont().getSize() * 3 ) );
      
      comboBox.addActionListener( this );
      
      selectPanel.add( comboBox );
      
    }// SolveDelayFrame.initComboBox()
    
    /** called by {@link #initComponents} */
    private void initActionButtons()
    {
      confirmPanel = new JPanel( new GridLayout(1,1) );
      
      // use a Box to space out the buttons properly
      Box $confirmBox = Box.createHorizontalBox();
      
      acceptBtn = new JButton( strAccept );
      cancelBtn = new JButton( strCancel );
      
      acceptBtn.setFont( myFont );
      cancelBtn.setFont( myFont );
      
      acceptBtn.setMnemonic( acceptBtn.getText().charAt(0) );
      cancelBtn.setMnemonic( cancelBtn.getText().charAt(0) );
      
      acceptBtn.addActionListener( this );
      cancelBtn.addActionListener( this );
      
      // glue creates spaces between buttons
      $confirmBox.add( Box.createGlue() );
      $confirmBox.add( acceptBtn );
      $confirmBox.add( Box.createGlue() );
      $confirmBox.add( cancelBtn );
      $confirmBox.add( Box.createGlue() );
      
      confirmPanel.add( $confirmBox );
      
    }// SolveDelayFrame.initActionButtons()

    /**
     *  @param ae - {@link ActionEvent}
     *  @see ActionListener#actionPerformed
     */
    @SuppressWarnings( "rawtypes" )
    public void actionPerformed( ActionEvent ae )
    {
      logger.info( ae.getActionCommand() );
      
      source = ae.getSource();
      
      if( source == comboBox )
      {
        src = (JComboBox)source;
        nSelection = src.getSelectedIndex();
        logger.info( "selectedIndex == " + nSelection );
      }
      else if( source == acceptBtn )
      {
        // check the new value
        confirm( true );
      }
      else if( source == cancelBtn )
      {
        // just close without changing anything
        confirm( false );
      }
      
    }// SolveDelayFrame.actionPerformed()
    
    /**
     *  Check, then reject or accept the choice for a new delay interval
     *  @param check - verify or ignore changes
     *  @see #solveTimer
     */
    private void confirm( boolean check )
    {
      if( check )
      {
        solveTimer.setDelay( solveDelay_msec = convertIndexToMsec() );
        
        logger.info( "New Delay == " + solveDelay_msec + " msec" );
      }
      
      dispose();
      
    }// SolveDelayFrame.confirm()
    
    /** Get the solve delay in msec from the combo box selection index
     *  @return delay in msec corresponding to selected index  */
    private int convertIndexToMsec()
    {
      int $result = 4 ;
      switch( nSelection )
      {
        case  0: $result =  1 ; break ;
        case  1: $result =  2 ; break ;
        case  2: $result =  4 ; break ;
        case  3: $result =  8 ; break ;
        case  4: $result = 16 ; break ;
        case  5: $result = 24 ; break ;
        case  6: $result = 32 ; break ;
        case  7: $result = 48 ; break ;
        case  8: $result = 64 ; break ;
        case  9: $result = 96 ; 
      }
      
      return( $result * 1000 );
      
    }// SolveDelayFrame.convertIndexToMsec()
    
    /** Get the selection index from the given parameter in msec
     *  @param msec - value to convert
     *  @return selection index  */
    private int convertMsecToIndex( int msec )
    {
      int $convert = msec/1000 ;
      int $result = 2 ;
      switch( $convert )
      {
        case  1: $result =  0 ; break ;
        case  2: $result =  1 ; break ;
        case  4: $result =  2 ; break ;
        case  8: $result =  3 ; break ;
        case 16: $result =  4 ; break ;
        case 24: $result =  5 ; break ;
        case 32: $result =  6 ; break ;
        case 48: $result =  7 ; break ;
        case 64: $result =  8 ; break ;
        case 96: $result =  9 ; 
      }
      
      return $result ;
      
    }// SolveDelayFrame.convertIndexToMsec()
    
  }/* INNER CLASS SolveDelayFrame */
  
 /*
  *            C O N S T R U C T O R S
  *************************************************************************************************************/
  
  /**
   *  USUAL constructor
   *  
   *  @param logLevel - initial Logging {@link Level} of this session
    * @param debugMode - enable debug actions
   */
  public Launcher( String logLevel, boolean debugMode )
  {
    DEBUG = debugMode ;
    System.out.println( "\t\t >> " + (DEBUG ? "In" : "NOT in") + " DEBUG mode." );
    
    System.out.println( "Code source location: " + Launcher.class.getProtectionDomain().getCodeSource().getLocation() );
    
    // init logging
    logControl = new LogControl( logLevel );
    
    if( logControl != null )
      logger = logControl.getLogger();
    
    if( logger == null )
      exitInit( "Constructor", "PROBLEM WITH LOGGER!" );
    
    initGameSize();
    
    initWindow();
    
  }// Launcher CONSTRUCTOR

 /*
  *            M E T H O D S
  *************************************************************************************************************/
  
 /* ======================================================================================================== 
                        #  M A I N  #
    ======================================================================================================== */
   
  /**
   *  @param args - the command line arguments 
   */
  public static void main( final String args[] )
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
          
          new Launcher( (args.length > 0 ? args[0] : null), (args.length > 1 ? true : false) ).go();
        }
      }
    ); 
  }
  
 // ===========================================================================================================
 //                               I N T E R F A C E
 // ===========================================================================================================
  
  /**
   *  See if the {@link Loader} thread has returned <br>
   *  
   *  @return done or not
   *  
   *  @see #createLoadWorker
   *  @see #gamesLoaded
   */
  boolean loadWorkerDone()
  {
    if( gamesLoaded )
      return true ;
    
    JOptionPane.showMessageDialog( this, strLoadDelay, strSolveDelay, JOptionPane.INFORMATION_MESSAGE );
    return false ;
    
  }// Launcher.loadWorkerDone()
  
  /**
   * Toggle the {@link #solveTimer} to send SOLVE events <br>
   * - invoked by the <b>Solve</b> Button or Hot Key <br>
   * - also called by {@link Grid#solveOneSqr} when NO more solving is possible
   * 
   * @see GameListener#actionPerformed
   * @see #setGridKeyMap
   */
  void toggleAutoSolve()
  {
    // user must have entered at least 1 value to start Solving
    if( !( autoSolveActive || DEBUG || grid.hasEntries() ) )
      return ;
    
    // do NOT accept any conflicts
    if( grid.hasConflicts() )
    {
      JOptionPane.showMessageDialog( this, strREMOVE_CONFLICTS_SOLVE, strConflicts + '!', JOptionPane.INFORMATION_MESSAGE );
      if( ! showConflicts )
        setConflicts( true );
      
      if( autoSolveActive )
        solveTimer.stop();
      
      return ;
    }
    
    solveButton.setText( autoSolveActive ? strSolve : strStopSolve );
    autoSolveActive = ! autoSolveActive ;
    
    logger.info( (autoSolveActive ? strSolve : strStopSolve) + "\n" );
    
    if( autoSolveActive )
      solveTimer.start();
    else
        solveTimer.stop();
    
  }// Launcher.toggleAutoSolve()
  
  /**
   *  Game has been filled in correctly <br>
   *  - Called ONLY by {@link Grid#newValue}
   */
  void gameOver()
  {
    logger.info( strSuccess + '!' );
    
    enableAutoSolve( autoSolveActive = false );
    
    updateInfoMesg( htmlINFO_SOLVED );
    clock.stop();
    solveTimer.stop();
    
    conflictsButton.setEnabled( false );
    hintButton.setEnabled( false );
    undoButton.setEnabled( false );
    
    running = false ;
    
  }// Launcher.gameOver()
  
  /**
   *  Set the game to update the count, OR change the show/hide status, of 'conflicts' <br>
   *  - i.e. the presence in the {@link Grid} of one or more conflicting {@link Square}s <br>
   *  
   *  @param flip - indicate if need to toggle the 'show conflicts' state
   *  
   *  @see Grid#hasConflicts
   *  @see Square#adjustConflict
   */
  void setConflicts( boolean flip )
  {
    if( ! running )
      return ;
    
    if( flip )
      showConflicts = !showConflicts ;
    
    // calling 'infoMesg.setForeground( SqrTypes.BAD.getColor() )' is just ignored...
    // so the easiest way to change the display color for conflicting values is to use Strings 
    // with embedded HTML, which JLabel supports
    conflictDisplay = showConflicts ? ( grid.hasConflicts() ? htmlCONFLICT_VALUES : htmlNO_CONFLICTS ) : strINFO_RUNNING ;
    
    updateInfoMesg( conflictDisplay );
    
    conflictsButton.setText( showConflicts ? strHideConflicts : strShowConflicts );
    
    repaint();
    
    logger.info( conflictDisplay + " ; flip == " + flip );
  
  }// Launcher.setConflicts()
  
  /**
   * Increment/decrement # of Solved actions
   * 
   * @param inc - amount to increment, +ve or -ve
   * @return updated {@link #solveCount}
   */
  int incSolveCount( final int inc )
  {
    solveCount += inc ;
    if( solveCount < 0 )
    {
      logger.warning( "Attempt to decrement solveCount to LT zero!" );
      solveCount = 0 ;
    }
    
    return solveCount ;
    
  }// Launcher.incSolveCount()
  
  /**
   * Is the game active?
   * 
   * @return {@link #running}
   */
  boolean isRunning() { return running; }
  
  /**
   * Is the 'Add game' frame active?
   * 
   * @return {@link #addingGame}
   */
  boolean isAddingGame() { return addingGame; }
  
  /**
   * Is the 'Solve game' function active?
   * 
   * @return {@link #autoSolveActive}
   */
  boolean autoSolveActivated() { return autoSolveActive; }
  
  /**
   * Are we indicating if any <em>conflicting</em> values are present in the grid?
   * 
   * @return {@link #showConflicts}
   */
  boolean showingConflicts() { return showConflicts; }
  
  /** @return {@link #pxSqrLength}  */
  int getSqrLength() { return pxSqrLength; }
  
  /** @return {@link #solveDelay_msec}  */
  int getSolveDelay() { return solveDelay_msec; }
  
  /** @return {@link #currentGameName}  */
  String getGameName() { return currentGameName; }
  
  /** @return {@link #difficulty}  */
  int getDifficulty() { return difficulty; }
  
  /**
   *  @param diff - new difficulty level
   * 
   *  @return success or failure 
   */
  boolean setDifficulty( final int diff )
  {
    if( (diff > Loader.nFAIL) && (diff < Loader.NUM_DIFFICULTIES) )
    {
      difficulty = diff ;
      return true ;
    }
    
    logger.warning( "Tried to set improper difficulty level: " + diff );
    return false ;
    
  }// Launcher.setDifficulty()
  
  /**
   * Enable/Disable the Undo button
   * 
   * @param yes - enable if true, disable if false
   */
  void enableUndo( final boolean yes )
  {
    if( undoButton.isEnabled() != yes )
      undoButton.setEnabled( yes );
  }
  
  /**
   * Enable/Disable the Redo button
   * 
   * @param yes - enable if true, disable if false
   */
  void enableRedo( final boolean yes )
  {
    if( redoButton.isEnabled() != yes )
      redoButton.setEnabled( yes );
  }
  
  /**
   * Enable/Disable the Solve button
   * 
   * @param yes - enable if true, disable if false
   */
  void enableAutoSolve( final boolean yes )
  {
    if( solveButton.isEnabled() != yes )
      solveButton.setEnabled( yes );
  }
  
  /**
   * Update the number of unknown {@link Square}s that remain
   * 
   * @param emp - number of remaining empty {@link Square}s
   * @see Grid#incBlankCount(int)
   */
  void updateSqrsMesg( final int emp )
  {
    logger.fine( "parameter = " + emp );
    sqrsLabel.setText( "<html><font color=red><b>" + Integer.toString(emp) + "</b></font>" + strSQUARES_TITLE + "</html>" );
  }
  
  /**
   * Update the time display in {@link #timeLabel}
   * 
   * @param str - <CODE>String</CODE> with the updated time value
   */
  void updateTime( final String str ) { timeLabel.setText( strTIME_TITLE + str ); }
  
  /**
   * Text to display in {@link #infoTitle}
   * 
   * @param str - <CODE>String</CODE> with updated info
   */
  void updateInfoTitle( final String str ) { infoTitle.setText( str ); }
  
  /**
   * Text to display in {@link #infoMesg}
   * 
   * @param str - <CODE>String</CODE> with updated info
   */
  void updateInfoMesg( final String str ) { infoMesg.setText( str ); }
  
 // end INTERFACE
 // ===========================================================================================================
 //                          I N I T I A L I Z A T I O N
 // ===========================================================================================================
  
  /**
   *  Query the awt toolkit to get screen parameters and set the dimensions of the different Game size options <br>
   *  - called by {@link #Launcher(String, boolean)}
   *  
   *  @see java.awt.Toolkit#getDefaultToolkit
   *  @see java.awt.Window#setSize(Dimension)
   */
  private void initGameSize()
  {
    // check the user's screen size
    pxScreenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    int res = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
    
    logger.logInit( "Screen width = " + pxScreenSize.width 
                    + " & height = " + pxScreenSize.height + " & Resolution = " + res );
    
    // User game starts with the smallest size
    pxSqrLength = ( DEBUG ? SQUARE_SIZE_MD : SQUARE_SIZE_SM );
    
    // make sure the msg frames are not wider than the user's screen
    pxMsgFrameWidth = Math.min( pxScreenSize.width, (SQUARE_SIZE_SM * INITIAL_GRID_LENGTH) + (X_BORDER * 2) );
    
    pxGameSize = new Dimension( (pxSqrLength * INITIAL_GRID_LENGTH) + (X_BORDER * 2),
                                (pxSqrLength * INITIAL_GRID_LENGTH) + (PANEL_HEIGHT * NUM_MAIN_PANELS) + (Y_BORDER * 2) );
    
    setSize( pxGameSize );
    
    logger.logInit( "Initial Game width = " + pxGameSize.width 
                     + " & height = " + pxGameSize.height + " & msg Frame width = " + pxMsgFrameWidth );
    
  }// Launcher.initGameSize()
    
  /**
   *  Set Window properties and get access to {@link #myContentPane} <br>
   *  - called by {@link #Launcher(String, boolean)}
   *  
   *  @see javax.swing.JFrame#getContentPane
   *  @see javax.swing.JFrame#setDefaultCloseOperation(int)
   *  @see java.awt.Component#setFocusable(boolean)
   *  @see java.awt.Frame#setResizable(boolean)
   */
  private void initWindow()
  {
    myContentPane = getContentPane();
    myContentPane.setBackground( COLOR_GAME_BKGRND );
    
    setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    
    initComponents();
    initActions();
    
    setName( GAME_NAME );
    setTitle( GAME_VERSION );
    
    setFocusable( true );
    setResizable( DEBUG );
    
  }// Launcher.initWindow()

  /**
   *  Init components and add the top level panels to {@link #myContentPane} <br>
   *  - called by {@link #initWindow}
   *  
   *  @see java.awt.Container#add(java.awt.Component, Object)
   */
  private final void initComponents()
  {
    logger.logInit();
    
    // MENU BAR
    initMenuBar();
    
    pxPanelSize = new Dimension( pxMsgFrameWidth, PANEL_HEIGHT );
    
    // TOP ENCLOSING PANEL
    initTopEnclosingPanel();
    
    // BOTTOM BUTTON PANEL
    initBotButtonPanel();
    
    // add top and bottom panels to content pane using default Border Layout
    myContentPane.add( topEnclosingPanel, "North" );
    myContentPane.add( botBtnPanel,       "South" );
    // Grid will go in "Center"
    
  }// Launcher.initComponents()

  /**
   *  Init Menu Bar & call Menu init methods <br>
   *  - called by {@link #initComponents}
   *  
   *  @see #initGameMenu
   *  @see #initSettingsMenu
   *  @see #initHelpMenu
   */
  private final void initMenuBar()
  {
    logger.logInit();
    
    initGameMenu();
    
    initSettingsMenu();
    
    initHelpMenu();
    
    frameMenuBar = new JMenuBar();
    
    frameMenuBar.add( gameMenu     );
    frameMenuBar.add( settingsMenu );
    frameMenuBar.add( helpMenu     );
    
    setJMenuBar( frameMenuBar );
    
  }// Launcher.initMenuBar()
  
  /**
   *  Init Game Menu & menu items <br>
   *  - called by {@link #initMenuBar}
   */
  private final void initGameMenu()
  {
    logger.logInit();
    
       newGameMenuItem = new JMenuItem( strNewGame    );
    selectGameMenuItem = new JMenuItem( strSelectGame );
       addGameMenuItem = new JMenuItem( strAddGame    );
          exitMenuItem = new JMenuItem( strExit       );
    
    gameMenu = new JMenu( strGAME );
    
    gameMenu.add(  newGameMenuItem   );
    gameMenu.add( selectGameMenuItem );
    gameMenu.add(  addGameMenuItem   );
    gameMenu.add(     exitMenuItem   );
    
  }// Launcher.initGameMenu()

  /**
   *  Init Settings Menu & menu items <br>
   *  - called by {@link #initMenuBar}
   */
  private final void initSettingsMenu()
  {
    logger.logInit();
    
          sizeMenuItem = new JMenuItem( strSize       );
    solveDelayMenuItem = new JMenuItem( strSolveDelay );
    difficultyMenuItem = new JMenuItem( strDifficulty );
    
    settingsMenu = new JMenu( strSettings );
    
    settingsMenu.add( sizeMenuItem       );
    settingsMenu.add( difficultyMenuItem );
    settingsMenu.add( solveDelayMenuItem );
    
  }// Launcher.initSettingsMenu()

  /**
   *  Init Help Menu & menu items <br>
   *  - called by {@link #initMenuBar}
   */
  private final void initHelpMenu()
  {
    logger.logInit();
    
    revealWrongMenuItem = new JMenuItem( strRevealWrong );
       instructMenuItem = new JMenuItem( strInstruct  );
          aboutMenuItem = new JMenuItem( strAbout     );
    
    helpMenu = new JMenu( strHelp );
    
    helpMenu.add( revealWrongMenuItem );
    helpMenu.add( instructMenuItem  );
    helpMenu.add( aboutMenuItem     );
    
  }// Launcher.initHelpMenu()

  /**
   *  Init Top Enclosing Panel & call sub-Panel init methods <br>
   *  - called by {@link #initComponents}
   *  
   *  @see #initMessagePanel
   *  @see #initTopButtonPanel
   */
  private final void initTopEnclosingPanel()
  {
    logger.logInit();
    
    // MESSAGE PANEL
    initMessagePanel();
    
    // TOP BUTTON PANEL
    initTopButtonPanel();
    
    topEnclosingPanel = new JPanel();
    topEnclosingPanel.setLayout( new GridLayout(2,1) );
    
    topEnclosingPanel.add( mesgPanel   );
    topEnclosingPanel.add( topBtnPanel );
    
  }// Launcher.initTopEnclosingPanel()
  
  /**
   *  Init Message Panel & contained panels and labels <br>
   *  - called by {@link #initTopEnclosingPanel}
   *  
   *  @see #mesgPanel
   *  @see #infoPanel
   */
  private final void initMessagePanel()
  {
    logger.logInit();
    
    // MESSAGE PANEL - contains sqrsLabel, infoPanel & timeLabel
    sqrsLabel = new JLabel();
    sqrsLabel.setHorizontalAlignment( SwingConstants.CENTER );
    
    // INFO PANEL - contains infoTitle & infoMesg
    infoTitle = new JLabel();
    infoTitle.setHorizontalAlignment( SwingConstants.CENTER );
    
    infoMesg = new JLabel();
    infoMesg.setHorizontalAlignment( SwingConstants.CENTER );
    
    infoPanel = new JPanel();
    infoPanel.setBackground( COLOR_GAME_BKGRND );
    infoPanel.setLayout( new GridLayout( 2, 1, 1, 2 ) );
    
    infoPanel.add( infoTitle );
    infoPanel.add( infoMesg );
    
    timeLabel = new JLabel();
    timeLabel.setHorizontalAlignment( SwingConstants.CENTER );
    
    mesgPanel = new JPanel();
    mesgPanel.setBackground( COLOR_GAME_BKGRND );
    mesgPanel.setPreferredSize( pxPanelSize );
    mesgPanel.setLayout( new GridLayout( 1, 5, 2, 1 ) );
    
    mesgPanel.add( sqrsLabel );
    mesgPanel.add( infoPanel );
    mesgPanel.add( timeLabel );
    
    updateSqrsMesg( 0 );
    updateInfoTitle( GAME_NAME );
    updateInfoMesg( strINFO_READY );
    updateTime( strZERO_TIME );
    
  }// Launcher.initMessagePanel()
  
  /**
   *  Init Top Button Panel & contained buttons <br>
   *  - called by {@link #initTopEnclosingPanel}
   */
  private final void initTopButtonPanel()
  {
    logger.logInit();
    
    newGameButton = new JButton( strNewGame );
    newGameButton.setToolTipText( strF2tooltip );
    
    conflictsButton = new JButton( strShowConflicts );
    conflictsButton.setToolTipText( strF4tooltip );
    
    hintButton = new JButton( strHint );
    
    topBtnPanel = new JPanel();
    topBtnPanel.setBackground( COLOR_GAME_BKGRND );
    topBtnPanel.setPreferredSize( pxPanelSize );
    topBtnPanel.setLayout( new GridLayout( 1, 5, 20, 2 ) );
    
    topBtnPanel.add( newGameButton   );
    topBtnPanel.add( conflictsButton );
    topBtnPanel.add( hintButton      );

  }// Launcher.initTopButtonPanel()
  
  /**
   *  Init Bottom Button Panel & contained buttons <br>
   *  - called by {@link #initComponents}
   */
  private final void initBotButtonPanel()
  {
    logger.logInit();
    
    undoButton = new JButton( strUndo );
    undoButton.setToolTipText( strF7tooltip );
    
    redoButton = new JButton( strRedo );
    redoButton.setToolTipText( strF8tooltip );
    
    solveButton = new JButton( strSolve );
    solveButton.setToolTipText( strF9tooltip );
    
    botBtnPanel = new JPanel();
    botBtnPanel.setBackground( COLOR_GAME_BKGRND );
    botBtnPanel.setPreferredSize( pxPanelSize );
    botBtnPanel.setLayout( new GridLayout( 1, 5, 20, 2 ) );
    
    botBtnPanel.add( undoButton  );
    botBtnPanel.add( redoButton  );
    botBtnPanel.add( solveButton );
    
  }// Launcher.initBotButtonPanel()

  /**
   *  FIRST get a game {@link #listener} <br>
   *  activate Buttons, {@link #clock} and {@link #solveTimer} <br>
   *  activate Menus <br>
   *  - called by {@link #initWindow}
   *  
   *  @see java.awt.event.ActionListener
   *  @see javax.swing.Timer
   */
  private final void initActions()
  {
    logger.logInit();
    
    listener = new GameListener();
    
    initButtonActions();
    
    // clock will send an event to listener every (clockDelaySeconds*1000) msec
    clock = new Timer( clockDelaySeconds*1000, listener );
    
    // solve timer will send an event to listener every solveDelay_msec
    solveDelay_msec = ( DEBUG ? DEBUG_SOLVE_DELAY_MSEC : USER_SOLVE_DELAY_MSEC );
    solveTimer = new Timer( solveDelay_msec, listener );
    solveTimer.setInitialDelay( INITIAL_SOLVE_DELAY_MSEC );
    
    initMenuActions();
    
  }// Launcher.initActions()
  
  /**
   *  initialize top and bottom Buttons <br>
   *  - called by {@link #initActions}
   */
  private void initButtonActions()
  {
    // top buttons
      newGameButton.addActionListener( listener );
    conflictsButton.addActionListener( listener );
         hintButton.addActionListener( listener );
    
    // bottom buttons
     undoButton.addActionListener( listener );
     redoButton.addActionListener( listener );
    solveButton.addActionListener( listener );
    
    // NOT ENABLED UNTIL A GAME IS LOADED
    conflictsButton.setEnabled( false );
         hintButton.setEnabled( false );
         undoButton.setEnabled( false );
         redoButton.setEnabled( false );
        solveButton.setEnabled( false );
        
  }// Launcher.initButtonActions()
  
  /* See setGridKeyMap() for Grid Key Events */
  /**
   *  Set Menu mnemonics and accelerators <br>
   *  - called by {@link #initActions}
   *  
   *  @see #setGridKeyMap
   */
  private final void initMenuActions()
  {
    logger.logInit();
    
    initGameMenuActions();
    
    initSettingsMenuActions();
    
    initHelpMenuActions();
    
  }// Launcher.initMenuActions()
  
  /**
   *  Set Game Menu mnemonics and accelerators <br>
   *  - called by {@link #initMenuActions}
   */
  private final void initGameMenuActions()
  {
    gameMenu.setMnemonic( strGAME.charAt(0) );
    
    newGameMenuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0) );
    newGameMenuItem.setMnemonic( strNewGame.charAt(0) );
    newGameMenuItem.addActionListener( listener );
    
    selectGameMenuItem.setMnemonic( strSelectGame.charAt(0) );
    selectGameMenuItem.addActionListener( listener );
    
    addGameMenuItem.setMnemonic( strAddGame.charAt(0) );
    addGameMenuItem.addActionListener( listener );
    
    exitMenuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_MASK) );
    exitMenuItem.setMnemonic( KeyEvent.VK_X ); 
    exitMenuItem.addActionListener( listener );
    
  }// Launcher.initGameMenuActions()
  
  /**
   *  Set Settings Menu mnemonics and accelerators <br>
   *  - called by {@link #initMenuActions}
   */
  private final void initSettingsMenuActions()
  {
    settingsMenu.setMnemonic( strSettings.charAt(0) );
    
    difficultyMenuItem.setMnemonic( strDifficulty.charAt(0) ); 
    difficultyMenuItem.addActionListener( listener );
    
    solveDelayMenuItem.setMnemonic( strSolveDelay.charAt(10) );
    solveDelayMenuItem.addActionListener( listener );
    
    sizeMenuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.ALT_MASK) );
    sizeMenuItem.setMnemonic( KeyEvent.VK_Z );
    sizeMenuItem.addActionListener( listener );
    
  }// Launcher.initSettingsMenuActions()
  
  /**
   *  Set Help Menu mnemonics and accelerators <br>
   *  - called by {@link #initMenuActions}
   */
  private final void initHelpMenuActions()
  {
    helpMenu.setMnemonic( strHelp.charAt(0) );
    
    revealWrongMenuItem.setMnemonic( strRevealWrong.charAt(0) );
    revealWrongMenuItem.addActionListener( listener );
    
    // NOT ENABLED UNTIL A GAME IS LOADED
    revealWrongMenuItem.setEnabled( false );
    
    instructMenuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0) );
    instructMenuItem.setMnemonic( strInstruct.charAt(0) );
    instructMenuItem.addActionListener( listener );
    
    aboutMenuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0) );
    aboutMenuItem.setMnemonic( strAbout.charAt(0) );
    aboutMenuItem.addActionListener( listener );
    
  }// Launcher.initHelpMenuActions()
  
  /**
   *  Enable {@link Grid}, {@link Loader} &amp; <code>Inner Class Frames</code>,
   *  then place the game on screen <br>
   *  - called by {@link #main}
   *  
   *  @see java.awt.Window#setVisible(boolean)
   *  @see #createGrid
   *  @see #createLoader
   *  @see #createInnerClasses
   */
  private final void go()
  {
    logger.logInit();
    
    // do this here to avoid possible thread problems
    setVisible( true );
    
    /* 1. MUST add the Grid AFTER Game is visible to get the size of the Content Pane properly */
    if( ! createGrid() )
      exitInit( "go()", "PROBLEM WITH GRID!" );
    
    /* 2. MUST create the Loader AFTER the Grid has been created */
    if( ! createLoader() )
      exitInit( "go()", "PROBLEM WITH LOADER!" );
    
    /* 3. MUST construct the Inner Classes AFTER the Loader is present */
    createInnerClasses();
    
    // place the game on the screen
    if( DEBUG )
      setLocation( pxScreenSize.width - pxGameSize.width - 160, 80 );
    else
        setLocationRelativeTo( null ); // centered
    
  }// Launcher.go()
  
  /**
   *  Insert the {@link Grid} into {@link #myContentPane} <br>
   *  - called by {@link #go}
   *  
   *  @return success or failure
   *  
   *  @see javax.swing.JFrame#getContentPane()
   *  @see #myLayeredPane
   *  @see javax.swing.JLayeredPane
   */
  private final boolean createGrid()
  {
    logger.logInit();
    
    myLayeredPane = new JLayeredPane();
    
    grid = new Grid( this );
    
    if( grid == null )
    {
      logger.severe( "Grid not constructed!" );
      return false ;
    }
    
    // grid will not position properly if don't use a JLayeredPane
    myLayeredPane.add( grid, JLayeredPane.DEFAULT_LAYER );
    
    // put the grid/pane in the center of the BorderLayout of the Content Pane
    myContentPane.add( myLayeredPane, "Center" );
    
    setGridKeyMap();
    
    return true ;
  
  }// Launcher.createGrid()
  
  /**
   *  Create a {@link #loader} and start the {@link #loadWorker} thread <br>
   *  - called by {@link #go}
   *  
   *  @return success or failure
   *  @see #createLoadWorker
   */
  private final boolean createLoader()
  {
    logger.logInit();
    
    loader = new Loader( this, MAX_NUM_LOADED_GAMES );
    
    if( loader == null )
    {
      logger.warning( "Loader not constructed!" );
      return false ;
    }
    
    // in case there are many games, load them in a separate thread 
    createLoadWorker();
    loadWorker.execute();
    
    return true ;
  
  }// Launcher.createLoader()
  
  /**
   *  Call {@link Loader} to load games from file or jar to {@link Loader.SavedGame}s <br>
   *  - called by {@link #createLoader}
   *  
   *  @see Loader#loadAllGames
   *  @see #loadWorker
   *  @see #loadWorkerDone
   */
  private final void createLoadWorker()
  {
    logger.logInit();
    
    /* load saved games  */
    loadWorker = new SwingWorker<Integer, Void>()
    {
      @Override
      public Integer doInBackground()
      {
        return Integer.valueOf(
          loader.loadAllGames()
          //loader.loadFiles( "..\\..\\..\\Resources\\games\\Pseudokeu\\testGames", "test" )
          /*/
          loader.loadFile( "..\\..\\..\\Resources\\games\\Pseudokeu\\bookGames",
                           "hard2-Ta36-p225.psk" )
          //*/
        );
      }
      
      @Override
      public void done()
      {
        gamesLoaded = true ;
      }
      
    };// new SwingWorker
    
  }// Launcher.createLoadWorker()
  
  /**
   *  Create the Inner Class Frames <br>
   *  - called by {@link #go}
   *  
   *  @see AddGameFrame
   *  @see GameSizeFrame
   *  @see SelectGameFrame
   *  @see SolveDelayFrame
   *  @see ChooseDifficultyFrame
   */
  private final void createInnerClasses()
  {
    logger.logInit();
    
        selectFrame = new SelectGameFrame();
           addFrame = new AddGameFrame();
          sizeFrame = new GameSizeFrame();
         delayFrame = new SolveDelayFrame();
    difficultyFrame = new ChooseDifficultyFrame();
  
  }// Launcher.createInnerClasses()
  
  /* See initMenuActions() for Launcher Key Events */
  /**
   *  Activate {@link Grid} keystroke actions. <br>
   *  - called by {@link #createGrid} <br>
   *  <ul>
   *    <li>F4 to show/hide <b>Conflicts</b>
   *    <li>F7 to UNDO the most recent <b>value assignment</b>
   *    <li>F8 to REDO the most recent <b>UNDO action</b>
   *    <li>F9 to SOLVE the game
   *    <li>Ctrl-L to INCREASE the amount of <b>logging</b> - or wrap around to LOWEST amount
   *    <li>Ctrl-Shft-L to DECREASE the amount of <b>logging</b> - or wrap around to HIGHEST amount
   *  </ul>
   *  
   *  @see #setGridDebugKeyMap
   *  @see #initMenuActions
   */
  @SuppressWarnings("serial")
  private void setGridKeyMap()
  {
    logger.logInit();
    
     InputMap $gImap = grid.getInputMap();
    ActionMap $gAmap = grid.getActionMap();
    
    // F4 = show/hide Conflicts
    $gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_F4, 0 ), strShowConflicts );
    $gAmap.put( strShowConflicts, new AbstractAction()
      { public void actionPerformed( ActionEvent aevt ) { setConflicts( true ); } } );
    
    // F7 = Undo last entry
    $gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_F7, 0 ), strUndo );
    $gAmap.put( strUndo, new AbstractAction()
      { public void actionPerformed( ActionEvent aevt ) { undoLastEntry(); } } );
    
    // F8 = Redo last Undo
    $gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_F8, 0 ), strRedo );
    $gAmap.put( strRedo, new AbstractAction()
      { public void actionPerformed( ActionEvent aevt ) { redoLastUndoAction(); } } );
    
    // F9 = Solve game
    $gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_F9, 0 ), strSolve );
    $gAmap.put( strSolve, new AbstractAction()
      { public void actionPerformed( ActionEvent aevt ) { toggleAutoSolve(); } } );
    
    // Ctrl-L = INCREASE Application Logging amount
    $gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK ), strMoreLogging );
    $gAmap.put( strMoreLogging, new AbstractAction()
      { public void actionPerformed( ActionEvent ae ) { logControl.changeLogging( true ); } } );
    
    // Ctrl-Shft-L = DECREASE Application Logging amount
    $gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK ),
               strLessLogging );
    $gAmap.put( strLessLogging, new AbstractAction()
      { public void actionPerformed( ActionEvent ae ) { logControl.changeLogging( false ); } } );
    
    setGridDebugKeyMap( $gImap, $gAmap );
  
  }// Launcher.setGridKeyMap()
  
  /**
   *  <b>Termination</b> because of <b>ERRORS</b> during <i>initialization</i> <br>
   *  
   *  @param source - calling method name
   *  @param msg - extra info to display
   *  
   *  @see #Launcher(String,boolean)
   *  @see #go
   */
  private static void exitInit( String source, String msg )
  {
    System.err.println( source + ": " + msg );
    
    try // pause briefly to allow any logging to finish
    {
      Thread.sleep( THREAD_SLEEP_MSEC );
    }
    catch( InterruptedException ie )
    {
      ie.printStackTrace();
    }
    finally
    {
      System.exit( MAX_NUM_LOADED_GAMES ); // just a convenient identifiable value...
    }
  
  }// Launcher.exitInit()
  
 // ===========================================================================================================
 //                              R U N N I N G
 // ===========================================================================================================
  
  /**
   *  <b>User wants to close the application</b> <br>
   *  <em>ONLY</em> Caller is <b>EXIT</b> action (via Button or Menu)
   *  
   *  @see GameListener#actionPerformed
   */
  private void halt()
  {
    clock.stop();
    running = false ;
    
    grid.removeMouseListener( grid );
    grid.removeKeyListener( grid );
    
    try // pause briefly to allow any active tasks to finish
    {
      Thread.sleep( THREAD_SLEEP_MSEC );
    }
    catch( InterruptedException ie )
    {
      ie.printStackTrace();
    }
    finally
    {
      // close any active sub-frames
      if( sizeFrame.isShowing()       )       sizeFrame.dispose();
      if( selectFrame.isShowing()     )     selectFrame.dispose();
      if( addFrame.isShowing()        )        addFrame.dispose();
      if( delayFrame.isShowing()      )      delayFrame.dispose();
      if( difficultyFrame.isShowing() ) difficultyFrame.dispose();
      
      System.out.println( "PROGRAM ENDED ON " + Thread.currentThread() );
      
      dispose();
    }
  
  }// Launcher.halt()
  
  /**
   *  Start a new game <br>
   *  - invoked by the <b>New Game</b> Button or Menu item &amp; {@link #selectFrame}
   *  
   *  @param index of the requested game, or {@link #RANDOM_GAME} for a random selection
   *  
   *  @see GameListener#actionPerformed
   *  @see Grid#activateGame
   */
  private void newGame( int index )
  {
    if( ! loadWorkerDone() )
      //TODO: inform user?
      return ;
    
    logger.info( "load game # " + index );
    
    if( running )
    {
      clock.stop();
      running = false ;
    }
    
    if( loader.getNumLoadedGames(difficulty) <= 0 )
    {
      updateInfoTitle( strProblem + ':' );
      updateInfoMesg( strNO_GAMES_LOADED );
      logger.warning( infoTitle.getText() + ' ' + infoMesg.getText() );
      return ;
    }
    
    reset();
    
    currentGameName = grid.activateGame( loader, difficulty, index );
    if( currentGameName == null )
    {
      updateInfoTitle( strProblem + ':' );
      updateInfoMesg( strNOT_ACTIVATE_GAME + index );
      logger.warning( infoTitle.getText() + ' ' + infoMesg.getText() );
      return ;
    }
    
    try
    {
      currentGameNum = Integer.parseInt( currentGameName.substring(loader.getFileName(difficulty).length()) );
    }
    catch( NumberFormatException nfe )
    {
      logger.warning( "Problem with currentGameNum: " + nfe.toString() );
      currentGameNum = 1 ;
    }
    
    // Conflicts & Hint buttons can now be enabled
    showConflicts = false ;
    conflictsButton.setEnabled( true );
    hintButton.setEnabled( true );
    
    // 'Reveal WRONG guess' menu item can now be enabled
    revealWrongMenuItem.setEnabled( true );
    
    updateInfoTitle( strLevel + ' ' + Loader.STR_DIFF_FOLDERS[difficulty] + " : " + strGAME + " #" + currentGameNum );
    updateInfoMesg( strINFO_RUNNING );
    logger.severe( "Loaded " + '\'' + currentGameName + '\'' );
    
    startClock();
    repaint();
  
  }// Launcher.newGame()
  
  /**
   * Tell the {@link Grid} to UNDO the most recent 'Set Value' action <br>
   * - invoked by the <b>Undo</b> Button or Hot Key
   * 
   * @see Grid#undoLastValue
   * @see GameListener#actionPerformed
   * @see #setGridKeyMap
   */
  private void undoLastEntry()
  {
    // turn OFF Solve if active
    if( autoSolveActive )
      toggleAutoSolve();
    
    grid.undoLastValue();
    repaint();
    
  }//Launcher.undoLastEntry()
  
  /**
   * Tell the {@link Grid} to REDO the most recent 'UNDO' action <br>
   * - invoked by the <b>Redo</b> Button or Hot Key
   * 
   * @see Grid#redoLastUndo
   * @see GameListener#actionPerformed
   * @see #setGridKeyMap
   */
  private void redoLastUndoAction()
  {
    if( grid.redoLastUndo() )
      repaint();
  }
  
  /**
   *  Tell the {@link Grid} to SOLVE a {@link Square} in the current game <br>
   *  - invoked by {@link GameListener#actionPerformed(ActionEvent)}
   */
  private void revealSquare()
  {                 // stop clock if game is minimized
    if( running && (getState() == NORMAL) )
    { 
      grid.solveOneSqr();
      logger.info( "Solve Count == " + (++solveCount) + "\n" );
    }
    
  }// Launcher.revealSquare()
  
  /**
   *  Implement the change after user has selected a new Game size <br>
   *  - called by {@link GameSizeFrame#confirm}
   *  
   *  @see #pxGameSize
   *  @see #sizeMenuItem
   *  @see #sizeFrame
   *  @see java.awt.Window#setSize
   */
  private void setGameSize()
  {
    // get the OLD dimensions of the Game Frame
    logger.appendln( "BEFORE setSize(): Game width = " + getWidth() + " & height = " + getHeight() );
    
    // calculate the new Game size
    pxGameSize.width  = ( pxSqrLength * grid.getLength() ) + ( X_BORDER * 2 );
    pxGameSize.height = ( pxSqrLength * grid.getLength() ) + ( PANEL_HEIGHT * NUM_MAIN_PANELS )+( Y_BORDER * 2 );
    logger.appendln( "CALCULATED New Game width = " + pxGameSize.width + " & height = " + pxGameSize.height );
    
    /* For some reason, Game will NOT resize programatically in non-DEBUG mode now (April 2014) 
     * UNLESS setResizable() is made true ... a difference with Java 7 ?? */
    setResizable( true );
    setSize( pxGameSize );
    // restore original condition of 'resizable'
    setResizable( DEBUG );
    
    // center the resized frame on the screen
    setLocationRelativeTo( null );
    
    // get the NEW dimensions of the Game Frame
    logger.append( "AFTER setSize(): Game width = " + getWidth() + " & height = " + getHeight() );
    
    logger.send( Level.INFO );
    
  }// Launcher.setGameSize()
  
  /**
   *  Actions needed so that user can define a new Game by adding values to a blank Grid <br>
   *  - <b>OR</b> clean-up after user has <em>finished</em> adding new Games
   *  
   *  @param start - set up or clean up
   *  @see #launchAddGameFrame
   */
  private void prepareToAddGame( final boolean start )
  {
    logger.info( "Current # of User Games = " + loader.getNumLoadedGames(Loader.USER) );
    
    clock.stop();
    
    reset();
    
    addingGame = start ;
    addGameMenuItem.setEnabled( ! start );
    
    running = start ;
    
    // Conflicts will be indicated when Adding
    showConflicts = start ;
    conflictsButton.setEnabled( ! start );
    
    // disabled while Adding
    newGameButton.setEnabled( ! start );
    
    updateInfoTitle( start ? strAddGame : GAME_NAME );
    updateInfoMesg( start ? strINFO_RUNNING : strINFO_READY );
    
    repaint();
    
  }// Launcher.prepareToAddGame()
  
  /**
   *  Check the values user has added to define a new Game, and accept if valid
   *  
   *  @see AddGameFrame#confirm
   *  @return game OK or NOT
   */
  private boolean verifyAddedGame()
  {
    String $msg ;
    int $numGames = loader.getNumLoadedGames( Loader.USER );
    
    logger.info( "Current # of User Games == " + $numGames );
    
    // do NOT accept any conflicts
    if( grid.hasConflicts() )
    {
      JOptionPane.showMessageDialog( this, strREMOVE_CONFLICTS_ADD, strConflicts + '!', JOptionPane.INFORMATION_MESSAGE );
      return false ;
    }
    
    // OK if Loader accepted the new game
    if( loader.addGame(grid) )
    {
      reset();
      
      $msg = strSUCCESSFULL_ADDGAME + String.valueOf( $numGames + 1 ) + ' ' + '!' ;
      JOptionPane.showMessageDialog( this, $msg, strSuccess, JOptionPane.INFORMATION_MESSAGE );
      
      return true ;
    }
    
    // Problem - perhaps NO write permission, etc
    JOptionPane.showMessageDialog( this, strPROBLEM_ADDGAME, strProblem + '!', JOptionPane.INFORMATION_MESSAGE );
    return false ;
    
  }// Launcher.verifyAddedGame()
  
  /**
   * Reset some key fields to default values <br>
   * 
   * @see #updateSqrsMesg
   * @see Grid#clear
   */
  private void reset()
  {
    updateSqrsMesg( grid.getTotalSqrs() );
    updateTime( strZERO_TIME );
    
    conflictsButton.setText( strShowConflicts );
    solveButton.setText( strSolve );
    
    // these buttons are NOT enabled until at least one value is entered
    undoButton.setEnabled( false );
    redoButton.setEnabled( false );
    solveButton.setEnabled( false );
    
    solveCount = 0 ;
    
    // Clear the Squares and Groups
    grid.clear();
    grid.repaint();
  
  }// Launcher.reset()
  
  /**
   * Launch a {@link GameSizeFrame} window <br>
   * - invoked by {@link #sizeMenuItem}
   * 
   * @see #sizeFrame
   * @see GameSizeFrame#actionPerformed
   */
  private void launchSetSizeFrame()
  {
    logger.info( getLocationOnScreen().toString() );
    
    sizeFrame.setLocation( getLocationOnScreen() );
    sizeFrame.setVisible( true );
  
  }// Launcher.launchSetSizeFrame()
  
  /**
   * Launch a {@link SelectGameFrame} window <br>
   * - allows user to select a particular {@link Loader.SavedGame} from those that are available <br>
   * - invoked by {@link #selectGameMenuItem}
   * 
   * @see #selectFrame
   * @see SelectGameFrame#actionPerformed
   */
  private void launchSelectGameFrame()
  {
    logger.info( getLocationOnScreen().toString() );
    
    // make sure Loader is not still in the worker thread
    if( ! loadWorkerDone() )
      return ;
    
    // get the number of loaded games for the current level of difficulty
    selectFrame.setSpinner();
    
    selectFrame.setLocation( getLocationOnScreen() );
    selectFrame.setVisible( true );
  
  }// Launcher.launchSelectGameFrame()
  
  /**
   * Launch a {@link ChooseDifficultyFrame} window <br>
   * - allows user to select a particular {@link Loader.SavedGame} level of difficulty from those available <br>
   * - invoked by {@link #difficultyMenuItem}
   * 
   * @see #difficultyFrame
   * @see SelectGameFrame#actionPerformed
   */
  private void launchChooseDifficultyFrame()
  {
    logger.info( getLocationOnScreen().toString() );
    
    // set the selection to the current level
    difficultyFrame.setComboBoxSelection( difficulty );
    
    difficultyFrame.setLocation( getLocationOnScreen() );
    difficultyFrame.setVisible( true );
  
  }// Launcher.launchChooseDifficultyFrame()
  
  /**
   * Launch an {@link AddGameFrame} window <br>
   * - allows user to ADD a game to the collection of Saved Games <br>
   * - invoked by {@link #addGameMenuItem}
   * 
   * @param start - launching OR closing an AddGameFrame
   * 
   * @see #addFrame
   * @see AddGameFrame#actionPerformed
   * @see Loader.SavedGame
   */
  private void launchAddGameFrame( final boolean start )
  {
    logger.info( (start ? "" : "Finished with ") + strAddGame );
    
    prepareToAddGame( start );
    
    if( start )
    {
      addFrame.setLocationRelativeTo( null );
      addFrame.setVisible( true );
    }
    else
        addFrame.dispose();
    
  }// Launcher.launchAddGameFrame()
  
  /**
   * Launch a {@link SolveDelayFrame} window <br>
   * - allows user to select the delay interval for revealing Solved {@link Square}s <br>
   * - invoked by {@link #solveDelayMenuItem}
   * 
   * @see #delayFrame
   * @see SelectGameFrame#actionPerformed
   */
  private void launchSolveDelayFrame()
  {
    logger.info( getLocationOnScreen().toString() );
    
    delayFrame.setLocation( getLocationOnScreen() );
    delayFrame.setVisible( true );
  
  }// Launcher.launchSolveDelayFrame()
  
  /**
   * Reveal an incorrect guess, if any, in the current game <br>
   * - invoked by {@link #revealWrongMenuItem}
   * 
   * @see Grid#findWrongGuess
   * @see JOptionPane#showMessageDialog
   */
  private void revealWrongGuess()
  {
    logger.fine( strRevealWrong );
    
    // should NOT still be finding a solution at this point...
    if( ! grid.solveWorkerDone() )
    {
      logger.warning( "Solution is NOT available!! ??" );
      JOptionPane.showMessageDialog( null, strSOLUTION_NOT_AVAIL, ":(", JOptionPane.INFORMATION_MESSAGE );
      return ;
    }
    
    // need at least one active guess
    if( ! grid.hasActiveEntries() )
    {
      logger.warning( "Need AT LEAST ONE user value" );
      JOptionPane.showMessageDialog( null, strNEED_USER_VALUE, ":o", JOptionPane.INFORMATION_MESSAGE );
      return ;
    }
    
    if( grid.findWrongGuess() )
      repaint();
    else
        JOptionPane.showMessageDialog( null, strALL_VALS_GOOD, ":)", JOptionPane.INFORMATION_MESSAGE );
    
  }// Launcher.revealWrongGuess()
  
  /**
   * Instructions on how to play this Pseudokeu Game <br>
   * - invoked by {@link #instructMenuItem}
   * 
   * @see JOptionPane#showMessageDialog
   */
  private void launchInstructionBox()
  {
    logger.info( strInstruct );
    
    JOptionPane.showMessageDialog( null, htmlINSTRUCT_CONTENT, strInstruct, JOptionPane.INFORMATION_MESSAGE );
    
  }// Launcher.launchInstructionBox()
  
  /**
   * Information about this Pseudokeu Game <br>
   * - invoked by {@link #aboutMenuItem}
   * 
   * @see JOptionPane#showMessageDialog
   */
  private void launchAboutBox()
  {
    logger.info( strAbout );
    
    JOptionPane.showMessageDialog( this, htmlABOUT_CONTENT, strAbout, JOptionPane.INFORMATION_MESSAGE );
    
  }// Launcher.launchAboutBox()
  
  /**
   *  Start {@link #clock} <br>
   *  Called ONLY by {@link #newGame}
   *  
   *  @see #initActions()
   */
  private void startClock()
  {
    logger.info( (new Date()).toString() );
    
    if( ! running )
    {
      running = true ;
      seconds = 0 ;
      clock.start();
    }
    
  }// Launcher.startClock()
  
  /**
   *  Update the elapsed time <br>
   *  - invoked by the action event generated by {@link #clock}
   *  
   *  @see Launcher.GameListener#actionPerformed
   */
  private void runClock()
  {                 // stop clock if game is minimized
    if( running && (getState() == NORMAL) )
    { 
      seconds += clockDelaySeconds ;
      
      hrs = seconds / 3600 ;
      minsecs = seconds % 3600 ;
      mins = minsecs / 60 ;
      secs = minsecs % 60 ;
      
      strHr  = ( hrs  < 10 ? "0" : "" ) + Integer.toString( hrs  );
      strMin = ( mins < 10 ? "0" : "" ) + Integer.toString( mins );
      strSec = ( secs < 10 ? "0" : "" ) + Integer.toString( secs );
      
      updateTime( strHr + ":" + strMin + ":" + strSec );
    }
  
  }// Launcher.runClock()
  
 // ===========================================================================================================
 //                              D E B U G
 // ===========================================================================================================
  
  String   myname() { return getClass().getSimpleName(); }
  String fullname() { return getClass().getName();       }
  
/*/ DEBUG ONLY - to see how often this gets called
  static int pi = 0 ;
  @Override
  public void paint( java.awt.Graphics g )
  {
    super.paint( g );
    logger.info( "Called " + (++pi) + " times" );
  }
//*/
  
  /**
   * Activate {@link Grid} DEBUG keystroke actions.<br>
   * - called by {@link #setGridKeyMap} <br>
   *  <ul>
   *    <li>Ctrl-A to display <b>ALL</b> Active items
   *    <li>Ctrl-S to display the Active {@link Square}, if it exists
   *    <li>Ctrl-Shft-S to display <b>ALL</b> <code>Squares</code>
   *    <li>Ctrl-R to display the Active {@link Row}
   *    <li>Ctrl-Shft-R to display <b>ALL</b> <CODE>Rows</CODE>
   *    <li>Ctrl-C to display the Active {@link Col}
   *    <li>Ctrl-Shft-C to display <b>ALL</b> <CODE>Cols</CODE>
   *    <li>Ctrl-Z to display the Active {@link Zone}
   *    <li>Ctrl-Shft-Z to display <b>ALL</b> <CODE>Zones</CODE>
   *    <li>Ctrl-G to display the Active {@link Group}s
   *    <li>Ctrl-Shft-G to display <b>ALL</b> <CODE>Groups</CODE>
   *    <li>Ctrl-F to display important <b>fields</b> of the {@link Grid}
   *    <li>Ctrl-V to run the <b>Locked Values</b> Solving algorithm
   *    <li>Ctrl-U to display the current game <b>SOLUTION</b>
   *    <li>Ctrl-P to set possible values as temp values in the {@link Grid}
   *    <li>Ctrl-Shft-P to clear ALL temp values in the {@link Grid}
   *    <li>Ctrl-K to toggle the display of <em>keystrokes</em>
   *    <li>Ctrl-M to toggle the display of <em>mouseclicks</em>
   *    <li>Ctrl-E to display info on the <em>principal active {@link Logger}s</em>
   *    <li>Ctrl-Shft-E to list the names of <b>ALL</b> <em>registered {@link Logger}s</em>
   *  </ul>
   *
   * @param gImap - {@link InputMap} for the {@link Grid}
   * @param gAmap - {@link ActionMap} for the {@link Grid}
   * 
   * @see InputMap#put
   * @see ActionMap#put
   */
  @SuppressWarnings("serial")
  void setGridDebugKeyMap( InputMap gImap, ActionMap gAmap )
  {
    if( DEBUG )
    {
      logger.logInit();
    
    // Ctrl-A = display ACTIVE Square AND Groups
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK ), strActive );
    gAmap.put(
      strActive, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.getActiveSqr().display( Level.SEVERE, LogControl.fine() ? Grid.FULL : Grid.BRIEF, strSquare );
          grid.displayActiveGroups( Level.SEVERE, LogControl.fine() ? Grid.FULL : Grid.BRIEF, strGroups ); } } );
    
    // Ctrl-S = display ACTIVE Square
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK ), strSquare );
    gAmap.put(
      strSquare, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.getActiveSqr().display( Level.SEVERE, LogControl.fine() ? Grid.FULL : Grid.BRIEF, strSquare ); } } );
    
    // Ctrl-Shft-S = display Active & Group Squares
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK ),
               strSquares );
    gAmap.put(
      strSquares, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.displayGrpSqrs( Level.SEVERE, strSquares ); } } );
    
    // Ctrl-R = display ACTIVE Row
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), strRow );
    gAmap.put(
      strRow, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.activeRow().display( Level.SEVERE, LogControl.fine() ? Grid.FULL : Grid.BRIEF, strRow ); } } );
    
    // Ctrl-Shft-R = display ALL Rows
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_R, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK ),
               strRows );
    gAmap.put(
      strRows, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.displayRows( Level.SEVERE, LogControl.fine() ? Grid.FULL : Grid.BRIEF, strRows ); } } );
    
    // Ctrl-C = display ACTIVE Col
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK ), strCol );
    gAmap.put(
      strCol, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.activeCol().display( Level.SEVERE, LogControl.fine() ? Grid.FULL : Grid.BRIEF, strCol ); } } );
    
    // Ctrl-Shft-C = display ALL Cols
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_C, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK ),
               strCols );
    gAmap.put(
      strCols, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.displayCols( Level.SEVERE, LogControl.fine() ? Grid.FULL : Grid.BRIEF, strCols ); } } );
    
    // Ctrl-Z = display ACTIVE Zone
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK ), strZone );
    gAmap.put(
      strZone, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.activeZone().display( Level.SEVERE, LogControl.fine() ? Grid.FULL : Grid.BRIEF, strZone ); } } );
    
    // Ctrl-Shft-Z = display ALL Zones
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_Z, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK ),
               strZones );
    gAmap.put(
      strZones, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.displayZones( Level.SEVERE, LogControl.fine() ? Grid.FULL : Grid.BRIEF, strZones ); } } );
    
    // Ctrl-G = display ACTIVE Groups
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK ), strGroups );
    gAmap.put(
      strGroups, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.displayActiveGroups( Level.SEVERE, LogControl.fine() ? Grid.FULL : Grid.BRIEF, strGroups ); } } );
    
    // Ctrl-Shft-G = display ALL Groups
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_G, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK ),
               strAllGroups );
    gAmap.put(
      strAllGroups, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.displayAllGroups( Level.SEVERE, LogControl.fine() ? Grid.FULL : Grid.BRIEF, strAllGroups ); } } );
    
    // Ctrl-F = display Grid Fields
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK ), strGrid );
    gAmap.put(
      strGrid, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.displayFields( Level.SEVERE, strGrid ); } } );
    
    // Ctrl-V = run 'Locked Values' Solving algorithm
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK ), strRunLockVals );
    gAmap.put(
      strRunLockVals, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.findLockedVals(); } } );
    
    // Ctrl-U = display the current game SOLUTION
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK ), strShowSolution );
    gAmap.put(
      strShowSolution, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.displaySolution(); } } );
    
    // Ctrl-P = set Square Possible values as TEMP VALUES
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK ), strPossValShow );
    gAmap.put(
      strPossValShow, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.setPossibleValsAsTempVals(); } } );
    
    // Ctrl-Shft-P = Clear ALL temp values
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_P, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK ),
               strClearTempVals );
    gAmap.put(
      strClearTempVals, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.clearAllTempVals(); } } );
    
    // Ctrl-K = display Keystrokes
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK ), strKeystrokes );
    gAmap.put(
      strKeystrokes, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.toggleKeystrokes(); } } );
    
    // Ctrl-M = display Mouseclicks
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK ), strMouseclicks );
    gAmap.put(
      strMouseclicks, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          grid.toggleMouseclicks(); } } );
    
    // Ctrl-E = display info on principal LoggErs
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK ), strMainLoggers );
    gAmap.put(
        strMainLoggers, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          LogControl.checkLogging(); } } );
    
    // Ctrl-Shft-E = print list of all rEgistered Loggers
    gImap.put( KeyStroke.getKeyStroke( KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK ),
               strRegLoggers );
    gAmap.put(
        strRegLoggers, new AbstractAction()
      { public void actionPerformed( ActionEvent ae )
        {
          LogControl.showLoggers(); } } );
    
    }// IF DEBUG MODE
    
  }// Launcher.setGridDebugKeystrokes()
  
 /*
  *            F I E L D S
  *************************************************************************************************************/
  
  /** game text and version info */
  static final String
                     strGAME = "Game" ,
                PROJECT_NAME = "Pseudokeu " ,
                   GAME_NAME = PROJECT_NAME + strGAME ,
                   
             // Start a new major number
             // OpenSUSE = 6, Ubuntu = 7 (Feb 2012), git = 8 (Mar 2014) 
             strMAJOR_VERSION = "8" ,
             strMINOR_VERSION = "1.2" ,
              strVERSION_NUM = strMAJOR_VERSION + "." + strMINOR_VERSION ,
                GAME_VERSION = PROJECT_NAME + "Version # " + strVERSION_NUM ;
  
  /** game default */
  static final int
                  MAX_NUM_LOADED_GAMES =  250 ,
                           RANDOM_GAME =   -1 ,
                     THREAD_SLEEP_MSEC =  666 ,
              INITIAL_SOLVE_DELAY_MSEC = 2000 ,
                DEBUG_SOLVE_DELAY_MSEC = 3000 ,
                 USER_SOLVE_DELAY_MSEC = 4000 ;
  
  /** component dimension */
  static final int
                  // in pixels
                  SQUARE_SIZE_SM =  48 ,
                  SQUARE_SIZE_MD =  60 , 
                  SQUARE_SIZE_LG =  72 , 
             
             INITIAL_GRID_LENGTH =   9 , // # of squares per side of the grid
                    PANEL_HEIGHT =  60 , // in pixels
                 NUM_MAIN_PANELS =   3 , // MesgPanel, TopBtnPanel, BotBtnPanel
             
             X_BORDER =  60 , // extra width in the frame so MesgPanel has enough space to display text properly
             Y_BORDER =  70 , // extra height so Grid is centered in the Content Pane
             
                    FONT_SIZE_XS = SQUARE_SIZE_SM/2 - 4 ,
                    FONT_SIZE_SM = SQUARE_SIZE_SM/2 + 2 ,
                    FONT_SIZE_MD = SQUARE_SIZE_MD/2 + 4 ,
                    FONT_SIZE_LG = SQUARE_SIZE_LG/2 + 4 ;
  
  /** default background {@link Color} */
  static final Color COLOR_GAME_BKGRND = new Color( 248, 232, 216 ); // beige
  
  /** message String */
  static final String
                     strZERO_TIME     = "00:00:00"  ,
                     strSQUARES_TITLE = "  Unknown Squares" ,
                     strTIME_TITLE    = "Time  "  ,
                     strINFO_READY    = "Ready to Go" ,
                     strINFO_RUNNING  = "Running..."  ,
               
    htmlINFO_SOLVED     = "<html><h2><font color=blue>SOLVED!</font></h2></html>" ,
    htmlCONFLICT_VALUES = "<html><font color=red><b>*** PROBLEM WITH VALUES ***</b></font></html>" ,
    htmlNO_CONFLICTS    = "<html><font color=green><b>NO conflicting values</b></font></html>" ,
    htmlINSTRUCT_CONTENT
    = "<html><ul>" +
        "<li><h3>Object of the game is to have a UNIQUE #1-9 in the nine Squares " +
          "of EVERY Zone (colored area), Row & Column.</h3></li>" +
        "<li><h3><font color=#131399>Use Left Mouse Button to click on a blank Square" +
          " and type in a value (1-9)</font></h3></li>" +
        "<li><h3><font color=#131399>To change a previous entry of yours, click on that Square" +
          " and type in a new value (or 0 to blank)</font></h3></li>" +
        "<li><h3><font color=#131399>Use the 'Show Conflicts' button or hot key [F4] " +
          "to indicate value 'clashes' </font></h3></li>" +
        "<li><h3><font color=#131399>Use the Undo/Redo buttons or hot keys [F7/F8] " +
          "to erase/restore entered values</font></h3></li>" +
        "<li><h3><font color=#131399>Hold the Alt key down while typing " +
          "to make temporary 'guesses' (up to 3 digits)</font></h3></li>" +
        "<li><h3><font color=#131399>Minimize the game window to Pause and stop the clock</font></h3></li>" +
        "<li><h3>The SOLVE button or hot key [F9] will reveal Square values, " + 
          "at a rate determined by the 'Solve Delay' setting.</h3></li>" +
          "<li><h3><em>Initial</em> SOLVE values <i>may</i> be INCORRECT if there are WRONG values present!</h3></li>" +
        "<li><h3><font color=red>Note: Some actions are NOT enabled UNLESS " +
          "a Square is selected!</font></h3></li>" +
      "</ul></html>",
    htmlABOUT_CONTENT = "<html><h2>Created by Mark Sattolo &copy; 2007</h2></html>" + "\n\n[ " + GAME_VERSION + " ]\n\n",
                 
                 strEVENT        =  " event" ,
                 strNewGame      =  "New " + strGAME ,
                 strSelectGame   =  "Select a " + strGAME + "..." ,
                 strAddGame      =  "Add a " + strGAME + "..." ,
                 strExit         =  "eXit"  ,
                 strLoadDelay    =  "Games have not finished loading -- Please wait a moment." ,
                 strSize         =  "Game Size..." ,
                 strDifficulty   =  "Difficulty Level..." ,
                 strSolveDelay   =  "Delay for Solve..." ,
                 strSettings     =  "Settings"  ,
                 strInstruct     =  "How to Play..."     ,
                 strAbout        =  "About " + GAME_NAME ,
                 strRevealWrong  =  "Reveal a WRONG value (if any)" ,
                 strHelp         =  "Help" ,
                 strExitCap      =  "EXIT" ,
                 strUndo         =  "Undo" ,
                 strRedo         =  "Redo" ,
                 strAccept       =  "Accept" ,
                 strCancel       =  "Cancel" ,
                 strConfirm      =  "Confirmation" ,
                 strConflicts    =  "Conflicts" ,
               strShowConflicts  =  "Show " + strConflicts ,
               strHideConflicts  =  "Hide " + strConflicts ,
                 strHint         =  "Hint" ,
                 strSolve        =  "Solve " + strGAME     ,
                 strStopSolve    =  "Stop Solving"         ,
                 strProblem      =  "PROBLEM"              ,
                 strSuccess      =  "SUCCESS"              ,
                 strLevel        =  "Level"                ,
                 
           strALL_VALS_GOOD      = "ALL values are good!" ,
           strNEED_USER_VALUE    = "Need AT LEAST ONE user value..." ,
           strSOLUTION_NOT_AVAIL = "Solution is not available yet." ,
           strNOT_ACTIVATE_GAME  = "COULD NOT ACTIVATE GAME #" ,
           strNO_GAMES_LOADED    = "NO GAMES LOADED!" ,
           strPROBLEM_ADDGAME    = "Problem with adding this game - please try again." ,
          strSUCCESSFULL_ADDGAME = "Successfully added game #" ,
         strREMOVE_CONFLICTS_ADD = "Remove the conflicting values before trying to add this game." ,
       strREMOVE_CONFLICTS_SOLVE = "Remove the conflicting values before trying to Solve!" ,
             
                 strActive       =  "Show Active Items"      ,
                 strSquare       =  "Show Active Square"     ,
                 strSquares      =  "Show Active & Group Squares"       ,
                 strGrid         =  "Show Grid Variables"    ,
                 strRow          =  "Show Current Row"       ,
                 strRows         =  "Show ALL Rows"          ,
                 strCol          =  "Show Current Col"       ,
                 strCols         =  "Show ALL Cols"          ,
                 strZone         =  "Show Current Zone"      ,
                 strZones        =  "Show ALL Zones"         ,
                 strGroups       =  "Show Active Groups"     ,
                 strAllGroups    =  "Show ALL Groups"        ,
                 strKeystrokes   =  "Toggle Keystrokes"      ,
                 strMouseclicks  =  "Toggle Mouseclicks"     ,
                 strMoreLogging  =  "Increase GAME Logging"  ,
                 strLessLogging  =  "Decrease GAME Logging"  ,
                 strMainLoggers  =  "Info on Main Loggers"   ,
                 strRegLoggers   =  "Show Registered Loggers" ,
                 strPossValShow  =  "Set Possible Vals as Temp Vals" ,
               strClearTempVals  =  "Clear ALL Temp Vals" ,
                 strRunLockVals  =  "Run the Locked Values algorithm" ,
                strShowSolution  =  "Show the current game SOLUTION" ,
                 strF2tooltip    =  "F2 - ABANDON the current game & Load a RANDOM Pseudokeu Game" ,
                 strF4tooltip    =  "F4 - Show or Hide all Squares with values duplicated by a Square" +
                 		                " in the same Row, Col, or Zone" ,
                 strF7tooltip    =  "F7 - Undo the last value set in the Grid" ,
                 strF8tooltip    =  "F8 - Redo the last UNDO action" ,
                 strF9tooltip    =  "F9 - Solve the current game (use the Settings menu to control " +
                 		                "the delay interval between revealing each Square)" ;
  
  /** 
   * Logging management
   * @see LogControl
   */
  protected static LogControl logControl ;
  
  /**
   * Logging actions
   * @see PskLogger
   */
  protected static PskLogger logger ;
  
  /** Control expression of DEBUG code  */
  static boolean DEBUG;
  
  /** Keep track of {@link Square} size (in pixels) */
  private int pxSqrLength ;
  
  /** User's Screen size (in pixels) */
  private Dimension pxScreenSize ;
  
  /** Keep track of Game size (in pixels) */
  private Dimension pxGameSize ;
  
  /** Keep track of Info Panels size (in pixels) */
  private Dimension pxPanelSize ;
  
  /** Width for the Message Frames (Inner Classes) in pixels */
  private int pxMsgFrameWidth ;
  
  /** current difficulty level
   *  @see Loader#STR_DIFF_FOLDERS
   */
  private int difficulty ;
  
  /** Name of the current game */
  private String currentGameName ;
  
  /** Number of the current game */
  private int currentGameNum ;
  
  /** Is the Game active?  */
  private boolean running = false ;
  
  /** Is the {@link AddGameFrame} showing?  */
  private boolean addingGame = false ;
  
  /**
   * Send SOLVE events to {@link GameListener}
   * @see javax.swing.Timer
   */
  private Timer solveTimer ;
  
  /** Is the Solve function active?  */
  private boolean autoSolveActive = false ;
  
  /** keep track of the number of SOLVE actions  */
  private int solveCount ;
  
  /**
   *  the SOLVE delay interval in msec
   *  @see #solveDelayMenuItem
   */
  private int solveDelay_msec ;
  
  /** Indicate <var>conflicting</var> {@link Square}s ?  */
  private boolean showConflicts = false ;
  
  /**
   *  to display conflict status
   *  @see #setConflicts
   */
  private String conflictDisplay ;
  
  /**
   *  For time calculation
   *  @see #runClock
   */
  private int hrs, minsecs, mins, secs ;
  
  /**
   *  For time display
   *  @see #runClock
   */
  private String strHr, strMin, strSec ;
  
  /** elapsed seconds of game time  */
  private int seconds ;
  
  /** the clock update interval  */
  private int clockDelaySeconds = 1 ;
  
  /**
   * Keep track of game time
   * @see javax.swing.Timer
   */
  private Timer clock ;
  
  /** Object of Inner Class {@link GameListener} */
  private GameListener listener ;
  
  /**
   *  Have Games finished loading?
   *  @see SwingWorker#isDone
   *  @see #loadWorkerDone
   */
  private boolean gamesLoaded = false ;
  
  /** Game {@link Loader} */
  private Loader loader ;
  
  /**
   *  Separate thread for getting {@link Loader.SavedGame}s from {@link Loader}
   *  @see SwingWorker
   *  @see #createLoadWorker
   */
  private SwingWorker<Integer, Void> loadWorker ;
  
  /** {@link Grid} of {@link Square}s */
  Grid grid ;
  
  //
  //
  //  COMPONENTS
  //
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** 
   *  Uses the <b>default</b> {@link java.awt.BorderLayout}: <br>
   *  with {@link #topEnclosingPanel} in North, {@link #botBtnPanel} in South, <br>
   *  and {@link #myLayeredPane} in Center
   */
  Container     myContentPane ;
  
  /**  contains the {@link Grid} */
  JLayeredPane  myLayeredPane ;
  
  // Menu items
  /** contains {@link #gameMenu} & {@link #settingsMenu} & {@link #helpMenu} */
  private JMenuBar  frameMenuBar ;
  
  /** contains {@link #newGameMenuItem}, {@link #selectGameMenuItem} & {@link #exitMenuItem} */
  private JMenu  gameMenu ;
  /** contained by {@link #gameMenu} */
  private JMenuItem  newGameMenuItem  ,
                     selectGameMenuItem ,
                     addGameMenuItem  ,
                     exitMenuItem     ;
  
  /**
   *  Launched by {@link #selectGameMenuItem}
   *  @see SelectGameFrame
   */
  private SelectGameFrame  selectFrame ;
  
  /**
   *  Launched by {@link #addGameMenuItem}
   *  @see AddGameFrame
   */
  private AddGameFrame  addFrame ;
  
  /** contains {@link #solveDelayMenuItem} & {@link #difficultyMenuItem} & {@link #sizeMenuItem} */
  private JMenu  settingsMenu       ;
  /** contained by {@link #settingsMenu} - launches {@link #sizeFrame} */
  private JMenuItem  sizeMenuItem       ;
  /** contained by {@link #settingsMenu} */
  private JMenuItem  solveDelayMenuItem      ,
                     difficultyMenuItem ;
  
  /**
   *  Launched by {@link #sizeMenuItem}
   *  @see GameSizeFrame
   */
  private GameSizeFrame  sizeFrame ;
  
  /**
   *  Launched by {@link #sizeMenuItem}
   *  @see ChooseDifficultyFrame
   */
  private ChooseDifficultyFrame  difficultyFrame ;
  
  /**
   *  Launched by {@link #solveDelayMenuItem}
   *  @see SolveDelayFrame
   */
  private SolveDelayFrame delayFrame ;
  
  /** contains {@link #instructMenuItem} & {@link #aboutMenuItem} & {@link #revealWrongMenuItem} */
  private JMenu  helpMenu         ;
  /** contained by {@link #helpMenu} */
  private JMenuItem  instructMenuItem ,
                     aboutMenuItem    ,
                     revealWrongMenuItem ;
  
  // Panels & Buttons
  /** contains {@link #mesgPanel} & {@link #topBtnPanel} <br>
   *  - contained by {@link #myContentPane} */
  JPanel  topEnclosingPanel ;
  
  /** contains {@link #sqrsLabel}, {@link #infoPanel} & {@link #timeLabel} <br>
   *  - contained by {@link #topEnclosingPanel} */
  private JPanel  mesgPanel   ;
  /** contained by {@link #mesgPanel} */
  private JLabel  sqrsLabel ,
                  timeLabel    ;
  
  /** contains {@link #infoTitle} & {@link #infoMesg} <br>
   *  - contained by {@link #mesgPanel} */
  private JPanel  infoPanel ;
  /** contained by {@link #infoPanel} */
  private JLabel  infoTitle ,
                  infoMesg  ;
  
  /** contains {@link #newGameButton}, {@link #conflictsButton} & {@link #hintButton} <br>
   *  - contained by {@link #topEnclosingPanel} */
  private JPanel  topBtnPanel     ;
  /** contained by {@link #topBtnPanel} */
  private JButton  newGameButton   ,
                   conflictsButton ,
                   hintButton      ;
  
  /** contains {@link #undoButton}, {@link #redoButton} & {@link #solveButton} <br>
   *  - contained by {@link #myContentPane} */
  JPanel  botBtnPanel ;
  /** contained by {@link #botBtnPanel} */
  private JButton  undoButton  ,
                   redoButton  ,
                   solveButton ;
  
  /** Just in case...  */
  private static final long serialVersionUID = -6137535886850988170L;

}// Class Launcher
