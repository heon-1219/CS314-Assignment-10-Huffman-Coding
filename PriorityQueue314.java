public class PriorityQueue314 {

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public class PriorityQueue314<E extends Comparable<? super E>> {

    private LinkedList<E> list;
    private int size;
    private int priority;

    public PriorityQueue314() {
        list = new LinkedList<>();
    }

    public void enqueue(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        list.add(e);
        Collections.sort(list);
    }

    public E poll() {
        if (list.isEmpty()) {
            return null;
        }
        E data = list.remove(0);
        Collections.sort(list);
        return data;
    }

    public int size() {
        return list.size();
    }

    public E peek() {
        if (list.isEmpty()) {
            return null;
        }
        return list.peek();
    }

    public boolean contains(E e) {
        return list.contains(e);
    }

    public Iterator<E> iterator() {
        return list.iterator();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }
}
