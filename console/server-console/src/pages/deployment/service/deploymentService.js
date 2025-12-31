import api from "../../../services/api";

const API_BASE = "/deployment";

export const getDeploymentById = async (id) => {
  const resp = await api.get(`${API_BASE}/${id}`);
  return resp.data?.responseData;
};

export const publishDeployment = async (id) => {
  return api.post(`${API_BASE}/${id}/publish`);
};

export const unpublishDeployment = async (id) => {
  return api.post(`${API_BASE}/${id}/unpublish`);
};

export const uploadJar = async (id, formData) => {
  return api.post(`${API_BASE}/${id}/upload-jar`, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

export const updateDeployment = async (id, body) => {
  return api.put(`${API_BASE}/${id}`, body);
};