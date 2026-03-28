package ChessResources;

import ChessResources.Pieces.PieceConsts;
import ChessResources.Pieces.PieceData;

import java.util.Arrays;

public class PreCalc {

    public static final short[] FEN_MAP = new short[128];
    public static final short[] WHITE_PIECES = { PieceData.WPAWN, PieceData.WKNIGHT, PieceData.WBISHOP,
            PieceData.WROOK, PieceData.WQUEEN, PieceData.WKING };
    public static final short[] BLACK_PIECES = { PieceData.BPAWN, PieceData.BKNIGHT, PieceData.BBISHOP,
            PieceData.BROOK, PieceData.BQUEEN, PieceData.BKING };

    public static final short[] ALL_PIECES = { PieceData.BPAWN, PieceData.BKNIGHT, PieceData.BBISHOP,
            PieceData.BROOK, PieceData.BQUEEN, PieceData.BKING, PieceData.WPAWN, PieceData.WKNIGHT, PieceData.WBISHOP,
            PieceData.WROOK, PieceData.WQUEEN, PieceData.WKING};

    public static final short[] WHITE_THREAT_IDS = BLACK_PIECES;
    public static final short[] BLACK_THREAT_IDS = WHITE_PIECES;

    public static final PieceData[] PIECE_ID_TO_PIECE_DATA_MAP = new PieceData[PieceData.MAX_PIECES];

    static {
        assignFenMap();
        assignPieceIdToPieceDataMap();
    }

    //region FEN_MAP
    public static void assignFenMap(){
        FEN_MAP['k'] = PieceData.BKING;
        FEN_MAP['q'] = PieceData.BQUEEN;
        FEN_MAP['b'] = PieceData.BBISHOP;
        FEN_MAP['n'] = PieceData.BKNIGHT;
        FEN_MAP['r'] = PieceData.BROOK;
        FEN_MAP['p'] = PieceData.BPAWN;

        FEN_MAP['K'] = PieceData.WKING;
        FEN_MAP['Q'] = PieceData.WQUEEN;
        FEN_MAP['B'] = PieceData.WBISHOP;
        FEN_MAP['N'] = PieceData.WKNIGHT;
        FEN_MAP['R'] = PieceData.WROOK;
        FEN_MAP['P'] = PieceData.WPAWN;
    }
    //endregion

    //region PIECE_ID_TO_PIECE_DATA_MAP
    public static void assignPieceIdToPieceDataMap(){
        Arrays.fill(PIECE_ID_TO_PIECE_DATA_MAP, PieceConsts.NO_PIECE);

        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.BPAWN] = PieceData.BPAWN_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.WPAWN] = PieceData.WPAWN_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.BROOK] = PieceData.BROOK_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.WROOK] = PieceData.WROOK_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.BKNIGHT] = PieceData.BKNIGHT_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.WKNIGHT] = PieceData.WKNIGHT_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.BBISHOP] = PieceData.BBISHOP_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.WBISHOP] = PieceData.WBISHOP_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.BQUEEN] = PieceData.BQUEEN_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.WQUEEN] = PieceData.WQUEEN_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.BKING] = PieceData.BKING_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.WKING] = PieceData.WKING_DATA;
    }
    //endregion
}
