import sys
import os
import grpc
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from services_pb2 import authorize_pb2 as authorize_pb2
from services_pb2_grpc import authorize_pb2_grpc as authorize_pb2_grpc
from database import databaseManager
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

Session = databaseManager.get_session()

def test_auth_service(stub):
    print("\n=== Testing AuthService ===")
    
    # Test Login with valid credentials
    login_request = authorize_pb2.LoginRequest(
        username_or_email="janedoe",
        password="default_password"
    )
    response = stub.Login(login_request)
    print(f"Login: success={response.success}, message={response.message}")
    if response.success:
        print(f"  - Logged in user: id={response.user.id}, username={response.user.username}")
    
    # Test Login with invalid password
    login_request = authorize_pb2.LoginRequest(
        username_or_email="testuser",
        password="wrong_password"
    )
    response = stub.Login(login_request)
    print(f"Login (invalid password): success={response.success}, message={response.message}")

def main():
    print("Starting AuthService Tests")
    print("==========================")
    
    try:
        channel = grpc.insecure_channel('localhost:50051')
        stub_auth = authorize_pb2_grpc.AuthServiceStub(channel)  # Updated stub name
    except Exception as e:
        print(f"Failed to connect to server: {str(e)}")
        return
    
    test_auth_service(stub_auth)
    
    channel.close()
    print("\n==========================")
    print("AuthService Tests Completed")

if __name__ == '__main__':
    main()