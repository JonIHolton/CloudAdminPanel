/* eslint-disable no-nested-ternary */
/* eslint-disable max-statements */
/* eslint-disable max-lines */
import { useEffect, useMemo, useState } from "react";
import { Helmet } from "react-helmet-async";
import { CgAddR } from "react-icons/cg";
import { FaPencilAlt } from "react-icons/fa";
import { DeleteIcon, ViewIcon } from "@chakra-ui/icons";
import {
  Alert,
  AlertIcon,
  AlertTitle,
  Box,
  Button,
  Flex,
  Heading,
  HStack,
  IconButton,
  Modal,
  ModalBody,
  ModalCloseButton,
  ModalContent,
  ModalFooter,
  ModalHeader,
  ModalOverlay,
  Text,
  Tooltip,
  useColorModeValue as mode,
  useDisclosure,
  useToast,
} from "@chakra-ui/react";
import { ActionIcon } from "@mantine/core";
import { IconRefresh } from "@tabler/icons-react";
import { useQuery } from "@tanstack/react-query";
import axios from "axios";
import {
  MantineReactTable,
  type MRT_ColumnDef,
  type MRT_ColumnFilterFnsState,
  type MRT_ColumnFiltersState,
  type MRT_PaginationState,
  type MRT_Row,
  type MRT_SortingState,
  useMantineReactTable,
} from "mantine-react-table";

import { api } from "~features/api";
import { useAuth } from "~features/auth";
import { canPerformAction } from "~features/auth/CheckPermissions";

export interface UserTableType {
  userId: string;
  name: string;
  email: string;
  role?: UserRole;
  createdAt: string;
}

export type UserRole =
  | "Owner"
  | "Manager"
  | "Engineer"
  | "Product Manager"
  | "User";

export const userRoles: UserRole[] = [
  "Owner",
  "Manager",
  "Engineer",
  "Product Manager",
  "User",
];
type UserApiResponse = {
  data: Array<UserTableType>;
  meta: {
    totalRowCount: number;
  };
};

interface Params {
  columnFilters: MRT_ColumnFiltersState;
  sorting: MRT_SortingState;
  pagination: MRT_PaginationState;
}

const useGetUsers = ({ columnFilters, sorting, pagination }: Params) => {
  const [lastQueryData, setLastQueryData] = useState<
    UserApiResponse | undefined
  >();

  const requestData = {
    start: pagination.pageIndex * pagination.pageSize,
    size: pagination.pageSize,
    filters: JSON.stringify(columnFilters ?? []),
    sorting: JSON.stringify(sorting ?? []),
  };

  console.log("start", requestData.start);
  console.log("size", requestData.size);
  console.log("filters", requestData.filters);
  console.log("sorting", requestData.sorting);

  return useQuery<UserApiResponse>({
    queryKey: ["userId", requestData], // refetch whenever the request data changes (columnFilters, globalFilter, sorting, pagination)
    queryFn: async ({ signal }): Promise<UserApiResponse> => {
      const source = axios.CancelToken.source();
      signal.addEventListener("abort", () => {
        source.cancel();
      });
      try {
        const { data: response } = await api.post(
          "/api/v1/users/getAllUsers",
          requestData,
          {
            cancelToken: source.token,
          },
        );
        console.log(response);
        console.log(response.data);
        // const response = generateMockUserApiResponse(100);
        setLastQueryData(response);
        return Promise.resolve(response);
      } catch (error) {
        console.log(error);
        throw new Error("Error fetching data");
      }
    },
    placeholderData: lastQueryData, // useful for paginated queries by keeping data from previous pages on screen while fetching the next page
    staleTime: 30_000, // don't refetch previously viewed pages until cache is more than 30 seconds old
  });
};

