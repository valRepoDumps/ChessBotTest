package ChessResources.HelperFuncs.BoardScan;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessSpaces;
import ChessResources.Pieces.PieceData;

public class BoardScan {
    public static ScanResult rayScan(MinimalChessGame<?extends ChessBoard> game,
                                              int[] targets, int range, ChessSpaces out) {
        for (int i = 0; i < range; ++i){
            int sq = targets[i];

            if (!ChessBoard.isValidSpaceId(sq))
                return ScanResult.INVALID_SCAN_RESULT;

            if (game.chessBoard.isEmptySpaceAt(sq)){
                if (out != null) out.addMoves(sq);
            }
            else return new ScanResult(game.getBoard().getPieceIdAt(sq), sq);
        }
        return ScanResult.OUT_OF_RANGE_SCAN_RESULT;
    }

    public static void rayScanMultiDirectional(MinimalChessGame<?extends ChessBoard> game,
                                               int[][] targets, int range, short[] dirs, ChessSpaces out,
                                               boolean addEnemies){
        for (short dir : dirs){
            ScanResult sr = rayScan(game, targets[dir], range, out);
            if (addEnemies && ScanResult.isValid(sr) && game.isEnemyPieceAt(sr.getSpaceId())){
                out.addMoves(sr.getSpaceId());
            }
        }
    }

    public static ScanResult[] jumpScan(MinimalChessGame<?extends ChessBoard> game,
                                  int[] targets, ChessSpaces out){
        ScanResult[] ans = new ScanResult[targets.length];
        int counter = 0;

        for (int moves: targets){

            if (!ChessBoard.isValidSpaceId(moves))
                continue;
            else if (game.chessBoard.isEmptySpaceAt(moves)){
                if (out != null)
                    out.addMoves(moves);
            }
            else {
                ans[counter] =  new ScanResult(game.getBoard().getPieceIdAt(moves), moves);
                counter++;
            }
        }
        return ans;
    }

    public static boolean rayScanFor(MinimalChessGame<?extends ChessBoard> game,
                                 int[] targets, int range, short pieceId){
        return rayScan(game, targets, range, null).getPieceId() == pieceId;
    }

    public static boolean jumpScanFor(MinimalChessGame<?extends ChessBoard> game,
                                 int[] targets, short pieceId){
        return returnArrayContainsPieceId(jumpScan(game, targets, null), pieceId);
    }

    public static boolean rayScanFor(MinimalChessGame<?extends ChessBoard> game,
                                     int[][] targets, int range, short[] dirs, short[] pieceIds){
        ScanResult sr;
        for (short dir : dirs){
            sr = rayScan(game, targets[dir], range, null);
            if (!ScanResult.isValid(sr)) continue;

            for (short pieceId : pieceIds){
                if (pieceId == sr.getPieceId()) return true;
            }
        }
        return false;
    }

    public static boolean rayScanFor(MinimalChessGame<?extends ChessBoard> game,
                                     int[][] targets, int range, short[] dirs, short pieceId){
        for (short dir : dirs){
            ScanResult sr = rayScan(game, targets[dir], range, null);
            if (!ScanResult.isValid(sr)) continue;

            if (pieceId == sr.getPieceId()) return true;
        }
        return false;
    }

    public static boolean jumpScanFor(MinimalChessGame<?extends ChessBoard> game,
                                      int[] targets, int[] pieceIds){
        return returnArrayContainsPieceIds(jumpScan(game, targets, null), pieceIds);
    }

    private static boolean returnArrayContainsPieceId(ScanResult[] arr, int target){
        for (ScanResult data : arr){
            if (ScanResult.isValid(data) && data.getPieceId() == target) return true;
            else break;
        }
        return false;
    }

    private static boolean returnArrayContainsPieceIds(ScanResult[] arr, int[] targets){
        for (ScanResult data : arr){
            if (!ScanResult.isValid(data)) break;

            for (int target : targets){
                if (data.getPieceId() == target) return true;
            }
        }
        return false;
    }
}
