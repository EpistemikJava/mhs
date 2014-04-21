/* ***************************************************************************************
 
   Mark Sattolo (epistemik@gmail.com)
 -----------------------------------------------
 $File: //depot/Eclipse/Java/workspace/Pseudokeu/src/mhs/pseudokeu/LogControl.java $
 $Revision: #11 $
 $Change: 174 $
 $DateTime: 2012/02/21 20:28:23 $
 -----------------------------------------------
 
  mhs.latinsquare.LogController.java
  Eclipse version created Jan 23, 2008, 09h31
  git version created Mar 8, 2014
 
*************************************************************************************** */

package mhs.pseudokeu;

import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

/**
 * Manage logging for the package
 * 
 * @author Mark Sattolo
 * @version 8.4
 */
public class LogControl
{
  /**
   * USUAL Constructor <br>
   * Set up my Logger, Handler(s) and Formatter(s) and initiate logging
   * 
   * @param lev - initial log {@link Level} received from {@link Launcher#Launcher(String,boolean)}
   * 
   * @see PskLogger#getNewLogger
   * @see FileHandler
   * @see Logger#addHandler(Handler)
   */
  public LogControl( final String lev )
  {
    try
    {
      setLevel( Level.parse(lev) );
    }
    catch( Exception e )
    {
      System.err.println( "\n>> Problem with parameter for initial Log Level: " + e.toString() );
      setLevel( DEFAULT_LEVEL );
    }
    
    // package Logger
    myLogger = PskLogger.getNewLogger( PskLogger.myname() );

    // Generally only want to output to file for DEBUG mode
    if( Launcher.DEBUG )
    {
      setHandlers();
      
      myLogger.addHandler( xmlHandler );
      myLogger.addHandler( textHandler );
    }
    else // need a handler for the package logger
    {
      myLogger.addHandler( new ConsoleHandler() );
      myLogger.getHandlers()[0].setFormatter( new PskFormatter() );
    }
    
    // set the level of detail that gets logged
    myLogger.setLevel( currentLevel );
    
    // need to set up root Logger to display properly to Console
    setRootLogger();
    
    myLogger.severe( "STARTING log level is " + currentLevel );
    
  }// CONSTRUCTOR
  
  /**
   * Set up my Handler(s) and Formatter(s) <br>
   *   - called by {@link LogControl#LogControl(String)}
   * 
   * @see FileHandler
   * @see Formatter
   * @see Handler#setFormatter
   */
  private void setHandlers()
  {
    // xml file handler
    try
    {
      xmlHandler = new FileHandler( LOG_SUBFOLDER + Launcher.PROJECT_NAME + LOG_ROLLOVER_SPEC + XML_LOGFILE_TYPE,
                                    LOGFILE_MAX_BYTES, MAX_NUM_LOG_FILES );
      xmlHandler.setFormatter( new XMLFormatter() );
    }
    catch( Exception e )
    {
      System.err.println( "xmlHandler exception: " + e );
    }
    //*/
    
    // text file handler
    try
    {
      textHandler = new FileHandler( LOG_SUBFOLDER + Launcher.PROJECT_NAME + LOG_ROLLOVER_SPEC + TEXT_LOGFILE_TYPE,
                                     LOGFILE_MAX_BYTES, MAX_NUM_LOG_FILES );
      textHandler.setFormatter( new PskFormatter() );
    }
    catch( Exception e )
    {
      System.err.println( "textHandler exception: " + e );
    }
    //*/
    
  }// setHandlers()
  
