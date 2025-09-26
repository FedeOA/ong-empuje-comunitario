import sys
import os
import datetime
import grpc
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from services_pb2 import user_pb2 as user_pb2
from services_pb2_grpc import user_pb2_grpc as user_pb2_grpc
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from database import databaseManager

Session = databaseManager.get_session()

def test_user_service(stub):
    print("\n=== Testing UserService ===")
    
    # Check for existing user or create a new one
    user_list = stub.ListUsers(user_pb2.Empty())
    user_id = None
    for u in user_list.user:
        if u.username.startswith("testuser"):
            user_id = u.id
            print(f"Using existing user: id={u.id}, username={u.username}")
            break
    
    if not user_id:
        # Test CreateUser
        user = user_pb2.User(
            username=f"testuser_{datetime.datetime.now().strftime('%Y%m%d%H%M%S')}",
            first_name="Test",
            last_name="User",
            phone="1234567890",
            email="gastonromero210@example.com",
            role_id=1,  # Assume role_id 1 exists
            is_active=True
        )
        response = stub.CreateUser(user)
        print(f"CreateUser: success={response.success}, message={response.message}")
        if response.success:
            user_list = stub.ListUsers(user_pb2.Empty())
            if user_list.user:
                user_id = user_list.user[-1].id
    
    # Test ListUsers
    user_list = stub.ListUsers(user_pb2.Empty())
    print(f"ListUsers: found {len(user_list.user)} users")
    if user_list.user:
        for u in user_list.user:
            print(f"  - User: id={u.id}, username={u.username}, email={u.email}, last_name={u.last_name}, role_id={u.role_id}")
    else:
        print("  - No users found, skipping UpdateUser and DeleteUser")
        return
    
    # Test UpdateUser
    if user_id:
        user_to_update = user_pb2.User(
            id=user_id,
            username=f"updateduser_{datetime.datetime.now().strftime('%Y%m%d%H%M%S')}",
            first_name="Updated Test",
            last_name="Updated User",
            phone="0987654321",
            email="updated@example.com",
            role_id=1,
            is_active=True
        )
        response = stub.UpdateUser(user_to_update)
        print(f"UpdateUser: success={response.success}, message={response.message}")
    
    # Test DeleteUser
    if user_id:
        user_to_delete = user_pb2.User(id=user_id)
        response = stub.DeleteUser(user_to_delete)
        print(f"DeleteUser: success={response.success}, message={response.message}")

def main():
    print("Starting UserService Tests")
    print("==========================")
    
    try:
        channel = grpc.insecure_channel('localhost:50051')
        stub_user = user_pb2_grpc.UserServiceStub(channel)
    except Exception as e:
        print(f"Failed to connect to server: {str(e)}")
        return
    
    test_user_service(stub_user)
    
    channel.close()
    print("\n==========================")
    print("UserService Tests Completed")

if __name__ == '__main__':
    main()