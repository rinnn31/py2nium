#ifndef PY2NIUM_IMAGE_PREPROCESSING_H
#define PY2NIUM_IMAGE_PREPROCESSING_H

#include <jni.h>
#include <vector>
#include <string>
#include <opencv2/opencv.hpp>

struct Operation {
    std::string name;
    std::vector<std::string> params;

    Operation(std::string  name, const std::vector<std::string>& params)
        : name(std::move(name)), params(params) {}
};

void initProcessor();

std::vector<Operation> parsePreprocessSteps(JNIEnv *env, jobjectArray preprocessSteps);

cv::Mat preprocessImage(const cv::Mat& img, const std::vector<Operation>& preprocessSteps);

cv::Mat getBmpMat(JNIEnv* env, jobject bitmap);

cv::Mat getBmpMatFromPath(const std::string& path);

#endif //PY2NIUM_IMAGE_PREPROCESSING_H
