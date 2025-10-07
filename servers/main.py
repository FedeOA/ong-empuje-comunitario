# main.py
import os
import sys
import subprocess
import time
import glob

# Ensure the virtual environment is activated
def get_venv_python():
    """Returns the path to the Python executable in the virtual environment."""
    print("DEBUG: Checking for virtual environment Python executable")
    if sys.platform == "win32":
        venv_python = os.path.join(os.path.dirname(__file__), "venv", "Scripts", "python.exe")
    else:
        venv_python = os.path.join(os.path.dirname(__file__), "venv", "bin", "python")
    
    if not os.path.exists(venv_python):
        print(f"ERROR: Virtual environment Python executable not found at: {venv_python}")
        print("Please create a virtual environment with: python -m venv venv")
        sys.exit(1)
    print(f"DEBUG: Found virtual environment Python at: {venv_python}")
    return venv_python

def create_venv_if_missing():
    """Create a virtual environment if it doesn't exist."""
    venv_dir = os.path.join(os.path.dirname(__file__), "venv")
    if not os.path.exists(venv_dir):
        print("INFO: Creating virtual environment...")
        try:
            subprocess.run([sys.executable, "-m", "venv", "venv"], check=True)
            print("SUCCESS: Virtual environment created successfully.")
        except subprocess.CalledProcessError as e:
            print(f"ERROR: Failed to create virtual environment: {e.stderr}")
            sys.exit(1)

# Compile .proto files and fix imports
def compile_proto(venv_python):
    # Importaciones dependientes del venv (grpc_tools)
    from grpc_tools import protoc
    
    print("DEBUG: Starting .proto compilation")
    proto_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "protos"))
    proto_files = [os.path.basename(f) for f in glob.glob(os.path.join(proto_dir, "*.proto"))]
    
    if not proto_files:
        print(f"ERROR: No .proto files found in: {proto_dir}")
        return False
    
    print(f"DEBUG: Found .proto files: {proto_files}")
    
    for proto_file in proto_files:
        proto_path = os.path.join(proto_dir, proto_file)
        if not os.path.exists(proto_path):
            print(f"ERROR: No se encontró el archivo .proto en: {proto_path}")
            return False
    
    pb2_output_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "services_pb2"))
    pb2_grpc_output_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "services_pb2_grpc"))
    os.makedirs(pb2_output_dir, exist_ok=True)
    os.makedirs(pb2_grpc_output_dir, exist_ok=True)
    print(f"DEBUG: Output directories set: {pb2_output_dir}, {pb2_grpc_output_dir}")
    
    command = [
        venv_python, "-m", "grpc_tools.protoc",
        "-I", "protos",
        f"--python_out={pb2_output_dir}",
        f"--grpc_python_out={pb2_grpc_output_dir}",
    ] + proto_files
    
    print("INFO: Compiling .proto files...")
    print(f"Command executed: {' '.join(command)}")
    
    try:
        result = subprocess.run(
            command,
            cwd=os.path.abspath(os.path.dirname(__file__)),
            capture_output=True,
            text=True,
            check=True
        )
        print("SUCCESS: Compilation successful")
        print(f"DEBUG: protoc output: {result.stdout}")
    except subprocess.CalledProcessError as e:
        print(f"ERROR: Failed to compile .proto files (return code: {e.returncode})")
        print(f"Error output: {e.stderr}")
        return False
    
    print("INFO: Fixing imports in gRPC and Protobuf files...")
    for proto_file in proto_files:
        base_name = os.path.splitext(proto_file)[0]
        grpc_file = os.path.join(pb2_grpc_output_dir, f"{base_name}_pb2_grpc.py")
        if os.path.exists(grpc_file):
            with open(grpc_file, 'r', encoding='utf-8') as f:
                content = f.read()
            corrected_content = content
            for pf in proto_files:
                dep_base_name = os.path.splitext(pf)[0]
                corrected_content = corrected_content.replace(
                    f"import {dep_base_name}_pb2", f"from services_pb2 import {dep_base_name}_pb2"
                )
            with open(grpc_file, 'w', encoding='utf-8') as f:
                f.write(corrected_content)
            print(f"SUCCESS: Fixed imports in {grpc_file}")
        
        pb2_file = os.path.join(pb2_output_dir, f"{base_name}_pb2.py")
        if os.path.exists(pb2_file):
            with open(pb2_file, 'r', encoding='utf-8') as f:
                content = f.read()
            corrected_content = content
            for pf in proto_files:
                dep_base_name = os.path.splitext(pf)[0]
                corrected_content = corrected_content.replace(
                    f"import {dep_base_name}_pb2", f"from services_pb2 import {dep_base_name}_pb2"
                )
            with open(pb2_file, 'w', encoding='utf-8') as f:
                f.write(corrected_content)
            print(f"SUCCESS: Fixed imports in {pb2_file}")
    
    print("DEBUG: .proto compilation and import fixing completed")
    return True

def serve():
    # Importaciones dependientes del venv (grpc, servicios, pb2_grpc)
    import grpc
    from concurrent.futures import ThreadPoolExecutor
    from database.databaseManager import init_db
    from services.UserService import UserService
    from services.AuthService import AuthService
    from services.DonationService import DonationService
    from services.EventService import EventService
    from services_pb2_grpc import (
        authorize_pb2_grpc,
        user_pb2_grpc,
        donation_pb2_grpc,
        event_pb2_grpc
    )
    
    print("DEBUG: Starting gRPC server")
    server = grpc.server(ThreadPoolExecutor(max_workers=10))
    user_pb2_grpc.add_UserServiceServicer_to_server(UserService(), server)
    authorize_pb2_grpc.add_AuthServiceServicer_to_server(AuthService(), server)
    donation_pb2_grpc.add_DonationServiceServicer_to_server(DonationService(), server)
    event_pb2_grpc.add_EventServiceServicer_to_server(EventService(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    print("SUCCESS: gRPC server started on port 50051")
    
    try:
        while True:
            time.sleep(86400)
    except KeyboardInterrupt:
        server.stop(0)
        print("INFO: Server stopped manually")

def main():
    """Run the main application logic using the virtual environment."""
    create_venv_if_missing()
    venv_python = get_venv_python()
    
    # Check if the script is running with the virtual environment's Python
    if sys.executable != venv_python:
        print(f"INFO: Relaunching with virtual environment Python: {venv_python}")
        try:
            result = subprocess.run(
                [venv_python, __file__],
                check=True,
                text=True,
                env={**os.environ, "PYTHONUNBUFFERED": "1"}
            )
            if result.stdout:
                print(result.stdout)
            sys.exit(result.returncode)
        except subprocess.CalledProcessError as e:
            print(f"ERROR: Error running main.py with virtual environment: {e.stderr}")
            sys.exit(1)
        except KeyboardInterrupt:
            print("INFO: Execution interrupted by user")
            sys.exit(0)
    
    # Agregar paths y hacer importaciones seguras
    sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "services_pb2")))
    sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "services_pb2_grpc")))
    print("DEBUG: Added services_pb2 and services_pb2_grpc to PYTHONPATH")
    
    print("DEBUG: Starting main.py execution")
    if not compile_proto(venv_python):
        sys.exit(1)
    
    print("DEBUG: Initializing database")
    from database.databaseManager import init_db
    init_db()
    print("DEBUG: Database initialized")
    
    serve()

if __name__ == "__main__":
    main()