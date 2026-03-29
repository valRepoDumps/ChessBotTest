package ChessResources;

import ChessResources.ChessBoard.ChessBoard;
import ChessResources.Pieces.PieceData;

import java.util.HashMap;
import java.util.Random;
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

    //region CASTLING
    public static final long WHITE_CASTLE_KING = 6917529027641081856L;
    public static final long WHITE_CASTLE_QUEEN = 1008806316530991104L;
    public static final long BLACK_CASTLE_KING = 96L;
    public static final long BLACK_CASTLE_QUEEN = 14L;
    //endregion

    public static final long ALL_ONES = -1L;
    public static final long ALL_ZEROES = 0L;
    //endregion

    //region PAWN_CAPTURE_MASKS
    public static final long[][] PAWN_CAPTURE_MASKS = new long[2][ChessBoard.TOTAL_SPACES];
    public static final long[][] PAWN_MOVEMENT_MASKS = new long[2][ChessBoard.TOTAL_SPACES];
    //endregion

    //region KNIGHT_ATK_MASKS
    public static final long[] KNIGHT_MOVE_MASKS = new long[ChessBoard.TOTAL_SPACES];
    public static final long[] KING_MOVE_MASKS = new long[ChessBoard.TOTAL_SPACES];

    //region RAY_MASKS
    //used for king threats calc, thus include the pieces.
    public static final long[][] D_RAY_MASK = new long[ChessBoard.TOTAL_SPACES][ChessBoard.TOTAL_SPACES];
    public static final long[][] HV_RAY_MASK = new long[ChessBoard.TOTAL_SPACES][ChessBoard.TOTAL_SPACES];
    public static final long[][] HVD_RAY_MASK = new long[ChessBoard.TOTAL_SPACES][ChessBoard.TOTAL_SPACES];
    //endregion

    private static final int ROOK_BITS   = 12;
    private static final int BISHOP_BITS = 9;
    private static final int ROOK_SIZE   = 1 << ROOK_BITS;   // 4096
    private static final int BISHOP_SIZE = 1 << BISHOP_BITS; // 512

    public static final int[] ROOK_SHIFTS = {52, 53, 53, 53, 53, 53, 53, 52, 53,
                                            54, 54, 54, 54, 54, 54, 53, 53, 54,
                                            54, 54, 54, 54, 54, 53, 53, 54, 54,
                                            54, 54, 54, 54, 53, 53, 54, 54, 54,
                                            54, 54, 54, 53, 53, 54, 54, 54, 54,
                                            54, 54, 53, 53, 54, 54, 54, 54, 54,
                                            54, 53, 52, 53, 53, 53, 53, 53, 53, 52};

    public static final int[] BISHOP_SHIFTS = {58, 59, 59, 59, 59, 59, 59, 58, 59,
                                            59, 59, 59, 59, 59, 59, 59, 59, 59, 57,
                                            57, 57, 57, 59, 59, 59, 59, 57, 55, 55,
                                            57, 59, 59, 59, 59, 57, 55, 55, 57, 59,
                                            59, 59, 59, 57, 57, 57, 57, 59, 59, 59,
                                            59, 59, 59, 59, 59, 59, 59, 58, 59, 59,
                                            59, 59, 59, 59, 58};

    //endregion

    // ---- Bishop magics ----
    public static final long[] BISHOP_MAGICS =
    { 0x8183004420820L, 0x42802018c010000L, 0x3210011461000018L, 0x102208200880000L,
            0xa0210403008b6L, 0x60020a3044800001L, 0x2009441028e82050L, 0x882801042000L,
            0x4182a2024068200L, 0x8000146408020062L, 0x600100081810028L, 0x89d82041c00005L,
            0x448091041000220L, 0x2000010108c00000L, 0x9000108090901040L, 0x480020201118802L,
            0x8402220244484L, 0x2200200a0c0120L, 0x100001010a0013L, 0x4a88002082004040L,
            0x40020104010c0024L, 0x101010601011940L, 0x810d010410884400L, 0x80402108440c2400L,
            0x18401020521200L, 0x14020110022840L, 0x1008c10110040080L, 0x448180048820002L,
            0x8840020802020L, 0x489004082005000L, 0x81204a3010120L, 0x4104100210c00L,
            0x2460084060020eL, 0x3004882010540420L, 0x10020240c1040100L, 0x20c0602120280080L,
            0x100200444a0104L, 0x1013014a02410100L, 0xa081201210080L, 0x418200821000a204L,
            0x88080801400cL, 0x22049028040408L, 0x8002012608040100L, 0x58090401000821L,
            0x400204411c000200L, 0x2960430218802808L, 0x3021022c102286L, 0x4008408108400a02L,
            0x22d8620220204003L, 0x1c00232802302040L, 0x16110401040004L, 0x40004420881000L,
            0x801002021002L, 0x8900400a44010080L, 0x2004040418020810L, 0x2210020085220022L,
            0x1100440401080208L, 0x958010400ac3400L, 0x420065200c24801L, 0x400e84011040900L,
            0x21001109210104L, 0x20242420200c2323L, 0x80040084a240040L, 0x8840042800890030L
    };

    // ---- Rook magics ----
    public static final long[] ROOK_MAGICS =
            { 0x8001a040001080L, 0x8600104082002102L, 0x200204200088010L, 0x80041000808800L,
            0x9100050050023800L, 0x8600040108020030L, 0x40010050c0a0088L, 0xa00082104004082L,
            0x880800084604001L, 0x1b00400050042002L, 0x8050040e0001100L, 0x2002012000840L,
            0x1240804400280080L, 0x8083000c01000812L, 0x82808002000300L, 0xc1a0800041001180L,
            0x288000884000L, 0x100434000201001L, 0x444808010002000L, 0x3d40808050000800L,
            0x5010808004000802L, 0x411010002082c00L, 0x9440010190802L, 0x1802001040931cL,
            0x200400080108020L, 0x9008200440100140L, 0x2880804200220410L, 0x80c0400a00102200L,
            0x101100500080100L, 0x5400040080320080L, 0x9042011400883022L, 0x8018008600004924L,
            0xc0804000a1800880L, 0x1105400100228cL, 0x1050008010802000L, 0x10100121000900L,
            0x1000080081800400L, 0x5400808400802200L, 0x2000061044000148L, 0x400040242000e91L,
            0x2060a08040008004L, 0x260020100041c000L, 0x920080420022L, 0x2610181001010020L,
            0x6022000820120004L, 0x1004400830008L, 0x900301280a040010L, 0x50910300a0420004L,
            0x140008000422080L, 0x150a0040608200L, 0x280904020030100L, 0x803300018210500L,
            0x80080092c0080L, 0x2a000810040200L, 0x2400280530229c00L, 0x20827008c004a00L,
            0x9041200402382L, 0xc301040a0810602L, 0xc01000830200143L, 0x2190005500021L,
            0xa40d005004120801L, 0x8802000401089002L, 0x4029080091102204L, 0x2841810400538122L
    };

    //region DIAG&HORIZON_MASKS

    public static final long[][] HV_TO_ATK_MASKS = new long[64][ROOK_SIZE];
    public static final long[][] D_TO_ATK_MASKS = new long[64][BISHOP_SIZE];
