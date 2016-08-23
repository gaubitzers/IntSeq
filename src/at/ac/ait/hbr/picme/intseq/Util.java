package at.ac.ait.hbr.picme.intseq;

import java.util.ArrayList;
import java.util.BitSet;

public class Util {
	
	public static String newline = System.getProperty("line.separator");

	public static String reverse(String s) {
		return new StringBuilder(s).reverse().toString();
	}

	public static String complement(String s) {
		char[] seq = s.toCharArray();

		seq = complementCharArray(seq);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < seq.length; i++) {
			sb.append(seq[i]);
		}
		return sb.toString();
	}

	private static char[] complementCharArray(char[] ca) {
		char[] tmpca = new char[ca.length];
		char c;
		for (int i = 0; i < ca.length; i++) {
			c = ca[i];
			switch (c) {
			case 'C':
				c = 'G';
				break;
			case 'G':
				c = 'C';
				break;
			case 'A':
				c = 'T';
				break;
			case 'T':
				c = 'A';
				break;
			default:
				break;
			}
			tmpca[i] = c;
		}
		return tmpca;
	}

	public static String reverseComplement(String sequence) {
		String ret;
		ret = Util.reverse(sequence);
		ret = Util.complement(ret);
		return ret;
	}

	public static int getLeadNum(String input, String c) {
		ArrayList<String> cigarParts = getCigarParts(input);
		int num = 0;
		for (String s : cigarParts) {
			if (s.endsWith(c)) {
				num += Integer.valueOf(s.substring(0, s.length() - 1));
			}
		}
		return num;
	}

	public static ArrayList<String> getCigarParts(String cigarString) {
		ArrayList<String> cigarParts = new ArrayList<>();
		char[] cig = cigarString.toCharArray();
		String tmp = "";
		for (int i = 0; i < cig.length; i++) {
			if (cig[i] >= '0' && cig[i] <= '9') {
				tmp = tmp.concat(String.valueOf(cig[i]));
			} else {
				tmp = tmp.concat(String.valueOf(cig[i]));
				cigarParts.add(tmp);
				tmp = "";
			}
		}
		return cigarParts;
	}

	public static ArrayList<String> complementArrayList(
			ArrayList<String> mdParts) {
		ArrayList<String> tmpList = new ArrayList<>();
		for (String s : mdParts) {
			switch (s) {
			case "C":
				s = "G";
				break;
			case "G":
				s = "C";
				break;
			case "A":
				s = "T";
				break;
			case "T":
				s = "A";
				break;
			default:
				break;
			}

			tmpList.add(s);
		}
		return tmpList;
	}

	public static ArrayList<String> reverseArrayList(ArrayList<String> list) {
		ArrayList<String> retList = new ArrayList<>();

		for (int i = list.size() - 1; i >= 0; i--) {
			retList.add(list.get(i));
		}
		return retList;
	}
	

	public static boolean isReverseComplemented(int flag) {
		BitSet bs16 = new BitSet();
		bs16.set(4);

		BitSet current = fromInt(flag);
		return current.intersects(bs16);
	}

	public static boolean isNextSegReverseComplemented(int flag) {
		BitSet bs32 = new BitSet();
		bs32.set(5);
		BitSet current = fromInt(flag);
		return current.intersects(bs32);

	}
	
	public static boolean isNextSegmentInTemplateUnmapped(int flag) {
		BitSet bs8 = new BitSet();
		bs8.set(3);
		BitSet current = fromInt(flag);
		return current.intersects(bs8);
	}

	static BitSet fromInt(int num) {
		BitSet bs = new BitSet();
		for (int k = 0; k < Integer.SIZE; k++) {
			if (((num >> k) & 1) == 1) {
				bs.set(k);
			}
		}
		return bs;
	}

}
