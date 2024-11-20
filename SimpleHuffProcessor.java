/*  Student information for assignment:
 *
 *  On our honor, Vihaan Mehta and Seheon (David) Chang, this programming assignment is our own work
 *  and we have not provided this code to any other student.
 *
 *  Number of slip days used: 0
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID: vjm655
 *  email address: mehtavihaanj@gmail.com
 *  Grader name: Gracelynn
 *
 *  Student 2
 *  UTEID: sc68766
 *  email address: seheon.chang@utexas.edu
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private PriorityQueue314<TreeNode> frequencyQueue;
    private HuffmanTree<TreeNode> compressionHuffTree;
    private Map<Integer, String> compressionHuffMap;
    private int[] freq;
    private int peof;

    public SimpleHuffProcessor() {
        frequencyQueue = new PriorityQueue314<>();
        compressionHuffTree = new HuffmanTree<>();
        freq = new int[IHuffConstants.ALPH_SIZE];
        compressionHuffMap = new HashMap<>();
    }

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * 
     * @param in           is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind
     *                     of header to use, standard count format, standard tree
     *                     format, or possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     *         Note, to determine the number of
     *         bits saved, the number of bits written includes
     *         ALL bits that will be written including the
     *         magic number, the header format number, the header to
     *         reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {

        BitInputStream bits = new BitInputStream(in);
        int inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
        // total - magic - headerFormat - header - totalCompressedinHuffman

        // Also make sure that it is equal to the magic number
        if ((headerFormat != IHuffConstants.STORE_COUNTS &&
                headerFormat != IHuffConstants.STORE_TREE) || inbits == -1) {
            System.out.println("Invalid header format");
            myViewer.showError("Error reading compressed file, "
                    + "did not start with magic huff number.");
            return -1;
        }
        freq = new int[IHuffConstants.ALPH_SIZE];
        // 0~255 + 256(Pseudo-EOF)
        int numBits = 0;
        while (inbits != -1) {
            freq[inbits]++;
            inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
            System.out.println("inbits: " + inbits);
            numBits += IHuffConstants.BITS_PER_WORD;
        }

        for (int i = 0; i < freq.length; i++) {
            if (freq[i] > 0) {
                frequencyQueue.enqueue(new TreeNode(i, freq[i]));
            }
        }

        // Adding PEOF value
        frequencyQueue.enqueue(new TreeNode(ALPH_SIZE, 1));


        // Generate huffman tree
        compressionHuffTree = new HuffmanTree<>(frequencyQueue);
        compressionHuffMap = compressionHuffTree.getHuffManCodes();
        peof = compressionHuffTree.getPEOFCode();

        int headerInfo = BITS_PER_INT * 2;
        headerInfo += headerFormat == IHuffConstants.STORE_COUNTS ?
                ALPH_SIZE * BITS_PER_INT : compressionHuffTree.treeHeader().length();
        in.close();


        // Number of bits in file minus 32 for magic number, minus 32 for STORE_COUNTS/
        // STORE_TREE constant, then subtract the amount of bits in compressed file,
        // then subtract header for compressed file (32 bits)
        return numBits - (headerInfo + compressionHuffTree.getSumOfAllCodes());
    }

    /**
     * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>pxreprocessCompress</code>
     * storing state used by this call.
     * <br>
     * pre: <code>preprocessCompress</code> must be called before this method
     * 
     * @param in    is the stream being compressed (NOT a BitInputStream)
     * @param out   is bound to a file/stream to which bits are written
     *              for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than
     *              the input file.
     *              If this is false do not create the output file if it is larger
     *              than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {

        int bitsWritten = 0;
        // Write magic number, SCF/STF constant, header data to rebuild tree, compressed
        // data,
        // and PEOF value
        BitOutputStream outBits = new BitOutputStream(out);
        BitInputStream inBitStream = new BitInputStream(in);

        outBits.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.MAGIC_NUMBER);

        // Check if BITS PER INT here is redundant
        outBits.writeBits(BITS_PER_INT, IHuffConstants.STORE_COUNTS);

        // TODO: Check what format the file is in, based on this write SCF/STF constant
        // accordingly
        // For now: write data for SCF
        for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {

            outBits.writeBits(IHuffConstants.BITS_PER_INT, freq[k]);
            bitsWritten += IHuffConstants.BITS_PER_INT;
        }

        int inBits = inBitStream.readBits(IHuffConstants.BITS_PER_WORD);
        bitsWritten += IHuffConstants.BITS_PER_INT * 2 + compressionHuffTree.getSumOfAllCodes();

        while (inBits != -1) {

            // TODO: The compressionMap needs to have its keys changed to Strings instead of integers
            // TODO: because of integer overflow on very long strings (ex: 0110111101111000011110)
            String writeBitString = compressionHuffMap.get(inBits);
                    outBits.writeBits(writeBitString.length(),
                    Integer.parseInt(writeBitString));
            inBits = inBitStream.readBits(IHuffConstants.BITS_PER_WORD);
        }
        outBits.writeBits(String.valueOf(peof).length(), peof);

        //System.out.println("bits written: " + bitsWritten);
        return bitsWritten;
    }

    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     * 
     * @param in  is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     *                     writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {

        BitInputStream inputStream = new BitInputStream(in);
        BitOutputStream outputStream = new BitOutputStream(out);
        int bitsWritten = 0;

        int magicNum = inputStream.readBits(BITS_PER_INT);
        int headerFormat = inputStream.readBits(BITS_PER_INT);

        if (magicNum != MAGIC_NUMBER || (headerFormat != STORE_COUNTS && headerFormat != STORE_TREE)) {
            throw new IOException("Invalid magic number.");
        }
        PriorityQueue314<TreeNode> decompressQueue = new PriorityQueue314<>();
        // ONLY FOR SCF
        for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
            int headerBit = Integer.parseInt("" + inputStream.readBits(BITS_PER_INT));
            if (headerBit > 0) {
                // value, frequency
                // Is the value encoded the binary equivalent of number in the alphabet?
                // additinally, how may bits read at once
                decompressQueue.enqueue(new TreeNode(k, headerBit));
            }
        }
        decompressQueue.enqueue(new TreeNode(ALPH_SIZE, 1));
        HuffmanTree<TreeNode> decompressionTree = new HuffmanTree<>(decompressQueue);
        HashMap<String, String> decompressMap = decompressionTree.getDecompressionCodes();

        int inBits = inputStream.readBits(IHuffConstants.BITS_PER_WORD);
        int newPEOF = decompressionTree.getPEOFCode();
        StringBuilder currentValue = new StringBuilder();
        currentValue.append(inBits);

        while (inBits != -1 && !currentValue.toString().equals(String.valueOf(newPEOF))) {

            String entry = decompressMap.get(currentValue.toString());
            if (entry != null) {
                currentValue.setLength(0);
                outputStream.writeBits(IHuffConstants.BITS_PER_WORD,
                        Integer.parseInt(entry));
                bitsWritten += IHuffConstants.BITS_PER_WORD;
            }
            inBits = inputStream.readBits(1);
            currentValue.append(inBits);
        }

         return bitsWritten;
    }



    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    private void showString(String s) {
        if (myViewer != null) {
            myViewer.update(s);
        }
    }

    private static class Tuple implements Comparable<Tuple> {
        private int code;
        private int frequency;

        public Tuple(int code, int frequency) {
            this.code = code;
            this.frequency = frequency;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (!(o instanceof Tuple)) {
                return false;
            }
            Tuple tuple = (Tuple) o;
            return code == tuple.code;
        }

        @Override
        public int compareTo(Tuple o) {
            return frequency - o.frequency;
        }
    }
}
