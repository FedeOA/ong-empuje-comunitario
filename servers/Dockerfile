FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Generar los archivos gRPC desde los .proto
RUN python -m grpc_tools.protoc -Iprotos --python_out=services_pb2 protos/*.proto \
 && python -m grpc_tools.protoc -Iprotos --grpc_python_out=services_pb2_grpc protos/*.proto

EXPOSE 50051

CMD ["python", "main.py"]
