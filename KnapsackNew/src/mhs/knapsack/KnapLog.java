/* ***************************************************************************************
 
   Mark Sattolo (epistemik@gmail.com)
 -----------------------------------------------
 $File: //depot/Eclipse/Java/workspace/KnapsackNew/src/mhs/knapsack/KnapLog.java $
 $Revision: #5 $
 $Change: 58 $
 $DateTime: 2011/02/02 11:56:15 $
 -----------------------------------------------
 
  mhs.latinsquare.LogController.java
  Eclipse version created Jan 23, 2008, 09h31
 
*************************************************************************************** */

package mhs.knapsack;

import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.*;

/**
 * Manage logging for the package
 * @author  Mark Sattolo
 * @version $Revision: #5 $
 */
class KnapLogManager
{
  /**
   * USUAL Constructor <br>
   * Set up my Logger and Handler(s) and initiate logging at the startup Level
   * @param level - initial log {@link Level} received from {@link KnapSack#KnapSack(String[])}
   * @see KnapLogger#getNewLogger
   * @see FileHandler
   * @see Handler#setFormatter
   */
  KnapLogManager( String level )
  {
    try
    {
      setLevel( Level.parse(level) );
    }
    catch( Exception e )
    {
      System.err.println( "Problem with parameter for initial Log Level: " + e.toString() );
      setLevel( DEFAULT_LEVEL );
    }
    
    // get Logger
    myLogger = KnapLogger.getNewLogger( getClass().getName() );
    
    try
    {
      /*
      xmlHandler = new FileHandler( LOG_SUBFOLDER + Launcher.PROJECT_NAME + LOG_ROLLOVER_SPEC + XML_LOGFILE_TYPE ,
                                    LOGFILE_MAX_BYTES, MAX_NUM_LOG_FILES );
      xmlHandler.setFormatter( new XMLFormatter() );
      //*/
      textHandler = new FileHandler( LOG_SUBFOLDER + KnapSack.PROJECT_NAME + LOG_ROLLOVER_SPEC + TEXT_LOGFILE_TYPE ,
                                     LOGFILE_MAX_BYTES, MAX_NUM_LOG_FILES );
    }
    catch( Exception e )
    {
      System.err.println( "FileHandler exception: " + e );
    }
    
    if( textHandler != null )
    {
      try
      {
        textHandler.setFormatter( new KnapFormatter() );
        
        // Send logger output to our FileHandler.
        myLogger.addHandler( textHandler );
      }
      catch( Exception e )
      {
        System.err.println( "textHandler exception: " + e );
      }
    }
    else
        System.err.println( "\t>> PROBLEM: textHandler is NULL!" );
    
    // Set the level of detail that gets logged.
    myLogger.setLevel( currentLevel );
    
  }// CONSTRUCTOR
  
  /** @return  private static {@link KnapLogger} <var>myLogger</var>  */
  protected KnapLogger getLogger() { return myLogger ; }
  
  /** @return  private static {@link Level} <var>currentLevel</var>  */
  protected Level getLevel() { return currentLevel ;}
  
  /** @param level - {@link java.util.logging.Level}  */
  protected void setLevel( Level level )
  {
    currentLevel = level ;
    intLevel = currentLevel.intValue();
  }
  
  /**
   * @param level - {@link java.util.logging.Level}
   * @return  at this Level or more
   */
  protected static boolean atLevel( Level level )
  {
    return intLevel <= level.intValue();
  }
  
  /** @return EXACTLY at {@link Level#SEVERE}  */
  static boolean severe() { return currentLevel.equals( Level.SEVERE ); }
  /** @return at {@link Level#WARNING} or lower  */
  static boolean warning() { return atLevel( Level.WARNING ); }
  /** @return at {@link Level#CONFIG} or lower  */
  static boolean config() { return atLevel( Level.CONFIG ); }
  /** @return at {@link Level#INFO} or lower  */
  static boolean info()   { return atLevel( Level.INFO ); }
  /** @return at {@link Level#FINE} or lower  */
  static boolean fine()   { return atLevel( Level.FINE ); }
  /** @return at {@link Level#FINER} or lower  */
  static boolean finer()  { return atLevel( Level.FINER ); }
  /** @return at {@link Level#FINEST} or lower  */
  static boolean finest() { return atLevel( Level.FINEST ); }
  
