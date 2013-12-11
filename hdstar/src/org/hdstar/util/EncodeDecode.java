package org.hdstar.util;

public class EncodeDecode {

	// ‘› ±œ»”√
	public static String encode(String s) {
		if (s == null) {
			return null;
		}
		String s1 = "";
		String s2 = "";
		if (!s.equals("")) {
			byte abyte0[];
			int i;
			int j;
			try {
				abyte0 = s.getBytes("utf-8");

				i = abyte0.length;
				j = 0;

				for (int k = i / 2; j < k; j++) {
					int j1 = (i - j) + -1;
					byte byte0 = abyte0[j];
					byte byte1 = abyte0[j1];
					abyte0[j] = byte1;
					abyte0[j1] = byte0;
					j = j + 1;
				}

				String s3;
				j = 0;
				s3 = "";

				for (; j < i; j++) {
					String s4 = "";
					byte byte2 = (byte) (abyte0[j] ^ 126);
					Object aobj[] = new Object[1];
					Byte byte3 = Byte.valueOf(byte2);
					aobj[0] = byte3;
					String s5 = String.format("%x", aobj);
					String s8;
					String s9;
					if (s5.length() == 1) {
						String s6 = String.valueOf(s4);
						String s7 = String.valueOf((new StringBuilder(s6))
								.append("0").toString());
						s4 = (new StringBuilder(s7)).append(s5).toString();
					} else {
						String s10 = String.valueOf(s4);
						s4 = (new StringBuilder(s10)).append(s5).toString();
					}
					s8 = String.valueOf(s3);
					s9 = (new StringBuilder(s8)).append(s4).toString();
					s3 = s9;
				}

				abyte0 = s3.getBytes();
				i = abyte0.length;
				j = 0;

				for (int l = i / 2; j < l; j++) {

					byte byte4 = abyte0[j];
					int l1 = (i - j) + -1;
					byte byte5 = abyte0[l1];
					abyte0[j] = byte5;
					int i2 = (i - j) + -1;
					abyte0[i2] = byte4;
				}

				// wjh
				i = abyte0.length;
				j = 1;
				for (int word = i / 2, replaceCount = word / 2; j < replaceCount; j++) {
					if (1 == j % 2) {

						int destIndex = (word - j) + -1;
						int srcIndex1 = j << 1;
						int srcIndex2 = srcIndex1 + 1;
						int desIndex1 = destIndex << 1;
						int desIndex2 = desIndex1 + 1;

						byte byteSrc1 = abyte0[srcIndex1];
						byte byteSrc2 = abyte0[srcIndex2];
						byte byteDest1 = abyte0[desIndex1];
						byte byteDest2 = abyte0[desIndex2];

						abyte0[srcIndex1] = byteDest1;
						abyte0[srcIndex2] = byteDest2;
						abyte0[desIndex1] = byteSrc1;
						abyte0[desIndex2] = byteSrc2;
					}
				}

				s1 = new String(abyte0);
				s2 = s1;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return s2;

	}

	public static String decode(String s) {
		if (s == null) {
			return null;
		}
		String s1 = "";
		String s2 = "";
		if (!s.equals("")) {
			byte abyte0[];
			int i;
			int j;
			try {
				abyte0 = s.getBytes("utf-8");
				i = abyte0.length;
				j = 0;

				for (int k = i / 2; j < k; j++) {
					int j1 = (i - j) + -1;
					byte byte0 = abyte0[j];
					byte byte1 = abyte0[j1];
					abyte0[j] = byte1;
					abyte0[j1] = byte0;
				}

				String s3;
				s3 = new String(abyte0);
				abyte0 = new byte[i / 2];
				j = 0;
				for (int l = i / 2; j < l; j++) {

					int l1 = j * 2;
					int i2 = j * 2 + 2;
					byte byte2 = (byte) (Integer.valueOf(s3.substring(l1, i2),
							16).intValue() ^ 126);
					abyte0[j] = byte2;
				}

				i = abyte0.length;
				j = 0;
				for (int i1 = i / 2; j < i1; j++) {
					int j2 = (i - j) + -1;
					byte byte3 = abyte0[j];
					byte byte4 = abyte0[j2];
					abyte0[j] = byte4;
					abyte0[j2] = byte3;
				}

				s1 = new String(abyte0, "UTF-8");
				s2 = s1;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return s2;
	}
}
