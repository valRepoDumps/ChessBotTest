package ChessResources;

import ChessResources.ChessBoard.ChessBoard;
import ChessResources.Pieces.PieceData;

import java.util.HashMap;

public class BitMasks {
    public static final int WIDX = 0;
    public static final int BIDX = 1;
    //region ROW_AND_COL_MASKS
    public static final long[] COL_MASKS = new long[ChessBoard.BOARD_SIZE];
    public static final long[] COL_MASKS_NO_TIPS = new long[ChessBoard.BOARD_SIZE];

    public static final long[] NOT_COL_MASKS = new long[ChessBoard.BOARD_SIZE];

    public static final long[] ROW_MASKS = new long[ChessBoard.BOARD_SIZE];
    public static final long[] ROW_MASKS_NO_TIPS = new long[ChessBoard.BOARD_SIZE];
    public static final long[] NOT_ROW_MASKS = new long[ChessBoard.BOARD_SIZE];

    public static final long[] LR_UP_DIAG_MASKS = new long[2*ChessBoard.BOARD_SIZE-1];
    public static final long[] LR_UP_DIAG_MASKS_NO_TIPS = new long[2*ChessBoard.BOARD_SIZE-1];
    public static final long[] LR_DOWN_DIAG_MASKS = new long[2*ChessBoard.BOARD_SIZE-1];
    public static final long[] LR_DOWN_DIAG_MASKS_NO_TIPS = new long[2*ChessBoard.BOARD_SIZE-1];

    public static final long[] ROOK_MASKS = new long[ChessBoard.TOTAL_SPACES];
    public static final long[] BISHOP_MASKS = new long[ChessBoard.TOTAL_SPACES];
    public static final long[] QUEEN_MASKS = new long[ChessBoard.TOTAL_SPACES];

    //for knight
    public static final long NOT_01_COL = -217020518514230020L;
    public static final long NOT_67_COL = 4557430888798830399L;
    //endregion

    //region PAWN_CAPTURE_MASKS
    public static final long[][] PAWN_CAPTURE_MASKS = new long[2][ChessBoard.TOTAL_SPACES];
    public static final long[][] PAWN_MOVEMENT_MASKS = new long[2][ChessBoard.TOTAL_SPACES];
    //endregion

    //region KNIGHT_ATK_MASKS
    public static final long[] KNIGHT_MOVE_MASKS = new long[ChessBoard.TOTAL_SPACES];
    public static final long[] KING_MOVE_MASKS = new long[ChessBoard.TOTAL_SPACES];

    //DIAG&HORIZON_MASKS
    public static final HashMap<Long, Long>[] D_TO_ATK_MASKS = new HashMap[ChessBoard.TOTAL_SPACES];
    public static final HashMap<Long, Long>[] HV_TO_ATK_MASKS = new HashMap[ChessBoard.TOTAL_SPACES];
    public static final HashMap<Long, Long>[] HVD_TO_ATK_MASKS = new HashMap[ChessBoard.TOTAL_SPACES];
    //endregion

    //endregion

