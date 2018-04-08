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

        for (int x = 3; x < 5; x++)
            for (int y = 3; y < 5; y++)
                _view.setCellImage(x, y);
    }

    public Player[][] getBoard() {
        return _board;
    }

    public void addEventHandlers() {
        _view.getEndButton().setOnAction(e -> Platform.exit());
        _view.getNewGameButton().setOnAction(e -> clearBoard());
    }

    public boolean isValidMove(int x, int y) {
        if (x > 8 || y > 8 || x < 0 || y < 0)
            return false;

        return _board[x][y] == null;
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
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                _board[i][j] = null;
    }

    public boolean hasWon(Player player) {
        return false;
    }

    public boolean isFull() {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                if (_board[i][j] == null)
                    return false;

        return true;
    }

    public String getTurnMessage(Player player) {
        return player.getUsername() + ", it's your turn";
    }
}
