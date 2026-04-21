#include <jni.h>

#include "image_preprocessing.h"
#include "image_matcher.h"

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* reserved)
{
    // Init processor handlers
    initProcessor();
    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_io_py2nium_server_utils_ImageLocator_matchImage(JNIEnv* env, jclass clazz, jobject srcBitmap, jobject tempBitmap, jboolean returnBestOnly, jdouble threshold, jobjectArray srcPreprocessSteps, jobjectArray tempPreprocessSteps) {
    try {
        cv::Mat src = getBmpMat(env, srcBitmap);
        cv::Mat temp = getBmpMat(env, tempBitmap);
        auto srcProcessSteps = parsePreprocessSteps(env, srcPreprocessSteps);
        auto tempProcessSteps = parsePreprocessSteps(env, tempPreprocessSteps);
        src = preprocessImage(src, srcProcessSteps);
        temp = preprocessImage(temp, tempProcessSteps);

        std::vector <Point> results = matchImage(src, temp, returnBestOnly, threshold);

        std::string resultsStr;
        for (const auto &point: results) {
            resultsStr += std::to_string(point.x) + "," + std::to_string(point.y) + std::to_string(temp.cols) + "," + std::to_string(temp.rows) + "|";
        }
        if (!resultsStr.empty()) {
            resultsStr.pop_back(); // Remove the last '|'
        }

        return env->NewStringUTF(resultsStr.c_str());
    } catch (const std::exception &e) {
        std::string errorMsg = "ERROR(\"" + std::string(e.what()) + "\")";
        return env->NewStringUTF(errorMsg.c_str());
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_io_py2nium_server_utils_ImageLocator_matchListImages(JNIEnv* env, jclass clazz, jobject srcBitmap, jobjectArray tempBitmaps, jdouble threshold, jobjectArray srcPreprocessSteps, jobjectArray tempPreprocessSteps) {

    try {
        auto srcProcessSteps = parsePreprocessSteps(env, srcPreprocessSteps);
        auto tempProcessSteps = parsePreprocessSteps(env, tempPreprocessSteps);
        cv::Mat src = getBmpMat(env, srcBitmap);
        src = preprocessImage(src, srcProcessSteps);

        jsize tempCount = env->GetArrayLength(tempBitmaps);
        for (jsize i = 0; i < tempCount; ++i) {
            auto bitmapPath = (jstring) env->GetObjectArrayElement(tempBitmaps, i);
            const char* pathChars = env->GetStringUTFChars(bitmapPath, nullptr);
            std::string pathStr(pathChars);

            cv::Mat temp = getBmpMatFromPath(pathStr);
            temp = preprocessImage(temp, tempProcessSteps);

            std::vector <Point> results = matchImage(src, temp, true, threshold);
            if (results.empty()) {
                env->ReleaseStringUTFChars(bitmapPath, pathChars);
                env->DeleteLocalRef(bitmapPath);
                continue;
            }

            std::string resultStr = pathStr + "," + std::to_string(results[0].x) + "," +
                                    std::to_string(results[0].y) + "," + std::to_string(temp.cols) + "," + std::to_string(temp.rows);
            env->ReleaseStringUTFChars(bitmapPath, pathChars);
            env->DeleteLocalRef(bitmapPath);
            return env->NewStringUTF(resultStr.c_str());
        }

        return env->NewStringUTF("");
    } catch (const std::exception &e) {
        std::string errorMsg = "ERROR(\"" + std::string(e.what()) + "\")";
        return env->NewStringUTF(errorMsg.c_str());
    }
}