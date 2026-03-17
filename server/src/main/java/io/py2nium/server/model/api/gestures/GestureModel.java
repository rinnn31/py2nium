/*
* Apache 2.0 License
*
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


package io.py2nium.server.model.api.gestures;

import io.py2nium.server.common.annotation.RequiredField;
import io.py2nium.server.common.exceptions.InvalidInputException;
import io.py2nium.server.model.api.InputModel;

public class GestureModel extends InputModel {
    public static final String ACTION_TOUCH_UP = "touch_up";
    public static final String ACTION_TOUCH_DOWN = "touch_down";
    public static final String ACTION_TOUCH_MOVE = "touch_move";
    public static final String ACTION_WAIT = "wait";
    @RequiredField
    public String action;
    public Integer x;
    public Integer y;
    public Integer duration;

    @Override
    public void validate() {
        super.validate();
        switch (action) {
            case ACTION_TOUCH_UP:
            case ACTION_TOUCH_DOWN:
            case ACTION_TOUCH_MOVE:
                if (x == null || y == null) {
                    throw new InvalidInputException("'x' and 'y' fields are required for " + action);
                }
                break;
            case ACTION_WAIT:
                if (duration == null) {
                    throw new InvalidInputException("'duration' field is required for " + action);
                }
                break;
        }

    }
}
