package app.lights.prism.com.prismlights;

import java.util.ArrayList;

public class CircularArray<T> extends ArrayList<T> {
    @Override
    public T get(int index) {
        return super.get(index % size());
    }
}
