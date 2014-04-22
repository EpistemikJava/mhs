/* ***************************************************************************************
 
   Mark Sattolo (epistemik@gmail.com)
 -----------------------------------------------
 $File: //depot/Eclipse/Java/workspace/Pseudokeu/src/mhs/pseudokeu/Loader.java $
 $Revision: #11 $
 $Change: 176 $
 $DateTime: 2012/02/25 11:40:50 $
 -----------------------------------------------
 
  mhs.latinsquare.LatinSquareLoader.java
  Eclipse version created on Nov 30, 2007, 10:49 PM
  git version created Mar 8, 2014
 
*************************************************************************************** */

package mhs.pseudokeu;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;

/**
 *  Access <b>games</b> from file or jar and Store as {@link SavedGame}s
 *  
 *  @author  Mark Sattolo
 *  @version 8.1.1
 */
public class Loader
{
 /*
  *            I N N E R    C L A S S E S
  *************************************************************************************************************/
  
  /**
   * Inner CLASS to store and restore Pseudokeu games
   */
  class SavedGame
  {
    /** A 2d int array (gridLength x gridLength) to save all values in a {@link Grid}  */
    private int[][] values ;
    
    /** number of {@link Square}s on each side of a {@link Grid}  */
    private int length ;
    
    /** for display */
    private String name ;
    
    /** level of difficulty -- equals the index into {@link Loader#STR_DIFF_FOLDERS} */
    private int difficulty ;
    
    /**
     * default Constructor
     * 
     * @param len - number of {@link Square}s on each side of a {@link Grid}
     * @param str - name
     */
    protected SavedGame( final int len, final String str )
    {
      length = len ;
      name = new String( str );
      values = new int[ length ][ length ];
    }
    
    /**
     * Constructor with 'difficulty' parameter
     * 
     * @param diff - level of difficulty
     * @param len  - number of {@link Square}s on each side of a {@link Grid}
     * @param str - name
     */
    protected SavedGame( final int len, final String str, final int diff )
    {
      this( len, str );
      difficulty = diff ;
      logger.info( "name = " + this.getName() + "; length = " + this.getLength() + "; difficulty = " + this.getDifficulty() );
    }
    
    String myname() { return SavedGame.class.getSimpleName(); }
    String fullname() { return SavedGame.class.getName(); }
    
    /**
     * Set a value at a particular row and col in {@link SavedGame#values} <br>
     * - ONLY called by {@link #loadScannedGame}
     * 
     * @param row - row position
     * @param col - col position
     * @param val - value to set
     * 
     * @return old value
     */
    protected int setValue( final int row, final int col, final int val )
    {
      int $val = val, 
          $oldVal = values[row][col] ;
      
      if( ($val < SqrTypes.BLANK_VAL) || ($val > SqrTypes.MAX_TEMP_VAL) )
      {
        $val = 0 ;
        logger.warning( "Problem with value '" + val + "' !" );
      }
      
      values[row][col] = $val ;
      
      return $oldVal ;
      
    }// SavedGame.setValue()
    
    /**
     * Get the value at a particular row and col in {@link SavedGame#values} <br>
     * 
     * @param row - row position
     * @param col - col position
     * 
     * @return value obtained
     */
    int getValue( final int row, final int col ) { return values[row][col]; }
    
    /** @return {@link SavedGame#length}  */
    int getLength() { return length ; }
    
    /** @return {@link SavedGame#name}  */
    String getName() { return name ; }
    
    /** @return {@link SavedGame#difficulty}  */
    int getDifficulty() { return difficulty ; }
    
    /**
     *  display data at submitted log {@link Level}
     *  @param lev - java.util.logging.Level
     */
    void display( final Level lev )
    {
      Level level = lev ;
      
      if( lev == null )
      {
        logger.severe( "Passed a null Level!!??" );
        level = LogControl.DEFAULT_LEVEL ;
      }
      
      if( ! LogControl.atLevel(level) )
        return ;
      
      logger.appendln( "GAME '" + name + "' : length = " + length
                       + " & difficulty = " + Loader.STR_DIFF_FOLDERS[difficulty] + "\n" );
      
      for( int r = 0; r < length; r++ )
      {
        logger.append( "\t" );
        for( int c = 0; c < length; c++ )
        {
          if( (r > 0) && (r % 3 == 0) && (c == 0) ) // horizontal space
            logger.append( "\n\t" );
          
          logger.append( values[r][c] + " " );
          
          logger.append( (c % 3 == 2) ? "   " : " " ); // vertical space
          
        }
        logger.appendln();
      }
      logger.appendln( "\n#######################################" );

      logger.send( level );
      
    }// SavedGame.display()
    
  }/* Inner Class SavedGame */

