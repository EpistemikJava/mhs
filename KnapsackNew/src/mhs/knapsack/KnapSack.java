/* ***************************************************************************************

   Mark Sattolo (epistemik@gmail.com)
  -----------------------------------------------
     $File: //depot/Eclipse/Java/workspace/KnapsackNew/src/mhs/knapsack/KnapSack.java $
     $Revision: #6 $
     $Change: 58 $
     $DateTime: 2011/02/02 11:56:15 $
   
   git version created Mar 22, 2014.
  -----------------------------------------------
  
  Best-First Search with Branch-and-Bound Pruning Algorithm for the 0-1 Knapsack Problem
  
    see p.235 of "Foundations of Algorithms: with C++ pseudocode", 2nd Ed. 1998, 
        by Richard Neapolitan & Kumarss Naimipour, Jones & Bartlett, ISBN 0-7637-0620-5
  
  Problem: Let n items be given, where each item has a weight and a profit,
    			 and these weights and profits are positive integers.
    			 Furthermore, let a positive integer W (maximum weight) be given.
    			 Determine an optimal set of items, i.e. the group of items with maximum total profit,
    			 under the constraint that the sum of their weights cannot exceed W.
  
  Inputs: 1) A positive integer giving the maximum value permitted for the sum
             of the weights of all selected items.
          2) Name of a file which contains a list of items which can be chosen.
             The file contains one item per line arranged as follows: 
             
	        <string (item name)>'whitespace'<integer (profit)>'whitespace'<integer (weight)>
  
  Outputs: 1) An integer that is the sum of the profits of the optimal set.
           2) An integer that is the sum of the weights of the optimal set.
           3) A list of the names of all the items comprising the optimal set.

**************************************************************************************** */

package mhs.knapsack;

import java.io.* ;
import java.util.* ;
import java.util.logging.Level;

/**
 * The main class for the KnapSack application.
 * @see KnapNode
 * @see KnapItemList
 * @see KnapPriQue
 * @author MARK SATTOLO
 * @version $Revision: #6 $
 */
public class KnapSack
{
  /*    FIELDS
  ==================================================================================================== */
  
  static final int MAXINPUT = 1024 ;
  
  static final String STR_DEFAULT_LEVEL = "CONFIG" ;
  
  static final Level DEFAULT_LEVEL = Level.parse( STR_DEFAULT_LEVEL );
  
  static Level currentLevel = DEFAULT_LEVEL ;
  
  /** 
   * Logging management
   * @see KnapLogManager
   */
  protected static KnapLogManager logManager ;
  
  /**
   * Logging actions
   * @see KnapLogger
   */
  protected static KnapLogger logger ;
  
  // initial sort for itemList
  static final Comparator<Object> PWR_ORDER = new Comparator<Object>()
  {
    public int compare( Object o1, Object o2 )
    {
      KnapNode k1 = (KnapNode)o1 ;
      KnapNode k2 = (KnapNode)o2 ;
      
      // descending order
      if( k1.getPwr() < k2.getPwr() )
        return 1 ;
      
      if( k1.getPwr() > k2.getPwr() )
        return -1 ;
      
      return 0 ;
    }
  };
  
  /** project name  */
  public static final String PROJECT_NAME = "KnapsackNew" ;
  
  private int numItems = 0 ,
              maxWeight    ; // maximum allowed weight of items (user-supplied)
  
  private String fileName ; // file from user listing items with their profit and weight
  
  KnapNode bestItems ; // keep track of which items are used for maxProfit
  
  KnapItemList itemList ; // the initial items ranked by profit/weight ratio
  
  /*    Constructors
  ==================================================================================================== */
  
