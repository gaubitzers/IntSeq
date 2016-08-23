/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.ait.hbr.picme.intseq;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author gaubitzers
 */
public class IntSeqCLI {
    private static IntSeq isWorker;    
    private boolean samTools;
    /**
     *
     * @param argv
     */
    public static void main(String... argv) {
        isWorker =  new IntSeq();
        IntSeqCLI isCLI = new IntSeqCLI();
                
        // check arguments
        if (!isCLI.parseArguments(argv)) {
            return;
        }
        // check files
        if (!isCLI.checkFilenames()) {
            return;
        }        
        isWorker.run();
    }

    /**
     * parses the given arguments and returns true if everything is correct
     *
     * @param argv command arguments
     * @return true if arguments are OK
     */
    private boolean parseArguments(String[] argv) {
        CommandLine cmd = null;
        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();

        Option help = Option.builder("h").longOpt("help")
                .desc("print this message").build();
        Option version = Option.builder("v").longOpt("version")
                .desc("print the version information and exit").build();
        Option saminputFile = Option.builder("i").required().hasArg()
                .longOpt("ifile").desc("SAM input file to process").build();
        Option gffinputFile = Option
                .builder("gff")
                .required()
                .hasArg()
                .longOpt("gfffile")
                .desc("GFF file with chromosome names, intron information and referencenames")
                .build();
        Option outputFile = Option.builder("o").required().hasArg()
                .longOpt("ofile").desc("SAM output file").build();
        Option fnainputFile = Option.builder("fna").hasArg().longOpt("fnafile")
                .required()
                .desc("FNA file with length information from the chromosome ")
                .build();
        Option coordFile = Option.builder("c").hasArg().longOpt("cfile")
                .desc("BED file with coordinates from transcriptomes ").build();
        Option bamOutput = Option.builder("b").longOpt("bam")
                .desc("Convert the created SAM file also to a BAM file")
                .build();
        options.addOption(help);
        options.addOption(version);
        options.addOption(saminputFile);
        options.addOption(gffinputFile);
        options.addOption(fnainputFile);
        options.addOption(outputFile);
        options.addOption(coordFile);
        options.addOption(bamOutput);

        CommandLineParser parser = new DefaultParser();
        try {
            cmd = parser.parse(options, argv);
        } catch (MissingOptionException moe) {
            System.out.println("missing mandatory options:");
            formatter.printHelp("SamTool", options, true);
            return false;
        } catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
            return false;
        }

        if (cmd.hasOption("h")) {
            formatter.printHelp("SamTool", options, true);
            return false;
        }
        if (cmd.hasOption("v")) {
            System.out.println("SamTool version:" + IntSeq.VERSION);
            return false;
        }


        
        isWorker.setIfile(new File(cmd.getOptionValue("ifile")));
        isWorker.setOfile(new File(cmd.getOptionValue("ofile")));
        isWorker.setGffFile(new File(cmd.getOptionValue("gfffile")));
        isWorker.setFnaFile(new File(cmd.getOptionValue("fnafile")));
        if (cmd.hasOption("c")) {
            isWorker.setCFile(new File(cmd.getOptionValue("cfile")));
        }
        if (cmd.hasOption("b")) {
            if (System.getProperty("os.name").startsWith("Windows")) {
                System.out
                        .println("You need SamTools in order to convert the output to BAM format!");
                return false;
            }
            Runtime rt = Runtime.getRuntime();
            Process proc;
            try {
                proc = rt.exec("samtools");
                try {
                    proc.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                System.out
                        .println("You need SamTools in order to convert the output to BAM format!");
                return false;
            }
            // the exitstatus from samtools is 1 without options therefore we
            // dont have to check the exit status
            samTools = true;
        }
        return true;
    }

    /**
     * Checks if the files exist and if they are readable respectively writeable
     *
     * @return true if everything is OK
     */
    private boolean checkFilenames() {
        if (!isWorker.getIfile().exists()) {
            System.out.println("The input SAM file " + isWorker.getIfile().getName()
                    + " doesn't exist");
            System.out.println("please correct the filename");
            return false;
        } else if (!isWorker.getIfile().canRead()) {
            System.out.println("Cannot open input SAM file " + isWorker.getIfile().getName());
            return false;
        }
        if (!isWorker.getGffFile().exists()) {
            System.out.println("The input GFF file " + isWorker.getGffFile().getName()
                    + " doesn't exist.");
            System.out.println("please correct the filename");
            return false;
        } else if (!isWorker.getGffFile().canRead()) {
            System.out.println("Cannot open input GFF file "
                    + isWorker.getGffFile().getName());
            return false;
        }
        if (!isWorker.getFnaFile().exists()) {
            System.out.println("The input FNA file " + isWorker.getFnaFile().getName()
                    + " doesn't exist.");
            System.out.println("please correct the filename");
            return false;
        } else if (!isWorker.getFnaFile().canRead()) {
            System.out.println("Cannot open input FNA file "
                    + isWorker.getFnaFile().getName());
            return false;
        }

        if (isWorker.getOfile().exists()) {
            if (isWorker.getOfile().canWrite()) {
                System.out.println("The output file already exists!!");
                System.out.println("Do you want to overwrite it? (y/n)");
                Scanner keyboard = new Scanner(System.in);
                String input = "";
                while (true) {

                    if (input.equalsIgnoreCase("n")) {
                        keyboard.close();
                        return false;
                    } else if (input.equalsIgnoreCase("y")) {
                        keyboard.close();
                        return true;
                    }
                    input = keyboard.next();
                }
            } else {
                System.out.println("output file is not writeable.");
                return false;
            }
        } else {
            if (!new File(isWorker.getOfile().getParent()).exists()
                    && isWorker.getOfile().getParent() != null) {
                if (!new File(isWorker.getOfile().getParent()).mkdirs()) {
                    System.out
                            .println("creation of destionation folder failed.");
                    return false;
                }
            } else {
            }
        }
        return true;
    }

}