 /*
  *            C O N S T R U C T O R S
  *************************************************************************************************************/

  /**
   * Constructor <br>
   * - ONLY Caller is {@link Launcher#createLoader}
   * 
   * @param frame - reference to the enclosing instance of {@link Launcher}
   * @param num - maximum number of <var>games</var> to store
   */
  protected Loader( final Launcher frame, final int num )
  {
    if( frame == null )
    {
      System.err.println( "Loader Constructor: passed a null Launcher!!??" );
      System.exit( this.hashCode() );
    }
    
    int numGames = num ;
    if( num < Loader.MIN_NUM_LOADED_GAMES )
      numGames = Launcher.MAX_NUM_LOADED_GAMES ;
      
    gameview = frame ;
    maxNumGames = numGames ;
    
    games = new SavedGame[NUM_DIFFICULTIES][ maxNumGames ];
    nLoadedGames = new int[NUM_DIFFICULTIES] ;
    base = gameview.grid.getLength() + 1 ;
    
    logger = Launcher.logger ;
    logger.logInit();
    
    pathSep = File.separator ;
  }
  
 /*
  *            M E T H O D S
  *************************************************************************************************************/
  
 // ===========================================================================================================
 //                          I N T E R F A C E
 // ===========================================================================================================
  
  /**
   *  Load ALL games from the default Jar OR Folder ({@link #DEFAULT_PATH})
   *  
   *  @return {@link #totalGames}
   *  @see Launcher#createLoader
   */
  int loadAllGames()
  {
    // make sure this is running on a Worker thread
    logger.logInit( "Running on " + Thread.currentThread() );
    
    userPrefix = STR_DIFF_FILES[USER] ;
    addfolderName = DEFAULT_PATH + pathSep + STR_DIFF_FOLDERS[USER] + pathSep ;
    loadGamesFromLocation( DEFAULT_PATH, GAME_SUFFIX );
    
    return totalGames ;
    
  }// Loader.loadAllGames()
  
  /**
   * Load files in the given folder
   * 
   * @param folder - with Pseudokeu games
   * @param prefix - of all games
   * 
   * @return {@link #nLoadedGames}[{@link #USER}]
   * 
   * @see Launcher#createLoader
   */
  int loadFiles( final String folder, final String prefix )
  {
    if( (folder == null) || folder.isEmpty() )
    {
      logger.warning( "Received a BAD folder String!" );
      return nFAIL ;
    }
    
    if( (prefix == null) || prefix.isEmpty() )
    {
      logger.warning( "Received a BAD prefix String!" );
      return nFAIL ;
    }
    
    logger.info( folder );
    
    gameview.setDifficulty( USER );
    userPrefix = prefix ;
    
    addfolderName = folder + pathSep ;
    loadGamesFromFolder( new File(folder), USER );
    
    return nLoadedGames[USER] ;
    
  }// Loader.loadFiles()
  
  /**
   * Load this game from the default Folder - {@link #DEFAULT_PATH}
   * 
   * @param relativePath - of the given file
   * @param name - of the file
   * 
   * @return {@link #nLoadedGames}[{@link #USER}]
   * 
   * @see Launcher#createLoader
   */
  int loadFile( final String relativePath, final String name )
  {
    if( (relativePath == null) || relativePath.isEmpty() )
    {
      logger.warning( "Received a BAD path String!" );
      return nFAIL ;
    }
    
    if( (name == null) || name.isEmpty() )
    {
      logger.warning( "Received a BAD game name String!" );
      return nFAIL ;
    }
    
    gameview.setDifficulty( USER );
    fileBasename = name ;
    
    userPrefix = STR_DIFF_FILES[USER] ;
    addfolderName = DEFAULT_PATH + pathSep + STR_DIFF_FOLDERS[USER] + pathSep ;
    
    String $filename = relativePath + pathSep + name ;
    logger.info( $filename );
    
    getFileGame( new File($filename), USER );
    
    return nLoadedGames[USER] ;
    
  }// Loader.loadFile()
  
