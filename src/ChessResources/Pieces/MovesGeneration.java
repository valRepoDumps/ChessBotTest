package ChessResources.Pieces;

import ChessLogic.MinimalChessGame;
import ChessResources.BitMasks;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessMove;



public class MovesGeneration {

    public static void generateMoves(MinimalChessGame game){
        getKingMoves(game);
        getQueenMovesLog(game);
        getKnightMovesLog(game);
        getBishopMovesLog(game);
        getPawnMovesLog(game);
        getRookMovesLog(game);
    }

    public static void getKingMoves(MinimalChessGame game){
        short pieceId;
        long QUEEN_SIDE_ID;
        long KING_SIDE_ID;
        if (game.getCurrentColorToMove() == PieceData.WHITE){
            pieceId = PieceData.WKING;
            QUEEN_SIDE_ID = ChessMove.WHITE_CASTLE_QUEEN_SIDE;
            KING_SIDE_ID = ChessMove.WHITE_CASTLE_KING_SIDE;
        }else{
            pieceId = PieceData.BKING;
            QUEEN_SIDE_ID = ChessMove.BLACK_CASTLE_QUEEN_SIDE;
            KING_SIDE_ID = ChessMove.BLACK_CASTLE_KING_SIDE;
        }

        long kBoard = game.getBoard().getBitBoard(pieceId);
        int spaceId;

        for (long i = kBoard; i != 0; i &= (i-1)){
            spaceId = Long.numberOfTrailingZeros(i);
            long moves = getKingMoves(game.getBoard().ANTI_TO_MOVE_PIECES, spaceId);
            for (long move = moves&game.getBoard().EMPTY; move != 0; move &= move-1){
                //check non-capture moves
                int spcId = Long.numberOfTrailingZeros(move);

                if (!game.spaceUnderThreat(spcId, game.getBoard().OCCUPIED_NO_KING, BitMasks.ALL_ONES))
                    addMove(game, spaceId, spcId, pieceId);
            }

            for (long move = moves&game.getBoard().NOT_TO_MOVE_PIECES; move != 0; move &= move-1){
                //check capture moves
                int spcId = Long.numberOfTrailingZeros(move);

                if (!game.spaceUnderThreat(spcId, game.getBoard().OCCUPIED_NO_KING, BitMasks.ALL_ONES))
                    addMove(game, spaceId, spcId, pieceId, ChessMove.CAPTURE);
            }

            if (game.canToMoveCastleKing() && game.getBoard().CAN_CASTLE_KING){
                int moveOne = spaceId + ChessBoard.getOffsets(1, ChessBoard.EAST);
                int moveTwo = spaceId + ChessBoard.getOffsets(2, ChessBoard.EAST);
                if (!game.spaceUnderThreat(spaceId, game.getBoard().OCCUPIED_NO_KING, BitMasks.ALL_ONES) &&
                    !game.spaceUnderThreat(moveOne, game.getBoard().OCCUPIED_NO_KING, BitMasks.ALL_ONES) &&
                    !game.spaceUnderThreat(moveTwo, game.getBoard().OCCUPIED_NO_KING, BitMasks.ALL_ONES)){
                    addMove(game, spaceId,
                            moveTwo,
                            pieceId,
                            KING_SIDE_ID);
                }
            }

            if (game.canToMoveCastleQueen() && game.getBoard().CAN_CASTLE_QUEEN){

                int moveOne = spaceId + ChessBoard.getOffsets(1, ChessBoard.WEST);
                int moveTwo = spaceId + ChessBoard.getOffsets(2, ChessBoard.WEST);
                if (!game.spaceUnderThreat(spaceId, game.getBoard().OCCUPIED_NO_KING, BitMasks.ALL_ONES) &&
                        !game.spaceUnderThreat(moveOne, game.getBoard().OCCUPIED_NO_KING, BitMasks.ALL_ONES) &&
                        !game.spaceUnderThreat(moveTwo, game.getBoard().OCCUPIED_NO_KING, BitMasks.ALL_ONES)){
                    addMove(game, spaceId,
                            moveTwo,
                            pieceId,
                            QUEEN_SIDE_ID);
                }

            }
        }

    }

