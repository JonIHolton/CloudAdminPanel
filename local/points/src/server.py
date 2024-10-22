
import threading
from time import sleep
import traceback
import grpc
from concurrent import futures
import logging
from sqlalchemy.orm import Session
from db.database import engine, Base, SessionLocal

import points_pb2
import points_pb2_grpc
from google.protobuf import empty_pb2
from google.protobuf.timestamp_pb2 import Timestamp
from sqlalchemy.exc import SQLAlchemyError

from db.database import SessionLocal
from model.model import AuthProvider, User, Points, Bank

from faker import Faker
import random
import uuid
fake = Faker()

def generate_mock_users(session: Session, n, batch_size=1200):
    total_users = 0
    while total_users < n:
        users = []
        current_batch_size = min(batch_size, n - total_users)  
        logging.info(f"Generating {current_batch_size} mock users...")
        while len(users) < current_batch_size:
            first_name = fake.first_name()
            last_name = fake.last_name()
            name = f"{first_name} {last_name}"
            email = f"{fake.email()}{uuid.uuid4().hex[:6].upper()}"

            exists = session.query(User.email).filter_by(email=email).first() is not None
            if exists:
                continue  

            user = User(
                name=name,
                first_name=first_name,
                last_name=last_name,
                email=email,
                role=random.choice(['User', 'Owner', 'Product Manager', 'Engineer', 'Manager']),
                provider=random.choice(list(AuthProvider)),
            )

            users.append(user)
        logging.info(f"Generated {current_batch_size} mock  users.")
        session.bulk_save_objects(users)
        try:
            session.commit()
            logging.info(f"Committed {current_batch_size} mock users.")
            total_users += len(users) 
        except SQLAlchemyError as e:
            session.rollback()  
            logging.error(f"Failed to commit {current_batch_size} mock users due to: {e}")

def generate_mock_banks():
    banks = [Bank(bank_name='DBS'), Bank(bank_name='UOB'),Bank(bank_name='CIMB'), Bank(bank_name='Trust')]
    return banks


def generate_mock_points(session, users, banks, n, batch_size=100):
    points = []
    bank_ids = [bank.bank_id for bank in banks] 

    for _ in range(0, n, batch_size):
        batch_points = []
        for _ in range(batch_size):
            user = random.choice(users)
            bank_id = random.choice(bank_ids)

            point = Points(
                user_id=user.id,
                points_balance=random.randint(1, 10000000),
                bank_id=bank_id,
            )
            batch_points.append(point)

        points.extend(batch_points)

    # Bulk insert points
    session.bulk_save_objects(points)
    session.commit()

# Base.metadata.create_all(engine, checkfirst=True, tables=[User.__table__, Points.__table__, Bank.__table__])
session = SessionLocal()

