# Standard catkin header things
cmake_minimum_required(VERSION 2.8.3)
project(lab6_msgs)

# Package's build-time dependencies
find_package(catkin REQUIRED rosjava_build_tools)
find_package(catkin REQUIRED
  COMPONENTS message_generation lab5_msgs)

# Declare message files to build
add_message_files(FILES
  GUIPolyMsg.msg
  GUIRectMsg.msg
)

# Actually generate language-specific message files
generate_messages(DEPENDENCIES
  lab5_msgs)

# Runtime dependencies
catkin_package(
  CATKIN_DEPENDS message_runtime lab5_msgs
)