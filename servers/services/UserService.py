from servers.servers_pb2 import Response, UserList
from servers import servers_pb2_grpc
from servers.db.dbManager import get_session
from servers.db.models import UserEvent, Donation, User
from sqlalchemy import or_
import bcrypt



class UserService( servers_pb2_grpc.UserServiceServicer):
    def CreateUser(self, request, context):
        session = get_session()
        try:
            password = "default_password"
            hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')
            new_user = User(
                username=request.username,
                first_name=request.first_name,
                last_name=request.last_name,
                phone=request.phone,
                email=request.email,
                is_active=request.is_active,
                password_hash=hashed_password
            )
            session.add(new_user)
            session.commit()
            return Response(success=True, message="User created successfully")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
        finally:
            session.close()

    def UpdateUser(self, request, context):
        session = get_session()
        try:
            user = session.query(User).filter_by(id=request.id).first()
            if not user:
                return Response(success=False, message="User not found")
            user.username = request.username
            user.first_name = request.first_name
            user.phone = request.phone
            user.email = request.email
            user.is_active = request.is_active
            if not user.password_hash:
                user.password_hash = bcrypt.hashpw("default_password".encode('utf-8'), bcrypt.gensalt()).decode('utf-8')
            session.commit()
            return Response(success=True, message="User updated successfully")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
        finally:
            session.close()

    def DeleteUser(self, request, context):
        session = get_session()
        try:
            user = session.query(User).filter_by(id=request.id).first()
            if not user:
                return Response(success=False, message="User not found")
            session.query(UserEvent).filter_by(user_id=user.id).delete()
            session.query(Donation).filter(or_(Donation.created_by == user.id, Donation.user_id == user.id)).delete()
            session.delete(user)
            session.commit()
            return Response(success=True, message="User deleted successfully")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
        finally:
            session.close()

    def ListUsers(self, request, context):
        session = get_session()
        try:
            users = session.query(User).all()
            user_list = UserList()
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
