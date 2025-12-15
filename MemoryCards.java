import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class MemoryCards {
    class Card {
        String cardName;
        ImageIcon cardImageIcon;
        Card(String cardName, ImageIcon cardImageIcon) {
            this.cardName = cardName;
            this.cardImageIcon = cardImageIcon;
        }
        public String toString() { return cardName; }
    }

    String[] cardList = {
        "darkness", "double", "fairy", "fighting", "fire",
        "grass", "lightning", "metal", "psychic", "water"
    };

    // Configurable difficulty
    String[] difficulties = {"Easy (3x4)", "Medium (4x5)", "Hard (6x6)"};
    int[] rowsOptions = {3, 4, 6};
    int[] colsOptions = {4, 5, 6};

    int rows, columns, cardWidth = 90, cardHeight = 128;
    ArrayList<Card> cardSet;
    ImageIcon cardBackImageIcon;

    JFrame frame = new JFrame("Pokemon Memory Match");
    JLabel statusLabel = new JLabel();
    JLabel timerLabel = new JLabel();
    JPanel topPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel controlPanel = new JPanel();
    JButton restartButton = new JButton("Restart");
    JComboBox<String> difficultyBox = new JComboBox<>(difficulties);
    JCheckBox twoPlayerBox = new JCheckBox("2-Player Mode");

    int errorCount = 0, matchCount = 0, totalPairs;
    ArrayList<JButton> board;
    Timer hideCardTimer, stopwatchTimer;
    boolean gameReady = false, isPlayer1Turn = true;
    JButton card1Selected, card2Selected;

    int seconds = 0;
    int player1Score = 0, player2Score = 0;

    MemoryCards() {
        setupDifficulty(1);
        setupUI();
        startNewGame();
    }

    void setupDifficulty(int diffIndex) {
        rows = rowsOptions[diffIndex];
        columns = colsOptions[diffIndex];
        totalPairs = (rows * columns) / 2;
    }

    void setupUI() {
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        topPanel.setLayout(new GridLayout(2, 1));
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        timerLabel.setHorizontalAlignment(JLabel.CENTER);
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        topPanel.add(statusLabel);
        topPanel.add(timerLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        controlPanel.add(new JLabel("Difficulty:"));
        controlPanel.add(difficultyBox);
        controlPanel.add(twoPlayerBox);
        controlPanel.add(restartButton);
        frame.add(controlPanel, BorderLayout.SOUTH);

        difficultyBox.addActionListener(e -> restartGame());
        twoPlayerBox.addActionListener(e -> restartGame());
        restartButton.addActionListener(e -> restartGame());

        hideCardTimer = new Timer(1200, e -> hideMismatchedCards());
        hideCardTimer.setRepeats(false);

        stopwatchTimer = new Timer(1000, e -> updateTimer());
    }

    void startNewGame() {
        setupDifficulty(difficultyBox.getSelectedIndex());
        setupCards();
        shuffleCards();

        frame.getContentPane().removeAll();
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(controlPanel, BorderLayout.SOUTH);

        board = new ArrayList<>();
        boardPanel = new JPanel(new GridLayout(rows, columns, 5, 5));
        boardPanel.setBackground(new Color(50, 50, 100));

        for (int i = 0; i < cardSet.size(); i++) {
            JButton tile = new JButton();
            tile.setPreferredSize(new Dimension(cardWidth, cardHeight));
            tile.setIcon(cardBackImageIcon);
            tile.setFocusable(false);

            final int index = i;
            tile.addActionListener(e -> tileClicked(tile, index));

            board.add(tile);
            boardPanel.add(tile);
        }

        frame.add(boardPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        resetGameState();
        hideAllCards();
    }

    void tileClicked(JButton tile, int index) {
        if (!gameReady || tile.getIcon() != cardBackImageIcon || card1Selected == tile || card2Selected == tile) {
            return;
        }

        if (card1Selected == null) {
            card1Selected = tile;
            card1Selected.setIcon(cardSet.get(index).cardImageIcon);
        } else if (card2Selected == null) {
            card2Selected = tile;
            card2Selected.setIcon(cardSet.get(index).cardImageIcon);

            if (card1Selected.getIcon().equals(card2Selected.getIcon())) {
                matchFound();
            } else {
                errorCount++;
                hideCardTimer.start();
            }
        }
    }

    void matchFound() {
        matchCount++;
        if (twoPlayerBox.isSelected()) {
            if (isPlayer1Turn) player1Score++;
            else player2Score++;
            updateStatus();
            // Same player goes again
        } else {
            player1Score = matchCount;
        }

        card1Selected = null;
        card2Selected = null;

        if (matchCount == totalPairs) {
            gameWon();
        }
    }

    void hideMismatchedCards() {
        if (card1Selected != null && card2Selected != null) {
            card1Selected.setIcon(cardBackImageIcon);
            card2Selected.setIcon(cardBackImageIcon);
            card1Selected = null;
            card2Selected = null;

            if (twoPlayerBox.isSelected()) {
                isPlayer1Turn = !isPlayer1Turn;
                updateStatus();
            }
        }
    }

    void hideAllCards() {
        new Timer(1500, e -> {
            for (JButton b : board) b.setIcon(cardBackImageIcon);
            gameReady = true;
            restartButton.setEnabled(true);
            stopwatchTimer.start();
            updateStatus();
        }).start();
    }

    void gameWon() {
        gameReady = false;
        stopwatchTimer.stop();
        JOptionPane.showMessageDialog(frame,
            "You Win!\n" +
            "Time: " + formatTime(seconds) + "\n" +
            "Errors: " + errorCount + "\n" +
            (twoPlayerBox.isSelected() ?
                "Player 1: " + player1Score + " | Player 2: " + player2Score :
                ""),
            "Victory!", JOptionPane.INFORMATION_MESSAGE);
    }

    void updateTimer() {
        seconds++;
        timerLabel.setText("Time: " + formatTime(seconds));
    }

    String formatTime(int secs) {
        int m = secs / 60;
        int s = secs % 60;
        return String.format("%02d:%02d", m, s);
    }

    void updateStatus() {
        if (!gameReady) {
            statusLabel.setText("Get Ready...");
            return;
        }

        String turn = twoPlayerBox.isSelected()
            ? (isPlayer1Turn ? "Player 1's Turn" : "Player 2's Turn")
            : "Your Turn";

        String score = twoPlayerBox.isSelected()
            ? " | P1: " + player1Score + " P2: " + player2Score
            : " | Matches: " + matchCount + "/" + totalPairs;

        statusLabel.setText(turn + " | Errors: " + errorCount + score);
    }

    void resetGameState() {
        errorCount = 0;
        matchCount = 0;
        player1Score = 0;
        player2Score = 0;
        isPlayer1Turn = true;
        seconds = 0;
        card1Selected = null;
        card2Selected = null;
        gameReady = false;
        timerLabel.setText("Time: 00:00");
        updateStatus();
        if (stopwatchTimer.isRunning()) stopwatchTimer.stop();
    }

    void restartGame() {
        if (stopwatchTimer.isRunning()) stopwatchTimer.stop();
        if (hideCardTimer.isRunning()) hideCardTimer.stop();
        startNewGame();
    }

    void setupCards() {
        cardSet = new ArrayList<>();
        int pairs = (rows * columns) / 2;
        ArrayList<String> selected = new ArrayList<>();
        for (int i = 0; i < pairs; i++) {
            selected.add(cardList[i % cardList.length]);
        }

        for (String name : selected) {
            Image img = new ImageIcon(getClass().getResource("./img/" + name + ".jpg")).getImage();
            ImageIcon icon = new ImageIcon(img.getScaledInstance(cardWidth, cardHeight, Image.SCALE_SMOOTH));
            cardSet.add(new Card(name, icon));
            cardSet.add(new Card(name, icon));
        }

        Image backImg = new ImageIcon(getClass().getResource("./img/back.jpg")).getImage();
        cardBackImageIcon = new ImageIcon(backImg.getScaledInstance(cardWidth, cardHeight, Image.SCALE_SMOOTH));
    }

    void shuffleCards() {
        for (int i = 0; i < cardSet.size(); i++) {
            int j = (int) (Math.random() * cardSet.size());
            Card temp = cardSet.get(i);
            cardSet.set(i, cardSet.get(j));
            cardSet.set(j, temp);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MemoryCards::new);
    }
}