/* eslint-disable no-nested-ternary */
/* eslint-disable max-statements */
/* eslint-disable max-lines */
import { useMemo, useState } from "react";
import { Helmet } from "react-helmet-async";
// import { ViewIcon } from "@chakra-ui/icons";
import {
  Alert,
  AlertIcon,
  AlertTitle,
  Box,
  Flex,
  Heading,
  /*
   * HStack,
   * IconButton,
   */
  Tooltip,
  useColorModeValue as mode,
} from "@chakra-ui/react";
import { ActionIcon } from "@mantine/core";
import { IconRefresh } from "@tabler/icons-react";
import { useQuery } from "@tanstack/react-query";
import {
  MantineReactTable,
  type MRT_ColumnDef,
  type MRT_ColumnFilterFnsState,
  type MRT_ColumnFiltersState,
  type MRT_PaginationState,
  type MRT_SortingState,
  useMantineReactTable,
} from "mantine-react-table";

import { api } from "~features/api";

export interface LogTableType {
  logId: string;
  timestamp: number;
  timezone: string;
  description: string;
  deviceInfo: string;
  ipAddress: string;
  initiatorUser: string;
  targetUser: string;
}

type LogApiResponse = {
  results: Array<LogTableType>;
  total: number;
};

interface Params {
  columnFilters: MRT_ColumnFiltersState;
  sorting: MRT_SortingState;
  pagination: MRT_PaginationState;
}

const useGetLogs = ({ columnFilters, sorting, pagination }: Params) => {
  const [lastQueryData, setLastQueryData] = useState<
    LogApiResponse | undefined
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

  // This should be the part where the construction of the query happens
  return useQuery({
    queryKey: ["logId", requestData], // refetch whenever the request data changes (columnFilters, globalFilter, sorting, pagination)
    // Inside useQuery
    queryFn: async (): Promise<unknown> => {
      try {
        console.log("Start of fetching logs");

        // Instead of api.get, use api.post and move requestData to the request body
        const { data: response } = await api.post(
          "/api/v1/logs/query",
          requestData,
        );

        console.log(response);
        setLastQueryData(response);
        return Promise.resolve(response);
      } catch (error) {
        console.error(error);
        throw new Error("Error fetching data");
      }
    },

    placeholderData: lastQueryData, // useful for paginated queries by keeping data from previous pages on screen while fetching the next page
    staleTime: 30_000, // don't refetch previously viewed pages until cache is more than 30 seconds old
  });
};

const ViewAllLogs = () => {
  const bgColor = "transparent";
  const expandedBgColor = mode("#1A1E43", "#1a202c");
  const textColor = mode("#1A1E43", "white");

  const columns = useMemo<MRT_ColumnDef<LogTableType>[]>(
    () => [
      {
        accessorFn: (row) => {
          const timestampInSeconds = Math.floor(row.timestamp);
          const nanoseconds = (row.timestamp - timestampInSeconds) * 1000;
          const timestampInMilliseconds =
            // eslint-disable-next-line prettier/prettier
            ((timestampInSeconds * 1000) + nanoseconds);
          return new Date(timestampInMilliseconds);
        },
        id: "timestamp",
        header: "Time",
        enableColumnFilter: true,
        filterVariant: "date-range",
        size: 150,
        enableColumnFilterModes: true,
        Cell: ({ cell }) => (
          <Box textAlign="center">
            {cell.getValue<Date>().toLocaleString("en-US", {
              year: "numeric",
              month: "2-digit",
              day: "2-digit",
              hour: "2-digit",
              minute: "2-digit",
              second: "2-digit",
              hour12: false, // Use 24-hour clock
            })}
          </Box>
        ),
        Header: ({ column }) => <em>{column.columnDef.header}</em>, // custom header markup
      },
      {
        id: "logId",
        accessorKey: "logId",
        header: "Log ID",
        enableClickToCopy: true,
        enableColumnFilterModes: false, // keep this as only string filter
      },
      {
        id: "userId",
        accessorKey: "initiatorUser",
        header: "User ID",
        enableClickToCopy: true,
        enableColumnFilterModes: false, // keep this as only string filter
      },
      {
        id: "description",
        accessorKey: "description",
        header: "Description",
      },
      /*
       * {
       *   id: "actions",
       *   header: "View Log",
       *   Cell: ({ row }) => (
       *     <HStack>
       *       <Tooltip hasArrow label="View Log" bg="#1A1E43">
       *         <Box>
       *           <IconButton
       *             aria-label="ViewLog"
       *             as="a"
       *             icon={<ViewIcon />}
       *             href={`/logs/view/${row.original.logId}`}
       *             variant=""
       *             color="#1A1E43"
       *             display="inline-flex"
       *           />
       *         </Box>
       *       </Tooltip>
       *     </HStack>
       *   ),
       * },
       */
    ],
    [],
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
          accessorKey === "logId" || accessorKey === "userid"
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
  const { data, isError, isFetching, isLoading, refetch } = useGetLogs({
    columnFilters,
    pagination,
    sorting,
  });

  // this will depend on your API response shape
  const fetchedUsers = (data as LogApiResponse)?.results ?? [];
  const totalRowCount = (data as LogApiResponse)?.total ?? 0;

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
        <title>Log Table</title>
        <meta name="description" content="Log Table" />
      </Helmet>
      <Flex justifyContent="space-between" m={4} alignItems="center">
        <Flex alignItems="center">
          <Heading as="h3" size="lg" color="branding.100">
            Logs
          </Heading>
        </Flex>
      </Flex>
      <MantineReactTable table={table} />
    </Box>
  );
};

export default ViewAllLogs;