  /**
   * Default Constructor
   * 
   * @param args - from command line
   */
  public KnapSack( String args[] )
  {
    // INIT LOGGING
    logManager = new KnapLogManager( args.length >= 2 ? args[1] : STR_DEFAULT_LEVEL );
    if( logManager == null )
    {
      System.err.print( "\t>> KnapSack CONSTRUCTOR: COULD NOT create a KnapLogManager!" );
      System.exit( this.hashCode() );
    }
    
    currentLevel = logManager.getLevel();
    
    logger = logManager.getLogger();
    if( logger == null )
    {
      System.err.print( "\t>> KnapSack CONSTRUCTOR: COULD NOT create a KnapLogger!" );
      System.exit( this.hashCode() );
    }
    
    // CHECK COMMAND LINE PARAMETERS
    // TODO handle logging setup as well
    setup( args );
    
    logManager.listLoggers();
    logManager.reportLevel();
    
    logger.log( " File name is '" + fileName + "'\n Max Weight = " + maxWeight );
    
    // report loop protection
    logger.log( " LOOP_LIMIT = " + KnapItemList.LOOP_LIMIT );
  }
  
  /*    METHODS
  ==================================================================================================== */
  
	/**
	 * MAIN
	 * 
	 * @param args - from user
	 */
  public static void main( final String args[] )
  {
    new KnapSack( args ).go();
    
  }/* main() */
  
  /**
   * Check the command line parameters
   * 
   * @param params - from main() via the Constructor
   */
  private void setup( String params[] ) 
  { 
    // need the name of the file containing the items
    if( params.length < 1 )
    {
      System.err.println( "\n Usage: java " + this.getClass().getSimpleName() + " <items file> [log_level] [max weight]\n"
                          + "        (log_level = INFO, CONFIG or FINE\n)" );
      
      System.exit( this.hashCode() );
    }
    
    fileName = params[0] ;
    if( fileName == null )
    {
      System.err.println( "\t BAD file name = " + fileName + "\n" );
      System.exit( this.hashCode() );
    }
    
    // maximum weight
    if( params.length < 3 )
    {
      System.out.print( "\nEnter the maximum weight: " );
      maxWeight = getInputInteger();
    }  
    else
	      maxWeight = Integer.parseInt( params[2] );
    
    // maxWeight must be positive
    if( maxWeight < 1 )
    {
      System.out.println( "Max Weight must be a positive integer!\n" );
      System.exit( maxWeight-1 );
    }
    
    System.out.println( "setup() OK \n" );
    
  }// setup()
  
  /**
   *  Run the KnapSack program
   */
  private void go()
  {
    itemList = new KnapItemList( logger );
    bestItems = new KnapNode( "" );
    
    if( ! getFileData() )
      System.exit( numItems );
    
    itemList.log( "initial" ); // the original vector
    
    // sort the KnapItemList by profit/weight ratio - descending order
    Collections.sort( itemList, PWR_ORDER );
    
    // display the sorted items
    logger.severe( "There are " + numItems + " items in the KnapItemList." );
    itemList.log( "Sorted by p/w" );
    
    /* RUN THE ALGORITHM */
    itemList.bestFirstSearch( bestItems, maxWeight );
    
    // display the results
    logger.severe( " For Weight limit " + maxWeight + ": Max Profit = " + bestItems.getProfit() 
                   + " (weight of items = " + bestItems.getWeight() + ")" 
                   + "\n Best items are: " + bestItems.getName() );
    
    logger.severe( "*** PROGRAM ENDED ***" );
  
  }// go()
  
