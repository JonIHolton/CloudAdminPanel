import { lazy, Suspense, useEffect } from "react";
import { Route, Routes } from "react-router-dom";
import { Box, Flex } from "@chakra-ui/react";
import Cookies from "js-cookie"; // Ensure you have 'js-cookie' installed or use another method to access cookies

import useAuthStore from "~shared/store/AuthStore";

import { useAuth } from "~features/auth";

const Loader = lazy(() => import("~components/loader/Loader"));
const NotFound = lazy(() => import("~pages/notfound/NotFound"));
const Footer = lazy(() => import("~components/footer/Footer"));

const PublicRoute = lazy(() => import("~shared/routes/PublicRoute"));
const PrivateRoute = lazy(() => import("~shared/routes/PrivateRoute"));

// Public Page
const LoginPage = lazy(() => import("~pages/auth/Login"));
const CallbackPage = lazy(() => import("~pages/auth/Callback"));
// Private Page
const ViewAllPage = lazy(() => import("~pages/users/ViewAll"));
const ViewUserPage = lazy(() => import("~pages/users/ViewUser"));

const EditUserPage = lazy(() => import("~pages/users/EditUser"));

const EditUserPointsPage = lazy(() => import("~pages/users/EditUserPoints"));

const CreateUserPage = lazy(() => import("~pages/users/CreateUser"));

const ViewAllLogs = lazy(() => import("~pages/logging/ViewAllLogs"));
const ViewLog = lazy(() => import("~pages/logging/ViewLog"));

const App = () => {
  const { isAuthenticated } = useAuth();
  useEffect(() => {
    const authTokenCookie = Cookies.get("auth_token");

    const handleStorageChange = (event: StorageEvent) => {
      if (
        (event.key === "auth-storage" && !event.newValue) ||
        !authTokenCookie
      ) {
        useAuthStore.getState().logout();
      }
    };

    window.addEventListener("storage", handleStorageChange);

    return () => window.removeEventListener("storage", handleStorageChange);
  }, []);
  return (
    <Flex direction="column" minH="100vh" bg={"white"}>
      <Suspense fallback={<Loader />}>
        <Box flex="1" bg="white">
          <Suspense fallback={<Loader />}>
            <Routes>
              <Route
                element={
                  <PublicRoute
                    strict={true}
                    isAuthenticated={isAuthenticated}
                  />
                }
              >
                <Route path="/" element={<LoginPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/auth/callback" element={<CallbackPage />} />
              </Route>

              <Route
                element={
                  <PrivateRoute
                    requiredResource="UserStorage"
                    requiredAction="read"
                  />
                }
              >
                <Route path="/users/viewall" element={<ViewAllPage />} />
              </Route>
              <Route
                element={
                  <PrivateRoute
                    requiredResource="UserStorage"
                    requiredAction="read"
                  />
                }
              >
                <Route path="/users/view/:userid" element={<ViewUserPage />} />
              </Route>
              <Route
                element={
                  <PrivateRoute
                    requiredResource="UserStorage"
                    requiredAction="update"
                  />
                }
              >
                <Route
                  path="/users/view/:userid/edit"
                  element={<EditUserPage />}
                />
              </Route>
              <Route
                element={
                  <PrivateRoute
                    requiredResource="PointsLedger"
                    requiredAction="update"
                  />
                }
              >
                <Route
                  path="/users/view/:userid/points/:pointsid/edit"
                  element={<EditUserPointsPage />}
                />
              </Route>
              <Route
                element={
                  <PrivateRoute
                    requiredResource="UserStorage"
                    requiredAction="create"
                  />
                }
              >
                <Route path="/users/create" element={<CreateUserPage />} />
                <Route path="/logs/viewall" element={<ViewAllLogs />} />
                <Route path="/logs/view/:requestid" element={<ViewLog />} />
              </Route>
              <Route
                element={
                  <PrivateRoute requiredResource="Logs" requiredAction="read" />
                }
              >
                <Route path="/logs/viewall" element={<ViewAllLogs />} />
                <Route path="/logs/view/:requestid" element={<ViewLog />} />
              </Route>
              <Route path="*" element={<NotFound />} />
            </Routes>
          </Suspense>
        </Box>
        <Footer />
      </Suspense>
    </Flex>
  );
};

export default App;
