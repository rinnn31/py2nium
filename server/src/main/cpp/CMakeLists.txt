cmake_minimum_required(VERSION 3.22.1)

project("py2nium-jni")

add_library(opencv SHARED IMPORTED)

set_target_properties(opencv PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libopencv_java4.so
)

target_include_directories(${CMAKE_PROJECT_NAME}
        PRIVATE
        ${CMAKE_SOURCE_DIR}/includes
)
target_link_libraries(${CMAKE_PROJECT_NAME}
    opencv
    android
    jnigraphics
    log
    c++)
