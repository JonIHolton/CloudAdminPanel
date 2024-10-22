import { Helmet } from "react-helmet-async";
import { FcGoogle } from "react-icons/fc";
import {
  Box,
  Button,
  Center,
  Flex,
  Heading,
  Stack,
  Text,
} from "@chakra-ui/react";

import { GOOGLE_AUTH_URL } from "~shared/constants";

// import { useAuth } from "~features/auth";

const LoginPage = () => {
  // const { googleAuth } = useAuth();

  return (
    <Flex minH="100vh" align="center" justify="center" bg={"branding.100"}>
      <Helmet>
        <title>Sign in</title>
        <meta name="description" content="Sign in" />
      </Helmet>
      <Stack
        spacing={8}
        mx="auto"
        maxW="lg"
        w={{ base: "90%", sm: "full" }}
        p={1}
      >
        <Stack align="center" mt="5" mb="10">
          <Heading color={"white"}>Ascenda Loyalty</Heading>
        </Stack>
        <Box rounded="lg" bg={"white"} boxShadow="lg" p={6}>
          <Stack align="center" mt="5" mb="10">
            <Heading fontSize="3xl" color={"black"}>
              Sign in
            </Heading>
          </Stack>
          <Stack spacing={6} alignItems="center" mb="6">
            <Button
              w={{ base: "full", sm: "80%" }}
              size="lg"
              as="a"
              variant="outline"
              colorScheme="google"
              leftIcon={<FcGoogle />}
              href={GOOGLE_AUTH_URL}
            >
              <Center>
                <Text>Sign in with Google</Text>
              </Center>
            </Button>
          </Stack>
        </Box>
      </Stack>
    </Flex>
  );
};

export default LoginPage;
