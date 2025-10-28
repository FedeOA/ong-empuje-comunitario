import os
import sys
import subprocess
import time
import grpc
from concurrent.futures import ThreadPoolExecutor

# Asegurar que los directorios de salida estén en el PYTHONPATH
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "services_pb2")))
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "services_pb2_grpc")))

# Compilar los archivos .proto y corregir importaciones
def compile_proto():
    proto_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "protos"))
    proto_files = ["user.proto", "authorize.proto", "event.proto", "donation.proto"]
    for proto_file in proto_files:
        proto_path = os.path.join(proto_dir, proto_file)
        if not os.path.exists(proto_path):
            print(f"❌ No se encontró el archivo .proto en: {proto_path}")
            return False
    
    pb2_output_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "services_pb2"))
    pb2_grpc_output_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "services_pb2_grpc"))
    os.makedirs(pb2_output_dir, exist_ok=True)
    os.makedirs(pb2_grpc_output_dir, exist_ok=True)
    
    command = [
        "python", "-m", "grpc_tools.protoc",
        "-I", "protos",
        f"--python_out={pb2_output_dir}",
        f"--grpc_python_out={pb2_grpc_output_dir}",
    ] + proto_files
    
    print("🔧 Compilando archivos .proto...")
    result = subprocess.run(command, cwd=os.path.abspath(os.path.dirname(__file__)), capture_output=True, text=True)
    if result.returncode != 0:
        print(f"❌ Error al compilar los archivos .proto (return code: {result.returncode})")
        print(f"Command executed: {' '.join(command)}")
        print(f"Error output: {result.stderr}")
        return False
    
    print("✅ Compilación exitosa")
    
    # Corregir importaciones en archivos *_pb2_grpc.py y *_pb2.py
    print("🔧 Corrigiendo importaciones en archivos gRPC y Protobuf...")
    for proto_file in proto_files:
        base_name = os.path.splitext(proto_file)[0]
        # Fix imports in *_pb2_grpc.py
        grpc_file = os.path.join(pb2_grpc_output_dir, f"{base_name}_pb2_grpc.py")
        if os.path.exists(grpc_file):
            with open(grpc_file, 'r', encoding='utf-8') as f:
                content = f.read()
            corrected_content = content
            for pf in proto_files:
                dep_base_name = os.path.splitext(pf)[0]
                corrected_content = corrected_content.replace(
                    f"import {dep_base_name}_pb2", f"import services_pb2.{dep_base_name}_pb2"
                )
            with open(grpc_file, 'w', encoding='utf-8') as f:
                f.write(corrected_content)
            print(f"✅ Corregido import en {grpc_file}")
        
        # Fix imports in *_pb2.py
        pb2_file = os.path.join(pb2_output_dir, f"{base_name}_pb2.py")
        if os.path.exists(pb2_file):
            with open(pb2_file, 'r', encoding='utf-8') as f:
                content = f.read()
            corrected_content = content
            for pf in proto_files:
                dep_base_name = os.path.splitext(pf)[0]
                corrected_content = corrected_content.replace(
                    f"import {dep_base_name}_pb2", f"import services_pb2.{dep_base_name}_pb2"
                )
            with open(pb2_file, 'w', encoding='utf-8') as f:
                f.write(corrected_content)
            print(f"✅ Corregido import en {pb2_file}")
    
    return True

from database.databaseManager import init_db
from services.UserService import UserService
from services.AuthService import AuthService
from services.DonationService import DonationService
from services.EventService import EventService
from services_pb2_grpc import authorize_pb2_grpc, user_pb2_grpc, donation_pb2_grpc, event_pb2_grpc

def serve():
    server = grpc.server(ThreadPoolExecutor(max_workers=10))
    user_pb2_grpc.add_UserServiceServicer_to_server(UserService(), server)
    authorize_pb2_grpc.add_AuthServiceServicer_to_server(AuthService(), server)
    donation_pb2_grpc.add_DonationServiceServicer_to_server(DonationService(), server)
    event_pb2_grpc.add_EventServiceServicer_to_server(EventService(), server)
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
    if not compile_proto():
        sys.exit(1)
    
    # ✅ Inicializamos la base de datos
    init_db()
    # ✅ Ejecutar el servidor gRPC
    serve()