    //region ROW_AND_COL_MASKS
    public static void assigneRowAndColMasks(){
        //region COL_MASK
        COL_MASKS[0] = 72340172838076673L;
        COL_MASKS[1] = 144680345676153346L;
        COL_MASKS[2] = 289360691352306692L;
        COL_MASKS[3] = 578721382704613384L;
        COL_MASKS[4] = 1157442765409226768L;
        COL_MASKS[5] = 2314885530818453536L;
        COL_MASKS[6] = 4629771061636907072L;
        COL_MASKS[7] = -9187201950435737472L;

        NOT_COL_MASKS[0] = -72340172838076674L;
        NOT_COL_MASKS[1] = -144680345676153347L;
        NOT_COL_MASKS[2] = -289360691352306693L;
        NOT_COL_MASKS[3] = -578721382704613385L;
        NOT_COL_MASKS[4] = -1157442765409226769L;
        NOT_COL_MASKS[5] = -2314885530818453537L;
        NOT_COL_MASKS[6] = -4629771061636907073L;
        NOT_COL_MASKS[7] = 9187201950435737471L;
        //endregion

        //region ROW_MASK
        ROW_MASKS[0] = 255L;
        ROW_MASKS[1] = 65280L;
        ROW_MASKS[2] = 16711680L;
        ROW_MASKS[3] = 4278190080L;
        ROW_MASKS[4] = 1095216660480L;
        ROW_MASKS[5] = 280375465082880L;
        ROW_MASKS[6] = 71776119061217280L;
        ROW_MASKS[7] = -72057594037927936L;

        NOT_ROW_MASKS[0] = -256L;
        NOT_ROW_MASKS[1] = -65281L;
        NOT_ROW_MASKS[2] = -16711681L;
        NOT_ROW_MASKS[3] = -4278190081L;
        NOT_ROW_MASKS[4] = -1095216660481L;
        NOT_ROW_MASKS[5] = -280375465082881L;
        NOT_ROW_MASKS[6] = -71776119061217281L;
        NOT_ROW_MASKS[7] = 72057594037927935L;
        //endregion

        //region UPDIAG
        LR_UP_DIAG_MASKS[0] = 1L;
        LR_UP_DIAG_MASKS[1] = 258L;
        LR_UP_DIAG_MASKS[2] = 66052L;
        LR_UP_DIAG_MASKS[3]  = 16909320L;
        LR_UP_DIAG_MASKS[4]  = 4328785936L;
        LR_UP_DIAG_MASKS[5]  = 1108169199648L;
        LR_UP_DIAG_MASKS[6]  = 283691315109952L;
        LR_UP_DIAG_MASKS[7]  = 72624976668147840L;
        LR_UP_DIAG_MASKS[8]  = 145249953336295424L;
        LR_UP_DIAG_MASKS[9]  = 290499906672525312L;
        LR_UP_DIAG_MASKS[10] = 580999813328273408L;
        LR_UP_DIAG_MASKS[11] = 1161999622361579520L;
        LR_UP_DIAG_MASKS[12] = 2323998145211531264L;
        LR_UP_DIAG_MASKS[13] = 4647714815446351872L;
        LR_UP_DIAG_MASKS[14] = -9223372036854775808L;
        //endregion

        //region DOWN_DIAG
        LR_DOWN_DIAG_MASKS[0]  = 72057594037927936L;
        LR_DOWN_DIAG_MASKS[1]  = 144396663052566528L;
        LR_DOWN_DIAG_MASKS[2]  = 288794425616760832L;
        LR_DOWN_DIAG_MASKS[3]  = 577588855528488960L;
        LR_DOWN_DIAG_MASKS[4]  = 1155177711073755136L;
        LR_DOWN_DIAG_MASKS[5]  = 2310355422147575808L;
        LR_DOWN_DIAG_MASKS[6]  = 4620710844295151872L;
        LR_DOWN_DIAG_MASKS[7]  = -9205322385119247871L;
        LR_DOWN_DIAG_MASKS[8]  = 36099303471055874L;
        LR_DOWN_DIAG_MASKS[9]  = 141012904183812L;
        LR_DOWN_DIAG_MASKS[10] = 550831656968L;
        LR_DOWN_DIAG_MASKS[11] = 2151686160L;
        LR_DOWN_DIAG_MASKS[12] = 8405024L;
        LR_DOWN_DIAG_MASKS[13] = 32832L;
        LR_DOWN_DIAG_MASKS[14] = 128L;
        //endregion
        //ensure no_tips is above piece maskes
        //region MASK_NO_TIPS
        for (int i = 0; i < ChessBoard.BOARD_SIZE; ++i){
            COL_MASKS_NO_TIPS[i] = (COL_MASKS[i] & NOT_ROW_MASKS[0]) & NOT_ROW_MASKS[7];
        }
        for (int i = 0; i < ChessBoard.BOARD_SIZE; ++i){
            ROW_MASKS_NO_TIPS[i] = (ROW_MASKS[i] & NOT_COL_MASKS[0]) & NOT_COL_MASKS[7];
        }
        for (int i = 0; i < 2*ChessBoard.BOARD_SIZE-1; ++i){
            LR_UP_DIAG_MASKS_NO_TIPS[i] = (((LR_UP_DIAG_MASKS[i] & NOT_ROW_MASKS[0]) & NOT_ROW_MASKS[7])
                    & NOT_COL_MASKS[0]) & NOT_COL_MASKS[7];
        }
        for (int i = 0; i < 2*ChessBoard.BOARD_SIZE-1; ++i){
            LR_DOWN_DIAG_MASKS_NO_TIPS[i] = (((LR_DOWN_DIAG_MASKS[i] & NOT_ROW_MASKS[0]) & NOT_ROW_MASKS[7])
                    & NOT_COL_MASKS[0]) & NOT_COL_MASKS[7];
        }
        //endregion

        //region PIECE_MASKS
        for (int i = 0; i < ChessBoard.TOTAL_SPACES; ++i){
            int row = ChessBoard.getRow(i);
            int col = ChessBoard.getCol(i);

            ROOK_MASKS[i] = (ROW_MASKS_NO_TIPS[ChessBoard.getRow(i)]|COL_MASKS_NO_TIPS[ChessBoard.getCol(i)])&
                    ~getSingleSpaceBitBoard(i);

            //endures final tip bits removed.
            BISHOP_MASKS[i] = (LR_DOWN_DIAG_MASKS_NO_TIPS[ChessBoard.getLRDownDiag(i)]|
                    LR_UP_DIAG_MASKS_NO_TIPS[ChessBoard.getLRUpDiag(i)])&~getSingleSpaceBitBoard(i);

            //endures final tip bits removed.
            QUEEN_MASKS[i] = BISHOP_MASKS[i]|ROOK_MASKS[i];
        }
        //endregion

    }
    //endregion
    static {
        assigneRowAndColMasks();
        preCalcPieceMasks();
    }
    //region PIECE_MASKS
    public static void preCalcPieceMasks(){
        preCalcPawnMasks();
        preCalcKnightMasks();
        preCalcKingMasks();
        preCalcAllDiagMasks();
        preCalcAllHorizontalAndVerticalMasks();
        preCalcAllDirsMasks();
    }

