package org.cloudvr.client.home.utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

/**
 * Utility class for Snackbar
 *
 * @author Pierfrancesco Soffritti
 */
public class SnackbarFactory {
    /**
     * @param parent
     * @param text
     * @param color
     * @param length
     */
    public static void snackbarRequest(View parent, int text, int color, int length) {
        assert parent != null;
        Snackbar snackbar = Snackbar.make(parent, text, length);
        View snackBarView = snackbar.getView();
        if(color > -1)
            snackBarView.setBackgroundColor(ContextCompat.getColor(parent.getContext(), color));
        snackbar.show();
    }

    /**
     * @param parent
     * @param text
     * @param undoText
     * @param color
     * @param listener
     * @param length
     */
    public static void snackbarActionRequest(View parent, String text, int undoText, int color, View.OnClickListener listener, int length) {
        assert parent != null;
        Snackbar snackbar = Snackbar.make(parent, Html.fromHtml(text), length);
        snackBarAction_internal(snackbar, parent.getContext(), undoText, color, listener);
    }

    /**
     * @param parent
     * @param text
     * @param undoText
     * @param color
     * @param listener
     * @param length
     */
    public static void snackbarActionRequest(View parent, int text, int undoText, int color, View.OnClickListener listener, int length) {
        assert parent != null;
        Snackbar snackbar = Snackbar.make(parent, Html.fromHtml(parent.getContext().getString(text)), length);
        snackBarAction_internal(snackbar, parent.getContext(), undoText, color, listener);
    }

    private static void snackBarAction_internal(Snackbar snackbar, Context context, int undoText, int color, View.OnClickListener listener) {
        snackbar.setAction(undoText, listener);
        View snackBarView = snackbar.getView();
        if(color > -1)
            snackBarView.setBackgroundColor(ContextCompat.getColor(context, color));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
    }
}