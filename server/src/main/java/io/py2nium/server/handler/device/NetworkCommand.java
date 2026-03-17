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


package io.py2nium.server.handler.device;

import static io.py2nium.server.utils.ModelConverter.toModelAndValidate;

import io.netty.handler.codec.http.HttpMethod;
import io.py2nium.server.common.exceptions.Py2niumException;
import io.py2nium.server.handler.CommandHandler;
import io.py2nium.server.http.HttpReq;
import io.py2nium.server.http.Py2niumResponse;
import io.py2nium.server.model.api.NetworkModel;
import io.py2nium.server.utils.StringUtils;
import io.py2nium.server.utils.service.WrappedNetworkManager;

public class NetworkCommand extends CommandHandler {
    public NetworkCommand(String uri, HttpMethod method) {
        super(uri, method);
    }

    @Override
    protected Py2niumResponse handle(HttpReq request) {
        NetworkModel model = toModelAndValidate(request.getBody(), NetworkModel.class);

        try {
            if(model.action.equals(NetworkModel.ACTION_QUERY)) {
                boolean res = false;
                switch (model.networkType) {
                    case NetworkModel.WIFI:
                        res = WrappedNetworkManager.isWifiEnabled();
                        break;
                    case NetworkModel.MOBILE_DATA:
                        res = WrappedNetworkManager.isMobileDataEnabled();
                        break;
                    case NetworkModel.AIRPLANE:
                        res = WrappedNetworkManager.isAirplaneModeEnabled();
                        break;
                }
                return new Py2niumResponse(Py2niumResponse.SUCCESS, res, null);
            } else {
                switch (model.networkType) {
                    case NetworkModel.WIFI:
                        boolean operationResult = WrappedNetworkManager.setWifiEnabled(model.enabled);
                        if (operationResult && model.enabled && !StringUtils.isNullOrEmpty(model.wifiSsid)) {
                            operationResult = WrappedNetworkManager.connectWifi(model.wifiSsid, model.wifiPassword);
                        }
                        return new Py2niumResponse(Py2niumResponse.SUCCESS, operationResult, null);

                    case NetworkModel.AIRPLANE:
                        return new Py2niumResponse(Py2niumResponse.SUCCESS, WrappedNetworkManager.toggleAirplaneMode(model.enabled), null);

                    case NetworkModel.MOBILE_DATA:
                        return new Py2niumResponse(Py2niumResponse.SUCCESS, WrappedNetworkManager.toggleMobileData(model.enabled), null);

                    default:
                        return new Py2niumResponse(Py2niumResponse.INTERNAL_ERROR, null, new Py2niumException("Invalid network type"));
                }
            }
        } catch (Exception e) {
            return new Py2niumResponse(Py2niumResponse.INTERNAL_ERROR, null, e);
        }
    }

}
