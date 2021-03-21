cmake_minimum_required(VERSION 3.7)
project(verifyGdml)

set(CMAKE_CXX_STANDARD 14)

find_package(Geant4 REQUIRED)

file(GLOB GDML "*.gdml")
file(COPY ${GDML} DESTINATION .)

add_executable(${PROJECT_NAME} main.cpp)

target_link_libraries(${PROJECT_NAME} ${Geant4_LIBRARIES})
