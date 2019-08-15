/*
 * Copyright © 2013 dvbviewer-controller Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dvbviewer.controller.utils;

import android.content.Context;
import android.content.res.Configuration;

/**
 * An assortment of UI helpers.
 */
public class UIUtils {

    /**
     * Checks if is tablet.
     *
     * @param context the context
     * @return true, if is tablet
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * Dip to pixel.
     *
     * @param context the context
     * @param dip     the dip
     * @return the float©
     */
    public static float dipToPixel(Context context, int dip) {
        return dip * context.getResources().getDisplayMetrics().density;
    }

}