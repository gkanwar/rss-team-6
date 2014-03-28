# Standard catkin header things
cmake_minimum_required(VERSION 2.8.3)
project(rss_msgs)

# Package's build-time dependencies
find_package(catkin REQUIRED rosjava_build_tools)
find_package(catkin REQUIRED
  COMPONENTS message_generation)

# Declare message files to build
add_message_files(FILES
  AnalogStatusMsg.msg
  ArmMsg.msg
  BallLocationMsg.msg
  BreakBeamMsg.msg
  BumpMsg.msg
  DigitalStatusMsg.msg
  EncoderMsg.msg
  MotionMsg.msg
  OdometryMsg.msg
  PositionTargetMsg.msg
  ResetMsg.msg
  ReverseMsg.msg
  SonarMsg.msg
)

# Actually generate language-specific message files
generate_messages()

# Runtime dependencies
catkin_package(
  CATKIN_DEPENDS message_runtime
)
