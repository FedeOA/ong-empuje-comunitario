import sys
import os
import grpc
import datetime
import time
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
from database.config import DB_USER, DB_PASS, DB_HOST, DB_PORT, DB_NAME
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker
from database.models import Event, EventDonation, ExternalEvent, EventAdhesion
import services_pb2.event_pb2 as event_pb2
import services_pb2.user_pb2 as user_pb2
import services_pb2.donation_pb2 as donation_pb2
import services_pb2_grpc.event_pb2_grpc as event_pb2_grpc
import services_pb2_grpc.user_pb2_grpc as user_pb2_grpc
import services_pb2_grpc.donation_pb2_grpc as donation_pb2_grpc

DB_URL = f"mysql+pymysql://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}"
engine = create_engine(DB_URL, echo=False)
SessionLocal = sessionmaker(bind=engine)

def get_session():
    return SessionLocal()

def test_event_service(stub, stub_user, stub_donation):
    print("\n=== Testing EventService ===")
    # Retrieve the first user
    user_id = None
    try:
        user_list = stub_user.ListUsers(user_pb2.Empty())
        if user_list.user:
            user_id = user_list.user[0].id
            username = user_list.user[0].username
            print(f"Using first user: username={username}, id={user_id}")
        else:
            print("No users found in database, skipping user-related tests")
            return
    except grpc.RpcError as e:
        print(f"Failed to list users: {str(e)}")
        return

    # Create a donation
    donation = donation_pb2.Donation(
        categoria=1,
        description="Event Donation",
        cantidad=50,
        eliminado=False,
        username="admin"
    )
    try:
        donation_response = stub_donation.CreateDonation(donation)
        print(f"CreateDonation: success={donation_response.success}, message={donation_response.message}")
        if not donation_response.success:
            return
    except grpc.RpcError as e:
        print(f"CreateDonation failed: {str(e)}")
        return

    # Retrieve donation_id
    donation_id = None
    for attempt in range(5):
        try:
            donation_list = stub_donation.ListDonations(donation_pb2.Empty())
            sorted_donations = sorted(donation_list.donation, key=lambda d: d.id, reverse=True)
            for d in sorted_donations:
                if d.description == "Event Donation" and d.categoria == 1:
                    donation_id = d.id
                    print(f"Retrieved donation_id={donation_id}")
                    break
            if donation_id:
                break
            time.sleep(1)
        except grpc.RpcError:
            pass
    if not donation_id:
        print("Failed to retrieve donation_id, skipping donation-related tests")
        return

    # Create an event
    future_date = (datetime.datetime.now() + datetime.timedelta(days=10)).strftime('%Y-%m-%dT%H:%M:%S')
    event = event_pb2.Event(
        name="Test Event",
        description="This is a test event",
        fecha_hora=future_date
    )
    try:
        response = stub.CreateEvent(event)
        print(f"CreateEvent: success={response.success}, message={response.message}")
        if not response.success:
            return
    except grpc.RpcError as e:
        print(f"CreateEvent failed: {str(e)}")
        return

    # Retrieve event_id and associate donation
    event_id = None
    for attempt in range(10):
        session = None
        try:
            session = get_session()
            db_event = session.query(Event).filter_by(name="Test Event").first()
            if db_event:
                event_id = db_event.id
                event_donation = EventDonation(
                    event_id=event_id,
                    donation_id=donation_id,
                    quantity_used=0
                )
                session.add(event_donation)
                session.commit()
                print(f"Associated donation_id={donation_id} with event_id={event_id}")
                break
            time.sleep(2)
        except Exception:
            pass
        finally:
            if session:
                session.close()
    if not event_id:
        print("Failed to retrieve event_id, skipping remaining tests")
        return

def test_publish_solidarity_event(stub_event):
    print("\n=== Testing PublishSolidarityEvent ===")
    event_request = event_pb2.SolidarityEventRequest(
        org_id=1,
        event_id=1,
        name="Visita a la escuela nº 99",
        description="Se organizarán juegos y repartirán útiles",
        date_time=(datetime.datetime.now() + datetime.timedelta(days=10)).strftime('%Y-%m-%d %H:%M:%S')
    )
    try:
        response = stub_event.PublishSolidarityEvent(event_request)
        print(f"PublishSolidarityEvent: success={response.success}, message={response.message}")
    except grpc.RpcError as e:
        print(f"PublishSolidarityEvent failed: {str(e)}")

