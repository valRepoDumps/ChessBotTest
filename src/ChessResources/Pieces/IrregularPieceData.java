package ChessResources.Pieces;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessSpaces;
import ChessResources.HelperFuncs.BoardScan.BoardScan;
import ChessResources.HelperFuncs.BoardScan.ScanResult;
import ChessResources.HelperFuncs.PieceFunc;
import ChessResources.PreCalc;

import javax.swing.*;
import java.util.UUID;

import static ChessResources.PreCalc.getPreGenerateKingMoves;

public class IrregularPieceData extends PieceData{

    //region KNIGHT_FUNCS
    public static final PieceFunc KNIGHT_MOVES_FUNC =
            (game,  spaceId,  spaces) ->{
                ScanResult[] sqrs = BoardScan.jumpScan(game, PreCalc.getPreGenerateKnightMoves(spaceId), spaces);
                for (ScanResult sq : sqrs){
                    if (ScanResult.isValid(sq) && game.isEnemyPieceAt(sq.getSpaceId())){
                        spaces.addMoves(sq.getSpaceId());
                    }
                }
            };

    //endregion

    //region KING FUNCS
    public static PieceFunc WKING_MOVES_FUNC =
            (game, spaceId, spaces) ->{
                int[] moves = PreCalc.getPreGenerateKingMoves(spaceId);
                for (int i = 0; i < moves.length; ++i){
                    if (!ChessBoard.isValidSpaceId(moves[i])){
                        continue;
                    }

                    if ((i == PreCalc.QUEEN_SIDE_CASTLE)) {
                        if (game.gameProperties[MinimalChessGame.WHITE_CASTLE_QUEEN]){
                            //meaning neither rook nor king has moved
                            ChessSpaces tmp = new ChessSpaces();

                            ScanResult sr = BoardScan.rayScan(game,
                                    PreCalc.SLIDING_MOVES[spaceId][ChessBoard.WEST],
                                    SlidingPieceData.NO_RANGE_LIMIT, tmp);
                            if (ScanResult.isValid(sr) && sr.getPieceId() == PieceData.WROOK){
                                for (int sq : tmp.getChessMoves()){
                                    if (!game.spaceNotUnderThreatAndEmpty(sq)) return;
                                }
                                spaces.addMoves(moves[i]);}
                            }
                    }
                    else if ((i == PreCalc.KING_SIDE_CASTLE)) {
                        if (game.gameProperties[MinimalChessGame.WHITE_CASTLE_KING]){
                            //meaning neither rook nor king has moved
                            ChessSpaces tmp = new ChessSpaces();

                            ScanResult sr = BoardScan.rayScan(game,
                                    PreCalc.SLIDING_MOVES[spaceId][ChessBoard.EAST],
                                    SlidingPieceData.NO_RANGE_LIMIT, tmp);

                            if (ScanResult.isValid(sr) && sr.getPieceId() == PieceData.WROOK){
                                for (int sq : tmp.getChessMoves()){
                                    if (!game.spaceNotUnderThreatAndEmpty(sq)) return;
                                }
                                spaces.addMoves(moves[i]);}
                        }
                    }
                    else if (game.spaceNotUnderThreatAndEmpty(moves[i])){
                        spaces.addMoves(moves[i]);
                    }
                    else if (game.isEnemyPieceAt(moves[i]) && game.spaceNotUnderThreat(moves[i])){

                        spaces.addMoves(moves[i]);
                    }
                }
            };

    public static PieceFunc BKING_MOVES_FUNC =
            (game, spaceId, spaces) ->{
                int[] moves = getPreGenerateKingMoves(spaceId);
                for (int i = 0; i < moves.length; ++i){
                    if (!ChessBoard.isValidSpaceId(moves[i])){
                        continue;
                    }
                    if ((i == PreCalc.QUEEN_SIDE_CASTLE)) {
                        if (game.gameProperties[MinimalChessGame.BLACK_CASTLE_QUEEN]){
                            //meaning neither rook nor king has moved
                            ChessSpaces tmp = new ChessSpaces();

                            ScanResult sr = BoardScan.rayScan(game,
                                    PreCalc.SLIDING_MOVES[spaceId][ChessBoard.WEST],
                                    SlidingPieceData.NO_RANGE_LIMIT, tmp);
                            if (ScanResult.isValid(sr) && sr.getPieceId() == PieceData.BROOK){
                                for (int sq : tmp.getChessMoves()){
                                    if (!game.spaceNotUnderThreatAndEmpty(sq)) return;
                                }
                                spaces.addMoves(moves[i]);}
                        }
                    }
                    else if ((i == PreCalc.KING_SIDE_CASTLE)) {
                        if (game.gameProperties[MinimalChessGame.BLACK_CASTLE_KING]){
                            //meaning neither rook nor king has moved
                            ChessSpaces tmp = new ChessSpaces();

                            ScanResult sr = BoardScan.rayScan(game,
                                    PreCalc.SLIDING_MOVES[spaceId][ChessBoard.EAST],
                                    SlidingPieceData.NO_RANGE_LIMIT, tmp);

                            if (ScanResult.isValid(sr) && sr.getPieceId() == PieceData.BROOK){
                                for (int sq : tmp.getChessMoves()){
                                    if (!game.spaceNotUnderThreatAndEmpty(sq)) return;
                                }
                                spaces.addMoves(moves[i]);}
                        }
                    }
                    else if (game.spaceNotUnderThreatAndEmpty(moves[i])){
                        spaces.addMoves(moves[i]);
                    }
                    else if (game.isEnemyPieceAt(moves[i]) && game.spaceNotUnderThreat(moves[i])){

                        spaces.addMoves(moves[i]);
                    }
                }
            };


    //endregion

    public PieceFunc movesFunc;

    @SuppressWarnings("unused")
    public IrregularPieceData(short pieceId, boolean color, int value, String name, ImageIcon graphic,
                              PieceFunc movesFunc) {
        super(pieceId, color, value, name, graphic);
        this.movesFunc = movesFunc;
    }
    public IrregularPieceData(UUID uuid, short pieceId, boolean color, int value, String name, ImageIcon graphic,
                              PieceFunc movesFunc) {
        super(uuid, pieceId, color, value, name, graphic);
        this.movesFunc = movesFunc;
    }

    public IrregularPieceData(short pieceId)
    {
        super(pieceId);
        switch (pieceId)
        {
            case BKNIGHT: case WKNIGHT:
                this.movesFunc = KNIGHT_MOVES_FUNC; break;
            case BKING: this.movesFunc = BKING_MOVES_FUNC; break;
            case WKING: this.movesFunc = WKING_MOVES_FUNC; break;
        }
    }

    public IrregularPieceData(IrregularPieceData piece)
    {
        super(piece);
        this.movesFunc = piece.movesFunc;
    }
    public void getPossibleMoves(MinimalChessGame<?> game, int spaceId, ChessSpaces spaces)
    {
        movesFunc.apply(game, spaceId, spaces);
    }

    @Override
    public IrregularPieceData clone(){
        return new IrregularPieceData(uuid, pieceId, color, value, name, graphic, movesFunc);
    }

    @Override
    public IrregularPieceData getUniqueClone(){
        return new IrregularPieceData(pieceId, color, value, name, graphic, movesFunc);
    }

}
