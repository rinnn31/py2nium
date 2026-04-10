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


package io.py2nium.server.handler.apps;

import static io.py2nium.server.utils.ModelConverter.toModelAndValidate;

import android.content.pm.PackageManager;

import io.netty.handler.codec.http.HttpMethod;
import io.py2nium.server.handler.CommandHandler;
import io.py2nium.server.http.HttpReq;
import io.py2nium.server.http.Py2niumResponse;
import io.py2nium.server.model.GetAppInfoModel;
import io.py2nium.server.model.api.AppInfo;
import io.py2nium.server.utils.service.WrappedPackageManager;

public class GetApplicationInfoCommand extends CommandHandler {
    public GetApplicationInfoCommand(String uri, HttpMethod... methods ) {
        super(uri, methods);
    }

    @Override
    protected Py2niumResponse handle(HttpReq request) {
        GetAppInfoModel model = toModelAndValidate(request.getBody(), GetAppInfoModel.class);
        try {
            AppInfo appInfo = WrappedPackageManager.getApplicationInfo(model.packageName, model.shouldDumpInfo);
            return new Py2niumResponse(Py2niumResponse.SUCCESS, appInfo, null);
        } catch (PackageManager.NameNotFoundException e) {
            return new Py2niumResponse(Py2niumResponse.INTERNAL_ERROR, null, e);
        }

    }
}
