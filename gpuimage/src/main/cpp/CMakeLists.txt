# Minimum required CMake version
cmake_minimum_required(VERSION 3.4.1)

# Create shared library 'yuv-decoder' from source file
add_library(
        gpu-image             # Library name
        SHARED                # Build type: shared (.so)
        gpu-image.c           # Source file
)

# Find Android NDK system library 'log'
find_library(
        log-lib                 # Variable name
        log                     # Library name in NDK
)

# Link target library with required system libs
target_link_libraries(
        gpu-image               # Our target native lib
        ${log-lib}              # Android log library
        GLESv2                  # OpenGL ES 2.0
        jnigraphics             # For Bitmap operations
)
