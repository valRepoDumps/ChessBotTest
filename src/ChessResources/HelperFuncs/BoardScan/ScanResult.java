package ChessResources.HelperFuncs.BoardScan;

import ChessResources.ChessBoard.ChessBoard;
import ChessResources.Pieces.PieceData;

public class ScanResult {
    protected short pieceId;
    protected int spaceId;

    public static final ScanResult OUT_OF_RANGE_SCAN_RESULT =
            new ScanResult((short) (PieceData.INVALID_PIECES+1), ChessBoard.INVALID_SPACE_ID);

    public static final ScanResult INVALID_SCAN_RESULT =
            new ScanResult(PieceData.INVALID_PIECES, ChessBoard.INVALID_SPACE_ID-1);

    public ScanResult(short pieceId, int spaceId){
        this.pieceId = pieceId;
        this.spaceId = spaceId;
    }

    public int getSpaceId(){return spaceId;}

    public int getPieceId() {
        return pieceId;
    }

    @Override
    public boolean equals(Object sc){
        if (!(sc instanceof ScanResult)) return false;

        return pieceId == ((ScanResult)sc).pieceId
                && spaceId == ((ScanResult)sc).spaceId;
    }

    public boolean isValid(){
        return !this.equals(INVALID_SCAN_RESULT) && !this.equals(OUT_OF_RANGE_SCAN_RESULT);
    }

    public static boolean isValid(ScanResult sc){
        return (sc != null) && sc.isValid();
    }

    @Override
    public String toString(){
        return getPieceId() + "(" + getSpaceId() + ")";
    }
}
