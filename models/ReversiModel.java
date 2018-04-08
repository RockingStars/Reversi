/*
 * Enjun
 *
 * @version     1.0 Beta 1
 * @author      Rocking Stars
 * @copyright   2018, Enjun
 *
 * Copyright 2018 RockingStars

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rockingstar.modules.Reversi.models;

import com.rockingstar.engine.game.Player;
import com.rockingstar.modules.Reversi.views.ReversiView;

import javafx.application.Platform;

public class ReversiModel {

    private ReversiView _view;

    private Player[][] _board = new Player[8][8];

    public ReversiModel(ReversiView view) {
        _view = view;
    }

    public void setStartingPositions(Player player1, Player player2) {
        Player black = player1.getCharacter() == 'b' ? player1 : player2;
        Player white = player1.getCharacter() == 'w' ? player1 : player2;

        _board[3][3] = white;
        _board[3][4] = black;
        _board[4][3] = black;
        _board[4][4] = white;

        for (int y = 3; y < 5; y++)
            for (int x = 3; x < 5; x++)
                _view.setCellImage(y, x);
    }

    public Player[][] getBoard() {
        return _board;
    }

    public void addEventHandlers() {
        _view.getEndButton().setOnAction(e -> Platform.exit());
        _view.getNewGameButton().setOnAction(e -> clearBoard());
    }

    public boolean isValidMove(int x, int y, Player player) {
        if (x > 8 || y > 8 || x < 0 || y < 0 || _board[y][x] != null)
            return false;

        // Test if this is a valid move by checking how many tiles would be flipped
        // Note: inspired by: https://inventwithpython.com/chapter15.html
        int directions[][] = {
            {0, 1}, {1, 1}, {1, 0}, {1, -1},
            {0, -1}, {-1, 0}, {-1, -1}, {-1, 1}
        };

        for (int[] direction : directions) {
            int posX = direction[0] + 1;
            int posY = direction[1] + 1;

            while (posX < _board.length && posY < _board.length && _board[posY][posX] != player && _board[posY][posX] != null) {
                posX += direction[0];
                posY += direction[1];
            }

            if (_board[posY][posX] == player)
                return true;
        }

        return false;
    }

    public void switchTiles(int x1, int y1, int x2, int y2) {
        // switch tiles here
    }

    public void setPlayerAtPosition(Player player, int x, int y) {
        _board[x][y] = player;
    }

    private void clearBoard() {
        _board = new Player[8][8];
        createCells();

        _view.setBoard(_board);
        _view.generateBoardVisual();
        _view.setIsFinished(false);
    }

    public void createCells() {
        for (int i = 0; i < _board.length; i++)
            for (int j = 0; j < _board[i].length; j++)
                _board[i][j] = null;
    }

    public boolean hasWon(Player player) {
        return false;
    }

    public boolean isFull() {
        for (int i = 0; i < _board.length; i++)
            for (int j = 0; j < _board[i].length; j++)
                if (_board[i][j] == null)
                    return false;

        return true;
    }

    public int getScoreForPlayer(Player player) {
        int score = 0;

        for (int i = 0; i < _board.length; i++)
            for (int j = 0; j < _board[i].length; j++)
                if (_board[i][j] != null && _board[i][j] == player)
                    score++;

        return score;
    }

    public String getTurnMessage(Player player) {
        return player.getUsername() + ", it's your turn";
    }
}