  /**
   * Set up the root Logger, which handles Console messages <br>
   *   - called by {@link LogControl#LogControl(String)}
   * 
   * @see LogManager#getLogger
   * @see Logger#getHandlers
   * @see Handler#setFormatter
   */
  private void setRootLogger()
  {
    rootLogger = LogManager.getLogManager().getLogger( "" );
    
    try
    {
      Handler[] arH = rootLogger.getHandlers();
      if( arH.length == 0 )
      {
        System.err.println( "\n root Logger has NO handlers!" );
        
        //rootLogger.addHandler( new ConsoleHandler() );
        //System.out.println( "\n >> SET UP a new ConsoleHandler." );
      }
      
      for( Handler h : arH )
      {
        // send root Logger output to a PskFormatter
        h.setFormatter( new PskFormatter() );
        
        // increase the level of the root handlers so ALL messages from PskLogger will be seen
        h.setLevel( Level.ALL );
      }
      
      rootLogger.setLevel( currentLevel );
    }
    catch( Exception e )
    {
      System.err.println( "rootLogger exception: " + e );
    }
    
  }// setRootLogger()
  
  /**
   *  @return {@link #myLogger}
   */
  PskLogger getLogger() { return myLogger ; }
  
  /**
   *  @return {@link #currentLevel} 
   */
  Level getLevel() { return currentLevel ; }
  
  /** @param lev - {@link java.util.logging.Level}   */
  void setLevel( final Level lev )
  {
    currentLevel = lev;
    intLevel = currentLevel.intValue();
  }
  
  /**
   *  @param lev - {@link java.util.logging.Level}
   *  @return true if at this Level or lower (i.e. messages of this Level will be logged) 
   */
  static boolean atLevel( final Level lev )
  {
    return intLevel <= lev.intValue();
  }
  
  /** @return EXACTLY at {@link Level#SEVERE} */
  static boolean severe()  { return currentLevel.equals( Level.SEVERE ); } // 1000
  /** @return at {@link Level#WARNING} or lower */
  static boolean warning() { return atLevel( Level.WARNING ); }            //  900
  /** @return at {@link Level#INFO} or lower */
  static boolean info()    { return atLevel( Level.INFO ); }               //  800
  /** @return at {@link Level#CONFIG} or lower */
  static boolean config()  { return atLevel( Level.CONFIG ); }             //  700
  /** @return at {@link Level#FINE} or lower */
  static boolean fine()    { return atLevel( Level.FINE ); }               //  500
  /** @return at {@link Level#FINER} or lower */
  static boolean finer()   { return atLevel( Level.FINER ); }              //  400
  /** @return at {@link Level#FINEST}, or <em>lower (i.e custom Level)</em> */
  static boolean finest()  { return atLevel( Level.FINEST ); }             //  300
  
  /**
   * Change the amount of information logged <br>
   *   - which means we must decrease or increase the {@link Level} of {@link #myLogger} <br>
   *   - wrap around when reach base or top level
   * 
   * @param more - increase the amount of logging if <em>true</em>, decrease if <em>false</em>
   * @return {@link #currentLevel}
   */
  Level changeLogging( final boolean more )
  {
    int $diff = 100 ;
    if( more )
    {
      if( intLevel == Level.FINEST.intValue() )
        intLevel = Level.SEVERE.intValue(); // wrap around to HIGHEST (least amount of logging) setting
      else if( intLevel == Level.CONFIG.intValue() )
          intLevel = Level.FINE.intValue(); // jump gap b/n CONFIG & FINE
      else
          intLevel -= $diff ; // go down to a finer (more logging) setting
    }
    else // less logging
    {
      if( intLevel == Level.SEVERE.intValue() )
        intLevel = Level.FINEST.intValue(); // wrap around to LOWEST (most logging) setting
      else if( intLevel == Level.FINE.intValue() )
          intLevel = Level.CONFIG.intValue(); // jump gap b/n CONFIG & FINE
      else
          intLevel += $diff ; // go down to a finer (more logging) setting
    }

    currentLevel = Level.parse( Integer.toString(intLevel) );
    myLogger.setLevel( currentLevel );
    
    /* CANNOT increase the level of the root Logger without getting a lot of UNWANTED LOGGING from other java components */
    //rootLogger.setLevel( currentLevel );
    
    myLogger.severe( "Log level is NOW at " + currentLevel );
    
    return currentLevel ;
    
  }// LogControl.changeLogging()
  
