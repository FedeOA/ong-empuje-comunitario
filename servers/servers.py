import grpc
from concurrent import futures
import time
import servers_pb2
import servers_pb2_grpc
from sqlalchemy import create_engine, Column, Integer, String, Boolean, DateTime, ForeignKey, or_
from sqlalchemy.orm import declarative_base, sessionmaker
import datetime
import bcrypt

# Database setup
Base = declarative_base()
engine = create_engine('mysql+mysqlconnector://root:1234@localhost:3306/ong-empuje-comunitario', echo=False)
Session = sessionmaker(bind=engine)

# Database Models
class User(Base):
    __tablename__ = 'users'
    id = Column(Integer, primary_key=True)
    username = Column(String(255))
    first_name = Column(String(255))
    last_name = Column(String(255))
    phone = Column(String(255))
    password_hash = Column(String(255))
    email = Column(String(255))
    is_active = Column(Boolean)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
    role_id = Column(Integer, ForeignKey('roles.id'))

class Donation(Base):
    __tablename__ = 'donations'
    id = Column(Integer, primary_key=True)
    description = Column(String(255))
    quantity = Column(Integer)
    is_deleted = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
    created_by = Column(Integer, ForeignKey('users.id'))
    updated_at = Column(DateTime)
    updated_by = Column(Integer, ForeignKey('users.id'))
    category_id = Column(Integer, ForeignKey('categories.id'))
    user_id = Column(Integer, ForeignKey('users.id'))

class Event(Base):
    __tablename__ = 'events'
    id = Column(Integer, primary_key=True)
    name = Column(String(255))
    description = Column(String(255))
    event_datetime = Column(DateTime)

class Role(Base):
    __tablename__ = 'roles'
    id = Column(Integer, primary_key=True)
    name = Column(String(255))

class Category(Base):
    __tablename__ = 'categories'
    id = Column(Integer, primary_key=True)
    name = Column(String(255))

class UserEvent(Base):
    __tablename__ = 'user_events'
    id = Column(Integer, primary_key=True)
    registration_date = Column(DateTime, default=datetime.datetime.utcnow)
    user_id = Column(Integer, ForeignKey('users.id'))
    event_id = Column(Integer, ForeignKey('events.id'))

class EventDonation(Base):
    __tablename__ = 'event_donations'
    id = Column(Integer, primary_key=True)
    quantity_used = Column(Integer)
    event_id = Column(Integer, ForeignKey('events.id'))
    donation_id = Column(Integer, ForeignKey('donations.id'))

Base.metadata.create_all(engine)

# gRPC Service Implementations
class UserService(servers_pb2_grpc.UserServiceServicer):
    def CreateUser(self, request, context):
        session = Session()
        try:
            password = "default_password"
            hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')
            new_user = User(
                username=request.username,
                first_name=request.first_name,
                phone=request.phone,
                email=request.email,
                is_active=request.is_active,
                password_hash=hashed_password
            )
            session.add(new_user)
            session.commit()
            return servers_pb2.Response(success=True, message="User created successfully")
        except Exception as e:
            session.rollback()
            return servers_pb2.Response(success=False, message=str(e))
        finally:
            session.close()

    def UpdateUser(self, request, context):
        session = Session()
        try:
            user = session.query(User).filter_by(id=request.id).first()
            if not user:
                return servers_pb2.Response(success=False, message="User not found")
            user.username = request.username
            user.first_name = request.first_name
            user.phone = request.phone
            user.email = request.email
            user.is_active = request.is_active
            if not user.password_hash:
                user.password_hash = bcrypt.hashpw("default_password".encode('utf-8'), bcrypt.gensalt()).decode('utf-8')
            session.commit()
            return servers_pb2.Response(success=True, message="User updated successfully")
        except Exception as e:
            session.rollback()
            return servers_pb2.Response(success=False, message=str(e))
        finally:
            session.close()

    def DeleteUser(self, request, context):
        session = Session()
        try:
            user = session.query(User).filter_by(id=request.id).first()
            if not user:
                return servers_pb2.Response(success=False, message="User not found")
            session.query(UserEvent).filter_by(user_id=user.id).delete()
            session.query(Donation).filter(or_(Donation.created_by == user.id, Donation.user_id == user.id)).delete()
            session.delete(user)
            session.commit()
            return servers_pb2.Response(success=True, message="User deleted successfully")
        except Exception as e:
            session.rollback()
            return servers_pb2.Response(success=False, message=str(e))
        finally:
            session.close()

    def ListUsers(self, request, context):
        session = Session()
        try:
            users = session.query(User).all()
            user_list = servers_pb2.UserList()
            for user in users:
                user_list.user.add(
                    id=user.id,
                    username=user.username,
                    first_name=user.first_name,
                    phone=user.phone,
                    email=user.email,
                    is_active=user.is_active
                )
            return user_list
        finally:
            session.close()

