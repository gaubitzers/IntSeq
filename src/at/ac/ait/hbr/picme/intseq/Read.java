package at.ac.ait.hbr.picme.intseq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Read {
	private String qName;		// Query template/pair NAME
	private int flag; 			// bitwise FLAG
	private String rName;		// Reference sequence NAME
	private int start;			// 
	private int mapQ;
	private String cigar;
	private String rNext;
	private int pNext;
	private int tLen;
	private String sequence;
	private String qual;
	private HashMap<String, String> tags;

	private List<Intron> intronsInside;

	private Transcript t; // Transcript

	public Read(int start) {
		this.setStart(start);
		// this.setLength(length);
		intronsInside = new ArrayList<Intron>();
		tags = new HashMap<String, String>();
	}

	public String getqName() {
		return qName;
	}

	public void setqName(String qName) {
		this.qName = qName;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLength() {
		int length = 0;
		for (String s : this.getCigar().split("[A-Z]")) {
			length += Integer.valueOf(s);
		}
		return length;
	}

	//
	// public void setLength(int length) {
	// this.length = length;
	// }

	public int getMapQ() {
		return mapQ;
	}

	public void setMapQ(int mapQ) {
		this.mapQ = mapQ;
	}

	public String getRName() {
		return rName;
	}

	public void setRName(String rName) {
		this.rName = rName;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String getQual() {
		return qual;
	}

	public void setQual(String qual) {
		this.qual = qual;
	}

	public String getCigar() {
		return cigar;
	}

	public void setCigar(String cigar) {
		this.cigar = cigar;
	}

	public String getrNext() {
		return rNext;
	}

	public void setrNext(String rNext) {
		this.rNext = rNext;
	}

	public int getpNext() {
		return pNext;
	}

	public void setpNext(int pNext) {
		this.pNext = pNext;
	}

	public int gettLen() {
		return tLen;
	}

	public void settLen(int tLen) {
		this.tLen = tLen;
	}

	public List<Intron> getIntronsInside() {
		return intronsInside;
	}

	public void setIntronsInside(List<Intron> introns) {
		this.intronsInside = introns;
	}

	public void addIntronInside(Intron i) {
		this.intronsInside.add(0, i);
	}

	public Transcript getT() {
		return t;
	}

	public void setT(Transcript t) {
		this.t = t;
	}

	public void addTag(String key, String value) {
		this.tags.put(key, value);
	}

	public String getTag(String key) {
		return this.tags.get(key);
	}

	public void setNewStart() {
		System.out.println(this.toDebugString());
		if (t.getSign() == '+') {
			int newStart = t.getExons().get(0).getStart() + this.getStart() - 1;
			int exonCount = t.getExons().size();
			if (exonCount > 1) {
				int i = 0;
				while (!(t.getExons().get(i).getEnd() >= newStart && newStart >= t
						.getExons().get(i).getStart())) {
					// newStart += (t.getExons().get(i).getStart() -
					// t.getExons()
					// .get(i + 1).getEnd()) - 1;
					newStart += t.getIntrons().get(i).getLength();
					this.addIntronInside(t.getIntrons().get(i));
					i++;

				}
			}
			this.setStart(newStart);
		} else {
			int endLastExon = t.getExons().get(t.getExons().size() - 1)
					.getEnd();
			int newStart = endLastExon - this.getLength() - this.getStart() + 2;
			int exonCount = t.getExons().size();
			if (exonCount > 1) {
				int i = exonCount - 1;
				while (!(t.getExons().get(i).getEnd() >= newStart && newStart >= t
						.getExons().get(i).getStart())) {
					// newStart -= (t.getExons().get(i).getStart() -
					// t.getExons()
					// .get(i - 1).getEnd()) - 1;
					if (i == 0) {
						System.out.println(i);
					}

					i--;
					newStart -= t.getIntrons().get(i).getLength();
					this.addIntronInside(t.getIntrons().get(i));

				}
			}
			this.setStart(newStart);
		}
	}

	public void processTranscript() {

		if (t.getSign() == '+') {

			/*
			 * Three_prime and five_prime bevore first exon
			 */
			int preStart = t.getExons().get(0).getStart() - t.getStart();
			int length = this.getLength();
			int start = t.getStart() + preStart + this.getStart() - 1;
			int end = start + length;
			int intrCount = t.getIntrons().size();

			if (intrCount > 0) {
				for (int i = 0; i < intrCount; i++) {
					if (t.getIntrons().get(i).getStart() < start) {
						start += t.getIntrons().get(i).getLength();
						end = start + length;
					} else {
						if (t.getIntrons().get(i).getStart() < end) {
							end += t.getIntrons().get(i).getLength();
							this.addIntronInside(t.getIntrons().get(i));
						} else {
							break;
						}
					}
				}
			}
			this.setStart(start);
		} else {
			int preStart = t.getEnd()
					- t.getExons().get(t.getExons().size() - 1).getEnd();
			int length = this.getLength();
			int end = t.getEnd() - preStart - this.getStart() + 2
					+ getNumInsertion() - getNumDeletions();
			int start = end - length;

			int intrCount = t.getIntrons().size();

			if (intrCount > 0) {
				for (int i = intrCount - 1; i >= 0; i--) {
					if (t.getIntrons().get(i).getEnd() > end) {
						end -= t.getIntrons().get(i).getLength();
						start = end - length;
					} else {
						if (t.getIntrons().get(i).getEnd() > start) {
							start -= t.getIntrons().get(i).getLength();
							this.addIntronInside(t.getIntrons().get(i));
						} else {
							break;
						}
					}
				}
			}
			this.setStart(start);
		}
	}

	public void adaptCigarString() {
		ArrayList<String> cigarParts;

		/*
		 * if there are no introns in the read and the transcript is not
		 * reversed or there is no Cigar String available we can return it
		 * unchanged
		 */

		if (this.getIntronsInside().isEmpty() && t.getSign() == '+'
				|| this.getCigar().equalsIgnoreCase("*")) {
			return;
		}

		cigarParts = Util.getCigarParts(this.getCigar());

		/*
		 * if the transcript is reversed we have to reverse the cirar string as
		 * well
		 */
		if (t.getSign() == '-') {
			cigarParts = Util.reverseArrayList(cigarParts);
			StringBuffer sb = new StringBuffer();
			for (String s : cigarParts) {
				sb.append(s);
			}
			this.setCigar(sb.toString());
		}

		/*
		 * in case of introns in the read we have to disassemble the string and
		 * include all introns
		 */
		if (!this.getIntronsInside().isEmpty()) {
			this.setCigar(insertIntronCigar(cigarParts));
		}

	}

	/**
	 * @param cigarParts
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String insertIntronCigar(ArrayList<String> cigarParts) {
		int iRelStart;
		int partLength;
		int pos = 0;
		boolean done;
		String partType;

		ArrayList<String> cigResult = new ArrayList<String>();

		for (Intron i : this.getIntronsInside()) {
			cigResult.clear();
			done = false;
			pos = 0;
			iRelStart = i.getStart() - this.getStart();
			for (String s : cigarParts) {
				partLength = Integer.valueOf(s.substring(0, s.length() - 1));
				partType = s.substring(s.length() - 1);
				if (partType.equalsIgnoreCase("I")) {
					cigResult.add(partLength + partType);
					cigarParts = (ArrayList<String>) cigResult.clone();
					continue;
				}
				pos += partLength;
				if (pos == iRelStart) {
					iRelStart--;
				}
				if (pos > iRelStart && !done) {
					if (partType.equalsIgnoreCase("D")) {
						int intronLength = partLength + i.getLength();
						cigResult.add(intronLength + "D");
						cigarParts = (ArrayList<String>) cigResult.clone();
						done = true;
					} else {
						cigResult
								.add(partLength - (pos - iRelStart) + partType);
						cigResult.add(i.getLength() + "D");
						cigResult.add(pos - iRelStart + partType);
						cigarParts = (ArrayList<String>) cigResult.clone();
						done = true;
					}
				} else {
					cigResult.add(partLength + partType);
					cigarParts = (ArrayList<String>) cigResult.clone();
				}
			}
			if (done == false) {
				cigResult.add(i.getLength() + "D");
				cigarParts = (ArrayList<String>) cigResult.clone();
			}
		}
		StringBuffer sb = new StringBuffer();
		for (String s : cigResult) {
			sb.append(s);
		}
		return sb.toString();
	}

	public void adaptMdString() {
		ArrayList<String> mdParts;

		// return if nothing should be changed
		if (this.getIntronsInside().isEmpty() && t.getSign() == '+') {
			return;
		}

		// reverse the MD-String
		String stmp = tags.get("MD").substring(5);

		mdParts = splitMdString(stmp);
		if (t.getSign() == '-') {
			mdParts = Util.reverseArrayList(mdParts);
			mdParts = Util.complementArrayList(mdParts);

			StringBuffer sb = new StringBuffer();
			for (String s : mdParts) {
				sb.append(s);
			}
			tags.put("MD", "MD:Z:" + sb.toString());

		}

		// insert the intron if present
		if (!this.getIntronsInside().isEmpty()) {
			tags.put("MD", "MD:Z:" + insertIntronMd(mdParts));
		}
	}

	private String insertIntronMd(ArrayList<String> mdParts) {
		int distance;
		int pos = 0;
		ArrayList<String> mdResult = new ArrayList<String>();

		for (Intron i : this.getIntronsInside()) {
			distance = i.getStart() - this.getStart();
			for (String s : mdParts) {
				if (Read.isNumeric(s)) {
					pos += Integer.valueOf(s);
				} else if (s.startsWith("^")) {
					pos += s.length() - 1;
				} else {
					pos++;
				}
				if (pos > distance) {
					if (Read.isNumeric(s)) {
						mdResult.add(String.valueOf(distance
								- (pos - Integer.valueOf(s))));
						mdResult.add("^");
						for (int x = 0; x < i.getLength(); x++) {
							mdResult.add("N");
						}
						mdResult.add(String.valueOf(pos - distance));
						pos += i.getLength();
						distance = Integer.MAX_VALUE;
					} else {
						mdResult.add("^");
						for (int x = 0; x < i.getLength(); x++) {
							mdResult.add("N");
						}
						mdResult.add(s);
						distance = Integer.MAX_VALUE;
					}
				} else {
					mdResult.add(s);
				}
			}
		}
		StringBuffer sb = new StringBuffer();
		for (String s : mdResult) {
			sb.append(s);
		}
		return sb.toString();
	}

	private ArrayList<String> splitMdString(String mDString) {
		ArrayList<String> mdParts = new ArrayList<String>();
		boolean intron = false;
		char[] md = mDString.toCharArray();
		String tmp = "";
		for (int i = 0; i < md.length; i++) {
			if (md[i] >= '0' && md[i] <= '9') {
				if (intron) {
					mdParts.add(tmp);
					tmp = "";
					intron = false;
				}
				tmp = tmp.concat(String.valueOf(md[i]));
			} else {
				if (intron) {
					tmp = tmp.concat(String.valueOf(md[i]));
				} else {
					if (md[i] == '^') {
						intron = true;
						mdParts.add(tmp);
						tmp = "";
						tmp = tmp.concat(String.valueOf(md[i]));
					} else {
						mdParts.add(tmp);
						tmp = "";
						mdParts.add(String.valueOf(md[i]));
					}
				}
			}
		}
		mdParts.add(tmp);
		return mdParts;
	}

	public String toDebugString() {
		StringBuffer sb = new StringBuffer();
		sb.append("------------READ------------");
		sb.append(Util.newline);
		sb.append("rName: " + this.getRName());
		sb.append(Util.newline);
		sb.append("qName: " + this.getqName());
		sb.append(Util.newline);
		sb.append("Start: " + this.getStart());
		sb.append(" Length: " + this.getLength());
		sb.append(Util.newline);
		sb.append("CIGAR: " + this.getCigar());
		sb.append(Util.newline);
		sb.append("Introns bevore: ");
		sb.append(Util.newline);
		sb.append("Introns inside: ");
		sb.append(Util.newline);
		for (Intron i : this.getIntronsInside()) {
			sb.append(i.toString());
		}

		return sb.toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getqName());
		sb.append("\t");
		sb.append(this.getFlag());
		sb.append("\t");
		sb.append(this.getRName());
		sb.append("\t");
		sb.append(this.getStart());
		sb.append("\t");
		sb.append(this.getMapQ());
		sb.append("\t");
		sb.append(this.getCigar());
		sb.append("\t");
		sb.append(this.getrNext());
		sb.append("\t");
		sb.append(this.getpNext());
		sb.append("\t");
		sb.append(this.gettLen());
		sb.append("\t");
		sb.append(this.getSequence());
		sb.append("\t");
		sb.append(this.getQual());
		for (Map.Entry<String, String> entry : tags.entrySet()) {
			sb.append("\t");
			sb.append(entry.getValue());
		}
		return sb.toString();
	}

	private static boolean isNumeric(String str) {
		try {
			@SuppressWarnings("unused")
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public void adaptSequence() {
		if (this.getIntronsInside().isEmpty()) {
			return;
		} else {

			for (Intron i : this.getIntronsInside()) {
				String oriSequence = this.getSequence();
				String oriQual = this.getQual();
				StringBuffer sbSeq = new StringBuffer();
				StringBuffer sbQual = new StringBuffer();
				int distance = i.getStart() - this.getStart();

				if (distance == oriSequence.length()) {
					distance--;
				} else if (distance > oriSequence.length()) {
					distance = oriSequence.length();
				}

				sbSeq.append(oriSequence.substring(0, distance));
				sbQual.append(oriQual.substring(0, distance));

				for (int n = 0; n < i.getLength(); n++) {
					sbSeq.append("N");
					sbQual.append("?");
				}
				sbSeq.append(oriSequence.substring(distance,
						oriSequence.length()));
				sbQual.append(oriQual.substring(distance, oriQual.length()));
				this.setSequence(sbSeq.toString());
				this.setQual(sbQual.toString());
			}
		}

	}

	private int getNumInsertion() {
		return Util.getLeadNum(this.getCigar(), "I");
	}

	private int getNumDeletions() {
		return Util.getLeadNum(this.getCigar(), "D");
	}

}