  /**
   *  User wants to add a game
   *  
   *  @param grid - reference to the {@link Grid} with the new Game's values
   *  @return success or failure
   *  
   *  @see Launcher#verifyAddedGame
   *  @see PrintWriter
   */
  boolean addGame( final Grid grid )
  {
    if( grid == null )
    {
      logger.severe( "Grid is not Created yet!" );
      return boolFAIL ;
    }
    
    boolean $result = boolFAIL ;
    
    fileBasename = userPrefix + Integer.toString( nLoadedGames[USER] + 1 );
     addfileName = addfolderName + fileBasename + GAME_SUFFIX ;
    
    logger.info( "Adding Game '" + addfileName + '\'' );
    
    // must have at least 1 value...
    if( grid.getNumValues() <= 0 )
      return boolFAIL ;
    
    int $numbase = Grid.BASE_KEYCODE_INDEX ;
    int $row, $col, $len = grid.getLength() ;
    Square[][] $sqrAr = grid.sqrs2dArray ;
    int $addVal, $min = SqrTypes.BLANK_VAL + 1, $max = $len ;
    
    try
    {
      pw = new PrintWriter( new BufferedWriter( new FileWriter(addfileName) ) );
      
      // get the values in the Grid
      for( $row=0; $row < $len; $row++ )
      {
        for( $col=0; $col < $len; $col++ )
        {
          $addVal = $sqrAr[$row][$col].getValue();
          
          if( ($addVal >= $min) && ($addVal <= $max) )
          {
            pw.write( $col + $numbase );
            pw.write( $addVal + $numbase );
            pw.write( " " );
          }
          
        } // for( cols )
        
        pw.println();
        
      }// for( rows )
      
      $result = true ;
    }
    catch( Exception e )
    {
      logger.warning( e.toString() );
    }
    finally
    {
      pw.flush();
      pw.close();
    }
    
    return( $result ? getFileGame( new File(addfileName), USER ) : boolFAIL );
  
  }// Loader.addGame()
  
  /**
   * Get a particular LOADED game to insert into the Grid <br>
   * 
   * @param difficulty - level of the requested game
   * @param index      - which game to return
   * 
   * @return an instance of {@link Loader.SavedGame} OR null if a problem
   * @see Grid#activateGame
   */
  SavedGame getSavedGame( final int difficulty, final int index )
  {
    // have to check the difficulty FIRST
    if( (difficulty <= nFAIL) || (difficulty >= NUM_DIFFICULTIES) )
    {
      logger.warning( "Received a BAD difficulty level!" );
      return null ;
    }
    
    // see if the index is good
    if( (index >= 0) && (index <= nLoadedGames[difficulty]) )
    {
      logger.info( "Returning Game '" + games[difficulty][index].getName() + '\'' );
      return games[difficulty][index] ;
    }
    
    // are there ANY games at this level?
    if( nLoadedGames[difficulty] > 0 )
    {
      logger.warning( "PROBLEM: index #" + index + " is NOT valid... returning game #" + nLoadedGames );
      return games[difficulty][nLoadedGames[difficulty] - 1] ;
    }
    
    logger.warning( "NO LOADED GAMES at level: " + STR_DIFF_FOLDERS[difficulty] );
    return null ;
  
  }// Loader.getSavedGame()
  
  /**
   * @param difficulty - level
   * @return {@link #nLoadedGames} for this difficulty level
   */
  int getNumLoadedGames( final int difficulty )
  {
    if( (difficulty <= nFAIL) || (difficulty >= NUM_DIFFICULTIES) )
    {
      logger.warning( "Received a BAD difficulty level!" );
      return nFAIL ;
    }
    
    return nLoadedGames[difficulty] ;
    
  }// Loader.getNumLoadedGames()
  
