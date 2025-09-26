import bcrypt
import os
import sys
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from services_pb2.user_pb2 import User as UserMessage, UserList, Response
from services_pb2_grpc.user_pb2_grpc import UserServiceServicer
from database.databaseManager import get_session
from database.models import User
import smtplib
from email.mime.text import MIMEText
from secrets import token_urlsafe
from database.config import SMTP_PASSWORD, SMTP_SERVER, SMTP_USER
import sys
import os
from kafka_module.kafka_manager import KafkaManager  # type: ignore

class UserService(UserServiceServicer):
    def __init__(self):
        self.kafka = KafkaManager()

    def CreateUser(self, request, context):
        session = get_session()
        try:
            random_password = token_urlsafe(12)
            password_hash = bcrypt.hashpw(random_password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')
            db_user = User(
                username=request.username,
                first_name=request.first_name,
                last_name=request.last_name,
                phone=request.phone,
                email=request.email,
                role_id=request.role_id,
                is_active=True,
                password_hash=password_hash
            )
            session.add(db_user)
            session.commit()

            user_data = {
                'id': db_user.id,
                'username': db_user.username,
                'email': db_user.email,
                'role_id': db_user.role_id
            }
            self.kafka.publish_user_event('create', user_data)

            if not SMTP_USER or not SMTP_PASSWORD:
                raise ValueError("SMTP credentials not configured")

            msg = MIMEText(f"Your account has been created.\n\nUsername: {request.username}\nPassword: {random_password}\n\nPlease change your password after logging in.")
            msg['Subject'] = 'Your New Account Password'
            msg['From'] = SMTP_USER
            msg['To'] = request.email

            with smtplib.SMTP(SMTP_SERVER, smtplib.SMTP_PORT) as server:
                server.starttls()
                server.login(SMTP_USER, SMTP_PASSWORD)
                server.send_message(msg)

            return Response(success=True, message="User created successfully and password emailed")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=f"Failed to create user or send email: {str(e)}")
        finally:
            session.close()

    def UpdateUser(self, request, context):
        session = get_session()
        try:
            db_user = session.query(User).filter_by(id=request.id).first()
            if not db_user:
                return Response(success=False, message="User not found")
            db_user.username = request.username
            db_user.first_name = request.first_name
            db_user.last_name = request.last_name
            db_user.phone = request.phone
            db_user.email = request.email
            db_user.role_id = request.role_id
            session.commit()

            user_data = {
                'id': db_user.id,
                'username': db_user.username,
                'email': db_user.email,
                'role_id': db_user.role_id
            }
            self.kafka.publish_user_event('update', user_data)

            return Response(success=True, message="User updated successfully")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
        finally:
            session.close()

    def DeleteUser(self, request, context):
        session = get_session()
        try:
            db_user = session.query(User).filter_by(id=request.id).first()
            if not db_user:
                return Response(success=False, message="User not found")
            db_user.is_active = not db_user.is_active
            session.commit()

            user_data = {
                'id': db_user.id,
                'username': db_user.username,
                'is_active': db_user.is_active
            }
            self.kafka.publish_user_event('delete', user_data)

            return Response(success=True, message="User deleted successfully")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
        finally:
            session.close()

    def ListUsers(self, request, context):
        session = get_session()
        try:
            db_users = session.query(User).all()
            users = [
                UserMessage(
                    id=user.id,
                    username=user.username,
                    first_name=user.first_name,
                    last_name=user.last_name or "",
                    phone=user.phone or "",
                    email=user.email or "",
                    role_id=user.role_id or 0,
                    is_active=user.is_active
                ) for user in db_users
            ]
            return UserList(user=users)
        except Exception as e:
            return UserList(user=[])
        finally:
            session.close()