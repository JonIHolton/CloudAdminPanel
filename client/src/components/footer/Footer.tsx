/*
 * import { ReactNode } from "react";
 * import { FaInstagram, FaTwitter, FaYoutube } from "react-icons/fa";
 */
import { Box, Flex, Stack, Text } from "@chakra-ui/react";

/*
 * const SocialButton = ({
 *   children,
 *   label,
 *   href,
 * }: {
 *   children: ReactNode;
 *   label: string;
 *   href: string;
 * }) => {
 *   return (
 *     <chakra.button
 *       bg={"blackAlpha.100"}
 *       rounded={"full"}
 *       w={8}
 *       h={8}
 *       cursor={"pointer"}
 *       as={"a"}
 *       href={href}
 *       display={"inline-flex"}
 *       alignItems={"center"}
 *       justifyContent={"center"}
 *       transition={"background 0.3s ease"}
 *       _hover={{
 *         bg: "blackAlpha.200",
 *       }}
 *     >
 *       <VisuallyHidden>{label}</VisuallyHidden>
 *       {children}
 *     </chakra.button>
 *   );
 * };
 */

const Footer = () => {
  return (
    <Box bg={"white"} color={"branding.100"} ml={0}>
      <Flex
        // maxW={"6xl"}
        py={4}
        p={5}
        direction={{ base: "column", md: "row" }}
        justify={{ base: "center", md: "space-between" }}
        align={{ base: "center", md: "center" }}
      >
        <Text ml="20%">© {new Date().getFullYear()} Ascenda. All rights reserved</Text>
        <Stack direction={"row"} spacing={6}>
        </Stack>
      </Flex>
    </Box>
  );
};

export default Footer;
