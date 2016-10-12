cmake_minimum_required(VERSION 3.0.2)

project(kaa-demo CXX)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

add_executable(kaa-demo src/KaaDemo.cpp)

add_subdirectory(kaa)

target_link_libraries(kaa-demo kaacpp)

