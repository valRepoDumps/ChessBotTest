package ChessResources.Pieces;

import ChessLogic.MinimalChessGame;
import ChessResources.BitMasks;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.PossibleMoves;
import ChessResources.BitMasks;
import ChessResources.HelperFuncs.BoardScan.BoardScan;
import ChessResources.PreCalc;

import static ChessResources.BitMasks.HVD_TO_ATK_MASKS;

public class MovesGeneration {

    public static void generateMoves(MinimalChessGame<? extends ChessBoard> game){
        getKingMoves(game);
        getQueenMoves(game);
        getKnightMoves(game);
        getBishopMoves(game);
        getPawnMoves(game);
        getRookMoves(game);
    }

    public static void getKingMoves(MinimalChessGame<? extends ChessBoard> game){
        short pieceId;
        if (game.getCurrentColorToMove() == PieceData.WHITE){
            pieceId = PieceData.WKING;
        }else{
            pieceId = PieceData.BKING;
        }

        long kBoard = game.getBoard().getBitBoard(pieceId);
        int spaceId;
        int spaceToMove;
        for (long i = kBoard; i != 0; i &= (i-1)){
            spaceId = Long.numberOfTrailingZeros(i);
            long moves = getKingMoves(game.ANTI_TO_MOVE_PIECES, spaceId);
            for (long move = moves; move != 0; move &= (move-1)){
                spaceToMove = Long.numberOfTrailingZeros(move);
                if (game.spaceNotUnderThreatAndEmpty(spaceToMove))
                    game.getPossibleMoves().addMoves(spaceId, spaceToMove);
            }
        }
    }

    public static void getQueenMoves(MinimalChessGame<? extends ChessBoard> game){
        short pieceId;
        if (game.getCurrentColorToMove() == PieceData.WHITE){
            pieceId = PieceData.WQUEEN;
        }else{
            pieceId = PieceData.BQUEEN;
        }

        long qBoard = game.getBoard().getBitBoard(pieceId);
        int spaceId;
        for (long i = qBoard; i != 0; i &= (i-1)){
            spaceId = Long.numberOfTrailingZeros(i);
            long moves = getQueenMoves(game.ANTI_TO_MOVE_PIECES, game.OCCUPIED, spaceId);
            for (long move = moves; move != 0; move &= (move-1)){
                game.getPossibleMoves().addMoves(spaceId, Long.numberOfTrailingZeros(move));
            }
        }
    }

    public static void getKnightMoves(MinimalChessGame<? extends ChessBoard> game){
        short pieceId;
        if (game.getCurrentColorToMove() == PieceData.WHITE){
            pieceId = PieceData.WKNIGHT;
        }else{
            pieceId = PieceData.BKNIGHT;
        }

        long kBoard = game.getBoard().getBitBoard(pieceId);
        int spaceId;
        for (long i = kBoard; i != 0; i &= (i-1)){
            spaceId = Long.numberOfTrailingZeros(i);
            long moves = getKnightMoves(game.ANTI_TO_MOVE_PIECES, spaceId);
            for (long move = moves; move != 0; move &= (move-1)){
                game.getPossibleMoves().addMoves(spaceId, Long.numberOfTrailingZeros(move));
            }
        }
    }

    public static void getBishopMoves(MinimalChessGame<? extends ChessBoard> game){
        short pieceId;
        if (game.getCurrentColorToMove() == PieceData.WHITE){
            pieceId = PieceData.WBISHOP;
        }else{
            pieceId = PieceData.BBISHOP;
        }

        long bBoard = game.getBoard().getBitBoard(pieceId);
        int spaceId;
        for (long i = bBoard; i != 0; i &= (i-1)){
            spaceId = Long.numberOfTrailingZeros(i);
            long moves = getBishopMoves(game.ANTI_TO_MOVE_PIECES, game.OCCUPIED, spaceId);
            for (long move = moves; move != 0; move &= (move-1)){
                game.getPossibleMoves().addMoves(spaceId, Long.numberOfTrailingZeros(move));
            }
        }
    }

    public static void getRookMoves(MinimalChessGame<? extends ChessBoard> game){
        short pieceId;
        if (game.getCurrentColorToMove() == PieceData.WHITE){
            pieceId = PieceData.WROOK;
        }else{
            pieceId = PieceData.BROOK;
        }

        long rBoard = game.getBoard().getBitBoard(pieceId);
        int spaceId;
        for (long i = rBoard; i != 0; i &= (i-1)){
            spaceId = Long.numberOfTrailingZeros(i);
            long moves = getRookMoves(game.ANTI_TO_MOVE_PIECES, game.OCCUPIED, spaceId);
            for (long move = moves; move != 0; move &= (move-1)){
                game.getPossibleMoves().addMoves(spaceId, Long.numberOfTrailingZeros(move));
            }
        }
    }

