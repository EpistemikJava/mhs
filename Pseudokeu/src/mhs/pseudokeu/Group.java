/* ***************************************************************************************
 
   Mark Sattolo (epistemik@gmail.com)
 -----------------------------------------------
 $File: //depot/Eclipse/Java/workspace/Pseudokeu/src/mhs/pseudokeu/Group.java $
 $Revision: #15 $
 $Change: 175 $
 $DateTime: 2012/02/25 09:39:04 $
 -----------------------------------------------
 
  mhs.latinsquare.LatinSquareGroup.java
  Eclipse version created on Dec 25, 2007, 13:53 PM
  git version created Mar 8, 2014
 
*************************************************************************************** */

package mhs.pseudokeu;

import java.awt.Color;
import java.util.Arrays;
import java.util.logging.Level;

/**
 *  The Grid sub-sections, either {@link Row}s, {@link Col}s, or {@link Zone}s,
 *  each composed of an array of {@link Square}s of size {@link #gridLength}<br>
 *  Each implemented <code>Group</code> <em>Subclass</em> is an extension of this <b>abstract</b> Class
 *
 *  @author  Mark Sattolo
 *  @version $Revision: #15 $
 */
public abstract class Group
{
 /*
  *            C O N S T R U C T O R S
  ***************************************************************************************/
  
  /**
   *  Base Constructor  
   *  @param mygrid - reference to the {@link Grid} I am in
   */
  public Group( final Grid mygrid )
  {
    grid = mygrid ;
    gridLength = grid.getLength() ;
    logger = Grid.logger ;
    
    nOpen = gridLength ;
    nSqrsWithVal = new int[ gridLength + 1 ]; // all array entries are 0 at start
    
     sqrsCanBeVal = new int[ gridLength + 1 ];
    nSqrsCanBeVal = new int[ gridLength + 1 ];
    
    // at most will keep track of quads
    blockSqrs = new int[] { -1, -1, -1, -1 };
    blockVals = new int[] { 0, 0, 0, 0 };
    maxBlockLength = blockVals.length ;
    logger.fine( "maxBlockLength = " + maxBlockLength );
  }
  
  /**
   *  Intermediate Constructor
   *  @param mygrid - reference to the {@link Grid} I am in
   *  @param row - 1st index into {@link Grid#sqrs2dArray}
   *  @param col - 2nd index into {@link Grid#sqrs2dArray}
   */
  public Group( final Grid mygrid, final int row, final int col )
  {
    this( mygrid );
    startingRow = row ;
    startingCol = col ;
  }
  
  /**
   *  USUAL Constructor
   *  @param mygrid - reference to the {@link Grid} I am in
   *  @param pos - index into the {@link Group} array for my type
   *  @param row - 1st index into {@link Grid#sqrs2dArray}
   *  @param col - 2nd index into {@link Grid#sqrs2dArray}
   */
  public Group( final Grid mygrid, final int pos, final int row, final int col )
  {
    this( mygrid, row, col );
    posnIndex = pos ;
  }
  
 /*
  *            M E T H O D S
  ***************************************************************************************/
  
  /** Each concrete subclass must implement this <b>abstract</b> method */
  abstract void initMySqrs();
  
  /**
   *  There is a {@link Square} with a changed <var>value</var> in this {@link Group}<br>
   *  Must update my <var>fields</var> then notify the <b>Group Squares</b> in this Group <br> 
   *  - called by {@link Square#newValue}
   *  @param oldval  - old <var>value</var> of the changed {@link Square}
   *  @param newval  - new <var>value</var> of the changed {@link Square}
   *  @param oldtype - old <var>type</var> of the changed {@link Square}
   *  @param newtype - new <var>type</var> of the changed {@link Square}
   *  @see #notifySqrsOfValChange
   */
  void changedSqr( final int oldval, final int newval, final SqrTypes oldtype, final SqrTypes newtype )
  {
    logger.fine( myPosn() + ": " + oldval + " (" + oldtype + ") -> " + newval + " (" + newtype + ")" );
    
    if( oldtype != newtype )
      adjustTypeCounts( oldtype, newtype );
    
    if( (oldtype == SqrTypes.GUESS) && (--nSqrsWithVal[oldval] == 0) )
      nVals-- ;
    
    if( (newtype == SqrTypes.GUESS) && (++nSqrsWithVal[newval] == 1) )
      nVals++ ;
    
    /* each Square in this Group now needs updating - continue the 'Set Value Chain' */
    notifySqrsOfValChange( oldval, newval );
  
  }// Group.changedSqr()
  
  /**
   *  <var>Type</var> of the <b>Active Square</b> is changing <br>
   *  - called by {@link #changedSqr} or {@link Square#setFixed}
   *  @param oldType - old <var>type</var> of the changed {@link Square}
   *  @param newType - new <var>type</var> of the changed {@link Square}
   */
  void adjustTypeCounts( final SqrTypes oldType, final SqrTypes newType )
  {
    logger.fine( myPosn() + ": '" + oldType + "' -> '" + newType + "'" );
    
    if( newType == SqrTypes.FIXED ) // Guess -> Fixed : ONLY when Loading a game
    {
      nFixed++ ;
      nGuesses-- ;
    }
    else // Guess -> Open
    if( oldType == SqrTypes.GUESS )
    {
      nGuesses-- ;
      nOpen++ ;
    }
    else // Open -> Guess
    {
      nGuesses++ ;
      nOpen-- ;
    }
  
  }// Group.adjustTypeCounts()
  
