package SecondTask;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.ToIntFunction;


public class HomeWork {
    public static void main(String[] args) {
        //task second, I use here the ShellSort, since that pretty rare used one
        // comparing to Bubble Sort, Selection Sort or Insertion Sort etc.
        //as well as a relatively simple algorithm with a performance of approximately O(n.pow(3/2))
        //and this also easy for writing that by my own and without taking lot of time

        Integer[] intArray = {5, 6, 3, 2, 5, 1, 4, 9};
        Object[] temp = HomeWork.shellSort(intArray, x -> x, true);
        Integer[] uniqueSortedIntArray = Arrays.copyOf(temp, temp.length, Integer[].class);

        assert !Arrays.toString(uniqueSortedIntArray).equals("[5, 6, 3, 2, 5, 1, 4, 9]");

        assert !Arrays.toString(uniqueSortedIntArray).equals("[1, 2, 3, 4, 5, 5, 6, 9]");

        assert Arrays.toString(uniqueSortedIntArray).equals("[1, 2, 3, 4, 5, 6, 9]");
    }

    /**
     * &#064;params:  mapper — for more flexibility for instance you can pass there User::getAge
     * distinct as function true - on, falls - off
     */
    public static <T> T[] shellSort(T[] array, ToIntFunction<T> mapper, boolean distinct) {
        int shift = array.length / 2;
        while (shift > 0) {
            for (int i = shift; i < array.length; i++) {
                for (int j = i; j >= shift && mapper.applyAsInt(array[j])
                        < mapper.applyAsInt(array[j - shift]); j -= shift) {
                    T temp = array[j];
                    array[j] = array[j - shift];
                    array[j - shift] = temp;
                }
            }
            shift = shift / 2;
        }
        // using linked 'list' in this case doesn't bring lots of benefits,
        // but if we will have quite high degree of duplicates it will work more efficiently
        // instead of pushing left all rights values each time we find repetition,
        // even if we have to copy array 2 times, I consider it as "pessimistic approach" =).

        return distinct ? new MyLittleLinkedIterable<T>(array).distinct() : array;
    }

    static class MyLittleLinkedIterable<T> implements Iterable<T> {

        private final Node first = new Node(null);
        private final Node last = new Node(null);
        public Long size = 0L;

        MyLittleLinkedIterable() {
            first.next = last;
            last.previous = first;
        }

        MyLittleLinkedIterable(T[] array) {
            this();
            if (array == null) throw new IllegalArgumentException();
            initAll(array);
        }

        private void initAll(T[] array) {
            Arrays.stream(array)
                    .forEach(this::set);
        }

        class Node {
            private Node previous;
            private Node next;
            public T value;

            private Node(T value) {
                this.value = value;
            }
        }

        public void delete(Node node) {
            node.previous.next = node.next;
            node.next.previous = node.previous;
            size--;
        }

        public void set(T value) {
            Node node = new Node(value);
            node.previous = last.previous;
            last.previous.next = node;
            last.previous = node;
            node.next = last;
            size++;
        }

        public T[] distinct() {
            Node current = first;
            while (current.next != last) {
                current = current.next;
                if (current.next != last
                        && current.next.value != null
                        && current.next.value.equals(current.value)) {
                    delete(current);
                }
            }
            return this.toArray();
        }

        @SuppressWarnings("unchecked")
        private T[] toArray() {
            int count = 0;
            Object[] object = new Object[size.intValue()];

            for (T elem : this) {
                object[count++] = elem;
            }
            return (T[]) object;
        }

        @Override
        public Iterator<T> iterator() {
            return new LinkedIterator() {
            };
        }

        private class LinkedIterator implements Iterator<T> {
            private Node current = first;

            @Override
            public boolean hasNext() {
                return current.next != last;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                current = current.next;
                return current.value;
            }
        }
    }
}
