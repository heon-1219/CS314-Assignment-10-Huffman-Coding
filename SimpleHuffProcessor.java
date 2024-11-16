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
import java.util.Map;
import java.util.HashMap;


public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private PriorityQueue314<TreeNode> frequencyQueue;
    private HuffmanTree<TreeNode> huffmanTree;

    public SimpleHuffProcessor() {
        frequencyQueue = new PriorityQueue314<>();
        huffmanTree = new HuffmanTree<>();
    }

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * 
     * @param in           is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind
     *                     of
     *                     header to use, standard count format, standard tree
     *                     format, or
     *                     possibly some format added in the future.
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
        if (headerFormat != IHuffConstants.STORE_COUNTS) {
            System.out.println("Invalid header format");
            return -1;
        }
        // freqMap = (actual number, frequency)
        Map<Integer, Integer> freqMap = new HashMap<>();
        // From lecture - see if this is possible without a map


        while ((inbits != -1)) {
            if (!freqMap.containsKey(inbits)) {
                freqMap.put(inbits, 1);
            } else {
                freqMap.put(inbits, freqMap.get(inbits) + 1);
            }

            System.out.println(inbits);
            inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
        }

        for (Map.Entry<Integer, Integer> entry : freqMap.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            frequencyQueue.enqueue(new TreeNode(key, value));
            System.out.println("Chunk: " + key + ", Frequency: " + value);
        }
        // Adding PEOF value
        frequencyQueue.enqueue(new TreeNode(256, 1));
        System.out.println("Frequency Queue: " + frequencyQueue);

        // Generate huffman tree
        huffmanTree = new HuffmanTree<>(frequencyQueue);



        showString("Not working yet");
        myViewer.update("Still not working");
        throw new IOException("preprocess not implemented");

        // return 0;
    }

    /**
     * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
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
        throw new IOException("compress is not implemented");
        // return 0;
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
}
