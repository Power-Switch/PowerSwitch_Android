/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.twofortyfouram.locale;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Utility class to generate a breadcrumb title string for {@code Activity} instances in Locale.
 * <p/>
 * This class cannot be instantiated.
 */
public final class BreadCrumber {
    /**
     * Private constructor prevents instantiation.
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private BreadCrumber() {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }

    /**
     * Static helper method to generate bread crumbs. Bread crumb strings will be properly formatted for the
     * current language, including right-to-left languages, as long as the proper
     * {@link com.twofortyfouram.locale.R.string#twofortyfouram_locale_breadcrumb_format} string
     * resources have been created.
     *
     * @param context      {@code Context} for loading platform resources. Cannot be null.
     * @param intent       {@code Intent} to extract the bread crumb from.
     * @param currentCrumb The last element of the bread crumb path.
     * @return {@code String} presentation of the bread crumb. If the intent parameter is null, then this
     * method returns currentCrumb. If currentCrumb is null, then this method returns the empty string
     * "". If intent contains a private Serializable instances as an extra, then this method returns
     * the empty string "".
     * @throws IllegalArgumentException if {@code context} is null.
     */
    public static CharSequence generateBreadcrumb(final Context context, final Intent intent,
                                                  final String currentCrumb) {
        if (null == context) {
            throw new IllegalArgumentException("context cannot be null"); //$NON-NLS-1$
        }

        try {
            if (null == currentCrumb) {
                Log.w(Constants.LOG_TAG, "currentCrumb cannot be null"); //$NON-NLS-1$
                return ""; //$NON-NLS-1$
            }
            if (null == intent) {
                Log.w(Constants.LOG_TAG, "intent cannot be null"); //$NON-NLS-1$
                return currentCrumb;
            }

            /*
             * Note: this is vulnerable to a private serializable attack, but the try-catch will solve that.
             */
            final String breadcrumbString = intent.getStringExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BREADCRUMB);
            if (null != breadcrumbString) {
                return context.getString(R.string.twofortyfouram_locale_breadcrumb_format, breadcrumbString, context.getString(R.string.twofortyfouram_locale_breadcrumb_separator), currentCrumb);
            }
            return currentCrumb;
        } catch (final Exception e) {
            Log.e(Constants.LOG_TAG, "Encountered error generating breadcrumb", e); //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
    }
}