class PointsServicer(points_pb2_grpc.PointsServiceServicer):

    def GetUserPointsAccountsByUserId(self, request, context):
        session: Session = SessionLocal()
        try:
            user_id = request.id
            points_accounts = session.query(Points).filter(Points.user_id == user_id).all()
            logging.info(f"Points accounts: {points_accounts}")
            
            if not points_accounts:
                logging.info(f"No points accounts found for user ID {user_id}")
                context.abort(grpc.StatusCode.NOT_FOUND, f'No points accounts found for user ID {user_id}')
                return
            
            points_accounts_response = [
                self._convert_to_grpc_points_account(points_account) for points_account in points_accounts
            ]
            logging.info(f"Conversion passed: {points_accounts_response}")
            return points_pb2.PointsResponse(pointsAccount=points_accounts_response)
        except SQLAlchemyError as e:
            logging.error(f"Database error while getting points accounts for user ID {request.id}: {str(e)}")
            context.abort(grpc.StatusCode.INTERNAL, 'Internal server error due to database issue')
        finally:
            session.close()

    def _convert_to_grpc_bank(self, bank_id):
        session: Session = SessionLocal()

        bank = session.query(Bank).filter(Bank.bank_id == bank_id.bank_id).first()
        return bank.bank_name if bank else "Unknown Bank"

    def _convert_to_grpc_points_account(self, points_account):
        created_at_pb = Timestamp()
        created_at_pb.FromDatetime(points_account.created_at)
        updated_at_pb = Timestamp()
        updated_at_pb.FromDatetime(points_account.updated_at)

        return points_pb2.PointsAccount(
            userid=str(points_account.user_id),
            pointsid=str(points_account.points_id),
            points=points_account.points_balance,
            bank= str(self._convert_to_grpc_bank(points_account.bank)),
            createdAt=created_at_pb,
            updatedAt=updated_at_pb
        )


    def DeleteUser(self, request, context):
        session: Session = SessionLocal()
        try:
            user_id = request.id
            with session.begin():
                session.query(Points).filter(Points.user_id == user_id).delete()
                user = session.query(User).filter(User.id == user_id).first()
                if not user:
                    context.abort(grpc.StatusCode.NOT_FOUND, f'User with ID {user_id} not found.')
                session.delete(user)
            return empty_pb2.Empty()
        except Exception as e:
            logging.error(f"Failed to delete user with ID {user_id}: {str(e)}")
            context.abort(grpc.StatusCode.INTERNAL, 'Internal server error')
        finally:
            session.close()



    def GetUserPointsAccountDetails(self, request, context):
        session: Session = SessionLocal()
        try:
            points_id = request.pointsid
            points_account = session.query(Points).filter(Points.points_id == points_id).first()
            if not points_account:
                context.abort(grpc.StatusCode.NOT_FOUND, f'Points account with ID {points_id} not found.')
            return points_pb2.PointsResponse(pointsAccount=[self._convert_to_grpc_points_account(points_account)])
        except Exception as e:
            logging.error(f"Failed to get details for points account ID {points_id}: {str(e)}")
            context.abort(grpc.StatusCode.INTERNAL, 'Internal server error')
        finally:
            session.close()


    def UpdateUserPoints(self, request, context):
        session: Session = SessionLocal()
        try:
            logging.info(f"Request: {request}")
            points_account_data = request
            if not isinstance(points_account_data.points, int) or points_account_data.points < 0:
                context.abort(grpc.StatusCode.INVALID_ARGUMENT, f'Points must be an integer greater than 0.')
            with session.begin():
                points_account = session.query(Points).filter(Points.points_id == points_account_data.pointsid).first()
                if not points_account:
                    context.abort(grpc.StatusCode.NOT_FOUND, f'Points account with ID {points_account_data.pointsid} not found.')
                points_account.points_balance = points_account_data.points
                session.commit()
            return points_pb2.PointsResponse(pointsAccount=[self._convert_to_grpc_points_account(points_account)])
        except Exception as e:
            logging.error(f"Failed to update points account ID {request.pointsAccount.pointsid}: {str(e)}")
            context.abort(grpc.StatusCode.INTERNAL, 'Internal server error')
        finally:
            session.close()


    def DeletePointsAccount(self, request, context):
        session: Session = SessionLocal()
        points_id = request.pointsid  
        try:
            with session.begin():
                points_account = session.query(Points).filter(Points.points_id == points_id).first()
                if points_account:
                    session.delete(points_account)
            return empty_pb2.Empty()
        except Exception as e:
            logging.error(f"Failed to delete points account with ID {points_id}: {str(e)}")
            context.abort(grpc.StatusCode.INTERNAL, 'Internal server error')
        finally:
            session.close()


    def Ping(self, request, context):
        return points_pb2.PingResponse(message="Pong")

    def CreateNewPointsAccount(self, request, context):
        session: Session = SessionLocal()
        try:
            new_points_account = Points(
                user_id=request.userid,
                points_id=request.pointsid,
                bank=request.bank, 
                # todo: need to change this to bank ID rather than bank name perhaps  
                points_balance=request.points,
                created_at=request.createdAt.ToDatetime(),
                updated_at=request.updatedAt.ToDatetime()
            )
            session.add(new_points_account)
            session.commit()
            return points_pb2.PointsResponse(pointsAccount=[self._convert_to_grpc_points_account(new_points_account)])
        except Exception as e:
            logging.error(f"Failed to create new points account: {str(e)}")
            context.abort(grpc.StatusCode.INTERNAL, 'Internal server error')
        finally:
            session.close()

    def GetAllBanks(self, request, context):
        session: Session = SessionLocal()
        try:
            banks = session.query(Bank).all()
            bank_names = [bank.bank_name for bank in banks]
            return points_pb2.BanksResponse(banks=bank_names)
        except Exception as e:
            logging.error(f"Failed to retrieve all banks: {str(e)}")
            context.abort(grpc.StatusCode.INTERNAL, 'Internal server error')
        finally:
            session.close()

    def GetPointsByPointsId(self, request, context):
        session: Session = SessionLocal()
        try:
            points_id = request.pointsid
            points_account = session.query(Points).filter(Points.points_id == points_id).first()
            if not points_account:
                context.abort(grpc.StatusCode.NOT_FOUND, f'Points account with ID {points_id} not found.')
            return points_pb2.PointsResponse(pointsAccount=[self._convert_to_grpc_points_account(points_account)])
        except Exception as e:
            logging.error(f"Failed to get points by points ID {points_id}: {str(e)}")
            context.abort(grpc.StatusCode.INTERNAL, 'Internal server error')
        finally:
            session.close()

    def UpdatePoints(self, request, context):
        self.UpdateUserPoints(request, context)

    def insert_mock_data(self, n_users=1000000, n_points=100000):
        # Create a new session
        session = SessionLocal()
        try:
            banks = generate_mock_banks()
            # Use the session to save objects
            session.bulk_save_objects(banks)
            session.commit()

            # Generate and insert mock users in bulks
            logging.info(f"Generating {n_users} mock users...")
            generate_mock_users(session=session, n=n_users, batch_size=2000)
            # No need to commit here since it's done in generate_mock_users
            logging.info(f"Generated {n_users} mock users.")
            users = session.query(User).all()  # Re-fetch users to ensure IDs are loaded
            logging.info(f"Generating {n_points} mock points...")
            points = generate_mock_points(session, users, banks, n_points)
            session.bulk_save_objects(points)
            session.commit()
        except Exception as e:
            # Properly handle the exception
            print(f"Error inserting mock data: {e}")
            session.rollback()  # Rollback in case of error
        finally:
            # Properly close the session
            session.close()
    def insert_mock_data2(self, n_points=100000):
        session = SessionLocal()
        try:
            # Fetch existing banks or generate if none exist
            banks = session.query(Bank).all()
            if not banks:
                banks = generate_mock_banks()
                session.bulk_save_objects(banks)
                session.commit()

            logging.info("Fetching existing users...")
            users = session.query(User).all() 
            if not users:
                logging.error("No existing users found. Please ensure the database has users.")
                return

            # Generate and insert mock points in bulks
            logging.info(f"Generating {n_points} mock points for existing users...")
            generate_mock_points(session, users, banks, n_points)
            logging.info("Mock points generation completed.")
        except Exception as e:
            logging.error(f"Error inserting mock data: {e}")
            session.rollback()
        finally:
            session.close()
def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    points_pb2_grpc.add_PointsServiceServicer_to_server(PointsServicer(), server)
    server.add_insecure_port('[::]:50052')
    server.start()
    logging.info("Points Service started on port 50052.")
    server.wait_for_termination()



if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')

    # servicer = PointsServicer()

    # # # Create a thread for insert_mock_data
    # thread = threading.Thread(target=lambda: servicer.insert_mock_data2())
    # thread.start()

    # Start the server concurrently
    serve()

    # thread.join()

    # thread = threading.Thread(target=lambda: servicer.insert_mock_data2())
    # thread.start()
    # thread.join()