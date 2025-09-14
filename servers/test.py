import grpc
import servers_pb2
import servers_pb2_grpc
import datetime
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from servers.services import EventDonation  # Import the EventDonation model

# Database setup for verification
DB_USER = "root"
DB_PASS = ""
DB_HOST = "127.0.0.1"
DB_PORT = "3306"
DB_NAME = "ong-empuje-comunitario"

DB_URL = f"mysql+pymysql://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}"

engine = create_engine(DB_URL, echo=True)
Session = sessionmaker(bind=engine)
Session = sessionmaker(bind=engine)

def test_user_service(stub, auth_stub):
    print("\n=== Testing UserService ===")
    
    # Test CreateUser
    user = servers_pb2.User(
        username="testuser",
        first_name="Test",
        phone="1234567890",
        email="test@example.com",
        is_active=True
    )
    response = stub.CreateUser(user)
    print(f"CreateUser: success={response.success}, message={response.message}")
    user_id = None
    
    # Test ListUsers
    user_list = stub.ListUsers(servers_pb2.Empty())
    print(f"ListUsers: found {len(user_list.user)} users")
    if user_list.user:
        for u in user_list.user:
            print(f"  - User: id={u.id}, username={u.username}, email={u.email}")
        user_id = user_list.user[-1].id
    else:
        print("  - No users found, skipping UpdateUser, DeleteUser, and Login tests")
        return user_id
    
    # Test Login
    if user_id:
        login_request = servers_pb2.LoginRequest(
            username_or_email="testuser",
            password="default_password"
        )
        response = auth_stub.Login(login_request)
        print(f"Login: success={response.success}, message={response.message}")
        if response.success:
            print(f"  - Logged in user: id={response.user.id}, username={response.user.username}")
    
    # Test Login with invalid password
    if user_id:
        login_request = servers_pb2.LoginRequest(
            username_or_email="testuser",
            password="wrong_password"
        )
        response = auth_stub.Login(login_request)
        print(f"Login (invalid password): success={response.success}, message={response.message}")
    
    # Test UpdateUser
    if user_id:
        user_to_update = servers_pb2.User(
            id=user_id,
            username="updateduser",
            first_name="Updated Test",
            phone="0987654321",
            email="updated@example.com",
            is_active=True
        )
        response = stub.UpdateUser(user_to_update)
        print(f"UpdateUser: success={response.success}, message={response.message}")
    
    # Test DeleteUser
    if user_id:
        user_to_delete = servers_pb2.User(id=user_id)
        response = stub.DeleteUser(user_to_delete)
        print(f"DeleteUser: success={response.success}, message={response.message}")
    
    return user_id

def test_donation_service(stub):
    print("\n=== Testing DonationService ===")
    
    # Test CreateDonation
    donation = servers_pb2.Donation(
        description="Test Donation",
        cantidad=100,
        eliminado=False
    )
    response = stub.CreateDonation(donation)
    print(f"CreateDonation: success={response.success}, message={response.message}")
    donation_id = None
    
    # Test ListDonations
    donation_list = stub.ListDonations(servers_pb2.Empty())
    print(f"ListDonations: found {len(donation_list.donation)} donations")
    if donation_list.donation:
        for d in donation_list.donation:
            print(f"  - Donation: id={d.id}, description={d.description}, cantidad={d.cantidad}")
        donation_id = donation_list.donation[-1].id
    else:
        print("  - No donations found, skipping UpdateDonation and DeleteDonation")
        return donation_id
    
    # Test UpdateDonation
    if donation_id:
        donation_to_update = servers_pb2.Donation(
            id=donation_id,
            description="Updated Donation",
            cantidad=200,
            eliminado=False
        )
        response = stub.UpdateDonation(donation_to_update)
        print(f"UpdateDonation: success={response.success}, message={response.message}")
    
    # Test DeleteDonation
    if donation_id:
        donation_to_delete = servers_pb2.Donation(id=donation_id)
        response = stub.DeleteDonation(donation_to_delete)
        print(f"DeleteDonation: success={response.success}, message={response.message}")
    
    return donation_id

