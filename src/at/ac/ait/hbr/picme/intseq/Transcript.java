package at.ac.ait.hbr.picme.intseq;

import java.util.ArrayList;
import java.util.List;

public class Transcript {

    private String rName;
    private String chromosome;
    private int start;
    private int end;
    private char sign;
    private Intron fivePrimeUTR;
    private Intron threePrimeUTR;
    private List<Intron> introns;
    private List<Exon> exons;

    public Transcript(String rName) {
        this.setrName(rName);
        introns = new ArrayList<>();
        exons = new ArrayList<>();
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public String getrName() {
        return rName;
    }

    public void setrName(String rName) {
        this.rName = rName;
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

    public int getLenth() {
        return this.getEnd() - this.getStart() + 1;
    }

    public char getSign() {
        return sign;
    }

    public void setSign(char sign) {
        this.sign = sign;
    }

    public Intron getFivePrimeUTR() {
        return fivePrimeUTR;
    }

    public void setFivePrimeUTR(Intron fivePrimeUTR) {
        this.fivePrimeUTR = fivePrimeUTR;
    }

    public Intron getThreePrimeUTR() {
        return threePrimeUTR;
    }

    public void setThreePrimeUTR(Intron threePrimeUTR) {
        this.threePrimeUTR = threePrimeUTR;
    }

    public List<Intron> getIntrons() {
        return introns;
    }

    public void setIntrons(List<Intron> introns) {
        this.introns = introns;
    }

    public void addIntron(Intron i) {
        this.introns.add(i);
    }

    public List<Exon> getExons() {
        return exons;
    }

    public void setExons(List<Exon> exons) {
        this.exons = exons;
    }

    public void addExon(Exon e) {
        this.exons.add(e);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Transcript|").append(this.rName).append(" Chromosome:").append(this.chromosome);
        sb.append(Util.newline);
        sb.append(" Start:").append(this.start).append(" End: ").append(this.end).append(" Length:").append(this.getLenth()).append(" Sign:").append(this.sign);
        sb.append(Util.newline);
        for (Intron i : this.introns) {
            sb.append(i.toString());
        }
        for (Exon e : this.exons) {
            sb.append(e.toString());
        }

        return sb.toString();
    }
}
