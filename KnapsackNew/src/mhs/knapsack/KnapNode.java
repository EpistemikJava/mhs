/* *****************************************************************************

    Mark Sattolo (epistemik@gmail.com)
   -----------------------------------------------
     $File: //depot/Eclipse/Java/workspace/KnapsackNew/src/mhs/knapsack/KnapNode.java $
     $Revision: #7 $
     $Change: 58 $
     $DateTime: 2011/02/02 11:56:15 $

****************************************************************************** */

package mhs.knapsack;

import java.util.logging.Level;

/**
 * A single <code>KnapNode</code> can contain a single item from the {@link KnapItemList}
 * or can represent many items grouped together, as in the state space tree
 * used in {@link KnapItemList#bestFirstSearch(KnapNode,int)}
 * @author MARK SATTOLO
 * @version $Revision: #7 $
 */
public class KnapNode // implements Comparable
{	
  /*    Fields
  ==================================================================================================== */
  
  private  String  $items  ;
  private  int     $profit ;
  private  int     $weight ;
  private  double  $pwr    ; // profit to weight ratio
  private  int     $index  ; // in the item list of the last item seen (also == level in state space tree)
  private  double  $bound  ; // potential maximum profit of each node
  
  /*    Constructors
  ==================================================================================================== */
  
  /**
   * Constructor with name of item(s)
   * @param s - item name(s)
   */
  public KnapNode( String s )
  {
    $items  = s ;
    $index  = 0 ;
    $profit = $weight = 0 ;
    $pwr    = $bound  = 0.0 ;
  };
	
  /**
   * Constructor with name of item(s) and index
   * @param s - item name(s)
   * @param i - index
   */
  public KnapNode( String s, int i )
  {
    $items  = s ;
    $index  = i ;
    $profit = $weight = 0 ;
    $pwr    = $bound  = 0.0 ;
  };
	
  /**
   * Constructor with name of item(s), profit & weight
   * @param s - item name(s)
   * @param p - profit
   * @param w - weight
   */
  public KnapNode( String s, int p, int w )
  {
    $items  = s ;
    $index  = 0 ;
    $profit = p ;
    $weight = w ;
    $pwr    = (w > 0) ? ( (double)p / (double)w ) : 0.0 ;
    $bound  = 0.0 ;
  };
  
  /**
   * Copy Constructor
   * @param k - KnapNode to copy
   */
  public KnapNode( KnapNode k )
  {
    $items  = k.getName() ;
    $index  = k.getIndex() ;
    $profit = k.getProfit() ;
    $weight = k.getWeight() ;
    $pwr    = k.getPwr() ;
    $bound  = k.getBound() ;
  };
  
  /*    METHODS
  ==================================================================================================== */
  
  String  getName() { return $items ; };
  void    setName( String s ) { $items = s ; };
  void    addName( String s ) { $items = $items.concat(s); };
  
  int	    getIndex() { return $index ; };
  void    setIndex( int i ) { $index = i ; };
  void    incIndex( int ii ) { $index += ii ; };
  
  int	    getProfit() { return $profit ; };
  void    setProfit( int p ) { $profit = p ; };
  
  int	    getWeight() { return $weight ; };
  void    setWeight( int w ) { $weight = w ; };
  
  double  getPwr() { return $pwr ; };
  void    setPwr( double r ) { $pwr = r ; };
  
  double  getBound() { return $bound ; };
  void    setBound( double b ) { $bound = b ; };
  
  // natural ordering method if implementing Comparable interface
  int compareTo( KnapNode k )
  {
    // descending order
    if( $pwr < k.getPwr() )
      return 1 ;

    if( $pwr > k.getPwr() )
      return -1 ;

    return 0 ;
  }
  
  /**
   * Calculate the bound for this {@link KnapNode} in the given {@link KnapItemList}
   * @param items - other KnapNodes to select from
   * @param maxWeight - restriction specified by user
   * @param label - info
   * @param lgr - function logger
   */
  void calculateBound( KnapItemList items, int maxWeight, String label, KnapLogger lgr )
  {
    int index ,     // of next item in itemList
        totweight ; // of items in the bound
    
    double result ;
    int numItems = items.size();
    KnapNode tempnode = new KnapNode( "" );
    
    lgr.info( " INSIDE bound(" + label + "):" );
    log( lgr, Level.FINE, "\tIncoming node =" );
    items.log( Level.FINER, "\tinitial bound() nodes" );
    
    if( $weight >= maxWeight ) 
    {
      // this item's weight is greater than allowed maximum
      result = 0.0 ;
      lgr.info( "\tWeight of '" + $items + "' exceeds maximum... exiting!" );
    }
    else // do the calculation
    {
      result = $profit;
      totweight = $weight;
      
      // get to the proper level of itemList
      index = $index + 1 ;
      lgr.finer( "\tbound(): index = " + index );
      
      // grab as many items as possible     
      while( index < numItems  &&  (tempnode = items.getItem(index)) != null )
      {
        if( totweight + tempnode.getWeight() > maxWeight )
          break ;
        
        totweight += tempnode.getWeight();
        result += tempnode.getProfit();
        index++ ;
        
        lgr.fine("\tbound(#" + index + "): prelim bound = " + result);
      }
      
      lgr.finer( "\tbound(): now, index = " + index );
      
      if( index < numItems  &&  tempnode != null )
        result += ( (maxWeight - totweight) * tempnode.getPwr() );
        // grab fraction of next item to calculate full potential of this node
      
      lgr.info( String.format(" bound(4): node %s has bound = %9.3f", $items, result) );
    }
    
    $bound = result ;
  
  }// calculateBound()
  
  /**
   * Log info for a node
   * @param k - logger to print to
   * @param lev - level to print at
   * @param s - extra info
   * 
   */
  public void log( KnapLogger k, Level lev, String s )
  {
    k.log( lev, s + display() );
    
  }// log()
  
  /**
   * String description of a node
   * @return String with info
   */
  public String display()
  {
    return String.format( "%1$16s: indx %2$3d ; prof %3$6d ; wt %4$5d ; bnd %5$9.3f ; pwr %6$7.3f \n",
                           $items, $index,      $profit,     $weight,   $bound,       $pwr );
  }// display()
  
}/* class KnapNode */