  /**
   * Increase the amount of information logged <br>
   *  - which means we must decrease the {@link Level} of <var>myLogger</var> <br>
   *  - wrap around when reach base level   
   * @return {@link Level} <var>currentLevel</var>
   */
  protected Level moreLogging()
  {
    if( intLevel == Level.FINEST.intValue() )
      intLevel = Level.SEVERE.intValue(); // wrap around to HIGHEST (least amount of logging) setting
    else
      if( intLevel == Level.CONFIG.intValue() )
        intLevel = Level.FINE.intValue(); // jump gap b/n CONFIG & FINE 
      else
          intLevel -= 100 ; // go down to a finer (more logging) setting
    
    currentLevel = Level.parse( Integer.toString(intLevel) );
    myLogger.setLevel( currentLevel );
    
    myLogger.severe( "Log level is NOW at " + currentLevel );
    
    return currentLevel ;
  
  }// LogControl.incLevel()
  
  String myname() { return getClass().getSimpleName(); }
  
  void listLoggers()
  {
    Enumeration<String> e = LogManager.getLogManager().getLoggerNames();
    while( e.hasMoreElements() )
      myLogger.appendln( e.nextElement() );
    myLogger.send( currentLevel );
  }
  
  void reportLevel()
  {
    myLogger.severe( "Current log level is " + currentLevel );
  }
  
  /** default value */
  static final int
                  MAX_NUM_LOG_FILES =   256 ,
                  LOGFILE_MAX_BYTES = ( 4 * 1024 * 1024 );
  
  /** default {@link Level} */
  static final Level
                    INIT_LEVEL = Level.SEVERE  , // Level to print initialization messages.
                 DEFAULT_LEVEL = Level.WARNING ; // If no value passed to Constructor from KnapSack.
  
  /** default Log name parameter */
  static final String
                     LOG_SUBFOLDER = "logs/" ,
                 LOG_ROLLOVER_SPEC = "%u-%g" ,
                  XML_LOGFILE_TYPE = ".xml"  ,
                 TEXT_LOGFILE_TYPE = ".log"  ;
  
  /** @see KnapLogger */
  private static KnapLogger myLogger ;
  
  /** @see FileHandler */
  private static FileHandler textHandler ;//, xmlHandler ;
  
  /** current {@link Level} */
  private static Level currentLevel ;
  
  /** integer value of {@link #currentLevel} */
  private static int intLevel ;

}// class KnapLogManager

/* ========================================================================================================================== */

/**
 *  Perform all the actual logging operations
 *  @author Mark Sattolo
 *  @see java.util.logging.Logger
 */
class KnapLogger extends Logger
{
 /*
  *             C O N S T R U C T O R S
  *****************************************************************************************************************/
   
  /**
   * USUAL constructor - just calls the super equivalent
   * @param name - may be <var>null</var>
   * @param resourceBundleName - may be <var>null</var>
   * @see Logger#Logger(String,String)
   */
  private KnapLogger( String name, String resourceBundleName )
  {
    super( name, resourceBundleName );
  }
  
 /*
  *              M E T H O D S
  *****************************************************************************************************************/
  
 // =============================================================================================================
 //                          I N T E R F A C E
 // =============================================================================================================
   
  /**
   * Allow other package classes to create a {@link Logger} <br>
   * - adds this new {@link Logger} to the {@link LogManager} namespace
   * @param name - identify the {@link Logger}
   * @return the <b>new</b> {@link Logger}
   * @see LogManager#addLogger(Logger)
   */
  protected static synchronized KnapLogger getNewLogger( String name )
  {
    KnapLogger mylogger = new KnapLogger( name, null );
    LogManager.getLogManager().addLogger( mylogger );
    return mylogger ;
    
  }// KnapLogger.getNewLogger()
  
  /**
   * Prepare and send a {@link LogRecord} with data from the log buffer
   * @param level - {@link Level} to log at
   */
  protected void send( Level level )
  {
    if( buffer.length() == 0 )
      return ;
    
    getCallerClassAndMethodName();
    LogRecord lr = getRecord( level, buffer.toString() );
    clean();
    
    sendRecord( lr );
  
  }// KnapLogger.send()
  
  /** Add data to the log buffer
   *  @param msg - data String */
  protected synchronized void append( String msg ) { buffer.append( msg ); }
  
  /** Add data to the log buffer with a terminating newline
   *  @param msg - data String */
  protected void appendln( String msg ) { append( msg + "\n" ); }
  
  /** Add a newline to the log buffer  */
  protected void appendnl() { append( "\n" ); }
  
