import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
import json
import time
import uuid
from kafka import KafkaConsumer, TopicPartition, KafkaAdminClient
from kafka.admin import NewTopic
from kafka.errors import UnknownTopicOrPartitionError
from kafka_module.topics.donation_offer import DonationOfferHandler
from kafka_module.producer import KafkaProducerManager

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

def test_publish():
    print("Testing publish method...")
    bootstrap_servers = 'localhost:9092'
    producer = KafkaProducerManager(bootstrap_servers=bootstrap_servers)
    handler = DonationOfferHandler(producer)
    
    offer_id = 1
    donor_org_id = 1
    donations = [{'category_id': 1, 'description': 'Puré de tomates', 'quantity': 2}]
    expected_message = {
        'offer_id': offer_id,
        'donor_org_id': donor_org_id,
        'donations': donations
    }
    
    try:
        handler.publish(offer_id, donor_org_id, donations)
        print("test_publish: OK")
        
        consumer = KafkaConsumer(
            'oferta_donaciones',
            bootstrap_servers=bootstrap_servers,
            auto_offset_reset='earliest',
            enable_auto_commit=True,
            group_id=f'test_donation_offer_{uuid.uuid4()}',
            value_deserializer=lambda x: json.loads(x.decode('utf-8'))
        )
        
        time.sleep(1)
        message_found = False
        for message in consumer:
            received_message = message.value
            if (received_message['offer_id'] == expected_message['offer_id'] and
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
    producer = KafkaProducerManager(bootstrap_servers=bootstrap_servers)
    handler = DonationOfferHandler(producer)
    
    offer_id = 1
    donor_org_id = 1
    donations = [{'category_id': 1, 'description': 'Puré de tomates', 'quantity': 2}]
    expected_message = {
        'offer_id': offer_id,
        'donor_org_id': donor_org_id,
        'donations': donations
    }
    
    try:
        callback_data = []
        def callback(data):
            print(f"Callback received data: {data}")
            callback_data.append(data)
        
        group_id = f"test_donation_offer_{uuid.uuid4()}"
        import threading
        consumer_thread = threading.Thread(
            target=handler.consume,
            args=(callback, group_id)
        )
        consumer_thread.daemon = True
        consumer_thread.start()
        
        time.sleep(5)  # Allow consumer to start and assign partitions
        
        producer.send_message('oferta_donaciones', expected_message)
        print("Test message published for consumption")
        
        time.sleep(5)  # Allow time for processing
        
        if expected_message in callback_data:
            print("test_consume: OK - Callback received correct data")
            return True
        else:
            print(f"test_consume: FAILED - Expected {expected_message}, got {len(callback_data)} callbacks")
            return False
    except Exception as e:
        print(f"test_consume: FAILED - Error: {str(e)}")
        return False
    finally:
        producer.close()

def main():
    print("Testing DonationOfferHandler...")
    publish_result = test_publish()
    consume_result = test_consume()
    
    if publish_result and consume_result:
        print("All DonationOfferHandler tests passed!")
    else:
        print("One or more DonationOfferHandler tests failed!")

if __name__ == '__main__':
    main()