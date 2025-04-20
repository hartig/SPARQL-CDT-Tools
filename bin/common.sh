#!/bin/sh
# This script resolves CDT_TOOLS_HOME, locates the Java binary, and sets 
# the classpath to point to the correct JAR file.

# Function to resolve symbolic links and return the absolute path of a file
resolveLink() {
  local NAME=$1  # Assign the first argument (the file name) to a local variable
  # Loop to resolve symbolic links until the actual file is found
  while [ -L "$NAME" ]; do
    case "$OSTYPE" in
      # For macOS or BSD systems, resolve the path using dirname and basename
      darwin*|bsd*) NAME=$(cd "$(dirname "$NAME")" && pwd -P)/$(basename "$NAME") ;;
      # For Linux and other systems, use readlink to resolve the full path
      *) NAME=$(readlink -f "$NAME") ;;
    esac
  done
  # Output the resolved absolute path
  echo "$NAME"
}

# If CDT_TOOLS_HOME is not already set, resolve it based on the script's location
if [ -z "$CDT_TOOLS_HOME" ]; then
  # Resolve the absolute path of the current script
  SCRIPT=$(resolveLink "$0")
  # Set CDT_TOOLS_HOME to the parent directory of the script's directory
  CDT_TOOLS_HOME=$(cd "$(dirname "$SCRIPT")/.." && pwd)
  # Export CDT_TOOLS_HOME so it can be used in child processes
  export CDT_TOOLS_HOME
fi

# If JAVA is not set, locate the Java binary
if [ -z "$JAVA" ]; then
  # If JAVA_HOME is set, use it to locate the Java binary
  if [ -z "$JAVA_HOME" ]; then
    JAVA=$(which java)  # If JAVA_HOME is not set, fall back to finding java in the system PATH
  else
    JAVA="$JAVA_HOME/bin/java"  # Use JAVA_HOME to find the Java binary
  fi
fi

# If JAVA is still not set, print an error message and exit the script
if [ -z "$JAVA" ]; then
  echo "Cannot find a Java JDK."
  echo "Please set JAVA or JAVA_HOME and ensure java (>=Java 17) is in your PATH." 1>&2
  exit 1  # Exit the script with an error code
fi

# Look for the directory that is expected to contain the JAR file
if [ -d "${CDT_TOOLS_HOME}/libs/" ]; then
  # If the libs directory exists, use it
  CDT_TOOLS_JAR_DIR=${CDT_TOOLS_HOME}/libs/
elif [ -d "${CDT_TOOLS_HOME}/target/" ]; then
  # Otherwise, if target/ exists, use that one
  CDT_TOOLS_JAR_DIR=${CDT_TOOLS_HOME}/target/
else
  # Otherwise, print an error message
  echo "Cannot find the directory ${CDT_TOOLS_HOME}/target/"
  echo "Did you forget to compile the project?"
  exit 2  # Exit the script with an error code
fi

# After determining the directory, look for the JAR file in that directory, and ..
CDT_TOOLS_CP=$(printf "%s\n" ${CDT_TOOLS_JAR_DIR}SPARQL-CDT-Tools-*.jar | grep -vE '(-sources|-javadoc)\.jar')
# .. check that the JAR file is actually there
if [ ! -f ${CDT_TOOLS_CP} ]; then
  echo "Cannot find the JAR file in ${CDT_TOOLS_JAR_DIR}"
  exit 3  # Exit the script with an error code
fi
