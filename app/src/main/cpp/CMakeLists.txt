cmake_minimum_required(VERSION 3.4.1)

file(GLOB bzip bzip/*.c)

add_library(
        native-lib

        SHARED

        native-lib.cpp

        ${bzip}

        bspatch.c
)

include_directories(bzip)

find_library(
        log-lib

        log)

target_link_libraries(
        native-lib

        ${log-lib})