  /** <b>Remove</b> <em>ALL</em> data in the log buffer  */
  protected void clean() { buffer.delete( 0, buffer.length() ); }
  
  /*/ for debugging  
  @Override
  public void log( LogRecord record )
  {
    System.out.println( "---------------------------------------------------" );
    System.out.println( "record Message is '" + record.getMessage() + "'" );
    System.out.println( "record Class caller is '" + record.getSourceClassName() + "'" );
    System.out.println( "record Method caller is '" + record.getSourceMethodName() + "'" );
    super.log( record );
  }
  //*/  
  
 // =============================================================================================================
 //                            P R I V A T E
 // =============================================================================================================
  
  /**
   * Provide a <b>new</b> {@link LogRecord} with Caller class and method name info
   * @param level - {@link Level} to log at
   * @param msg - info to insert in the {@link LogRecord}
   * @return the produced {@link LogRecord}
   */
  private LogRecord getRecord( Level level, String msg )
  {
    LogRecord lr = new LogRecord( (level == null ? KnapLogManager.DEFAULT_LEVEL : level), msg );
    lr.setSourceClassName( callclass );
    lr.setSourceMethodName( callmethod );
    return lr ;
    
  }// KnapLogger.getRecord()
  
  /**
   *  Actually send the {@link LogRecord} to the logging handler
   *  @param lr - {@link LogRecord} to send
   *  @see Logger#log(LogRecord)
   */
  private synchronized void sendRecord( LogRecord lr )
  {
    callclass  = null ;
    callmethod = null ;
    
    super.log( lr );
    
  }// KnapLogger.sendRecord()
  
  /**
   *  Get the name of the {@link Class} and <em>Method</em> that called {@link KnapLogger}
   *  @see Throwable#getStackTrace
   *  @see StackTraceElement#getClassName
   *  @see StackTraceElement#getMethodName
   */
  private void getCallerClassAndMethodName()
  {
    Throwable t = new Throwable();
    StackTraceElement[] elements = t.getStackTrace();
    
    if( elements.length < 3 )
      callclass = callmethod = strUNKNOWN ;
    else
    {
      callclass = elements[2].getClassName();
      callmethod = elements[2].getMethodName();
    }
  
  }// KnapLogger.getCallerClassAndMethodName()
  
 /*
  *            F I E L D S
  *****************************************************************************************************************/
  
  /** Class calling the Logger */
  private String callclass = null ;
  /** Method calling the Logger */
  private String callmethod = null ;
  
  /**
   *  Store info from multiple {@link KnapLogger#append} or {@link KnapLogger#appendln} calls <br>
   *  - i.e. do a 'bulk send'
   *  @see StringBuilder
   */
  private StringBuilder buffer = new StringBuilder( 1024 );
  
  /** default if cannot get method or class name  */
  static final String strUNKNOWN = "unknown" ;
  
}// class KnapLogger

/* ========================================================================================================================== */

/**
 *  Do all the actual formatting of {@link LogRecord}s for {@link KnapLogger}
 *  @author Mark Sattolo
 *  @see java.util.logging.Formatter
 */
class KnapFormatter extends Formatter
{
  /**
   *  Instructions on how to format a {@link LogRecord}
   *  @see Formatter#format
   */
  @Override
  public String format( LogRecord record )
  {
    return( record.getLevel() + rec + (++count) + nl + record.getSourceClassName() + sp
            + record.getSourceMethodName() + mi + nl + record.getMessage() + nl + nl         );
  }
  
  /**
   *  Printed at the beginning of a Log file
   *  @see Formatter#getHead
   */
  @Override
  public String getHead( Handler h )
  {
    return( head + DateFormat.getDateTimeInstance().format( new Date() ) + nl + div + nl + nl );
  }
  
  /**
   *  Printed at the end of a Log file
   *  @see Formatter#getTail
   */
  @Override
  public String getTail( Handler h )
  {
    return( div + nl + tail + DateFormat.getDateTimeInstance().format( new Date() ) + nl );
  }
  
  /** Number of times {@link KnapFormatter#format(LogRecord)} has been called  */
  private int count ;
  
  /** useful String constant */
  static String sp   = " "  ,
                nl   = "\n" ,
                mi   = "()" , // method indicator
                div  = "=================================================================" ,
                head = "KnapsackNew START" + nl ,
                rec  = ": KnapsackNew record #" ,
                tail = "KnapsackNew END" + nl   ;

}// Class KnapFormatter