    public static void getQueenMovesLog(MinimalChessGame game){
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
            getQueenMovesLog(game, spaceId, pieceId);
        }
    }

    public static void getKnightMovesLog(MinimalChessGame game){
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
            long moves = getKnightMoves(game.getBoard().ANTI_TO_MOVE_PIECES, spaceId);

            //adding non capture moves
            addBitBoardToPossibleMoves(game, spaceId, moves&game.getBoard().EMPTY, pieceId);

            for (long move = moves&game.getBoard().NOT_TO_MOVE_PIECES; move != 0; move &= move-1){
                int spaceIdMoveTo = Long.numberOfTrailingZeros(move);
                addMove(game, spaceId, spaceIdMoveTo, pieceId, ChessMove.CAPTURE);
            }
        }
    }

    public static void getBishopMovesLog(MinimalChessGame game){
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
            getBishopMovesLog(game, spaceId, pieceId);
        }
    }

    public static void getRookMovesLog(MinimalChessGame game){
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
            getRookMovesLog(game, spaceId, pieceId);
        }
    }

    public static void getPawnMovesLog(MinimalChessGame game){
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
            getPawnMovesLog(game, idx, beginRow, spaceId, pieceId);
        }
    }

    private static void getPawnMovesLog(MinimalChessGame game,
                                        int idx, int beginRow, int spaceId, short pieceId){
        int promotionRow;
        int enpassantDir;

        if (pieceId == PieceData.WPAWN){
            promotionRow = 1;
            enpassantDir = ChessBoard.directionOffsets[ChessBoard.SOUTH];
        }else{
            promotionRow = ChessBoard.BOARD_SIZE-2;
            enpassantDir = ChessBoard.directionOffsets[ChessBoard.NORTH];
        }

        boolean promotion = ChessBoard.getRow(spaceId) == promotionRow;
        long moves = BitMasks.PAWN_CAPTURE_MASKS[idx][spaceId] &
                game.getBoard().NOT_TO_MOVE_PIECES;
        int spaceIdCaptureAt;

        for (long move = moves; move != 0; move &= move-1) {
            spaceIdCaptureAt = Long.numberOfTrailingZeros(move);
            if (!promotion) {
                addMove(game, spaceId,
                        spaceIdCaptureAt,
                        pieceId,
                        ChessMove.CAPTURE);
            }else{
                addPromotionMoves(game,
                        spaceId,
                        spaceIdCaptureAt,
                        spaceIdCaptureAt,
                        pieceId,
                        ChessMove.CAPTURE);
            }
        }

        moves = BitMasks.PAWN_CAPTURE_MASKS[idx][spaceId] &
                game.getBoard().ENPASSANT;

        for (long move = moves; move != 0; move &= move-1) {
            spaceIdCaptureAt = Long.numberOfTrailingZeros(move);
            if (!promotion) {
                addMove(game, spaceId,                 // ← fixed: pawn's actual current square
                        spaceIdCaptureAt,
                        spaceIdCaptureAt + enpassantDir,              // captured pawn's square
                        pieceId,
                        ChessMove.CAPTURE | ChessMove.EN_PASSANT);
            }else{
                addPromotionMoves(game, spaceId,                 // ← fixed: pawn's actual current square
                        spaceIdCaptureAt,
                        spaceIdCaptureAt + enpassantDir,              // captured pawn's square
                        pieceId,
                        ChessMove.CAPTURE | ChessMove.EN_PASSANT);
            }
        }

        long forward = BitMasks.PAWN_MOVEMENT_MASKS[idx][spaceId]
                & game.getBoard().EMPTY;
        moves = forward;

        for (long move = moves; move != 0; move &= move-1) {
            spaceIdCaptureAt = Long.numberOfTrailingZeros(move);
            if (!promotion) {
                addMove(game, spaceId, spaceIdCaptureAt, pieceId);
            }else{
                addPromotionMoves(game, spaceId, spaceIdCaptureAt, spaceIdCaptureAt, pieceId, 0L);
            }
        }

        long spaceIdBoard = BitMasks.getSingleSpaceBitBoard(spaceId);

        if (ChessBoard.getRow(spaceId) == beginRow
                && forward != 0){
            if (game.getCurrentColorToMove() == PieceData.WHITE)
                moves = (spaceIdBoard >>
                        (-ChessBoard.getOffsets(2, ChessBoard.NORTH)) &
                        game.getBoard().EMPTY);
            else
                moves = (spaceIdBoard <<
                        (ChessBoard.getOffsets(2, ChessBoard.SOUTH)) &
                        game.getBoard().EMPTY);

            for (long move = moves; move != 0; move &= move-1) {
                int spaceIdArriveAt = Long.numberOfTrailingZeros(move);
                addMove(game, spaceId, spaceIdArriveAt, pieceId,
                        (spaceIdArriveAt + enpassantDir));
                //no need for promotion check, cause never promoting
            }
        }
    }

    private static void addPromotionMoves(MinimalChessGame game,
                                          int spaceId, int spaceIdArriveAt,
                                          int spaceIdCaptureAt,
                                          short pieceId, long flag){
        if (PieceData.getColor(pieceId) == PieceData.WHITE) {
            addMove(game, spaceId, spaceIdArriveAt, spaceIdCaptureAt, pieceId, flag | ChessMove.PROMOTION_WBISHOP);
            addMove(game, spaceId, spaceIdArriveAt, spaceIdCaptureAt,pieceId, flag | ChessMove.PROMOTION_WROOK);
            addMove(game, spaceId, spaceIdArriveAt, spaceIdCaptureAt,pieceId, flag | ChessMove.PROMOTION_WKNIGHT);
            addMove(game, spaceId, spaceIdArriveAt, spaceIdCaptureAt,pieceId, flag | ChessMove.PROMOTION_WQUEEN);
        }else{
            addMove(game, spaceId, spaceIdArriveAt, spaceIdCaptureAt,pieceId, flag | ChessMove.PROMOTION_BBISHOP);
            addMove(game, spaceId, spaceIdArriveAt, spaceIdCaptureAt,pieceId, flag | ChessMove.PROMOTION_BROOK);
            addMove(game, spaceId, spaceIdArriveAt, spaceIdCaptureAt,pieceId, flag | ChessMove.PROMOTION_BKNIGHT);
            addMove(game, spaceId, spaceIdArriveAt, spaceIdCaptureAt,pieceId, flag | ChessMove.PROMOTION_BQUEEN);
        }
    }

    public static void getRookMovesLog(MinimalChessGame game,
                                       int spaceId, short pieceId){
        long unRefinedMoves = getRookMoves(game.getBoard().OCCUPIED, spaceId);

        addBitBoardToPossibleMoves(game, spaceId,
                unRefinedMoves & game.getBoard().EMPTY,
                pieceId);

        long captureMoves = unRefinedMoves & game.getBoard().NOT_TO_MOVE_PIECES;
        for (long move = captureMoves; move != 0; move&=move-1){
            addMove(game, spaceId, Long.numberOfTrailingZeros(move), pieceId, ChessMove.CAPTURE);
        }
    }

    public static long getRookMoves(long occupied, int spaceId){
        return BitMasks.rookAttacks(spaceId, occupied);
//        return BitMasks.HV_TO_ATK_MASKS[spaceId].get(occupied&BitMasks.ROOK_MASKS[spaceId]);
    }

    public static void getBishopMovesLog(MinimalChessGame game,
                                         int spaceId, short pieceId){
        long unRefinedMoves = getBishopMoves(game.getBoard().OCCUPIED,spaceId);
        addBitBoardToPossibleMoves(game, spaceId,
                unRefinedMoves & game.getBoard().EMPTY,
                pieceId);

        long captureMoves = unRefinedMoves & game.getBoard().NOT_TO_MOVE_PIECES;

        for (long move = captureMoves; move != 0; move&=move-1){
            addMove(game, spaceId, Long.numberOfTrailingZeros(move), pieceId, ChessMove.CAPTURE);
        }
    }

    public static long getBishopMoves(long occupied, int spaceId){
        return BitMasks.bishopAttacks(spaceId, occupied);
    }

    public static long getKnightMoves(long notPieceOnSameSide, int spaceId){
        return BitMasks.KNIGHT_MOVE_MASKS[spaceId] & notPieceOnSameSide;
    }

    public static void getQueenMovesLog(MinimalChessGame game,
                                        int spaceId, short pieceId){
        long unRefinedMoves = getQueenMoves(game.getBoard().OCCUPIED, spaceId);


        addBitBoardToPossibleMoves(game, spaceId,
                unRefinedMoves & game.getBoard().EMPTY,
                pieceId);

        long captureMoves = unRefinedMoves & game.getBoard().NOT_TO_MOVE_PIECES;

        for (long move = captureMoves; move != 0; move&=move-1){
            addMove(game, spaceId, Long.numberOfTrailingZeros(move), pieceId, ChessMove.CAPTURE);
        }
    }

    public static long getQueenMoves(long occupied, int spaceId){
        return BitMasks.queenAttacks(spaceId, occupied);
    }

    public static long getKingMoves(long notPieceOnSameSide, int spaceId){
        return BitMasks.KING_MOVE_MASKS[spaceId]&notPieceOnSameSide;
    }

    private static void addMove(MinimalChessGame game, int currSpaceId,
                                int spaceIdArriveAt, int spaceIdCaptureAt, short pieceId,
                                long flags){
        long OCCUPIED = BitMasks.setBit(
                BitMasks.unSetBit(
                BitMasks.unSetBit(
                        game.getBoard().OCCUPIED,
                        spaceIdCaptureAt),
                        currSpaceId),
                        spaceIdArriveAt);

        long setMoves = BitMasks.getSingleSpaceBitBoardZero(spaceIdCaptureAt);
        int kingSpc;
        if (pieceId == PieceData.WKING || pieceId == PieceData.BKING){
            kingSpc = spaceIdArriveAt;
        }else{
            kingSpc = game.getKingToMoveSpaceId();
        }

        if (!game.spaceUnderThreat(kingSpc, OCCUPIED, setMoves)){
            game.getPossibleMoves().addMoves(new ChessMove(currSpaceId,
                    spaceIdArriveAt, spaceIdCaptureAt,
                    pieceId, flags));
        }
    }

    private static void addMove(MinimalChessGame game, int currSpaceId,
                                int spaceIdArriveAt, short pieceId,
                                long flags){
        addMove(game, currSpaceId, spaceIdArriveAt, spaceIdArriveAt, pieceId, flags);
    }

    private static void addMove(MinimalChessGame game, int currSpaceId,
                                int spaceIdArriveAt, short pieceId){
        addMove(game, currSpaceId, spaceIdArriveAt, spaceIdArriveAt, pieceId, 0L);
    }
    public static long getSingleSpaceBitBoard(int spaceId){
        return 1L << spaceId;
    }

    public static void addBitBoardToPossibleMoves(MinimalChessGame game,
                                                  int spaceId, long moves, short pieceId){
        for (long move = moves; move != 0; move &= move-1){
            int spaceIdMoveTo = Long.numberOfTrailingZeros(move);
            addMove(game, spaceId, spaceIdMoveTo, pieceId);
        }
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
