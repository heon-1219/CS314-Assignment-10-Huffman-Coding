

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HuffmanTree<E extends Comparable<? super E>>
{
    private TreeNode root;
    private Map<Integer, String> huffManCodes;
    // Check with TAs if this has to be generic or not
    public HuffmanTree(PriorityQueue314<TreeNode> pq) {
        this();
        ArrayList<Integer> list = new ArrayList<>();
        Iterator<TreeNode> it  = pq.iterator();

        while (it.hasNext()) {
            list.add(it.next().getValue());
        }

        while (pq.size() > 1) {
            TreeNode left = pq.poll();
            TreeNode right = pq.poll();
            pq.enqueue(new TreeNode(left, left.getValue() + right.getValue(), right));
        }

        root = pq.poll();
        for (Integer value : list) {
            huffManCodes.put(value, getCode(value));
        }


    }

    public String getCode(int value) {
        StringBuilder sb = new StringBuilder();
        TreeNode node = root;
        return getCodeHelper(value, node, new StringBuilder());
    }

    public String getCodeHelper(int value, TreeNode node, StringBuilder code) {
        if (node.getValue() == value) {
            return code.toString();
        }

        code.append(0);
        getCodeHelper(value, node.getLeft(), code);
        code.replace(code.length() - 1, code.length(), "");

        code.append(1);
        getCodeHelper(value, node.getRight(), code);
        code.replace(code.length() - 1, code.length(), "");

        return code.toString();
    }

    public HuffmanTree() {
        huffManCodes = new HashMap<>();
    }

    public HashMap<Integer, String> getHuffManCodes() {
        return (HashMap<Integer, String>) huffManCodes;
    }
}