  /**
   * Open the user-specified file and parse the data
   * 
   * @return boolean indicating if data was retrieved without problem
   */
  private boolean getFileData()
  {
    int p=0, w=0 ; // temps for profit and weight from file data
    String item = new String(); // temp for item name(s)
    Reader r = null ;
    
    // open the file and get data
    try
    {
      r = new BufferedReader( new FileReader(fileName) );
      StreamTokenizer st = new StreamTokenizer( r );
      
      while( st.nextToken() != StreamTokenizer.TT_EOF )
      {
        if( st.ttype == StreamTokenizer.TT_WORD )
          item = st.sval ;
        else
            throw new Exception( "Problem finding name of item #" + (numItems+1) );
        
        st.nextToken();
        if( st.ttype == StreamTokenizer.TT_NUMBER )
          p = (int)st.nval ;
        else
            throw new Exception( "Problem finding profit of item #" + (numItems+1) );
        
        st.nextToken();
        if( st.ttype == StreamTokenizer.TT_NUMBER )
          w = (int)st.nval ;
        else
            throw new Exception( "Problem finding weight of item #" + (numItems+1) );
        
        // create a new KnapNode and load it into the KnapItemList
        itemList.add( new KnapNode(item, p, w) );
        
        itemList.get( numItems ).log( logger, Level.FINE, "\nNode #" + (numItems+1) + "\n---------------------------------\n" );
        logger.finer( "Name is " + item + " ; Profit is " + p + " ; Weight is " + w );
        
        numItems++ ; // total number of items in the file
      
      }// while
      
      assert numItems == itemList.size()
            :( "numItems (" + numItems + ") != itemList.size (" + itemList.size() + ")" );
      
      logger.severe( "There were " + numItems + " items in file '" + fileName + "'" );
      
    }
    catch( FileNotFoundException f )
    {
      logger.severe( "\t>> Find file problem: " + f.toString() );
      return false ;
    }
    catch( Exception e )
    {
      logger.severe( "\t>> Get file data problem: " + e.toString() );
      return false ;
    }
    finally
    {
      try { if( r != null ) r.close(); }
      catch( Exception e ) { logger.severe( "\t?? Could NOT close the file: " + e.toString() ); }
    }
    
    return true ;
    
  }// getFileData()
  
  /**
   * get user String
   * 
   * @return input String
   */
  static String getInputString()
  {
    byte response[] = new byte[ MAXINPUT ];
    
    try
    {
      System.in.read( response );
    }
    catch( Exception e )
    {
      logger.severe( e.toString() );
    }
    
    return new String( response ).trim();
    
  }// getInputString()
  
  /**
   * get user integer
   * 
   * @return input int
   */
  static int getInputInteger()
  {
    return Integer.parseInt( getInputString() );
    
  }// getInputInteger()
  
}/* class KnapSack */

/* *************************************************************************************************************** */

/**
 * A collection of {@link KnapNode}s, ordered by highest profit-to-weight ratio,
 * used to store the initial list of items received by {@link KnapSack}.
 * 
 * @author MARK SATTOLO
 * @version $Revision: #6 $
 */
@SuppressWarnings( "serial" )
class KnapItemList extends Vector<KnapNode>
{
  /**
   * Constructor with Logger
   * 
   * @param $logger - {@link KnapLogger} to use
   */
  public KnapItemList( KnapLogger $logger )
  {
    logger = $logger ;
  }
  
  /*    METHODS
  ==================================================================================================== */
  
  /**
   * Verify the requested index and if good then return the item at that position, otherwise {@link System#exit(int)}
   * 
   * @param index - place in the list of the KnapNode to return
   * @return {@link KnapNode}
   */
  KnapNode getItem( int index )
  {
    if( index < 0  || index >= size() )
    {
      logger.severe( "Requested index (" + index + ") NOT VALID! My size = " + size() + ".\n" );
      System.exit( this.hashCode() );
    }
    
    return get( index );
    
  }// getItem()
  
