/*
 * Copyright (C) 2012 dvbviewer-controller Project
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
package org.dvbviewer.controller.io.data;

import android.content.Context;

import org.dvbviewer.controller.data.DbHelper;
import org.dvbviewer.controller.entities.Channel;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * The Class ChannelListParser.
 *
 * @author RayBa
 * @date 05.07.2012
 */
public class ChannelListParser {

	private static Charset	ASCII	= Charset.forName("ASCII");
	private static Charset	CP1252	= Charset.forName("CP1252");

	/**
	 * Parses the channel list.
	 *
	 * @param context the context
	 * @param bytes the bytes
	 * @return the list©
	 * @author RayBa
	 * @date 05.07.2012
	 */
	public static List<Channel> parseChannelList(Context context, byte[] bytes) {
		List<Channel> result = null;
		ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);

		DataInputStream is = new DataInputStream(byteStream);
		try {
			byte currentByte = 0;
			int length = is.readByte();
			byte[] tmp = new byte[4];
			for (int i = 0; i < 4; i++) {
				tmp[i] = is.readByte();
			}
			String s = new String(tmp);
			int highVersion = is.readByte();
			int lowVersion = is.readByte();
			int position = 0;
			result = new ArrayList<Channel>();
			while (true) {
				Channel c = new Channel();
				readTunerInfos(is, c);
				for (int i = 0; i < 26; i++) {
					currentByte = is.readByte();
				}
				tmp = new byte[26];
				for (int i = 0; i < 26; i++) {
					tmp[i] = is.readByte();
				}
				s = new String(tmp, CP1252.name()).trim();
				// s = s.replaceAll("\\([^\\(]*\\)", "").trim();
				c.setName(s);
				for (int i = 0; i < 26; i++) {
					tmp[i] = is.readByte();
				}
				is.readByte();
				is.readByte();
				if (c.isFlagSet(Channel.FLAG_ADDITIONAL_AUDIO)) {
					continue;
				}
				c.setPosition(position);
				result.add(c);
				position++;
			}
		} catch (EOFException e) {
			/**
			 * End of File Nothing happens;
			 */
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (result != null && result.size() > 0) {
			DbHelper dbHelper = new DbHelper(context);
			dbHelper.saveChannels(result);
		}
		return result;
	}

	/**
	 * Read tuner infos.
	 *
	 * @param is the is
	 * @param c the c
	 * @return the channel©
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @author RayBa
	 * @date 05.07.2012
	 */
	private static Channel readTunerInfos(DataInputStream is, Channel c) throws IOException {
		int tunterType = is.readByte();
		int channelGroup = is.readByte();
		int satModulationSystem = is.readByte();
		int flags = is.readByte();
		int frequency = is.readInt();
		int symbolrate = is.readInt();
		short lnbLof = is.readShort();
		short pmtPid = is.readShort();
		short reserved1 = is.readShort();
		byte satModulation = is.readByte();
		byte avFormat = is.readByte();
		byte fec = is.readByte();
		byte reserved2 = is.readByte();
		short reserved3 = is.readShort();
		byte polarity = is.readByte();
		byte reserved4 = is.readByte();
		int orbitalPosition = byteArrayToInt(readWord(is));
		byte tone = is.readByte();
		byte reserved6 = is.readByte();
		short diseqCext = is.readShort();
		byte disecq = is.readByte();
		byte reserved7 = is.readByte();
		short reserved8 = is.readShort();
		int audioPID = byteArrayToInt(readWord(is));
		short reserved9 = is.readShort();
		int videoPID = byteArrayToInt(readWord(is));
		int transportStreamID = byteArrayToInt(readWord(is));
		short teletextPID = is.readShort();
		int originalNetworkId = byteArrayToInt(readWord(is));
		int serviceId = byteArrayToInt(readWord(is));
		BitSet bitSet = convert(flags);
		boolean isadditionalAudio = bitSet.get(7);
		int tvRadioFLag = bitSet.get(3) ? 1 : bitSet.get(4) ? 2 : 0;
//		long channelId = generateChannelId(tunterType, audioPID, serviceId);
		long channelId = generateChannelId(serviceId, audioPID, tunterType, transportStreamID, orbitalPosition, tvRadioFLag);
		long favId = generateFavId(tunterType + 1, serviceId);
		long epgId = generateEPGId(tunterType, originalNetworkId, transportStreamID, serviceId);
		short rcrPID = is.readShort();
		c.setEpgID(epgId);
		c.setId(channelId);
		c.setFavID(favId);
		if (isadditionalAudio) {
			c.setFlag(Channel.FLAG_ADDITIONAL_AUDIO);
		}
		return c;
	}

