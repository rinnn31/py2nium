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

public class EventWaitModel extends InputModel {
    @RequiredField
    public Integer eventId;

    @RequiredField
    public Integer timeout;

    @Override
    public void validate() {
        super.validate();
        if(timeout != null && timeout < 0) {
            throw new InvalidInputException("Field 'timeout' value cannot be negative");
        }
    }
}
