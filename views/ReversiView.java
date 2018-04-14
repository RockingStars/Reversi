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

package com.rockingstar.modules.Reversi.views;

import com.rockingstar.engine.game.AI;
import com.rockingstar.engine.game.Player;
import com.rockingstar.engine.io.models.Util;
import com.rockingstar.engine.lobby.views.LobbyView;
import com.rockingstar.modules.Reversi.controllers.ReversiController;
import com.rockingstar.modules.Reversi.models.ReversiModel;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Timer;
import java.util.TimerTask;

import java.awt.*;
import java.net.URISyntaxException;
import java.util.Optional;

public class ReversiView {

    private BorderPane _borderPane;

    // Top
    private VBox _gameInfo;
    private Label _status;
    private Label _errorStatus;
    private Label _timer;

    // Left
    private VBox _player1Info;
    private Label _player1Name;
    private Label _player1Score;
    private Label _player1Color;

    // Right
    private VBox _player2Info;
    private Label _player2Name;
    private Label _player2Score;
    private Label _player2Color;

    // Center
    private GridPane _pane;
    private Label _name1;
    private Label _name2;
    private Label _score1;
    private Label _score2;
    private Button _colorImage1;
    private Button _colorImage2;
    private int counter;
    private Label countLabel;

    // Bottom
    private HBox _buttons;
    private Button _endButton;
    private Button _newGameButton;
    private Button _rageQuit;
    private Button _hanze;

    private Player[][] _board;
    private ReversiController _controller;
    private boolean _isFinished;

    GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    double width = graphicsDevice.getDisplayMode().getWidth();
    double height = graphicsDevice.getDisplayMode().getHeight();

    public ReversiView(ReversiController controller) {
        _borderPane = new BorderPane();
        _controller = controller;

        _isFinished = false;

        setup();
    }

