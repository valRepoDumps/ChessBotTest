package ChessGUI;

import ChessLogic.ChessGame;
import ChessResources.ChessBoardUI;

import javax.swing.*;
import java.awt.*;

public class ChessGUI {
    protected static ChessGame chessGame;
    public final int BOARD_PIXEL_SIZE;

    public ChessGUI()
    {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000,1000);
        frame.setLayout(new BorderLayout());

        JPanel outerPanel = new JPanel(new GridBagLayout());
        frame.add(outerPanel, BorderLayout.CENTER);

        chessGame = new ChessGame();
        JPanel boardGraphic = chessGame.chessBoardUI.boardGraphic;
        BOARD_PIXEL_SIZE = ChessBoardUI.BOARD_SIZE * ChessBoardUI.SQUARE_PIXEL_SIZE;

        boardGraphic.setPreferredSize(new Dimension(BOARD_PIXEL_SIZE, BOARD_PIXEL_SIZE));
        boardGraphic.setMaximumSize(boardGraphic.getPreferredSize());
        boardGraphic.setMinimumSize(boardGraphic.getPreferredSize());

        // Add boardGraphic to outerPanel using constraints that DO NOT fill
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; //where the component is placed.
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;         // important: don't ask to stretch, is how component resize to fit cells
        gbc.anchor = GridBagConstraints.CENTER;     // center it
        outerPanel.add(boardGraphic, gbc);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}