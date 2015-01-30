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

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * The Class NetUtils.
 *
 * @author RayBa
 * @date 07.04.2013
 */
public class NetUtils {

    private static final int PORT = 9;

    /**
     * Try to extract a hardware MAC address from a given IP address or machine name using the
     * ARP cache (/proc/net/arp).<br>
     * <br>
     * We assume that the file has this structure:<br>
     * <br>
     * IP address       HW type     Flags       HW address            Mask     Device<br>
     * 192.168.18.11    0x1         0x2         00:04:20:06:55:1a     *        eth0<br>
     * 192.168.18.36    0x1         0x2         00:22:43:ab:2a:5b     *        eth0<br>
     *
     * @param ip the ip or a machine name
     * @return the MAC from the ARP cache
     * @author RayBa
     * @date 07.04.2013
     */
    public static String getMacFromArpCache(String ip) {
        if (ip == null)
            return null;
        BufferedReader br = null;
        try {
            ip = InetAddress.getByName(ip).getHostAddress();
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
                    // Basic sanity check
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        return mac;
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Send wake on lan.
     *
     * @param macAddress the MACaddress
     * @param macAddress the mac address
     * @author RayBa
     * @date 07.04.2013
     */
    public static void sendWakeOnLan(String macAddress, int port) {
        Log.d(NetUtils.class.getSimpleName(), "sendWakeOnLan");
        Log.d(NetUtils.class.getSimpleName(), "macAddress:" + macAddress);
        try {
            byte[] macBytes = getMacBytes(macAddress);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }
            InetAddress address = InetAddress.getByName("255.255.255.255");
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the mac bytes.
     *
     * @param macStr the mac str
     * @return the mac bytes
     * @throws IllegalArgumentException the illegal argument exception
     * @author RayBa
     * @date 07.04.2013
     */
    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }

}
