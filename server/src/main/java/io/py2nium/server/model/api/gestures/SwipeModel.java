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

import io.py2nium.server.common.annotation.RequiredField;
import io.py2nium.server.common.exceptions.InvalidInputException;
import io.py2nium.server.model.api.InputModel;

public class SwipeModel extends InputModel {
    @RequiredField
    public Integer startX;

    @RequiredField
    public Integer startY;

    @RequiredField
    public Integer endX;

    @RequiredField
    public Integer endY;

    public Integer speed;

    public Integer displayId;

    public Boolean isDragMode = false;

    public Point getStartPoint() {
        return new Point(startX, startY);
    }
    public Point getEndPoint() {
        return new Point(endX, endY);
    }

    @Override
    public void validate() {
        super.validate();
        if(speed != null && speed <0 ) {
            throw new InvalidInputException("Field 'speed' value cannot be negative");
        }
    }


}

