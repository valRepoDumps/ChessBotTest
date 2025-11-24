package ChessResources.Pieces.PieceDatas;

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

    public PieceDatas()
    {
    }

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
            case PieceData.BPAWN -> PieceData.copyPiece(BPAWN_DATA);
            case PieceData.WPAWN -> PieceData.copyPiece(WPAWN_DATA);
            case PieceData.BROOK -> PieceData.copyPiece(BROOK_DATA);
            case PieceData.WROOK -> PieceData.copyPiece(WROOK_DATA);
            case PieceData.BKNIGHT -> PieceData.copyPiece(BKNIGHT_DATA);
            case PieceData.WKNIGHT -> PieceData.copyPiece(WKNIGHT_DATA);
            case PieceData.BBISHOP -> PieceData.copyPiece(BBISHOP_DATA);
            case PieceData.WBISHOP -> PieceData.copyPiece(WBISHOP_DATA);
            case PieceData.BQUEEN -> PieceData.copyPiece(BQUEEN_DATA);
            case PieceData.WQUEEN -> PieceData.copyPiece(WQUEEN_DATA);
            case PieceData.BKING -> PieceData.copyPiece(BKING_DATA);
            case PieceData.WKING -> PieceData.copyPiece(WKING_DATA);
            default -> NO_PIECE;
        };
    }
}