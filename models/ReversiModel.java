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

import java.util.ArrayList;
import java.util.LinkedList;


public class ReversiModel {

    private Player _ghost;
    private ReversiView _view;

    private Player[][] _board = new Player[8][8];

    private static final int DIRECTIONS[][] = {
            {0, 1}, {1, 1}, {1, 0}, {1, -1},
            {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}
    };

    public ReversiModel(ReversiView view) {
        _view = view;
        _ghost = new Player("PossibleMoves", null, 'p');
    }

    public void setStartingPositions(Player player1, Player player2) {
        Player black = player1.getCharacter() == 'b' ? player1 : player2;
        Player white = player1.getCharacter() == 'w' ? player1 : player2;


        setPlayerAtPosition(white,3,3);
        setPlayerAtPosition(black,3,4);
        setPlayerAtPosition(black,4,3);
        setPlayerAtPosition(white,4,4);

        for (int y = 3; y < 5; y++)
            for (int x = 3; x < 5; x++)
                _view.setCellImage(x, y);

        getPossibleMoves(black);

    }

    public Player[][] getBoard() {
        return _board;
    }

    public void addEventHandlers() {
        _view.getEndButton().setOnAction(e -> Platform.exit());
        _view.getNewGameButton().setOnAction(e -> clearBoard());
    }

    public void flipTiles(LinkedList<Integer> tilesToFlip, Player player){
        for (Integer tile : tilesToFlip) {
            setPlayerAtPosition(player, tile%8,tile/8);
            _view.setCellImage(tile % 8, tile / 8);
        }
    }

    public LinkedList<Integer> getFlippableTiles(int baseX, int baseY, Player player) {
        LinkedList<Integer> tilesToFlip = new LinkedList<>();

        char currentPlayer = player.getCharacter();
        char opponent = currentPlayer == 'b' ? 'w': 'b';

        if (!moveIsOnBoard(baseX,baseY) || _board[baseX][baseY] != null) {
            return tilesToFlip;
        }

        // houd bij welke tiles geflipt moeten worden
        int counter = 0; //for debugging

        for(int[] direction : DIRECTIONS) {
            counter++;

            int x = baseX;
            int y = baseY;

            x += direction[0];
            y += direction[1];

            if (moveIsOnBoard(x, y) && _board[x][y] != null && _board[x][y].getCharacter() == opponent) {
                //localTilesToFlip.add(y * 8 + x); // add first neighbour opponent
                x += direction[0];
                y += direction[1]; // one step deeper

                if (!moveIsOnBoard(x, y)) {
                    continue;
                }
                while (_board[x][y] != null && _board[x][y].getCharacter() == opponent) {
                    //localTilesToFlip.add(y * 8 + x);
                    x += direction[0];
                    y += direction[1];
                    if (!moveIsOnBoard(x, y)) {
                        break;
                    }
                }
                if (!moveIsOnBoard(x, y)){
                    continue;
                }

                if(_board[x][y] != null && _board[x][y].getCharacter() == currentPlayer){
                    while(true){
                        x -= direction[0];
                        y -= direction[1];
                        if(x == baseX && y == baseY){
                            break;
                        }
                        tilesToFlip.add(y * 8 + x);
                    }
                }
            }
        }

        return tilesToFlip;
    }


    public boolean isValidMove(int x, int y, Player player){
        return getFlippableTiles(x, y, player).size() > 0;
    }

    public boolean moveIsOnBoard(int x, int y){
        if (x < _board.length && y < _board.length && x >= 0 && y >= 0){
            return true;
        }
        return false;
    }

    public ArrayList<Integer> getPossibleMoves(Player player){
        clearPossibleMoves();
        ArrayList<Integer> possibleMoves = new ArrayList<>();
        for(int i = 0; i < _board.length; i++){
            for(int j = 0; j < _board.length; j++){
                if(_board[i][j] == null) {
                    if (isValidMove(i, j, player)) {
                        possibleMoves.add(j * 8 + i);
                        setPlayerAtPosition(_ghost,i,j);
                        _view.setCellImage(i,j);
                        //System.out.printf("Move from player %s\n", player.getUsername());
                    }
                }
            }
        }
        return possibleMoves;
    }

    public void clearPossibleMoves() {
        for (int i = 0; i < _board.length; i++) {
            for (int j = 0; j < _board.length; j++) {
                if (_board[j][i] == _ghost) {
                    //System.out.println(j * 8 + i);
                    _board[j][i] = null;
                    _view.setCellImage(j, i);
                }
            }
        }
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