package ChessResources.Hasher;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.Pieces.PieceData;
import ChessResources.Pieces.PieceDatas;

import java.util.Map;
import java.util.Random;

public final class ZobristHasher<Board extends ChessBoard> {
    long[] hashList = new long[Board.BOARD_SIZE*Board.BOARD_SIZE*PieceDatas.TOTAL_PIECES];

    long[] castlingRights = new long[16];
    long sideToMoveIsBlack;
    long[] fileOfValidEnPassant = new long[8];
    private final Random rand;

    public ZobristHasher(long seed){
        rand = new Random(seed);

        constructRandomHashes();
    }

    public ZobristHasher(){
        rand = new Random();

        constructRandomHashes();
    }

    private void constructRandomHashes(){
        generateRandomLongList(hashList);
        generateRandomLongList(castlingRights);
        generateRandomLongList(fileOfValidEnPassant);
        sideToMoveIsBlack = rand.nextLong();
    }

    private void generateRandomLongList(long[] list){
        for (int i = 0; i < list.length; ++i){
            list[i] = rand.nextLong();
        }
    }

    private int translateGameCastlingRights(boolean[] gp){
            return (gp[MinimalChessGame.WHITE_CASTLE_KING]  ? 1 : 0)
                    | (gp[MinimalChessGame.WHITE_CASTLE_QUEEN] ? 2 : 0)
                    | (gp[MinimalChessGame.BLACK_CASTLE_KING]  ? 4 : 0)
                    | (gp[MinimalChessGame.BLACK_CASTLE_QUEEN] ? 8 : 0);
    }

    public long getHashWithSpaceIdAndPiece(int spaceId, int pieceId){
        return hashList[spaceId*PieceDatas.TOTAL_PIECES + PieceDatas.convertPieceIdToArrayIdx(pieceId)];}

    public long getGameHash(MinimalChessGame<Board> game) {
        long key = 0L;

        for (Map.Entry<PieceData, Integer> entry : game.chessBoard.currPieceLocationWhite.entrySet())
        {
            int startSquare = entry.getValue();
            PieceData piece = entry.getKey();

            key^=getHashWithSpaceIdAndPiece(startSquare, piece.getPieceId());
        }

        for (Map.Entry<PieceData, Integer> entry : game.chessBoard.currPieceLocationBlack.entrySet())
        {
            int startSquare = entry.getValue();
            PieceData piece = entry.getKey();

            key^=getHashWithSpaceIdAndPiece(startSquare, piece.getPieceId());
        }

        if (game.getGameProperties()[MinimalChessGame.SIDE_TO_MOVE] == Board.BLACK){
            key ^= sideToMoveIsBlack;
        }

        if(game.getGameStats()[MinimalChessGame.ENPASSANT_TARGET] != MinimalChessGame.INVALID_ENPASSANT_TARGET){
            key^=fileOfValidEnPassant[Board.getRow(game.getGameStats()[MinimalChessGame.ENPASSANT_TARGET])];
        }

        key^=castlingRights[translateGameCastlingRights(game.getGameProperties())];

        return key;
    }

}
