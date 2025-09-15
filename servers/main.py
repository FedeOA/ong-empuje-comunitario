import os
import sys
import time
import grpc
from concurrent import futures
import subprocess

# ✅ Asegurar que 'servers/' esté en el PYTHONPATH
sys.path.insert(0, os.path.abspath(os.path.dirname(__file__)))

# ✅ Compilar el archivo .proto si es necesario
def compile_proto():
    proto_path = os.path.join(os.path.dirname(__file__), "servers.proto")
    if not os.path.exists(proto_path):
        print(f"❌ No se encontró el archivo .proto en: {proto_path}")
        return False
    comand = [
        "python", "-m", "grpc_tools.protoc",
        "-I", ".",
        "--python_out=.",
        "--grpc_python_out=.",
        "servers.proto"
    ]
    print("🔧 Compilando archivo .proto...")
    resultado = subprocess.run(comand, cwd=os.path.dirname(__file__))
    if resultado.returncode == 0:
        print("✅ Compilación exitosa")
        return True
    else:
        print("❌ Error al compilar el archivo .proto")
        return False

from servers.db.dbManager import init_db
from servers.services.UserService import UserService
from servers.services.AuthService import AuthService
from servers.services.DonationService import DonationService
from servers.services.EventService import EventService
import servers_pb2_grpc

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    servers_pb2_grpc.add_UserServiceServicer_to_server(UserService(), server)
    servers_pb2_grpc.add_AuthServiceServicer_to_server(AuthService(), server)
    servers_pb2_grpc.add_DonationServiceServicer_to_server(DonationService(), server)
    servers_pb2_grpc.add_EventServiceServicer_to_server(EventService(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    print("✅ Servidor gRPC iniciado en puerto 50051")
    try:
        while True:
            time.sleep(86400)
    except KeyboardInterrupt:
        server.stop(0)
        print("⏹ Servidor detenido manualmente")

# 🧩 Flujo completo
if __name__ == "__main__":
    if compile_proto():
        
        # ✅ Inicializamos la base de datos
        init_db()
        # ✅ Ejecutar el servidor gRPC
        serve()