  /**
   *  Have a new <var>value</var> in one of my {@link Square}s <br>
   *  - must update each <b>Group Square</b> of the Active Square <br>
   *  - called by {@link #changedSqr}
   *  @param oldVal - old <var>value</var> in the changed {@link Square}
   *  @param newVal - new <var>value</var> in the changed {@link Square}
   *  @see Zone#notifySqrsOfValChange
   *  @see Square#adjustGroupSqrCounts
   */
  void notifySqrsOfValChange( final int oldVal, final int newVal )
  {
    logger.fine( myPosn() + ": '" + oldVal + "' -> '" + newVal + "'" );
    
    Square $activeSqr = grid.getActiveSqr();
    
    for( Square s : mySqrs )
    {
      logger.finer( "Sqr " + ( s == $activeSqr ? "=" : "!" ) + "= activeSqr" + "; type = " + s.getType() );
      
      // EXCLUDE the active Square
      if( s != $activeSqr )
        /* continue the 'Set Value Chain' */
        s.adjustGroupSqrCounts( oldVal, newVal );
    }
    
  }// Group.notifySqrsOfValChange()
  
  /** Set all mutable fields to default values */
  final void clear()
  {
    for( int v=0; v <= gridLength; v++ )
    {
      nSqrsWithVal[v] = 0 ;
       sqrsCanBeVal[v] = 0 ;
      nSqrsCanBeVal[v] = 0 ;
    }
    
    nVals = nFixed = nGuesses = 0 ;
    nOpen = gridLength ;
    
    resetBlocks();
    
  }// Group.clear()
  
  /**
   * Find one of my {@link Square}s that is the Color match for the parameter Square at the parameter value <br>
   * - called by {@link Grid#buildColorChain}
   * @param chainColor - current level
   * @param val - to check
   * @return success or failure
   */
  boolean findColorSqr( final int chainColor, final int val )
  {
    boolean $interim, $result = false ;
    logger.fine( myPosn() );
    
    for( Square s : mySqrs )
      if( ( !s.inColorChain() ) && s.canBeVal(val) )
      {
        $interim = grid.buildColorChain( chainColor, val, s );
        if( ! $result )
          $result = $interim ;
      }
    
    return $result ;
    
  }// Group.findColorSqr()
  
  /**
   *  Find one of my {@link Square}s that is the <b>only possibility</b> for a particular value <br>
   *  - called by {@link Grid#findGrpSingle}
   *  @return the Square or null if none
   */
  Square findSingleSqrForVal()
  {
    logger.append( myPosn() + ": " );
    
    int $posn=0 ;
    for( int v=1; v <= gridLength; v++ )
      if( nSqrsCanBeVal[v] == 1 )
      {
        $posn = Integer.numberOfTrailingZeros( sqrsCanBeVal[v] );
        mySqrs[$posn].solvedValue = v ;

        logger.append( "Found sqrsCanBeVal[" + v + "] == mySqrs[" + $posn + "] / Sqr " + mySqrs[$posn].strGridPosn() );
        logger.send( Level.INFO );
        
        return mySqrs[$posn] ;
      }
    
    logger.send( Level.FINE );
    return null ;
    
  }// Group.findSingleSqrForVal()
  
  /**
   *  Find any locked values and adjust the possible vals of affected Squares, if necessary <br>
   *  - called by {@link Grid#findLockedVals}
   *  @return success or failure
   */
  boolean findLockedVals()
  {
    int $sqrLocn1=0, $sqrLocn2=0, $sqrLocn3=0 ;
    Group $grp = null ;
    boolean $found = false, $interim = false, $result = false ;
    
    // make sure I have some Open Squares
    if( nOpen < 1 )
      return false ;
    
    logger.appendln( "Search " + myPosn() + ":" );
    
    for( int v=1; v <= gridLength; v++ )
      // make sure I do not have this value
      if( nSqrsWithVal[v] == 0 )
      {
        // this value can ONLY be in 2 OR 3 Squares
        if( (nSqrsCanBeVal[v] == 2) || (nSqrsCanBeVal[v] == 3) )
        {
          $sqrLocn1 = Helper.getBitPosn( sqrsCanBeVal[v], 1 );
          $sqrLocn2 = Helper.getBitPosn( sqrsCanBeVal[v], 2 );
          $found = true ;
          logger.append( "\t Found Locked val '" + v + "'" + " at Sqrs " + $sqrLocn1 + "," + $sqrLocn2 );
          
          if( nSqrsCanBeVal[v] == 3 )
          {
            $sqrLocn3 = Helper.getBitPosn( sqrsCanBeVal[v], 3 );
            logger.appendln( "," + $sqrLocn3 + " / 3" );
          }
          else
            {
              $sqrLocn3 = 0 ;
              logger.appendln( " / 2" );
            }
          
          $grp = getCommonGroup( $sqrLocn1, $sqrLocn2, $sqrLocn3 );
          
          if( $grp != null )
          {
            logger.appendln( "\t\t with a Common Group: " + $grp.myPosn() );
            
            $interim = $grp.setPossValsFromLockedVals( this, v );
            if( ! $result )
              $result = $interim ;
          }
        }// if only in 2 OR 3 sqrs
      }// if do not have this value
    
    logger.send( $found ? Level.INFO : Level.FINE );
    //logger.info( myPosn() + ": result == " + result );
    return $result ;
    
  }// Group.findLockedVals()
  
  /**
   *  See if the Locked vals in a {@link Row} or {@link Col} have a common {@link Zone} <br>
   *  - called by {@link #findLockedVals}
   *  @param sqr1 - index of first Square to check
   *  @param sqr2 - index of second Square to check
   *  @param sqr3 - index of third Square to check
   *  @return common Sub{@link Group} if any, or null
   *  @see Zone#getCommonGroup
   */
  Group getCommonGroup( final int sqr1, final int sqr2, final int sqr3 )
  {
    boolean $result = false ;
    
    Group g = mySqrs[sqr1].getZone();
    if( g == mySqrs[sqr2].getZone() )
    {
      $result = true ;
      if( (sqr3 > 0) && (g != mySqrs[sqr3].getZone()) )
        $result = false ;
    }
    
    if( $result )
      return g ;
    
    logger.appendln( "\t\t $ NO Common Zone for Sqrs " + sqr1 + "," + sqr2 + (sqr3 > 0 ? ("," + sqr3) : "") );
    return null ;
    
  }// Group.getCommonGroup()
  
