import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..')))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..')))
from kafka import KafkaProducer
import time

try:
    producer = KafkaProducer(
        bootstrap_servers='localhost:9092',
        value_serializer=lambda v: v  # Use raw bytes for test message
    )
    future = producer.send('test_topic', b'Test message')
    future.get(timeout=10)  # Wait for send to complete
    print("Message sent successfully")
    producer.flush(timeout=30)  # Allow more time to flush
    producer.close(timeout=30)  # Allow more time to close
except Exception as e:
    print(f"Error: {str(e)}")
finally:
    if 'producer' in locals():
        producer.close(timeout=30)