import logging
import os
import sys
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from services_pb2.donation_pb2 import Response, DonationList
from services_pb2_grpc import donation_pb2_grpc
from database.databaseManager import get_session
from database.models import Donation, DonationRequest, EventDonation, User
from kafka_module.kafka_manager import KafkaManager  # type: ignore
import datetime

class DonationService(donation_pb2_grpc.DonationServiceServicer):
    def __init__(self):
        self.kafka = KafkaManager(bootstrap_servers=os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'localhost:9092'))

    def CreateDonation(self, request, context):
        session = get_session()
        try:
            user_id = session.query(User).filter_by(username=request.username).first()
            new_donation = Donation(
                description=request.description,
                quantity=request.cantidad,
                is_deleted=False,
                category_id=request.categoria,
                created_at=datetime.datetime.utcnow(),
                created_by=user_id.id if user_id else None
            )
            session.add(new_donation)
            session.commit()

            donation_data = {
                'id': new_donation.id,
                'description': new_donation.description,
                'quantity': new_donation.quantity,
                'category_id': new_donation.category_id
            }
            self.kafka.publish_donation_event('create', donation_data)

            return Response(success=True, message="Donation created successfully")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
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
            donation.updated_by = user_id.id if user_id else None
            session.commit()

            donation_data = {
                'id': donation.id,
                'description': donation.description,
                'quantity': donation.quantity,
                'category_id': donation.category_id
            }
            self.kafka.publish_donation_event('update', donation_data)

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
            session.delete(donation)
            session.commit()

            donation_data = {
                'id': donation.id,
                'description': donation.description
            }
            self.kafka.publish_donation_event('delete', donation_data)

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
            
    def TransferDonation(self, request, context):
        session = get_session()
        try:
            donations = [
                {
                    'category_id': donation.categoria,
                    'description': donation.description,
                    'quantity': donation.cantidad
                } for donation in request.donations
            ]
            self.kafka.publish_donation_transfer(
                request_id=request.request_id,
                donor_org_id=request.donor_org_id,
                donations=donations,
                recipient_org_id=request.recipient_org_id
            )
            return Response(success=True, message="Donation transfer published successfully")
        except Exception as e:
            return Response(success=False, message=str(e))
        finally:
            session.close()
            
    def OfferDonation(self, request, context):
        try:
            donations = [
                {
                    'category_id': donation.categoria,
                    'description': donation.description,
                    'quantity': donation.cantidad
                } for donation in request.donations
            ]
            self.kafka.publish_donation_offer(
                offer_id=request.offer_id,
                donor_org_id=request.donor_org_id,
                donations=donations
            )
            return Response(success=True, message="Donation offer published successfully")
        except Exception as e:
            return Response(success=False, message=str(e))
            
    def CancelDonationRequest(self, request, context):
        try:
            self.kafka.publish_donation_request_cancellation(
                org_id=request.org_id,
                request_id=request.request_id
            )
            # Optionally, mark the request as canceled in the local database
            session = get_session()
            try:
                db_request = session.query(DonationRequest).filter_by(
                    request_id=request.request_id,
                    org_id=request.org_id
                ).first()
                if db_request:
                    db_request.is_canceled = True
                    session.commit()
                else:
                    logging.warning(f"Donation request {request.request_id} not found locally")
            except Exception as e:
                session.rollback()
                logging.error(f"Failed to update donation request: {str(e)}")
            finally:
                session.close()
            return Response(success=True, message="Donation request cancellation published successfully")
        except Exception as e:
            return Response(success=False, message=str(e))
