import sys
import os
import grpc
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
import services_pb2.donation_pb2 as donation_pb2
import services_pb2_grpc.donation_pb2_grpc as donation_pb2_grpc
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from database import databaseManager
import logging

# Suppress all SQLAlchemy logs
logging.getLogger('sqlalchemy').setLevel(logging.CRITICAL)
logging.getLogger('sqlalchemy.engine').setLevel(logging.CRITICAL)
logging.getLogger('sqlalchemy.dialects').setLevel(logging.CRITICAL)
logging.getLogger('sqlalchemy.orm').setLevel(logging.CRITICAL)

# Add servers/ directory to sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

Session = databaseManager.get_session()

def test_donation_service(stub):
    print("\n=== Testing DonationService ===")
    
    # Test CreateDonation
    donation = donation_pb2.Donation(
        categoria=1,  # Use valid category (e.g., ROPA)
        description="Test Donation",
        cantidad=100,
        eliminado=False,
        username="admin"  # Add valid username for audit
    )
    try:
        response = stub.CreateDonation(donation)
        print(f"CreateDonation: success={response.success}, message={response.message}")
    except grpc.RpcError as e:
        print(f"CreateDonation failed: {str(e)}")
        return
    
    # Test ListDonations
    try:
        donation_list = stub.ListDonations(donation_pb2.Empty())
        print(f"ListDonations: found {len(donation_list.donation)} donations")
        donation_id = None
        if donation_list.donation:
            for d in donation_list.donation:
                print(f"  - Donation: id={d.id}, description={d.description}, cantidad={d.cantidad}, eliminado={d.eliminado}")
            donation_id = max(d.id for d in donation_list.donation)  # Get the latest donation ID
        else:
            print("  - No donations found, skipping UpdateDonation and DeleteDonation")
            return
    except grpc.RpcError as e:
        print(f"ListDonations failed: {str(e)}")
        return
    
    # Test UpdateDonation
    if donation_id:
        donation_to_update = donation_pb2.Donation(
            id=donation_id,
            categoria=1,
            description="Updated Donation",
            cantidad=200,
            eliminado=False,
            username="admin"
        )
        try:
            response = stub.UpdateDonation(donation_to_update)
            print(f"UpdateDonation: success={response.success}, message={response.message}")
        except grpc.RpcError as e:
            print(f"UpdateDonation failed: {str(e)}")
    
    # Test DeleteDonation
    if donation_id:
        donation_to_delete = donation_pb2.Donation(id=donation_id)
        try:
            response = stub.DeleteDonation(donation_to_delete)
            print(f"DeleteDonation: success={response.success}, message={response.message}")
        except grpc.RpcError as e:
            print(f"DeleteDonation failed: {str(e)}")

def test_donation_transfer(stub_donation):
    print("\n=== Testing TransferDonation ===")
    transfer_request = donation_pb2.DonationTransferRequest(
        request_id=1,
        donor_org_id=1,
        recipient_org_id=2,
        donations=[
            donation_pb2.Donation(
                categoria=1,
                description="Test Donation",
                cantidad=10
            )
        ]
    )
    try:
        response = stub_donation.TransferDonation(transfer_request)
        print(f"TransferDonation: success={response.success}, message={response.message}")
    except grpc.RpcError as e:
        print(f"TransferDonation failed: {str(e)}")

def test_donation_request_cancellation(stub_donation):
    print("\n=== Testing CancelDonationRequest ===")
    cancellation_request = donation_pb2.DonationRequestCancellationRequest(
        org_id=1,
        request_id=1
    )
    try:
        response = stub_donation.CancelDonationRequest(cancellation_request)
        print(f"CancelDonationRequest: success={response.success}, message={response.message}")
    except grpc.RpcError as e:
        print(f"CancelDonationRequest failed: {str(e)}")

def main():
    print("Starting DonationService Tests")
    print("==========================")
    
    try:
        channel = grpc.insecure_channel('localhost:50051')
        stub_donation = donation_pb2_grpc.DonationServiceStub(channel)
    except Exception as e:
        print(f"Failed to connect to server: {str(e)}")
        return
    
    test_donation_service(stub_donation)
    test_donation_transfer(stub_donation)
    test_donation_request_cancellation(stub_donation)
    
    channel.close()
    print("\n==========================")
    print("DonationService Tests Completed")

if __name__ == '__main__':
    main()