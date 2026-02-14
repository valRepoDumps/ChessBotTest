package ChessResources.Hasher;

import java.util.Arrays;

public class HashContainer {
    long[] hash = new long[2];

    public HashContainer(long hash1, long hash2){
        hash[0] = hash1;
        hash[1] = hash2;
    }

    public HashContainer(long[] hash){
        this.hash = hash.clone();
    }

    //region GETTERS

    public long[] getHash() {
        return hash.clone();
    }
    //endregion

    @Override
    public boolean equals(Object hs){
        if (!(hs instanceof HashContainer)) return false;

        return this.getHash()[0] == ((HashContainer) hs).getHash()[0] &&
                this.getHash()[1] == ((HashContainer) hs).getHash()[1];
    }

    @Override
    public int hashCode() {
        int h1 = Long.hashCode(hash[0]);
        int h2 = Long.hashCode(hash[1]);
        return h1 ^ h2;
    }

    @Override
    public HashContainer clone() {
        return new HashContainer(hash); //long[] is clone in constructor already.
    }

    @Override
    public String toString(){
        return Arrays.toString(hash);
    }
}
