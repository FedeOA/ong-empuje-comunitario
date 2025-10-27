import { roles } from "../constants/roles";

export const rolePermissions = {
  [roles.PRESIDENTE]: ["users", "events", "externalEvents", "donations", "donation-requests", "donation-offers", "donation-transfers", "donation-reports", "donation-report-excel", "soap-data"],
  [roles.VOCAL]: ["donations", "donation-requests", "donation-offers", "donation-transfers", "donation-reports", "donation-report-excel", "soap-data"],
  [roles.COORDINADOR]: ["events","externalEvents"],
  [roles.VOLUNTARIO]: ["events", "externalEvents"],
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

