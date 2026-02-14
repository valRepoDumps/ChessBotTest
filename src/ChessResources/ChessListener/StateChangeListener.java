package ChessResources.ChessListener;

import java.util.ArrayList;

@FunctionalInterface
public interface StateChangeListener<T> {

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
