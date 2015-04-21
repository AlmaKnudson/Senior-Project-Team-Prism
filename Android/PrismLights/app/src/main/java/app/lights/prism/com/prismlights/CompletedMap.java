package app.lights.prism.com.prismlights;

import java.util.HashMap;
import java.util.Set;

public class CompletedMap {
    private HashMap<String, Boolean> completed;
    private OnCompletedListener onCompletedListener;
    public CompletedMap(Set<String> ids, OnCompletedListener onCompletedListener) {
        completed = new HashMap<String, Boolean>();
        for(String id: ids) {
            completed.put(id, false);
        }
        this.onCompletedListener = onCompletedListener;
        if(completed.isEmpty()&& onCompletedListener != null) {
            onCompletedListener.onCompleted();
        }
    }

    public void complete(String id) {
        completed.put(id, true);
        if(allCompleted() && onCompletedListener != null) {
            onCompletedListener.onCompleted();
        }
    }

    private boolean allCompleted() {
        for(String id: completed.keySet()) {
            if(!completed.get(id)) {
                return false;
            }
        }
        return true;
    }

}
