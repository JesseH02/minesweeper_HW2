import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Minesweeper extends JFrame {
    private static final int ROWS = 10;
    private static final int COLS = 10;
    private static final int MINES = 10;

    private CellButton[][] buttons;
    private boolean[][] mineBoard;
    private boolean[][] revealed;
    private boolean[][] flagged;
    private int[][] neighborCounts;

    private JPanel boardPanel;
    private JLabel statusLabel;
    private boolean gameOver;
    private int cellsToReveal;

    public Minesweeper() {
        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        statusLabel = new JLabel("Minesweeper - Left click to reveal, Right click to flag");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(statusLabel, BorderLayout.NORTH);

        boardPanel = new JPanel(new GridLayout(ROWS, COLS));
        add(boardPanel, BorderLayout.CENTER);

        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> initializeGame());
        add(restartButton, BorderLayout.SOUTH);

        initializeGame();
        setVisible(true);
    }

    private void initializeGame() {
        boardPanel.removeAll();

        buttons = new CellButton[ROWS][COLS];
        mineBoard = new boolean[ROWS][COLS];
        revealed = new boolean[ROWS][COLS];
        flagged = new boolean[ROWS][COLS];
        neighborCounts = new int[ROWS][COLS];

        gameOver = false;
        cellsToReveal = ROWS * COLS - MINES;

        placeMines();
        calculateNeighborCounts();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                CellButton button = new CellButton(row, col);
                button.setFont(new Font("Arial", Font.BOLD, 18));

                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameOver) {
                            return;
                        }

                        int r = button.getRow();
                        int c = button.getCol();

                        if (SwingUtilities.isRightMouseButton(e)) {
                            toggleFlag(r, c);
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            revealCell(r, c);
                        }
                    }
                });

                buttons[row][col] = button;
                boardPanel.add(button);
            }
        }

        statusLabel.setText("Minesweeper - Left click to reveal, Right click to flag");
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private void placeMines() {
        Random random = new Random();
        int placed = 0;

        while (placed < MINES) {
            int row = random.nextInt(ROWS);
            int col = random.nextInt(COLS);

            if (!mineBoard[row][col]) {
                mineBoard[row][col] = true;
                placed++;
            }
        }
    }

    private void calculateNeighborCounts() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (mineBoard[row][col]) {
                    neighborCounts[row][col] = -1;
                    continue;
                }

                int count = 0;
                for (int r = row - 1; r <= row + 1; r++) {
                    for (int c = col - 1; c <= col + 1; c++) {
                        if (isInBounds(r, c) && mineBoard[r][c]) {
                            count++;
                        }
                    }
                }
                neighborCounts[row][col] = count;
            }
        }
    }

    private void toggleFlag(int row, int col) {
        if (revealed[row][col]) {
            return;
        }

        flagged[row][col] = !flagged[row][col];
        buttons[row][col].setText(flagged[row][col] ? "F" : "");
    }

    private void revealCell(int row, int col) {
        if (!isInBounds(row, col) || revealed[row][col] || flagged[row][col]) {
            return;
        }

        revealed[row][col] = true;
        buttons[row][col].setEnabled(false);

        if (mineBoard[row][col]) {
            buttons[row][col].setText("X");
            buttons[row][col].setBackground(Color.RED);
            endGame(false);
            return;
        }

        cellsToReveal--;

        int count = neighborCounts[row][col];
        if (count > 0) {
            buttons[row][col].setText(String.valueOf(count));
        } else {
            buttons[row][col].setText("");
            for (int r = row - 1; r <= row + 1; r++) {
                for (int c = col - 1; c <= col + 1; c++) {
                    if (isInBounds(r, c) && !revealed[r][c]) {
                        revealCell(r, c);
                    }
                }
            }
        }

        if (cellsToReveal == 0) {
            endGame(true);
        }
    }

    private void endGame(boolean won) {
        gameOver = true;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (mineBoard[row][col]) {
                    buttons[row][col].setText("X");
                    buttons[row][col].setEnabled(false);
                }
            }
        }

        if (won) {
            statusLabel.setText("You win!");
            JOptionPane.showMessageDialog(this, "Congratulations! You cleared the board!");
        } else {
            statusLabel.setText("Game Over!");
            JOptionPane.showMessageDialog(this, "You hit a mine. Game over!");
        }
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Minesweeper::new);
    }

    private static class CellButton extends JButton {
        private final int row;
        private final int col;

        public CellButton(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }
    }
}