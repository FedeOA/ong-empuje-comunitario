import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from ..producer import KafkaProducerManager
from database.databaseManager import get_session
from database.models import Donation
from datetime import datetime
import logging

class DonationTransferHandler:
    def __init__(self, producer: KafkaProducerManager):
        self.producer = producer

    def publish(self, request_id, donor_org_id, donations, recipient_org_id):
        if not self.producer.producer:
            logging.warning("Kafka producer not initialized, skipping donation transfer publishing")
            return
        topic = f'transferencia_donaciones_{recipient_org_id}'
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
        self.producer.send_message(topic, message)
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

    def consume(self, org_id, callback, group_id=None, topic=None):
        from ..consumer import KafkaConsumerManager
        topic = topic or f'transferencia_donaciones_{org_id}'
        group_id = group_id or f'donation_transfer_{org_id}'
        consumer_manager = KafkaConsumerManager(
            topic=topic,
            bootstrap_servers=self.producer.bootstrap_servers,
            group_id=group_id,
            enable_auto_commit=False
        )
        def process_message(data):
            session = get_session()
            try:
                for donation in data['donations']:
                    db_donation = session.query(Donation).filter_by(
                        category_id=donation['category_id'],
                        description=donation['description'],
                        is_deleted=False
                    ).first()
                    if db_donation:
                        db_donation.quantity += donation['quantity']
                        db_donation.updated_at = datetime.utcnow()
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
                logging.info(f"Processed donation transfer: request_id={data['request_id']}")
                callback(data)
                consumer_manager.consumer.commit()
            except Exception as e:
                session.rollback()
                logging.error(f"Failed to process donation transfer: {str(e)}")
                raise
            finally:
                session.close()
        consumer_manager.consume(process_message)