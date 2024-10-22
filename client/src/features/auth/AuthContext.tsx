/* eslint-disable @typescript-eslint/no-unused-vars */
import React, { createContext, useContext, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useToast } from "@chakra-ui/react";

import useAuthStore from "~shared/store/AuthStore";

import { api, handleResponse } from "~api";
import { AuthContextType } from "~types/auth";

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const auth = useProvideAuth();

  return <AuthContext.Provider value={auth}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};

const useProvideAuth = (): AuthContextType => {
  const { isAuthenticated, user, login, logout } = useAuthStore();
  const toast = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    const handleInvalidToken = () => {
      // Directly call the signOut function defined below
      signOut();
    };

    window.addEventListener("invalid-token-detected", handleInvalidToken);

    // Cleanup the event listener on component unmount
    return () => {
      window.removeEventListener("invalid-token-detected", handleInvalidToken);
    };
  }, [isAuthenticated]); // Depend on isAuthenticated to ensure it's current

  const getUserData = async (): Promise<void> => {
    try {
      const response = await api.get("/api/v1/user/me");
      const data = await handleResponse(response);
      const userData = {
        user_id: data.userId,
        first_name: data.firstName,
        last_name: data.lastName,
        email: data.email,
        role: data.role,
        profile_pic: data.imageUrl,
      };
      if (userData) {
        login(userData);
        // navigate to current user id at /users/view/userid
        navigate("/users/view/" + userData.user_id);
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Something went wrong",
        status: "error",
        duration: 5000,
        isClosable: true,
      });
      navigate("/login");
    }
  };

  const signOut = async (): Promise<void> => {
    try {
      const response = await api.post("/api/v1/auth/logout");
      console.log(response);
      // Logout should proceed regardless of the response status
    } catch (error) {
      console.log(error);
    }
    if (isAuthenticated) {
      logout();
      localStorage.clear();
      navigate("/");
      toast({
        title: "Logged Out",
        description: "You have been logged out.",
        status: "info",
        duration: 5000,
        isClosable: true,
      });
    }
  };

  return {
    isAuthenticated,
    user,
    getUserData,
    signOut,
  };
};

export default AuthProvider;
