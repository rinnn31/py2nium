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

import android.graphics.Bitmap;

import io.py2nium.server.common.exceptions.InvalidInputException;

public class TakeScreenshotModel extends InputModel {
    public int quality = 100;

    public String format = "PNG";

    public float scale = 1.0f;

    public Integer[] cropArea;

    public String savePath;

    public int displayId = 0;

    @Override
    public void validate() {
        super.validate();
        if(displayId < 0) {
            throw new InvalidInputException("Display ID must be a non-negative integer");
        }

        if (quality < 0 || quality > 100) {
            throw new InvalidInputException("Quality must be between 0 and 100");
        }

        if(scale <= 0) {
            throw new InvalidInputException("Scale must be a positive number");
        }

        try {
            Bitmap.CompressFormat.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Format must be one of PNG, JPEG, WEBP_LOSS or WEBP_LOSSLESS");
        }

        if(cropArea != null && cropArea.length != 4) {
            throw new InvalidInputException("'crop_area' field must have exactly 4 values representing the coordinates of the area to be cut");
        }

    }

}
