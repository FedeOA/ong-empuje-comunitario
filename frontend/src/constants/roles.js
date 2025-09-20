export const roles = {
  PRESIDENTE: "PRESIDENTE",
  VOCAL: "VOCAL",
  COORDINADOR: "COORDINADOR",
  VOLUNTARIO: "VOLUNTARIO",
};

export const roleById = {
  1: "PRESIDENTE",
  2: "COORDINADOR",
  3: "VOLUNTARIO",
  4: "VOCAL"
};

export const getRoleName = (roleId) => roleById[roleId] || "Desconocido";

export const getRoleOptions = () =>
  Object.entries(roles).map(([key, value]) => ({
    value: key,
    label: value.charAt(0) + value.slice(1).toLowerCase(),
  }));