    private void setup() {
        _gameInfo = new VBox();
        _gameInfo.setMinHeight(height/10);
        _gameInfo.setMinWidth(width);
        _gameInfo.setAlignment(Pos.CENTER);
        _gameInfo.setId("gameInfo");


        _status = new Label();
        _status.setFont(new Font(45));
        _status.setTextFill(Color.TEAL);
        _status.setId("titleText");

        _errorStatus = new Label();
        _errorStatus.setId("errorStatus");

        countLabel = new Label();
        countLabel.setId("titleText");
        Timer timer = new Timer();
        counter = 10;
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                while (_controller.getIsYourTurn() == true){
                    Platform.runLater(()  -> {
                        countLabel.setText("" + counter);
                    });

                    System.out.println("Deze : " + counter);
                    counter = counter - 1;
                    if (counter < 1) {
                        //Einde beurt
                        //counter = 10;
                    }
                }
                counter = 10;

                if (_isFinished == true){
                    timer.cancel();
                }
            }
        };
        while (_controller.getIsYourTurn() == true)
            timer.scheduleAtFixedRate(task,1000,1000);


        _timer = new Label("10 Seconden");
        _timer.setId("timer");
        _timer.setFont(new Font(30));

        _gameInfo.getChildren().addAll(_status, _errorStatus, countLabel ,_timer);

        //Player 1
        _player1Info = new VBox(30);
        _player1Info.setMinWidth(width/10);
        _player1Info.setId("p1info");

        _name1 = new Label("Player:");
        _name1.getStyleClass().add("titles");

        _player1Name = new Label(_controller.player1Name());
        _player1Name.getStyleClass().add("players");

        _score1 = new Label("Score:");
        _score1.getStyleClass().add("titles");

        _player1Score = new Label();
        _player1Score.getStyleClass().add("players");

        _player1Color = new Label("Color:");
        _player1Color.getStyleClass().add("titles");

        _colorImage1 = new Button();
        _colorImage1.setMinHeight(100);
        _colorImage1.setMinWidth(100);
        System.out.println("Player 1 = " + _controller.getColorP1());
        char Testing = _controller.getColorP1();
        System.out.println("Testing = "+Testing);
        if (_controller.getColorP1() == 'w'){
            _colorImage1.setId("white");
            System.out.println("White: "+ Testing);
        } else if (_controller.getColorP1() == 'b'){
            _colorImage1.setId("black");
            System.out.println("Black: " + Testing);
        }

        //Player2
        _player2Info = new VBox(30);
        _player2Info.setAlignment(Pos.BASELINE_RIGHT);
        _player2Info.setMinWidth(width/10);
        _player2Info.setId("p2info");

        _name2 = new Label("Player:");
        _name2.getStyleClass().add("titles");

        _player2Name = new Label(_controller.player2Name());
        _player2Name.getStyleClass().add("players");
        _score2 = new Label("Score:");
        _score2.getStyleClass().add("titles");
        _player2Score = new Label();
        _player2Score.getStyleClass().add("players");
        _player2Color = new Label("Color:");
        _player2Color.getStyleClass().add("titles");

        _colorImage2 = new Button();
        _colorImage2.setMinHeight(100);
        _colorImage2.setMinWidth(100);
        if (_controller.getColorP1() == 'w'){
            _colorImage2.setId("black");
        } else if (_controller.getColorP1() == 'b'){
            _colorImage2.setId("white");
        }

        //Set
        _player1Info.getChildren().addAll(_name1, _player1Name, _score1, _player1Score, _player1Color, _colorImage1);
        _player2Info.getChildren().addAll(_name2, _player2Name, _score2,_player2Score, _player2Color, _colorImage2);

        _endButton = new Button("End game");
        _newGameButton = new Button("New game");
        _rageQuit = new Button("");
        _rageQuit.setId("rageQuit");
        _rageQuit.setMinWidth(172);
        _rageQuit.setMinHeight(100);
        _hanze = new Button("");
        _hanze.setId("Hanze");
        _hanze.setMinWidth(100);
        _hanze.setMinHeight(80);

        _pane = new GridPane();
        _pane.setAlignment(Pos.CENTER);
        _pane.setPadding(new Insets(20));

        _buttons = new HBox();

        _buttons.setSpacing(60.0);
        _buttons.setMinHeight(50);
        _buttons.setAlignment(Pos.CENTER);
        _buttons.getChildren().addAll(_newGameButton, _endButton, _hanze, _rageQuit);

        _borderPane.setTop(_gameInfo);
        _borderPane.setLeft(_player1Info);
        _borderPane.setRight(_player2Info);
        _borderPane.setCenter(_pane);
        _borderPane.setBottom(_buttons);

    }

    public void generateBoardVisual() {
        _pane.getChildren().clear();

        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                setCellImage(i, j);
    }

    public Button getEndButton() {
        return _endButton;
    }

    public void setCellImage(int x, int y) {
        String fileName;
        ImageView imageView = new ImageView();
        try {
            if (_board[x][y] != null) {
                switch (_board[x][y].getCharacter()) {
                    case 'b':
                        fileName = "black1.png";
                        break;
                    case 'w':
                        fileName = "white1.png";
                        break;
                    case 'p':
                        fileName = "possible1.png";
                        break;
                    default:
                        fileName = null;
                }

                if (fileName != null)
                    imageView.setImage(new Image(getClass().getClassLoader().getResource("com/rockingstar/modules/Reversi/" + fileName).toURI().toString()));
            }
            else
                imageView.setImage(new Image(getClass().getClassLoader().getResource("com/rockingstar/modules/Reversi/empty1.png").toURI().toString()));
        }
        catch (URISyntaxException | NullPointerException e) {
            Util.exit("Loading Reversi images");
        }

        Platform.runLater(() -> {
            if (_board[x][y] != null && _board[x][y].getCharacter() == 'p') {
                int tempX = x;
                int tempY = y;

                if (!(_controller.getPlayerToMove() instanceof AI)) {

                    imageView.setOnMousePressed(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            if (!_isFinished && _controller.getIsYourTurn()) {
                                _controller.doPlayerMove(tempX, tempY);
                                imageView.removeEventFilter(MouseEvent.MOUSE_CLICKED, this);
                            } else if (!_controller.getIsYourTurn())
                                _errorStatus.setText("It's not your turn.");
                        }
                    });
                }
            }
            _pane.add(imageView, x, y);
        });
    }

    public Button getNewGameButton() {
        return _newGameButton;
    }

    public Button getRageQuitButton(){
        return _rageQuit;
    }
    public Node getNode() {
        return _borderPane;
    }


    public void setBoard(Player[][] board) {
        _board = board;
    }

    public void setStatus(String status) {
        Platform.runLater(() -> _status.setText(status));
    }

    public void setErrorStatus(String errorStatus) {
        Platform.runLater(() -> _errorStatus.setText(errorStatus));
    }

    public void setIsFinished(boolean isFinished) {
        _isFinished = isFinished;
    }

    public Label getP1Score(){
        return _player1Score;
    }

    public Label getP2Score(){
        return _player2Score;
    }

}