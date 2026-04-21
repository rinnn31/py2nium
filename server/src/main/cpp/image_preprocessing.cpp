#include "image_preprocessing.h"

#include <functional>
#include <unordered_map>
#include <opencv2/opencv.hpp>
#include <android/bitmap.h>

using Handler = std::function<void(cv::Mat&, const std::vector<std::string>&)>;

std::unordered_map<std::string, Handler> handlers;

void initProcessor() {
    handlers["gray"] = [](cv::Mat& img, const std::vector<std::string>& params) {
        cv::cvtColor(img, img, cv::COLOR_BGR2GRAY);
    };
    handlers["blur"] = [](cv::Mat& img, const std::vector<std::string>& params) {
        int ksize = 5, sigmaX = 0;
        if (params.size() >= 1) ksize = std::stoi(params[0]);
        if (ksize % 2 == 0) ksize++; // Ensure ksize is odd

        if (params.size() >= 2) sigmaX = std::stoi(params[1]);
        cv::GaussianBlur(img, img, cv::Size(ksize, ksize), sigmaX);
    };
    handlers["scale"] = [](cv::Mat& img, const std::vector<std::string>& params) {
        double fx = 1.0, fy = 1.0;
        if (params.size() >= 1) fx = std::stod(params[0]);
        if (params.size() >= 2) fy = std::stod(params[1]);
        cv::resize(img, img, cv::Size(), fx, fy);
    };
    handlers["rotate"] = [](cv::Mat& img, const std::vector<std::string>& params) {
        double angle = 0;
        if (!params.empty()) angle = std::stod(params[0]);
        cv::Point2f center(img.cols / 2.0F, img.rows / 2.0F);
        cv::Mat rot = cv::getRotationMatrix2D(center, angle, 1.0);
        cv::warpAffine(img, img, rot, img.size());
    };
    handlers["flip"] = [](cv::Mat& img, const std::vector<std::string>& params) {
        int flipCode = 0;
        if (!params.empty()) flipCode = std::stoi(params[0]);
        cv::flip(img, img, flipCode);
    };
    handlers["crop"] = [](cv::Mat& img, const std::vector<std::string>& params) {
        if (params.size() < 4) return;
        int x = std::stoi(params[0]);
        int y = std::stoi(params[1]);
        int width = std::stoi(params[2]);
        int height = std::stoi(params[3]);
        if (x < 0 || y < 0 || width <= 0 || height <= 0 || x + width > img.cols || y + height > img.rows) {
            return; // Invalid crop parameters
        }
        cv::Rect roi(x, y, width, height);
        img = img(roi).clone();
    };
}

std::vector<Operation> parsePreprocessSteps(JNIEnv *env, jobjectArray preprocessSteps) {
    std::vector<Operation> operations;
    jsize stepCount = env->GetArrayLength(preprocessSteps);
    for (jsize i = 0; i < stepCount; ++i) {
        auto stepStr = (jstring) env->GetObjectArrayElement(preprocessSteps, i);
        const char* stepCStr = env->GetStringUTFChars(stepStr, nullptr);
        std::string step(stepCStr);
        env->ReleaseStringUTFChars(stepStr, stepCStr);
        env->DeleteLocalRef(stepStr);

        size_t pos = step.find(':');
        if (pos == std::string::npos) {
            operations.emplace_back(step, std::vector<std::string>{});
        } else {
            std::string name = step.substr(0, pos);
            std::vector<std::string> params;
            size_t start = pos + 1;
            while (start < step.size()) {
                size_t end = step.find(',', start);
                if (end == std::string::npos) end = step.size();
                params.push_back(step.substr(start, end - start));
                start = end + 1;
            }
            operations.emplace_back(name, params);
        }
    }
    return operations;
}

cv::Mat preprocessImage(const cv::Mat& img, const std::vector<Operation>& preprocessSteps) {
    cv::Mat result = img.clone();
    for (const auto& op : preprocessSteps) {
        auto it = handlers.find(op.name);
        if (it != handlers.end()) {
            it->second(result, op.params);
        }
    }
    return result;
}

cv::Mat getBmpMat(JNIEnv* env, jobject bitmap) {
    AndroidBitmapInfo info;
    void* pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) != ANDROID_BITMAP_RESULT_SUCCESS) {
        throw std::runtime_error("Failed to get bitmap info");
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        throw std::runtime_error("Bitmap format is not RGBA_8888");
    }
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        throw std::runtime_error("Failed to lock bitmap pixels");
    }

    cv::Mat mat(info.height, info.width, CV_8UC4, pixels);

    cv::Mat result;
    cvtColor(mat, result, cv::COLOR_RGBA2BGR);

    AndroidBitmap_unlockPixels(env, bitmap);

    return result.clone();
}

cv::Mat getBmpMatFromPath(const std::string& path) {
    cv::Mat img = cv::imread(path, cv::IMREAD_UNCHANGED);
    if (img.empty()) {
        return cv::Mat();
    }
    if (img.channels() == 4) {
        cv::cvtColor(img, img, cv::COLOR_BGRA2BGR);
    }
    return img;
}