  /**
   *  Adjust the possible vals of my Squares <br>
   *  - called by {@link #findLockedVals}
   *  @param own - Zone to exclude
   *  @param val - value to remove from Squares
   *  @return success or failure
   *  @see Square#removePossibleVal
   *  @see Zone#setPossValsFromLockedVals
   */
  boolean setPossValsFromLockedVals( final Group own, final int val )
  {
    boolean $interim = false, $result = false ;
    for( Square s : mySqrs )
      if( s.getZone() != own )
      {
        $interim = s.removePossibleVal( val );
        if( ! $result )
          $result = $interim ;
      }
    
    //logger.info( myPosn() + ": result == " + result );
    return $result ;
    
  }// Group.setPossValsFromLockedVals()
  
  /**
   *  Remove a {@link Square} from {@link #sqrsCanBeVal}[] <br>
   *  - called by {@link Square#removeSqrFromGrpVal}
   *  @param sqr - Square to remove
   *  @param val - value to remove from
   *  @return success or failure
   */
  boolean removeSqrFromVal( final Square sqr, final int val )
  {
    int $bitIndex ;
    boolean $result = false ;
    
    for( int i=0; i < gridLength; i++ )
      // find the index of this Square
      if( mySqrs[i] == sqr )
      {
        $bitIndex = ( 1 << i );
        // make sure this index is in the array
        if( (sqrsCanBeVal[val] | $bitIndex) == sqrsCanBeVal[val] )
        {
          logger.append( "\t\t\t\t $ " + myPosn() + ": REMOVE mySqrs[" + i + "] from val[" + val 
                         + "] == " + Helper.displaySetBits(sqrsCanBeVal[val], gridLength, "") );
          
          $result = true ;
          
           sqrsCanBeVal[val] -= $bitIndex ;
          nSqrsCanBeVal[val]-- ;
          
          logger.appendln( " -> NOW == " + Helper.displaySetBits(sqrsCanBeVal[val], gridLength, "")
                           + " / n." + nSqrsCanBeVal[val] );
        }
      }
    
    return $result ;
    
  }// Group.removeSqrFromVal()
  
  /**
   *  Set the {@link Square} indices for each of my Open values <br>
   *  - called by {@link Grid#setSqrsCanBeVal}
   *  @see #sqrsCanBeVal
   *  @return success or failure
   */
  boolean setSqrsCanBeVal()
  {
    logger.appendln( myPosn() + ": " );
    
    boolean $result = false ;
    for( int v=1; v <= gridLength; v++ )
    {
      // clear the old settings
       sqrsCanBeVal[v] = 0 ;
      nSqrsCanBeVal[v] = 0 ;
      
      for( int i=0; i < gridLength; i++ )
        // find Open Squares that could contain this value
        if( mySqrs[i].isOpen() && (mySqrs[i].numGrpSqrsWithVal(v) == 0) && (! mySqrs[i].getExcludeVal(v))  )
        {
           sqrsCanBeVal[v] += ( 1 << i );
          nSqrsCanBeVal[v]++ ;
          
          $result = true ;
          logger.appendln( "Add mySqrs[" + i + "] to val[" + v + "]" );
        }
    }
    
    logger.send( $result ? Level.FINE : Level.FINER );
    return $result ;
    
  }// Group.setSqrsCanBeVal()
  
  /**
   *  Find Square Pairs [aka "Naked Pairs"]<br>
   *  - called by {@link Grid#findPairs}
   *  @return success or failure
   */
  boolean findSqrPairs()
  {
    logger.append( "\t" + myPosn() + ": find Square Pairs" );
    
    boolean $interim = false, $result = false ;
    
    // nothing to do if 2 or fewer open Squares
    if( nOpen > 2 )
    {
      resetBlocks();
      
      for( int i=0; i < gridLength; i++ )
      {
        if( mySqrs[i].numPossibleVals() == 2 )
          for( int j=i+1; j < gridLength ; j++ )
            if( mySqrs[j].getPossibleVals() == mySqrs[i].getPossibleVals() )
            {
              logger.appendln( "\n\t nOpen == " + nOpen + " / i == " + i + " ; j == " + j );
              
              blockSqrs[0] = i ;
              blockSqrs[1] = j ;
              
              blockVals[0] = Helper.getBitPosn( mySqrs[i].getPossibleVals(), 1 );
              blockVals[1] = Helper.getBitPosn( mySqrs[i].getPossibleVals(), 2 );
              
              $interim = setPossValsFromBlocks( SQUARE_BLOCKS );
              if( ! $result )
                $result = $interim ;
            }
      }
    }// if( nOpen > 2 )
    
    logger.send( Level.INFO );
    return $result ;
    
  }// Group.findSqrPairs()
  
  /**
   *  Find Group Pairs [aka "Hidden Pairs"]<br>
   *  - called by {@link Grid#findPairs}
   *  @return success or failure
   */
  boolean findGrpPairs()
  {
    logger.append( "\t" + myPosn() + ": find Group Pairs" );
    
    boolean $interim = false, $result = false ;
    
    // nothing to do if 2 or fewer open Squares
    if( nOpen > 2 )
    {
      resetBlocks();
      
      for( int v=1; v <= gridLength; v++ )
      {
        if( nSqrsCanBeVal[v] == 2 )
          for( int w=v+1; w <= gridLength ; w++ )
            if( sqrsCanBeVal[w] == sqrsCanBeVal[v] )
            {
              logger.appendln( "\n\t nOpen == " + nOpen + " / v == " + v + " ; w == " + w );
              
              blockSqrs[0] = Helper.getBitPosn( sqrsCanBeVal[v], 1 );
              blockSqrs[1] = Helper.getBitPosn( sqrsCanBeVal[v], 2 );
              
              blockVals[0] = v ;
              blockVals[1] = w ;
              
              $interim = setPossValsFromBlocks( GROUP_BLOCKS );
              if( ! $result )
                $result = $interim ;
            }
      }
    }// if( nOpen > 2 )
    
    logger.send( Level.INFO );
    return $result ;
    
  }// Group.findGrpPairs()
  
