import datetime
import random
import uuid
from sqlalchemy import exc
import grpc
from concurrent import futures
import logging
import user_pb2_grpc
import google.protobuf.empty_pb2
import user_pb2
from model.user import AuthProvider, User
from db.database import engine, Base, SessionLocal
from faker import Faker
from sqlalchemy import or_, func
import json
from sqlalchemy.exc import SQLAlchemyError
from google.protobuf.timestamp_pb2 import Timestamp
from sqlalchemy import text
from google.protobuf import field_mask_pb2
from dateutil import parser



# Base.metadata.create_all(engine,checkfirst=True, if_not_exists=True)
session = SessionLocal()
class DateParsingError(Exception):
    pass

class FilterValidationError(Exception):
    pass

class SortingValidationError(Exception):
    pass

class UUIDValidationError(Exception):
    pass

class UserServicer(user_pb2_grpc.UserManagementServiceServicer):
    def __init__(self):
        pass


    def safe_call(func):
        def wrapper(servicer, request, context):
            try:
                logging.info(f"Request received for {func.__name__}")
                response = func(servicer, request, context)
                logging.info(f"Successfully processed {func.__name__}")
                return response
            except grpc.RpcError as e:
                logging.error(f"gRPC error in {func.__name__}: {e.code()} - {e.details()}")
                context.abort(e.code(), e.details())
            except Exception as e:
                logging.error(f"Exception in {func.__name__}: {str(e)}")
                context.abort(grpc.StatusCode.INTERNAL, "Internal server error")
        return wrapper

   
    def GetUserByUserId(self, request, context):
        with session:
            #  the id here can be email or userid because protofile allows either one to be used in the getrequest message
            user = session.query(User).filter(User.id == request.id).first()
            if user:
                return self.convert_to_grpc_user(user)
            else:
                context.abort(grpc.StatusCode.NOT_FOUND, f'User with userId {request.id} not found.')

    def DeleteUser(self, request, context):
        with session:
            user = session.query(User).filter(User.id == request.id).first()
            if user:
                session.delete(user)
                session.commit()
                return google.protobuf.empty_pb2.Empty()
            else:
                context.abort(grpc.StatusCode.NOT_FOUND, f'User with ID {request.id} not found.')


    def UpdateUser(self, request, context):
        with session:
            # Fetch the user by ID from the database
            user = session.query(User).filter(User.id == request.userId).first()
            if not user:
                context.abort(grpc.StatusCode.NOT_FOUND, f'User with ID {request.userId} not found.')
                return

            # Check and update fields if necessary
            changes = False
            if user.first_name != request.user.firstName:
                user.first_name = request.user.firstName
                changes = True
            if user.last_name != request.user.lastName:
                user.last_name = request.user.lastName
                changes = True
            if user.email != request.user.email:
                user.email = request.user.email
                changes = True
            if user.image_url != request.user.imageUrl:
                user.image_url = request.user.imageUrl
                changes = True
            if user.is_enabled != request.user.isEnabled:
                user.is_enabled = request.user.isEnabled
                changes = True
            if user.email_verified != request.user.emailVerified:
                existing_user = session.query(User).filter(User.email ==request.user.emailVerified).first()
                if existing_user:
                    logging.error(f"User with email {request.user.emailVerified} already exists.")
                    context.abort(grpc.StatusCode.ALREADY_EXISTS, 'User already exists.')
                    return
                user.email_verified = request.user.emailVerified
                changes = True
            logging.info("current role : "+user.role)
            logging.info("new role : "+request.user.role)
            if user.role != request.user.role:
                user.role = request.user.role
                changes = True

            # Commit changes to the database if any field was updated
            if changes:
                try:
                    session.commit()
                    logging.info("User updated successfully.")
                except Exception as e:
                    session.rollback()
                    logging.error(f"Failed to update user: {e}")
                    context.abort(grpc.StatusCode.INTERNAL, 'Failed to update user.')
                    return
            else:
                logging.info("No changes were made to the user.")

            # Return the updated user
            updated_user = self.convert_to_grpc_user(user)
            return user_pb2.UpdateUserResponse(user=updated_user)

    # @safe_call
    def GetUserByEmail(self, request, context):
        with session:
            #  the id here can be email or userid because protofile allows either one to be used in the getrequest message
            try:
                logging.info("Request id : "+request.id)
                user = session.query(User).filter(User.email == str(request.id)).first()
                logging.info("User mayhaps founded")

                if user:
                    return self.convert_to_grpc_user(user)
                else:
                    logging.error(f"User with email {request.id} not found.")
                    context.abort(grpc.StatusCode.NOT_FOUND, f'User with email {request.id} not found.')
            except Exception as e:
                logging.error(f"Failed to find user: {e}")
                context.abort(grpc.StatusCode.NOT_FOUND, f'User with email {request.id} not found.')

    def CreateNewUser(self, request, context):
            logging.info(request)
            user_data = {
                "firstName": request.user.firstName,
                "lastName": request.user.lastName,
                "email": request.user.email,
                "imageUrl": request.user.imageUrl,
                "emailVerified": request.user.emailVerified,
                "provider": request.user.provider, 
                "role": request.user.role
            }
            logging.info(user_data)

            # Check if the user already exists
            existing_user = session.query(User).filter(User.email == user_data["email"]).first()
            if existing_user:
                logging.error(f"User with email {user_data['email']} already exists.")
                context.abort(grpc.StatusCode.ALREADY_EXISTS, 'User already exists.')
                return

            try:
                logging.info("Creating new user")
                new_user = self.create_user(user_data) 
                logging.info(new_user)
                return user_pb2.CreateUserResponse(user=new_user)
            except exc.IntegrityError as e:
                logging.error(f"Duplicate entry: {e}")
                context.abort(grpc.StatusCode.ALREADY_EXISTS, 'Duplicate entry, user already exists.')
            except Exception as e:
                logging.error(f"Failed to create user: {e}")
                context.abort(grpc.StatusCode.INTERNAL, 'Failed to create user.')
                return
            
    def GetAllUsers(self, request, context):
        query = session.query(User)
        start = request.start
        size = request.size
        filters = request.filters
        sorting = request.sorting
        logging.info(f"start: {start}, size: {size}, filters: {filters}, sorting: {sorting}")

        try:
            # Validate 'start' and 'size'
            start = int(start)
            size = int(size)
            if start < 0 or size <= 0:
                raise ValueError("Start index must be >= 0 and size > 0.")

            parsed_filters = json.loads(filters) if filters else []
            parsed_sorting = json.loads(sorting) if sorting else []

            # Sorting logic
            if parsed_sorting:
                sort_field = parsed_sorting[0]['id']
                sort_order = 'desc' if parsed_sorting[0]['desc'] else 'asc'
                field_to_column_mapping = {"enrollmentDate": "created_at", "userId": "id"}
                sort_field = field_to_column_mapping.get(sort_field, sort_field)

                if sort_field not in [column.name for column in User.__table__.columns] or sort_order not in ['asc', 'desc']:
                    raise SortingValidationError("Invalid sorting field or order.")

                query = query.order_by(text(f"{sort_field} {sort_order}"))

            # Filtering logic
            for filter in parsed_filters:
                filter_id = filter['id']
                filter_value = filter['value']
                logging.info(f"Filter: {filter_id} - {filter_value}")

                if filter_id == 'enrollmentDate':
                    try:
                        # Handle date range filtering
                        start_date, end_date = None, None
                        if filter_value[0]:
                            start_date = parser.isoparse(filter_value[0])
                            query = query.filter(User.created_at >= start_date)
                        if len(filter_value) > 1 and filter_value[1]:
                            end_date = parser.isoparse(filter_value[1])
                            query = query.filter(User.created_at <= end_date)
                    except Exception as e:
                        raise DateParsingError(f"Error parsing dates: {e}")

                elif filter_id == 'name':
                    # Apply name filtering logic
                    first_name_condition = User.first_name.ilike(f"%{filter_value}%")
                    last_name_condition = User.last_name.ilike(f"%{filter_value}%")
                    full_name_condition = func.concat(User.first_name, ' ', User.last_name).ilike(f"%{filter_value}%")
                    query = query.filter(or_(first_name_condition, last_name_condition, full_name_condition))

                elif filter_id == 'userId':
                    try:
                        # Validate that filter_value is a valid UUID
                        valid_uuid = uuid.UUID(filter_value)
                        query = query.filter(User.id == valid_uuid)
                    except ValueError:
                        # If filter_value is not a valid UUID, raise custom UUIDValidationError
                        logging.exception(f"Invalid UUID format for userId: {filter_value}")
                        query = query.filter(User.id == filter_value)
 
                elif filter_id == 'role':
                    query = query.filter(User.role == filter_value)

                elif filter_id == 'email':
                    query = query.filter(User.email.ilike(f"%{filter_value}%"))
                else:
                    raise ValueError("Invalid filter field value.")


            total_users = query.count()
            users = query.offset(start).limit(size).all()

            logging.info(f"Found {total_users} users")
            grpc_users = [self.convert_to_grpc_user(user) for user in users]

            return user_pb2.GetAllUsersResponse(users=grpc_users, totalUsers=total_users)

        except ValueError as e:
            context.abort(grpc.StatusCode.INVALID_ARGUMENT, f"Invalid parameter: {e}")
        except DateParsingError as e:
            context.abort(grpc.StatusCode.INVALID_ARGUMENT, f"Date parsing error: {e}")
        except SortingValidationError as e:
            context.abort(grpc.StatusCode.INVALID_ARGUMENT, f"Sorting validation error: {e}")
        except SQLAlchemyError as e:
            logging.error(f"Database error: {e}")
            context.abort(grpc.StatusCode.INTERNAL, "Internal server error.")
        except Exception as e:
            logging.error(f"Unknown error: {e}")
            context.abort(grpc.StatusCode.UNKNOWN, "An unknown error occurred.")

    def create_user(self, user_data: dict):
            """Creates a new user in the database."""
            try:
                u_role = None
                if user_data["role"] and user_data["role"].strip() != "":
                    u_role = user_data["role"] 
                     
                new_user = User(
                    name=user_data["firstName"] + " " + user_data["lastName"],
                    first_name=user_data["firstName"],
                    last_name=user_data["lastName"],
                    email=user_data["email"],
                    image_url=user_data["imageUrl"],
                    email_verified=user_data["emailVerified"],
                    provider=user_data["provider"],
                    role=u_role,
                    is_expired=False,
                    is_locked=False,
                    is_credentials_expired=False,
                    is_enabled=True
                )
                
                # Add the new user to the session and commit
                session.add(new_user)
                session.commit()

                # Refresh the session to get the updated user instance
                session.refresh(new_user)

                logging.info("created user")

                # logging.info(new_user.provider)
                # logging.info(new_user.provider.name)
                # logging.info(str(new_user.provider.value))
                # logging.info(AuthProvider(new_user.provider).name)

                # Convert the SQLAlchemy model instance to a gRPC User message
                # Assuming you have a method or logic to do this conversion
                grpc_user = self.convert_to_grpc_user(new_user)
                logging.info("Response sent")
                return grpc_user

            except Exception as e:
                session.rollback()  # Rollback the session on error
                logging.error(f"Failed to create user: {e}")
                raise  # Re-raise the exception to be handled by the caller

    def convert_to_grpc_user(self, user):
            """Converts a SQLAlchemy User model instance to a gRPC User message."""
            # Implement conversion logic based on your User model and user_pb2.User message structure
            logging.info("Converting to grpc user")
            # logging.info(type(user.id))
            # logging.info(type(user.provider.value))
            # logging.info(str(user.provider.value))
                # Create a Timestamp object from the user's created_at datetime
            created_at_pb = Timestamp()
            created_at_pb.FromDatetime(user.created_at)
            # logging.info(created_at_pb)
            # return None
            return user_pb2.User(
                userId=str(user.id),
                firstName=user.first_name,
                lastName=user.last_name,
                email=user.email,
                imageUrl=user.image_url,
                emailVerified=user.email_verified,
                provider=user.provider.value,
                role=user.role,
                isExpired=user.is_expired,
                isLocked=user.is_locked,
                isCredentialsExpired=user.is_credentials_expired,
                isEnabled=user.is_enabled,
                createdAt=created_at_pb 
            )
def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    user_pb2_grpc.add_UserManagementServiceServicer_to_server(UserServicer(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    logging.info("Users Service started on port 50051.")
    server.wait_for_termination()



if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')

    servicer = UserServicer()
    # servicer.insert_mock_data(1000)
    serve()
