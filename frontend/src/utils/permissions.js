import { roles } from "../constants/roles";

export const rolePermissions = {
  [roles.PRESIDENTE]: ["users", "events", "donations","externalEvents","donation-requests"],
  [roles.VOCAL]: ["donations"],
  [roles.COORDINADOR]: ["events","externalEvents"],
  [roles.VOLUNTARIO]: ["events"],
};

export function hasPermission(role, section) {
  return rolePermissions[role]?.includes(section);
}

export const defaultSectionByRole = {
  PRESIDENTE: "users",
  VOCAL: "donations",
  COORDINADOR: "events",
  VOLUNTARIO: "events",
};

export function getDefaultSection(role) {
  return defaultSectionByRole[role] || null;
}

