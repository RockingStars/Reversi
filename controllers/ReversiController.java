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
import com.rockingstar.engine.game.AI;
import com.rockingstar.engine.game.AbstractGame;
import com.rockingstar.engine.game.Player;
import com.rockingstar.engine.game.State;
import com.rockingstar.engine.game.models.VectorXY;
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

        if (player1 instanceof AI)
            ((AI) player1).setModel(_model);
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
                if (_model.isValidMove(x, y, player1)) {

                    _model.flipTiles(_model.getFlippableTiles(x,y,player1), player1);
                    _model.setPlayerAtPosition(player1, x, y);
                    _view.setCellImage(x, y);
                    CommandExecutor.execute(new MoveCommand(ServerConnection.getInstance(), y * 8 + x));
                }
                else {
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
            //setGameState(State.GAME_FINISHED);
            return;
        }

        if (player1 instanceof AI) {
            VectorXY coordinates = ((AI) player1).getMove(player1, possibleMoves);
            doPlayerMove(coordinates.x, coordinates.y);
        }
    }

    @Override
    public void showPossibleMoves() {
        _model.getPossibleMoves(player1);
    }

    @Override
    public void gameEnded(String result) {
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

        _model.setStartingPositions(player1, player2);
    }
}