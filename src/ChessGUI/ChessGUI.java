package ChessGUI;

import ChessLogic.ChessGame;
import ChessLogic.Debug.Tests;
import ChessResources.ChessBoard.ChessBoardUI;
import ChessResources.ChessHistoryTracker.BoardStateChanges.PropertiesStatsChange;
import ChessResources.Pieces.PieceData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.BiFunction;

public class ChessGUI {
    public static ChessGame chessGame;
    public final int BOARD_PIXEL_SIZE;
    JPanel boardGraphic;

    BiFunction<Integer, Boolean, Short> choosePiecePromotionUI =
            (Integer _, Boolean color) ->    {
                String[] options = {"Queen", "Rook", "Bishop", "Knight"};

                int choice = JOptionPane.showOptionDialog(
                        boardGraphic,
                        "Promote pawn to:",
                        "Promotion",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        options[0]
                );

                // If closed or invalid → default to queen
                if (color == PieceData.WHITE)
                {
                    return switch (choice) {
                        case 1 -> PieceData.WROOK;
                        case 2 -> PieceData.WBISHOP;
                        case 3 -> PieceData.WKNIGHT;
                        default -> PieceData.WQUEEN;
                    };
                }
                else
                {
                    return switch (choice) {
                        case 1 -> PieceData.BROOK;
                        case 2 -> PieceData.BBISHOP;
                        case 3 -> PieceData.BKNIGHT;
                        default -> PieceData.BQUEEN;
                    };
                }
            };

    public ChessGUI()
    {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000,1000);
        frame.setLayout(new BorderLayout());

        JPanel outerPanel = new JPanel(new GridBagLayout());
        frame.add(outerPanel, BorderLayout.CENTER);

        //region ADDING_INPUTS
        outerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "backspacePressed");

        outerPanel.getActionMap().put("backspacePressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //System.out.println("ENTER pressed!");
                try {
                    chessGame.undoTurn();
                    System.out.println(new PropertiesStatsChange(chessGame.gameProperties, chessGame.gameStats));

                }
                catch (Exception ex){
                    System.out.println(ex.getMessage());
                }
            }
        });
        //endregion
        //region CHESS_GAME_GRAPHICS
        chessGame = new ChessGame(this, choosePiecePromotionUI);

        boardGraphic = chessGame.chessBoard.boardGraphic;
        BOARD_PIXEL_SIZE = ChessBoardUI.BOARD_SIZE * ChessBoardUI.SQUARE_PIXEL_SIZE;

        boardGraphic.setPreferredSize(new Dimension(BOARD_PIXEL_SIZE, BOARD_PIXEL_SIZE));
        boardGraphic.setMaximumSize(boardGraphic.getPreferredSize());
        boardGraphic.setMinimumSize(boardGraphic.getPreferredSize());
        //endregion

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

    public int test(int depth){
        return Tests.moveGenerationTest(depth, chessGame);
    }
}