 // DEBUGGING
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  String myname()
  { return getClass().getName(); }
  
  /**
   *  Display list of registered {@link Logger}s
   *  
   *  @see LogManager#getLoggerNames
   */
  static void showLoggers()
  {
    System.out.println( "\n Currently registered Loggers:" );
    for( Enumeration<String> e = LogManager.getLogManager().getLoggerNames(); e.hasMoreElements(); )
    {
      System.out.println( '\t' + e.nextElement() );
    }

    System.out.println( ">>> END OF LOGGER LIST." );
    
  }// LogControl.showLoggers()
  
  /**
   *  Display information about active {@link Logger}s
   *  
   *  @see LogControl#loggerInfo(Logger,String)
   *  @see LogManager#getLogger(String)
   */
  static void checkLogging()
  {
    // package logger
    loggerInfo( myLogger, "package" );
    
    // root logger
    loggerInfo( rootLogger, "root" );
    
    // check the global logger too - just in case
    Logger $globLogr = LogManager.getLogManager().getLogger( "global" );
    loggerInfo( $globLogr, "global" );
    
    System.out.println();
    
  }// LogControl.checkLogging()
  
  /**
   * Display the <var>name</var>, {@link Level}, {@link Handler}s, and {@link Formatter}s of the submitted {@link Logger}
   * 
   * @param lgr - logger to query
   * 
   * @see Logger#getHandlers()
   * @see Handler#getFormatter()
   */
  private static void loggerInfo( final Logger lgr, final String name )
  {
    if( lgr == null )
    {
      System.err.println( "LogControl.loggerInfo: passed Logger is null!" );
      return ;
    }
    
    System.out.println( "\n" + name + " = '" + lgr.getName() + "' at Level '" + lgr.getLevel() + "'" );
    
    Handler[] $handlerAr = lgr.getHandlers();
    System.out.println( "Have " + $handlerAr.length + " handlers:" );
    for( Handler h : $handlerAr )
    {
      Formatter f = h.getFormatter();
      Level l = h.getLevel();
      System.out.println( "Handler " + h.getClass().getName() + " has Formatter '"
                          + f.getClass().getName() + "' & Level = " + l.getName() );
    }
  }// LogControl.loggerInfo()
  
 // 
 //    FIELDS
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** default value */
  static final int MAX_NUM_LOG_FILES = 128,
                   LOGFILE_MAX_BYTES = 512 * 1024 ;
  
  /** {@link Level} to print initialization messages = Level.SEVERE */
  static final Level   INIT_LEVEL = Level.SEVERE ;
      
  /** default {@link Level} if no value passed to Constructor from Launcher = Level.WARNING */
  static final Level   DEFAULT_LEVEL = Level.WARNING ;
  
  /** default Log name parameter */
  static final String   LOG_SUBFOLDER = "logs/" ,
                    LOG_ROLLOVER_SPEC = "%u-%g" ,
                     XML_LOGFILE_TYPE = ".xml"  ,
                    TEXT_LOGFILE_TYPE = ".log"  ;
  
  /** @see PskLogger */
  private static PskLogger myLogger ;
  
  /**
   * Can access the ConsoleHandler via the root Logger <br>
   *   - all our logging is sent to a default ConsoleHandler <br>
   *   - because of the setting in logging.properties (??) <br>
   *   - even though there is no ConsoleHandler registered with PskLogger ... <br>
   *   - the root Logger is also sending info from several other components, e.g. drawing,<br>
   *     &nbsp;&nbsp;so CANNOT increase the level of the root Logger without getting a lot of <b>unwanted logging</b>
   * @see Logger 
   * @see ConsoleHandler
   */
  private static Logger rootLogger ;
  
  /** @see FileHandler */
  private static FileHandler textHandler, xmlHandler ;
  
  /** current {@link Level} */
  private static Level currentLevel ;
  
  /** integer value of {@link #currentLevel} */
  private static int intLevel ;
  
}// class LogControl