  /**
   * Get the file name used at the requested difficulty level
   * 
   * @param difficulty - level
   * @return {@link String} with the requested file name
   * 
   * @see #userPrefix
   * @see #STR_DIFF_FILES
   */
  String getFileName( final int difficulty )
  {
    // have to check the difficulty FIRST
    if( (difficulty <= nFAIL) || (difficulty >= NUM_DIFFICULTIES) )
    {
      logger.warning( "Received a BAD difficulty level!" );
      return strFAIL ;
    }
    
    if( difficulty == USER )
      return userPrefix ;
    
    return STR_DIFF_FILES[difficulty] ;
    
  }// Loader.getFileName()
  
  /**
   * Get the name of the game at the requested difficulty level and index
   * 
   * @param difficulty - level
   * @param index - of the requested {@link SavedGame}
   * 
   * @return {@link String} with the name of the requested game
   */
  String getGameName( final int difficulty, final int index )
  {
    // have to check the difficulty FIRST
    if( (difficulty <= nFAIL) || (difficulty >= NUM_DIFFICULTIES) )
    {
      logger.warning( "Received a BAD difficulty level!" );
      return strFAIL ;
    }
    
    if( (index < 0) || (index >= nLoadedGames[difficulty]) )
    {
      logger.warning( "Received a BAD game index!" );
      return strFAIL ;
    }
    
    return games[difficulty][index].getName();
    
  }// Loader.getGameName()
  
  /**
   * Get the index of the given game in the array of the given difficulty
   * 
   * @param difficulty - level
   * @param name - of the requested {@link SavedGame}
   * 
   * @return index of the requested game
   */
  int getGameIndex( final int difficulty, final String name )
  {
    if( (difficulty <= nFAIL) || (difficulty >= NUM_DIFFICULTIES) )
    {
      logger.warning( "Received a BAD difficulty level!" );
      return nFAIL ;
    }
    
    if( (name == null) || name.isEmpty() )
    {
      logger.warning( "Received a BAD game name!" );
      return nFAIL ;
    }
    
    for( int i=0; i < nLoadedGames[difficulty]; i++ )
      if( games[difficulty][i].getName().contentEquals(name) )
        return i ;
    
    return nFAIL ;
    
  }// Loader.getGameIndex()
  
  /**
   * Get the position in the difficulty array of a particular difficulty level
   * 
   * @param difficulty - name of the level
   * @return position index of the requested level
   * 
   * @see #STR_DIFF_FOLDERS
   */
  int getDifficultyIndex( final String difficulty )
  {
    if( (difficulty == null) || difficulty.isEmpty() )
    {
      logger.warning( "Received a BAD difficulty String!" );
      return nFAIL ;
    }
    
    for( int i=0; i < maxNumGames; i++ )
      if( STR_DIFF_FOLDERS[i].contentEquals(difficulty) )
        return i ;
    
    return nFAIL ;
    
  }// Loader.getDifficultyIndex()
  
 // ===========================================================================================================
 //                            P R I V A T E
 // ===========================================================================================================
  
  /**
   * Load ALL games with this prefix and suffix from the given location
   * 
   * @param location - of Pseudokeu games
   * @param   suffix - of game names (e.g. '.psk')
   * 
   * @return success or failure
   */
  private boolean loadGamesFromLocation( final String location, final String suffix )
  {
    if( (location == null) || location.isEmpty() )
    {
      logger.warning( "Received a BAD location String!" );
      return boolFAIL ;
    }
    
    if( (suffix == null) || suffix.isEmpty() )
    {
      logger.warning( "Received a BAD suffix String!" );
      return boolFAIL ;
    }
    
    boolean $result = boolFAIL ;
    logger.info( "Loading from Location '" + location + "'" );
    
    $result = loadFromDifficultyFolders( location );
    
    if( ! $result ) // see if we are running from a jar file
    {
      pathSep = JAR_SEP ;
      $result = loadFromJar( location, suffix );
    }
    
    return $result ;
    
  }// Loader.loadGamesFromLocation()
  