def test_event_service(stub, stub_user, stub_donation):
    print("\n=== Testing EventService ===")
    
    # Create prerequisite data
    user = servers_pb2.User(
        username="eventuser",
        first_name="Event",
        phone="1112223333",
        email="eventuser@example.com",
        is_active=True
    )
    user_response = stub_user.CreateUser(user)
    print(f"CreateUser (for events): success={user_response.success}, message={user_response.message}")
    user_id = None
    user_list = stub_user.ListUsers(servers_pb2.Empty())
    if user_list.user:
        user_id = user_list.user[-1].id
    
    donation = servers_pb2.Donation(
        description="Event Donation",
        cantidad=50,
        eliminado=False
    )
    donation_response = stub_donation.CreateDonation(donation)
    print(f"CreateDonation (for events): success={donation_response.success}, message={donation_response.message}")
    donation_id = None
    donation_list = stub_donation.ListDonations(servers_pb2.Empty())
    if donation_list.donation:
        donation_id = donation_list.donation[-1].id
    
    event = servers_pb2.Event(
        name="Test Event",
        description="This is a test event",
        fecha_hora=datetime.datetime.now().isoformat()
    )
    response = stub.CreateEvent(event)
    print(f"CreateEvent: success={response.success}, message={response.message}")
    event_id = None
    
    # Test ListEvents
    event_list = stub.ListEvents(servers_pb2.Empty())
    print(f"ListEvents: found {len(event_list.event)} events")
    if event_list.event:
        for e in event_list.event:
            print(f"  - Event: id={e.id}, name={e.name}, fecha_hora={e.fecha_hora}")
        event_id = event_list.event[-1].id
    else:
        print("  - No events found, skipping remaining EventService tests")
        return
    
    # Test AddUser
    if user_id and event_id:
        user_event = servers_pb2.UserEventRequest(
            user_id=user_id,
            event_id=event_id
        )
        response = stub.AddUser(user_event)
        print(f"AddUser: success={response.success}, message={response.message}")
        event_list = stub.ListEvents(servers_pb2.Empty())
        for e in event_list.event:
            if e.id == event_id:
                print(f"  - Users in event {e.id}: {[u.username for u in e.users]}")
    
    # Test RemoveUser
    if user_id and event_id:
        user_event = servers_pb2.UserEventRequest(
            user_id=user_id,
            event_id=event_id
        )
        response = stub.RemoveUser(user_event)
        print(f"RemoveUser: success={response.success}, message={response.message}")
        event_list = stub.ListEvents(servers_pb2.Empty())
        for e in event_list.event:
            if e.id == event_id:
                print(f"  - Users in event {e.id}: {[u.username for u in e.users]}")
    
    # Test UseDonations
    if donation_id and event_id:
        donation_event = servers_pb2.DonationEventRequest(
            donation_id=donation_id,
            event_id=event_id
        )
        response = stub.UseDonations(donation_event)
        print(f"UseDonations: success={response.success}, message={response.message}")
        # Verify event_donations
        session = Session()
        try:
            event_donation = session.query(EventDonation).filter_by(event_id=event_id, donation_id=donation_id).first()
            if event_donation:
                print(f"  - EventDonation created: event_id={event_donation.event_id}, donation_id={event_donation.donation_id}, quantity_used={event_donation.quantity_used}")
            else:
                print("  - EventDonation not found")
        finally:
            session.close()
    
    # Test UpdateEvent
    if event_id:
        event_to_update = servers_pb2.Event(
            id=event_id,
            name="Updated Event",
            description="Updated test event",
            fecha_hora=datetime.datetime.now().isoformat()
        )
        response = stub.UpdateEvent(event_to_update)
        print(f"UpdateEvent: success={response.success}, message={response.message}")
    
    # Test DeleteEvent
    if event_id:
        event_to_delete = servers_pb2.Event(id=event_id)
        response = stub.DeleteEvent(event_to_delete)
        print(f"DeleteEvent: success={response.success}, message={response.message}")

def main():
    print("Starting gRPC Service Tests")
    print("==========================")
    
    try:
        channel = grpc.insecure_channel('localhost:50051')
        stub_user = servers_pb2_grpc.UserServiceStub(channel)
        stub_auth = servers_pb2_grpc.AuthServiceStub(channel)
        stub_donation = servers_pb2_grpc.DonationServiceStub(channel)
        stub_event = servers_pb2_grpc.EventServiceStub(channel)
    except Exception as e:
        print(f"Failed to connect to server: {str(e)}")
        return
    
    test_user_service(stub_user, stub_auth)
    test_donation_service(stub_donation)
    test_event_service(stub_event, stub_user, stub_donation)
    
    channel.close()
    print("\n==========================")
    print("Tests Completed")

if __name__ == '__main__':
    main()