    public static void preCalcPawnMasks(){
        for (int i = 0; i < ChessBoard.TOTAL_SPACES; ++i){
            PAWN_CAPTURE_MASKS[WIDX][i]  = preCalcPawnCapture(PieceData.WHITE, i);
            PAWN_CAPTURE_MASKS[BIDX][i]  = preCalcPawnCapture(PieceData.BLACK, i);
            PAWN_MOVEMENT_MASKS[WIDX][i] = preCalcPawnMoveForward(PieceData.WHITE, i);
            PAWN_MOVEMENT_MASKS[BIDX][i] = preCalcPawnMoveForward(PieceData.BLACK, i);
        }
    }

    public static long preCalcPawnCapture(boolean color, int spaceId){
        long P = getSingleSpaceBitBoard(spaceId);
        long pawnMoves;
        if (color == PieceData.WHITE) {
            pawnMoves = (P >> (-ChessBoard.getOffsets(ChessBoard.NORTH_WEST))) & NOT_COL_MASKS[7];
            pawnMoves |= (P >> (-ChessBoard.getOffsets(ChessBoard.NORTH_EAST))) & NOT_COL_MASKS[0];
        }else{
            pawnMoves = (P << (ChessBoard.getOffsets(ChessBoard.SOUTH_WEST))) & NOT_COL_MASKS[7];
            pawnMoves |= (P << (ChessBoard.getOffsets(ChessBoard.SOUTH_EAST))) & NOT_COL_MASKS[0];
        }
        return pawnMoves;
    }

    public static long preCalcPawnMoveForward(boolean color, int spaceId){
        long p = getSingleSpaceBitBoard(spaceId);
        long pawnMoves;
        int row = ChessBoard.getRow(spaceId);

        if (color == PieceData.WHITE){
            if (row == 0) return 0L;
            pawnMoves = p >> (-ChessBoard.getOffsets(ChessBoard.NORTH));
//            if (row == 6){
//                pawnMoves |= p >> (-ChessBoard.getOffsets(2, ChessBoard.NORTH));
//            }
        }else{
            if (row == 7) return 0L;
            pawnMoves = p << (ChessBoard.getOffsets(ChessBoard.SOUTH));
//            if (row == 6){
//                pawnMoves |= p << (ChessBoard.getOffsets(2, ChessBoard.SOUTH));
//            }
        }
        return pawnMoves;
    }

