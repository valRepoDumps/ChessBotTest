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

public class IrregularPieceData extends PieceData{
    public static final int QUEEN_SIDE_CASTLE = 8;
    public static final int KING_SIDE_CASTLE = 9;

    //region KNIGHT_FUNCS
    public static final PieceFunc KNIGHT_MOVES_FUNC =
            (game,  spaceId,  spaces) ->{
                ScanResult[] sqrs = BoardScan.jumpScan(game, getPreGenerateKnightMoves(spaceId), spaces);
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
                int[] moves = getPreGenerateKingMoves(spaceId);
                for (int i = 0; i < moves.length; ++i){
                    if (!ChessBoard.isValidSpaceId(moves[i])){
                        continue;
                    }

                    if ((i == QUEEN_SIDE_CASTLE)) {
                        if (game.gameProperties[MinimalChessGame.WHITE_CASTLE_QUEEN]){
                            //meaning neither rook nor king has moved
                            ChessSpaces tmp = new ChessSpaces();

                            ScanResult sr = BoardScan.rayScan(game,
                                    PreCalc.PRECOMPUTED_MOVES[spaceId][ChessBoard.WEST],
                                    SlidingPieceData.NO_RANGE_LIMIT, tmp);
                            if (ScanResult.isValid(sr) && sr.getPieceId() == PieceData.WROOK){
                                boolean flag = true;
                                for (int sq : tmp.getChessMoves()){
                                    if (!game.spaceNotUnderThreatAndEmpty(sq)) flag = false;
                                }
                                if (flag)
                                    spaces.addMoves(moves[i]);}
                            }
                    }
                    else if ((i == KING_SIDE_CASTLE)) {
                        if (game.gameProperties[MinimalChessGame.WHITE_CASTLE_KING]){
                            //meaning neither rook nor king has moved
                            ChessSpaces tmp = new ChessSpaces();

                            ScanResult sr = BoardScan.rayScan(game,
                                    PreCalc.PRECOMPUTED_MOVES[spaceId][ChessBoard.EAST],
                                    SlidingPieceData.NO_RANGE_LIMIT, tmp);

                            if (ScanResult.isValid(sr) && sr.getPieceId() == PieceData.WROOK){
                                boolean flag = true;
                                for (int sq : tmp.getChessMoves()){
                                    if (!game.spaceNotUnderThreatAndEmpty(sq)) flag = false;
                                }
                                if (flag)
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
                    if ((i == QUEEN_SIDE_CASTLE)) {
                        if (game.gameProperties[MinimalChessGame.BLACK_CASTLE_QUEEN]){
                            //meaning neither rook nor king has moved
                            ChessSpaces tmp = new ChessSpaces();

                            ScanResult sr = BoardScan.rayScan(game,
                                    PreCalc.PRECOMPUTED_MOVES[spaceId][ChessBoard.WEST],
                                    SlidingPieceData.NO_RANGE_LIMIT, tmp);
                            if (ScanResult.isValid(sr) && sr.getPieceId() == PieceData.BROOK){
                                boolean flag = true;
                                for (int sq : tmp.getChessMoves()){
                                    if (!game.spaceNotUnderThreatAndEmpty(sq)) flag = false;
                                }
                                if (flag)
                                    spaces.addMoves(moves[i]);}
                        }
                    }
                    else if ((i == KING_SIDE_CASTLE)) {
                        if (game.gameProperties[MinimalChessGame.BLACK_CASTLE_KING]){
                            //meaning neither rook nor king has moved
                            ChessSpaces tmp = new ChessSpaces();

                            ScanResult sr = BoardScan.rayScan(game,
                                    PreCalc.PRECOMPUTED_MOVES[spaceId][ChessBoard.EAST],
                                    SlidingPieceData.NO_RANGE_LIMIT, tmp);

                            if (ScanResult.isValid(sr) && sr.getPieceId() == PieceData.BROOK){
                                boolean flag = true;
                                for (int sq : tmp.getChessMoves()){
                                    if (!game.spaceNotUnderThreatAndEmpty(sq)) flag = false;
                                }
                                if (flag)
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

    public static int POSSIBLE_KNIGHT_MOVES = 8;
    public static int POSSIBLE_KING_MOVES = 6;

    public static int[][] PRECALC_KNIGHT_MOVES = new int[ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE][];

    public static int[][] PRECALC_KING_MOVES = new int[ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE][];

    public PieceFunc movesFunc;

    static {
        preGenerateIrregularPieceMoves();
    }

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

    private static int[] getPreGenerateKnightMoves(int spaceId){
        return PRECALC_KNIGHT_MOVES[spaceId];
    }

    private static int[] getPreGenerateKingMoves(int spaceId){
        return PRECALC_KING_MOVES[spaceId];
    }

    private static void assignPreGenerateKnightMoves(int spaceId, int[] moves){
        PRECALC_KNIGHT_MOVES[spaceId] = moves;
    }

    private static void assignPreGenerateKingMoves(int spaceId, int[] moves){
        PRECALC_KING_MOVES[spaceId] = moves;
    }

    private static void preGenerateKnightMoves(int spaceId){

        int[] possibleSquares = new int[] {ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID};

        int row = ChessBoard.getRow(spaceId);
        int col = ChessBoard.getCol(spaceId);

        if (row-2 >= 0 && col-1 >= 0) { //assumes input is already valid.
            possibleSquares[0] = spaceId +
                    2*ChessBoard.directionOffsets[ChessBoard.NORTH] + //upleft
                    ChessBoard.directionOffsets[ChessBoard.WEST];
        }

        if (row-2>=0 && col+1 < ChessBoard.BOARD_SIZE){
            possibleSquares[1] = spaceId +
                    2*ChessBoard.directionOffsets[ChessBoard.NORTH] + //upright
                    ChessBoard.directionOffsets[ChessBoard.EAST];
        }

        if (row+2< ChessBoard.BOARD_SIZE && col-1 >= 0) {
            possibleSquares[2] = spaceId + 2 * ChessBoard.directionOffsets[ChessBoard.SOUTH] + //downleft
                    ChessBoard.directionOffsets[ChessBoard.WEST];
        }

        if (row+2< ChessBoard.BOARD_SIZE && col+1 < ChessBoard.BOARD_SIZE){
            possibleSquares[3] = spaceId + 2* ChessBoard.directionOffsets[ChessBoard.SOUTH] + //downright
                    ChessBoard.directionOffsets[ChessBoard.EAST];
        }

        if (col-2 >= 0 && row -1 >= 0){
            possibleSquares[4] = spaceId + 2* ChessBoard.directionOffsets[ChessBoard.WEST] + //leftup
                    ChessBoard.directionOffsets[ChessBoard.NORTH];}

        if (col-2 >= 0 && row +1 < ChessBoard.BOARD_SIZE){
            possibleSquares[5] = spaceId +
                    2*ChessBoard.directionOffsets[ChessBoard.WEST] + //leftdown
                    ChessBoard.directionOffsets[ChessBoard.SOUTH];
        }

        if (col+2 < ChessBoard.BOARD_SIZE && row-1 >= 0){
            possibleSquares[6] = spaceId +
                    2*ChessBoard.directionOffsets[ChessBoard.EAST] + //rightup
                    ChessBoard.directionOffsets[ChessBoard.NORTH];
        }
        if (col+2 < ChessBoard.BOARD_SIZE && row + 1 < ChessBoard.BOARD_SIZE) {
            possibleSquares[7] = spaceId +
                    2*ChessBoard.directionOffsets[ChessBoard.EAST] + //rightdown
                    ChessBoard.directionOffsets[ChessBoard.SOUTH];
        }

        assignPreGenerateKnightMoves(spaceId, possibleSquares);
    }

    private static void preGenerateKingMoves(int spaceId){

        int[] possibleSquares = new int[] {ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID};

        int row = ChessBoard.getRow(spaceId);
        int col = ChessBoard.getCol(spaceId);

        if (row < ChessBoard.BOARD_SIZE-1){
            possibleSquares[0] = spaceId + ChessBoard.directionOffsets[ChessBoard.SOUTH];
            if (col < ChessBoard.BOARD_SIZE-1){
                possibleSquares[1] = spaceId + ChessBoard.directionOffsets[ChessBoard.SOUTH_EAST];
            }
            if (col > 0){
                possibleSquares[2] = spaceId + ChessBoard.directionOffsets[ChessBoard.SOUTH_WEST];
            }
        }

        if (row > 0){
            possibleSquares[3] = spaceId + ChessBoard.directionOffsets[ChessBoard.NORTH];
            if (col < ChessBoard.BOARD_SIZE-1){
                possibleSquares[4] = spaceId + ChessBoard.directionOffsets[ChessBoard.NORTH_EAST];
            }
            if (col > 0){
                possibleSquares[5] = spaceId + ChessBoard.directionOffsets[ChessBoard.NORTH_WEST];
            }
        }

        if (col > 0){
            possibleSquares[6] = spaceId + ChessBoard.directionOffsets[ChessBoard.WEST];
        }

        if (col < ChessBoard.BOARD_SIZE-1){
            possibleSquares[7] = spaceId + ChessBoard.directionOffsets[ChessBoard.EAST];
        }

        //
        if (row == 0 || row == ChessBoard.BOARD_SIZE - 1){
            if (col > 3){
                possibleSquares[QUEEN_SIDE_CASTLE] = spaceId + 2*ChessBoard.directionOffsets[ChessBoard.WEST];
            }
            if (col < 5){
                possibleSquares[KING_SIDE_CASTLE] = spaceId + 2*ChessBoard.directionOffsets[ChessBoard.EAST];
            }
        }

        assignPreGenerateKingMoves(spaceId, possibleSquares);
    }

    private static void preGenerateIrregularPieceMoves(){
        for (int i = 0; i < ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE; ++i){
            preGenerateKnightMoves(i);
            preGenerateKingMoves(i);
        }
    }
}
