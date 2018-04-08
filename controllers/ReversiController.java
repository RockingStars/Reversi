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

import com.rockingstar.engine.game.AbstractGame;
import com.rockingstar.engine.game.Player;
import com.rockingstar.modules.Reversi.models.ReversiModel;
import com.rockingstar.modules.Reversi.views.ReversiView;
import javafx.scene.Node;

public class ReversiController extends AbstractGame {

    private ReversiModel _model;
    private ReversiView _view;

    public ReversiController(Player player1, Player player2) {
        super(player1, player2);

        _model = new ReversiModel();
        _view = new ReversiView(this);
    }

    @Override
    public Node getView() {
        return null;
    }
}
