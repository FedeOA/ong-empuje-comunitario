from servers.servers_pb2 import Response, EventList
from servers import servers_pb2_grpc
from servers.db.dbManager import get_session
from servers.db.models import Event, UserEvent, EventDonation, User
import datetime

class EventService(servers_pb2_grpc.EventServiceServicer):
    def CreateEvent(self, request, context):
        session = get_session()
        try:
            new_event = Event(
                name=request.name,
                description=request.description,
                event_datetime=datetime.datetime.fromisoformat(request.fecha_hora)
            )
            session.add(new_event)
            session.commit()
            return Response(success=True, message="Event created successfully")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
        finally:
            session.close()

    def UpdateEvent(self, request, context):
        session = get_session()
        try:
            event = session.query(Event).filter_by(id=request.id).first()
            if not event:
                return Response(success=False, message="Event not found")
            event.name = request.name
            event.description = request.description
            event.event_datetime = datetime.datetime.fromisoformat(request.fecha_hora)
            session.commit()
            return Response(success=True, message="Event updated successfully")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
        finally:
            session.close()

    def DeleteEvent(self, request, context):
        session = get_session()
        try:
            event = session.query(Event).filter_by(id=request.id).first()
            if not event:
                return Response(success=False, message="Event not found")
            session.query(UserEvent).filter_by(event_id=event.id).delete()
            session.query(EventDonation).filter_by(event_id=event.id).delete()
            session.delete(event)
            session.commit()
            return Response(success=True, message="Event deleted successfully")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
        finally:
            session.close()

    def ListEvents(self, request, context):
        session = get_session()
        try:
            events = session.query(Event).all()
            event_list = EventList()
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
        session = get_session()
        try:
            user_event = UserEvent(
                user_id=request.user_id,
                event_id=request.event_id
            )
            session.add(user_event)
            session.commit()
            return Response(success=True, message="User added to event")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
        finally:
            session.close()

    def RemoveUser(self, request, context):
        session = get_session()
        try:
            user_event = session.query(UserEvent).filter_by(
                user_id=request.user_id,
                event_id=request.event_id
            ).first()
            if not user_event:
                return Response(success=False, message="User-event relation not found")
            session.delete(user_event)
            session.commit()
            return Response(success=True, message="User removed from event")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
        finally:
            session.close()

    def UseDonations(self, request, context):
        session = get_session()
        try:
            event_donation = EventDonation(
                event_id=request.event_id,
                donation_id=request.donation_id,
                quantity_used=1
            )
            session.add(event_donation)
            session.commit()
            return Response(success=True, message="Donation used in event")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
        finally:
            session.close()