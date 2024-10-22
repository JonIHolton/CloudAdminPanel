# Navigate to the directory containing the proto files
cd /app/protos

# Loop through all .proto files in the current directory
for %%F in (*.proto) do (
    # Generate Python files from the .proto file
    python -m grpc_tools.protoc -I. --python_out=/app --grpc_python_out=/app "%%F"
    
    echo %%F has been processed.
)

echo All proto files have been processed and Python files are generated in /app directory.