const ViewUser = () => {
  const toast = useToast();
  const { isOpen, onOpen, onClose } = useDisclosure();
  const [activeDeleteId, setActiveDeleteId] = useState<string | null>(null);
  const bgColor = "transparent";
  const expandedBgColor = mode("#1A1E43", "#1a202c");
  const textColor = mode("#1A1E43", "white");

  const handleDelete = (row: MRT_Row<UserTableType>) => {
    setActiveDeleteId(row.original.userId);
    onOpen();
  };
  // Define a local state to store permissions for actions
  const [canUpdate, setCanUpdate] = useState(false);
  const [canDelete, setCanDelete] = useState(false);

  const { user } = useAuth();

  useEffect(() => {
    const fetchPermissions = async () => {
      console.log("user?.role", user?.role);
      const updatePermission = await canPerformAction(
        user?.role as string,
        "UserStorage",
        "update",
      );
      console.log("updatePermission", updatePermission);
      const deletePermission = await canPerformAction(
        user?.role as string,
        "UserStorage",
        "delete",
      );
      console.log("deletePermission", deletePermission);

      setCanUpdate(updatePermission);
      setCanDelete(deletePermission);
    };

    fetchPermissions();
  }, [user?.role]);

  const deleteUser = async (userId: string) => {
    try {
      const response = await api.delete(`/api/v1/users/${userId}`);
      return response.data; // Or handle the response as per your API contract
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(
          error.response?.data?.message || "Failed to delete user",
        );
      }
    }
  };

  const onDelete = async () => {
    if (activeDeleteId) {
      try {
        await deleteUser(activeDeleteId);
        // Assuming the API does not return a body for a successful delete
        toast({
          title: `User: ${activeDeleteId} deleted successfully.`,
          status: "success",
        });
        refetch(); // Refresh the data to reflect the deletion
      } catch (error) {
        let errorMessage =
          "An error occurred while trying to delete the user. Please try again.";

        if (axios.isAxiosError(error)) {
          // Handle specific status codes as per your API's contract
          if (error.response?.status === 400) {
            errorMessage = "You cannot delete your own account.";
          } else if (error.response?.status === 500) {
            errorMessage = "A server error occurred while deleting the user.";
          } else {
            // Use the API's error message, if available
            errorMessage = error.response?.data?.message || errorMessage;
          }
        }

        toast({
          title: "Failed to delete user",
          description: errorMessage,
          status: "error",
        });
      } finally {
        onClose(); // Always close the confirmation modal
        setActiveDeleteId(null); // Reset the activeDeleteId state regardless of outcome
      }
    }
  };

  const columns = useMemo<MRT_ColumnDef<UserTableType>[]>(
    () => [
      {
        id: "userId",
        accessorKey: "userId",
        header: "User ID",
        enableClickToCopy: true,
        enableColumnFilterModes: false, // keep this as only string filter
      },
      {
        id: "name",
        accessorKey: "name",
        header: "Name",
        filterVariant: "autocomplete",
      },
      {
        id: "email",
        accessorKey: "email",
        header: "Email",
      },
      {
        id: "role",
        accessorKey: "role",
        header: "Role",
        filterVariant: "select",
        columnFilterModeOptions: ["equals"],
        mantineFilterSelectProps: {
          data: userRoles,
          placeholder: "Roles",
          nothingFound: "No roles found",
        },
        size: 170,
        Cell: ({ cell }) => {
          const cellValue = cell.getValue<string>();
          const roleName = cellValue;

          return roleName;
        },
      },
      {
        accessorFn: (row) => {
          // convert to Date for sorting and filtering
          const sDay = new Date(row.createdAt);
          sDay.setHours(0, 0, 0, 0); // remove time from date (useful if filter by equals exact date)
          return sDay;
        },
        id: "enrollmentDate",
        header: "Enrollment Date",
        enableColumnFilter: true,
        filterVariant: "date-range",
        sortingFn: "datetime",

        size: 150,
        enableColumnFilterModes: false, // keep this as only date-range filter with between inclusive filterFn
        Cell: ({ cell }) => (
          <Box textAlign="center">
            {cell.getValue<Date>()?.toLocaleDateString()}
          </Box>
        ),
        Header: ({ column }) => <em>{column.columnDef.header}</em>,
      },
      {
        id: "actions",
        header: "Actions",
        Cell: ({ row }) => (
          <HStack>
            {canUpdate && (
              <Tooltip hasArrow label="Edit User" bg="gray.300" color="black">
                <Box>
                  <IconButton
                    aria-label="EditUser"
                    as="a"
                    icon={<FaPencilAlt />}
                    href={`/users/view/${row.original.userId}/edit/`}
                    variant=""
                    color={"#1A1E43"}
                    display="inline-flex"
                  />
                </Box>
              </Tooltip>
            )}
            <Tooltip hasArrow label="View User" bg="#1A1E43">
              <Box>
                <IconButton
                  aria-label="EditPoints"
                  as="a"
                  icon={<ViewIcon />}
                  href={`/users/view/${row.original.userId}`}
                  variant=""
                  color="#1A1E43"
                  display="inline-flex"
                />
              </Box>
            </Tooltip>
            {canDelete && (
              <Tooltip hasArrow label="Delete User" bg="red.600">
                <Box>
                  <IconButton
                    aria-label="Delete"
                    icon={<DeleteIcon />}
                    onClick={() => handleDelete(row)}
                    variant="outline"
                    colorScheme="red"
                    display="inline-flex"
                    onFocus={(event) => event.preventDefault()}
                  />
                </Box>
              </Tooltip>
            )}
          </HStack>
        ),
      },
    ],
    [canUpdate, canDelete],
  );

  // Manage MRT state that we want to pass to our API
  const [columnFilters, setColumnFilters] = useState<MRT_ColumnFiltersState>(
    [],
  );
  const [columnFilterFns, setColumnFilterFns] =
    useState<MRT_ColumnFilterFnsState>(
      Object.fromEntries(
        columns.map(({ accessorKey }) => [
          accessorKey,
          accessorKey === "userId" || accessorKey === "role"
            ? "equalsString"
            : "contains",
        ]),
      ),
    ); // setting default filter mode

  const [sorting, setSorting] = useState<MRT_SortingState>([]);
  const [pagination, setPagination] = useState<MRT_PaginationState>({
    pageIndex: 0,
    pageSize: 10,
  });

  // call custom react-query hook
  const { data, isError, isFetching, isLoading, refetch } = useGetUsers({
    columnFilters,
    pagination,
    sorting,
  });

  // this will depend on your API response shape
  const fetchedUsers = (data as UserApiResponse)?.data ?? [];
  const totalRowCount = (data as UserApiResponse)?.meta?.totalRowCount ?? 0;

  const table = useMantineReactTable({
    columns,
    data: fetchedUsers,
    enableColumnFilterModes: false,
    enableFacetedValues: true,
    enableGlobalFilter: false,
    enableColumnOrdering: true,
    initialState: { showColumnFilters: true },
    manualFiltering: true,
    manualPagination: true,
    paginationDisplayMode: "pages",
    manualSorting: true,
    enableFullScreenToggle: false,
    enableColumnDragging: false,
    enableClickToCopy: false,
    mantinePaperProps: ({ table }) => ({
      withBorder: false,
      shadow: undefined,
      bg: table.getState().isFullScreen ? expandedBgColor : bgColor,
      sx: {
        "& .mantine-Alert-root": {
          backgroundColor: "transparent",
        },
        "& .mantine-Input-input": {
          color: textColor,
        },
      },
    }),
    mantineTopToolbarProps: {
      bg: bgColor,
      sx: {
        "& .mantine-Input-input": {
          color: textColor,
        },
      },
    },
    mantineBottomToolbarProps: {
      bg: bgColor,
      style: {
        color: textColor,
      },
      sx: {
        "& .mantine-InputWrapper-label": {
          color: textColor,
        },
      },
    },
    mantineTableHeadRowProps: {
      bg: bgColor,
    },
    mantineTableBodyCellProps: {
      bg: bgColor,
      style: {
        color: textColor,
      },
    },
    mantineTableProps: {
      sx: {
        "& .mantine-TextInput-input": {
          color: textColor,
        },
        "& .mantine-Alert-root": {
          backgroundColor: "transparent",
        },
      },
      highlightOnHover: false,
    },
    mantineTableHeadCellProps: {
      style: {
        color: textColor,
      },
    },
    mantineFilterTextInputProps: {
      style: {
        color: textColor,
      },
      sx: {
        "& .mantine-Input-input": {
          color: textColor,
        },
      },
    },
    onColumnFilterFnsChange: setColumnFilterFns,
    onColumnFiltersChange: setColumnFilters,
    onPaginationChange: setPagination,
    onSortingChange: setSorting,
    renderTopToolbarCustomActions: () => (
      <Tooltip label="Refresh Data" placement="top">
        <ActionIcon onClick={() => refetch()}>
          <IconRefresh />
        </ActionIcon>
      </Tooltip>
    ),
    renderToolbarAlertBannerContent: () => (
      <Alert status="error">
        <AlertIcon />
        <AlertTitle color={textColor}>There was an error!</AlertTitle>
      </Alert>
    ),
    rowCount: totalRowCount,
    state: {
      columnFilterFns,
      columnFilters,
      isLoading,
      pagination,
      showAlertBanner: isError,
      showProgressBars: isFetching,
      sorting,
    },
  });

  return (
    <Box p={4} bg={mode("white", "gray.800")}>
      <Helmet>
        <title>User Table</title>
        <meta name="description" content="User Table" />
      </Helmet>
      <Flex justifyContent="space-between" m={4} alignItems="center">
        <Flex alignItems="center">
          <Heading as="h3" size="lg" color="branding.100">
            Users
          </Heading>
        </Flex>
        <Flex>
          <Button
            as="a"
            href="/users/create"
            leftIcon={<CgAddR />}
            color="white"
            border={"none,"}
            bg="#1A1E43"
            display={{ base: "none", sm: "inline-flex" }}
            _hover={{
              bg: "#282e69",
              color: "white",
            }}
          >
            Enroll new user
          </Button>
          <IconButton
            aria-label="Enroll new user"
            icon={<CgAddR />}
            as="a"
            href="/users/create"
            color="white"
            bg="#1A1E43"
            display={{ base: "inline-flex", sm: "none" }}
          />
        </Flex>
      </Flex>
      <MantineReactTable table={table} />
      <Modal blockScrollOnMount={false} isOpen={isOpen} onClose={onClose}>
        <ModalOverlay />
        <ModalContent>
          <ModalHeader>Confirmation</ModalHeader>
          <ModalCloseButton />
          <ModalBody>
            <Text fontWeight="bold" mb="1rem">
              Are you sure you want to delete User: {activeDeleteId}?
            </Text>
          </ModalBody>
          <ModalFooter>
            <Button colorScheme="blue" mr={3} onClick={onClose}>
              Close
            </Button>
            <Button variant="outline" colorScheme="red" onClick={onDelete}>
              Delete
            </Button>
          </ModalFooter>
        </ModalContent>
      </Modal>
    </Box>
  );
};

export default ViewUser;
