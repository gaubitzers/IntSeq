package at.ac.ait.hbr.picme.intseq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

	/**
	 * enriches the transcript list with the information from the gffFile
	 * 
	 * @param tl
	 *            empty Transcript list
	 * @param gffFile
	 *            input file with intron/extron Information
	 * @return enriched transcript list
	 * 				the method can extract the cromosome of the transcriptome
	 * 				it will also add each exon which is located in the region pro transcriptome
	 */
	public static ArrayList<Transcript> getTranskiptInformation(ArrayList<Transcript> tl, File gffFile) {
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(gffFile));
		} catch (FileNotFoundException e) {
			System.out.println("The input GFF file " + gffFile.getName() + "doesn't exist.");
			System.out.println("please correct the filename");
			e.printStackTrace();
		}

		String cline;
		String procTranscript = "fsjule";
		int tn = -1;
		try {
			while ((cline = br.readLine()) != null) {
				if (cline.contains("mRNA")) {
					for (Transcript t : tl) {
						if (cline.contains(t.getrName())) {
							String a[] = cline.split("\t");
							t.setChromosome(a[0]);
							t.setStart(Integer.valueOf(a[3]));
							t.setEnd(Integer.valueOf(a[4]));
							t.setSign(a[6].charAt(0));
							procTranscript = t.getrName();
							tn++;
							break;
						}
					}
				} else if (cline.contains(procTranscript)) {
					if (cline.contains("CDS")) {
						String a[] = cline.split("\t");
						Exon e = new Exon(Integer.valueOf(a[3]), Integer.valueOf(a[4]));
						Transcript t = tl.get(tn);
						t.addExon(e);
					}
				}
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return tl;
	}

	/**
	 * For each transcript fill the gaps in between the exons with introns
	 * 
	 * @param tl
	 *            transcript list with already included exons
	 */
	public static void addTranskriptIntrons(ArrayList<Transcript> tl) {
		for (Transcript t : tl) {
			List<Exon> exonList = t.getExons();
			for (int i = 0; i < exonList.size() - 1; i++) {
				Intron intr = new Intron(exonList.get(i).getEnd() + 1, exonList.get(i + 1).getStart() - 1);
				t.addIntron(intr);
			}

		}
	}

}
