import logging
from services_pb2.donation_pb2 import Response, DonationList
from services_pb2_grpc import donation_pb2_grpc
from database.databaseManager import get_session
from database.models import Donation, EventDonation, User
import datetime


class DonationService(donation_pb2_grpc.DonationServiceServicer):
    def CreateDonation(self, request, context):
        session = get_session()
        try:
            # Log incoming request
            logging.info(f"Received CreateDonation request: username={request.username}, description={request.description}, quantity={request.cantidad}, category_id={request.categoria}")

            # Validate user
            user = session.query(User).filter_by(username=request.username).first()
            if not user:
                logging.error(f"User not found: username={request.username}")
                return Response(success=False, message=f"User not found for username: {request.username}")

            # Validate category and quantity
            if request.categoria <= 0:
                logging.error(f"Invalid category ID: {request.categoria}")
                return Response(success=False, message="Invalid category ID")
            if request.cantidad <= 0:
                logging.error(f"Invalid quantity: {request.cantidad}")
                return Response(success=False, message="Quantity must be positive")

            new_donation = Donation(
                description=request.description,
                quantity=request.cantidad,
                is_deleted=False,
                category_id=request.categoria,
                created_at=datetime.datetime.utcnow(),
                created_by=user.id  # Use user.id directly
            )
            session.add(new_donation)
            session.commit()
            logging.info(f"Donation created: description={request.description}, created_by_id={user.id}")
            return Response(success=True, message="Donation created successfully")
        except Exception as e:
            session.rollback()
            logging.error(f"Failed to create donation: {str(e)}")
            return Response(success=False, message=f"Failed to create donation: {str(e)}")
        finally:
            session.close()

    def UpdateDonation(self, request, context):
        session = get_session()
        try:
            user_id = session.query(User).filter_by(username=request.username).first()
            donation = session.query(Donation).filter_by(id=request.id).first()
            if not donation:
                return Response(success=False, message="Donation not found")
            donation.description = request.description
            donation.quantity = request.cantidad
            donation.updated_at = datetime.datetime.utcnow()
            donation.updated_by=user_id.id if user_id else None
            session.commit()
            return Response(success=True, message="Donation updated successfully")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
        finally:
            session.close()

    def DeleteDonation(self, request, context):
        session = get_session()
        try:
            donation = session.query(Donation).filter_by(id=request.id).first()
            if not donation:
                return Response(success=False, message="Donation not found")
            session.query(EventDonation).filter_by(donation_id=donation.id).delete()
            donation.is_deleted = True 
            session.commit()
            return Response(success=True, message="Donation deleted successfully")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
        finally:
            session.close()

    def ListDonations(self, request, context):
        session = get_session()
        try:
            donations = session.query(Donation).filter(Donation.is_deleted == False).all()
            donation_list = DonationList()
            for donation in donations:
                donation_list.donation.add(
                    id=donation.id,
                    description=donation.description,
                    cantidad=donation.quantity,
                    categoria=donation.category_id,
                )
            return donation_list
        finally:
            session.close()