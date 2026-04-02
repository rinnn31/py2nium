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


package io.py2nium.server.utils;

import static android.util.DisplayMetrics.DENSITY_DEFAULT;

import android.app.UiAutomation;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.py2nium.server.common.exceptions.ScreenshotException;
import io.py2nium.server.core.UiAutomatorWrapper;
import io.py2nium.server.model.AndroidDevice;

public class ScreenshotHelper {
    private static final int PNG_MAGIC_LENGTH = 8;

    private static byte[] compress(final Bitmap bitmap, CompressParams compressParams) {
        Bitmap resultBitmap;
        if (Math.abs(compressParams.scale - 1.0f) < Float.MIN_NORMAL) {
            resultBitmap = bitmap;
        } else {
            int width = Math.round(bitmap.getWidth() * compressParams.scale);
            int height = Math.round(bitmap.getHeight() * compressParams.scale);
            resultBitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    width,
                    height,
                    true
            );
            bitmap.recycle();
        }

        try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            if (!resultBitmap.compress(compressParams.format,compressParams.quality, stream)) {
                throw new ScreenshotException("Failed to compress screenshot as "+compressParams.format);
            }
            return stream.toByteArray();
        } catch (IOException e) {
            throw new ScreenshotException("Failed to compress screenshot as "+compressParams.format, e);
        }
    }

    private static Bitmap crop(Bitmap bitmap, Rect cropArea) {
        final Rect bitmapRect = new Rect(
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight()
        );
        final Rect intersectionRect = new Rect();

        if (!intersectionRect.setIntersect(bitmapRect, cropArea)) {
            throw new ScreenshotException("Cannot crop screenshot to the specified area");
        }

        return Bitmap.createBitmap(
                bitmap,
                intersectionRect.left,
                intersectionRect.top,
                intersectionRect.width(),
                intersectionRect.height()
        );
    }
    private static Bitmap takeDeviceScreenshot(@Nullable Integer displayId) {
        if(displayId == null || displayId < 0) {
            displayId = 0;
        }

        UiAutomation uiAutomation = UiAutomatorWrapper.getUiAutomation();
        Display display = AndroidDevice.getInstance().getDisplayById(displayId);
        if(display == null) {
            throw new ScreenshotException("Cannot find display with id: " + displayId);
        }
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        Bitmap screenshot = null;
        if(metrics.densityDpi == DENSITY_DEFAULT) {
            ParcelFileDescriptor pfd = null;
            try {
                pfd = uiAutomation.executeShellCommand("screencap -p -d " + displayId);
                InputStream is = new ParcelFileDescriptor.AutoCloseInputStream(pfd);

                byte[] pngBytes = StringUtils.inputStreamToByteArray(is);
                if(pngBytes.length <= PNG_MAGIC_LENGTH) {
                    throw new ScreenshotException("screencap returned an invalid response");
                }

                screenshot = BitmapFactory.decodeByteArray(pngBytes, 0, pngBytes.length);
            } catch (Exception e) {
                Logger.e(ScreenshotHelper.class, "An error occurred while taking screenshot", e);
            } finally {
                if(pfd != null) {
                    try {
                        pfd.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if(screenshot == null) {
            Logger.i(ScreenshotHelper.class, "Falling back to UiAutomation-based screenshot. Display id will be ignored");
            screenshot = uiAutomation.takeScreenshot();
        }

        if(screenshot == null || screenshot.getWidth() == 0 || screenshot.getHeight() == 0) {
            throw new ScreenshotException("Failed to take screenshot");
        }

        Logger.d(ScreenshotHelper.class, String.format("Got screenshot with resolution: %sx%s", screenshot.getWidth(), screenshot.getHeight()));

        return screenshot;
    }

    public static Bitmap takeScreenshot(@Nullable Rect cropArea, @Nullable Integer displayId) {
        if(displayId == null || displayId < 0) {
            displayId = 0;
        }

        Bitmap screenshot = takeDeviceScreenshot(displayId);
        Bitmap processedScreenshot = screenshot;
        if(cropArea != null) {
            processedScreenshot = crop(screenshot, cropArea);
            screenshot.recycle();
        }

        return processedScreenshot;
    }

    public static void takeScreenshotAndSave(CompressParams compressParams, @Nullable Rect cropArea, String path, @Nullable Integer displayId) throws IOException {
        Bitmap screenshot = takeScreenshot(cropArea, displayId);

        // check file exist
        File file = new File(path);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Logger.e(ScreenshotHelper.class, "An error occurred while creating file: " + path, e);
                throw e;
            }
        }

        try (OutputStream os = new FileOutputStream(file)) {
            os.write(compress(screenshot, compressParams));
        } catch (IOException e) {
            Logger.e(ScreenshotHelper.class, "An error occurred while saving screenshot to file: " + path, e);
            throw e;
        } finally {
            screenshot.recycle();
        }
    }

    public static String takeEncodedScreenshot(CompressParams compressParams, @Nullable Rect cropArea, @Nullable Integer displayId) {
        Bitmap screenshot = takeScreenshot(cropArea, displayId);
        byte[] compressed = compress(screenshot, compressParams);
        screenshot.recycle();
        return Base64.encodeToString(compressed, Base64.NO_WRAP);
    }

    public static class CompressParams {
        public final Bitmap.CompressFormat format;
        public final int quality;
        public final float scale;

        public CompressParams(String format, int quality, float scale) {
            this.format = Bitmap.CompressFormat.valueOf(format.toUpperCase());
            this.quality = quality;
            this.scale = scale;
        }
    }
}
