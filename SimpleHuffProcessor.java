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

        // 0~255 + 256(Pseudo-EOF)
        int numBits = 0;
        while (inbits != -1) {
            freq[inbits]++;
            inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
            // System.out.println(inbits);
            numBits += IHuffConstants.BITS_PER_WORD;
        }

        for (int i = 0; i < freq.length; i++) {
            if (freq[i] > 0) {
                frequencyQueue.enqueue(new TreeNode(i, freq[i]));
            }
            // System.out.println("Chunk: " + i + ", Freq: " + freq[i]);
        }

        // Adding PEOF value
        frequencyQueue.enqueue(new TreeNode(ALPH_SIZE, 1));
        // System.out.println("Frequency Queue: " + frequencyQueue);

        // Generate huffman tree
        compressionHuffTree = new HuffmanTree<>(frequencyQueue);
        compressionHuffMap = compressionHuffTree.getHuffManCodes();

        System.out.println("codes: " + compressionHuffTree.getHuffManCodes());
        // TODO STF
        String STF = compressionHuffTree.treeHeader();
        System.out.println(numBits - (STF.length() + (BITS_PER_INT * 2) + compressionHuffTree.getSumOfAllCodes()));
        int headerInfo = BITS_PER_INT * 2 + (ALPH_SIZE * BITS_PER_INT);

        // Number of bits in file minus 32 for magic number, minus 32 for STORE_COUNTS/
        // STORE_TREE constant, then subtract the amount of bits in compressed file,
        // then subtract header for compressed file (32 bits)
        System.out.println(numBits - (headerInfo + compressionHuffTree.getSumOfAllCodes())); // huffmanTree.getSumOfAllCodes());
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
        }

        int inBits = inBitStream.readBits(IHuffConstants.BITS_PER_INT);
        while (inBits != -1) {
            outBits.writeBits(IHuffConstants.BITS_PER_INT,
                    Integer.parseInt(compressionHuffMap.get(inBits)));
            inBits = inBitStream.readBits(IHuffConstants.BITS_PER_INT);
            bitsWritten += IHuffConstants.BITS_PER_INT;
        }
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

        throw new IOException("uncompress not implemented");
        // return 0;
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
