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
import com.rockingstar.modules.Reversi.controllers.ReversiController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
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

import java.net.URISyntaxException;

public class ReversiView {

    private BorderPane _borderPane;

    // Top
    private HBox _gameInfo;
    private Label _status;
    private Label _errorStatus;
    private Label _timer;

    // Left
    private VBox _player1Info;
    private Label _player1Name;
    private Label _player1Score;

    // Right
    private VBox _player2Info;
    private Label _player2Name;
    private Label _player2Score;

    // Center
    private GridPane _pane;

    // Bottom
    private HBox _buttons;
    private Button _endButton;
    private Button _newGameButton;

    private Player[][] _board;
    private ReversiController _controller;
    private boolean _isFinished;

    public ReversiView(ReversiController controller) {
        _borderPane = new BorderPane();
        _controller = controller;

        _isFinished = false;

        setup();
    }

    private void setup() {
        _gameInfo = new HBox();

        _status = new Label();
        _status.setFont(new Font(16));

        _errorStatus = new Label();
        _errorStatus.setFont(new Font(16));
        _errorStatus.setTextFill(Color.RED);

        _timer = new Label("10 Seconden");
        _timer.setFont(new Font(30));

        _gameInfo.getChildren().addAll(_status, _errorStatus, _timer);

        _player1Info = new VBox();
        _player2Info = new VBox();

        _player1Name = new Label("Player 1");
        _player1Name.setFont(new Font(16));
        _player1Score = new Label("15");
        _player1Score.setFont(new Font(16));

        _player2Name = new Label("Player 1");
        _player2Name.setFont(new Font(16));
        _player2Score = new Label("15");
        _player2Score.setFont(new Font(16));


        _player1Info.getChildren().addAll(_player1Name, _player1Score);
        _player2Info.getChildren().addAll(_player2Name, _player2Score);


        _endButton = new Button("End game");
        _newGameButton = new Button("New game");

        _pane = new GridPane();
        _pane.setAlignment(Pos.CENTER);
        _pane.setPadding(new Insets(20));

        _buttons = new HBox();
        _buttons.setSpacing(60.0);
        _buttons.setMinHeight(50);
        _buttons.setAlignment(Pos.CENTER);
        _buttons.getChildren().addAll(_newGameButton, _endButton);

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
                        fileName = "black.png";
                        break;
                    case 'w':
                        fileName = "white.png";
                        break;
                    case 'p':
                        fileName = "possible.png";
                        break;
                    default:
                        fileName = null;
                }

                if (fileName != null)
                    imageView.setImage(new Image(getClass().getClassLoader().getResource("com/rockingstar/modules/Reversi/" + fileName).toURI().toString()));
            }
            else
                imageView.setImage(new Image(getClass().getClassLoader().getResource("com/rockingstar/modules/Reversi/empty.png").toURI().toString()));
        }
        catch (URISyntaxException | NullPointerException e) {
            Util.exit("Loading Reversi images");
        }

        if (!(_controller.getPlayerToMove() instanceof AI)) {
            Platform.runLater(() -> {
                if (_board[x][y] != null && _board[x][y].getCharacter() == 'p') {
                    int tempX = x;
                    int tempY = y;

                    imageView.setOnMousePressed(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            if (_controller.getPlayerToMove() instanceof AI)
                                return;

                            if (!_isFinished && _controller.getIsYourTurn()) {
                                System.out.printf("Zeg makker. Dit is het veld op coordinaten (%d, %d)\n", x, y);
                                _controller.doPlayerMove(tempX, tempY);
                                imageView.removeEventFilter(MouseEvent.MOUSE_CLICKED, this);
                            }
                            else if (!_controller.getIsYourTurn())
                                _errorStatus.setText("It's not your turn.");
                        }
                    });
                }
                _pane.add(imageView, x, y);
            });
        }
    }

    public Button getNewGameButton() {
        return _newGameButton;
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
}