class AuthService(servers_pb2_grpc.AuthServiceServicer):
    def Login(self, request, context):
        session = Session()
        try:
            user = session.query(User).filter(
                or_(User.username == request.username_or_email, User.email == request.username_or_email)
            ).first()
            if not user:
                return servers_pb2.LoginResponse(
                    success=False,
                    message="User not found",
                    user=servers_pb2.User()
                )
            if bcrypt.checkpw(request.password.encode('utf-8'), user.password_hash.encode('utf-8')):
                return servers_pb2.LoginResponse(
                    success=True,
                    message="Login successful",
                    user=servers_pb2.User(
                        id=user.id,
                        username=user.username,
                        first_name=user.first_name,
                        phone=user.phone,
                        email=user.email,
                        is_active=user.is_active
                    )
                )
            else:
                return servers_pb2.LoginResponse(
                    success=False,
                    message="Invalid password",
                    user=servers_pb2.User()
                )
        except Exception as e:
            return servers_pb2.LoginResponse(
                success=False,
                message=str(e),
                user=servers_pb2.User()
            )
        finally:
            session.close()

class DonationService(servers_pb2_grpc.DonationServiceServicer):
    def CreateDonation(self, request, context):
        session = Session()
        try:
            new_donation = Donation(
                description=request.description,
                quantity=request.cantidad,
                is_deleted=request.eliminado,
                category_id=1,
                user_id=None
            )
            session.add(new_donation)
            session.commit()
            return servers_pb2.Response(success=True, message="Donation created successfully")
        except Exception as e:
            session.rollback()
            return servers_pb2.Response(success=False, message=str(e))
        finally:
            session.close()

    def UpdateDonation(self, request, context):
        session = Session()
        try:
            donation = session.query(Donation).filter_by(id=request.id).first()
            if not donation:
                return servers_pb2.Response(success=False, message="Donation not found")
            donation.description = request.description
            donation.quantity = request.cantidad
            donation.is_deleted = request.eliminado
            donation.updated_at = datetime.datetime.utcnow()
            session.commit()
            return servers_pb2.Response(success=True, message="Donation updated successfully")
        except Exception as e:
            session.rollback()
            return servers_pb2.Response(success=False, message=str(e))
        finally:
            session.close()

    def DeleteDonation(self, request, context):
        session = Session()
        try:
            donation = session.query(Donation).filter_by(id=request.id).first()
            if not donation:
                return servers_pb2.Response(success=False, message="Donation not found")
            session.query(EventDonation).filter_by(donation_id=donation.id).delete()
            session.delete(donation)
            session.commit()
            return servers_pb2.Response(success=True, message="Donation deleted successfully")
        except Exception as e:
            session.rollback()
            return servers_pb2.Response(success=False, message=str(e))
        finally:
            session.close()

    def ListDonations(self, request, context):
        session = Session()
        try:
            donations = session.query(Donation).all()
            donation_list = servers_pb2.DonationList()
            for donation in donations:
                donation_list.donation.add(
                    id=donation.id,
                    description=donation.description,
                    cantidad=donation.quantity,
                    eliminado=donation.is_deleted
                )
            return donation_list
        finally:
            session.close()

