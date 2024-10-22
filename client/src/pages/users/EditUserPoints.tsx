/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable id-length */
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  Box,
  Button,
  Flex,
  FormControl,
  FormLabel,
  Input,
  useToast,
} from "@chakra-ui/react";

import { api } from "~features/api";

const EditPointsPage = () => {
  const [pointsDetails, setPointsDetails] = useState({
    owner: "",
    existingPoints: "",
    newPoints: "",
  });
  const { userid, pointsid } = useParams();
  const toast = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    // Fetch points details when the component mounts
    const fetchData = async () => {
      try {
        const pointsResponse = await api.get(
          `/api/v1/users/${userid}/points/${pointsid}`,
        );
        console.log(pointsResponse.data);
        setPointsDetails({
          owner: pointsResponse.data.userId,
          existingPoints: pointsResponse.data.points,
          newPoints: "",
        });
      } catch (error) {
        toast({
          title: "Error fetching data.",
          description: "Could not fetch points details from the server.",
          status: "error",
          duration: 9000,
          isClosable: true,
        });
        navigate(`/users/view/${userid}`);
      }
    };

    fetchData();
  }, [userid, pointsid, toast]);

  const handleChange = (e: { target: { name: any; value: any } }) => {
    const { name, value } = e.target;
    setPointsDetails((prevDetails) => ({
      ...prevDetails,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: { preventDefault: () => void }) => {
    e.preventDefault();
    try {
      await api.post(`/api/v1/users/${userid}/points/${pointsid}/edit`, {
        points: parseInt(pointsDetails.newPoints, 10),
      });
      toast({
        title: "Points updated.",
        description: "Points updated successfully.",
        status: "success",
        duration: 9000,
        isClosable: true,
      });
      navigate(`/users/view/${userid}`);
    } catch (error: any) {
      toast({
        title: "Error updating points.",
        description: "There was an error updating the points.",
        status: "error",
        duration: 9000,
        isClosable: true,
      });
    }
  };

  return (
    <Flex width="full" align="center" justifyContent="center">
      <Box
        p={8}
        maxWidth="500px"
        borderWidth={1}
        borderRadius={8}
        boxShadow="lg"
      >
        <Box textAlign="center" my={4}>
          <form onSubmit={handleSubmit}>
            <FormControl isReadOnly>
              <FormLabel>Owner</FormLabel>
              <Input
                type="text"
                value={pointsDetails.owner}
                isReadOnly
                isDisabled
              />
            </FormControl>
            <FormControl isReadOnly mt={6}>
              <FormLabel>Existing Point Balance</FormLabel>
              <Input
                type="number"
                value={pointsDetails.existingPoints}
                isReadOnly
                isDisabled
              />
            </FormControl>
            <FormControl isRequired mt={6}>
              <FormLabel>New Point Balance</FormLabel>
              <Input
                type="number"
                placeholder="Enter new points balance"
                name="newPoints"
                value={pointsDetails.newPoints}
                onChange={handleChange}
              />
            </FormControl>
            <Button colorScheme="blue" width="full" mt={4} type="submit">
              Save Changes
            </Button>
          </form>
        </Box>
      </Box>
    </Flex>
  );
};

export default EditPointsPage;
