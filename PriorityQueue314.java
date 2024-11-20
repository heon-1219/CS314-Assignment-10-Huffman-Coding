

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public class PriorityQueue314<E extends Comparable<? super E>>  {

    private LinkedList<E> list;
    public PriorityQueue314() {
        list = new LinkedList<>();
    }

    public void enqueue(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        else if (size() == 0) {
            list.add(e);
        }
        else {
            int index = 0;
            boolean indexFound = false;
            while (index < list.size() && !indexFound) {
                // Does the current value have a higher frequency than value to add? then break
                int compare = list.get(index).compareTo(e);

                indexFound = compare > 0;
                index++;
            }
            if (index - 1 >= 0 && list.get(index - 1).compareTo(e) > 0) {
                index--;
            }
            list.add(index, e);
        }
    }

    public E poll() {
        if (list.isEmpty()) {
            return null;
        }
        return list.remove(0);
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
}
