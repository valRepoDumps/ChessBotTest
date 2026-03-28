package ChessResources.Hasher;

import ChessLogic.Configurations.PropertiesStats;
import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.Pieces.PieceData;
import ChessResources.PreCalc;

import java.util.Random;

public final class ZobristHasher {
    long[] hashList = new long[ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE*PieceData.TOTAL_PIECES];

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

    private int translateGameCastlingRights(PropertiesStats ps){
            return ps.getCastlingFlag();
    }

    public long getHashWithSpaceIdAndPiece(int spaceId, short pieceId){
        return hashList[spaceId*PieceData.TOTAL_PIECES + PieceData.convertPieceIdToArrayIdx(pieceId)];}

    public long getGameHash(MinimalChessGame game){
        long key = 0L;

        for (short pieceId : PreCalc.ALL_PIECES){
            for (long move = game.getBoard().getBitBoard(PieceData.convertArrayIdxToPieceId(pieceId));
                 move != 0; move&=move-1){
                key^=getHashWithSpaceIdAndPiece(Long.numberOfTrailingZeros(move), pieceId);
            }
        }

        if (game.getGameProperties().getSideToMove() == ChessBoard.BLACK){
            key ^= sideToMoveIsBlack;
        }

        if(game.getGameProperties().canEnPassant()){
            key^=fileOfValidEnPassant[ChessBoard.getRow(game.getGameProperties().getEnPassantTarget())];
        }

        key^=castlingRights[translateGameCastlingRights(game.getGameProperties())];

        return key;
    }

}
