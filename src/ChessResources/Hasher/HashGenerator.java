package ChessResources.Hasher;

import ChessLogic.MinimalChessGame;
import java.util.Random;

public class HashGenerator {
    ZobristHasher hasher1;
    ZobristHasher hasher2;
    MinimalChessGame game;

    public HashGenerator(MinimalChessGame game){
        this.game = game;
        Random rand = new Random();
        hasher1 = new ZobristHasher(rand.nextLong());
        hasher2 = new ZobristHasher(rand.nextLong());
    }

    @SuppressWarnings("unused")
    public HashGenerator(MinimalChessGame game, long seed1, long seed2){
        this.game = game;
        hasher1 = new ZobristHasher(seed1);
        hasher2 = new ZobristHasher(seed2);
    }

    public HashContainer getCurrentHash(){
        return new HashContainer(hasher1.getGameHash(game), hasher2.getGameHash(game));
    }

    public long getSeed1() {
        return hasher1.getSeed();
    }

    public long getSeed2() {
        return hasher2.getSeed();
    }

}
