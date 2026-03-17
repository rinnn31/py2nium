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

public class NetworkModel extends InputModel {
    public static final String WIFI = "wifi";
    public static final String MOBILE_DATA = "mobile_data";
    public static final String AIRPLANE = "airplane";
    public static final String ACTION_QUERY = "query";
    public static final String ACTION_SET = "set";

    @RequiredField
    public String networkType;

    @RequiredField
    public String action;

    public Boolean enabled;

    public String wifiSsid;

    public String wifiPassword;

    @Override
    public void validate() {
        super.validate();
        if(!networkType.equals(WIFI) && !networkType.equals(MOBILE_DATA) && !networkType.equals(AIRPLANE)) {
            throw new InvalidInputException(String.format("Network type must be either %s", String.join(",", WIFI, MOBILE_DATA, AIRPLANE)));
        }
        if(!action.equals(ACTION_QUERY) && !action.equals(ACTION_SET)) {
            throw new InvalidInputException(String.format("Action must be either %s", String.join("," , ACTION_QUERY, ACTION_SET)));
        }
    }
}
