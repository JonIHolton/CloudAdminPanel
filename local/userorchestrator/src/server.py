import grpc
from concurrent import futures
import logging

# Assuming these imports are generated from your protobuf files
import user_pb2
import user_pb2_grpc
import points_pb2
import points_pb2_grpc
import user_orchestrator_pb2_grpc

USER_SERVICE_ADDRESS = '127.0.0.1:50051'
POINTS_SERVICE_ADDRESS = '127.0.0.1:50052' 
# USER_SERVICE_ADDRESS = 'users:50051'
# POINTS_SERVICE_ADDRESS = 'points:50052' 

class UserOrchestratorServicer(user_orchestrator_pb2_grpc.UserManagementServiceServicer):
    def __init__(self):
        self.user_stub = user_pb2_grpc.UserManagementServiceStub(grpc.insecure_channel(USER_SERVICE_ADDRESS))
        self.points_stub = points_pb2_grpc.PointsServiceStub(grpc.insecure_channel(POINTS_SERVICE_ADDRESS))

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

    # User Service Forwarding Methods, each method forwards the request to the User Service
    @safe_call
    def Ping(self, request, context):
        return self.user_stub.PingResponse(message="Service is alive!")

    @safe_call
    def GetUserByUserId(self, request, context):
        return self.user_stub.GetUserByUserId(request)

    @safe_call
    def GetUserByEmail(self, request, context):
        return self.user_stub.GetUserByEmail(request)

    @safe_call
    def CreateNewUser(self, request, context):
        logging.info(request)
        return self.user_stub.CreateNewUser(request)

    @safe_call
    def UpdateUser(self, request, context):
        return self.user_stub.UpdateUser(request)

    @safe_call
    def DeleteUser(self, request, context):
        return self.user_stub.DeleteUser(request)

    @safe_call
    def GetAllUsers(self, request, context):
        users = self.user_stub.GetAllUsers(request)
        # get length of this users list
        users_length = users.totalUsers
        logging.info(f"Total users: {users_length}")
        return users

    # Points Service Forwarding Methods
    @safe_call
    def GetUserPointsAccountsByUserId(self, request, context):
        return self.points_stub.GetUserPointsAccountsByUserId(request)

    @safe_call
    def GetUserPointsAccountDetails(self, request, context):
        return self.points_stub.GetUserPointsAccountDetails(request)

    @safe_call
    def UpdateUserPoints(self, request, context):
        return self.points_stub.UpdateUserPoints(request)

    @safe_call
    def DeletePointsAccount(self, request, context):
        return self.points_stub.DeletePointsAccount(request)

    @safe_call
    def GetPointsByPointsId(self, request, context):
        return self.points_stub.GetPointsByPointsId(request)

    @safe_call
    def UpdatePoints(self, request, context):
        return self.points_stub.UpdateUserPoints(request)

    @safe_call
    def CreateNewPointsAccount(self, request, context):

        return self.points_stub.CreateNewPointsAccount(request)

    @safe_call
    def GetAllBanks(self, request, context):

        return self.points_stub.GetAllBanks(request)

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    user_orchestrator_pb2_grpc.add_UserManagementServiceServicer_to_server(UserOrchestratorServicer(), server)
    server.add_insecure_port('[::]:50053')
    server.start()
    logging.info("User Orchestrator Service started on port 50053.")
    server.wait_for_termination()


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    serve()
