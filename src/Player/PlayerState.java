package Player;

import java.nio.file.Path;

public class PlayerState {
    String name;
    Path imagePath;
    int Elo;

    public PlayerState(String name, int Elo, Path imagePath){
        this.name = name;
        this.Elo = Elo;
        this.imagePath = imagePath;
    }

    public void updatePlayerName(String newName){
        this.name = newName;
    }

    public void updatePlayerElo(int newElo){
        this.Elo=newElo;
    }

    public void updatePlayerImage(Path newImagePath){
        this.imagePath = newImagePath;
    }
}