    public static void preCalcKingMasks(){
        for (int i = 0; i < ChessBoard.TOTAL_SPACES; ++i){
            KING_MOVE_MASKS[i] = preCalcKingMasks(i);
        }
    }

    public static long preCalcKingMasks(int spaceId){
        long K = getSingleSpaceBitBoard(spaceId);
        long kingMoves = (K >> (-ChessBoard.getOffsets(ChessBoard.NORTH_EAST))) & NOT_COL_MASKS[0];

        kingMoves |= (K >> (-ChessBoard.getOffsets(ChessBoard.NORTH_WEST))) & NOT_COL_MASKS[7];

        kingMoves |= (K << (ChessBoard.getOffsets(ChessBoard.SOUTH_EAST))) & NOT_COL_MASKS[0];

        kingMoves |= (K << (ChessBoard.getOffsets(ChessBoard.SOUTH_WEST))) & NOT_COL_MASKS[7];

        kingMoves |= (K << (ChessBoard.getOffsets(ChessBoard.EAST))) & NOT_COL_MASKS[0];

        kingMoves |= (K >> (-ChessBoard.getOffsets(ChessBoard.WEST))) &  NOT_COL_MASKS[7];

        kingMoves |= (K << ChessBoard.getOffsets(ChessBoard.SOUTH)) & NOT_COL_MASKS[0];

        kingMoves |= (K >> (-ChessBoard.getOffsets(ChessBoard.NORTH))) & NOT_COL_MASKS[7];
        return kingMoves;
    }

    public static void preCalcKnightMasks(){
        for (int i = 0; i < ChessBoard.TOTAL_SPACES; ++i){
            KNIGHT_MOVE_MASKS[i] = preCalcKnightMasks(i);
        }
    }

    public static long preCalcKnightMasks(int spaceId){
        long N = getSingleSpaceBitBoard(spaceId);

        long knightMoves = (N >> (-ChessBoard.getNOffsets(ChessBoard.K_NE))) & NOT_COL_MASKS[0];

        knightMoves |= (N >> (-ChessBoard.getNOffsets(ChessBoard.K_NW))) & NOT_COL_MASKS[7];

        knightMoves |= (N << (ChessBoard.getNOffsets(ChessBoard.K_SE))) & NOT_COL_MASKS[0];

        knightMoves |= (N << (ChessBoard.getNOffsets(ChessBoard.K_SW))) & NOT_COL_MASKS[7];

        knightMoves |= (N >> (-ChessBoard.getNOffsets(ChessBoard.K_EN))) & NOT_01_COL;

        knightMoves |= (N >> (-ChessBoard.getNOffsets(ChessBoard.K_WN))) &  NOT_67_COL;

        knightMoves |= (N << ChessBoard.getNOffsets(ChessBoard.K_ES)) & NOT_01_COL;

        knightMoves |= (N << ChessBoard.getNOffsets(ChessBoard.K_WS)) & NOT_67_COL;

        return knightMoves;
    }

    public static void preCalcAllDiagAtkMasksAt(int spaceId){
        long mask = preCalcRefinedDiagAtkMask(spaceId);
        D_TO_ATK_MASKS[spaceId] = new HashMap<>();
        for (long occ = mask; ; occ = (occ - 1) & mask) {
            if(D_TO_ATK_MASKS[spaceId].get(occ) != null){
                System.out.println("IMPOSSIBLE");
            }

            D_TO_ATK_MASKS[spaceId].put(occ, preCalcDiagAtkMask(spaceId, occ));
            if (occ == 0) break;
        }
    }

    public static void preCalcAllDiagMasks(){
        for (int i = 0; i < ChessBoard.TOTAL_SPACES; ++i){
            preCalcAllDiagAtkMasksAt(i);
        }
    }

    public static long preCalcDiagAtkMask(int spaceId, long occupanciesBoard) {
        long piece = getSingleSpaceBitBoard(spaceId);

        int upIdx = ChessBoard.getLRUpDiag(spaceId);
        int downIdx = ChessBoard.getLRDownDiag(spaceId);

        long upMask = BitMasks.LR_UP_DIAG_MASKS[upIdx];
        long downMask = BitMasks.LR_DOWN_DIAG_MASKS[downIdx];

        long occUp = occupanciesBoard & upMask;
        long occDown = occupanciesBoard & downMask;

        long upMoves =
                (occUp - (piece << 1)) ^
                        Long.reverse(Long.reverse(occUp) - (Long.reverse(piece) << 1));
        long downMoves =
                (occDown - (piece << 1)) ^
                        Long.reverse(Long.reverse(occDown) - (Long.reverse(piece) << 1));
        return (upMoves & upMask) | (downMoves & downMask);
    }

