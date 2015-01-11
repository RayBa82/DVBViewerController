/*
 * Copyright © 2012 dvbviewer-controller Project
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

import java.util.regex.Pattern;

import android.text.TextUtils;

/**
 * * @author RayBa
 * @date 13.04.2012
 */
public class Config { 
	/** The is first start. */
	public static boolean	IS_FIRST_START			= false;
	
	/** The rs profile names. */
	public static String[]	RS_PROFILE_NAMES		= null;
	
	/** The channels synced. */
	public static boolean	CHANNELS_SYNCED			= false;
	
	/** The current rs profile. */
	public static String	CURRENT_RS_PROFILE		= "";
	
	/** The current dvbv profile. */
	public static String	CURRENT_DVBV_PROFILE	= "";

	/**
	 * Checks if is old rs version.
	 *
	 * @param version the version
	 * @return true, if is old rs version
	 * @author RayBa
	 * @date 18.08.2013
	 */
	public static boolean isOldRsVersion (String version) {
		if (TextUtils.isEmpty(version)) {
			return true;
		}
        String s1 = normalisedVersion(version);
        String s2 = normalisedVersion("1.24");
        int cmp = s1.compareTo(s2);
        return cmp < 0;
    }

    /**
     * Normalised version.
     *
     * @param version the version
     * @return the string©
     * @author RayBa
     * @date 18.08.2013
     */
    private static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    /**
     * Normalised version.
     *
     * @param version the version
     * @param sep the sep
     * @param maxWidth the max width
     * @return the string©
     * @author RayBa
     * @date 18.08.2013
     */
    private static String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }
	
}
