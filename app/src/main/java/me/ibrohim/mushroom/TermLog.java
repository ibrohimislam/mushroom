package me.ibrohim.mushroom;

/**
 * Created by ibrohim on 9/25/16.
 */
public abstract class TermLog {
    public static int LOG_VERBOSE = 0;
    public static int LOG_INFO = 1;
    public static int LOG_ERROR = 2;

    public void Log(int level, String msg) {
        String prefix = "";

        switch (level) {
            case 0: prefix= "[VERBOSE] "; break;
            case 1: prefix= "[INFO] "; break;
            case 2: prefix= "[ERROR] "; break;
        }

        Log(prefix + msg);
    }

    public abstract void Log(String msg);
}
