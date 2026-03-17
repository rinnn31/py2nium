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


package io.py2nium.server.model.api;

import io.py2nium.server.common.annotation.RequiredField;
import io.py2nium.server.common.exceptions.InvalidInputException;

public class RotationModel extends InputModel {
    @RequiredField
    public Integer rotation;

    public Integer displayId = 0;

    @Override
    public void validate() {
        super.validate();
        if(rotation < -2 || rotation > 360 || rotation % 90 != 0) {
            throw new InvalidInputException("Rotation must be a multiple of 90 degrees and between 0 " +
                    "and 360 or -1 for freeze screen  or -2 for unfreeze screen.");
        }
    }
}
