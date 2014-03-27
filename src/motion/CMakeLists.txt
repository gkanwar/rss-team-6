# Standard catkin header things
cmake_minimum_required(VERSION 2.8.3)
project(motion)

# Build-time dependencies
find_package(catkin REQUIRED
  rosjava_build_tools
  rosjava_messages
  rss_msgs
  orc_utils
)

# Runtime dependencies
catkin_package(CATKIN_DEPENDS
  rss_msgs
  orc_utils
)