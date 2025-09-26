import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from ..producer import KafkaProducerManager
from database.databaseManager import get_session
from database.models import EventAdhesion
from datetime import datetime
import logging
import json

class EventAdhesionHandler:
    def __init__(self, producer: KafkaProducerManager):
        self.producer = producer

    def publish(self, event_id, org_id, volunteer_id, volunteer_name, volunteer_last_name, volunteer_phone, volunteer_email, volunteer_org_id):
        if not self.producer.producer:
            logging.warning("Kafka producer not initialized, skipping event adhesion publishing")
            return
        topic = f'adhesion_evento_{org_id}'
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
        self.producer.send_message(topic, message)

    def consume(self, own_org_id, callback, group_id=None, topic=None):
        from ..consumer import KafkaConsumerManager
        topic = topic or f'adhesion_evento_{own_org_id}'
        group_id = group_id or f'event_adhesion_consumer_{own_org_id}'
        consumer_manager = KafkaConsumerManager(
            topic=topic,
            bootstrap_servers=self.producer.bootstrap_servers,
            group_id=group_id,
            enable_auto_commit=False
        )
        def process_message(adhesion_data):
            session = get_session()
            try:
                existing = session.query(EventAdhesion).filter_by(
                    event_id=adhesion_data['event_id'],
                    org_id=adhesion_data['org_id'],
                    volunteer_id=adhesion_data['volunteer']['id']
                ).first()
                if existing:
                    logging.info(f"Skipping duplicate adhesion: event_id={adhesion_data['event_id']}, volunteer_id={adhesion_data['volunteer']['id']}")
                    callback(adhesion_data)
                    consumer_manager.consumer.commit()
                    return

                adhesion = EventAdhesion(
                    event_id=adhesion_data['event_id'],
                    org_id=adhesion_data['org_id'],
                    volunteer_id=adhesion_data['volunteer']['id'],
                    volunteer_name=adhesion_data['volunteer']['name'],
                    volunteer_last_name=adhesion_data['volunteer']['last_name'],
                    volunteer_phone=adhesion_data['volunteer']['phone'],
                    volunteer_email=adhesion_data['volunteer']['email'],
                    volunteer_org_id=adhesion_data['org_id'],
                    created_at=datetime.utcnow()
                )
                session.add(adhesion)
                session.commit()
                logging.info(f"Stored event adhesion: event_id={adhesion_data['event_id']}, volunteer_id={adhesion_data['volunteer']['id']}")
                callback(adhesion_data)
                consumer_manager.consumer.commit()
            except Exception as e:
                session.rollback()
                logging.error(f"Failed to process event adhesion: {str(e)}")
                raise
            finally:
                session.close()
        consumer_manager.consume(process_message)