/* ***************************************************************************************
 * 
 *  Mark Sattolo (epistemik@gmail.com) 
 * -----------------------------------------------
 * $File: //depot/Eclipse/Java/workspace/StanfordTetris/src/mhs/tetris/stanford/JBrainTetris.java $
 * $Revision: #4 $ 
 * $Change: 166 $ 
 * $DateTime: 2012/01/02 22:14:27 $
 * -----------------------------------------------
 * 
 * mhs.tetris.stanford.JBrainTetris.java 
 * Eclipse version created on Jan 2, 2012
 * 
 * ***************************************************************************************
 */

package mhs.tetris.stanford;

import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

/**
 * Tetris that plays without user interaction
 * @author Lews Therin
 * @version $Revision: #4 $
 */
class JBrainTetris extends JTetris
{
  /**
   * Creates new JBrainTetris 
   * @param width of game
   * @param height of game 
   */
  JBrainTetris( int width, int height )
  {
    super( width, height );
    
  }// CONSTRUCTOR
  
 /*
  *    M E T H O D S
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
     
  void tick( int verb )
  {
    if( brainPlay.isSelected() )
    {
      board.undo();
      if( verb == DOWN )
      {
        if( cur_count != super.count )
        {
          mMove = mBrain.bestMove( board, currentPiece, board.getHeight() - TOP_SPACE );
          cur_count = super.count;
        }
        
        if( mMove == null || mMove.piece == null || currentPiece == null )
        {
          stopGame();
          return; // game over
        }
        
        if( !currentPiece.equals( mMove.piece ) )
          super.tick( ROTATE );
        
        if( currentX < mMove.x )
          super.tick( RIGHT );
        if( currentX > mMove.x )
          super.tick( LEFT );
      }
    }
    super.tick( verb );
    
  }// tick()
  
  public java.awt.Container createControlPanel()
  {
    java.awt.Container $panel2 = Box.createVerticalBox();
    $panel2 = super.createControlPanel();
    
    brainPlay = new JCheckBox( "Brain Play", false );
    if( testMode )
      brainPlay.setSelected( true );
    $panel2.add( brainPlay );
    
    JPanel $row2 = new JPanel();
    
    // ADVERSARY slider
    $row2.add( Box.createVerticalStrut( 12 ) );
    $row2.add( new JLabel( "Adversary:" ) );
    
    adversary = new JSlider( 0, 100, 0 ); // min, max, current
    adversary.setPreferredSize( new Dimension( 100, 15 ) );
    $row2.add( adversary );
    
    JPanel $text = new JPanel();
    $text.add( adStat = new JLabel( ADVERSARY_OFF ) );
    
    $panel2.add( $text );
    $panel2.add( $row2 );
    
    JPanel $row3 = new JPanel();
    
    // Mr. Happy slider
    $row3.add( Box.createVerticalStrut( 12 ) );
    $row3.add( new JLabel( "Mr. Happy:" ) );
    
    happy = new JSlider( 0, 100, 0 ); // min, max, current
    happy.setPreferredSize( new Dimension( 100, 15 ) );
    $row3.add( happy );
    
    JPanel $text2 = new JPanel();
    $text2.add( adHappy = new JLabel( HAPPY_OFF ) );
    
    $panel2.add( $text2 );
    $panel2.add( $row3 );
    
    return ( $panel2 );
    
  }// createControlPanel()

  Piece pickNextPiece()
  {
    if( adversary.getValue() == 0 && happy.getValue() == 0 )
    {
      adStat.setText( ADVERSARY_OFF );
      adHappy.setText( HAPPY_OFF );
      return ( super.pickNextPiece() ); // not to mess with the sequence of random numbers for test mode
    }

    if( adversary.getValue() != 0 && happy.getValue() != 0 )
    {
      adversary.setValue( 0 );
      adversary.repaint();
    }

    if( random.nextInt( 100 ) <= adversary.getValue() )
    {
      adStat.setText( ADVERSARY_ON );
      return getWorstPiece( true );
    }
    adStat.setText( ADVERSARY_OFF );
    
    if( random.nextInt( 100 ) <= happy.getValue() )
    {
      adHappy.setText( HAPPY_ON );
      return getWorstPiece( false );
    }
    adHappy.setText( HAPPY_OFF );
    
    return super.pickNextPiece();
    
  }// pickNextPiece()

  private Piece getWorstPiece( boolean hurt_player )
  {
    Brain.Move $wMove = null;
    Brain.Move $tMove;
    
    int $index = 0;
    for( int i = 0; i < pieces.length; i++ )
    {
      $tMove = mBrain.bestMove( board, pieces[i], board.getHeight() - TOP_SPACE );
      
      if( i == 0 )
        $wMove = $tMove;
      
      if( $tMove == null )
      { // this piece loses the game now
        return pieces[i];
      }
      
      if( ( hurt_player && $tMove.score >= $wMove.score ) || ( !hurt_player && $tMove.score <= $wMove.score ) )
      {
        $wMove = $tMove;
        $index = i;
      }
    }
    return pieces[$index];
    
  }// getWorstPiece()
  
 /*
  *    F I E L D S
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    
  private Brain mBrain = new LameBrain();
  
  private Brain.Move mMove ;
  
  // Controls
  protected JSlider adversary;
  protected JLabel adStat;
  protected JSlider happy;
  protected JLabel adHappy;
  protected JCheckBox brainPlay;
  
  private int cur_count = -1;
  
  static final String
                     ADVERSARY_ON = "Malice Mode On >:-(" ,
                    ADVERSARY_OFF = "Malice Mode Off"     ,
                         HAPPY_ON = "Happy Mode On :-)"   ,
                        HAPPY_OFF = "Happy Mode Off"      ;
  
  /** keep the compiler from complaining */
  private static final long serialVersionUID = -5101282490961169538L;

}// class JBrainTetris
