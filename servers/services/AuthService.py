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

            print(f"User query result: {user.username if user else 'None'}") 

            if not user:
                response = LoginResponse(
                    success=False,
                    message="User not found",
                    role_id=0,
                    username="",
                    organization_id=0
                )
                print("Login failed: User not found")
                return response

            password_match = bcrypt.checkpw(request.password.encode('utf-8'), user.password_hash.encode('utf-8'))
            print(f"Password match: {password_match}")

            if password_match:
                response = LoginResponse(
                    success=True,
                    message="Login successful",
                    role_id=user.role_id,
                    username=user.username,
                    organization_id=user.organization_id or 0  # Ensure non-null
                )
                print(f"Login success: org_id={user.organization_id}")
                return response
            else:
                response = LoginResponse(
                    success=False,
                    message="Invalid password",
                    role_id=0,
                    username="",
                    organization_id=0
                )
                print("Login failed: Invalid password")
                return response
        except Exception as e:
            print(f"Login exception: {str(e)}")
            return LoginResponse(
                success=False,
                message=str(e),
                role_id=0,
                username="",
                organization_id=0
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