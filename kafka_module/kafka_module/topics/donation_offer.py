import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from ..producer import KafkaProducerManager
import logging

class DonationOfferHandler:
    def __init__(self, producer: KafkaProducerManager):
        self.producer = producer

    def publish(self, offer_id, donor_org_id, donations):
        if not self.producer.producer:
            logging.warning("Kafka producer not initialized, skipping donation offer publishing")
            return
        topic = 'oferta_donaciones'
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
        self.producer.send_message(topic, message)

    def consume(self, callback, group_id='donation_offer_consumer', topic='oferta_donaciones'):
        from ..consumer import KafkaConsumerManager
        logging.info(f"Starting consumer for topic: {topic}, group_id: {group_id}")
        consumer_manager = KafkaConsumerManager(
            topic=topic,
            bootstrap_servers=self.producer.bootstrap_servers,
            group_id=group_id,
            enable_auto_commit=False
        )
        logging.info(f"Consumer assigned partitions: {consumer_manager.consumer.assignment()}")
        def process_message(offer_data):
            logging.info(f"Processing message: {offer_data}")
            callback(offer_data)
            consumer_manager.consumer.commit()
            logging.info("Message committed")
        try:
            consumer_manager.consume(process_message)
        except Exception as e:
            logging.error(f"Error in consumer: {str(e)}")
            raise
        finally:
            consumer_manager.close()
            logging.info(f"Closed consumer for topic: {topic}")