package ChessLogic.Debug;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessSpaces;
import ChessResources.GetMovesLogic.PossibleMoves;

import java.util.*;

public class Tests {
    public static <Board extends ChessBoard, ChessMoves> int moveGenerationTest(int depth, MinimalChessGame<Board> game){
        if (depth == 0) return 1;
        int numPos = 0;

        PossibleMoves currPossibleMoves = game.getCurrentPossibleMoves();
        HashMap<Integer, ChessSpaces> cloneMoves = (HashMap<Integer, ChessSpaces>)
                game.getCurrentPossibleMoves().getMoves().clone();
        for (Map.Entry<Integer, ChessSpaces> entry : cloneMoves.entrySet()){
            int spaceId = entry.getKey();
            for (int spaceIdArriveAt : entry.getValue().getChessMoves()){
                game.movePiece(spaceId, spaceIdArriveAt);
                numPos += moveGenerationTest(depth - 1, game);
                try {
                    game.undoTurn();
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
        }

        return numPos;
    }
}
