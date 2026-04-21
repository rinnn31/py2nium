#ifndef PY2NIUM_IMAGE_MATCHER_H
#define PY2NIUM_IMAGE_MATCHER_H

#include <jni.h>
#include <utility>
#include <vector>
#include <string>
#include <opencv2/opencv.hpp>

struct Point {
    int x;
    int y;

    Point(int x, int y) : x(x), y(y) {}
};

std::vector<Point> matchImage(cv::Mat& src, cv::Mat& temp, bool returnBestOnly, double threshold);

#endif //PY2NIUM_IMAGE_MATCHER_H