/* ========================================================================================================================= */

/**
 * Perform all the Pseudokeu logging operations
 * 
 * @author Mark Sattolo
 * @see java.util.logging.Logger
 */
class PskLogger extends Logger
{
 /*
  *        C O N S T R U C T O R S
  ************************************************************************************************************ */
  
  /**
   * USUAL constructor - just calls the super equivalent
   * 
   * @param name - may be <var>null</var>
   * @param resourceBundleName - may be <var>null</var>
   * 
   * @see Logger#Logger(String,String)
   */
  private PskLogger( final String name, final String resourceBundleName )
  {
    super( name, resourceBundleName );
  }
  
 /*
  *        M E T H O D S
  *********************************************************************************************************** */
  
  // ===========================================================================================================
  //  I N T E R F A C E
  // ===========================================================================================================
  
  /**
   * Allow other package classes to create a {@link Logger} <br>
   *   - register this new {@link Logger} with the {@link LogManager}
   *   
   * @param name - identify the {@link Logger}
   * @return the <b>new</b> {@link Logger}
   * 
   * @see LogManager#addLogger(Logger)
   */
  protected static synchronized PskLogger getNewLogger( final String name )
  {
    PskLogger $logger = new PskLogger( name, null );
    LogManager.getLogManager().addLogger( $logger );
    
    return $logger;
    
  }// PskLogger.getNewLogger()

  /**
   * Prepare and send a customized {@link LogRecord} for an <em>initialization</em> method
   * 
   * @param msg - the text to insert in the {@link LogRecord}
   */
  protected void logInit( final String msg )
  {
    if( ( callclass == null ) || ( callmethod == null ) )
    {
      getCallerClassAndMethodName();
    }
    
    LogRecord $logRec = getRecord( LogControl.INIT_LEVEL, "<INIT> " + msg );
    
    sendRecord( $logRec );
    
  }// PskLogger.logInit(String)
  
  /**
   *  Prepare and send a basic {@link LogRecord} for an initialization method 
   */
  protected void logInit()
  {
    getCallerClassAndMethodName();
    logInit( "basic" );
    
  }// PskLogger.logInit()
  
  /**
   * Prepare and send a {@link LogRecord} with data from {@link PskLogger#buffer}
   * 
   * @param level - {@link Level} to log at
   */
  protected void send( final Level level )
  {
    if( buffer.length() == 0 )
      return ;
    
    getCallerClassAndMethodName();
    LogRecord $logRec = getRecord( level, buffer.toString() );
    clean();
    
    sendRecord( $logRec );
    
  }// PskLogger.send()
  
  /**
   * Add data to {@link PskLogger#buffer}
   * 
   * @param msg - data String
   */
  protected synchronized void append( final String msg )
  { buffer.append( msg ); }
  
  /**
   * Add data to {@link PskLogger#buffer} with newline
   * 
   * @param msg - data String
   */
  protected void appendln( final String msg )
  { append( msg + "\n" ); }
  
  /**
   *  Add newline to {@link PskLogger#buffer}
   */
  protected void appendln()
  { append( "\n" ); }
  
  /**
   *  <b>Remove</b> <em>ALL</em> data in {@link PskLogger#buffer} 
   */
  protected void clean()
  { buffer.delete( 0, buffer.length() ); }
  
 // DEBUGGING
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  static String myname() { return "mhs.pseudokeu.PskLogger"; }
  
  /*/
  @Override
  void log( LogRecord record )
  {
    System.out.println( "---------------------------------------------------" );
    System.out.println( "record Message is '" + record.getMessage() + "'" );
    System.out.println( "record Class caller is '" + record.getSourceClassName() + "'" );
    System.out.println( "record Method caller is '" + record.getSourceMethodName() + "'" );
    super.log( record );
  }
  //*/
  
 // ===========================================================================================================
 //  P R I V A T E
 // ===========================================================================================================
  
