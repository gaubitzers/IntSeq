package at.ac.ait.hbr.picme.intseq;

public class Exon extends NucSequence {
	
	public Exon(int start, int end) {
		super(start, end);
	}

	@Override
	public String toString() {
		return "Exon|" + "Start:" + this.start + " End:" + this.end
				+ " Length: " + this.getLength() + Util.newline;
	}
}