import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
import json
import time
import uuid
from datetime import datetime
from kafka import KafkaConsumer, TopicPartition, KafkaAdminClient
from kafka.admin import NewTopic
from kafka.errors import UnknownTopicOrPartitionError
from kafka_module.topics.donation_transfer import DonationTransferHandler
from kafka_module.producer import KafkaProducerManager
from database.databaseManager import get_session
from database.models import Donation

def reset_topic(topic, bootstrap_servers):
    try:
        admin_client = KafkaAdminClient(bootstrap_servers=bootstrap_servers)
        admin_client.create_topics(
            [NewTopic(name=topic, num_partitions=1, replication_factor=1)],
            validate_only=False
        )
        admin_client.close()
    except UnknownTopicOrPartitionError:
        pass  # Topic already exists
    except Exception as e:
        print(f"Failed to create topic {topic}: {str(e)}")

    consumer = KafkaConsumer(
        bootstrap_servers=bootstrap_servers,
        auto_offset_reset='earliest',
        enable_auto_commit=False,
        group_id=f'reset_{uuid.uuid4()}'
    )
    consumer.subscribe([topic])
    
    timeout = 10  # seconds
    start_time = time.time()
    while not consumer.assignment() and time.time() - start_time < timeout:
        consumer.poll(timeout_ms=1000)
    
    partitions = consumer.partitions_for_topic(topic)
    if partitions:
        for partition in partitions:
            tp = TopicPartition(topic, partition)
            consumer.seek_to_beginning(tp)
    else:
        print(f"No partitions found for topic {topic}")
    consumer.close()

def seed_donation():
    session = get_session()
    try:
        donation = session.query(Donation).filter_by(
            category_id=1,
            description='Puré de tomates',
            is_deleted=False
        ).first()
        if not donation:
            donation = Donation(
                description='Puré de tomates',
                quantity=100,
                is_deleted=False,
                category_id=1,
                created_at=datetime.utcnow()
            )
            session.add(donation)
        else:
            donation.quantity = 100
            donation.updated_at = datetime.utcnow()
        session.commit()
    finally:
        session.close()

def test_publish():
    print("Testing publish method...")
    bootstrap_servers = 'localhost:9092'
    seed_donation()
    
    producer = KafkaProducerManager(bootstrap_servers=bootstrap_servers)
    handler = DonationTransferHandler(producer)
    
    request_id = 1
    donor_org_id = 1
    donations = [{'category_id': 1, 'description': 'Puré de tomates', 'quantity': 2}]
    recipient_org_id = 2
    expected_message = {
        'request_id': request_id,
        'donor_org_id': donor_org_id,
        'donations': donations
    }
    
    try:
        handler.publish(request_id, donor_org_id, donations, recipient_org_id)
        print("test_publish: OK")
        
        consumer = KafkaConsumer(
            f'transferencia_donaciones_{recipient_org_id}',
            bootstrap_servers=bootstrap_servers,
            auto_offset_reset='earliest',
            enable_auto_commit=True,
            group_id=f'test_donation_transfer_{uuid.uuid4()}',
            value_deserializer=lambda x: json.loads(x.decode('utf-8'))
        )
        
        time.sleep(1)
        message_found = False
        for message in consumer:
            received_message = message.value
            if (received_message['request_id'] == expected_message['request_id'] and
                received_message['donor_org_id'] == expected_message['donor_org_id'] and
                received_message['donations'] == expected_message['donations']):
                print("Published message verified in Kafka topic")
                message_found = True
                break
        consumer.close()
        
        if not message_found:
            print("test_publish: FAILED - Message not found in Kafka topic")
            return False
        return True
    except Exception as e:
        print(f"test_publish: FAILED - Error: {str(e)}")
        return False
    finally:
        producer.close()

def test_consume():
    print("Testing consume method...")
    bootstrap_servers = 'localhost:9092'
    test_topic = f'transferencia_donaciones_test_{uuid.uuid4()}'
    reset_topic(test_topic, bootstrap_servers)
    
    producer = KafkaProducerManager(bootstrap_servers=bootstrap_servers)
    handler = DonationTransferHandler(producer)
    
    org_id = 1
    request_id = 1
    donor_org_id = 2
    donations = [{'category_id': 1, 'description': 'Puré de tomates', 'quantity': 2}]
    expected_message = {
        'request_id': request_id,
        'donor_org_id': donor_org_id,
        'donations': donations
    }
    
    try:
        producer.send_message(test_topic, expected_message)
        print("Test message published for consumption")
        
        callback_data = []
        def callback(data):
            print(f"Callback received data: {data}")
            callback_data.append(data)
        
        group_id = f"test_donation_transfer_{uuid.uuid4()}"
        import threading
        consumer_thread = threading.Thread(
            target=handler.consume,
            args=(org_id, callback, group_id, test_topic)
        )
        consumer_thread.daemon = True
        consumer_thread.start()
        
        time.sleep(10)
        
        session = get_session()
        try:
            donation = session.query(Donation).filter_by(
                category_id=donations[0]['category_id'],
                description=donations[0]['description']
            ).first()
            if donation and donation.quantity >= donations[0]['quantity']:
                print(f"test_consume: OK - Found updated donation in database: description={donation.description}, quantity={donation.quantity}")
                return True
            else:
                print("test_consume: FAILED - Donation not found or quantity mismatch in database")
                return False
        finally:
            session.close()
    except Exception as e:
        print(f"test_consume: FAILED - Error: {str(e)}")
        return False
    finally:
        producer.close()

def main():
    print("Testing DonationTransferHandler...")
    publish_result = test_publish()
    consume_result = test_consume()
    
    if publish_result and consume_result:
        print("All DonationTransferHandler tests passed!")
    else:
        print("One or more DonationTransferHandler tests failed!")

if __name__ == '__main__':
    main()