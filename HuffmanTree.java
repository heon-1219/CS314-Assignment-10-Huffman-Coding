import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HuffmanTree<E extends Comparable<? super E>> {
    private TreeNode root;
    private Map<String, String> huffManCodes;
    private Map<Integer, Integer> getFreqPerCode;


    // constructor for generating huffman tree with a priority queue
    public HuffmanTree(PriorityQueue314<TreeNode> pq) {
        getFreqPerCode = new HashMap<>();
        huffManCodes = new HashMap<>();

        ArrayList<Integer> list = new ArrayList<>();
        Iterator<TreeNode> it = pq.iterator();

        while (it.hasNext()) {
            TreeNode temp = it.next();
            list.add(temp.getValue());
            getFreqPerCode.put(temp.getValue(), temp.getFrequency());
        }

        while (pq.size() > 1) {
            TreeNode left = pq.poll();
            TreeNode right = pq.poll();
            pq.enqueue(new TreeNode(left, -1, right));
        }
        root = pq.poll();
        for (Integer value : list) {
            String code = getCode(value);
            String val = String.valueOf(value);
            huffManCodes.put(val, code);
        }
    }

    // constructor for generating huffman tree with a tree header
    public HuffmanTree(String treeData) throws IOException {
        int[] hasPEOF = new int[1];
        root = rebuildTree(treeData, new TreeNode(0, 0), hasPEOF, new int[1]);
        if (hasPEOF[0] == 0) {

            throw new IOException("Error reading compressed file. \n" +
                    "unexpected end of input. No PSEUDO_EOF character.");
        }
    }

    // recursive helper method for rebuilding the tree with tree header
    private TreeNode rebuildTree(String treeData, TreeNode node, int[] hasEOF, int[] index)
    {
        int bit = Integer.parseInt(treeData.substring(index[0], index[0] + 1));
        index[0]++;
       // treeData = new StringBuilder(treeData.substring(1));

        if (bit == 0) {
            // internal node
            node = new TreeNode(-1, 1);
            node.setLeft(rebuildTree(treeData, node.getLeft(), hasEOF, index));
            node.setRight(rebuildTree(treeData, node.getRight(), hasEOF, index));
        } else {
            // turn the binary formatted value into decimal. Now index for huffman.
            int binaryValue = IHuffConstants.BITS_PER_WORD + 1;
            int value = Integer.parseInt(treeData.substring(index[0], index[0] + binaryValue), 2);
            index[0] += binaryValue;

            // check if PSEUDO_EOF value exists
            if (value == IHuffConstants.ALPH_SIZE) {
                hasEOF[0]++;
            }
            return new TreeNode(value, 1);
        }
        return node;
    }

    // default constructor
    public HuffmanTree() {
        huffManCodes = new HashMap<>();
        getFreqPerCode = new HashMap<>();
    }

    // return the huffman code for a specific ascii value
    private String getCode(int value) {
        StringBuilder sb = new StringBuilder();
        TreeNode node = root;
        getCodeHelper(value, node, sb);
        return sb.toString();
    }

    // return the length of total huffman code
    public int getSumOfAllCodes() {
        int sum = 0;

        for (String value : huffManCodes.keySet()) {
            sum += huffManCodes.get(value).length() * getFreqPerCode.get(Integer.parseInt(value));
        }
        return sum;
    }

    // recursive method to help obtain the huffman code
    private int getCodeHelper(int value, TreeNode node, StringBuilder code) {
        if (node != null && node.getValue() != value) {
            code.append(0);
            int leftResult = getCodeHelper(value, node.getLeft(), code);
            if (leftResult == value) {
                return leftResult;
            }
            code.replace(code.length() - 1, code.length(), "");

            code.append(1);
            int rightResult = getCodeHelper(value, node.getRight(), code);
            if (rightResult == value) {
                return rightResult;
            }
            code.replace(code.length() - 1, code.length(), "");
        } else if (node != null && node.getValue() == value) {
            return value;
        }
        return -1;
    }

    // return a hashmap of huffman codes, <ASCII, HUFF CODE>
    public HashMap<String, String> getHuffManCodes() {
        return (HashMap<String, String>) huffManCodes;
    }

    // preorder traversal to figure out size rep. and tree shape
    public String preorderShape(TreeNode node, StringBuilder soFar) {
        if (node == null) {
            return soFar.toString();
        }

        if (!node.isLeaf()) {
            soFar.append(0);
            preorderShape(node.getLeft(), soFar);
            preorderShape(node.getRight(), soFar);
        } else {
            soFar.append(1);
            // Append the node value in binary format with 9 bits
            soFar.append(toBinary(node.getValue(), 9));
        }

        return "";
    }

    // calculate and return the size of tree in binary format
    public String treeSize() {
        StringBuilder shape = new StringBuilder();
        shape.append(preorderShape(root, shape));

        int size = shape.toString().length();

        return toBinary(size, IHuffConstants.BITS_PER_INT);
    }

    // put together a tree header and return it
    public String treeHeader() {
        StringBuilder header = new StringBuilder();

        // tree size / shape
        header.append(preorderShape(root, header));
        int lenNow = header.length();

        header.insert(0, toBinary(lenNow, 32));
        return header.toString();
    }

    // convert an integer value into binary format with n(format) places
    public static String toBinary(int value, int format) {
        StringBuilder binary = new StringBuilder();
        while (value > 0) {
            binary.append(value % 2);
            value /= 2;
        }
        binary.reverse();

        while (format > binary.length()) {
            binary.insert(0, 0);
        }

        return binary.toString();
    }

    public TreeNode getRoot() {
        return root;
    }
}
