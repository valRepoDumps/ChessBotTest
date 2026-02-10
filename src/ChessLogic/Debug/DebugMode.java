package ChessLogic.Debug;

public class DebugMode {
    public static void debugPrint(Debuggable obj, Object msg){
        if (obj.isDebuggable()){
            System.out.println(msg);
        }
    }
}
