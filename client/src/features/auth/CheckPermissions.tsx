/* eslint-disable @typescript-eslint/no-explicit-any */
import { policyDocument } from "./PolicyDocument";

interface ActionPermissions {
  actions: string[];
  constraints?: string[];
}

interface ResourcePermissions {
  [resourceName: string]: ActionPermissions;
}

export interface RolesPolicy {
  [roleName: string]: ResourcePermissions;
}

// Enhanced memoization utility
function memoizeAsync<T extends (...args: any[]) => Promise<any>>(
  fn: T,
  { cacheExpirationMs }: { cacheExpirationMs?: number } = {},
): T {
  const cache = new Map<
    string,
    { expirationTime: number; value: Promise<any> }
  >();

  return function (...args: any[]): Promise<any> {
    const key = JSON.stringify(args);
    const now = Date.now();

    // Check if we have a cached value and it hasn't expired
    if (cache.has(key)) {
      const cached = cache.get(key);
      if (
        cached &&
        (cacheExpirationMs === undefined || cached.expirationTime > now)
      ) {
        return cached.value;
      }
    }

    // If not, or it's expired, call the function and cache the result
    const result = fn(...args);
    cache.set(key, {
      expirationTime: now + (cacheExpirationMs || 0),
      value: result,
    });

    // Optional: Consider implementing cache size limits and eviction policies here

    result.catch(() => {
      // In case of error, remove from cache
      cache.delete(key);
    });

    return result;
  } as T;
}

// Usage with async function, including cache expiration (optional)
const memoizedCanPerformAction = memoizeAsync(
  async (
    role: keyof RolesPolicy,
    resource: string,
    action: string,
  ): Promise<boolean> => {
    try {
      const policy = await policyDocument(); // Wait for the policy document
      const permissions = policy[role]?.[resource];

      if (!permissions) {
        return false;
      }

      return permissions.actions.includes(action);
    } catch (error) {
      console.error("Error in canPerformAction:", error);
      throw error; // Rethrow to allow the memoization utility to catch and handle it
    }
  },
  { cacheExpirationMs: 1000 * 60 * 10 }, // Example: cache expires after 10 minutes
);

export { memoizedCanPerformAction as canPerformAction };
