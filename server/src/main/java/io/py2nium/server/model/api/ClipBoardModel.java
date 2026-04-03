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

import java.util.List;

import io.py2nium.server.common.annotation.RequiredField;
import io.py2nium.server.utils.service.WrappedClipboardManager;

public class ClipBoardModel extends InputModel {
    @RequiredField
    public String dataType;

    @RequiredField
    public String data;

    public ClipBoardModel(String dataType, String data) {
        this.dataType = dataType;
        this.data = data;
    }

    @Override
    public void validate() {
        super.validate();
        List<String> supportedType = WrappedClipboardManager.SupportedDataType.getSupportedType();
        if(!supportedType.contains(dataType)) {
            throw new IllegalArgumentException(String.format("This api only supports the following data types: %s",
                    String.join(", ", supportedType)));
        }
    }
}
