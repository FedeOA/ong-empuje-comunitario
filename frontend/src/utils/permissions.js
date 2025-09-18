import { roles } from "../constants/roles";

export const rolePermissions = {
  [roles.PRESIDENTE]: ["users", "events", "donations"],
  [roles.VOCAL]: ["donations"],
  [roles.COORDINADOR]: ["events"],
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