  /**
   *  Find Square Triples [aka "Naked Triples"]<br>
   *  - called by {@link Grid#findTriples}
   *  @return success or failure
   */
  boolean findSqrTriples()
  {
    logger.append( "\t" + myPosn() + ": find Square Triples" );
    
    int $res1=0, $res2=0 ;
    boolean $result = false ;
    
    // nothing to do if 3 or fewer open Squares
    if( nOpen > 3 )
    {
      resetBlocks();
      
      loop:
      for( int i=0, j=0, k=0; i < gridLength; i++ )
        if( (mySqrs[i].numPossibleVals() == 2) || (mySqrs[i].numPossibleVals() == 3) )
          for( j=i+1; j < gridLength ; j++ )
            if( (mySqrs[j].numPossibleVals() == 2) || (mySqrs[j].numPossibleVals() == 3) )
            {
              $res1 = mySqrs[i].getPossibleVals() | mySqrs[j].getPossibleVals() ;
              
              // make sure we have exactly 3 values
              if( Helper.numSetBits($res1, gridLength) != 3 )
                continue ;
              
              for( k=j+1; k < gridLength ; k++ )
                if( (mySqrs[k].numPossibleVals() == 2) || (mySqrs[k].numPossibleVals() == 3) )
                {
                  $res2 = $res1 | mySqrs[k].getPossibleVals() ;
                  if( $res2 == $res1 )
                  {
                    logger.appendln( "\n\t nOpen == " + nOpen + " / i == " + i + " ; j == " + j + " ; k == " + k
                                     + " ; res1 == " + Helper.displaySetBits($res1, gridLength, "")
                                     + " ; res2 == " + Helper.displaySetBits($res2, gridLength, "") );
                    
                    blockSqrs[0] = i ;
                    blockSqrs[1] = j ;
                    blockSqrs[2] = k ;
                    
                    blockVals[0] = Helper.getBitPosn( $res2, 1 );
                    blockVals[1] = Helper.getBitPosn( $res2, 2 );
                    blockVals[2] = Helper.getBitPosn( $res2, 3 );
                    
                    $result = setPossValsFromBlocks( SQUARE_BLOCKS );
                    if( $result )
                      break loop ;
                  }
                }
            }
    }// if( nOpen > 3 )
    
    logger.send( Level.INFO );
    return $result ;
    
  }// Group.findSqrTriples()
  
  /**
   *  Find Group Triples [aka "Hidden Triples"]<br>
   *  - called by {@link Grid#findTriples}
   *  @return success or failure
   */
  boolean findGrpTriples()
  {
    logger.append( "\t" + myPosn() + ": find Group Triples" );
    
    int $res1=0, $res2=0 ;
    boolean $result = false ;
    
    // nothing to do if 3 or fewer open Squares
    if( nOpen > 3 )
    {
      resetBlocks();
      
      loop:
      for( int v=1, w=1, x=1; v <= gridLength; v++ )
        if( (nSqrsCanBeVal[v] == 2) || (nSqrsCanBeVal[v] == 3) )
          for( w=v+1; w <= gridLength ; w++ )
            if( (nSqrsCanBeVal[w] == 2) || (nSqrsCanBeVal[w] == 3) )
            {
              $res1 = sqrsCanBeVal[v] | sqrsCanBeVal[w] ;
              
              // make sure we have exactly 3 Squares
              if( Helper.numSetBits($res1, gridLength) != 3 )
                continue ;
              
              for( x=w+1; x <= gridLength ; x++ )
                if( (nSqrsCanBeVal[x] == 2) || (nSqrsCanBeVal[x] == 3) )
                {
                  $res2 = $res1 | sqrsCanBeVal[x] ;
                  if( $res2 == $res1 )
                  {
                    logger.appendln( "\n\t nOpen == " + nOpen + " / v == " + v + " ; w == " + w + " ; x == " + x
                                     + " ; res1 == " + Helper.displaySetBits($res1, gridLength, "")
                                     + " ; res2 == " + Helper.displaySetBits($res2, gridLength, "") );
                    
                    blockVals[0] = v ;
                    blockVals[1] = w ;
                    blockVals[2] = x ;
                    
                    blockSqrs[0] = Helper.getBitPosn( $res2, 1 );
                    blockSqrs[1] = Helper.getBitPosn( $res2, 2 );
                    blockSqrs[2] = Helper.getBitPosn( $res2, 3 );
                    
                    $result = setPossValsFromBlocks( GROUP_BLOCKS );
                    if( $result )
                      break loop ;
                  }
                }
            }
    }// if( nOpen > 3 )
    
    logger.send( Level.INFO );
    return $result ;
    
  }// Group.findGrpTriples()
  