  /**
   * Find the optimal set of items for the given maximum weight
   * 
   * @param bestItems - store the optimal set
   * @param maxWeight - weight restriction
   */
  void bestFirstSearch( KnapNode bestItems, int maxWeight )
  {
    int count        =  1 , // # of times through the while loop
        totalProfit  =  0 ,
        sumOfWeights =  0 ;
    
    sst = new KnapPriQue();
    
    // initial values for the top node
    topNode = new KnapNode( START_STRING, INIT );
    
    // calculate the initial bound
    topNode.calculateBound( this, maxWeight, "Root", logger );
    topNode.log( logger, Level.FINE, "Root node =" );
    
    // put the root node on the priority queue
    sst.add( topNode );
    sst.log( logger, Level.INFO, "Initial" );
    
    logger.severe( " START WHILE LOOP..." );
    
    String name ;
    /* loop through the state space tree */
    while( !sst.isEmpty() && count < LOOP_LIMIT ) // LOOP_LIMIT prevents a runaway loop
    { 
      logger.info( " count = " + count );
      
      sst.log( logger, Level.FINE, " while" );
      
      topNode = sst.remove( TOP ); // remove node with best bound
      topNode.log( logger, Level.FINE, " BFS(topNode):" );
      
      if( topNode.getBound() > totalProfit ) // check if this node is promising
      {
        logger.finer( " topNode.bound = " + topNode.getBound() );
        
        // want to get the next item in the item list that this node hasn't seen yet
        topNode.incIndex( 1 );
        
        name = topNode.getName();
        
        /* check the Node that DOES NOT INCLUDE the next item from the item list */
        
        topNode.addName( EXCLUDE_STRING );
        topNode.log( logger, Level.INFO, " BFS(top-):" );
        
        topNode.calculateBound( this, maxWeight, "topNode-Exclude", logger );
        if( topNode.getBound() > totalProfit ) // if this node is promising
          sst.add( new KnapNode(topNode) ); // re-insert into sst
        
        /* check the Node that INCLUDES the next item from the list */
        
        nextItemNode = getItem( topNode.getIndex() );
        nextItemNode.log( logger, Level.FINE, " BFS(nextItemNode):" );
        
        topNode.setName( name + INCLUDE_STRING + nextItemNode.getName() );
        topNode.setWeight( topNode.getWeight() + nextItemNode.getWeight() ); 
        topNode.setProfit( topNode.getProfit() + nextItemNode.getProfit() );
        topNode.log( logger, Level.INFO, " BFS(top+nextItem): " );
        
        // check the updated total profit
        if( (topNode.getWeight() <= maxWeight) && (topNode.getProfit() > totalProfit) )
        {
          totalProfit  = topNode.getProfit();
          sumOfWeights = topNode.getWeight();
          bestItems.setName( topNode.getName() ); // keep track of overall list of best items
          
          logger.severe( "BFS(" + count + "): totalProfit now = " + totalProfit 
                         + "\t current best items are " + bestItems.getName() 
                         + "\t current weight of items is " + sumOfWeights );
          
          sst.log( logger, Level.INFO, (sst.isEmpty() ? "sst is EMPTY" : "New best item") );
        }
        
        topNode.calculateBound( this, maxWeight, " top+nextItem", logger );
        if( topNode.getBound() > totalProfit ) // see if this node is promising
          sst.add( topNode );
        
      } // if( topNode.getBound() > totalProfit )
      else
      {
        logger.severe( " >> NO more promising nodes in sst!" );
        break ;
      }
      
      count++ ;
    
    }// while( !sst.isEmpty() && count < LOOP_LIMIT )
    
    if( count >= LOOP_LIMIT )
      logger.severe( "WARNING: REACHED LIMIT OF SEARCH LOOP = " + count + "!" );
    else
        logger.info( " Num times through loop = " + count );
    
    bestItems.setWeight( sumOfWeights );
    bestItems.setProfit( totalProfit );
  
  }// bestFirstSearch()
  
  /**
   * Log each {@link KnapNode} in the list
   * 
   * @param lev - level to print at
   * @param s - extra info to print
   */
  public void log( Level lev, String s )
  {
    logger.appendln( " KnapItemList '" + s + "': " );
    
    for( int j=0; j < size(); j++ )
    {
      logger.append( " #" );
      if( j < 9 )
        logger.append( " " );
      logger.append( (j+1) + " " );
      logger.append( get(j).display() );
    }
    logger.appendnl();
    
    logger.send( lev );
    
  }// log()
  
  /**
   * Log each {@link KnapNode} in the list
   * 
   * @param s - extra info to print
   */
  public void log( String s )
  {
    log( KnapSack.currentLevel, s );
  }
  
  /*    FIELDS
  ==================================================================================================== */
  
  static final int INIT = -1 , // after increment, the initial index into the item list will be 0
                    TOP =  0 ; // get the top (highest priority) item from the state space tree
  
  static final String   START_STRING = ">" ,
                      INCLUDE_STRING = "&" ,
                      EXCLUDE_STRING = "-" ;
  
  static final int LOOP_LIMIT = 1024 ;
  
  /**
   * Logging actions
   * @see KnapLogger
   */
  protected static KnapLogger logger ;
  
  // state space tree containing nodes with the various combinations of items
  private KnapPriQue sst ;
  
  // working nodes
  private KnapNode topNode, nextItemNode ;
  
}/* class KnapItemList */