  /**
   * Get games from the different difficulty folders at this location
   * 
   * @param location - of Pseudokeu games
   * @return success or failure
   */
  private boolean loadFromDifficultyFolders( final String location )
  {
    if( (location == null) || location.isEmpty() )
    {
      logger.warning( "Received a BAD location String!" );
      return boolFAIL ;
    }
    
    logger.info( "Find difficulty folders in '" + location + "'" );
    
    File $folder ;
    boolean $interim=boolFAIL, $result=boolFAIL ;
    for( int i=0; i < NUM_DIFFICULTIES; i++ )
    {
      $folder = new File( location + pathSep + STR_DIFF_FOLDERS[i] );
      logger.logInit( $folder + " absolute path: " + $folder.getAbsolutePath() );
      
      $interim = loadGamesFromFolder( $folder, i );
      if( ! $interim )
        logger.warning( "No games at '" + $folder + "'... probably running from a jar." );
      else
      if( ! $result )
        $result = true ;
    }
    
    return $result ;
    
  }// Loader.loadFromDifficultyFolders()
  
  /**
   * Load ALL games from the folder with the supplied name
   * 
   * @param folder - folder with LatinSquare games (psk files)
   * @param difficulty - level
   * 
   * @return success or failure
   */
  private boolean loadGamesFromFolder( final File folder, final int difficulty )
  {
    if( folder == null )
    {
      logger.severe( "Received a null folder File!" );
      return boolFAIL ;
    }
    
    if( ! folder.exists() )
    {
      logger.warning( "! Received a BAD folder name: " + folder.toString() );
      return boolFAIL ;
    }
    
    if( (difficulty <= nFAIL) || (difficulty >= NUM_DIFFICULTIES) )
    {
      logger.warning( "Received a BAD difficulty level!" );
      return boolFAIL ;
    }
    
    boolean $result = boolFAIL ;
    
    try
    {
      if( folder.isDirectory() )
      {
        logger.config( "Loading from Folder '" + folder.toString() + "'" );
        
        File[] $children = folder.listFiles();
        for( int j=0 ; (j < maxNumGames) && (j < $children.length) ; j++ )
        {
          if( ! $children[j].isDirectory() )
          {
            setFileBasename( $children[j].getName() );
            
            $result = getFileGame( $children[j], difficulty );
          }
        }// for( files )
      }
      else
          logger.warning( ">> '" + folder + "' is NOT a Folder!" );
    }
    catch( Exception e )
    {
      logger.warning( e.toString() );
      e.printStackTrace();
    }
    
    return $result ;
  
  }// Loader.loadGamesFromFolder()
  
  /**
   * Scan and Load a game of the given difficulty from the given file
   * 
   * @param file - with the <var>game</var> to load
   * @param difficulty - level
   * 
   * @return success or failure
   */
  private boolean getFileGame( final File file, final int difficulty )
  {
    if( file == null )
    {
      logger.severe( "Received a null File!" );
      return boolFAIL ;
    }
    
    if( ! file.exists() )
    {
      logger.warning( "! Received a BAD file name: " + file.toString() );
      return boolFAIL ;
    }
    
    if( (difficulty <= nFAIL) || (difficulty >= NUM_DIFFICULTIES) )
    {
      logger.warning( "Received a BAD difficulty level!" );
      return boolFAIL ;
    }
    
    logger.config( "Looking for game file '" + file.toString() + "'" );
    
    boolean $result = scanFile( file );
    
    if( $result )
      $result = loadScannedGame( difficulty ) ;
    
    return $result ;
  
  }// Loader.getFileGame()
  
  /**
   *  Scan a file with a game
   *  
   *  @param file - the <var>file</var> to scan
   *  @return success or failure
   *  
   *  @see java.util.Scanner
   */
  private boolean scanFile( final File file )
  {
    if( file == null )
    {
      logger.warning( "Received a null File!" );
      return boolFAIL ;
    }
    
    boolean $result = boolFAIL ;
    loadscanner = null ;
    
    try
    {
      if( file.exists() )
      {
        loadscanner = new Scanner( file );
        logger.info( "Scanned file '" + file.toString() + "'" );
      }
      else
          logger.warning( "! Could NOT find file '" + file.toString() + "'" );
    }
    catch( Exception e )
    {
      logger.severe( "** PROBLEM scanning file '" + file.toString() + "' -- " + e.toString() );
    }
    
    if( loadscanner != null )
      $result = true ;
    
    return $result ;
  
  }// Loader.scanFile()
  