    public static long preCalcRefinedDiagAtkMask(int spaceId){
        return preCalcDiagAtkMask(spaceId, 0L) & BISHOP_MASKS[spaceId];
    }

    //use hyperbolean quintessance.
    public static long preCalcHorizontalAndVerticalAtkMask(int spaceId, long occupanciesBoard) {
        long piece = getSingleSpaceBitBoard(spaceId);

        int row = ChessBoard.getRow(spaceId);
        int col = ChessBoard.getCol(spaceId);

        long rowMask = BitMasks.ROW_MASKS[row];
        long colMask = BitMasks.COL_MASKS[col];

        long occRow = occupanciesBoard & rowMask;
        long occCol = occupanciesBoard & colMask;

        long horizontal =
                (occRow - (piece << 1)) ^
                        Long.reverse(Long.reverse(occRow) - (Long.reverse(piece) << 1));

        long vertical =
                (occCol - (piece << 1)) ^
                        Long.reverse(Long.reverse(occCol) - (Long.reverse(piece) << 1));

        return (horizontal & rowMask) | (vertical & colMask);
    }

    public static long preCalcRefinedHorizontalAndVerticalMove(int spaceId){
        return (preCalcHorizontalAndVerticalAtkMask(spaceId, 0L) & ROOK_MASKS[spaceId]);
    }

    public static void preCalcAllHorizontalAndVerticalAtkMasksAt(int spaceId){
        long mask = preCalcRefinedHorizontalAndVerticalMove(spaceId);
        HV_TO_ATK_MASKS[spaceId] = new HashMap<>();
        for (long occ = mask; ; occ = (occ - 1) & mask) {
            if(HV_TO_ATK_MASKS[spaceId].get(occ) != null){
                System.out.println("IMPOSSIBLE");
            }

            HV_TO_ATK_MASKS[spaceId].put(occ, preCalcHorizontalAndVerticalAtkMask(spaceId, occ));
            if (occ == 0) break; //break last cause still want to include case == 0.
        }
    }

    public static void preCalcAllHorizontalAndVerticalMasks(){
        for (int i = 0; i < ChessBoard.TOTAL_SPACES; ++i){
            preCalcAllHorizontalAndVerticalAtkMasksAt(i);
        }
    }

    public static void preCalcAllDirsMasks(){
        for (int i = 0; i < ChessBoard.TOTAL_SPACES; ++i){
            preCalcAllDirsMasksAt(i);
        }
    }

    public static void preCalcAllDirsMasksAt(int spaceId){
        long mask = preCalcRefinedHorizontalAndVerticalMove(spaceId) | preCalcRefinedDiagAtkMask(spaceId);
        HVD_TO_ATK_MASKS[spaceId] = new HashMap<>();
        for (long occ = mask; ; occ = (occ - 1) & mask) {
            if(HVD_TO_ATK_MASKS[spaceId].get(occ) != null){
                System.out.println("IMPOSSIBLE");
            }

            HVD_TO_ATK_MASKS[spaceId].put(occ,
                    preCalcHorizontalAndVerticalAtkMask(spaceId, occ)|preCalcDiagAtkMask(spaceId, occ) );
            if (occ == 0) break;
        }
    }

    //endregion

    //region HELPER
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

    public static int countBit(long bitBoard){
        int i = 0;
        for (; bitBoard != 0; ++i){
            bitBoard &= bitBoard-1;
        }
        return i;
    }

    public static int getZeroesBeforeBit(long bitBoard){
        return Long.numberOfTrailingZeros(bitBoard);
    }
    public static void printBitBoard(long bitBoard){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ChessBoard.TOTAL_SPACES; ++i){
            sb.append((bitBoard & getSingleSpaceBitBoard(i)) != 0 ? "1 " : "0 ");
            if ((i+1) % ChessBoard.BOARD_SIZE == 0) sb.append("\n");
        }
        System.out.println(sb);
    }



    //endregion
}
