# Standard catkin header things
cmake_minimum_required(VERSION 2.8.3)
project(gui_msgs)

# Package's build-time dependencies
find_package(catkin REQUIRED rosjava_build_tools)
find_package(catkin REQUIRED
  COMPONENTS message_generation std_msgs)

# Declare message files to build
add_message_files(FILES
  ColorMsg.msg
  GUIEraseMsg.msg
  GUIGraphMsg.msg
  GUILineMsg.msg
  GUIParticleCloudMsg.msg
  GUIPathMsg.msg
  GUIPointMsg.msg
  GUIPolyMsg.msg
  GUIRectMsg.msg
  GUISegmentMsg.msg
  GUIPolyMsg.msg
  GUIRectMsg.msg
  PointDataMsg.msg
  PointMappingMsg.msg
)

# Actually generate language-specific message files
generate_messages(DEPENDENCIES
  std_msgs)

# Runtime dependencies
catkin_package(
  CATKIN_DEPENDS message_runtime std_msgs
)