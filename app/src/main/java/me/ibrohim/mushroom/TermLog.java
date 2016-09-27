package me.ibrohim.mushroom;

import android.text.Html;
import android.text.Spanned;

/**
 * Created by ibrohim on 9/25/16.
 */
public abstract class TermLog {
    public static int LOG_VERBOSE = 0;
    public static int LOG_INFO = 1;
    public static int LOG_ERROR = 2;

    public void Log(int level, String msg) {
        Spanned text;
        String prefix = "";

        switch (level) {
            case 0: prefix= "<font color=\"#BDBDBD\">[VERBOSE]</font> "; break;
            case 1: prefix= "<font color=\"#BBDEFB\">[INFO]</font> "; break;
            case 2: prefix= "<font color=\"#F44336\">[ERROR]</font> "; break;
        }


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            text = Html.fromHtml(prefix + msg + "<br/>",Html.FROM_HTML_MODE_LEGACY);
        } else {
            text = Html.fromHtml(prefix + msg + "<br/>");
        }

        Log(text);
    }

    public abstract void Log(Spanned msg);
}
