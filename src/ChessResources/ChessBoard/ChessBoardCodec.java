package ChessResources.ChessBoard;

import ChessResources.Pieces.PieceData;
import ChessResources.GameSaver.JsonUtil;
import com.google.gson.JsonObject;

//DOESTN SAVE ANY LISTENERS!!!!!!.
/** Lives in the ChessBoard package so it can use the protected boardSquares / setPieceAt / clearBoard. */
public final class ChessBoardCodec {
    private ChessBoardCodec() {}

    public static JsonObject write(ChessBoard b) {
        JsonObject o = new JsonObject();
        o.addProperty("pieceTypes", PieceData.TOTAL_PIECES);
        o.add("bitboards", JsonUtil.hexArray(b.boardSquares)); // array-index order of PieceData
        // currPieceAtLocation and all the generateBitMasks() fields are derived: not saved.

        return o;
    }

    /** reg may be null (history snapshots carry no external listeners). */
    public static ChessBoard read(JsonObject o) {
        //basically just repeats
        long[] bbs = JsonUtil.unhexArray(o.getAsJsonArray("bitboards"));
        if (bbs.length != PieceData.TOTAL_PIECES)
            throw new IllegalStateException("Save has " + bbs.length + " piece boards, code expects " + PieceData.TOTAL_PIECES);

        ChessBoard b = new ChessBoard();   // constructor re-wires BOARD_STATE_CHANGE_LOGGER

        b.clearBoard();
        for (int idx = 0; idx < bbs.length; idx++) {
            short piece = (short) PieceData.convertArrayIdxToPieceId((short) idx);
            for (long bits = bbs[idx]; bits != 0; bits &= bits - 1) {
                b.setPieceAt(Long.numberOfTrailingZeros(bits), piece, PieceData.INVALID_PIECES);
            }
        }
        return b;
    }
}