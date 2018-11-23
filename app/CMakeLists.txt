# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.


#set( lib_build_DIR /Users/soultoxik/Downloads/dlib-19.13/build/dlib )
#file(MAKE_DIRECTORY ${lib_build_DIR})

# Adds the CMakeLists.txt file located in the specified directory
# as a build dependency.
#add_subdirectory( # Specifies the directory of the CMakeLists.txt file.
#                  ${lib_src_DIR}
#
#                  # Specifies the directory for the build outputs.
#                  ${lib_build_DIR} )

# Adds the output of the additional CMake build as a prebuilt static
# library and names it lib_gmath.
#add_library( lib_dlib STATIC IMPORTED )
#set_target_properties( lib_dlib PROPERTIES IMPORTED_LOCATION
#                       /Users/soultoxik/Downloads/dlib-19.13/build/install/lib/libdlib.a )
#include_directories( /Users/soultoxik/Downloads/dlib-19.13/build/install/include )

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib
                       #lib_dlib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )