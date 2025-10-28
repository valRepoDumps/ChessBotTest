package ChessResources.Pieces.PieceDatas;

public class ConditionalSlidingPieceData extends SlidingPieceData {
    public boolean useSecondMaxRange;
    public int secondMaxRange;

    //region CONSTRUCTOR
    ConditionalSlidingPieceData(short pieceId, boolean useSecondMaxRange)
    {
        super(pieceId);
        switch(pieceId)
        {
            case BPAWN: case WPAWN:
                this.secondMaxRange =2;
                this.useSecondMaxRange = useSecondMaxRange;
        }
    }

    ConditionalSlidingPieceData(short pieceId)
    {
        super(pieceId);
        switch(pieceId)
        {
            case BPAWN: case WPAWN:
            this.secondMaxRange =2;
            this.useSecondMaxRange = true;
        }
    }

    ConditionalSlidingPieceData(short pieceId, short[] possibleDirections,
                                int maxRange, boolean useSecondMaxRange, int secondMaxRange)
    {
        super(pieceId, possibleDirections, maxRange);
        this.useSecondMaxRange = useSecondMaxRange;
        this.secondMaxRange = secondMaxRange;
    }

    public ConditionalSlidingPieceData(ConditionalSlidingPieceData piece)
    {
        super(piece);
        this.useSecondMaxRange = piece.useSecondMaxRange;
        this.secondMaxRange = piece.secondMaxRange;
    }
    //endregion

    public short[] getPossibleDirections() {return possibleDirections;}
    public int getMaxRange() {return useSecondMaxRange ? secondMaxRange : maxRange;};
}
