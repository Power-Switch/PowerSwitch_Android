/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.clipboard;

import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.StringRes;

import eu.power_switch.shared.exception.clipboard.EmptyClipboardException;

/**
 * Helper class for convenient access to android clipboard manager
 * <p/>
 * Created by Markus on 13.08.2016.
 */
public class ClipboardHelper {

    /**
     * Copy a text to clipboard
     *
     * @param context any suitable context
     * @param label   label for the given clipboard content
     * @param content content for clipboard
     */
    public static void copyToClipboard(Context context, @StringRes int label, @StringRes int content) {
        copyToClipboard(context, context.getString(label), context.getString(content));
    }

    /**
     * Copy a text to clipboard
     *
     * @param context any suitable context
     * @param label   label for the given clipboard content
     * @param content content for clipboard
     */
    public static void copyToClipboard(Context context, String label, String content) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            @SuppressWarnings("deprecation")
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(content);
        } else {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(label, content);
            clipboard.setPrimaryClip(clip);
        }
    }

    /**
     * Get the current clipboard content as text
     *
     * @param context any suitable context
     * @return clipboard content text
     * @throws EmptyClipboardException if the clipboard is empty
     */
    public static String getClipboardContent(Context context) throws EmptyClipboardException {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        if (!clipboard.hasPrimaryClip()) {
            throw new EmptyClipboardException();
        }

        if (clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            return clipboard.getPrimaryClip().getItemAt(0).getText().toString();
        } else {
            return clipboard.getPrimaryClip().getItemAt(0).coerceToText(context).toString();
        }
    }
}