  /**
   * Load ALL games with this prefix and suffix from the parameter jar resource
   * 
   * @param jar - with Pseudokeu games
   * @param suffix - of game names (e.g. '.psk')
   * 
   * @return success or failure
   */
  private boolean loadFromJar( final String jar, final String suffix )
  {
    if( (jar == null) || jar.isEmpty() )
    {
      logger.warning( "Received a BAD jar String!" );
      return boolFAIL ;
    }
    
    if( (suffix == null) || suffix.isEmpty() )
    {
      logger.warning( "Received a BAD suffix String!" );
      return boolFAIL ;
    }
    
    logger.logInit( "Loading from Jar '" + jar + "'" + '\n' );
    
    boolean $result = boolFAIL ;
    String $fileName ;
    
    for( int i=0; i < NUM_DIFFICULTIES; i++ )
    {
      for( int j=1; j < maxNumGames; j++ )
      {
        fileBasename = STR_DIFF_FILES[i] + Integer.toString( j );
        $fileName = jar + pathSep + STR_DIFF_FOLDERS[i] + pathSep + fileBasename + suffix ;
        
        $result = getJarGame( $fileName, i );
      }
    }
    
    return $result ;
    
  }// Loader.loadGamesFromJar()
  
  /**
   * Scan and Load a game from a jar
   * 
   * @param name - of the <var>game</var> to load
   * @param difficulty - level
   * 
   * @return success or failure
   */
  private boolean getJarGame( final String name, final int difficulty )
  {
    if( (name == null) || name.isEmpty() )
    {
      logger.warning( "Received a BAD name String!" );
      return boolFAIL ;
    }
    
    if( (difficulty <= nFAIL) || (difficulty >= NUM_DIFFICULTIES) )
    {
      logger.warning( "Received a BAD difficulty level!" );
      return boolFAIL ;
    }
    
    logger.config( "Looking for game '" + name + "'" );
    
    boolean $result = scanJar( name );
    
    if( $result )
      $result = loadScannedGame( difficulty ) ;
    
    return $result ;
  
  }// Loader.getJarGame()
  
  /**
   *  Scan a jar resource game
   *  
   *  @param name - of the <em>jar game</em> to scan
   *  @return success or failure
   *  
   *  @see java.util.Scanner
   */
  private boolean scanJar( final String name )
  {
    if( (name == null) || name.isEmpty() )
    {
      logger.warning( "Received a BAD name String!" );
      return boolFAIL ;
    }
    
    boolean $success = false ;
    loadscanner = null ;
    URL $url ;
    
    try
    {
      $url = getClass().getResource( JAR_SEP + name );
      if( $url != null )
      {
        logger.appendln( "Loaded Resource '" + name + "'" );
        
        URLConnection $site = $url.openConnection();
        loadscanner = new Scanner( new BufferedInputStream($site.getInputStream()) );
        logger.logInit( "Scanned file '" + $url.toString() + "'" );
      }
      else
        logger.appendln( "Could NOT find '" + name + "'" );
    }
    catch( Exception e )
    {
      logger.appendln( "PROBLEM scanning '" + name + "' -- " + e.toString() );
    }
    
    if( loadscanner != null )
      $success = true ;
    
    logger.send( Level.INFO );
    
    return $success ;
    
  }// Loader.scanJar()
  