    public static void getPawnMoves(MinimalChessGame<? extends ChessBoard> game){
        short pieceId;
        int idx;
        int beginRow;
        if (game.getCurrentColorToMove() == PieceData.WHITE){
            pieceId = PieceData.WPAWN;
            idx = BitMasks.WIDX;
            beginRow = 6;
        }else{
            pieceId = PieceData.BPAWN;
            idx = BitMasks.BIDX;
            beginRow = 1;
        }

        long pBoard = game.getBoard().getBitBoard(pieceId);
        int spaceId;
        for (long occ = pBoard; occ != 0 ; occ &= (occ - 1)) {
            spaceId = Long.numberOfTrailingZeros(occ);
            long moves = getPawnMoves(game, idx, beginRow, spaceId);
            
            for (long move = moves; move != 0; move &= (move-1)){
                game.getPossibleMoves().addMoves(spaceId, Long.numberOfTrailingZeros(move));
            }
        }
    }

    private static long getPawnMoves(MinimalChessGame<? extends ChessBoard> game,
                                    int idx, int beginRow, int spaceId){

        long possibleMoves = BitMasks.PAWN_CAPTURE_MASKS[idx][spaceId] & game.NOT_TO_MOVE_PIECES_AND_ENPASSANT;
        long forward = BitMasks.PAWN_MOVEMENT_MASKS[idx][spaceId] & game.EMPTY;
        possibleMoves |= forward;
        long spaceIdBoard = BitMasks.getSingleSpaceBitBoard(spaceId);

        if (ChessBoard.getRow(spaceId) == beginRow
                && forward != 0){
            if (game.getCurrentColorToMove() == PieceData.WHITE)
                possibleMoves |= (spaceIdBoard >> (-ChessBoard.getOffsets(2, ChessBoard.NORTH)) & game.EMPTY);
            else
                possibleMoves |= (spaceIdBoard << (ChessBoard.getOffsets(2, ChessBoard.SOUTH)) & game.EMPTY);
        }
        return possibleMoves;
    }

    public static long getRookMoves(long notPieceOnSameSide, long occupied, int spaceId){
        return getRookMoves(occupied, spaceId)& notPieceOnSameSide;
    }

    public static long getRookMoves(long occupied, int spaceId){
        return BitMasks.HV_TO_ATK_MASKS[spaceId].get(occupied&BitMasks.ROOK_MASKS[spaceId]);
    }

    public static long getBishopMoves(long notPieceOnSameSide, long occupied, int spaceId){
        return getBishopMoves(occupied,spaceId)&
                notPieceOnSameSide;
    }

    public static long getBishopMoves(long occupied, int spaceId){
        return BitMasks.D_TO_ATK_MASKS[spaceId].get(occupied&BitMasks.BISHOP_MASKS[spaceId]);
    }

    public static long getKnightMoves(long notPieceOnSameSide, int spaceId){
        return BitMasks.KNIGHT_MOVE_MASKS[spaceId] & notPieceOnSameSide;
    }

    public static long getQueenMoves(long notPieceOnSameSide, long occupied, int spaceId){
        return getQueenMoves(occupied, spaceId)& notPieceOnSameSide;
    }

    public static long getQueenMoves(long occupied, int spaceId){
        return HVD_TO_ATK_MASKS[spaceId].get(occupied&BitMasks.QUEEN_MASKS[spaceId]);
    }

    public static long getKingMoves(long notPieceOnSameSide, int spaceId){
        return BitMasks.KING_MOVE_MASKS[spaceId]&notPieceOnSameSide;
    }

    public static long getSingleSpaceBitBoard(int spaceId){
        return 1L << spaceId;
    }

    public static long setBit(long bitBoard, int spaceId){
        return bitBoard | getSingleSpaceBitBoard(spaceId);
    }

    public static long getBit(long bitBoard, int spaceId){
        return bitBoard & getSingleSpaceBitBoard(spaceId);
    }

    public static long unSetBit(long bitBoard, int spaceId){
        return bitBoard & ~getSingleSpaceBitBoard(spaceId);
    }

    public static void printBitBoard(long bitBoard){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ChessBoard.TOTAL_SPACES; ++i){
            sb.append((bitBoard & getSingleSpaceBitBoard(i)) != 0 ? "1 " : "0 ");
            if ((i+1) % ChessBoard.BOARD_SIZE == 0) sb.append("\n");
        }
        System.out.println(sb);
    }
}
