package io.py2nium.server.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import io.py2nium.server.model.api.MatchImageModel;
import io.py2nium.server.model.api.MatchListImagesModel;
import io.py2nium.server.model.api.MatchResultModel;

public class ImageLocator {
    public static MatchResultModel[] matchImage(MatchImageModel model) {
        String[] templateCvProcessingCodes = model.templateCvProcessingCodes != null ? model.templateCvProcessingCodes.toArray(new String[0]) : new String[0];
        String[] screenCvProcessingCodes = model.screenCvProcessingCodes != null ? model.screenCvProcessingCodes.toArray(new String[0]) : new String[0];

        Bitmap sourceBitmap;
        if (model.encodedImage != null) {
            byte[] imageBytes = Base64.decode(model.encodedImage, Base64.DEFAULT);
            sourceBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } else {
            sourceBitmap = ScreenshotHelper.takeScreenshot(null, model.captureDisplayId);
        }

        Bitmap templateBitmap = BitmapFactory.decodeFile(model.imagePath);

        String result = matchImage(sourceBitmap, templateBitmap, model.findFirstMatchOnly, model.threshold, screenCvProcessingCodes, templateCvProcessingCodes);
        if (result != null && result.contains("ERROR(")) {
            throw new RuntimeException(result);
        }

        String[] resultArray = result.split("\\|");
        MatchResultModel[] matchResults = new MatchResultModel[resultArray.length];
        for (int i = 0; i < resultArray.length; i++) {
            String[] parts = resultArray[i].split(",");
            MatchResultModel matchResult = new MatchResultModel();
            matchResult.x = Integer.parseInt(parts[0]);
            matchResult.y = Integer.parseInt(parts[1]);
            matchResult.width = Integer.parseInt(parts[2]);
            matchResult.height = Integer.parseInt(parts[3]);
            matchResults[i] = matchResult;
        }

        return matchResults;
    }

    public static MatchResultModel matchListImages(MatchListImagesModel model) {
        String[] templateCvProcessingCodes = model.templateCvProcessingCodes != null ? model.templateCvProcessingCodes.toArray(new String[0]) : new String[0];
        String[] screenCvProcessingCodes = model.screenCvProcessingCodes != null ? model.screenCvProcessingCodes.toArray(new String[0]) : new String[0];

        Bitmap sourceBitmap = ScreenshotHelper.takeScreenshot(null, model.captureDisplayId);

        String result = matchListImages(sourceBitmap, model.imagePaths, model.threshold, screenCvProcessingCodes, templateCvProcessingCodes);
        if (result != null && result.contains("ERROR(")) {
            throw new RuntimeException(result);
        }

        if (result.isEmpty()) {
            return null;
        }

        String[] parts = result.split(",");
        MatchResultModel matchResult = new MatchResultModel();
        matchResult.imagePath = parts[0];
        matchResult.x = Integer.parseInt(parts[1]);
        matchResult.y = Integer.parseInt(parts[2]);
        matchResult.width = Integer.parseInt(parts[3]);
        matchResult.height = Integer.parseInt(parts[4]);

        return matchResult;
    }

    private static native String matchImage(Bitmap sourceBitmap, Bitmap templateBitmap, boolean findFirst, double threshold, String[] screenCvProcessingCodes, String[] templateCvProcessingCodes);

    private static native String matchListImages(Bitmap sourceBitmap, String[] templateImagePaths, double threshold, String[] screenCvProcessingCodes, String[] templateCvProcessingCodes);
}
