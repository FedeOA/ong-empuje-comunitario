import sys
import os
# Add servers/ directory to sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
import grpc
import services_pb2.event_pb2 as event_pb2
import services_pb2.user_pb2 as user_pb2
import services_pb2.donation_pb2 as donation_pb2
import services_pb2_grpc.event_pb2_grpc as event_pb2_grpc
import services_pb2_grpc.user_pb2_grpc as user_pb2_grpc
import services_pb2_grpc.donation_pb2_grpc as donation_pb2_grpc
import datetime
from database.models import EventDonation
from database import databaseManager

Session = databaseManager.get_session()

def test_event_service(stub, stub_user, stub_donation):
    print("\n=== Testing EventService ===")
    
    # Create prerequisite data: User
    user = user_pb2.User(
        username=f"eventuser_{datetime.datetime.now().strftime('%Y%m%d%H%M%S')}",  # Unique username
        first_name="Event",
        last_name="User",
        phone="1112223333",
        email="eventuser@example.com",
        role_id=1,  # Assume role_id 1 exists
        is_active=True
    )
    user_response = stub_user.CreateUser(user)
    print(f"CreateUser (for events): success={user_response.success}, message={user_response.message}")
    user_id = None
    user_list = stub_user.ListUsers(user_pb2.Empty())
    if user_list.user:
        user_id = user_list.user[-1].id
    
    # Create prerequisite data: Donation
    donation = donation_pb2.Donation(
        description="Event Donation",
        cantidad=50,
        eliminado=False
    )
    donation_response = stub_donation.CreateDonation(donation)
    print(f"CreateDonation (for events): success={donation_response.success}, message={donation_response.message}")
    donation_id = None
    donation_list = stub_donation.ListDonations(donation_pb2.Empty())
    if donation_list.donation:
        donation_id = donation_list.donation[-1].id
    
    # Test CreateEvent
    event = event_pb2.Event(
        name="Test Event",
        description="This is a test event",
        fecha_hora=datetime.datetime.now().isoformat()
    )
    response = stub.CreateEvent(event)
    print(f"CreateEvent: success={response.success}, message={response.message}")
    event_id = None
    
    # Test ListEvents
    event_list = stub.ListEvents(event_pb2.Empty())
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
        user_event = event_pb2.UserEventRequest(
            user_id=user_id,
            event_id=event_id
        )
        response = stub.AddUser(user_event)
        print(f"AddUser: success={response.success}, message={response.message}")
        event_list = stub.ListEvents(event_pb2.Empty())
        for e in event_list.event:
            if e.id == event_id:
                print(f"  - Users in event {e.id}: {[u.username for u in e.users]}")
    
    # Test RemoveUser
    if user_id and event_id:
        user_event = event_pb2.UserEventRequest(
            user_id=user_id,
            event_id=event_id
        )
        response = stub.RemoveUser(user_event)
        print(f"RemoveUser: success={response.success}, message={response.message}")
        event_list = stub.ListEvents(event_pb2.Empty())
        for e in event_list.event:
            if e.id == event_id:
                print(f"  - Users in event {e.id}: {[u.username for u in e.users]}")
    
    # Test UseDonations
    if donation_id and event_id:
        donation_event = event_pb2.DonationEventRequest(
            donation_id=donation_id,
            event_id=event_id
        )
        response = stub.UseDonations(donation_event)
        print(f"UseDonations: success={response.success}, message={response.message}")
        # Verify event_donations
        try:
            event_donation = Session.query(EventDonation).filter_by(event_id=event_id, donation_id=donation_id).first()
            if event_donation:
                print(f"  - EventDonation created: event_id={event_donation.event_id}, donation_id={event_donation.donation_id}, quantity_used={event_donation.quantity_used}")
            else:
                print("  - EventDonation not found")
        finally:
            Session.close()
    
    # Test UpdateEvent
    if event_id:
        event_to_update = event_pb2.Event(
            id=event_id,
            name="Updated Event",
            description="Updated test event",
            fecha_hora=datetime.datetime.now().isoformat()
        )
        response = stub.UpdateEvent(event_to_update)
        print(f"UpdateEvent: success={response.success}, message={response.message}")
    
    # Test DeleteEvent
    if event_id:
        event_to_delete = event_pb2.Event(id=event_id)
        response = stub.DeleteEvent(event_to_delete)
        print(f"DeleteEvent: success={response.success}, message={response.message}")

def main():
    print("Starting EventService Tests")
    print("==========================")
    
    try:
        channel = grpc.insecure_channel('localhost:50051')
        stub_user = user_pb2_grpc.UserServiceStub(channel)
        stub_donation = donation_pb2_grpc.DonationServiceStub(channel)
        stub_event = event_pb2_grpc.EventServiceStub(channel)
    except Exception as e:
        print(f"Failed to connect to server: {str(e)}")
        return
    
    test_event_service(stub_event, stub_user, stub_donation)
    
    channel.close()
    print("\n==========================")
    print("EventService Tests Completed")

if __name__ == '__main__':
    main()