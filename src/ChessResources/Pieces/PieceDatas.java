package ChessResources.Pieces;

public class PieceDatas
{
    //region PIECES_CONSTS
    public static final PieceData BPAWN_DATA = new SlidingPieceData(PieceData.BPAWN);
    public static final PieceData WPAWN_DATA = new SlidingPieceData(PieceData.WPAWN);
    public static final PieceData BROOK_DATA = new SlidingPieceData(PieceData.BROOK);
    public static final PieceData WROOK_DATA = new SlidingPieceData(PieceData.WROOK);
    public static final PieceData BBISHOP_DATA = new SlidingPieceData(PieceData.BBISHOP);
    public static final PieceData WBISHOP_DATA = new SlidingPieceData(PieceData.WBISHOP);
    public static final PieceData BQUEEN_DATA = new SlidingPieceData(PieceData.BQUEEN);
    public static final PieceData WQUEEN_DATA = new SlidingPieceData(PieceData.WQUEEN);

    public static final PieceData BKING_DATA = new IrregularPieceData(PieceData.BKING);
    public static final PieceData WKING_DATA = new IrregularPieceData(PieceData.WKING);

    public static final PieceData BKNIGHT_DATA = new IrregularPieceData(PieceData.BKNIGHT);
    public static final PieceData WKNIGHT_DATA = new IrregularPieceData(PieceData.WKNIGHT);

    public static final PieceData NO_PIECE = null;
    public final static int PIECES_DIFF = 16;
    //endregion

    public static final int TOTAL_PIECES = 12;
    public static PieceData makePiece(short pieceId)
    {
        return switch (pieceId) {
            case PieceData.BPAWN, PieceData.WPAWN, PieceData.BROOK, PieceData.WROOK,
                 PieceData.BBISHOP, PieceData.WBISHOP, PieceData.BQUEEN, PieceData.WQUEEN,
                 PieceData.BKING, PieceData.WKING -> new SlidingPieceData(pieceId);
            case PieceData.BKNIGHT, PieceData.WKNIGHT -> new IrregularPieceData(pieceId);
            default -> null;
        };
    }

    public static PieceData getCopyOfPiece(int pieceId)
    {
        return switch (pieceId) {
            case PieceData.BPAWN -> PieceDatas.copyPiece(BPAWN_DATA);
            case PieceData.WPAWN -> PieceDatas.copyPiece(WPAWN_DATA);
            case PieceData.BROOK -> PieceDatas.copyPiece(BROOK_DATA);
            case PieceData.WROOK -> PieceDatas.copyPiece(WROOK_DATA);
            case PieceData.BKNIGHT -> PieceDatas.copyPiece(BKNIGHT_DATA);
            case PieceData.WKNIGHT -> PieceDatas.copyPiece(WKNIGHT_DATA);
            case PieceData.BBISHOP -> PieceDatas.copyPiece(BBISHOP_DATA);
            case PieceData.WBISHOP -> PieceDatas.copyPiece(WBISHOP_DATA);
            case PieceData.BQUEEN -> PieceDatas.copyPiece(BQUEEN_DATA);
            case PieceData.WQUEEN -> PieceDatas.copyPiece(WQUEEN_DATA);
            case PieceData.BKING -> PieceDatas.copyPiece(BKING_DATA);
            case PieceData.WKING -> PieceDatas.copyPiece(WKING_DATA);
            default -> NO_PIECE;
        };
    }

    public static int convertPieceIdToArrayIdx(int pieceId){
        if ((pieceId & 0b1000) == 0){
            return ((pieceId & 0b111)-1);
        }
        else{
            return ((TOTAL_PIECES/2 + pieceId&0b111)-1);
        }
    }
    public static boolean getColor(PieceData piece)
    {
        return (piece.pieceId & 16) == 0 ? PieceData.BLACK : PieceData.WHITE;
    }

    public static PieceData copyPiece(PieceData pieceData)
    {
        return switch (pieceData) {
            case null -> PieceDatas.NO_PIECE;
            case SlidingPieceData slidingPieceData -> new SlidingPieceData(slidingPieceData);
            case IrregularPieceData irregularPieceData -> new IrregularPieceData(irregularPieceData);
            default -> new PieceData(pieceData);
        };
    }

}