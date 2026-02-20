package ChessLogic.Debug;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.PossibleMoves;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Tests {
    public static <Board extends ChessBoard> int moveGenerationTest(int depth, MinimalChessGame<Board> game){
        if (depth == 0) return 1;
        int numPos = 0;

        Scanner scanner = new Scanner(System.in);
        PossibleMoves currPossibleMoves = game.getCurrentPossibleMoves();
        List<Integer> keysAsArray = new ArrayList<Integer>(currPossibleMoves.getMoves().keySet());
        List<Move> allMoves = new ArrayList<>();
        for (int spaceId : keysAsArray){
            for (int spaceIdArriveAt: currPossibleMoves.getMoves().get(spaceId).getChessMoves()){
                allMoves.add(new Move(spaceId, spaceIdArriveAt));
            }
        }

        for (Move move : allMoves){
            game.movePiece(move.spaceId, move.spaceIdArriveAt);

            numPos+=moveGenerationTest(depth-1, game);

            //System.out.println("Press Enter to continue...");
            //scanner.nextLine();

            try{game.undoTurn();}
            catch(Exception e){
                System.out.println(e.getMessage());
            }
        }

        return numPos;
    }
}
