import React, { useEffect, useState } from "react";
import { Box, Button, Typography,
  Accordion, AccordionSummary, AccordionDetails, Divider, TextField, Dialog, DialogTitle, DialogContent, DialogActions,
  List, ListItem, ListItemButton, CircularProgress, MenuItem, FormControl, InputLabel, Select, Paper, Chip } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { useNavigate } from "react-router-dom";
import api from "../services/api"; 

const sanitizeInput = (raw) => raw.replace(/[^a-zA-Z0-9\s_]/g, "").toUpperCase().trim();
const formatNameFinal = (raw) => raw.toUpperCase().trim().replace(/\s+/g, "_");

const AlertsPage = () => {
  const [alertsGrouped, setAlertsGrouped] = useState([]);
  const [loading, setLoading] = useState(true);
  const [openCreateAlertDialog, setOpenCreateAlertDialog] = useState(false);
  const [alertName, setAlertName] = useState("");
  const [openCreateSubtypeDialog, setOpenCreateSubtypeDialog] = useState(false);
  const [creatingFor, setCreatingFor] = useState(null);
  const [subTypeRaw, setSubTypeRaw] = useState("");
  const [audience, setAudience] = useState("INDIVIDUAL");
  const [topic, setTopic] = useState("");
  const [busyCreating, setBusyCreating] = useState(false);
  const [error, setError] = useState("");


  const navigate = useNavigate();

  useEffect(() => {
    fetchAlerts();
  }, []);

  const fetchAlerts = async () => {
    setLoading(true);
    try {
      const resp = await api.get("/alerts?pageNumber=0&pageSize=10");
      setAlertsGrouped(resp.data.responseData || []);
    } catch (err) {
      console.error("Error fetching alerts", err);
      setAlertsGrouped([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateAlert = async () => {
    if (!alertName) {
        setError("Alert name cannot be empty.");
        return;
    }
    setError("");
    const finalName = formatNameFinal(alertName);

    try {
      await api.post("/alerts", { alertType: finalName });
      setOpenCreateAlertDialog(false);
      setAlertName("");
      setError(""); 
      await fetchAlerts();
    } catch (err) {
      console.error("Failed to create alert type", err);
      setError("Failed to create alert type. It might already exist or there was an API error.");
    }
  };

  const handleAlertNameChange = (e) => {
    const raw = e.target.value;
    setAlertName(sanitizeInput(raw)); 
  };

  const openCreateSubtypeForGroup = (group) => {
    const parentAlert =
      (group.alerts && group.alerts.find((a) => !a.alertSubType)) || (group.alerts && group.alerts[0]) || null;

    setCreatingFor({
      alertType: group.alertType,
      parentAlertId: parentAlert?.id || null,
    });

    setSubTypeRaw("");
    setAudience("INDIVIDUAL");
    setTopic("");
    setError("");
    setOpenCreateSubtypeDialog(true);
  };

  const closeCreateSubtypeDialog = () => {
    setOpenCreateSubtypeDialog(false);
    setCreatingFor(null);
    setError("");
  };

  const handleSubTypeRawChange = (e) => {
    setSubTypeRaw(sanitizeInput(e.target.value));
  };
  
  const handleCreateSubtype = async () => {
    if (!subTypeRaw) {
        setError("Subtype name cannot be empty.");
        return;
    }

    const formatted = formatNameFinal(subTypeRaw);
    if (!formatted) {
        setError("Invalid subtype name after formatting.");
        return;
    }
    
    if (!creatingFor.parentAlertId) {
      setError("Cannot determine parent alert id for this alert type.");
      return;
    }

    const payload = {
      alertType: creatingFor.alertType,
      alertSubType: formatted,
      audience,
      topic: audience === "TOPIC" ? topic : "",
      templates: [],
    };

    setBusyCreating(true);
    setError("");

    try {
      await api.post(`/alerts`, payload);
      await fetchAlerts();
      closeCreateSubtypeDialog();

      const resp = await api.get("/alerts?pageNumber=0&pageSize=10");
      const latestGroups = resp.data.responseData || [];
      const grp = latestGroups.find((g) => g.alertType === creatingFor.alertType);
      const newAlertObj = grp ? (grp.alerts || []).find((a) => a.alertSubType === formatted) : null;

      if (newAlertObj && newAlertObj.id) {
        navigate(`/alerts/${newAlertObj.id}`);
      } else {
        await fetchAlerts();
        console.log("Subtype created but could not auto-navigate.");
      }
    } catch (err) {
      console.error("Failed to create subtype", err);
      setError("Failed to create subtype. It may already exist or there was an API error.");
    } finally {
      setBusyCreating(false);
    }
  };

  if (loading) {
    return (
      <Box 
        sx={{ 
          width: "100%", 
          minHeight: "80vh", 
          padding: "20px", 
          display: "flex", 
          justifyContent: "center", 
          alignItems: "center", 
          backgroundColor: "#f4f7f9" 
        }}>
        <CircularProgress color="primary" />
      </Box>
    );
  }

  return (
    <Box sx={{ width: "100%", minHeight: "100vh", padding: "32px", backgroundColor: "#f4f7f9" }}>
      
      <Box sx={{ display: "flex", justifyContent: "space-between", mb: 4, alignItems: "center" }}>
        <Typography variant="h4" sx={{ fontWeight: 700, color: '#1a202c' }}>Alerts Configuration</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => {setOpenCreateAlertDialog(true); setError("");}}
            sx={{ 
              backgroundColor: '#007bff', '&:hover': { backgroundColor: '#0056b3' },
              borderRadius: '8px',
              textTransform: 'none'
            }}
        >
          Create New Alert Type
        </Button>
      </Box>

      <Paper elevation={3} sx={{ p: 4, borderRadius: '12px' }}>
      {alertsGrouped.length === 0 ? (
        <Box sx={{ textAlign: 'center', p: 4 }}>
            <Typography variant="h6" color="text.secondary">No alert types have been configured yet.</Typography>
            <Typography variant="body2" color="text.disabled">Click 'Create New Alert Type' to begin configuration.</Typography>
        </Box>
      ) : (
        alertsGrouped.map((group, idx) => {
          const subtypes = (group.alerts || []).filter((a) => a.alertSubType);
          const totalSubtypes = subtypes.length;

          return (
            <Accordion key={group.alertType + "_" + idx} defaultExpanded={true} sx={{ mb: 2, borderRadius: '8px !important', overflow: 'hidden', border: '1px solid #e2e8f0' }}>
              <AccordionSummary expandIcon={<ExpandMoreIcon sx={{ color: '#007bff' }} />}
                sx={{ 
                    backgroundColor: '#f7f9fb', 
                    '&:hover': { backgroundColor: '#edf2f7' },
                    borderBottom: '1px solid #e2e8f0' 
                }}
              >
                <Typography variant="h6" sx={{ flexShrink: 0, fontWeight: 700, color: '#1a202c' }}>
                    {group.alertType}
                </Typography>
                <Typography sx={{ color: 'text.secondary', ml: 2, alignSelf: 'center' }}>
                    ({totalSubtypes} Subtype{totalSubtypes !== 1 ? 's' : ''})
                </Typography>
              </AccordionSummary>

              <AccordionDetails sx={{ p: 0 }}>
                {totalSubtypes === 0 ? (
                  <Box sx={{ p: 3, textAlign: 'center' }}>
                    <Typography color="text.secondary">No subtypes available for this alert type.</Typography>
                  </Box>
                ) : (
                  <List disablePadding>
                    {subtypes.map((a, sIdx) => (
                      <React.Fragment key={a.id}>
                        <ListItem disablePadding sx={{ backgroundColor: 'white', '&:hover': { backgroundColor: '#f0f8ff' } }}>
                          <ListItemButton onClick={() => navigate(`/alerts/${a.id}`)} sx={{ py: 1.5, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <Box>
                                <Typography variant="body1" sx={{ fontWeight: 500 }}>{a.alertSubType}</Typography>
                                <Typography variant="caption" color="text.secondary">{a.description || 'No description'}</Typography>
                            </Box>
                            <Chip label={a.audience} size="small" color={a.audience === 'BROADCAST' ? 'error' : a.audience === 'TOPIC' ? 'info' : 'success'} sx={{ fontWeight: 600 }}/>
                          </ListItemButton>
                        </ListItem>
                        {sIdx < totalSubtypes - 1 && <Divider component="li" light />}
                      </React.Fragment>
                    ))}
                  </List>
                )}

                <Box sx={{ p: 2, borderTop: '1px solid #e2e8f0', backgroundColor: '#f9f9f9', display: 'flex', justifyContent: 'flex-end' }}>
                  <Button variant="outlined" onClick={() => openCreateSubtypeForGroup(group)} startIcon={<AddIcon />}
                    sx={{ 
                        borderRadius: '6px', 
                        textTransform: 'none',
                        borderColor: '#007bff',
                        color: '#007bff',
                        '&:hover': { backgroundColor: '#e6f7ff', borderColor: '#0056b3' }
                    }}
                  >
                    Create Subtype
                  </Button>
                </Box>
              </AccordionDetails>
            </Accordion>
          );
        })
      )}
      </Paper>

      {/* Create AlertType Dialog */}
      <Dialog open={openCreateAlertDialog} onClose={() => {setOpenCreateAlertDialog(false); setError("");}} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ backgroundColor: '#007bff', color: 'white' }}>Create New Alert Type</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          {error && <Typography color="error" sx={{ mb: 2 }}>{error}</Typography>}
          <TextField label="Alert Type Name (Will be UP_CASE_UNDERSCORE)" value={alertName} onChange={handleAlertNameChange} fullWidth margin="normal" helperText={`Formatted Preview: ${formatNameFinal(alertName)}`} placeholder="e.g., User Status Change"/>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => {setOpenCreateAlertDialog(false); setError("");}} color="secondary">Cancel</Button>
          <Button variant="contained" onClick={handleCreateAlert} disabled={!alertName} sx={{ backgroundColor: '#007bff' }}>
            Create
          </Button>
        </DialogActions>
      </Dialog>

      {/* Inline Create Subtype Dialog */}
      <Dialog open={openCreateSubtypeDialog} onClose={closeCreateSubtypeDialog} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ backgroundColor: '#007bff', color: 'white' }}>
            Create Subtype {creatingFor?.alertType ? `for ${creatingFor.alertType}` : ""}
        </DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
            {error && <Typography color="error" sx={{ mb: 2 }}>{error}</Typography>}
            <TextField label="Subtype Name (Allows A-Z, 0-9, spaces, and _)" fullWidth margin="normal" value={subTypeRaw} onChange={handleSubTypeRawChange} placeholder="e.g., PASSWORD_RESET_SUCCESS" helperText={`Formatted Preview: ${formatNameFinal(subTypeRaw)}`}/>
            <FormControl fullWidth margin="normal">
                <InputLabel>Audience</InputLabel>
                <Select value={audience} label="Audience" onChange={(e) => setAudience(e.target.value)}>
                    <MenuItem value="BROADCAST">BROADCAST (All subscribers)</MenuItem>
                    <MenuItem value="INDIVIDUAL">INDIVIDUAL (Single user target)</MenuItem>
                    <MenuItem value="TOPIC">TOPIC (Specific topic channel)</MenuItem>
                </Select>
            </FormControl>

            {audience === "TOPIC" && (
                <TextField label="Topic Name" fullWidth margin="normal" value={topic} onChange={(e) => setTopic(e.target.value)} helperText="Required when audience is TOPIC" />
            )}
        </DialogContent>

        <DialogActions sx={{ p: 2 }}>
          <Button onClick={closeCreateSubtypeDialog}>Cancel</Button>
          <Button variant="contained" onClick={handleCreateSubtype} disabled={!subTypeRaw || busyCreating || (audience === 'TOPIC' && !topic)} sx={{ backgroundColor: '#007bff' }}>
            {busyCreating ? <CircularProgress size={24} color="inherit" /> : "Create Subtype"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AlertsPage;