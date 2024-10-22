// export const roles policy makes api request to /policy endpoint which returns a Map<String,object>
import { api } from "~features/api";

export const policyDocument = async () => {
  try {
    const response = await api.get("/api/v1/policy");
    if (response.status === 200) {
      return response.data;
    } else {
      throw new Error("Failed to fetch policy document");
    }
  } catch (error) {
    console.error("Error fetching policy document:", error);
    throw error;
  }
};
