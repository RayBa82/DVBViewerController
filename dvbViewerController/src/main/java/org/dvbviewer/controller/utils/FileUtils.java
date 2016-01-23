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

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;

/**
 * The Class FileUtils.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class FileUtils {
	
	private static final long K = 1024;
	private static final long M = K * K;
	private static final long G = M * K;
	private static final long T = G * K;

	/**
	 * Byte to human string.
	 *
	 * @param value the value
	 * @return the string©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	public static String byteToHumanString(final long value){
	    final long[] dividers = new long[] { T, G, M, K, 1 };
	    final String[] units = new String[] { "TB", "GB", "MB", "KB", "B" };
	    String result = StringUtils.EMPTY;
	    if(value < 1)
	        return result;
	    for(int i = 0; i < dividers.length; i++){
	        final long divider = dividers[i];
	        if(value >= divider){
	            result = format(value, divider, units[i]);
	            break;
	        }
	    }
	    return result;
	}

	/**
	 * Format.
	 *
	 * @param value the value
	 * @param divider the divider
	 * @param unit the unit
	 * @return the string©
	 * @author RayBa
	 * @date 07.04.2013
	 */
	private static String format(final long value,
	    final long divider,
	    final String unit){
	    final double result =
	        divider > 1 ? (double) value / (double) divider : (double) value;
	    return new DecimalFormat("#,##0.#").format(result) + " " + unit;
	}

}
