import { useEffect, useState } from "react";
import {
  Box, Paper, Typography, Grid, Button, CircularProgress,
  Alert, Divider, TextField, IconButton
} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import ExpandLessIcon from "@mui/icons-material/ExpandLess";
import Autocomplete from "@mui/material/Autocomplete";
import api from "../../services/api";

const APP_ID = "IDENTITY-ONBOARDING-CONFIG";

const IDP_LIST_URL = "/identity-provider/get-all-onboarding-idp?pageNumber=0&pageSize=20";
const ROLE_LOOKUP_URL = "/role/lookup?pageNumber=0&pageSize=100";
const TENANT_URL = "/tenants?pageNumber=0&pageSize=100";
const ROLE_BY_ID_URL = (id) => `/role/${encodeURIComponent(id)}`;
const TENANT_BY_ID_URL = (id) => `/tenants/${encodeURIComponent(id)}`;

export default function OnBoardingConfiguration() {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const [idps, setIdps] = useState([]);           // list of available onboarding IDPs
  const [roles, setRoles] = useState([]);         // flattened roles
  const [tenants, setTenants] = useState([]);     // tenant objects

  const [sections, setSections] = useState([]);   // [{ idpId, roleId, tenantId, expanded }]

  const [addMode, setAddMode] = useState(false);  // show "add new config section"
  const [newConfig, setNewConfig] = useState({ idpId: "", roleId: "", tenantId: "" });

  // ---------------------------
  // UTIL: Normalize Roles
  // ---------------------------
  const normalizeRoles = (respObj) => {
    if (!respObj || typeof respObj !== "object") return [];

    const flat = [];

    Object.entries(respObj).forEach(([idpKey, arr]) => {
      if (Array.isArray(arr)) {
        arr.forEach((obj) => {
          if (typeof obj === "object") {
            Object.entries(obj).forEach(([id, name]) => {
              flat.push({ id: String(id), label: String(name), raw: obj });
            });
          }
        });
      }
    });

    return flat;
  };

  // ---------------------------
  // MAIN INITIAL LOAD
  // ---------------------------
  useEffect(() => {
    loadAll();
  }, []);

  async function loadAll() {
    setLoading(true);
    setError("");

    try {
      // fetch config, idps, roles, tenants
      const [cfgResp, idpResp, rolesResp, tenantsResp] = await Promise.allSettled([
        api.get(`/config-properties?applicationId=${APP_ID}`),
        api.get(IDP_LIST_URL),
        api.get(ROLE_LOOKUP_URL),
        api.get(TENANT_URL)
      ]);

      // ---------------------------
      // IDPs
      // ---------------------------
      let idpList = [];
      if (idpResp.status === "fulfilled" && idpResp.value.data?.success) {
        idpList = idpResp.value.data.responseData || [];
      }
      setIdps(idpList);

      // ---------------------------
      // ROLES (flatten nested object)
      // ---------------------------
      let rolesList = [];
      if (rolesResp.status === "fulfilled" && rolesResp.value.data?.success) {
        rolesList = normalizeRoles(rolesResp.value.data.responseData);
      }
      setRoles(rolesList);

      // ---------------------------
      // TENANTS
      // ---------------------------
      let tenantList = [];
      if (tenantsResp.status === "fulfilled" && tenantsResp.value.data?.success) {
        tenantList = (tenantsResp.value.data.responseData || []).map((t) => ({
          id: t.tenantId,
          label: t.name,
          raw: t
        }));
      }
      setTenants(tenantList);

      // ---------------------------
      // CONFIG
      // ---------------------------
      let existingSections = [];
      if (cfgResp.status === "fulfilled" && cfgResp.value.data?.success) {
        const cfg = cfgResp.value.data?.responseData?.config || {};

        idpList.forEach((idp) => {
          const rid = cfg[`photon.identity.onboarding.${idp.id}.role-id`] || "";
          const tid = cfg[`photon.identity.onboarding.${idp.id}.tenant-id`] || "";

          if (rid || tid) {
            existingSections.push({
              idpId: idp.id,
              roleId: rid,
              tenantId: tid,
              expanded: false
            });
          }
        });
      }

      setSections(existingSections);
    } catch (err) {
      console.error(err);
      setError("Failed to load onboarding configuration");
    } finally {
      setLoading(false);
    }
  }

  // ---------------------------
  // SAVE ONE SECTION
  // ---------------------------
  const saveSection = async (section) => {
    setSaving(true);

    const payload = {
      id: APP_ID,
      config: {
        [`photon.identity.onboarding.${section.idpId}.role-id`]: section.roleId || null,
        [`photon.identity.onboarding.${section.idpId}.tenant-id`]: section.tenantId || null
      }
    };

    try {
      await api.patch(`/config-properties/${APP_ID}`, payload);
      alert("Saved successfully.");
    } catch (err) {
      console.error(err);
      alert("Failed to save.");
    } finally {
      setSaving(false);
    }
  };

  // ---------------------------
  // ADD NEW SECTION
  // ---------------------------
  const handleAddSave = async () => {
    if (!newConfig.idpId) return alert("Select an IDP");

    const payload = {
      id: APP_ID,
      config: {
        [`photon.identity.onboarding.${newConfig.idpId}.role-id`]: newConfig.roleId || null,
        [`photon.identity.onboarding.${newConfig.idpId}.tenant-id`]: newConfig.tenantId || null
      }
    };

    try {
      await api.patch(`/config-properties/${APP_ID}`, payload);

      setSections([
        ...sections,
        { ...newConfig, expanded: false }
      ]);

      setNewConfig({ idpId: "", roleId: "", tenantId: "" });
      setAddMode(false);
      alert("Configuration Added!");
    } catch (err) {
      console.error(err);
      alert("Failed to add configuration");
    }
  };

  // ---------------------------
  // AVAILABLE IDPs FOR NEW CONFIG
  // ---------------------------
  const usedIdps = sections.map((s) => s.idpId);
  const availableIdps = idps.filter((i) => !usedIdps.includes(i.id));

  // ---------------------------
  // UI
  // ---------------------------
  if (loading) {
    return <CircularProgress sx={{ display: "block", margin: "auto", mt: 5 }} />;
  }

  return (
    <Box sx={{ width: "80vw", padding: 4 }}>
      <Typography variant="h4" sx={{ mb: 2, color: "#0b3d91" }}>
        Onboarding Configuration
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {/* EXISTING SECTIONS */}
      {sections.map((sec, idx) => (
        <Paper key={sec.idpId} sx={{ p: 2, mb: 2 }}>
          <Box sx={{ display: "flex", justifyContent: "space-between", cursor: "pointer" }}
               onClick={() => {
                 const updated = [...sections];
                 updated[idx].expanded = !updated[idx].expanded;
                 setSections(updated);
               }}>
            <Typography variant="h6">{sec.idpId}</Typography>
            <IconButton>
              {sec.expanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
            </IconButton>
          </Box>

          {sec.expanded && (
            <>
              <Divider sx={{ my: 2 }} />

              {/* ROLE */}
              <Autocomplete
                options={roles}
                getOptionLabel={(opt) => opt.label}
                value={roles.find((r) => r.id === sec.roleId) || null}
                onChange={(e, v) => {
                  const updated = [...sections];
                  updated[idx].roleId = v ? v.id : "";
                  setSections(updated);
                }}
                renderInput={(params) => (
                  <TextField {...params} label="Role" />
                )}
                sx={{ mb: 2 }}
              />

              {/* TENANT */}
              <Autocomplete
                options={tenants}
                getOptionLabel={(opt) => opt.label}
                value={tenants.find((t) => t.id === sec.tenantId) || null}
                onChange={(e, v) => {
                  const updated = [...sections];
                  updated[idx].tenantId = v ? v.id : "";
                  setSections(updated);
                }}
                renderInput={(params) => (
                  <TextField {...params} label="Tenant" />
                )}
                sx={{ mb: 2 }}
              />

              <Button variant="contained" disabled={saving}
                onClick={() => saveSection(sec)}>
                {saving ? "Saving..." : "Save"}
              </Button>
            </>
          )}
        </Paper>
      ))}

      {/* ADD NEW CONFIG SECTION */}
      {!addMode && availableIdps.length > 0 && (
        <Button variant="outlined" onClick={() => setAddMode(true)}>
          + Add New Onboarding Mapping
        </Button>
      )}

      {addMode && (
        <Paper sx={{ p: 2, mt: 2 }}>
          <Typography variant="h6">Add New Configuration</Typography>
          <Divider sx={{ my: 2 }} />

          {/* IDP SELECTION */}
          <Autocomplete
            options={availableIdps}
            getOptionLabel={(opt) => opt.name}
            value={availableIdps.find((i) => i.id === newConfig.idpId) || null}
            onChange={(e, v) => setNewConfig({ ...newConfig, idpId: v ? v.id : "" })}
            renderInput={(params) => (
              <TextField {...params} label="Select IDP" />
            )}
            sx={{ mb: 2 }}
          />

          {/* ROLE */}
          <Autocomplete
            options={roles}
            getOptionLabel={(opt) => opt.label}
            value={roles.find((r) => r.id === newConfig.roleId) || null}
            onChange={(e, v) =>
              setNewConfig({ ...newConfig, roleId: v ? v.id : "" })
            }
            renderInput={(params) => (
              <TextField {...params} label="Default Role" />
            )}
            sx={{ mb: 2 }}
          />

          {/* TENANT */}
          <Autocomplete
            options={tenants}
            getOptionLabel={(opt) => opt.label}
            value={tenants.find((t) => t.id === newConfig.tenantId) || null}
            onChange={(e, v) =>
              setNewConfig({ ...newConfig, tenantId: v ? v.id : "" })
            }
            renderInput={(params) => (
              <TextField {...params} label="Default Tenant" />
            )}
            sx={{ mb: 2 }}
          />

          <Button variant="contained" sx={{ mr: 2 }} onClick={handleAddSave}>
            Save
          </Button>
          <Button variant="text" onClick={() => setAddMode(false)}>
            Cancel
          </Button>
        </Paper>
      )}
    </Box>
  );
}