/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.ait.hbr.picme.intseq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gaubitzers
 */
public class IntSeq {

    static List<String> transcriptList = new ArrayList<>();
    static List<Read> readList = new ArrayList<>();
    static Map<String, Integer> chromosomeList = new HashMap<>();
    static ArrayList<Transcript> tl = new ArrayList<>();
    private File oFile, iFile, gffFile, cFile, fnaFile;
    static String VERSION = "0.1";
    private final boolean firstSequenceLine;
    static boolean samTools;
    BufferedWriter bw;
    BufferedReader br;

    /* logging */
    static int negReads;

    public IntSeq() {
        this.firstSequenceLine = true;
    }
    
    /**
     *
     */
    public void run() {
        // open the SAM inputfile and the SAM outputfile
		br = openInputFile();
		bw = openOutputFile();
                
		// get transcriptlist from SAM file
		// for input of the GFF file data extraction
		getTranscriptList(br);
		System.out.println("transcript list loaded...");

		// get GFF data
		tl = FileUtil.getTranskiptInformation(tl, gffFile);
		System.out.println("additional transcript information loaded...");

		// enrich the List with introns in between the exons
		FileUtil.addTranskriptIntrons(tl);

		// fill a list with used cromosomes and their size
		getChromosomeSize();
		System.out.println("chromosomes for used transcipts loaded...");

		// print the header of the output file with the chromosome names and
		// sizes
		printHeader(bw);
		System.out.println("print SAM-header");

		// process the input SAM file line by line
		String cline;
		int i = 0;
		try {			
			while ((cline = br.readLine()) != null) {
				i++;
				if (i % 10000 == 0) {
					System.out.println(i + " reads processed..."
							+ " readlistsize: " + readList.size());
				}

				String s = checkLine(cline);
				if (s != null) {
					bw.write(s);
					bw.newLine();

				}
			}
			System.out.println(readList.size() + " reads without pair...");

		} catch (IOException e) {
			System.out.println("Could not write to output file "
					+ oFile.getName());
			System.out
					.println("please correct the filename and check permissions");
		}
		try {
			bw.close();
			br.close();

		} catch (IOException e) {
			System.out.println("Could not close the files correctly");
			e.printStackTrace();
		}

		/*
		 * create a second file with the coordinates of the Chromosomes it can
		 * be used to locate the positions in the IGV viewer.
		 */
		if (cFile != null) {
			FileOutputStream fos = null;
			if (cFile.getParent() != null
					&& !(new File(cFile.getParent()).exists())) {
				if (!new File(cFile.getParent()).mkdirs()) {
					System.out
							.println("creation of destionation folder failed.");
				}
			}
			try {
				fos = new FileOutputStream(cFile);

				BufferedWriter cbw = new BufferedWriter(new OutputStreamWriter(
						fos));
				for (Transcript t : tl) {
					cbw.write(t.getChromosome());
					cbw.write("\t");
					cbw.write(String.valueOf(t.getStart()));
					cbw.write("\t");
					cbw.write(String.valueOf(t.getEnd()));
					cbw.write("\n");
				}
				cbw.close();
			} catch (IOException e) {
				System.out
						.println("Error during creation of the coordinates file!");
			}
		}
		if (samTools) {
			System.out.println("generating BAM output (with SAMTOOLS)...");
			Runtime rt = Runtime.getRuntime();
			Process proc = null;
			int ret = 0;
			System.out.println(oFile.getName());
			String filename = oFile.getName().split("\\.")[0];
			String workingdir = oFile.getAbsoluteFile().getParentFile()
					.getAbsolutePath();
			String command = "samtools view -b -o " + workingdir + "/"
					+ filename + ".bam " + oFile.getAbsolutePath();
			System.out.println("Command: " + command);
			try {
				proc = rt.exec(command);
				ret = proc.waitFor();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			if (ret != 0) {
				System.out.println("Failure during conversation!");
				System.out.println("samtools returncode: " + ret);
			}
			System.out.println("sort BAM output (with SAMTOOLS)...");
			command = "samtools sort " + workingdir + "/" + filename + ".bam "
					+ workingdir + "/" + filename + "_sorted";
			System.out.println("Command: " + command);
			try {
				proc = rt.exec(command);
				ret = proc.waitFor();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			if (ret != 0) {
				System.out.println("Failure during sorting!");
				System.out.println("samtools returncode: " + ret);
			}

		}
		System.out.println("Finished!");
    }

    /**
     * Opens the input SAM file for reading
     *
     * @return the BufferedReader from the input file
     */
    private BufferedReader openInputFile() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(iFile));
        } catch (FileNotFoundException e) {
            System.out.println("Cannot open input SAM file " + iFile.getName());
            e.printStackTrace();
        }
        return br;
    }

    /**
     * Opens the output SAM file for writing
     *
     * @return the BufferedWriter for the result file
     */
    private BufferedWriter openOutputFile() {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(oFile);
        } catch (FileNotFoundException e) {
            System.out
                    .println("Cannot open output SAM file " + oFile.getName());
            e.printStackTrace();
        }
        return new BufferedWriter(new OutputStreamWriter(fos));
    }
    
    /**
     * prints the header of the output sam file with all chromosomes which are
     * used.
     *
     * @param bw writer of the output file
     */
    private void printHeader(BufferedWriter bw) {
        try {
            StringBuilder sb;
            sb = new StringBuilder();
            bw.write("@HD	VN:1.0	SO:unsorted");
            bw.write(Util.newline);

            for (Map.Entry<String, Integer> chromosome : chromosomeList
                    .entrySet()) {
                sb.append("@SQ\tSN:");
                sb.append(chromosome.getKey());
                sb.append("\tLN:");
                sb.append(chromosome.getValue());
                sb.append(Util.newline);
                bw.write(sb.toString());
                sb.setLength(0);
            }
        } catch (IOException e) {
            System.out.println("Could not write to output file "
                    + oFile.getName());
            System.out
                    .println("please correct the filename and check permissions");
            e.printStackTrace();
        }

    }

    /**
     * reads the "reference sequence name" from the input SAM file into the
     * transcript list (simple Strings) The transcripts are stored in the
     * beginning of the file and each "reference sequence name" is located in
     * one line. After the creation of this list the Buffered Reader is set to
     * the beginning again.
     *
     * @param br the BufferedReader which points to the SAM input file
     */
    private void getTranscriptList(BufferedReader br) {
        String cline;
        try {
            br.mark(1);
            while ((cline = br.readLine()) != null) {
                if (cline.startsWith("@")) {
                    if (cline.startsWith("@SQ")) {
                        int startPos = cline.indexOf("SN:");
                        int endPos = cline.lastIndexOf('\t');
                        String transcript = cline.substring(startPos + 3,
                                endPos);
                        Transcript t = new Transcript(transcript);
                        tl.add(t);
                    }
                } else {
                    br.reset();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read from input file!");
            e.printStackTrace();
        }

    }

    private String checkLine(String line) {
        if (line.startsWith("@")) {
            return null;
        } else {
            return processSequence(line);
        }
    }

    /**
     * fills a list of chromosomes in which transcripts are located the fna file
     * is used to get also the size of the chromosome
     */
    private void getChromosomeSize() {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(fnaFile));
        } catch (FileNotFoundException e) {
            System.out
                    .println("Could not open the FNA file, please coorect the path: "
                            + fnaFile.getName());
        }
        String cline;
        try {
            String chromosome;
            Integer length;
            while ((cline = br.readLine()) != null) {
                if (cline.startsWith(">")) {
                    chromosome = cline.substring(1, cline.indexOf(" "));
                    length = Integer.valueOf(cline.substring(
                            cline.indexOf("length=") + 7, cline.length()));
                    for (Transcript t : tl) {
                        if (t.getChromosome().equalsIgnoreCase(chromosome)) {
                            chromosomeList.put(chromosome, length);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading the input FNA file "
                    + fnaFile.getName());
            System.out.println("please correct the filename");
            e.printStackTrace();
        }
    }

    /**
     * gets on read of the SAM file, splits the values, and converts them to the
     * right format.
     *
     * @param line read from the SAM file
     * @return converted read
     */
    private String processSequence(String line) {

        Transcript ct = null;

        String a[] = line.split("\t");

        Read r = new Read(Integer.valueOf((a[3])));
        r.setqName(a[0]);
        r.setFlag(Integer.valueOf(a[1]));
        r.setRName(a[2]);
        r.setMapQ(Integer.valueOf(a[4]));

        r.setCigar(a[5]);
        if (r.getCigar().equalsIgnoreCase("*")) {
            return null;
        }

        r.setrNext(a[6]);
        r.setpNext(Integer.valueOf(a[7]));
        r.settLen(Integer.valueOf(a[8]));
        r.setSequence(a[9]);
        r.setQual(a[10]);

        for (int i = 11; i < a.length; i++) {
            r.addTag(a[i].substring(0, 2), a[i].substring(0));
        }

        for (Transcript t : tl) {
            if (r.getRName().contains(t.getrName())) {
                ct = t;
            }
        }

        // Change the Identifier to the chromosome name
        r.setRName(ct.getChromosome());

        // calculate the new Start of the transcriptome
        r.setT(ct);
        r.processTranscript();
        // r.setNewStart();

        r.adaptCigarString();
        r.adaptMdString();

        if (r.getT().getSign() == '-') {
            r.setSequence(Util.reverseComplement(r.getSequence()));
            r.setQual(Util.reverse(r.getQual()));
            processFlags16(r);
            negReads++;
            /*
             * if the current read is already in the read list process the flags
             * of both reads and print them into the output file, remove the
             * located read from the list.
             */

            switch (r.getrNext()) {
                case "*":
                    break;
                case "=":
                    processFlags32(r);
                    break;
                default:
                    for (Transcript t : tl) {
                        if (t.getrName().equalsIgnoreCase(r.getrNext())) {
                            if (t.getSign() == '-') {
                                processFlags32(r);
                            } else {
                                break;
                            }
                        }
                    }
                    break;
            }
        }

        // adapt pNext and tLen
        if (r.getrNext().equalsIgnoreCase("=") && r.gettLen() == 0) {
            r.setpNext(r.getStart());
        } else if (r.getrNext().equalsIgnoreCase("=") && r.gettLen() != 0) {
            Read readPair = null;
            for (Read or : readList) {
                if (or.getqName().equalsIgnoreCase(r.getqName())) {
                    readPair = or;
                    break;
                }
            }
            if (readPair != null) {
                r.setpNext(readPair.getStart());
                readPair.setpNext(r.getStart());
                int tlen = r.getStart() + r.getLength() - readPair.getStart();
                readPair.settLen(tlen);
                r.settLen(-tlen);

                StringBuilder sb;
                sb = new StringBuilder();
                sb.append(readPair);
                sb.append(Util.newline);
                sb.append(r);
                readList.remove(readPair);
                return sb.toString();
            } else {
                readList.add(r);
                return null;
            }
        }

        return r.toString();
    }

    private static void processFlags16(Read r) {
        if (Util.isReverseComplemented(r.getFlag())) {
            r.setFlag(r.getFlag() - 16);
        } else {
            r.setFlag(r.getFlag() + 16);
        }

    }

    private static void processFlags32(Read r) {
        if (Util.isNextSegReverseComplemented(r.getFlag())) {
            r.setFlag(r.getFlag() - 32);
        } else {
            r.setFlag(r.getFlag() + 32);
        }
    }
     
    public void setIfile(File f) {
        this.iFile = f;
    }
    
    public void setOfile(File f) {
        this.oFile = f;
    }
    
    public void setGffFile(File f) {
        this.gffFile = f;
    }    
    
    public void setCFile(File f) {
        this.cFile = f;
    }
    
    public void setFnaFile(File f) {
        this.fnaFile = f;
    }
    
    public File getIfile() {
        return this.iFile;
    }
    public File getOfile() {
        return this.oFile;
    }
    
    public File getGffFile() {
        return this.gffFile;
    }
    
    public File getCfile() {
        return this.cFile;        
    }
    
    public File getFnaFile() {
        return this.fnaFile;
    }
            
}
