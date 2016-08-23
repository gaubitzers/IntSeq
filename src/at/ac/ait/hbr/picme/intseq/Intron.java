package at.ac.ait.hbr.picme.intseq;

public class Intron extends NucSequence {

	public Intron(int start, int end) {
		super(start, end);
	}

	@Override
	public String toString() {
		return "Intron|" + "Start:" + this.start + " End:" + this.end
				+ " Length: " + this.getLength() + "\r\n";
	}
}