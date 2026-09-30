package ChessResources.ChessListener;

import java.util.ArrayList;

@FunctionalInterface
public interface StateChangeListener<T> {

    static <T> void notifyListeners(StateChangeListener<T> listener, T change) {
        listener.onChange(change);
    }

    void onChange(T change);
    static <T> void notifyListeners(ArrayList<StateChangeListener<T>> stateChangeListenerList,
                                           T change)
    {
        for (StateChangeListener<T> stateChangeListener : stateChangeListenerList)
        {
            stateChangeListener.onChange(change);
        }
    }
}
