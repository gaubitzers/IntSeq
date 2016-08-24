# IntSeq

## About the IntSeq project

To visualise and compare Sequences in the IGV Viewer we needed to insert missing
intron information into the SAM file.

You can ether use the command line tool to create your file or the GUI
version. Both implement the same algorithm.

## Input

The following files are needed to feed the tool:
- SAM input file to process
- GFF file with chromosome names, intron information and reference-names
- FASTA file with length information from the chromosome

## Output

- SAM output file with introns included
- [optional] BED file with coordinates from transcriptomes
- [optional] BAM file if SAMTOOLS are installed and the checkbox for converting
the output file is activated.

## Processing steps

- Get transcript list  
reads the "reference sequence name" from the input SAM file into a
transcript list (simple Strings). The transcripts should be stored in the
beginning of the file and each "reference sequence name" is located in
one line.
- Enrich the list with exon information from the gff file    
the gff file holds all the information for the introns and exons. The programm is searching for the string "mRNA",
checks if the transcript is already in the transcript list and adds exon information (lenght, position) by searching for following "CDS" strings.
- For each transcript fill the gaps in between the exons with introns
- Create a list of chromosomes in which transcripts are located the fna file, it is used to get also the size of the chromosome
- for each sequence in the input SAM file do the following steps
    - Change the Identifier to the chromosome name
    - calculate the new Start of the transcriptome
    - adapt CIGAR String
    - adapt MD String
    - if Sign = '-' reverse and complement the Sequence; reverse the Quality String
    - adapt pNext and tLen
    - also a check for the pair is implemented
- If selected create a second file with the coordinates of the Chromosomes it can be used to locate the positions in the IGV viewer.
- If SAMTOOLS are installed and the option to convert the file also to BAM format the new file will automatically converted into a BAM file with the same name.
