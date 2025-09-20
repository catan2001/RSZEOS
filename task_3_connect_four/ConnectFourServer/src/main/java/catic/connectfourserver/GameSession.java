/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package catic.connectfourserver;

/**
 *
 * @author catic
 */
public class GameSession {

    private ConnectedClient playerOne;
    private ConnectedClient playerTwo;
    private ConnectedClient currentTurn;
    private final int[][] board = new int[6][7]; // 6 rows x 7 columns
    private final int NUMBER_OF_CELLS = 6 * 7;
    private int usedCells = 0;

    // This is used in the method setAndCheckNewGame
    private boolean newGameWantedPlayerOne = false;
    private boolean newGameWantedPlayerTwo = false;

    public GameSession(ConnectedClient p1, ConnectedClient p2) {
        this.playerOne = p1;
        this.playerTwo = p2;
        this.currentTurn = p1; // p1 starts first
    }

    public boolean hasPlayer(String username) {
        return playerOne.getUsername().equals(username) || playerTwo.getUsername().equals(username);
    }

    /**
     * Handles a move from a client
     *
     * @param player
     * @param col
     */
    public void handleMove(ConnectedClient player, int col) {
        if (player != currentTurn) {
            player.sendMessage("SERVER_GAME_WAIT_FOR_OPPONENTS_MOVE");
            return;
        }

        int row = dropDisc(col, (player == playerOne ? 1 : 2));

        if (row == -1) {
            player.sendMessage("SERVER_GAME_INVALID_MOVE");
            return;
        }
        // Notify both clients about the move
        String moveMsg = "SERVER_GAME_MOVE:" + (player == playerOne ? 1 : 0) + ";" + row + ";" + col;
        playerOne.sendMessage(moveMsg);
        playerTwo.sendMessage(moveMsg);
        // Check for win
        if (checkWin(row, col)) {
            player.sendMessage("SERVER_GAME_WON");
            getOpponent(player).sendMessage("SERVER_GAME_LOST");
            return;
        }
        // Check if players used all of the cells and no win or lose
        if (usedCells >= NUMBER_OF_CELLS) {
            playerOne.sendMessage("SERVER_GAME_DRAW");
            playerTwo.sendMessage("SERVER_GAME_DRAW");
        }
        // Switch turns
        currentTurn = getOpponent(player);
    }

    /**
     * Drops a disc into the board
     */
    private int dropDisc(int col, int disc) {
        for (int r = board.length - 1; r >= 0; r--) {
            if (board[r][col] == 0) {
                board[r][col] = disc;
                return r;
            }
        }
        return -1; // invalid move
    }

    /**
     * Very simple restart (clears the board and notifies clients)
     */
    private void restartGame() {
        usedCells = 0;
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                board[r][c] = 0;
            }
        }
        playerOne.sendMessage("SERVER_GAME_RESTARTS");
        playerTwo.sendMessage("SERVER_GAME_RESTARTS");

        // Swap the players, when a new game starts the players should
        // swap sides and the player that played second will now play the first
        ConnectedClient playerTemp = playerOne;
        playerOne = playerTwo;
        playerTwo = playerTemp;

        currentTurn = playerOne;
    }

    private ConnectedClient getOpponent(ConnectedClient player) {
        return (player == playerOne) ? playerTwo : playerOne;
    }

    // Sets the wantedness of the player. Checks which player accepted
    // a new game and sets it's wantedness to true;
    public void setNewGameWanted(ConnectedClient player) {
        if (player == playerOne) {
            newGameWantedPlayerOne = true;
            return;
        }
        newGameWantedPlayerTwo = true;
    }

    // Checks if both players want to restart the game and if so
    // it calls restartGame method.
    public void checkNewGameWanted() {
        if (newGameWantedPlayerOne && newGameWantedPlayerTwo) {
            // Reset so that users can restart the game again
            // or not restart if they chose so.
            newGameWantedPlayerOne = false;
            newGameWantedPlayerTwo = false;
            restartGame();
        }
    }

    public void handleDisconnect(ConnectedClient disconnectedPlayer) {
        ConnectedClient opponent = (disconnectedPlayer == playerOne) ? playerTwo : playerOne;
        if (opponent != null && opponent.isAlive()) {
            opponent.sendMessage("SERVER_OPPONENT_DISCONNECTED");
        }
    }

    public void endGameSession() {
        playerOne.sendMessage("SERVER_GAME_EXIT");
        playerTwo.sendMessage("SERVER_GAME_EXIT");
    }

    private boolean checkWin(int row, int col) {
        usedCells++; // increments used cells
        int disc = board[row][col]; // 1 or 2
        if (disc == 0) {
            return false;
        }

        int count; // count the number of correct discs 
        // find some faster algorithm for this

        count = 1;
        // left
        for (int c = col - 1; c >= 0; c--) {
            // maybe the c>=0 is not good way but it should
            // evenrually exit
            if (board[row][c] == disc) {
                count++;
            } else {
                break;
            }
        }
        // right
        for (int c = col + 1; c < board[0].length; c++) {
            if (board[row][c] == disc) {
                count++;
            } else {
                break;
            }
        }
        if (count >= 4) {
            return true;
        }

        // check vertical up to down side
        count = 1;
        // up
        for (int r = row - 1; r >= 0; r--) {
            if (board[r][col] == disc) {
                count++;
            } else {
                break;
            }
        }
        // down
        for (int r = row + 1; r < board.length; r++) {
            if (board[r][col] == disc) {
                count++;
            } else {
                break;
            }
        }
        if (count >= 4) {
            return true;
        }

        // check diagonal top left to bottom right
        count = 1;
        // top-left
        int r = row - 1, c = col - 1;
        while (r >= 0 && c >= 0) {
            if (board[r][c] == disc) {
                count++;
            } else {
                break;
            }
            r--;
            c--;
        }
        // bottom right
        r = row + 1;
        c = col + 1;
        while (r < board.length && c < board[0].length) {
            if (board[r][c] == disc) {
                count++;
            } else {
                break;
            }
            r++;
            c++;
        }
        if (count >= 4) {
            return true;
        }

        // check diagonal top right to bottom left
        count = 1;
        // top right
        r = row - 1;
        c = col + 1;
        while (r >= 0 && c < board[0].length) {
            if (board[r][c] == disc) {
                count++;
            } else {
                break;
            }
            r--;
            c++;
        }
        // bottom left
        r = row + 1;
        c = col - 1;
        while (r < board.length && c >= 0) {
            if (board[r][c] == disc) {
                count++;
            } else {
                break;
            }
            r++;
            c--;
        }
        return count >= 4;
    }
}
