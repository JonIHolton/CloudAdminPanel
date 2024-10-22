import React, { useEffect, useState } from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useToast } from "@chakra-ui/react";

import Loader from "~components/loader/Loader";
import SidebarWithHeader from "~components/sidebar/Sidebar";

import { useAuth } from "~features/auth";
import { canPerformAction } from "~features/auth/CheckPermissions";

interface PrivateRouteProps {
  requiredResource: string;
  requiredAction: string;
}

const PrivateRoute: React.FC<PrivateRouteProps> = ({
  requiredResource,
  requiredAction,
}) => {
  const { isAuthenticated, user } = useAuth();
  const [accessGranted, setAccessGranted] = useState(false);
  const [checkingAccess, setCheckingAccess] = useState(true);
  const toast = useToast();

  useEffect(() => {
    const checkAccess = async () => {
      if (!isAuthenticated || !user) {
        setAccessGranted(false);
        setCheckingAccess(false);
        return;
      }
      const hasAccess = await canPerformAction(
        user.role,
        requiredResource,
        requiredAction,
      );
      setAccessGranted(hasAccess);
      setCheckingAccess(false);

      if (!hasAccess) {
        toast({
          title: "Access Denied",
          description: "You are not allowed to access this resource",
          status: "error",
          duration: 5000,
          isClosable: true,
        });
      }
    };

    checkAccess();
  }, [isAuthenticated, user, requiredResource, requiredAction, toast]);

  if (checkingAccess) {
    return <Loader></Loader>;
  }

  if (!isAuthenticated) {
    return <Navigate to={"/login"} />;
  }

  if (!accessGranted) {
    return <Navigate to={"/users/viewall"} />;
  }

  return (
    <SidebarWithHeader>
      <Outlet />
    </SidebarWithHeader>
  );
};

export default PrivateRoute;
