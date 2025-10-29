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
            print(f"\n=== UpdateEvent called ===\nRequest: id={request.id}, name={request.name}, fecha_hora={request.fecha_hora}, is_published={request.is_published}\n")
            event = session.query(Event).filter_by(id=request.id).first()
            if not event:
                return Response(success=False, message="Event not found")
            try:
                event_datetime = parser.parse(request.fecha_hora)
            except Exception as e:
                print(f"Error parsing fecha_hora '{request.fecha_hora}': {e}")
                raise

            try:
                from sqlalchemy import text
                update_sql = text(
                    "UPDATE events SET name = :name, description = :description, is_published = :is_published, event_datetime = :event_datetime WHERE id = :id"
                )
                session.execute(update_sql, {
                    "name": request.name,
                    "description": request.description,
                    "is_published": bool(request.is_published),
                    "event_datetime": event_datetime,
                    "id": event.id
                })
                session.flush()
                raw_before = session.execute(text("SELECT is_published FROM events WHERE id = :id"), {"id": event.id}).scalar()
                print(f"Raw is_published value after explicit UPDATE (session): {raw_before} (type: {type(raw_before)})")
                session.commit()
                print(f"UpdateEvent: committed event id={event.id} (is_published={request.is_published})")
                try:
                    verify_sess = get_session()
                    try:
                        raw_verify = verify_sess.execute(text("SELECT is_published FROM events WHERE id = :id"), {"id": event.id}).scalar()
                        print(f"Raw is_published value after commit (new session): {raw_verify} (type: {type(raw_verify)})")
                    finally:
                        verify_sess.close()
                except Exception as e:
                    print(f"Could not verify raw value in new session: {e}")
            except Exception as e:
                print(f"Explicit UPDATE failed: {e}")
                raise
            return Response(success=True, message="Event updated successfully")
        except Exception as e:
            session.rollback()
            import traceback
            tb = traceback.format_exc()
            print(f"Exception in UpdateEvent: {e}\nTraceback:\n{tb}")
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
        print("\n=== Starting ListEvents ===")
        session = None
        try:
            session = get_session()
            print("Session created successfully")
            try:
                from sqlalchemy import text
                raw_events = session.execute(text("SELECT id, is_published FROM events WHERE remote_id IS NULL")).fetchall()
                print("\nRAW DATABASE VALUES:")
                for row in raw_events:
                    print(f"Event {row[0]} - Raw DB is_published value: {row[1]} (type: {type(row[1])})")
            except Exception as e:
                print(f"Error getting raw values: {str(e)}")
            
            print("About to execute queries")
            try:
                from sqlalchemy.orm import selectinload
                events_count = session.query(Event).count()
                print(f"Found {events_count} total events in database")
                print("Executing main query")
                events = (
                    session.query(Event)
                    .options(
                        selectinload(Event.user_events).selectinload(UserEvent.user),
                        selectinload(Event.voluntary_events).selectinload(VoluntaryEvent.voluntary),
                        selectinload(Event.event_donations).selectinload(EventDonation.donation),
                    )
                    .filter(Event.remote_id.is_(None))
                    .all()
                )
                print(f"Main query successful. Found {len(events)} events")
            except Exception as e:
                print(f"Error querying events: {str(e)}")
                print(f"Error type: {type(e)}")
                import traceback
                print(f"Traceback: {traceback.format_exc()}")
                return EventList()

            print("Donation in event:")
            event_list = EventList()

            for event in events:
                # Consulta directa para obtener el valor real de is_published
                try:
                    raw_value = session.execute(text("SELECT is_published FROM events WHERE id = :id"), 
                                              {"id": event.id}).scalar()
                    print(f"Raw value from direct query for event {event.id}: {raw_value} (type: {type(raw_value)})")
                    
                    # Si es bytes, convertir a bool
                    if isinstance(raw_value, bytes):
                        is_published = raw_value == b'\x01'
                    # Si es número (0 o 1), convertir a bool
                    elif isinstance(raw_value, (int, float)):
                        is_published = bool(raw_value)
                    # Si es string, manejar '0', '1', 'true', 'false'
                    elif isinstance(raw_value, str):
                        is_published = raw_value.lower() in ('true', '1', 't')
                    # Si ya es bool, usar directamente
                    elif isinstance(raw_value, bool):
                        is_published = raw_value
                    else:
                        # Valor por defecto si no podemos determinar
                        is_published = False
                    
                    print(f"Event {event.id} - Original value: {event.is_published} (type: {type(event.is_published)})")
                    print(f"Event {event.id} - Converted value: {is_published} (type: {type(is_published)})")
                except Exception as e:
                    print(f"Error converting is_published for event {event.id}: {e}")
                    is_published = False
                
                try:
                    new_event = event_list.event.add(
                        id=event.id,
                        name=event.name,
                        description=event.description,
                        fecha_hora=event.event_datetime.isoformat(),
                        is_published=is_published
                    )
                except Exception as e:
                    print(f"Error creating event proto for event {event.id}: {e}")
                    continue
                    
                
                for ed in event.event_donations:
                    print("Donation in event:", ed)
                    if ed.donation:
                        new_donation = new_event.donations.add()
                        new_donation.category_id = ed.donation.category_id
                        new_donation.description = ed.donation.description
                        new_donation.quantity_used = ed.quantity_used

                for ue in event.user_events:
                    if ue.user:
                        new_event.users.append(ue.user.username)
               
                for ve in event.voluntary_events:
                    if ve.voluntary:
                        new_event.users.append(ve.voluntary.email)

            print("Successfully created event_list, returning response")
            return event_list
        except Exception as e:
            print(f"Error in ListEvents: {str(e)}")
            import traceback
            print(f"Traceback: {traceback.format_exc()}")
            return EventList()
        finally:
            if session:
                try:
                    session.close()
                    print("Session closed successfully")
                except Exception as e:
                    print(f"Error closing session: {str(e)}")

    def ListExternalEvents(self, request, context):
        
        session = get_session()
        try:
            events = (
                session.query(Event)
                .options(
                    joinedload(Event.user_events).joinedload(UserEvent.user),
                    joinedload(Event.voluntary_events).joinedload(VoluntaryEvent.voluntary),
                )
                .filter(Event.remote_id.isnot(None))
                .all()
            )
            
            event_list = ExternalEventList()

            for event in events:
                # Read raw DB value for is_published and convert reliably (handles bytes/ints/strings)
                try:
                    from sqlalchemy import text
                    raw_value = session.execute(text("SELECT is_published FROM events WHERE id = :id"), {"id": event.id}).scalar()
                    if isinstance(raw_value, bytes):
                        is_published = raw_value == b'\x01'
                    elif isinstance(raw_value, (int, float)):
                        is_published = bool(raw_value)
                    elif isinstance(raw_value, str):
                        is_published = raw_value.lower() in ('true', '1', 't')
                    elif isinstance(raw_value, bool):
                        is_published = raw_value
                    else:
                        is_published = False
                except Exception:
                    is_published = bool(event.is_published)

                new_event = event_list.externalEvent.add(
                    id=event.id,
                    name=event.name,
                    description=event.description,
                    fecha_hora=event.event_datetime.isoformat(),
                    is_published=is_published,
                    remote_id=event.remote_id,
                    organization_id=event.organization_id
                )

                # append users from UserEvent (local users)
                for ue in event.user_events:
                    if ue.user:
                        new_event.users.append(ue.user.username)

                # append users from VoluntaryEvent (voluntaries who joined)
                for ve in getattr(event, 'voluntary_events', []):
                    if ve and ve.voluntary and ve.voluntary.email:
                        new_event.users.append(ve.voluntary.email)
                
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