def test_cancel_solidarity_event(stub_event):
    print("\n=== Testing CancelSolidarityEvent ===")
    session = None
    try:
        session = get_session()
        external_event = ExternalEvent(
            event_id=1,
            org_id=2,
            name="Test External Event",
            description="Test event from another org",
            date_time=datetime.datetime.now() + datetime.timedelta(days=20),
            is_canceled=False,
            created_at=datetime.datetime.now()
        )
        session.add(external_event)
        session.commit()
        print("Inserted test external event successfully")
    except Exception:
        print("Failed to insert test external event")
        return
    finally:
        if session:
            session.close()

    cancellation_request = event_pb2.EventCancellationRequest(
        org_id=2,
        event_id=1
    )
    try:
        response = stub_event.CancelSolidarityEvent(cancellation_request)
        print(f"CancelSolidarityEvent: success={response.success}, message={response.message}")
    except grpc.RpcError as e:
        print(f"CancelSolidarityEvent failed: {str(e)}")
        return

    max_attempts = 5
    for attempt in range(max_attempts):
        session = None
        try:
            session = get_session()
            event = session.query(ExternalEvent).filter_by(event_id=1, org_id=2).first()
            if event and event.is_canceled:
                print(f"External event {event.event_id} successfully marked as canceled")
                return
            time.sleep(2)
        except Exception:
            pass
        finally:
            if session:
                session.close()
    print("Failed to verify event cancellation")

def test_adhere_to_event(stub_event, stub_user):
    print("\n=== Testing AdhereToEvent ===")
    unique_suffix = datetime.datetime.now().strftime('%Y%m%d%H%M%S')
    user = user_pb2.User(
        username=f"volunteer_{unique_suffix}",
        first_name="Volunteer",
        last_name="Test",
        phone="5556667777",
        email=f"volunteer_{unique_suffix}@example.com",
        role_id=4,  # VOLUNTARIO
        is_active=True
    )
    try:
        user_response = stub_user.CreateUser(user)
        print(f"CreateUser: success={user_response.success}, message={user_response.message}")
        if not user_response.success:
            return
    except grpc.RpcError as e:
        print(f"CreateUser failed: {str(e)}")
        return

    user_id = None
    for attempt in range(5):
        try:
            user_list = stub_user.ListUsers(user_pb2.Empty())
            for u in user_list.user:
                if u.username == user.username:
                    user_id = u.id
                    print(f"Retrieved user_id={user_id}, username={u.username}")
                    break
            if user_id:
                break
            time.sleep(1)
        except grpc.RpcError:
            pass
    if not user_id:
        print("Failed to retrieve user_id, skipping adhesion test")
        return

    session = None
    try:
        session = get_session()
        external_event = ExternalEvent(
            event_id=2,
            org_id=1,
            name="Adhesion Test Event",
            description="Test event for adhesion",
            date_time=datetime.datetime.now() + datetime.timedelta(days=15),
            is_canceled=False,
            created_at=datetime.datetime.now()
        )
        session.add(external_event)
        session.commit()
        print("Inserted test external event successfully")
    except Exception:
        print("Failed to insert test external event")
        return
    finally:
        if session:
            session.close()

    adhesion_request = event_pb2.EventAdhesionRequest(
        event_id=2,
        org_id=1,
        volunteer_id=user_id,
        volunteer_name=f"{user.first_name} {user.last_name}",
        volunteer_last_name=user.last_name,
        volunteer_phone=user.phone,
        volunteer_email=user.email
    )
    try:
        response = stub_event.AdhereToEvent(adhesion_request)
        print(f"AdhereToEvent: success={response.success}, message={response.message}")
        if not response.success:
            return
    except grpc.RpcError as e:
        print(f"AdhereToEvent failed: {str(e)}")
        return

    max_attempts = 12
    for attempt in range(max_attempts):
        session = None
        try:
            session = get_session()
            adhesion = session.query(EventAdhesion).filter_by(
                event_id=2,
                volunteer_id=user_id,
                org_id=1
            ).first()
            if adhesion:
                print(f"Event adhesion stored: event_id={adhesion.event_id}, volunteer_id={adhesion.volunteer_id}")
                return
            time.sleep(5)
        except Exception:
            pass
        finally:
            if session:
                session.close()
    print("Failed to verify event adhesion")

def main():
    print("Starting EventService Tests")
    print("==========================")
    
    try:
        channel = grpc.insecure_channel('localhost:50051')
        stub_user = user_pb2_grpc.UserServiceStub(channel)
        stub_donation = donation_pb2_grpc.DonationServiceStub(channel)
        stub_event = event_pb2_grpc.EventServiceStub(channel)
    except Exception as e:
        print(f"Failed to connect to gRPC server: {str(e)}")
        return
    
    test_event_service(stub_event, stub_user, stub_donation)
    test_publish_solidarity_event(stub_event)
    test_cancel_solidarity_event(stub_event)
    test_adhere_to_event(stub_event, stub_user)
    
    channel.close()
    print("\n==========================")
    print("EventService Tests Completed")

if __name__ == '__main__':
    main()