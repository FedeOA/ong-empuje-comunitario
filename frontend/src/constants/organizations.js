export const organizationById = {
  1: "ONG EMPUJE COMUNITARIO",
  2: "ONG SOMOS MAS",
  3: "ONG CREANDO LAZOS",
  4: "ONG ABRIENDO CAMINOS"
};

export const getOrganizationName = (organizationId) => organizationById[organizationId] || "Desconocido";