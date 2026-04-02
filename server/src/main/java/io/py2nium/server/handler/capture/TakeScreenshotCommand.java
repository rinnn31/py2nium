/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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


package io.py2nium.server.handler.capture;

import static io.py2nium.server.utils.ModelConverter.toModelAndValidate;

import android.graphics.Rect;

import java.io.IOException;

import io.netty.handler.codec.http.HttpMethod;
import io.py2nium.server.handler.CommandHandler;
import io.py2nium.server.http.HttpReq;
import io.py2nium.server.http.Py2niumResponse;
import io.py2nium.server.model.api.TakeScreenshotModel;
import io.py2nium.server.utils.ScreenshotHelper;

public class TakeScreenshotCommand extends CommandHandler {
    public TakeScreenshotCommand(String uri, HttpMethod method) {
        super(uri, method);
    }

    @Override
    protected Py2niumResponse handle(HttpReq request) {
        TakeScreenshotModel model = toModelAndValidate(request.getBody(), TakeScreenshotModel.class);

        try {
            Rect cropArea = null;
            if(model.cropArea != null) {
                cropArea = new Rect(model.cropArea[0], model.cropArea[1], model.cropArea[2], model.cropArea[3]);
            }
            ScreenshotHelper.CompressParams compressParams = new ScreenshotHelper.CompressParams(model.format, model.quality, model.scale);
            if(model.savePath != null) {
                ScreenshotHelper.takeScreenshotAndSave(compressParams, cropArea, model.savePath, model.displayId);
                return new Py2niumResponse(Py2niumResponse.SUCCESS, null,null);
            } else {
                String encodedScreenshot = ScreenshotHelper.takeEncodedScreenshot(compressParams, cropArea, model.displayId);
                return new Py2niumResponse(Py2niumResponse.SUCCESS, encodedScreenshot, null);
            }
        } catch (IOException e) {
            return new Py2niumResponse(Py2niumResponse.INTERNAL_ERROR, null, e);
        }

    }



}