  /**
   * Provide a <b>new</b> {@link LogRecord} with Caller class and method name info
   * 
   * @param level - {@link Level} to log at
   * @param msg - info to insert in the {@link LogRecord}
   * 
   * @return the produced {@link LogRecord}
   */
  private LogRecord getRecord( final Level level, final String msg )
  {
    LogRecord $logRec = new LogRecord( ( level == null ? LogControl.DEFAULT_LEVEL : level ), msg );
    $logRec.setSourceClassName( callclass );
    $logRec.setSourceMethodName( callmethod );
    
    return $logRec;
    
  }// PskLogger.getRecord()
  
  /**
   * Actually send the {@link LogRecord} to the logging handler
   * 
   * @param rec - {@link LogRecord} to send
   * @see Logger#log(LogRecord)
   */
  private synchronized void sendRecord( LogRecord rec )
  {
    callclass = null ;
    callmethod = null ;
    
    super.log( rec );
    
  }// PskLogger.sendRecord()
  
  /**
   * Get the name of the {@link Class} and <em>Method</em> that called {@link PskLogger}
   * 
   * @see Throwable#getStackTrace
   * @see StackTraceElement#getClassName
   * @see StackTraceElement#getMethodName
   */
  private void getCallerClassAndMethodName()
  {
    Throwable $t = new Throwable();
    StackTraceElement[] $elementAr = $t.getStackTrace();
    
    if( $elementAr.length < 3 )
    {
      callclass = callmethod = strUNKNOWN ;
    }
    else
    {
      callclass = $elementAr[2].getClassName();
      callmethod = $elementAr[2].getMethodName();
    }
    
  }// PskLogger.getCallerClassAndMethodName()
  
  /*
   *        F I E L D S
   ************************************************************************************************************ */
  
  /** Class calling the Logger */
  private String callclass = null ;
  
  /** Method calling the Logger */
  private String callmethod = null ;
  
  /**
   * Store info from multiple {@link PskLogger#append} or {@link PskLogger#appendln} calls <br>
   *   - i.e. do a 'bulk send'
   *   
   * @see StringBuilder
   */
  private StringBuilder buffer = new StringBuilder( 1024 );
  
  /** default if cannot get method or class name */
  static final String strUNKNOWN = "unknown" ;
  
}// class PskLogger
  
/* ========================================================================================================================= */
  
/**
 * Do all the actual {@link LogRecord} formatting for {@link PskLogger}
 * 
 * @author Mark Sattolo
 * @see java.util.logging.Formatter
 */
class PskFormatter extends Formatter
{
  /**
   * Instructions on how to format a Pseudokeu {@link LogRecord}
   * 
   * @see Formatter#format
   */
  @Override
  public String format( LogRecord rec )
  {
    return ( rec.getLevel() + REC + ( ++count ) + NLN + rec.getSourceClassName()+ SPC 
             + rec.getSourceMethodName() + NLN + rec.getMessage() + NLN + NLN );
  }
  
  /**
   * Printed at the beginning of a Pseudokeu Log file
   */
  @Override
  public String getHead( Handler h )
  {
    return ( NLN + DIV + NLN + HEAD + DateFormat.getDateTimeInstance().format( new Date() ) + NLN + DIV + NLN + NLN );
  }
  
  /**
   * Printed at the end of a Pseudokeu Log file
   */
  @Override
  public String getTail( Handler h )
  {
    return ( DIV + NLN + TAIL + DateFormat.getDateTimeInstance().format( new Date() ) + NLN + DIV + NLN + NLN );
  }
  
  /** Number of times {@link PskFormatter#format(LogRecord)} has been called */
  private int count;
  
  /** useful String constant */
  static final String SPC = " "  ,
                      NLN = "\n" ,
                      DIV = "=================================================================" ,
                      REC = ": Pseudokeu record #"  ,
                     HEAD = "Pseudokeu START" + NLN ,
                     TAIL = "Pseudokeu END" + NLN   ;
  
}// class PskFormatter
