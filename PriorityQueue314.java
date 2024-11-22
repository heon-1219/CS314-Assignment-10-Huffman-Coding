import java.util.Iterator;
import java.util.LinkedList;

public class PriorityQueue314<E extends Comparable<? super E>> {
    private LinkedList<E> list;

    // constructor for priority queue. Internal storage is java linked list.
    public PriorityQueue314() {
        list = new LinkedList<>();
    }

    // enqueue an element into the priority que.
    public void enqueue(E e) {
        if (e == null) {
            throw new NullPointerException();
        } else if (size() == 0) {
            list.add(e);
        } else {
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

    // poll the next element from the priority queue
    public E poll() {
        if (list.isEmpty()) {
            return null;
        }
        return list.remove(0);
    }

    // return the size of this priority queue
    public int size() {
        return list.size();
    }

    // see the first value in the pq without removing it
    public E peek() {
        if (list.isEmpty()) {
            return null;
        }
        return list.peek();
    }

    // returns true if contains element e, if not false
    public boolean contains(E e) {
        return list.contains(e);
    }

    // returns iterator for this pq
    public Iterator<E> iterator() {
        return list.iterator();
    }

    // returns true if the priority queue is empty
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    // overriden toString method
    public String toString() {
        return list.toString();
    }
}