  /**
   *  Find Square Quads [aka "Naked Quads"]<br>
   *  - called by {@link Grid#findQuads}
   *  @return success or failure
   */
  boolean findSqrQuads()
  {
    logger.append( "\t" + myPosn() + ": find Square Quads" );
    
    int $res1=0, $res2=0, $res3=0 ;
    boolean $result = false ;
    
    // nothing to do if 4 or fewer open Squares
    if( nOpen > 4 )
    {
      resetBlocks();
      
      loop:
      for( int i=0; i < gridLength; i++ )
        if( (mySqrs[i].numPossibleVals() >= 2) && (mySqrs[i].numPossibleVals() <= 4) )
          for( int j=i+1; j < gridLength ; j++ )
            if( (mySqrs[j].numPossibleVals() >= 2) && (mySqrs[j].numPossibleVals() <= 4) )
            {
              $res1 = mySqrs[i].getPossibleVals() | mySqrs[j].getPossibleVals() ;
              
              // make sure we have NO MORE than 4 values
              if( Helper.numSetBits($res1, gridLength) > 4 )
                continue ;
              
              for( int k=j+1; k < gridLength ; k++ )
                if( (mySqrs[k].numPossibleVals() >= 2) && (mySqrs[k].numPossibleVals() <= 4) )
                {
                  $res2 = $res1 | mySqrs[k].getPossibleVals() ;
                  
                  // now make sure we have EXACTLY 4 values
                  if( Helper.numSetBits($res2, gridLength) != 4 )
                    continue ;
                  
                  for( int m=k+1; m < gridLength ; m++ )
                    if( (mySqrs[m].numPossibleVals() >= 2) && (mySqrs[m].numPossibleVals() <= 4) )
                    {
                      $res3 = $res2 | mySqrs[m].getPossibleVals() ;
                      if( $res3 == $res2 )
                      {
                        logger.appendln( "\n\t nOpen == " + nOpen + " / i == " + i 
                                         + " ; j == " + j + " ; k == " + k + " ; m == " + m
                                         + " ; res1 == " + Helper.displaySetBits($res1, gridLength, "")
                                         + " ; res2 == " + Helper.displaySetBits($res2, gridLength, "")
                                         + " ; res3 == " + Helper.displaySetBits($res3, gridLength, "")
                                       );
                        blockSqrs[0] = i ;
                        blockSqrs[1] = j ;
                        blockSqrs[2] = k ;
                        blockSqrs[3] = m ;
                        
                        blockVals[0] = Helper.getBitPosn( $res2, 1 );
                        blockVals[1] = Helper.getBitPosn( $res2, 2 );
                        blockVals[2] = Helper.getBitPosn( $res2, 3 );
                        blockVals[3] = Helper.getBitPosn( $res2, 4 );
                        
                        $result = setPossValsFromBlocks( SQUARE_BLOCKS );
                        if( $result )
                          break loop ;
                      }
                    }
                }
            }
    }// if( nOpen > 4 )
    
    logger.send( Level.INFO );
    return $result ;
    
  }// Group.findSqrQuads()
  
  /**
   *  Find Group Quads [aka "Hidden Quads"]<br>
   *  - called by {@link Grid#findQuads}
   *  @return success or failure
   */
  boolean findGrpQuads()
  {
    logger.append( "\t" + myPosn() + ": find Group Quads" );
    
    int $res1=0, $res2=0, $res3=0 ;
    boolean $result = false ;
    
    // nothing to do if 4 or fewer open Squares
    if( nOpen > 4 )
    {
      resetBlocks();
      
      loop:
      for( int v=1; v <= gridLength; v++ )
        if( (nSqrsCanBeVal[v] >= 2) && (nSqrsCanBeVal[v] <= 4) )
          for( int w=v+1; w <= gridLength ; w++ )
            if( (nSqrsCanBeVal[w] >= 2) && (nSqrsCanBeVal[w] <= 4) )
            {
              $res1 = sqrsCanBeVal[v] | sqrsCanBeVal[w] ;
              
              // make sure we have NO MORE than 4 Squares
              if( Helper.numSetBits($res1, gridLength) > 4 )
                continue ;
              
              for( int x=w+1; x <= gridLength ; x++ )
                if( (nSqrsCanBeVal[x] >= 2) && (nSqrsCanBeVal[x] <= 4) )
                {
                  $res2 = $res1 | sqrsCanBeVal[x] ;
                  
                  // now make sure we have EXACTLY 4 Squares
                  if( Helper.numSetBits($res2, gridLength) != 4 )
                    continue ;
                  
                  for( int y=x+1; y <= gridLength ; y++ )
                    if( (nSqrsCanBeVal[y] >= 2) && (nSqrsCanBeVal[y] <= 4) )
                    {
                      $res3 = $res2 | sqrsCanBeVal[y] ;
                      if( $res3 == $res2 )
                      {
                        logger.appendln( "\n\t nOpen == " + nOpen + " / v == " + v
                                         + " ; w == " + w + " ; x == " + x + " ; y == " + y
                                         + " ; res1 == " + Helper.displaySetBits($res1, gridLength, "")
                                         + " ; res2 == " + Helper.displaySetBits($res2, gridLength, "")
                                         + " ; res3 == " + Helper.displaySetBits($res3, gridLength, "")
                                       );
                        blockVals[0] = v ;
                        blockVals[1] = w ;
                        blockVals[2] = x ;
                        blockVals[3] = y ;
                        
                        blockSqrs[0] = Helper.getBitPosn( $res2, 1 );
                        blockSqrs[1] = Helper.getBitPosn( $res2, 2 );
                        blockSqrs[2] = Helper.getBitPosn( $res2, 3 );
                        blockSqrs[3] = Helper.getBitPosn( $res2, 4 );
                        
                        $result = setPossValsFromBlocks( GROUP_BLOCKS );
                        if( $result )
                          break loop ;
                      }
                    }
                }
            }
    }// if( nOpen > 4 )
    
    logger.send( Level.INFO );
    return $result ;
    
  }// Group.findGrpQuads()
  
