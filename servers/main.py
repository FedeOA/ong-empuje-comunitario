import datetime
import logging
import os
import sys
import subprocess
from threading import Thread
import time
import grpc
from concurrent.futures import ThreadPoolExecutor
from kafka_integration.KafkaIntegration import KafkaIntegration
from database.models import DonationOffer, DonationRequest, EventAdhesion, ExternalEvent

# Asegurar que los directorios de salida estén en el PYTHONPATH
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "services_pb2")))
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "services_pb2_grpc")))

# Compilar los archivos .proto y corregir importaciones
def compile_proto():
    proto_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "protos"))
    proto_files = ["user.proto", "authorize.proto", "event.proto", "donation.proto"]
    proto_paths = [os.path.join(proto_dir, proto_file) for proto_file in proto_files]
    
    for proto_path in proto_paths:
        if not os.path.exists(proto_path):
            print(f"❌ No se encontró el archivo .proto en: {proto_path}")
            return False
    
    pb2_output_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "services_pb2"))
    pb2_grpc_output_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "services_pb2_grpc"))
    os.makedirs(pb2_output_dir, exist_ok=True)
    os.makedirs(pb2_grpc_output_dir, exist_ok=True)
    
    command = [
        sys.executable,
        "-m", "grpc_tools.protoc",
        f"-I{proto_dir}",
        f"--python_out={pb2_output_dir}",
        f"--grpc_python_out={pb2_grpc_output_dir}",
    ] + proto_paths
    
    print("🔧 Compilando archivos .proto...")
    try:
        result = subprocess.run(command, capture_output=True, text=True, check=True)
        print("✅ Compilación exitosa")
    except subprocess.CalledProcessError as e:
        print(f"❌ Error al compilar los archivos .proto (return code: {e.returncode})")
        print(f"Command executed: {' '.join(command)}")
        print(f"Error output: {e.stderr}")
        return False
    
    # Corregir importaciones en archivos *_pb2_grpc.py y *_pb2.py
    print("🔧 Corrigiendo importaciones en archivos gRPC y Protobuf...")
    for proto_file in proto_files:
        base_name = os.path.splitext(proto_file)[0]
        grpc_file = os.path.join(pb2_grpc_output_dir, f"{base_name}_pb2_grpc.py")
        if os.path.exists(grpc_file):
            with open(grpc_file, 'r', encoding='utf-8') as f:
                content = f.read()
            corrected_content = content
            for pf in proto_files:
                dep_base_name = os.path.splitext(pf)[0]
                corrected_content = corrected_content.replace(
                    f"import {dep_base_name}_pb2", f"import services_pb2.{dep_base_name}_pb2"
                )
            with open(grpc_file, 'w', encoding='utf-8') as f:
                f.write(corrected_content)
            print(f"✅ Corregido import en {grpc_file}")
        
        pb2_file = os.path.join(pb2_output_dir, f"{base_name}_pb2.py")
        if os.path.exists(pb2_file):
            with open(pb2_file, 'r', encoding='utf-8') as f:
                content = f.read()
            corrected_content = content
            for pf in proto_files:
                dep_base_name = os.path.splitext(pf)[0]
                corrected_content = corrected_content.replace(
                    f"import {dep_base_name}_pb2", f"import services_pb2.{dep_base_name}_pb2"
                )
            with open(pb2_file, 'w', encoding='utf-8') as f:
                f.write(corrected_content)
            print(f"✅ Corregido import en {pb2_file}")
    
    return True

from database.databaseManager import get_session, init_db
from services.UserService import UserService
from services.AuthService import AuthService
from services.DonationService import DonationService
from services.EventService import EventService
from services_pb2_grpc import authorize_pb2_grpc, user_pb2_grpc, donation_pb2_grpc, event_pb2_grpc
from dotenv import load_dotenv

load_dotenv()
KAFKA_BOOTSTRAP_SERVERS = os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'localhost:9092')
GRPC_PORT = os.getenv('GRPC_PORT', '50051')

