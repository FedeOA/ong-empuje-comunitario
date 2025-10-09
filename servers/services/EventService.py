from services_pb2.event_pb2 import Response, EventList , ExternalEventList
from services_pb2_grpc import event_pb2_grpc
from database.databaseManager import get_session
from database.models import Event, UserEvent, EventDonation, User, VoluntaryEvent, Voluntary
from dateutil import parser
from sqlalchemy.orm import joinedload

class EventService(event_pb2_grpc.EventServiceServicer):
    def CreateEvent(self, request, context):
        session = get_session()
        try:

            event_datetime = parser.parse(request.fecha_hora)

            new_event = Event(
                name=request.name,
                description=request.description,
                is_published=request.is_published,
                event_datetime= event_datetime
            )
            session.add(new_event)
            session.commit()
            return Response(success=True, message="Event created successfully")
        except Exception as e:
            print("Exception es:", e)
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
            
            event_datetime = parser.parse(request.fecha_hora)

            event.name = request.name
            event.description = request.description
            event.is_published = request.is_published
            event.event_datetime =event_datetime
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
            session.query(VoluntaryEvent).filter_by(event_id=event.id).delete()
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
            events = (
                session.query(Event)
                .options(
                    joinedload(Event.user_events).joinedload(UserEvent.user),
                    joinedload(Event.voluntary_events).joinedload(VoluntaryEvent.voluntary),
                )
                .filter(Event.remote_id.is_(None))
                .all()
            )

            event_list = EventList()

            for event in events:
                new_event = event_list.event.add(
                    id=event.id,
                    name=event.name,
                    description=event.description,
                    fecha_hora=event.event_datetime.isoformat(),
                    is_published=event.is_published,
                )

                for ue in event.user_events:
                    if ue.user:
                        new_event.users.append(ue.user.username)
               
                for ve in event.voluntary_events:
                    if ve.voluntary:
                        new_event.users.append(ve.voluntary.email)

            return event_list
        finally:
            session.close()

    def ListExternalEvents(self, request, context):
        
        session = get_session()
        try:
            events = (
                session.query(Event)
                .options(joinedload(Event.user_events).joinedload(UserEvent.user))
                .filter(Event.remote_id.isnot(None))
                .all()
            )
            
            event_list = ExternalEventList()

            for event in events:
                new_event = event_list.externalEvent.add(
                    id=event.id,
                    name=event.name,
                    description=event.description,
                    fecha_hora=event.event_datetime.isoformat(),
                    is_published=event.is_published,
                    remote_id=event.remote_id,
                    organization_id=event.organization_id
                )

                for ue in event.user_events:
                    if ue.user:
                        new_event.users.append(ue.user.username)
                
            return event_list
        finally:
            session.close()

    def AddUser(self, request, context):
        session = get_session()
        try:
            print("Request to add user:", request.username, "to event:", request.event_id)
            user_id = session.query(User).filter_by(username=request.username).first()
            
            if not user_id:
                return Response(success=False, message="User not found")
            
            user_event = UserEvent(
                user_id= user_id.id,
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
            user_id = session.query(User).filter_by(username=request.username).first()
            
            if not user_id:
                user_id = session.query(Voluntary).filter_by(email=request.email).first()
                if not user_id:
                    return Response(success=False, message="User not found")
                else:
                    voluntary_event = session.query(VoluntaryEvent).filter_by(
                    voluntary_id= user_id.id,
                    event_id=request.event_id
                    ).first()
                    if not voluntary_event:
                        return Response(success=False, message="Voluntary-event relation not found")
                    session.delete(voluntary_event)
                    session.commit()
            else:
                user_event = session.query(UserEvent).filter_by(
                user_id= user_id.id,
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