  /**
   *  Update possible values using the Block fields <br>
   *  - called by the Block Solving methods
   *  @param fromSqrBlock - from a Square block or a Group block
   *  @return success or failure
   *  @see #blockSqrs
   *  @see #blockVals
   */
  boolean setPossValsFromBlocks( final boolean fromSqrBlock )
  {
    int i, v ;
    boolean $interim = false, $result = false ;
    
    logger.append( "\t\t" + myPosn() + ": " ); 
    
    if( fromSqrBlock ) // blockVals has values to EXCLUDE from ALL Squares NOT IN blockSqrs
    {
      logger.appendln( "EXCLUDE values " + Arrays.toString(blockVals) + " except for Sqrs " + Arrays.toString(blockSqrs) );
      
      for( i=0 ; i < gridLength ; i++ )
        if( (i != blockSqrs[0]) && (i != blockSqrs[1]) && (i != blockSqrs[2]) && (i != blockSqrs[3]) )
          for( v=0 ; v < maxBlockLength ; v++ )
            if( (blockVals[v] > 0) && (! mySqrs[i].getExcludeVal( blockVals[v] )) )
            {
              logger.appendln( "\t\t mySqrs[" + i + "].removePossibleVal( blockVals[" + v + "] == " + blockVals[v] + " )" );
              $interim = mySqrs[i].removePossibleVal( blockVals[v] );
              if( ! $result )
                $result = $interim ;
            }
    }
    else // blockVals has the ONLY values to retain in the Squares IN blockSqrs
    {
      logger.appendln( "ONLY values " + Arrays.toString(blockVals) + " in Sqrs " + Arrays.toString(blockSqrs) );
      
      for( i=0 ; i < gridLength ; i++ )
        if( (i == blockSqrs[0]) || (i == blockSqrs[1]) || (i == blockSqrs[2]) || (i == blockSqrs[3]) )
          for( v=1 ; v <= gridLength ; v++ )
            if( (v != blockVals[0]) && (v != blockVals[1]) && (v != blockVals[2]) && (v != blockVals[3])
                && (! mySqrs[i].getExcludeVal(v)) )
            {
              logger.appendln( "\t\t mySqrs[" + i + "].removePossibleVal( " + v + " )" );
              $interim = mySqrs[i].removePossibleVal( v );
              if( ! $result )
                $result = $interim ;
            }
    }
    
    return $result ;
    
  }// Group.setPossValsFromBlocks()
  
  /**
   *  Process Rectad value <br>
   *  - called by {@link Grid#findRectads}
   * @param val - value
   * @param sqr1 - 1st Square to exclude
   * @param sqr2 - 2nd Square to exclude
   * @return success or failure
   */
  boolean processRectadVal( final int val, final int sqr1, final int sqr2 )
  {
    resetBlocks();
    
    blockSqrs[0] = sqr1 ;
    blockSqrs[1] = sqr2 ;
    
    blockVals[0] = val ;
    
    return setPossValsFromBlocks( Group.SQUARE_BLOCKS );
    
  }// Group.processRectadVal()
  
  /**
   *  Process Rectad value <br>
   *  - called by {@link Grid#findHexad}
   * @param val - value
   * @param sqr1 - 1st Square to exclude
   * @param sqr2 - 2nd Square to exclude
   * @param sqr3 - 3rd Square to exclude
   * @return success or failure
   */
  boolean processHexadVal( final int val, final int sqr1, final int sqr2, final int sqr3 )
  {
    resetBlocks();
    
    blockSqrs[0] = sqr1 ;
    blockSqrs[1] = sqr2 ;
    blockSqrs[2] = sqr3 ;
    
    blockVals[0] = val ;
    
    return setPossValsFromBlocks( Group.SQUARE_BLOCKS );
    
  }// Group.processHexadVal()
  
  /**
   *  Reset Block squares and values <br>
   *  - called by all the 'findBlock' methods
   */
  void resetBlocks()
  {
    for( int i=0; i < maxBlockLength; i++ )
    {
      blockSqrs[i] = -1 ;
      blockVals[i] = 0 ;
    }
    
  }// Group.resetBlocks()
  
  /**
   *  Get the index in {@link #mySqrs} of the parameter {@link Square} <br>
   *  @param sqr - Square to check
   *  @return index, or -1 if this Square is NOT one of my Squares
   */
  int getSqrIndex( final Square sqr )
  {
    for( int i=0; i < gridLength; i++ )
      if( mySqrs[i] == sqr )
        return i ;
    
    return -1 ;
    
  }// Group.getSqrIndex()
  
  /**
   * What is my position in the grid?
   * @return int with <var>posnIndex</var>
   */
  int getPosn() { return posnIndex ;}
  
 // ===========================================================================================================
 //                            D E B U G    C O D E
 // ===========================================================================================================
  
  String myname() { return this.getClass().getSimpleName(); }
  String myPosn() { return( this.getClass().getSimpleName() + "[" + posnIndex + "]" ); }
  
  /**
   *  Send this {@link Group}'s parameters to {@link #logger}
   *  @param level - {@link Level} to display at
   *  @param brief - print less information
   *  @param info  - extra text to display
   */
  void display( final Level level, final boolean brief, final String info )
  {
    int i ;
    
    if( !brief ) logger.appendln( info );
    
    logger.appendln( myPosn() );
    if( !brief ) logger.appendln( "-----------------------------------------------------------------" );
    
    logger.append( " My Sqrs: " );
    for( i=0; i < gridLength; i++ )
    {
      logger.append( "[" + mySqrs[i].getValue() + "]" );
      if( (i+1)%3 == 0 ) logger.append( " " );
    }
    logger.append( " V." + nVals + " F." + nFixed + " G." + nGuesses + " O." + nOpen );
    
    logger.append( "\n nSqrsCanBeVal (1-" + gridLength + "): " );
    for( i=1; i <= gridLength; i++ )
      logger.append( i + "|" + nSqrsCanBeVal[i] + " " );
    
    logger.append( "\n  sqrsCanBeVal (1-" + gridLength + "): " );
    for( i=1; i <= gridLength; i++ )
      logger.append( nSqrsCanBeVal[i] > 0 ? (i + "|" + Helper.displaySetBits(sqrsCanBeVal[i], gridLength, "") + " ") : "" );
      //logger.append( i + "|" + Helper.displaySetBits(gridLength, sqrsCanBeVal[i], "") + " " );
    
    if( !brief )
    {
      logger.append( "\n Block values " + Arrays.toString(blockVals) + "& Block Sqrs " + Arrays.toString(blockSqrs) );
      
      logger.append( "\n       nSqrsWithVal (1-" + gridLength + "): " );
      for( i=1; i <= gridLength; i++ )
        logger.append( i + "|" + nSqrsWithVal[i] + " " );
    }
    
    logger.append( "\n-----------------------------------------------------------------" );
    
    logger.send( level );
    
  }// Group.display()

