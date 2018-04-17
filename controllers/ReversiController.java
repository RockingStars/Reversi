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
import com.rockingstar.engine.command.client.ForfeitCommand;
import com.rockingstar.engine.command.client.MoveCommand;
import com.rockingstar.engine.game.*;
import com.rockingstar.engine.game.models.VectorXY;
import com.rockingstar.engine.gui.controllers.AudioPlayer;
import com.rockingstar.engine.io.models.Util;
import com.rockingstar.modules.Reversi.models.ReversiModel;
import com.rockingstar.modules.Reversi.views.ReversiView;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;
import java.util.Optional;

/**
 * This class sets up Reversi
 * @author Rocking Stars
 * @since 1.0 Beta 1
 */
public class ReversiController extends AbstractGame {

    /**
     * The model contains basic reversi algorithms, used for determining if we have a winner
     */
    private ReversiModel _model;

    /**
     * Stores the graphical board, among other things
     */
    private ReversiView _view;

    /**
     * An instance of the AudioPlayer, containing the background music.
     */
    private AudioPlayer _backgroundMusic;

    /**
     * ReversiController constructor.
     * @param player1 The local player
     * @param player2 The opponent
     */
    public ReversiController(Player player1, Player player2) {
        super(player1, player2);

        _view = new ReversiView(this);
        _model = new ReversiModel(_view);

        _model.createCells();

        _view.setBoard(_model.getBoard());
        _view.generateBoardVisual();

        addEventHandlers();
        setupBackgroundMusic();
        _backgroundMusic.start();

        if (player1 instanceof HardAI) {
            ((HardAI) player1).setCounter(0);
            ((HardAI) player1).setModel(_model);
            ((HardAI) player1).setController(this);
        }
        else if (player1 instanceof EasyAI)
            ((EasyAI) player1).setModel(_model);
    }

    /**
     * Returns the view
     * @return The view
     */
    @Override
    public Node getView() {
        return _view.getNode();
    }

    /**
     * Handles player moves by the local player (after clicking on a tile).
     * @param x The x position
     * @param y The y position
     */
    @Override
    public void doPlayerMove(int x, int y) {
        _model.clearPossibleMoves();
        if (!(getGameState() == State.GAME_FINISHED)) {
            if (yourTurn) {
                Util.displayStatus("Amount of flippable tiles: " + _model.getFlippableTiles(x,y,player1));
                if (_model.isValidMove(x, y, player1)) {
                    _model.flipTiles(_model.getFlippableTiles(x,y,player1), player1);
                    _model.setPlayerAtPosition(player1, x, y);
                    _view.setCellImage(x, y);
                    CommandExecutor.execute(new MoveCommand(ServerConnection.getInstance(), y * 8 + x));
                    _view.setStatus("It is not your turn");
                }
                else {
                    System.out.println("Not a valid move");
                    _view.setErrorStatus("Invalid move");
                    _model.getPossibleMoves(player1);
                }
            }
            else {
                _view.setErrorStatus("It's not your turn");
            }
        }
    }

    /**
     * Handles moves by external players.
     * @param position The position
     */
    @Override
    public void doPlayerMove(int position) {
        Platform.runLater(() -> getScores());
        if (!(getGameState() == State.GAME_FINISHED)) {
            if (yourTurn) {
                Platform.runLater(() -> getScores());
                yourTurn = false;
                _view.stopTimer();
                _view.newTimerThread();
                return;
            } else {
                _view.stopTimer();

                int x = position % 8;
                int y = position / 8;

                _model.clearPossibleMoves();
                _model.flipTiles(_model.getFlippableTiles(x, y, player2), player2);
                _model.setPlayerAtPosition(player2, x, y);
                _view.setCellImage(x, y);

                _view.newTimerThread();
            }
        }
    }

    /**
     * Makes sure the player is able to make a move. When the local player is an AI, a move is
     * automatically sent back in this method.
     */
    @Override
    public void doYourTurn () {
        yourTurn = true;
        ArrayList<Integer> possibleMoves = _model.getPossibleMoves(player1);

        if (possibleMoves.size() == 0) {
            if (getGameState() != State.GAME_FINISHED) {
                yourTurn = false;
                Util.displayStatus("No possible moves left, switching turns");
                return;
            }
        }

        _view.setStatus("It is your turn");

        if (player1 instanceof AI) {
            _model.clearPossibleMoves();
            VectorXY coordinates = ((AI) player1).getMove(player1, possibleMoves);

            Util.displayStatus("AI MOVE: " + coordinates.x + ", " + coordinates.y);
            //Util.displayStatus("Player at position: " + coordinates.x + ", " + coordinates.y + " : " + _model.getBoard()[coordinates.x][coordinates.y].getCharacter());
            doPlayerMove(coordinates.x, coordinates.y);

        }
    }

    /**
     * Ends the game with a certain result.
     * @param result The result of the game (WIN< LOSS or DRAW)
     */
    @Override
    public void gameEnded (String result){
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

    /**
     * Sets the starting player
     * @param player The starting player
     */
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

    /**
     * Adds event handlers to the view
     */
    private void addEventHandlers() {
        _view.getEndButton().setOnAction(e ->{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Closing game");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to give up?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                CommandExecutor.execute(new ForfeitCommand(ServerConnection.getInstance()));
            }
        });

        _view.getRageQuitButton().setOnAction(e -> Platform.exit());
    }

    /**
     * Retrieves the scores of both players and adds them to the view
     */
    private void getScores() {
        int[] scores = _model.getScore();
        _view.getP1Score().setText("" + scores[player1.getCharacter() == 'b' ? 0 : 1]);
        _view.getP2Score().setText("" + scores[player2.getCharacter() == 'b' ? 0 : 1]);
    }

    /**
     * Sets up the background music
     */
    private void setupBackgroundMusic() {
        _backgroundMusic = new AudioPlayer("ReversiMusic.mp3", true);
    }

    /**
     * Returns the username of player 1
     * @return The username of player 1
     */
    public String getPlayer1Name(){
        return player1.getUsername();
    }

    /**
     * Returns the username of player 2
     * @return The username of player 2
     */
    public String getPlayer2Name(){
        return player2.getUsername();
    }

    /**
     * Returns the character (not color) of player 1.
     * @return Player 1's character
     */
    public char getColorP1(){
        return player1.getCharacter();
    }

    /**
     * Returns a Player object of the local player
     * @return The local player
     */
    public Player getPlayer1(){
        return player1;
    }

    /**
     * Returns a Player objects of the external player
     * @return The external player
     */
    public Player getPlayer2(){
        return player2;
    }
}