
import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from services_pb2.user_pb2 import User as UserMessage
from services_pb2.authorize_pb2 import LoginResponse
from services_pb2_grpc import authorize_pb2_grpc
from database.databaseManager import get_session
from database.models import User
from sqlalchemy import or_
import bcrypt
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from dotenv import load_dotenv 
import os
import secrets
import string

load_dotenv()  # Carga las variables de entorno del archivo .env

class AuthService(authorize_pb2_grpc.AuthServiceServicer):

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
                    role_id=0,
                    username=""
                )

            if bcrypt.checkpw(request.password.encode('utf-8'), user.password_hash.encode('utf-8')):

                return LoginResponse(
                    success=True,
                    message="Login successful",
                    role_id=user.role_id,
                    username=user.username
                )
            else:
                return LoginResponse(
                    success=False,
                    message="Invalid password",
                    role_id=0,
                    username=""
                )
        except Exception as e:
            return LoginResponse(
                success=False,
                message=str(e),
            )
        finally:
            session.close()

    def generateRandomPassword(self, length=12):
        characters = string.ascii_letters + string.digits + string.punctuation
        password = ''.join(secrets.choice(characters) for _ in range(length))
        return password

    def sendPassword(self, to_email, username, password):
        sender_email = os.getenv("SENDER_EMAIL")
        sender_password = os.getenv("SENDER_PASSWORD")

        if not sender_email or not sender_password:
            print("Error: SENDER_EMAIL or SENDER_PASSWORD not set in .env")
            return

        subject = "Your account password"
        body = f"Hello {username},\n\nYour account has been created. Your password is:\n\n{password}\n\n"

        msg = MIMEMultipart()
        msg['From'] = sender_email
        msg['To'] = to_email
        msg['Subject'] = subject
        msg.attach(MIMEText(body, 'plain'))

        try:
            server = smtplib.SMTP('smtp.gmail.com', 587)
            server.starttls()
            server.login(sender_email, sender_password)
            server.send_message(msg)
            server.quit()
            print(f"Password email sent to {to_email}")
        except Exception as e:
            print(f"Error sending email: {str(e)}")