 /*
  *            F I E L D S
  ***************************************************************************************/
 
  /** Perforce file version */
  static final String strP4_VERSION = "$Revision: #15 $" ;
  
  /** Reference to my {@link Grid} */
  static Grid grid ;
  
  /** number of {@link Square}s on each side of the {@link Grid} */ 
  static int gridLength ;
  
  /** See {@link PskLogger} */
  protected static PskLogger logger ;
  
  /**
   * my position (index) in one of the {@link Group}s of {@link Grid} <br>
   * - depends on my subclass ({@link Row}, {@link Col}, or {@link Zone}) 
   */
  private int posnIndex ;

  /** first row of {@link Grid} I am in */
  int startingRow ;
  /** first col of {@link Grid} I am in */
  int startingCol ;
  
  /** Array of references to the {@link Square}s I have  */
  Square[] mySqrs ;
  
  /** How many of my {@link Square}s are {@link SqrTypes#FIXED}?  */
  private int nFixed ;
  
  /** How many of my {@link Square}s are a {@link SqrTypes#GUESS}?  */
  private int nGuesses ;
  
  /** How many of my {@link Square}s are Open? i.e. {@link SqrTypes#OPEN} or {@link SqrTypes#TEMP} */
  private int nOpen ;
  
  /**
   * For how many values do I have a {@link Square} with that value? <br>
   * - i.e. number of entries in {@link #nSqrsWithVal} which are > 0
   */
  private int nVals ;
  
  /**
   * For each value, how many of my {@link Square}s have that value <br>
   * - have to keep track of more than one in case of conflicting (duplicate) values and when guesses change
   */
  int[] nSqrsWithVal ;
  
  /** How many possible {@link Square}s do I have for each value? */
  int[] nSqrsCanBeVal ;
  
  /** Keep track (using bits for the indices of {@link #mySqrs}) 
   *  of which {@link Square}s can be each of my values */
  int[] sqrsCanBeVal ;
  
  /** maximum number of blocks - currently quads */
  private int maxBlockLength ;
  
  /** Keep note while Solving of which {@link Square}s have blocks (pairs, triples or quads) */
  int[] blockSqrs ;
  
  /** Keep note while Solving of which {@link Square}.<var>values</var> are found in blocks (pairs, triples or quads) */
  int[] blockVals ;
  
  /** to distinguish the two types of block, especially in {@link #setPossValsFromBlocks(boolean)} */
  static final boolean SQUARE_BLOCKS = true, GROUP_BLOCKS = false ;
  
}// Class Group


 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^  *
  *  <                                                                                           >  *
  *  <                               S U B C L A S S E S                                         >  *
  *  <                                                                                           >  *
  *  ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^  *
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */


////////////////////////////////////
///       CLASS COL             //////////////////////////////////////////////////////////////////////////
//////////////////////////////////

/**
 *  {@link Grid} sub-section composed of a vertical line of {@link Square}s, numbering <var>Grid.gridLength</var>
 *  @author Mark Sattolo
 *  @see Grid#getLength()
 */
class Col extends Group
{
  /**
   *  Constructor
   *  @param mygrid - reference to my {@link Grid}
   *  @param pos - index into the {@link Group} array for my type
   *  @param row - 1st index into {@link Grid#sqrs2dArray}
   *  @param col - 2nd index into {@link Grid#sqrs2dArray}
   */
  public Col( Grid mygrid, int pos, int row, int col )
  {
    super( mygrid, pos, row, col );

    initMySqrs();
    
  }// Col Constructor
  
  /**
   *  Fill {@link #mySqrs}[] with references to the appropriate
   *  {@link Square}s in {@link Grid#sqrs2dArray} <br><br>
   *  ONLY called by {@link Col#Col(Grid,int,int,int)}
   */
  @Override
  protected void initMySqrs()
  {
    mySqrs = new Square[ gridLength ];
    
    for( int i=0; i < gridLength; i++ )
      mySqrs[i] = grid.sqrs2dArray[i][startingCol] ;

  }// Col.initMySquares()
  
}/* SUBCLASS Col */


////////////////////////////////////
///       CLASS ROW             //////////////////////////////////////////////////////////////////////////
//////////////////////////////////

/**
 *  {@link Grid} sub-section composed of a horizontal line of {@link Square}s, numbering <var>Grid.gridLength</var>
 *  @author Mark Sattolo
 *  @see Grid#getLength()
 */
class Row extends Group
{
  /**
   *  Constructor
   *  
   *  @param mygrid - reference to my {@link Grid}
   *  @param pos - index into the {@link Group} array for my type
   *  @param row - 1st index into {@link Grid#sqrs2dArray}
   *  @param col - 2nd index into {@link Grid#sqrs2dArray}
   */
  public Row( final Grid mygrid, final int pos, final int row, final int col )
  {
    super( mygrid, pos, row, col );
    
    initMySqrs();
    
  }// Row Constructor
  
  /**
   *  Fill {@link #mySqrs}[] with references to the appropriate {@link Square}s in {@link Grid#sqrs2dArray} <br><br>
   *  ONLY called by {@link Row#Row(Grid,int,int,int)}
   */
  @Override
  protected void initMySqrs()
  {
    mySqrs = new Square[ gridLength ];
    
    for( int i=0; i < gridLength; i++ )
      mySqrs[i] = grid.sqrs2dArray[startingRow][i] ;
  
  }// Row.initMySquares()
  
}/* SUBCLASS Row */


////////////////////////////////////
///       CLASS ZONE            //////////////////////////////////////////////////////////////////////////
//////////////////////////////////

