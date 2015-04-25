package app.lights.prism.com.prismlights;

import java.util.ArrayList;

/**
 * A simple class preventing index out of bounds exceptions in the upward direction
 * @param <T>
 */
public class CircularArray<T> extends ArrayList<T> {
    /**
     *
     * @param index the index of the element. If greater than size() -1, the index used will be index % size()
     * @return the element at index or index %size()
     */
    @Override
    public T get(int index) {
        return super.get(index % size());
    }
}
