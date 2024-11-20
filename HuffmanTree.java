
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HuffmanTree<E extends Comparable<? super E>> {
    private TreeNode root;
    private Map<Integer, String> huffManCodes;
    private Map<Integer, Integer> getFreqPerCode;
    private Map<String, String> decompressionCodes;


    // TODO Check with TAs if this has to be generic or not
    // Is this still needed to be asked? @mehtavihaanj
    public HuffmanTree(PriorityQueue314<TreeNode> pq) {
        this();
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

        // System.out.println("DATA: " + root.getRight().getLeft().getValue());
        // Collections.sort(list);
        // System.out.println(list);
        for (Integer value : list) {
            String code = getCode(value);
            huffManCodes.put(value, code);
            decompressionCodes.put(code, String.valueOf(value));
        }


    }

    public int getPEOFCode() {
        return Integer.parseInt(getCode(IHuffConstants.ALPH_SIZE));
    }

    private String getCode(int value) {
        StringBuilder sb = new StringBuilder();
        TreeNode node = root;
        getCodeHelper(value, node, sb);
        return sb.toString();
    }

    public int getSumOfAllCodes() {
        int sum = 0;

        for (Integer value : huffManCodes.keySet()) {
            sum += huffManCodes.get(value).length() * getFreqPerCode.get(value);
        }
        return sum;
    }

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

    public HuffmanTree() {
        huffManCodes = new HashMap<>();
        getFreqPerCode = new HashMap<>();
        decompressionCodes = new HashMap<>();
    }

    public HashMap<Integer, String> getHuffManCodes() {
        return (HashMap<Integer, String>) huffManCodes;
    }

    public HashMap<String, String> getDecompressionCodes() {
        return (HashMap<String, String>) decompressionCodes;
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

    /**
     * Puts together tree header and returns it
     * 
     * @return tree's header + actual value
     */
    public String treeHeader() {
        StringBuilder header = new StringBuilder();

        // tree size / shape
        header.append(preorderShape(root, header));
        int lenNow = header.length();

        header.insert(0, toBinary(lenNow, 32));

        // for (Map.Entry<Integer, String> entry : huffManCodes.entrySet()) {
        // String huffCode = entry.getValue();
        // header.append(huffCode);

        // // append binarified value to header
        // // header.append(toBinary(entry.getKey(), 0));
        // }

        return header.toString();
    }

    /**
     * convert integer value into its binary form
     * 
     * @param value  value being transformed
     * @param format determine the length. e.g., 9 will return 9 bits
     * @return binary form of the value given
     */
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


}