/**
 *  {@link Grid} sub-section composed of an 'N x N' block of {@link Square}s, and N = square root of <var>Grid.gridLength</var>
 *  @author Mark Sattolo
 *  @see Grid#getLength()
 */
class Zone extends Group
{
  /** A different {@link Color} for each {@link Zone}  */
  static final Color[] COLORS =
                   {
                     Color.PINK ,
                     Color.yellow.darker() ,
                     new Color( 180, 80, 254 ), // violet
                     Color.ORANGE ,
                     Color.GRAY.brighter() ,
                     Color.CYAN ,
                     Color.GREEN.brighter(),
                     Color.MAGENTA,// new Color( 254, 99, 200 ), // lighter than MAGENTA ,
                     Color.YELLOW.brighter()
                   };    
  
  /**
   *  Constructor
   *  
   *  @param mygrid - reference to my {@link Grid}
   *  @param pos - index into the {@link Group} array for my type
   *  @param row - 1st index into {@link Grid#sqrs2dArray}
   *  @param col - 2nd index into {@link Grid#sqrs2dArray}
   */
  public Zone( final Grid mygrid, final int pos, final int row, final int col )
  {
    super( mygrid, pos, row, col );
    
    initMySqrs();
    
  }// Zone Constructor
  
  /**
   *  Fill {@link #mySqrs}[] with references to the appropriate {@link Square}s in {@link Grid#sqrs2dArray} <br>
   *  - called by {@link Zone#Zone(Grid,int,int,int)}
   */
  @Override
  protected void initMySqrs()
  {
    mySqrs = new Square[ gridLength ];
    
    int i, j, k = 0 ;
    for( i = startingRow; i < startingRow + grid.getZoneLength(); i++ )
      for( j = startingCol; j < startingCol + grid.getZoneLength(); j++ )
      {
        if( (i < gridLength) && (j < gridLength) && (k < gridLength) )
        {
          mySqrs[k] = grid.sqrs2dArray[i][j] ;
          logger.fine( myname() + "[" + getPosn() + "].mySquares[" + k + "] = grid2dArray[" + i + "][" + j + "]" );
        }
        else
          {
            logger.warning( "! k = " + k + "; i = " + i + "; j = " + j );
            return ;
          }
        k++ ;
      }
  }// Zone.initMySquares()
  
  /**
   *  Have a new <var>value</var> in one of my {@link Square}s <br>
   *  - must update each <b>Group Square</b> of the Active Square <br>
   *  - called by {@link #changedSqr}
   *  @param oldVal - old <var>value</var> in the changed {@link Square}
   *  @param newVal - new <var>value</var> in the changed {@link Square}
   *  @see Group#notifySqrsOfValChange
   *  @see Square#adjustGroupSqrCounts
   */
  @Override
  protected void notifySqrsOfValChange( final int oldVal, final int newVal )
  {
    logger.fine( myPosn() + ": '" + oldVal + "' -> '" + newVal + "'" );
    
    Square $activeSqr = grid.getActiveSqr();
    int $activeRow = $activeSqr.getRowIndex();
    int $activeCol = $activeSqr.getColIndex();
    
    for( Square s : mySqrs )
    {
      logger.finer( "Sqr " + ( s == $activeSqr ? "=" : "!") + "= activeSqr" + "; type = " + s.getType() );
      
      if( (s != $activeSqr) // NOT the current active Square
          &&
          /* MUST EXCLUDE Zone Squares that OVERLAP those in the active Row & Col */
          !( (s.getRowIndex() == $activeRow) || (s.getColIndex() == $activeCol) )
        )
        /* continue the 'Set Value Chain' */
        s.adjustGroupSqrCounts( oldVal, newVal );
    }
    
  }// Zone.notifySqrsOfValChange()
  
  /**
   *  Adjust the possible vals of my Squares <br>
   *  - called by {@link #findLockedVals}
   *  @param own - Row or Col to exclude
   *  @param val - value to remove from Squares
   *  @return success or failure
   *  @see Square#removePossibleVal
   *  @see Group#setPossValsFromLockedVals
   */
  @Override
  boolean setPossValsFromLockedVals( final Group own, final int val )
  {
    boolean $interim = false, $result = false ;
    for( Square s : mySqrs )
      if( !( (s.getCol() == own) || (s.getRow() == own) ) )
      {
        $interim = s.removePossibleVal( val );
        if( ! $result )
          $result = $interim ;
      }
    
    return $result ;
    
  }// Zone.setPossValsFromLockedVals()
  
  /**
   *  See if the Locked vals in a {@link Zone} have a common {@link Row} or {@link Col} <br>
   *  - called by {@link #findLockedVals}
   *  @param sqr1 - index of first Square to check
   *  @param sqr2 - index of second Square to check
   *  @param sqr3 - index of third Square to check
   *  @return common Sub{@link Group} if any, or null
   *  @see Group#getCommonGroup
   */
  @Override
  Group getCommonGroup( final int sqr1, final int sqr2, final int sqr3 )
  {
    boolean $result = false ;
    
    Group g = mySqrs[sqr1].getRow();
    if( g == mySqrs[sqr2].getRow() )
    {
      $result = true ;
      if( (sqr3 > 0) && (g != mySqrs[sqr3].getRow()) )
        $result = false ;
    }
    
    if( $result )
      return g ;
    
    g = mySqrs[sqr1].getCol();
    if( g == mySqrs[sqr2].getCol() )
    {
      $result = true ;
      if( (sqr3 > 0) && (g != mySqrs[sqr3].getCol()) )
        $result = false ;
    }
    
    if( $result )
      return g ;
    
    logger.appendln( "\t\t $ NO Common Row or Col for Sqrs " + sqr1 + "," + sqr2 + (sqr3 > 0 ? ("," + sqr3) : "") );
    return null ;
    
  }// Zone.getCommonGroup()
  
}/* SUBCLASS Zone */
