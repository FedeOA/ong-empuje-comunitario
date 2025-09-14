from servers.servers_pb2 import User as UserMessage, LoginResponse
from servers import servers_pb2_grpc
from servers.db.dbManager import get_session
from servers.db.models import User
from sqlalchemy import or_
import bcrypt


class AuthService(servers_pb2_grpc.AuthServiceServicer):
    def Login(self, request, context):
        session = get_session()
        try:
            user = session.query(User).filter(
                or_(User.username == request.username_or_email, User.email == request.username_or_email)
            ).first()
            if not user:
                return LoginResponse(
                    success=False,
                    message="User not found",
                    user=UserMessage()
                )
            if bcrypt.checkpw(request.password.encode('utf-8'), user.password_hash.encode('utf-8')):
                return LoginResponse(
                    success=True,
                    message="Login successful",
                    user=UserMessage(
                        id=user.id,
                        username=user.username,
                        first_name=user.first_name,
                        phone=user.phone,
                        email=user.email,
                        is_active=user.is_active
                    )
                )
            else:
                return LoginResponse(
                    success=False,
                    message="Invalid password",
                    user=UserMessage()
                )
        except Exception as e:
            return LoginResponse(
                success=False,
                message=str(e),
                user=UserMessage()
            )
        finally:
            session.close()