	/**
	 * Generate epg id.
	 *
	 * @param tunerType the tuner type
	 * @param networkId the network id
	 * @param streamId the stream id
	 * @param serviceId the service id
	 * @return the long©
	 * @author RayBa
	 * @date 05.07.2012
	 */
	private static long generateEPGId(int tunerType, int networkId, int streamId, int serviceId) {
		long epgId = 0;
		/**
		 * Formel: (TunerType + 1)*2^48 + NID*2^32 + TID*2^16 + SID
		 */
		epgId = (long) ((tunerType + 1) * Math.pow(2, 48) + networkId * Math.pow(2, 32) + streamId * Math.pow(2, 16) + serviceId);
		return epgId;
	}

	/**
	 * Generate channel id.
	 *
	 * @param tunerType the tuner type
	 * @param audioPID the audio pid
	 * @param serviceId the service id
	 * @return the long©
	 * @author RayBa
	 * @date 05.07.2012
	 */
	public static long generateChannelId(int tunerType, int audioPID, int serviceId) {
		long channelId = 0;
		/**
		 * Formel: (tunertype + 1) * 536870912 + APID * 65536 + SID
		 */
		channelId = (long) ((tunerType + 1) * 536870912 + audioPID * 65536 + serviceId);
		return channelId;
	}
	
	public static long generateChannelId(int serviceId, int audioPID, int tunerType, int transportstreamID, int orbitalPosition, int tvRadioFlag) {
		long channelId = 0;
		/**
		 * Service ID + AudioPID x 2^16 + (Tunertyp+1) x 2^29 + TransportstreamID x 2^32 + (OrbitalPosition x 10) x 2^48 + TV/Radio-Flag x 2^61
		 */
		long audioPIDPowered = audioPID * (BigInteger.valueOf(2).pow(16)).longValue();
		long tunerTypePowered = (tunerType +1) * (BigInteger.valueOf(2).pow(29)).longValue();
		long transportstreamIDPowered = transportstreamID * (BigInteger.valueOf(2).pow(32)).longValue();
		long orbitalPositionPowered = orbitalPosition * (BigInteger.valueOf(2).pow(48)).longValue();
		long tvRadioFlagPowered = tvRadioFlag * (BigInteger.valueOf(2).pow(61)).longValue();
		channelId = serviceId +audioPIDPowered+tunerTypePowered+transportstreamIDPowered+orbitalPositionPowered+tvRadioFlagPowered;
		return channelId;
	}

	/**
	 * Generate fav id.
	 *
	 * @param tunerType the tuner type
	 * @param serviceId the service id
	 * @return the long©
	 * @author RayBa
	 * @date 05.07.2012
	 */
	public static long generateFavId(int tunerType, int serviceId) {
		long channelId = 0;
		channelId = (long) ((tunerType + 1) * 536870912 + serviceId);
		return channelId;
	}

	/**
	 * Byte array to int.
	 *
	 * @param bytes the bytes
	 * @return the int©
	 * @author RayBa
	 * @date 05.07.2012
	 */
	private static int byteArrayToInt(byte[] bytes) {
		int value = 0;
		for (int i = 0; i < bytes.length; i++) {
			value += (bytes[i] & 0xff) << (8 * i);
		}
		return value;
	}

	/**
	 * Convert.
	 *
	 * @param value the value
	 * @return the bit set©
	 * @author RayBa
	 * @date 05.07.2012
	 */
	private static BitSet convert(int value) {
		BitSet bits = new BitSet();
		int index = 0;
		while (value != 0) {
			if (value % 2L != 0) {
				bits.set(index);
			}
			++index;
			value = value >>> 1;
		}
		return bits;
	}

	/**
	 * Read bytes.
	 *
	 * @param is the is
	 * @param length the length
	 * @return the byte[]©
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @author RayBa
	 * @date 05.07.2012
	 */
	private static byte[] readBytes(DataInputStream is, int length) throws IOException {
		byte[] result = new byte[length];
		for (int i = 0; i < length; i++) {
			result[i] = readByte(is);
		}
		return result;
	}

	/**
	 * Read word.
	 *
	 * @param is the is
	 * @return the byte[]©
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @author RayBa
	 * @date 05.07.2012
	 */
	private static byte[] readWord(DataInputStream is) throws IOException {
		return readBytes(is, 2);
	}

	/**
	 * Read d word.
	 *
	 * @param is the is
	 * @return the byte[]©
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @author RayBa
	 * @date 05.07.2012
	 */
	private static byte[] readDWord(DataInputStream is) throws IOException {
		return readBytes(is, 4);
	}

	/**
	 * Read byte.
	 *
	 * @param is the is
	 * @return the byte©
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @author RayBa
	 * @date 05.07.2012
	 */
	private static byte readByte(DataInputStream is) throws IOException {
		return is.readByte();
	}

	/**
	 * Checks if is bit set.
	 *
	 * @param value the value
	 * @param bit the bit
	 * @return true, if is bit set
	 * @author RayBa
	 * @date 05.07.2012
	 */
	private static boolean isBitSet(byte value, int bit) {
		return (value & (1 << bit)) != 0;
	}

}
