package at.ac.ait.hbr.picme.intseq;

public abstract class NucSequence {

	int start;
	int end;
	String sequence;

	public NucSequence(int start, int end) {
		this.setStart(start);
		this.setEnd(end);
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getLength() {
		return this.end - this.start + 1;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

}
