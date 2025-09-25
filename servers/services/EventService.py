import logging
import os

import grpc
from services_pb2 import event_pb2
from services_pb2.event_pb2 import Response, EventList
from services_pb2_grpc import event_pb2_grpc
from database.databaseManager import get_session
from database.models import Event, ExternalEvent, UserEvent, EventDonation, User, EventAdhesion
from kafka_integration.KafkaIntegration import KafkaIntegration
import datetime

class EventService(event_pb2_grpc.EventServiceServicer):
    def __init__(self):
        self.kafka = KafkaIntegration()

    def CreateEvent(self, request, context):
        session = get_session()
        try:
            event = Event(
                name=request.name,
                description=request.description,
                event_datetime=datetime.datetime.strptime(request.fecha_hora, '%Y-%m-%dT%H:%M:%S')
            )
            session.add(event)
            session.commit()
            logging.info(f"Created event: name={request.name}, id={event.id}")

            event_data = {
                'id': event.id,
                'name': event.name,
                'description': event.description,
                'fecha_hora': event.event_datetime.isoformat()
            }
            self.kafka.publish_event_event('create', event_data)

            return Response(success=True, message="Event created successfully")
        except Exception as e:
            session.rollback()
            logging.error(f"Failed to create event: {str(e)}")
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
            event.event_datetime = datetime.datetime.strptime(request.fecha_hora, '%Y-%m-%dT%H:%M:%S')
            session.commit()

            event_data = {
                'id': event.id,
                'name': event.name,
                'description': event.description,
                'fecha_hora': event.event_datetime.isoformat()
            }
            self.kafka.publish_event_event('update', event_data)

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

            event_data = {
                'id': event.id,
                'name': event.name
            }
            self.kafka.publish_event_event('delete', event_data)

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
            user = session.query(User).filter_by(username=request.username).first()
            if not user:
                return Response(success=False, message="User not found")
            if user.role_id != 4:  # VOLUNTARIO
                return Response(success=False, message="Only VOLUNTARIO users can be added to events")
            user_event = UserEvent(
                user_id=user.id,
                event_id=request.event_id
            )
            session.add(user_event)
            session.commit()

            event_data = {
                'event_id': request.event_id,
                'user_id': user.id,
                'action': 'add_user'
            }
            self.kafka.publish_event_event('add_user', event_data)

            return Response(success=True, message="User added to event")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
        finally:
            session.close()

    def RemoveUser(self, request, context):
        session = get_session()
        try:
            user = session.query(User).filter_by(username=request.username).first()
            if not user:
                return Response(success=False, message="User not found")
            if user.role_id != 4:  # VOLUNTARIO
                return Response(success=False, message="Only VOLUNTARIO users can be removed from events")
            user_event = session.query(UserEvent).filter_by(
                user_id=user.id,
                event_id=request.event_id
            ).first()
            if not user_event:
                return Response(success=False, message="User-event relation not found")
            session.delete(user_event)
            session.commit()

            event_data = {
                'event_id': request.event_id,
                'user_id': user.id,
                'action': 'remove_user'
            }
            self.kafka.publish_event_event('remove_user', event_data)

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

            event_data = {
                'event_id': request.event_id,
                'donation_id': request.donation_id,
                'action': 'use_donation'
            }
            self.kafka.publish_event_event('use_donation', event_data)

            return Response(success=True, message="Donation used in event")
        except Exception as e:
            session.rollback()
            return Response(success=False, message=str(e))
        finally:
            session.close()

    def PublishSolidarityEvent(self, request, context):
        try:
            self.kafka.publish_solidarity_event(
                org_id=request.org_id,
                event_id=request.event_id,
                name=request.name,
                description=request.description,
                date_time=request.date_time
            )
            return Response(success=True, message="Solidarity event published successfully")
        except Exception as e:
            return Response(success=False, message=str(e))

    def CancelSolidarityEvent(self, request, context):
        try:
            self.kafka.publish_event_cancellation(
                org_id=request.org_id,
                event_id=request.event_id
            )
            session = get_session()
            try:
                db_event = session.query(Event).filter_by(id=request.event_id).first()
                if db_event:
                    db_event.is_deleted = True
                    session.commit()
                else:
                    logging.warning(f"Event {request.event_id} not found locally")
            except Exception as e:
                session.rollback()
                logging.error(f"Failed to update local event: {str(e)}")
            finally:
                session.close()
            return Response(success=True, message="Event cancellation published successfully")
        except Exception as e:
            return Response(success=False, message=str(e))

    def ListExternalEvents(self, request, context):
        session = get_session()
        try:
            current_datetime = datetime.now()
            events = session.query(ExternalEvent).filter(
                ExternalEvent.is_canceled == False,
                ExternalEvent.date_time > current_datetime
            ).all()
            return event_pb2.EventList(
                events=[
                    event_pb2.Event(
                        id=event.event_id,
                        name=event.name,
                        description=event.description,
                        date_time=event.date_time.strftime('%Y-%m-%d %H:%M:%S')
                    ) for event in events
                ]
            )
        finally:
            session.close()

    def AdhereToEvent(self, request, context):
        session = get_session()
        try:
            # Verify external event exists and is not canceled
            event = session.query(ExternalEvent).filter_by(
                event_id=request.event_id,
                org_id=request.org_id,
                is_canceled=False
            ).first()
            if not event:
                return event_pb2.EventAdhesionResponse(
                    success=False,
                    message="Event not found or canceled"
                )

            # Validate volunteer
            volunteer = session.query(User).filter_by(id=request.volunteer_id).first()
            if not volunteer:
                return event_pb2.EventAdhesionResponse(
                    success=False,
                    message="Volunteer not found"
                )

            # Create adhesion record
            adhesion = EventAdhesion(
                event_id=request.event_id,
                org_id=request.org_id,
                volunteer_id=request.volunteer_id,
                volunteer_name=request.volunteer_name,
                volunteer_last_name=request.volunteer_last_name,
                volunteer_phone=request.volunteer_phone,
                volunteer_email=request.volunteer_email,
                volunteer_org_id=request.org_id,  # Default to event's org_id
                created_at=datetime.datetime.now()
            )
            session.add(adhesion)
            session.commit()
            logging.info(f"Adhesion stored: event_id={request.event_id}, volunteer_id={request.volunteer_id}, org_id={request.org_id}")

            # Publish to Kafka
            topic = f"adhesion-evento-{request.org_id}"  # Use valid topic name
            message = {
                "event_id": request.event_id,
                "org_id": request.org_id,
                "volunteer_id": request.volunteer_id,
                "volunteer_name": request.volunteer_name,
                "volunteer_last_name": request.volunteer_last_name,
                "volunteer_phone": request.volunteer_phone or "",
                "volunteer_email": request.volunteer_email,
                "volunteer_org_id": request.org_id,
                "created_at": datetime.datetime.now().isoformat()
            }
            logging.debug(f"Sending Kafka message to topic {topic}: {message}")
            if self.kafka.producer is None:
                logging.warning("Kafka producer not initialized, skipping message send")
            else:
                self.kafka.producer.send(topic, message)
                self.kafka.producer.flush()
                logging.info(f"Published adhesion to Kafka topic {topic}")

            return event_pb2.EventAdhesionResponse(
                success=True,
                message="Adhesion published successfully"
            )
        except Exception as e:
            logging.error(f"AdhereToEvent failed: {str(e)}")
            context.set_details(str(e))
            context.set_code(grpc.StatusCode.UNKNOWN)
            return event_pb2.EventAdhesionResponse(success=False, message=str(e))
        finally:
            session.close()
            
    def ListEventAdhesions(self, request, context):
        session = get_session()
        try:
            adhesions = session.query(EventAdhesion).filter_by(org_id=int(os.getenv('ORG_ID', '1'))).all()
            return event_pb2.EventAdhesionList(
                adhesions=[
                    event_pb2.EventAdhesion(
                        event_id=adhesion.event_id,
                        org_id=adhesion.org_id,
                        volunteer_id=adhesion.volunteer_id,
                        volunteer_name=adhesion.volunteer_name,
                        volunteer_last_name=adhesion.volunteer_last_name,
                        volunteer_phone=adhesion.volunteer_phone,
                        volunteer_email=adhesion.volunteer_email,
                        volunteer_org_id=adhesion.volunteer_org_id
                    ) for adhesion in adhesions
                ]
            )
        finally:
            session.close()