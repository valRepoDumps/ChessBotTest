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
    //endregion

    public static final int TOTAL_PIECES = 12;

    public static PieceData makePiece(int pieceId)
    {
        return switch (pieceId) {
            case PieceData.BPAWN -> PieceDatas.getUniqueClone(BPAWN_DATA);
            case PieceData.WPAWN -> PieceDatas.getUniqueClone(WPAWN_DATA);
            case PieceData.BROOK -> PieceDatas.getUniqueClone(BROOK_DATA);
            case PieceData.WROOK -> PieceDatas.getUniqueClone(WROOK_DATA);
            case PieceData.BKNIGHT -> PieceDatas.getUniqueClone(BKNIGHT_DATA);
            case PieceData.WKNIGHT -> PieceDatas.getUniqueClone(WKNIGHT_DATA);
            case PieceData.BBISHOP -> PieceDatas.getUniqueClone(BBISHOP_DATA);
            case PieceData.WBISHOP -> PieceDatas.getUniqueClone(WBISHOP_DATA);
            case PieceData.BQUEEN -> PieceDatas.getUniqueClone(BQUEEN_DATA);
            case PieceData.WQUEEN -> PieceDatas.getUniqueClone(WQUEEN_DATA);
            case PieceData.BKING -> PieceDatas.getUniqueClone(BKING_DATA);
            case PieceData.WKING -> PieceDatas.getUniqueClone(WKING_DATA);
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
        return getColor(piece.getPieceId());
    }

    public static boolean getColor(int pieceId)
    {
        return (pieceId & 16) == 0 ? PieceData.BLACK : PieceData.WHITE;
    }

    public static PieceData getUniqueClone(PieceData pieceData)
    {
        if (pieceData == null) return null;
        return pieceData.getUniqueClone();
    }

    public static PieceData getClone(PieceData pieceData){
        if (pieceData == null) return null;
        return  pieceData.clone();
    }

}