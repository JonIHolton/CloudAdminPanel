import React from "react";
import {
  Box,
  Checkbox,
  Table,
  Tbody,
  Td,
  Th,
  Thead,
  Tr,
} from "@chakra-ui/react";

// Define interfaces for the actions and roles
interface ModuleActions {
  actions: string[];
  constraints?: string[];
}

interface RolePermissions {
  [module: string]: ModuleActions;
}

interface Roles {
  [role: string]: RolePermissions;
}

interface AccessControlTableProps {
  roles: Roles;
}

const AccessControlTable: React.FC<AccessControlTableProps> = ({ roles }) => {
  const allActions = new Set<string>();
  Object.values(roles).forEach((rolePermissions) => {
    Object.values(rolePermissions).forEach((module) => {
      module.actions.forEach((action) => allActions.add(action));
    });
  });
  const actionsArray = Array.from(allActions);

  const hasAction = (role: string, module: string, action: string) => {
    return roles[role][module] && roles[role][module].actions.includes(action);
  };

  return (
    <Box overflowX="auto">
      <Table variant="simple">
        <Thead>
          <Tr>
            <Th>Role</Th>
            {actionsArray.map((action) => (
              <Th key={action} isNumeric>
                {action.charAt(0).toUpperCase() + action.slice(1)}
              </Th>
            ))}
          </Tr>
        </Thead>
        <Tbody>
          {Object.keys(roles).map((role) => (
            <Tr key={role}>
              <Td>{role}</Td>
              {actionsArray.map((action) => (
                <Td key={action} isNumeric>
                  {Object.keys(roles[role]).some((module) =>
                    hasAction(role, module, action),
                  ) ? (
                    <Checkbox
                      color="branding.100"
                      isChecked={true}
                      isDisabled={true}
                    />
                  ) : (
                    <Checkbox isChecked={false} isDisabled={true} />
                  )}
                </Td>
              ))}
            </Tr>
          ))}
        </Tbody>
      </Table>
    </Box>
  );
};

export default AccessControlTable;
