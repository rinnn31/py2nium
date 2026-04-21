#include <android/bitmap.h>
#include "image_matcher.h"

std::vector<Point> matchImage(cv::Mat& src, cv::Mat& temp, bool returnBestOnly, double threshold) {
    std::vector<Point> results;
    cv::Mat result;
    cv::matchTemplate(src, temp, result, cv::TM_CCOEFF_NORMED);

    if (returnBestOnly) {
        double maxVal;
        cv::Point maxLoc;
        cv::minMaxLoc(result, nullptr, &maxVal, nullptr, &maxLoc);
        if (maxVal >= threshold) {
            results.emplace_back(maxLoc.x, maxLoc.y);
        }
    } else {
        for (int y = 2; y < result.rows - 2; y++) {
            for (int x = 2; x < result.cols - 2; x++) {
                float val = result.at<float>(y, x);

                if (val >= threshold) {
                    bool isMax = true;

                    for (int dy = -2; dy <= 2 && isMax; dy++) {
                        for (int dx = -2; dx <= 2; dx++) {
                            if (result.at<float>(y + dy, x + dx) > val) {
                                isMax = false;
                                break;
                            }
                        }
                    }

                    if (isMax) {
                        results.emplace_back(x, y);
                    }
                }
            }
        }
    }

    return results;
}