  /**
   * Load a game into a {@link SavedGame} from the <var>scanner</var>
   * 
   * @param difficulty - level
   * @return success or failure
   * 
   * @see java.util.StringTokenizer
   */
  private boolean loadScannedGame( final int difficulty )
  {
    logger.info( fileBasename + " : difficulty = " + difficulty );
    
    if( (difficulty <= nFAIL) || (difficulty >= NUM_DIFFICULTIES) )
    {
      logger.warning( "Received a BAD difficulty level!" );
      return boolFAIL ;
    }
    
    if( nLoadedGames[difficulty] == maxNumGames )
    {
      logger.warning( "MAX GAMES REACHED!" );
      return boolFAIL ;
    }
    
    games[difficulty][nLoadedGames[difficulty]] = new SavedGame( gameview.grid.getLength(), fileBasename, difficulty );
    
    int $row=0, $col, $val, $token ;
    StringTokenizer $st ;
    String $next ;
    try
    {
      while( loadscanner.hasNext() )
      {
        $next = loadscanner.nextLine();
        
        logger.appendln( "Row #" + $row );
        
        $st = new StringTokenizer( $next );
        while( $st.hasMoreTokens() )
        {
          $token = Integer.parseInt( $st.nextToken() );
          $col = $token/base ;
          $val = $token%base ;
          
          // assign the val
          games[difficulty][nLoadedGames[difficulty]].setValue( $row, $col, $val );
          logger.appendln( "col: " + $col + " - value = " + $val );
        }
        $row++ ;
      }
      logger.appendln( "loadscanner: END of Input." );
    }
    catch( Exception e )
    {
      logger.appendln( "\t !! PROBLEM: " + e.toString() );
      logger.send( Level.WARNING );
      return boolFAIL ;
    }
    finally
    {
      loadscanner.close(); 
    }
    
    nLoadedGames[difficulty]++ ;
    totalGames++ ;
    
    logger.send( Level.FINE );
    
    return true ;
    
  }// Loader.loadScannedGame()
  
  /**  
   *  Input is a file name <b>WITHOUT</b> path but <b>may have</b> a file type extension<br>
   *  - sets <var>fileBasename</var> to file name <b>WITHOUT</b> the extension, if any
   *  
   *  @param name - of the game file
   *  @return success or failure 
   */
  private boolean setFileBasename( final String name )
  {
    if( (name == null) || name.isEmpty() )
    {
      logger.severe( "Received a null/empty name String!" );
      fileBasename = STR_DIFF_FILES[USER] ;
      return boolFAIL ;
    }
    
    if( ! name.contains(".") )
    {
      logger.warning( "File Name String has no extension: " + name );
      fileBasename = name ;
      return true ;
    }
    
    fileBasename = name.substring( 0, name.lastIndexOf('.') );
    return true ;
    
  }// Loader.setFileBasename()
  
 // ===========================================================================================================
 //                             D E B U G    C O D E
 // ===========================================================================================================
  
  String myname() { return getClass().getSimpleName(); }
  
 /*
  *            F I E L D S
  *************************************************************************************************************/
  
  /** Logging */
  protected static PskLogger logger ;
  
  /**
   * Maximum number of {@link SavedGame}s to store <br>
   *  - set in {@link #Loader(Launcher,int)}
   */
  private int maxNumGames = 99 ;
  
  /**
   * Number of {@link SavedGame}s available at each difficulty level <br>
   *  - set in {@link #loadScannedGame}
   */
  private int nLoadedGames[] ;
  
  /**
   * Number of {@link SavedGame}s that are loaded and available to solve <br>
   *  - set in {@link #loadScannedGame}
   */
  private int totalGames ;
  
  /** For calculating row & value in {@link #loadScannedGame} */
  private int base ;
  
  /** system path separator */
  private String pathSep ;
  
  /** file name prefix for USER files */
  private String userPrefix ;
  
  /** {@link SavedGame} array */
  private SavedGame[][] games ;
  
  /** Use this for {@link SavedGame#name}  */
  private String fileBasename ;
  
  /** the name of an ADDED file */
  private String addfileName ;
  
  /** the folder name used for an ADDED file */
  private String addfolderName ;
  
  /**
   * to write out an ADDED Game as a .psk file
   * @see #addGame
   */
  private PrintWriter pw ;
  
  /**
   *  Used so can load <b>game files</b> from a <em>filesystem</em> or as a <em>Jar resource</em>
   *  
   *  @see java.util.Scanner
   *  @see Scanner#Scanner(File)
   *  @see Scanner#Scanner(java.io.InputStream)
   *  @see Class#getResource(String)
   *  @see java.net.URL
   */
  private Scanner loadscanner ;
  
  /** boolean FAILURE */
  static final boolean boolFAIL = false ;
  
