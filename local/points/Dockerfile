# Start with a Python slim base image
FROM python:3.9-slim

# Avoid prompts from apt
ENV DEBIAN_FRONTEND=noninteractive

# Set timezone (required for some configurations)
ENV TZ=Asia/Singapore
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Install OCRmyPDF, Tesseract OCR, Protocol Buffers Compiler, and other dependencies
RUN apt-get update && \
    apt-get install -y \
    protobuf-compiler \
    libpq-dev \
    gcc\
    python3-dev\
    dos2unix \
    && apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Install gRPC tools for Python
RUN pip install grpcio grpcio-tools protobuf Faker

# Set the working directory in the container
WORKDIR /app

# Copy the requirements file and install Python dependencies
COPY requirements.txt /app/
RUN pip install --no-cache-dir -r requirements.txt

# Copy the application files and scripts into the container
COPY ./src /app
# this is to fix the windows adding in different line endings in the script file
COPY process_proto.sh /app/
# COPY migrations.sh /app/


# Ensure the script is executable
RUN chmod +x /app/process_proto.sh
RUN chmod +x /app/process_proto.sh

RUN dos2unix /app/process_proto.sh
# RUN dos2unix /app/migrations.sh
# # Compile the proto files to generate Python code using the script
# # RUN /app/process_proto.sh
RUN bash /app/process_proto.sh
# RUN bash /app/migrations.sh

# Set the default command to run the app
CMD ["python3", "-m", "server"]
# ENTRYPOINT ["sh", "-c", "echo $JAVA_HOME && while true; do sleep 1; done"]
