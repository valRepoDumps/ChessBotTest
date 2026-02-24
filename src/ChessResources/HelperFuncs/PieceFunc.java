package ChessResources.HelperFuncs;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessSpaces;

@FunctionalInterface
public interface PieceFunc {
    void apply(MinimalChessGame<? extends ChessBoard> game, int from, ChessSpaces out);
}
