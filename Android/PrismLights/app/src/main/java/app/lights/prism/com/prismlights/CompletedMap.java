package app.lights.prism.com.prismlights;

import java.util.HashMap;
import java.util.Set;

/**
 * A set which calls it's on completed listener when all of the items have been completed
 */
public class CompletedMap {
    private HashMap<String, Boolean> completed; //if an element is false, it hasn't been completed, if it's true it has
    private OnCompletedListener onCompletedListener;

    /**
     * Initializes the map and calls on completed if it's not null and there are no ids in the set
     * @param ids the ids for the map
     * @param onCompletedListener can be null
     */
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

    /**
     * Completes a single item
     * @param id the id to be completed
     */
    public void complete(String id) {
        completed.put(id, true);
        if(allCompleted() && onCompletedListener != null) {
            onCompletedListener.onCompleted();
        }
    }

    /**
     * Checks if all items are completed
     * @return true if they are, false otherwise
     */
    private boolean allCompleted() {
        for(String id: completed.keySet()) {
            if(!completed.get(id)) {
                return false;
            }
        }
        return true;
    }

}
