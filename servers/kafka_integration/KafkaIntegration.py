from kafka import KafkaProducer, KafkaConsumer
from kafka.errors import KafkaError, NoBrokersAvailable
import json
import logging
import os
from database.databaseManager import get_session
from database.models import Donation, DonationOffer, DonationRequest, ExternalEvent, EventAdhesion
from datetime import datetime

# Removed: logging.getLogger('kafka').setLevel(logging.DEBUG)

class KafkaIntegration:
    def __init__(self, bootstrap_servers='localhost:9092'):
        self.producer = None
        self.bootstrap_servers = bootstrap_servers
        if os.getenv('KAFKA_ENABLED', 'true').lower() == 'false':
            logging.info("Kafka integration disabled via environment variable")
            return
        try:
            self.producer = KafkaProducer(
                bootstrap_servers=bootstrap_servers,
                value_serializer=lambda v: json.dumps(v).encode('utf-8'),
                retries=5,
                acks='all',
                request_timeout_ms=30000,
                max_block_ms=30000
            )
            logging.info("Kafka producer initialized successfully")
        except NoBrokersAvailable as e:
            logging.error(f"Kafka broker unavailable at {bootstrap_servers}: {str(e)}")
            logging.warning("Continuing without Kafka integration")
        except Exception as e:
            logging.error(f"Failed to initialize Kafka producer: {str(e)}")
            logging.warning("Continuing without Kafka integration")

    def publish_user_event(self, action, user_data):
        if not self.producer:
            logging.warning("Kafka producer not initialized, skipping user event publishing")
            return
        try:
            message = {
                'service': 'user',
                'action': action,
                'data': user_data
            }
            future = self.producer.send('user_events', message)
            future.get(timeout=10)
            logging.info(f"Published user event: action={action}, data={user_data}")
        except KafkaError as e:
            logging.error(f"Failed to publish user event: {str(e)}")
        except Exception as e:
            logging.error(f"Error processing user event: {str(e)}")

    def publish_donation_event(self, action, donation_data):
        if not self.producer:
            logging.warning("Kafka producer not initialized, skipping donation event publishing")
            return
        try:
            message = {
                'service': 'donation',
                'action': action,
                'data': donation_data
            }
            future = self.producer.send('donation_events', message)
            future.get(timeout=10)
            logging.info(f"Published donation event: action={action}, data={donation_data}")
        except KafkaError as e:
            logging.error(f"Failed to publish donation event: {str(e)}")
        except Exception as e:
            logging.error(f"Error processing donation event: {str(e)}")

    def publish_event_event(self, action, event_data):
        if not self.producer:
            logging.warning("Kafka producer not initialized, skipping event publishing")
            return
        try:
            message = {
                'service': 'event',
                'action': action,
                'data': event_data
            }
            future = self.producer.send('event_events', message)
            future.get(timeout=10)
            logging.info(f"Published event: action={action}, data={event_data}")
        except KafkaError as e:
            logging.error(f"Failed to publish event: {str(e)}")
        except Exception as e:
            logging.error(f"Error processing event: {str(e)}")

    def publish_donation_transfer(self, request_id, donor_org_id, donations, recipient_org_id):
        if not self.producer:
            logging.warning("Kafka producer not initialized, skipping donation transfer publishing")
            return
        try:
            message = {
                'request_id': request_id,
                'donor_org_id': donor_org_id,
                'donations': [
                    {
                        'category_id': donation['category_id'],
                        'description': donation['description'],
                        'quantity': donation['quantity']
                    } for donation in donations
                ]
            }
            topic = f'transferencia_donaciones_{recipient_org_id}'
            future = self.producer.send(topic, message)
            future.get(timeout=10)
            logging.info(f"Published donation transfer to topic {topic}: request_id={request_id}")
            # Update donor's inventory
            session = get_session()
            try:
                for donation in donations:
                    db_donation = session.query(Donation).filter_by(
                        category_id=donation['category_id'],
                        description=donation['description'],
                        is_deleted=False
                    ).first()
                    if db_donation:
                        if db_donation.quantity < donation['quantity']:
                            raise ValueError(f"Insufficient quantity for donation {donation['description']}")
                        db_donation.quantity -= donation['quantity']
                        db_donation.updated_at = datetime.utcnow()
                        self.publish_donation_event('update', {
                            'id': db_donation.id,
                            'description': db_donation.description,
                            'quantity': db_donation.quantity,
                            'category_id': db_donation.category_id
                        })
                    else:
                        raise ValueError(f"Donation {donation['description']} not found")
                session.commit()
                logging.info(f"Updated donor inventory for request_id={request_id}")
            except Exception as e:
                session.rollback()
                logging.error(f"Failed to update donor inventory: {str(e)}")
                raise
            finally:
                session.close()
        except KafkaError as e:
            logging.error(f"Failed to publish donation transfer: {str(e)}")
            raise
        except Exception as e:
            logging.error(f"Error processing donation transfer: {str(e)}")
            raise

    def consume_donation_transfer(self, org_id, callback):
        try:
            consumer = KafkaConsumer(
                f'transferencia_donaciones_{org_id}',
                bootstrap_servers=self.bootstrap_servers,
                auto_offset_reset='earliest',
                enable_auto_commit=True,
                group_id=f'donation_transfer_{org_id}',
                value_deserializer=lambda x: json.loads(x.decode('utf-8'))
            )
            logging.info(f"Started consumer for donation transfers: topic=transferencia_donaciones_{org_id}")
            for message in consumer:
                donation_data = message.value
                logging.info(f"Received donation transfer: request_id={donation_data['request_id']}")
                session = get_session()
                try:
                    for donation in donation_data['donations']:
                        db_donation = session.query(Donation).filter_by(
                            category_id=donation['category_id'],
                            description=donation['description'],
                            is_deleted=False
                        ).first()
                        if db_donation:
                            db_donation.quantity += donation['quantity']
                            db_donation.updated_at = datetime.utcnow()
                            self.publish_donation_event('update', {
                                'id': db_donation.id,
                                'description': db_donation.description,
                                'quantity': db_donation.quantity,
                                'category_id': db_donation.category_id
                            })
                        else:
                            new_donation = Donation(
                                description=donation['description'],
                                quantity=donation['quantity'],
                                is_deleted=False,
                                category_id=donation['category_id'],
                                created_at=datetime.utcnow()
                            )
                            session.add(new_donation)
                        session.commit()
                        logging.info(f"Processed donation transfer: description={donation['description']}, quantity={donation['quantity']}")
                        callback(donation_data)
                except Exception as e:
                    session.rollback()
                    logging.error(f"Failed to process donation transfer: {str(e)}")
                finally:
                    session.close()
        except NoBrokersAvailable as e:
            logging.error(f"Kafka broker unavailable at {self.bootstrap_servers}: {str(e)}")
        except Exception as e:
            logging.error(f"Failed to initialize or run Kafka consumer: {str(e)}")

    def publish_donation_offer(self, offer_id, donor_org_id, donations):
        if not self.producer:
            logging.warning("Kafka producer not initialized, skipping donation offer publishing")
            return
        try:
            message = {
                'offer_id': offer_id,
                'donor_org_id': donor_org_id,
                'donations': [
                    {
                        'category_id': donation['category_id'],
                        'description': donation['description'],
                        'quantity': donation['quantity']
                    } for donation in donations
                ]
            }
            topic = 'oferta_donaciones'
            future = self.producer.send(topic, message)
            future.get(timeout=10)
            logging.info(f"Published donation offer: offer_id={offer_id}, donor_org_id={donor_org_id}")
        except KafkaError as e:
            logging.error(f"Failed to publish donation offer: {str(e)}")
            raise
        except Exception as e:
            logging.error(f"Error processing donation offer: {str(e)}")
            raise

    def consume_donation_offer(self, callback):
        try:
            consumer = KafkaConsumer(
                'oferta_donaciones',
                bootstrap_servers=self.bootstrap_servers,
                auto_offset_reset='earliest',
                enable_auto_commit=True,
                group_id='donation_offer_consumer',
                value_deserializer=lambda x: json.loads(x.decode('utf-8'))
            )
            logging.info("Started consumer for donation offers: topic=oferta_donaciones")
            for message in consumer:
                offer_data = message.value
                logging.info(f"Received donation offer: offer_id={offer_data['offer_id']}")
                callback(offer_data)
        except NoBrokersAvailable as e:
            logging.error(f"Kafka broker unavailable at {self.bootstrap_servers}: {str(e)}")
        except Exception as e:
            logging.error(f"Failed to initialize or run Kafka consumer: {str(e)}")

    def publish_donation_request_cancellation(self, org_id, request_id):
        if not self.producer:
            logging.warning("Kafka producer not initialized, skipping donation request cancellation publishing")
            return
        try:
            message = {
                'org_id': org_id,
                'request_id': request_id
            }
            topic = 'baja_solicitud_donaciones'
            future = self.producer.send(topic, message)
            future.get(timeout=10)
            logging.info(f"Published donation request cancellation: org_id={org_id}, request_id={request_id}")
        except KafkaError as e:
            logging.error(f"Failed to publish donation request cancellation: {str(e)}")
            raise
        except Exception as e:
            logging.error(f"Error processing donation request cancellation: {str(e)}")
            raise

    def consume_donation_request_cancellation(self, callback):
        try:
            consumer = KafkaConsumer(
                'baja_solicitud_donaciones',
                bootstrap_servers=self.bootstrap_servers,
                auto_offset_reset='earliest',
                enable_auto_commit=True,
                group_id='donation_request_cancellation_consumer',
                value_deserializer=lambda x: json.loads(x.decode('utf-8'))
            )
            logging.info("Started consumer for donation request cancellations: topic=baja_solicitud_donaciones")
            for message in consumer:
                cancellation_data = message.value
                logging.info(f"Received donation request cancellation: org_id={cancellation_data['org_id']}, request_id={cancellation_data['request_id']}")
                callback(cancellation_data)
        except NoBrokersAvailable as e:
            logging.error(f"Kafka broker unavailable at {self.bootstrap_servers}: {str(e)}")
        except Exception as e:
            logging.error(f"Failed to initialize or run Kafka consumer: {str(e)}")

    def publish_solidarity_event(self, org_id, event_id, name, description, date_time):
        if not self.producer:
            logging.warning("Kafka producer not initialized, skipping solidarity event publishing")
            return
        try:
            # Validate that date_time is in the future
            event_datetime = datetime.strptime(date_time, '%Y-%m-%d %H:%M:%S')
            current_datetime = datetime.now()
            if event_datetime <= current_datetime:
                raise ValueError("Event date and time must be in the future")
            
            message = {
                'org_id': org_id,
                'event_id': event_id,
                'name': name,
                'description': description,
                'date_time': date_time
            }
            topic = 'eventos_solidarios'
            future = self.producer.send(topic, message)
            future.get(timeout=10)
            logging.info(f"Published solidarity event: event_id={event_id}, name={name}")
        except ValueError as e:
            logging.error(f"Invalid event data: {str(e)}")
            raise
        except KafkaError as e:
            logging.error(f"Failed to publish solidarity event: {str(e)}")
            raise
        except Exception as e:
            logging.error(f"Error processing solidarity event: {str(e)}")
            raise

    def consume_solidarity_event(self, own_org_id, callback):
        try:
            consumer = KafkaConsumer(
                'eventos_solidarios',
                bootstrap_servers=self.bootstrap_servers,
                auto_offset_reset='earliest',
                enable_auto_commit=True,
                group_id='solidarity_event_consumer',
                value_deserializer=lambda x: json.loads(x.decode('utf-8'))
            )
            logging.info("Started consumer for solidarity events: topic=eventos_solidarios")
            for message in consumer:
                event_data = message.value
                logging.info(f"Received solidarity event: event_id={event_data['event_id']}, name={event_data['name']}")
                # Skip events from own organization
                if event_data['org_id'] == own_org_id:
                    logging.info(f"Skipped event from own organization: org_id={own_org_id}, event_id={event_data['event_id']}")
                    continue
                # Validate event is in the future
                event_datetime = datetime.strptime(event_data['date_time'], '%Y-%m-%d %H:%M:%S')
                current_datetime = datetime.now()
                if event_datetime <= current_datetime:
                    logging.info(f"Skipped past event: event_id={event_data['event_id']}, name={event_data['name']}")
                    continue
                callback(event_data)
        except NoBrokersAvailable as e:
            logging.error(f"Kafka broker unavailable at {self.bootstrap_servers}: {str(e)}")
        except Exception as e:
            logging.error(f"Failed to initialize or run Kafka consumer: {str(e)}")

    def publish_event_cancellation(self, org_id, event_id):
        if not self.producer:
            logging.warning("Kafka producer not initialized, skipping event cancellation publishing")
            return
        try:
            message = {
                'org_id': org_id,
                'event_id': event_id
            }
            topic = 'baja_evento_solidario'
            future = self.producer.send(topic, message)
            future.get(timeout=10)
            logging.info(f"Published event cancellation: org_id={org_id}, event_id={event_id}")
        except KafkaError as e:
            logging.error(f"Failed to publish event cancellation: {str(e)}")
            raise
        except Exception as e:
            logging.error(f"Error processing event cancellation: {str(e)}")
            raise

    def consume_event_cancellation(self, callback):
        try:
            consumer = KafkaConsumer(
                'baja_evento_solidario',
                bootstrap_servers=self.bootstrap_servers,
                auto_offset_reset='earliest',
                enable_auto_commit=True,
                group_id='event_cancellation_consumer',
                value_deserializer=lambda x: json.loads(x.decode('utf-8'))
            )
            logging.info("Started consumer for event cancellations: topic=baja_evento_solidario")
            for message in consumer:
                cancellation_data = message.value
                logging.info(f"Received event cancellation: org_id={cancellation_data['org_id']}, event_id={cancellation_data['event_id']}")
                callback(cancellation_data)
        except NoBrokersAvailable as e:
            logging.error(f"Kafka broker unavailable at {self.bootstrap_servers}: {str(e)}")
        except Exception as e:
            logging.error(f"Failed to initialize or run Kafka consumer: {str(e)}")

    def publish_event_adhesion(self, event_id, org_id, volunteer_id, volunteer_name, volunteer_last_name, volunteer_phone, volunteer_email, volunteer_org_id):
        if not self.producer:
            logging.warning("Kafka producer not initialized, skipping event adhesion publishing")
            return
        try:
            message = {
                'event_id': int(event_id),
                'volunteer': {
                    'id': int(volunteer_id),
                    'name': str(volunteer_name),
                    'last_name': str(volunteer_last_name),
                    'phone': str(volunteer_phone) if volunteer_phone else None,
                    'email': str(volunteer_email)
                },
                'org_id': int(volunteer_org_id)
            }
            topic = f'adhesion_evento_{org_id}'
            message_bytes = json.dumps(message).encode('utf-8')
            future = self.producer.send(topic, message_bytes)
            result = future.get(timeout=10)
            logging.info(f"Published event adhesion: event_id={event_id}, volunteer_id={volunteer_id}, topic={topic}, partition={result.partition}, offset={result.offset}")
        except KafkaError as e:
            logging.error(f"Failed to publish event adhesion to {topic}: {str(e)}")
            raise
        except Exception as e:
            logging.error(f"Error processing event adhesion: {str(e)}")
            raise

    def consume_event_adhesion(self, own_org_id, callback):
        try:
            consumer = KafkaConsumer(
                f'adhesion_evento_{own_org_id}',
                bootstrap_servers=self.bootstrap_servers,
                auto_offset_reset='earliest',
                enable_auto_commit=False,
                group_id=f'event_adhesion_consumer_{own_org_id}',
                value_deserializer=lambda x: json.loads(x.decode('utf-8'))
            )
            logging.info(f"Started consumer for event adhesions: topic=adhesion_evento_{own_org_id}")
            for message in consumer:
                adhesion_data = message.value
                logging.info(f"Received event adhesion: event_id={adhesion_data['event_id']}, volunteer_id={adhesion_data['volunteer']['id']}")
                try:
                    callback(adhesion_data)
                    consumer.commit()
                    logging.info(f"Processed and committed adhesion: event_id={adhesion_data['event_id']}")
                except Exception as e:
                    logging.error(f"Failed to process adhesion for event_id={adhesion_data.get('event_id', 'unknown')}: {str(e)}")
        except NoBrokersAvailable as e:
            logging.error(f"Kafka broker unavailable at {self.bootstrap_servers}: {str(e)}")
        except Exception as e:
            logging.error(f"Failed to initialize or run Kafka consumer for adhesion_evento_{own_org_id}: {str(e)}")

    def close(self):
        if self.producer:
            try:
                self.producer.flush(timeout=30)
                self.producer.close(timeout=30)
                logging.info("Kafka producer closed")
            except Exception as e:
                logging.error(f"Error closing Kafka producer: {str(e)}")