//    public static final HashMap<Long, Long>[] HVD_TO_ATK_MASKS = new HashMap[ChessBoard.TOTAL_SPACES];
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
//        preCalcAllRaysMasks();
//        findMagicsAtRuntime();
    }
    //region PIECE_MASKS
    public static void preCalcPieceMasks(){
        preCalcPawnMasks();
        preCalcKnightMasks();
        preCalcKingMasks();
        preCalcAllDiagMasks();
        preCalcAllHorizontalAndVerticalMasks();
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
            pawnMoves = (P >>> (-ChessBoard.getOffsets(ChessBoard.NORTH_WEST))) & NOT_COL_MASKS[7];
            pawnMoves |= (P >>> (-ChessBoard.getOffsets(ChessBoard.NORTH_EAST))) & NOT_COL_MASKS[0];
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
            pawnMoves = p >>> (-ChessBoard.getOffsets(ChessBoard.NORTH));
//            if (row == 6){
//                pawnMoves |= p >>> (-ChessBoard.getOffsets(2, ChessBoard.NORTH));
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
        long kingMoves = (K >>> (-ChessBoard.getOffsets(ChessBoard.NORTH_EAST))) & NOT_COL_MASKS[0];

        kingMoves |= (K >>> (-ChessBoard.getOffsets(ChessBoard.NORTH_WEST))) & NOT_COL_MASKS[7];

        kingMoves |= (K << (ChessBoard.getOffsets(ChessBoard.SOUTH_EAST))) & NOT_COL_MASKS[0];

        kingMoves |= (K << (ChessBoard.getOffsets(ChessBoard.SOUTH_WEST))) & NOT_COL_MASKS[7];

        kingMoves |= (K << (ChessBoard.getOffsets(ChessBoard.EAST))) & NOT_COL_MASKS[0];

        kingMoves |= (K >>> (-ChessBoard.getOffsets(ChessBoard.WEST))) &  NOT_COL_MASKS[7];

        kingMoves |= (K << ChessBoard.getOffsets(ChessBoard.SOUTH));

        kingMoves |= (K >>> (-ChessBoard.getOffsets(ChessBoard.NORTH)));

        return kingMoves;
    }

    public static void preCalcKnightMasks(){
        for (int i = 0; i < ChessBoard.TOTAL_SPACES; ++i){
            KNIGHT_MOVE_MASKS[i] = preCalcKnightMasks(i);
        }
    }

    public static long preCalcKnightMasks(int spaceId){
        long N = getSingleSpaceBitBoard(spaceId);

        long knightMoves = (N >>> (-ChessBoard.getNOffsets(ChessBoard.K_NE))) & NOT_COL_MASKS[0];

        knightMoves |= (N >>> (-ChessBoard.getNOffsets(ChessBoard.K_NW))) & NOT_COL_MASKS[7];

        knightMoves |= (N << (ChessBoard.getNOffsets(ChessBoard.K_SE))) & NOT_COL_MASKS[0];

        knightMoves |= (N << (ChessBoard.getNOffsets(ChessBoard.K_SW))) & NOT_COL_MASKS[7];

        knightMoves |= (N >>> (-ChessBoard.getNOffsets(ChessBoard.K_EN))) & NOT_01_COL;

        knightMoves |= (N >>> (-ChessBoard.getNOffsets(ChessBoard.K_WN))) &  NOT_67_COL;

        knightMoves |= (N << ChessBoard.getNOffsets(ChessBoard.K_ES)) & NOT_01_COL;

        knightMoves |= (N << ChessBoard.getNOffsets(ChessBoard.K_WS)) & NOT_67_COL;

        return knightMoves;
    }

    public static void preCalcAllDiagAtkMasksAt
            (int spaceId, long magic, int shift){
        long mask = preCalcRefinedDiagAtkMask(spaceId);

        for (long occ = mask; ; occ = (occ - 1) & mask) {
            int idx = (int) ((occ*magic)>>>shift);
            D_TO_ATK_MASKS[spaceId][idx] = preCalcDiagAtkMask(spaceId, occ);
            if (occ == 0) break;
        }
    }

    public static void preCalcAllDiagMasks(){
        for (int i = 0; i < ChessBoard.TOTAL_SPACES; ++i){
            preCalcAllDiagAtkMasksAt(i, BISHOP_MAGICS[i], BISHOP_SHIFTS[i]);
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

    public static void preCalcAllHorizontalAndVerticalAtkMasksAt
            (int spaceId, long magic, int shift){
        long mask = preCalcRefinedHorizontalAndVerticalMove(spaceId);

        for (long occ = mask; ; occ = (occ - 1) & mask) {
            int idx = (int) ((occ*magic)>>>shift);

            HV_TO_ATK_MASKS[spaceId][idx] = preCalcHorizontalAndVerticalAtkMask(spaceId, occ);
            if (occ == 0) break; //break last cause still want to include case == 0.
        }
    }

    public static void preCalcAllHorizontalAndVerticalMasks(){
        for (int i = 0; i < ChessBoard.TOTAL_SPACES; ++i){
            preCalcAllHorizontalAndVerticalAtkMasksAt(i, ROOK_MAGICS[i], ROOK_SHIFTS[i]);
        }
    }

//    public static void preCalcAllDirsMasks(){
//        for (int i = 0; i < ChessBoard.TOTAL_SPACES; ++i){
//            preCalcAllDirsMasksAt(i);
//        }
//    }

//    public static void preCalcAllDirsMasksAt(int spaceId){
//        long mask = preCalcRefinedHorizontalAndVerticalMove(spaceId) | preCalcRefinedDiagAtkMask(spaceId);
//        HVD_TO_ATK_MASKS[spaceId] = new HashMap<>();
//        for (long occ = mask; ; occ = (occ - 1) & mask) {
//            if(HVD_TO_ATK_MASKS[spaceId].get(occ) != null){
//                System.out.println("IMPOSSIBLE");
//            }
//
//            HVD_TO_ATK_MASKS[spaceId].put(occ,
//                    preCalcHorizontalAndVerticalAtkMask(spaceId, occ)|preCalcDiagAtkMask(spaceId, occ) );
//            if (occ == 0) break;
//        }
//    }

//    public static void preCalcAllRaysMasks(){
//        for (int i = 0; i < ChessBoard.TOTAL_SPACES; ++i){
//            for (int j = 0; j < ChessBoard.TOTAL_SPACES; ++j){
//                D_RAY_MASK[i][j] = preCalcLRDownUpDiagRays(i, j);
//                HV_RAY_MASK[i][j] = preCalcHAndVRays(i, j);
//                HVD_RAY_MASK[i][j] = preCalcHVDRays(i, j);
//            }
//        }
//    }

//    public static long preCalcLRDownUpDiagRays(int from, int to){
//
//        long fromBitBoard = getSingleSpaceBitBoard(from);
//        long toBitBoard = getSingleSpaceBitBoard(to);
//        if (ChessBoard.getLRDownDiag(from) == ChessBoard.getLRDownDiag(to) ||
//            ChessBoard.getLRUpDiag(from) == ChessBoard.getLRUpDiag(to)){
//            long fromAtk = setBit(BitMasks.D_TO_ATK_MASKS[from].get(toBitBoard&BitMasks.BISHOP_MASKS[from]), to);
//
//            long toAtk = setBit(BitMasks.D_TO_ATK_MASKS[to].get(fromBitBoard&BitMasks.BISHOP_MASKS[to]), to);
//
//            return fromAtk & toAtk;
//        }else{
//            return 0L;
//        }
//    }

//    public static long preCalcHAndVRays(int from, int to){
//
//        long fromBitBoard = getSingleSpaceBitBoard(from);
//        long toBitBoard = getSingleSpaceBitBoard(to);
//        if (ChessBoard.getRow(from) == ChessBoard.getRow(to)||
//                ChessBoard.getCol(from) == ChessBoard.getCol(to)){
//            long fromAtk = setBit(rookAttacks(from, toBitBoard&BitMasks.ROOK_MASKS[from]), to);
//            long toAtk = setBit(rookAttacks(to, toBitBoard&BitMasks.ROOK_MASKS[to]), to);
//
//            return fromAtk & toAtk;
//        }else{
//            return 0L;
//        }
//    }

//    public static long preCalcHVDRays(int from, int to){
//        long hv = preCalcHAndVRays(from, to);
//        long d = preCalcLRDownUpDiagRays(from, to);
//
//        if (hv != 0) return hv;
//        return d;
//    }

    public static long rookAttacks(int square, long occupancy) {
        long index = ((occupancy & BitMasks.ROOK_MASKS[square]) * ROOK_MAGICS[square])
                >>> ROOK_SHIFTS[square];
        return HV_TO_ATK_MASKS[square][(int) index];
    }

    /** Bishop attacks from {@code square} given the full board occupancy. */
    public static long bishopAttacks(int square, long occupancy) {
        long index = ((occupancy & BitMasks.BISHOP_MASKS[square]) * BISHOP_MAGICS[square])
                >>> BISHOP_SHIFTS[square];
        return D_TO_ATK_MASKS[square][(int) index];
    }

    /** Queen attacks (rook | bishop) from {@code square}. */
    public static long queenAttacks(int square, long occupancy) {
        return rookAttacks(square, occupancy) | bishopAttacks(square, occupancy);
    }

    //endregion

    //region HELPE

    public static long getSingleSpaceBitBoard(int spaceId){
        return 1L << spaceId;
    }

    public static long getSingleSpaceBitBoardZero(int spaceId){
        return ~getSingleSpaceBitBoard(spaceId);
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
        System.out.println(getBitBoardString(bitBoard));
    }

    public static String getBitBoardString(long bitBoard){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ChessBoard.TOTAL_SPACES; ++i){
            sb.append((bitBoard & getSingleSpaceBitBoard(i)) != 0 ? "1 " : "0 ");
            if ((i+1) % ChessBoard.BOARD_SIZE == 0) sb.append("\n");
        }
        return sb.toString();
    }
    //endregion


    public static void findMagicsAtRuntime() {
        Random rng = new Random(0); // fixed seed → deterministic
        for (int sq = 0; sq < 64; sq++) {
            ROOK_MAGICS[sq]   = findMagic(sq, true,  rng);
            BISHOP_MAGICS[sq] = findMagic(sq, false, rng);
        }
        System.out.println("[MagicBitboards] Runtime magic search complete.");
        printFoundMagics(); // handy: copy these into the constants above!
    }

    /**
     * Finds a valid magic number for one square by random trial-and-error.
     * A sparse random number (few set bits) converges much faster.
     *
     * @param sq     square index 0-63
     * @param isRook true = rook, false = bishop
     * @param rng    random source
     * @return       a collision-free magic number
     */
    private static long findMagic(int sq, boolean isRook, Random rng) {
        long mask  = isRook ? BitMasks.ROOK_MASKS[sq] : BitMasks.BISHOP_MASKS[sq];
        int  bits  = Long.bitCount(mask);
        int  shift = 64 - bits;
        int  size  = 1 << bits;

        // Enumerate every possible occupancy subset of the mask (carry-rippler)
        long[] occupancies = new long[size];
        long[] attacks     = new long[size];
        int n = 0;
        for (long occ = mask; ; occ = (occ - 1) & mask) {
            occupancies[n] = occ;
            attacks[n]     = isRook
                    ? BitMasks.preCalcHorizontalAndVerticalAtkMask(sq, occ)
                    : BitMasks.preCalcDiagAtkMask(sq, occ);
            n++;
            if (occ == 0) break;
        }

        long[] table = new long[size];

        for (;;) { // retry until collision-free
            // Sparse random: AND three random longs → ~12 set bits on average
            long magic = rng.nextLong() & rng.nextLong() & rng.nextLong();
            if (magic == 0) continue;

            java.util.Arrays.fill(table, 0L);
            boolean fail = false;

            for (int i = 0; i < n && !fail; i++) {
                int idx = (int) ((occupancies[i] * magic) >>> shift);
                if (table[idx] == 0L) { //table empty at position, adding attacks calc there.
                    table[idx] = attacks[i];
                } else if (table[idx] != attacks[i]) {
                    fail = true; // collide->try new magic number.
                }
            }

            if (!fail) {
                // Store shift and fill the real attack table
                if (isRook) {
                    ROOK_SHIFTS[sq] = shift;
                    System.arraycopy(table, 0, HV_TO_ATK_MASKS[sq], 0, size);
                } else {
                    BISHOP_SHIFTS[sq] = shift;
                    System.arraycopy(table, 0, D_TO_ATK_MASKS[sq], 0, size);
                }
                return magic;
            }
        }
    }

    private static void fillTable(int sq, boolean isRook, long magic, int shift) {
        long mask = isRook ? BitMasks.ROOK_MASKS[sq] : BitMasks.BISHOP_MASKS[sq];
        long[][] table = isRook ? HV_TO_ATK_MASKS : D_TO_ATK_MASKS;

        for (long occ = mask; ; occ = (occ - 1) & mask) {
            int  idx = (int) ((occ * magic) >>> shift);
            long atk = isRook
                    ? BitMasks.preCalcHorizontalAndVerticalAtkMask(sq, occ)
                    : BitMasks.preCalcDiagAtkMask(sq, occ);
            table[sq][idx] = atk;
            if (occ == 0) break;
        }
    }

    public static void printFoundMagics() {
        System.out.println("// ---- Rook magics ----");
        printMagicArray(ROOK_MAGICS);
        System.out.println("// ---- Bishop magics ----");
        printMagicArray(BISHOP_MAGICS);
    }

    private static void printMagicArray(long[] arr) {
        StringBuilder sb = new StringBuilder("{ ");
        for (int i = 0; i < arr.length; i++) {
            sb.append("0x").append(Long.toHexString(arr[i])).append("L");
            if (i < arr.length - 1) sb.append(", ");
            if ((i + 1) % 4 == 0) sb.append("\n  ");
        }
        sb.append("}");
        System.out.println(sb);
    }
}
