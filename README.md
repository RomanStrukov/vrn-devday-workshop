# Humanrecognition

Android Project implements age, gender and distance recognition in realtime.

# Description

This repo contains blank project with all settings and camera configuration for developing simple human recognition app

With first start you will see just camera preview. 

You will need to complete implementations for 3 classes

```kotlin
AgeGenderDetector
FaceDetector
FaceProcessing
```

# Utilities reference

Project right from the start contains several utilities for setting up basic app functions like camera preview and permissions management

Permissions package contains simple utility for checking android camera and storage permissions.

Camera package contains logic for setting up camera parameters e.g. camera id, frame size and e.t.c.

OpencvFix package contains classes that override default OpenCV camera implementation. The reason we doing this is that default OpenCV camera does not support portrait orientation for camera preview. You can inspect code by yourself to see small difference between our and OpenCV implementations

In the assets folder you can find files that we need to run Tensor and OpenCV functionality

Feel free to ask questions about template for our project


