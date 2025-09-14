from servers.servers_pb2 import Response, DonationList
from servers import servers_pb2_grpc
from servers.db.dbManager import get_session
from servers.db.models import Donation, EventDonation
import datetime


class DonationService(servers_pb2_grpc.DonationServiceServicer):
    def CreateDonation(self, request, context):
        session = get_session()
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
            return Response(success=True, message="Donation created successfully")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
        finally:
            session.close()

    def UpdateDonation(self, request, context):
        session = get_session()
        try:
            donation = session.query(Donation).filter_by(id=request.id).first()
            if not donation:
                return Response(success=False, message="Donation not found")
            donation.description = request.description
            donation.quantity = request.cantidad
            donation.is_deleted = request.eliminado
            donation.updated_at = datetime.datetime.utcnow()
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
            session.delete(donation)
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
            donations = session.query(Donation).all()
            donation_list = DonationList()
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