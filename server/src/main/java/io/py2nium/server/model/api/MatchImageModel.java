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

package io.py2nium.server.model.api;

import java.util.List;

public class MatchImageModel extends InputModel {
    public String encodedImage;

    public String imagePath;

    public Double threshold = 0.8;

    public Integer captureDisplayId = 0;

    public Boolean findFirstMatchOnly = true;
    public List<String> templateCvProcessingCodes;
    public List<String> screenCvProcessingCodes;

    @Override
    public void validate() {
        if (encodedImage == null && imagePath == null) {
            throw new IllegalArgumentException("Either encodedImage or imagePath must be provided");
        }
        if (threshold < 0 || threshold > 1) {
            throw new IllegalArgumentException("Threshold must be between 0 and 1");
        }
        if (captureDisplayId < 0) {
            throw new IllegalArgumentException("captureDisplayId must be non-negative");
        }
    }
}
