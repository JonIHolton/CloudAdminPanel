/* eslint-disable @typescript-eslint/no-explicit-any */
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import {
  Box,
  Divider,
  Flex,
  FormControl,
  FormLabel,
  Heading,
  Input,
  useToast,
  VStack,
} from "@chakra-ui/react";

import Loader from "~components/loader/Loader";

import { api } from "~features/api";

const ViewLog = () => {
  const { requestid } = useParams();
  const toast = useToast();
  const [logDetails, setLogDetails] = useState<any>(null);

  // console log the log id
  console.log(requestid);

  useEffect(() => {
    const fetchLog = async () => {
      try {
        // cicd wrok ? heoks
        console.log(requestid);
        const logResponse = await api.get("/api/v1/logs/" + requestid);
        setLogDetails(logResponse.data);
      } catch (error: any) {
        toast({
          title: "Error fetching data",
          description: error.message || "Could not fetch data.",
          status: "error",
          duration: 9000,
          isClosable: true,
        });
      }
    };
    // pls work
    if (requestid != undefined && requestid != null) {
      fetchLog();
    }
  }, [requestid, toast]);

  // Changed form logDetails
  if (!logDetails) {
    return <Loader></Loader>;
  }

  return (
    <Flex width="full" align="center" justifyContent="center" mt={10}>
      <Box
        p={8}
        maxWidth="700px"
        w="full"
        borderWidth={1}
        borderRadius={8}
        boxShadow="lg"
      >
        <VStack spacing={4} align="flex-start">
          <Heading as="h1" size="xl">
            Log
          </Heading>
          <Divider />
          <FormControl isReadOnly>
            <FormLabel htmlFor="log-id">Log ID</FormLabel>
            <Input
              id="log-id"
              type="text"
              value={logDetails.logId}
              isReadOnly
              isDisabled
            />
          </FormControl>
          <FormControl isReadOnly>
            <FormLabel htmlFor="user-id">User ID</FormLabel>
            <Input
              id="user-id"
              type="text"
              value={logDetails.userId}
              isReadOnly
              isDisabled
            />
          </FormControl>
          <FormControl isReadOnly>
            <FormLabel htmlFor="activity">Activity</FormLabel>
            <Input
              id="activity"
              type="text"
              value={logDetails.activity}
              isReadOnly
              isDisabled
            />
          </FormControl>
          <FormControl isReadOnly>
            <FormLabel htmlFor="result">Result</FormLabel>
            <Input
              id="result"
              type="text"
              value={logDetails.result}
              isReadOnly
              isDisabled
            />
          </FormControl>
        </VStack>
      </Box>
    </Flex>
  );
};

export default ViewLog;
