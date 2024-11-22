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
    private Map<String, String> compressionHuffMap;
    private int[] freq;
    private boolean countOrTree;

    // basic constructor for simple huff processor. Instance variables initialized.
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
        countOrTree = (headerFormat == IHuffConstants.STORE_COUNTS);
        BitInputStream bits = new BitInputStream(in);
        int inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);

        int numBits = 0;
        while (inbits != -1) {
            freq[inbits]++;
            inbits = bits.readBits(IHuffConstants.BITS_PER_WORD);
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

        int headerInfo = BITS_PER_INT * 2;

        headerInfo += headerFormat == IHuffConstants.STORE_COUNTS ? ALPH_SIZE * BITS_PER_INT
                : compressionHuffTree.treeHeader().length();
        // in.close();
        // bits.close();

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
        BitOutputStream outBits = new BitOutputStream(out);
        BitInputStream inBitStream = new BitInputStream(in);

        // 1. write the magic number
        outBits.writeBits(IHuffConstants.BITS_PER_INT, IHuffConstants.MAGIC_NUMBER);

        // 2 and 3. header format, info to recreate the tree per header format
        if (countOrTree) { // SCF
            outBits.writeBits(BITS_PER_INT, IHuffConstants.STORE_COUNTS);

            for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
                outBits.writeBits(IHuffConstants.BITS_PER_INT, freq[k]);
                bitsWritten += IHuffConstants.BITS_PER_INT;
            }

            bitsWritten += IHuffConstants.BITS_PER_INT * 2 + compressionHuffTree.getSumOfAllCodes();
        } else { // STF
            outBits.writeBits(BITS_PER_INT, IHuffConstants.STORE_TREE);

            // size of the tree
            String treeSize = compressionHuffTree.treeSize();
            for (int i = 0; i < treeSize.length(); i++) {
                outBits.writeBits(1, treeSize.charAt(i) - '0');
            }

            // then tree shape
            String treeHeader = compressionHuffTree.treeHeader();
            for (int i = 0; i < treeHeader.length(); i++) {
                outBits.writeBits(1, treeHeader.charAt(i) - '0');
            }

            bitsWritten = treeHeader.length() + treeSize.length() + IHuffConstants.BITS_PER_INT
                    + compressionHuffTree.getSumOfAllCodes();
        }

        int inBits = inBitStream.readBits(IHuffConstants.BITS_PER_WORD);
        String temporaryVal = "";

        // 4. The actual compressed data
        while (inBits != -1) {
            String writeBitString = compressionHuffMap.get(String.valueOf(inBits));
            System.out.println("character just read: " + (char) inBits);
            System.out.println("writing the bitstring: " + writeBitString + "decimal value: "
                    + Integer.parseInt(writeBitString, 2));
            System.out.println("length: " + writeBitString.length());
            int value = Integer.parseInt(writeBitString, 2);

            outBits.writeBits(writeBitString.length(),
                    value);
            temporaryVal += writeBitString;
            inBits = inBitStream.readBits(IHuffConstants.BITS_PER_WORD);
            System.out.println("inbits is " + inBits);
        }

        String stringAlph = String.valueOf(IHuffConstants.ALPH_SIZE);

        // 5. Write PSEUDO_EOF code
        System.out.println("map: " + compressionHuffMap);
        String peof = compressionHuffMap.get(stringAlph);
        temporaryVal += peof;
        System.out.println("peof is " + temporaryVal + peof.length() + " int " + Integer.parseInt(peof, 2));

        outBits.writeBits(4, 4);

        // outBits.close();
        // inBitStream.close();

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
        int bitsRead = 0;

        // read the magic num, header format, and evaluate rather SCF or STF
        int magicNum = inputStream.readBits(BITS_PER_INT);
        int headerFormat = inputStream.readBits(BITS_PER_INT);
        bitsRead += BITS_PER_INT * 2;

        // determine if the correct file or header format
        if (magicNum != MAGIC_NUMBER || (headerFormat != STORE_COUNTS && headerFormat != STORE_TREE)) {
            myViewer.showError("Error reading compressed file. \n" +
                    "File did not start with the huff magic number.");
            inputStream.close();
            outputStream.close();
            return -1;
        }

        // decompressing the tree starts here
        HuffmanTree<TreeNode> decompressedTree;

        if (headerFormat == STORE_COUNTS) { // SCF
            PriorityQueue314<TreeNode> decompressQueue = new PriorityQueue314<>();

            // read the 256 lines and turn it in to a frequency que, then turn the q to tree
            for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
                int headerBit = inputStream.readBits(BITS_PER_INT);
                if (headerBit > 0) {
                    decompressQueue.enqueue(new TreeNode(k, headerBit));
                }
            }

            bitsRead += ALPH_SIZE * BITS_PER_INT;
            decompressQueue.enqueue(new TreeNode(ALPH_SIZE, 1));
            decompressedTree = new HuffmanTree<>(decompressQueue);

        } else { // STF
            int treeSize = inputStream.readBits(BITS_PER_INT);
            treeSize = binaryToDecimal(treeSize);

            String treeData = Integer.toString(inputStream.readBits(treeSize));
            bitsRead += BITS_PER_INT + treeSize;

            decompressedTree = new HuffmanTree<>(treeData);
        }
        String newPEOF = decompressedTree.getHuffManCodes().get(String.valueOf(ALPH_SIZE));
        int inBits = 0;
        StringBuilder currentValue = new StringBuilder();
        TreeNode traversalNode = decompressedTree.getRoot();

        // read the huffman codes
        while (inBits != -1 && !currentValue.toString().equals(newPEOF)) {
            inBits = inputStream.readBits(1);
            if (inBits == 1) {
                traversalNode = traversalNode.getRight();
            } else {
                traversalNode = traversalNode.getLeft();
            }

            if (traversalNode.isLeaf() && traversalNode.getValue() != Integer.parseInt(newPEOF, 2)) {
                outputStream.writeBits(BITS_PER_WORD, traversalNode.getValue());
                // increment bits written
                traversalNode = decompressedTree.getRoot();
            }
        }

        return bitsRead;
    }

    // convert binary to decimal
    private int binaryToDecimal(int binary) {
        int decimal = 0;
        int base = 1;

        while (binary > 0) {
            int lastDigit = binary % 10;
            decimal += lastDigit * base;

            base *= 2;
            binary /= 10;
        }

        return decimal;
    }

    // set the viewer to interact with EX: GUIHuffviewer
    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    // show a string value on the viewer
    private void showString(String s) {
        if (myViewer != null) {
            myViewer.update(s);
        }
    }
}