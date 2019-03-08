import java.net.*;
import java.io.*;
import org.json.simple.*;

import javax.swing.text.DefaultHighlighter;
import java.util.HashMap;
import java.util.UUID;

class Type{

    static final String SYS_STATE = "SYS_STATE"; // System state
    static final String SYS_ERR = "SYS_ERR"; // System Error
    static final String CONFIG = "CONFIG"; // Change configuration

    static final String GAME_STATE = "GAME_STATE"; // Game state
    static final String GAME_ERR = "GAME_ERR"; // Game error
    static final String GAME_EXITED_ERR = "GAME_EXITED_ERR";
    static final String GAME_NON_EXIST_ERR = "GAME_NON_EXIST_ERR";

    static final String USER_STATE = "USER_STATE";

    
    public static final String EVENT_GAME_START = "EVENT_GAME_START"; // Game start event
    static final String EVENT_PLACE_CHAR = "EVENT_PLACE_CHAR"; // Any player places a char
    static final String EVENT_VOTE = "EVENT_VOTE"; // Any player votes
    static final String EVENT_HIGHLIGHT = "EVENT_HIGHLIGHT"; // Any player highlights
    static final String EVENT_GAME_END = "EVENT_GAME_END";
    static final String EVENT_GAME_RESULT = "EVENT_GAME_RESULT"; // Result of the game
    static final String EVENT_HIGHLIGHT_CELL = "EVENT_HIGHLIGHT_CELL";
    static final String NOTIFICATION = "NOTIFICATION";

    static final String PLAIN = "PLAIN"; // Plain text
    static final String UNKNOWN_ERR = "UNKNOWN_ERR";

    static final String INVITATION = "INVITATION";


}

class UserState {
    public static final String IN_GAME = "IN_GAME"; // Playing the game
    public static final String WAITING = "WAITING"; // Waiting for the game to start
    public static final String WATCHING = "WATCHING"; // Watching the game
    public static final String FREE = "FREE"; // Wandering around
}

class GameStage {
    public static final int HIBERNATED = 0;
    public static final int PLACE = 1;
    public static final int HIGHLIGHT = 2;
    public static final int VOTE = 3;

}

