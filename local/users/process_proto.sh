#!/bin/bash

# Navigate to the directory containing the proto files
cd /app/protos

# Loop through all .proto files in the current directory
for proto_file in *.proto; do
    # Check if the glob gets expanded to existing files.
    if [ -e "$proto_file" ]; then
        # Generate Python files from the .proto file
        python -m grpc_tools.protoc -I. --python_out=/app --grpc_python_out=/app "$proto_file"
        
        echo "$proto_file has been processed."
    else
        echo "No .proto files found."
        break  # Break the loop if no .proto files are found
    fi
done

echo "All proto files have been processed and Python files are generated in /app directory."
