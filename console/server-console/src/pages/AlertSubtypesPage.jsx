import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Box, Typography, List, ListItem, ListItemButton, Divider, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, FormControl, InputLabel, Select} from '@mui/material';
import api from '../services/api';

const AlertSubtypesPage = () => {
  const { state } = useLocation();
  const navigate = useNavigate();

  const [alertType, setAlertType] = useState(state?.alertType || '');
  const [alerts, setAlerts] = useState(state?.alerts || []);
  const [parentAlert, setParentAlert] = useState(state?.parentAlert || null);

  const [openCreate, setOpenCreate] = useState(false);
  const [subTypeRaw, setSubTypeRaw] = useState('');
  const [audience, setAudience] = useState('INDIVIDUAL');
  const [topic, setTopic] = useState('');

  useEffect(() => {
    if (!state || !state.alertType) {
      return;
    }
    setAlertType(state.alertType);
    setAlerts(state.alerts || []);
    setParentAlert(state.parentAlert || null);
  }, [state]);

  const openCreateDialog = () => {
    setSubTypeRaw('');
    setAudience('INDIVIDUAL');
    setTopic('');
    setOpenCreate(true);
  };

  const closeCreateDialog = () => setOpenCreate(false);

  const formatSubType = (raw) => {
    const cleaned = raw.replace(/[^a-zA-Z\s]/g, ''); // only letters and spaces
    return cleaned.toUpperCase().trim().replace(/\s+/g, '_'); // uppercase w/ underscores
  };

  const handleCreateSubtype = async () => {
    if (!subTypeRaw) return alert('Please provide subtype name');

    if (!parentAlert || !parentAlert.id) {
      return alert('Cannot create subtype: parent alert not found on this alertType.');
    }

    const formatted = formatSubType(subTypeRaw);

    const payload = {
      alertType: alertType,
      alertSubType: formatted,
      audience,
      topic: audience === 'TOPIC' ? topic : '',
      templates: [], // initially empty
    };

    try {
      // PUT to parent alert id to add subtype (as per your API)
      await api.patch(`/alerts/${parentAlert.id}`, payload);

      // fetch updated parent alert to get the new entry
      const resp = await api.get(`/alerts/${parentAlert.id}`);
      const updatedAlert = resp.data.responseData;

      // updatedAlert could be the single alert; update local alerts list:
      // If API returns the new alert with subtype, push it into alerts
      // If the response shape differs, you may need to adjust this
      if (updatedAlert) {
        // If backend returns an alert (single) or array, normalize:
        const newEntry = Array.isArray(updatedAlert) ? updatedAlert[0] : updatedAlert;
        // Merge into alerts: remove any existing with same id then add
        setAlerts(prev => {
          const filtered = prev.filter(a => a.id !== newEntry.id);
          return [...filtered, newEntry];
        });
      } else {
        // fallback: append client-side (minimal)
        setAlerts(prev => [...prev, {
          id: Date.now(), // temporary id
          alertType: alertType,
          alertSubType: formatted,
          audience,
          topic: audience === 'TOPIC' ? topic : '',
          templates: []
        }]);
      }

      setOpenCreate(false);
    } catch (err) {
      console.error('Failed to create subtype', err);
      alert('Failed to create subtype');
    }
  };

  const handleOpenDetails = (alertObj) => {
    navigate(`/alerts/${alertObj.id}`);
  };

  if (!state || !state.alertType) {
    return (
      <Box sx={{ width: '80vw', minHeight: '80vh', padding: '20px', backgroundColor: '#f0f4f8' }}>
        <Typography variant="h6">No alert data provided. Please return to Alerts list.</Typography>
        <Button sx={{ mt: 2 }} onClick={() => navigate('/alerts')}>Back to Alerts</Button>
      </Box>
    );
  }

  const subtypes = alerts.filter(a => a.alertSubType);

  return (
    <Box sx={{ width: '80vw', minHeight: '80vh', padding: '20px', backgroundColor: '#f0f4f8' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4">{alertType} - Subtypes</Typography>
        <Button variant="contained" onClick={openCreateDialog}>+ Create Subtype</Button>
      </Box>

      <Divider sx={{ mb: 2 }} />

      {subtypes.length === 0 ? (
        <Box>
          <Typography>No subtypes found for this alert type.</Typography>
          <Box sx={{ mt: 2 }}>
            <Button variant="outlined" onClick={openCreateDialog}>Create new subtype</Button>
          </Box>
        </Box>
      ) : (
        <List>
          {subtypes.map((a) => (
            <React.Fragment key={a.id}>
              <ListItem disablePadding>
                <ListItemButton onClick={() => handleOpenDetails(a)}>
                  <Typography>{a.alertSubType}</Typography>
                </ListItemButton>
              </ListItem>
              <Divider />
            </React.Fragment>
          ))}
        </List>
      )}

      {/* Create Subtype Dialog */}
      <Dialog open={openCreate} onClose={closeCreateDialog}>
        <DialogTitle>Create Subtype for {alertType}</DialogTitle>
        <DialogContent>
          <TextField label="Subtype Name" fullWidth margin="normal" value={subTypeRaw} onChange={(e) => setSubTypeRaw(e.target.value)} placeholder="Letters and spaces only — will be uppercased and spaces replaced with underscores"/>
          <FormControl fullWidth margin="normal">
            <InputLabel>Audience</InputLabel>
            <Select value={audience} label="Audience" onChange={(e) => setAudience(e.target.value)}>
              <MenuItem value="BROADCAST">BROADCAST</MenuItem>
              <MenuItem value="INDIVIDUAL">INDIVIDUAL</MenuItem>
              <MenuItem value="TOPIC">TOPIC</MenuItem>
            </Select>
          </FormControl>

          {audience === 'TOPIC' && (
            <TextField label="Topic" fullWidth margin="normal" value={topic} onChange={(e) => setTopic(e.target.value)} />
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={closeCreateDialog}>Cancel</Button>
          <Button onClick={handleCreateSubtype} variant="contained" disabled={!subTypeRaw}>
            Create
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AlertSubtypesPage;