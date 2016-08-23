package at.ac.ait.hbr.picme.intseq;

import java.util.ArrayList;

public class Cromosome {
	String cname;
	Integer clength;
	ArrayList<Transcript> transcripts;

	
	public Cromosome() {
		this.transcripts = new ArrayList<Transcript>();
	}
	public String getCname() {
		return cname;
	}

	public void setCname(String cname) {
		this.cname = cname;
	}

	public Integer getClength() {
		return clength;
	}

	public void setClength(Integer clength) {
		this.clength = clength;
	}

	public ArrayList<Transcript> getTranscripts() {
		return transcripts;
	}

	public void setTranscripts(ArrayList<Transcript> transcripts) {
		this.transcripts = transcripts;
	}

}
