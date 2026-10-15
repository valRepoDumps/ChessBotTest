package ChessResources.GetMovesLogic;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.ChessBoard.DrawBoard;
import ChessResources.Pieces.PieceData;

import java.util.*;

public class PossibleMoves {
    //region PRE_CONSTRUCTOR
    //region DATAS
    public static int MAX_POSSIBLE_MOVES =  2*ChessBoard.TOTAL_SPACES;
    private final ChessMove[] possibleMoves = new ChessMove[MAX_POSSIBLE_MOVES];
    public int currLen = 0;
    protected MinimalChessGame chessGame;
    //endregion
    //endregion

    public PossibleMoves(MinimalChessGame chessGame)
    {
        this.chessGame = chessGame;
    }

    public PossibleMoves(PossibleMoves pm){
        this(pm.chessGame);
        copyPossibleMoves(pm.possibleMoves, pm.currLen);
        this.currLen = pm.currLen;
    }

    //region HELPERS

    public void sortMoves(Comparator<ChessMove> cmp){
        Arrays.sort(possibleMoves, 0, currLen, cmp);
    }

    public void copyPossibleMoves(ChessMove[] copyFrom, int size){
        if (size >= 0) System.arraycopy(copyFrom, 0, this.possibleMoves, 0, size);
    }

    public void addMoves(ChessMove moves){
        possibleMoves[currLen] = moves;
        ++currLen;
    }

    public void clearPossibleMoves()
    {
        currLen = 0;
    }

    public PossibleMoves getClone(){
        return new PossibleMoves(this);
    }

    public void setPossibleMoves(PossibleMoves pm){
        this.chessGame = pm.chessGame;
        copyPossibleMoves(pm.possibleMoves, pm.currLen);
        this.currLen = pm.currLen;
    }

    public ArrayList<ChessMove> canMoveToFrom(int spaceId){
        ArrayList<ChessMove> ans = new ArrayList<>();
        for (int i = 0; i < currLen; ++i){
            ChessMove move = possibleMoves[i];
            if (move.spaceIdToMove == spaceId){
                ans.add(move);
            }
        }
        return ans;
    }

    public boolean isEmpty(){
        return currLen == 0;
    }

    @SuppressWarnings("unused")
    public ChessMove[] getMoves(){return possibleMoves;}

    public boolean canMoveToFrom(int spaceId, int spaceIdArriveAt) {
        return getMove(spaceId, spaceIdArriveAt) != null;
    }

    public ChessMove getMove(int spaceId, int spaceIdArriveAt, short promotedPieceType){
        for (int i = 0; i < currLen; ++i){
            ChessMove move = possibleMoves[i];
            if (move.spaceIdToMove == spaceId && move.spaceIdArriveAt == spaceIdArriveAt){
                if (!move.isPromotion() || PieceData.getType(promotedPieceType) == PieceData.INVALID_PIECES) {
                    return move;
                }
                else{
                    if (PieceData.getType(move.getPromotionPieceId()) == PieceData.getType(promotedPieceType)){
                        return move;
                    }
                    else{
                        continue;
                    }
                }
            }
        }
        return null;
    }

    public ChessMove getMove(int spaceId, int spaceIdArriveAt){
        return getMove(spaceId, spaceIdArriveAt, PieceData.INVALID_PIECES);
    }

    public ChessMove getMovePromotion(int spaceId, int spaceIdArriveAt, short pieceIdPromotion){
        for (int i = 0; i < currLen; ++i){
            ChessMove move = possibleMoves[i];
            if (move.spaceIdToMove == spaceId
                    && move.spaceIdArriveAt == spaceIdArriveAt
                    && move.getPromotionPieceId() == pieceIdPromotion){
                return move;
            }
        }
        return null;
    }
    //endregion

    @Override
    public String toString() {
//        if (possibleMoves == null) {
//            return "possibleMoves: <empty>";
//        }
        StringBuilder sb = new StringBuilder("possibleMoves:\n");
        for (int i = 0; i < currLen ; ++i) {

            sb.append("  ")
                    .append(i)
                    .append(" -> ")
                    .append(possibleMoves[i])
                    .append('\n');
        }
        return sb.toString();
    }
}