class EventService(servers_pb2_grpc.EventServiceServicer):
    def CreateEvent(self, request, context):
        session = Session()
        try:
            new_event = Event(
                name=request.name,
                description=request.description,
                event_datetime=datetime.datetime.fromisoformat(request.fecha_hora)
            )
            session.add(new_event)
            session.commit()
            return servers_pb2.Response(success=True, message="Event created successfully")
        except Exception as e:
            session.rollback()
            return servers_pb2.Response(success=False, message=str(e))
        finally:
            session.close()

    def UpdateEvent(self, request, context):
        session = Session()
        try:
            event = session.query(Event).filter_by(id=request.id).first()
            if not event:
                return servers_pb2.Response(success=False, message="Event not found")
            event.name = request.name
            event.description = request.description
            event.event_datetime = datetime.datetime.fromisoformat(request.fecha_hora)
            session.commit()
            return servers_pb2.Response(success=True, message="Event updated successfully")
        except Exception as e:
            session.rollback()
            return servers_pb2.Response(success=False, message=str(e))
        finally:
            session.close()

    def DeleteEvent(self, request, context):
        session = Session()
        try:
            event = session.query(Event).filter_by(id=request.id).first()
            if not event:
                return servers_pb2.Response(success=False, message="Event not found")
            session.query(UserEvent).filter_by(event_id=event.id).delete()
            session.query(EventDonation).filter_by(event_id=event.id).delete()
            session.delete(event)
            session.commit()
            return servers_pb2.Response(success=True, message="Event deleted successfully")
        except Exception as e:
            session.rollback()
            return servers_pb2.Response(success=False, message=str(e))
        finally:
            session.close()

    def ListEvents(self, request, context):
        session = Session()
        try:
            events = session.query(Event).all()
            event_list = servers_pb2.EventList()
            for event in events:
                event_proto = event_list.event.add(
                    id=event.id,
                    name=event.name,
                    description=event.description,
                    fecha_hora=event.event_datetime.isoformat()
                )
                user_events = session.query(UserEvent).filter_by(event_id=event.id).all()
                for ue in user_events:
                    user = session.query(User).filter_by(id=ue.user_id).first()
                    if user:
                        event_proto.users.add(
                            id=user.id,
                            username=user.username,
                            first_name=user.first_name,
                            phone=user.phone,
                            email=user.email,
                            is_active=user.is_active
                        )
            return event_list
        finally:
            session.close()

    def AddUser(self, request, context):
        session = Session()
        try:
            user_event = UserEvent(
                user_id=request.user_id,
                event_id=request.event_id
            )
            session.add(user_event)
            session.commit()
            return servers_pb2.Response(success=True, message="User added to event")
        except Exception as e:
            session.rollback()
            return servers_pb2.Response(success=False, message=str(e))
        finally:
            session.close()

    def RemoveUser(self, request, context):
        session = Session()
        try:
            user_event = session.query(UserEvent).filter_by(
                user_id=request.user_id,
                event_id=request.event_id
            ).first()
            if not user_event:
                return servers_pb2.Response(success=False, message="User-event relation not found")
            session.delete(user_event)
            session.commit()
            return servers_pb2.Response(success=True, message="User removed from event")
        except Exception as e:
            session.rollback()
            return servers_pb2.Response(success=False, message=str(e))
        finally:
            session.close()

    def UseDonations(self, request, context):
        session = Session()
        try:
            event_donation = EventDonation(
                event_id=request.event_id,
                donation_id=request.donation_id,
                quantity_used=1
            )
            session.add(event_donation)
            session.commit()
            return servers_pb2.Response(success=True, message="Donation used in event")
        except Exception as e:
            session.rollback()
            return servers_pb2.Response(success=False, message=str(e))
        finally:
            session.close()

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    servers_pb2_grpc.add_UserServiceServicer_to_server(UserService(), server)
    servers_pb2_grpc.add_AuthServiceServicer_to_server(AuthService(), server)
    servers_pb2_grpc.add_DonationServiceServicer_to_server(DonationService(), server)
    servers_pb2_grpc.add_EventServiceServicer_to_server(EventService(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    print("Server started on port 50051")
    try:
        while True:
            time.sleep(86400)
    except KeyboardInterrupt:
        server.stop(0)

if __name__ == '__main__':
    serve()