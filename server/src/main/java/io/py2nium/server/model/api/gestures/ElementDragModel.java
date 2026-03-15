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

import android.graphics.Point;

import io.py2nium.server.common.exceptions.InvalidInputException;
import io.py2nium.server.model.api.InputModel;

public class ElementDragModel extends InputModel {
    public String targetElement;
    public Integer destX;
    public Integer destY;

    public Integer speed;

    public Point toPoint() {
        return new Point(destX,destY);
    }

    @Override
    public void validate() {
        super.validate();
        if(targetElement == null && (destX == null || destY == null)) {
            throw new InvalidInputException("Either 'target_element' or 'destX' and 'destY' must be provided");
        }
        if(speed != null && speed < 0) {
            throw new InvalidInputException("Field 'speed' value cannot be negative");
        }
    }
}
