# Start with a Python slim base image
FROM python:3.9-slim

# Avoid prompts from apt
ENV DEBIAN_FRONTEND=noninteractive

# Set timezone (required for some configurations)
ENV TZ=Asia/Singapore
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Set the working directory in the container
WORKDIR /app

# Copy the requirements file and install Python dependencies
COPY requirements.txt /app/
RUN pip install --no-cache-dir -r requirements.txt

# Copy the application files and scripts into the container
COPY ./src /app

# Set the default command to run the app
CMD ["python3", "-m", "server"]
# ENTRYPOINT ["sh", "-c", "echo $JAVA_HOME && while true; do sleep 1; done"]
