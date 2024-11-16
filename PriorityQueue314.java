

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

public class PriorityQueue314<E extends Comparable<? super E>>  {

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
        list.sort(new PriorityQueue314Comparator());

    }

    public E poll() {
        if (list.isEmpty()) {
            return null;
        }
        E data = list.remove(0);

        list.sort(new PriorityQueue314Comparator());

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

    @Override
    public String toString() {
        return list.toString();
    }


private class PriorityQueue314Comparator implements Comparator<E> {

    @Override
    public int compare(E o1, E o2) {
        int compare = o1.compareTo(o2);
        if (compare != 0) {
            return compare;
        }
        return -1;
    }
}

}