def serve():
    server = grpc.server(ThreadPoolExecutor(max_workers=10))
    user_pb2_grpc.add_UserServiceServicer_to_server(UserService(), server)
    authorize_pb2_grpc.add_AuthServiceServicer_to_server(AuthService(), server)
    donation_pb2_grpc.add_DonationServiceServicer_to_server(DonationService(), server)
    event_pb2_grpc.add_EventServiceServicer_to_server(EventService(), server)
    server.add_insecure_port(f'[::]:{GRPC_PORT}')
    server.start()
    print(f"✅ Servidor gRPC iniciado en puerto {GRPC_PORT}")
    kafka = None
    try:
        kafka = KafkaIntegration(bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS)
        org_id = int(os.getenv('ORG_ID', '1'))

        # Donation transfer consumer
        consumer_thread_transfer = Thread(
            target=kafka.consume_donation_transfer,
            args=(org_id, lambda x: logging.info(f"Processed donation transfer: request_id={x['request_id']}"))
        )
        consumer_thread_transfer.daemon = True
        consumer_thread_transfer.start()

        # Donation offer consumer
        def store_donation_offer(offer_data):
            session = get_session()
            try:
                for donation in offer_data['donations']:
                    offer = DonationOffer(
                        offer_id=offer_data['offer_id'],
                        donor_org_id=offer_data['donor_org_id'],
                        category_id=donation['category_id'],
                        description=donation['description'],
                        quantity=donation['quantity'],
                        created_at=datetime.utcnow()
                    )
                    session.add(offer)
                session.commit()
                logging.info(f"Stored donation offer: offer_id={offer_data['offer_id']}")
            except Exception as e:
                session.rollback()
                logging.error(f"Failed to store donation offer: {str(e)}")
            finally:
                session.close()

        consumer_thread_offer = Thread(
            target=kafka.consume_donation_offer,
            args=(store_donation_offer,)
        )
        consumer_thread_offer.daemon = True
        consumer_thread_offer.start()

        # Donation request cancellation consumer
        def process_cancellation(cancellation_data):
            session = get_session()
            try:
                db_request = session.query(DonationRequest).filter_by(
                    request_id=cancellation_data['request_id'],
                    org_id=cancellation_data['org_id']
                ).first()
                if db_request:
                    db_request.is_canceled = True
                    session.commit()
                    logging.info(f"Marked donation request as canceled: request_id={cancellation_data['request_id']}")
                else:
                    logging.warning(f"Donation request not found: request_id={cancellation_data['request_id']}")
            except Exception as e:
                session.rollback()
                logging.error(f"Failed to process cancellation: {str(e)}")
            finally:
                session.close()

        consumer_thread_cancellation = Thread(
            target=kafka.consume_donation_request_cancellation,
            args=(process_cancellation,)
        )
        consumer_thread_cancellation.daemon = True
        consumer_thread_cancellation.start()

        # Solidarity event consumer
        def store_external_event(event_data):
            session = get_session()
            try:
                event = ExternalEvent(
                    event_id=event_data['event_id'],
                    org_id=event_data['org_id'],
                    name=event_data['name'],
                    description=event_data['description'],
                    date_time=datetime.strptime(event_data['date_time'], '%Y-%m-%d %H:%M:%S'),
                    is_canceled=False,
                    created_at=datetime.utcnow()
                )
                session.add(event)
                session.commit()
                logging.info(f"Stored external event: event_id={event_data['event_id']}, name={event_data['name']}")
            except Exception as e:
                session.rollback()
                logging.error(f"Failed to store external event: {str(e)}")
            finally:
                session.close()

        consumer_thread_event = Thread(
            target=kafka.consume_solidarity_event,
            args=(org_id, store_external_event)
        )
        consumer_thread_event.daemon = True
        consumer_thread_event.start()

        # Event cancellation consumer
        def process_event_cancellation(cancellation_data):
            session = get_session()
            try:
                db_event = session.query(ExternalEvent).filter_by(
                    event_id=cancellation_data['event_id'],
                    org_id=cancellation_data['org_id']
                ).first()
                if db_event:
                    db_event.is_canceled = True
                    session.commit()
                    logging.info(f"Marked external event as canceled: event_id={cancellation_data['event_id']}")
                else:
                    logging.warning(f"External event not found: event_id={cancellation_data['event_id']}")
            except Exception as e:
                session.rollback()
                logging.error(f"Failed to process event cancellation: {str(e)}")
                raise
            finally:
                session.close()

        consumer_thread_event_cancellation = Thread(
            target=kafka.consume_event_cancellation,
            args=(process_event_cancellation,)
        )
        consumer_thread_event_cancellation.daemon = True
        consumer_thread_event_cancellation.start()

        # Event adhesion consumer
        def process_event_adhesion(adhesion_data):
            session = get_session()
            try:
                adhesion = EventAdhesion(
                    event_id=adhesion_data['event_id'],
                    org_id=org_id,
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
            except Exception as e:
                session.rollback()
                logging.error(f"Failed to store event adhesion: {str(e)}")
                raise
            finally:
                session.close()

        consumer_thread_adhesion = Thread(
            target=kafka.consume_event_adhesion,
            args=(org_id, process_event_adhesion)
        )
        consumer_thread_adhesion.daemon = True
        consumer_thread_adhesion.start()
        logging.info(f"Started event adhesion consumer: topic=adhesion_evento_{org_id}")

        while True:
            time.sleep(86400)
    except KeyboardInterrupt:
        if kafka:
            kafka.close()
        server.stop(0)
        print("⏹ Servidor detenido manualmente")
    except Exception as e:
        logging.error(f"Error in server: {str(e)}")
        if kafka:
            kafka.close()
        server.stop(0)
        print("⏹ Servidor detenido debido a un error")
        
if __name__ == "__main__":
    if not compile_proto():
        sys.exit(1)
    
    init_db()
    serve()