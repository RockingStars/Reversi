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

package com.rockingstar.modules.Reversi.controllers;

import com.rockingstar.engine.ServerConnection;
import com.rockingstar.engine.command.client.CommandExecutor;
import com.rockingstar.engine.command.client.MoveCommand;
import com.rockingstar.engine.game.*;
import com.rockingstar.engine.game.models.VectorXY;
import com.rockingstar.engine.io.models.Util;
import com.rockingstar.modules.Reversi.models.ReversiModel;
import com.rockingstar.modules.Reversi.views.ReversiView;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;

public class ReversiController extends AbstractGame {

    private ReversiModel _model;
    private ReversiView _view;

    public ReversiController(Player player1, Player player2) {
        super(player1, player2);

        _view = new ReversiView(this);
        _model = new ReversiModel(_view);

        _model.addEventHandlers();
        _model.createCells();

        _view.setBoard(_model.getBoard());
        _view.generateBoardVisual();

        if (player1 instanceof OverPoweredAI)
            ((OverPoweredAI) player1).setModel(_model);
        else if (player1 instanceof Lech)
            ((Lech) player1).setModel(_model);
        else if (player1 instanceof MinimaxAI) {
            ((MinimaxAI) player1).setModel(_model);
            ((MinimaxAI) player1).setController(this);
        }
    }

    @Override
    public Node getView() {
        return _view.getNode();
    }

    @Override
    public void doPlayerMove(int x, int y) {
        _model.clearPossibleMoves();
        if (!(getGameState() == State.GAME_FINISHED)) {
            if (yourTurn) {
                Util.displayStatus("Amount of flippable tiles: " + _model.getFlippableTiles(x,y,player1));
                if (_model.isValidMove(x, y, player1)) {
                    System.out.println("Valid move");
                    _model.flipTiles(_model.getFlippableTiles(x,y,player1), player1);
                    System.out.println("komt hier 1");
                    _model.setPlayerAtPosition(player1, x, y);
                    System.out.println("komt hier 2");
                    _view.setCellImage(x, y);
                    System.out.println("komt hier 3");
                    CommandExecutor.execute(new MoveCommand(ServerConnection.getInstance(), y * 8 + x));
                }
                else {
                    System.out.println("Not a valid move");
                    _view.setErrorStatus("Invalid move");
                    _model.getPossibleMoves(player1);
                }
            }
            else
                _view.setErrorStatus("It's not your turn");
        }
    }

    @Override
    public void doPlayerMove(int position) {
        Platform.runLater(() -> getScores());
        if (!(getGameState() == State.GAME_FINISHED)) {

            if (yourTurn) {
                yourTurn = false;
                return;
            }

            int x = position % 8;
            int y = position / 8;

            _model.clearPossibleMoves();
            _model.flipTiles(_model.getFlippableTiles(x,y,player2), player2);
            _model.setPlayerAtPosition(player2, x, y);
            _view.setCellImage(x, y);
            yourTurn = true;
        }
    }

    @Override
    public void doYourTurn() {
        yourTurn = true;

        ArrayList<Integer> possibleMoves = _model.getPossibleMoves(player1);

        if (possibleMoves.size() == 0) {
            if (getGameState() != State.GAME_FINISHED) {
                CommandExecutor.execute(new MoveCommand(ServerConnection.getInstance()));
            }

            Util.displayStatus("No possible moves.");
            return;
        }

        if (player1 instanceof AI) {
            _model.clearPossibleMoves();
            VectorXY coordinates = ((AI) player1).getMove(player1, possibleMoves);

            Util.displayStatus("AI MOVE: " + coordinates.x + ", " + coordinates.y);
            //Util.displayStatus("Player at position: " + coordinates.x + ", " + coordinates.y + " : " + _model.getBoard()[coordinates.x][coordinates.y].getCharacter());
            doPlayerMove(coordinates.x, coordinates.y);

        }
    }

    @Override
    public void gameEnded(String result) {
        Platform.runLater(() -> getScores());
        super.gameEnded(result);
        _view.setIsFinished(true);

        switch (result) {
            case "WIN":
                _view.setStatus("Player " + player1.getUsername() + " has won! Congratulations!");
                break;
            case "LOSS":
                _view.setStatus("You've lost!");
                break;
            case "DRAW":
                _view.setStatus("It's a draw. Noobs.");
                break;
            default:
                _view.setStatus("Unknown result");
        }

        Platform.runLater(() -> {
            Alert returnToLobby = new Alert(Alert.AlertType.CONFIRMATION);

            returnToLobby.setTitle("Game ended!");
            returnToLobby.setHeaderText(null);
            returnToLobby.setContentText("Do you want to return to the lobby?");
            returnToLobby.showAndWait();

            if (returnToLobby.getResult() == ButtonType.OK)
                toLobby();
        });

    }

    public void setStartingPlayer(Player player) {
        player1.setCharacter(player.getUsername().equals(player1.getUsername()) ? 'b' : 'w');
        player2.setCharacter(player1.getCharacter() == 'b' ? 'w' : 'b');

        if (player1.getCharacter() == 'w') {
            Util.displayStatus("Player 1 is white");
        } else if(player1.getCharacter() == 'b'){
            Util.displayStatus("Player 1 is black");
        }

        _model.setStartingPositions(player1, player2);
        _view.updatePlayerColors();
    }

    public void getScores(){
        int[] scores = _model.getScore();
        _view.getP1Score().setText("" + scores[player1.getCharacter() == 'b' ? 0 : 1]);
        _view.getP2Score().setText("" + scores[player2.getCharacter() == 'b' ? 0 : 1]);
    }

    public String getPlayer1Name(){
        return player1.getUsername();
    }

    public String getPlayer2Name(){
        return player2.getUsername();
    }

    public char getColorP1(){
        return player1.getCharacter();
    }

    public Player getPlayer1(){
        return player1;
    }

    public Player getPlayer2(){
        return player2;
    }
}