  /** {@link String} FAILURE */
  static final String strFAIL = "" ;
  
  /** path separator for jar resources */
  static final String JAR_SEP = "/" ;
  
  /** Default Path with saved games (psk files) */
  static final String DEFAULT_PATH = "savedGames" ;
  
  /** Default suffix for <CODE>Game</CODE> files */
  static final String GAME_SUFFIX = ".psk" ;
  
  /** Minimum number of {@link SavedGame}s to store */
  private static final int MIN_NUM_LOADED_GAMES = 8 ;
  
  /**
   * File name prefixes for the different levels of difficulty <br>
   *  - MUST match with the values of the static ints
   */
  static final String[] STR_DIFF_FILES = { "easy" , "mod" , "hard" , "pain" , "dead" , "user" };
  
  /**
   * Names of difficulty levels/folders containing game files of different difficulty <br>
   *  - MUST match with the values of the static ints
   */
  static final String[] STR_DIFF_FOLDERS = { "EASY" , "MODERATE" , "HARD" , "PAINFUL" , "DEADLY" , "USER" };
  
  /** level of difficulty */
  static final int
                 nFAIL = -1 ,
                  EASY =  0 ,
              MODERATE = EASY     + 1 ,
                  HARD = MODERATE + 1 ,
               PAINFUL = HARD     + 1 ,
                DEADLY = PAINFUL  + 1 ,
                  USER = DEADLY   + 1 , 
      NUM_DIFFICULTIES = USER     + 1 ;
  
  /** Reference to the enclosing {@link Launcher} object */
  private static Launcher gameview = null ;
  
}// CLASS Loader

/* ========================================================================================================== */

/**
 *  Centralize various utility methods for Pseudokeu game
 *  
 *  @author Mark Sattolo
 */
class Helper
{
  /**
   * Get the number of set bits in the $int parameter
   * 
   * @param val - int to examine
   * @param lim - number of bits to process, starting at LSbit
   * 
   * @return number of set bits
   */
  static int numSetBits( final int val, final int lim )
  {
    int $bits = val ;
    int $res = 0 ;
    
    int $limit = lim ;
    if( $limit > Integer.SIZE )
      $limit = Integer.SIZE ;
    
    for( int i=0; i <= $limit; i++ )
    {
      //logger.finest( "bits == " + bits );
      if( Integer.numberOfTrailingZeros($bits) == 0 )
      {
        $res++ ;
        //logger.finer( "result == " + result );
      }
      
      $bits >>= 1 ;
    }
    
    return $res ;
  
  }// Helper.numSetBits()
  
  /**
   * Find the position (zero-based, from LSbit) in $int of the set bit specified by place
   * 
   * @param val - int to examine
   * @param place - which set bit to find position of, i.e. 1st, 2nd, 3rd...
   * 
   * @return position index of the requested set bit
   */
  static int getBitPosn( final int val, final int place )
  {
    if( (place < 1) || (place > Integer.SIZE) )
      return 0 ;
    
    int $bits = val ;
    int i=0, $shift=0, $res=-1 ;
    while( i < place )
    {
      $shift = Integer.numberOfTrailingZeros( $bits ) + 1 ;
      $res += $shift ;
      $bits >>= $shift ;
      
      i++ ;
    }
    
    return $res ;
  
  }// Helper.getBitPosn()
  
  /**
   * Produce a String indicating the positions of the set bits in $int
   * 
   * @param val - int to examine
   * @param lim - number of bits to process 
   * @param extra - formatting
   * 
   * @return processed String
   */
  static String displaySetBits( final int val, final int lim, final String extra )
  {
    int $bits = val ;
    StringBuilder result = new StringBuilder();
    
    int $limit = lim ;
    if( $limit > Integer.SIZE )
      $limit = Integer.SIZE ;
    
    for( int i=0; i <= $limit; i++ )
    {
      //logger.finest( "bits == " + bits );
      if( Integer.numberOfTrailingZeros($bits) == 0 )
      {
        result.append( i + extra );
        //logger.finer( "result == " + result );
      }
      
      $bits >>= 1 ;
    }
    
    return result.toString() ;
  
  }// Helper.strSetBits()
  
}// CLASS Helper
