package ChessResources.Hasher;

import ChessLogic.Debug.DebugMode;
import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.ChessHistoryTracker.BoardStateChanges.BoardStateChange;
import ChessResources.ChessHistoryTracker.GameStateChanges;
import ChessResources.ChessListener.StateChangeListener;

public class HashGenerator<Board extends ChessBoard> {
    ZobristHasher<Board> hasher1;
    ZobristHasher<Board> hasher2;
    MinimalChessGame<Board> game;

    public HashGenerator(MinimalChessGame<Board> game){
        this.game = game;
        hasher1 = new ZobristHasher<>();
        hasher2 = new ZobristHasher<>();
    }

    public HashGenerator(MinimalChessGame<Board> game, long seed1, long seed2){
        this.game = game;
        hasher1 = new ZobristHasher<>(seed1);
        hasher2 = new ZobristHasher<>(seed2);
    }

    public HashContainer getCurrentHash(){
        return new HashContainer(hasher1.getGameHash(game), hasher2.getGameHash(game));
    }
}
