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
import io.py2nium.server.handler.CommandHandler;
import io.py2nium.server.http.HttpReq;
import io.py2nium.server.http.Py2niumResponse;
import io.py2nium.server.model.api.ExecuteShellModel;
import io.py2nium.server.utils.ShellRunner;

public class ExecuteShellCommand extends CommandHandler {
    public ExecuteShellCommand(String uri, HttpMethod method) {
        super(uri, method);
    }

    @Override
    protected Py2niumResponse handle(HttpReq request){

        ExecuteShellModel model = toModelAndValidate(request.getBody(), ExecuteShellModel.class);
        try {
            String result = ShellRunner.exec(model.cmd);
            return new Py2niumResponse(Py2niumResponse.SUCCESS, result, null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Py2niumResponse(Py2niumResponse.INTERNAL_ERROR, null, e);
        }
    }

}
