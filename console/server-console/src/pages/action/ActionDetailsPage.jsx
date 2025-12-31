import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { 
  Box, Typography, Table, TableBody, TableCell, TableContainer, TableRow, 
  Button, MenuItem, Select, FormControl, InputLabel, Collapse, IconButton,
  Paper, TextField, Dialog, DialogTitle, DialogContent, DialogActions, Chip, 
  Autocomplete, List, ListItem, ListItemText, Switch } from '@mui/material';
import ExpandLess from '@mui/icons-material/ExpandLess';
import ExpandMore from '@mui/icons-material/ExpandMore';
import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';
import UndoIcon from '@mui/icons-material/Undo';
import api from '../../services/api';


// --- Internal AddSecurityDialog Component ---
const AddSecurityDialog = ({ open, onClose, onAddRoles, accessLevel }) => {
  const [idpList, setIdpList] = useState([]);
  const [rolesByIdp, setRolesByIdp] = useState({});
  const [selectedIdp, setSelectedIdp] = useState('');
  const [roleOptions, setRoleOptions] = useState([]);
  const [selectedRoles, setSelectedRoles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (open) {
      fetchIdpRoles();
      setSelectedIdp('');
      setSelectedRoles([]);
      setError('');
    }
  }, [open, accessLevel]);

  const fetchIdpRoles = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get(`/role/lookup?accessLevel=${accessLevel}&pageNumber=0&pageSize=10`);
      
      if (res.data.success) {
        const data = res.data.responseData || {};
        setRolesByIdp(data);
        setIdpList(Object.keys(data));
      } else {
        setError('Failed to load IDPs and roles from API.');
      }
    } catch (err) {
      console.error('Error fetching IDP roles:', err);
      setError('An error occurred while fetching IDP data.');
    } finally {
      setLoading(false);
    }
  };

  const handleIdpChange = (e) => {
    const idp = e.target.value;
    setSelectedIdp(idp);
    setSelectedRoles([]);

    const idpRolesArray = rolesByIdp[idp] || [];
    const options = [];

    idpRolesArray.forEach(roleObject => {
      Object.entries(roleObject).forEach(([id, name]) => {
        options.push({
          roleId: id,
          roleName: name,
        });
      });
    });

    setRoleOptions(options);
  };

  const handleRolesChange = (event, values) => {
    setSelectedRoles(values);
  };

  const handleSubmit = () => {
    if (!selectedIdp || selectedRoles.length === 0) {
      setError('Please select an IDP and at least one Role.');
      return;
    }
    setError('');
    
    onAddRoles({
      idp: selectedIdp,
      roles: selectedRoles,
    });
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>Add API Security Roles</DialogTitle>
      <DialogContent dividers>
        {error && (
            <Box sx={{ color: 'error.main', mb: 2, p: 1, border: '1px solid', borderColor: 'error.main', borderRadius: 1 }}>
                <Typography variant="body2">{error}</Typography>
            </Box>
        )}
        <FormControl fullWidth margin="normal" disabled={loading}>
          <InputLabel>Identity Provider (IDP)</InputLabel>
          <Select value={selectedIdp} onChange={handleIdpChange} label="Identity Provider (IDP)">
            {loading ? (
                <MenuItem disabled>Loading IDPs...</MenuItem>
            ) : idpList.length === 0 ? (
                <MenuItem disabled>No IDPs available</MenuItem>
            ) : (
                idpList.map(idp => (
                    <MenuItem key={idp} value={idp}>{idp}</MenuItem>
                ))
            )}
          </Select>
        </FormControl>
        {selectedIdp && (
          <Autocomplete multiple disableCloseOnSelect options={roleOptions} getOptionLabel={opt => `${opt.roleName} (${opt.roleId})`} value={selectedRoles}onChange={handleRolesChange}
            renderInput={params => (
              <TextField {...params} label="Roles" placeholder="Select roles" margin="normal" />
            )} sx={{ mt: 2 }}
          />
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="secondary">Cancel</Button>
        <Button onClick={handleSubmit} variant="contained" color="primary" disabled={loading || !selectedIdp || selectedRoles.length === 0}>
          Add Roles
        </Button>
      </DialogActions>
    </Dialog>
  );
};
// --- End of Internal AddSecurityDialog Component ---


const ActionDetailsPage = () => {
  const location = useLocation();
  const { action, appId, appConfig } = location.state || {};
  
  const featureId = action.featureId?.toLowerCase() || '';
  const actionId = action.actionId?.toLowerCase() || '';
  const CONFIG_PREFIX = `photon.${appId?.toLowerCase()}.alert.event.${featureId}.${actionId}.`;

  const [initialSecurityLevel, setInitialSecurityLevel] = useState(action?.securityLevel || 'AUTHENTICATED');
  const [securityLevel, setSecurityLevel] = useState(action?.securityLevel || 'AUTHENTICATED');
  const [userRoles, setUserRoles] = useState(action?.userRoles || {});
  const [initialUserRoles, setInitialUserRoles] = useState(action?.userRoles || {});
  
  // ----------------------------------------------------------------------
  // NEW Event Config State (List-based, replacing old single-ID state)
  // ----------------------------------------------------------------------
  // State: Tracks the map of currently configured events (eventKey: "true" | "false")
  const [initialEvents, setInitialEvents] = useState({});
  const [currentEvents, setCurrentEvents] = useState({});
  
  // State: UI flags
  const [isConfigSettingActive, setIsConfigSettingActive] = useState(false); // Controlled by useEffect
  const [newEventKey, setNewEventKey] = useState('');
  
  // Derived state for the new list-based config
  const hasExistingEvents = Object.keys(initialEvents).length > 0;
  
  // UI and Dialog State (Keep as is)
  const [openDialog, setOpenDialog] = useState(false);
  const [authTypes, setAuthTypes] = useState([]); 
  const [openRoles, setOpenRoles] = useState({}); 

  // Derived State for Modification Tracking
  const isSecurityLevelModified = securityLevel !== initialSecurityLevel;
  const isUserRolesModified = JSON.stringify(userRoles) !== JSON.stringify(initialUserRoles);
  
  // Check if any event in currentEvents is different from initialEvents
  const isEventConfigModified = JSON.stringify(initialEvents) !== JSON.stringify(currentEvents);
  
  const isTotalModified = isSecurityLevelModified || isUserRolesModified || isEventConfigModified;


  // Initialization and Data Fetching
  useEffect(() => {
    if (action) {
        setInitialSecurityLevel(action.securityLevel);
        setSecurityLevel(action.securityLevel);
        setUserRoles(action.userRoles || {});
        setInitialUserRoles(action.userRoles || {});
    }
    
    // --- NEW Event Config Parsing Logic ---
    const eventsMap = {};
    if (appConfig) {
        Object.keys(appConfig).forEach(fullKey => {
            if (fullKey.startsWith(CONFIG_PREFIX)) {
                const eventKey = fullKey.substring(CONFIG_PREFIX.length);
                eventsMap[eventKey] = appConfig[fullKey]; // "true" or "false"
            }
        });
    }
    setInitialEvents(eventsMap);
    setCurrentEvents(eventsMap);
    
    // Initial state: Active for editing if events exist, otherwise disabled
    setIsConfigSettingActive(Object.keys(eventsMap).length > 0);
    // --- END NEW Event Config Parsing Logic ---

    fetchAuthTypes();
    fetchUserRoles(); 
  }, [action, appConfig, CONFIG_PREFIX]);
  

  const fetchAuthTypes = async () => {
    try {
      const response = await api.get('/identity-meta'); 
      if (response.data.success) {
        let authTypesResponse = response.data.responseData.authTypes
          .filter(o => o.isActive)
          .map(a => a.id);
        setAuthTypes(authTypesResponse);
      }
    } catch (error) {
      console.error('Error fetching auth types:', error);
    }
  };

  const fetchUserRoles = async () => {
    if (!action?.id) return;
    try {
      const response = await api.get(`/api-manager/services/action/${action.id}`);
      if (response.data.success) {
        const apiAction = response.data.responseData[0]?.features[0]?.actions[0];
        if (apiAction) {
             const fetchedRoles = apiAction.userRoles || {};
             setUserRoles(fetchedRoles);
             setInitialUserRoles(fetchedRoles);
        }
      }
    } catch (error) {
      console.error('Error fetching user roles:', error);
    }
  };

  const handleSecurityLevelChange = (event) => {
    setSecurityLevel(event.target.value);
  };

  const handleAddSecurityClick = () => setOpenDialog(true);
  const handleCloseDialog = () => setOpenDialog(false);

  const handleAddRoles = ({ idp, roles }) => {
    setUserRoles(prev => {
      const updated = { ...prev };
      if (!updated[idp]) updated[idp] = {};
      roles.forEach(r => {
        updated[idp][r.roleId] = r.roleName;
      });
      return updated;
    });
    setOpenDialog(false);
  };

  const handleDeleteRole = (idp, roleId) => {
    setUserRoles(prev => {
      const updated = { ...prev };
      if (updated[idp]) {
        const { [roleId]: deleted, ...rest } = updated[idp];
        if (Object.keys(rest).length > 0) {
          updated[idp] = rest;
        } else {
          delete updated[idp];
        }
      }
      return updated;
    });
  }; 
  
  const handleToggleExpand = (idp) => {
    setOpenRoles(prev => ({ ...prev, [idp]: !prev[idp] }));
  };
  
  const handleDeleteIdp = (idp) => {
    setUserRoles(prev => {
      const updated = { ...prev };
      delete updated[idp];
      return updated;
    });

    setOpenRoles(prev => {
      const newOpen = { ...prev };
      delete newOpen[idp];
      return newOpen;
    });
  };
  
  // ----------------------------------------------------------------------
  // NEW Handlers for Event Config (List-based)
  // ----------------------------------------------------------------------

  const handleEnableConfig = () => {
    setIsConfigSettingActive(true);
  };

  const handleCancelConfig = () => {
    // Revert current state back to initial saved state
    setCurrentEvents(initialEvents);
    setNewEventKey('');
    // If there are no saved events, revert to the disabled state
    if (!hasExistingEvents) {
         setIsConfigSettingActive(false);
    }
  };

  const handleToggleEvent = (eventKey, isChecked) => {
    setCurrentEvents(prev => ({
        ...prev,
        [eventKey]: isChecked ? "true" : "false" // Preserve the string type
    }));
  };

  const handleAddEvent = () => {
    const key = newEventKey.trim();
    if (key && !currentEvents.hasOwnProperty(key)) {
        setCurrentEvents(prev => ({
            ...prev,
            [key]: "true" // Default to true when adding
        }));
        setNewEventKey('');
    }
  };

  const handleDeleteEvent = (eventKey) => {
    setCurrentEvents(prev => {
        const { [eventKey]: removed, ...rest } = prev;
        return rest;
    });
  };

  // --- Action Handlers (Updated for new state) ---
  const handleUndo = () => {
    setUserRoles({ ...initialUserRoles });
    setSecurityLevel(initialSecurityLevel);
    
    // NEW: Reset event config state
    setCurrentEvents({ ...initialEvents });
    setNewEventKey('');
    setIsConfigSettingActive(Object.keys(initialEvents).length > 0);
  };
  
  const handleSave = async () => {
    const payload = {};
    if (isSecurityLevelModified) {
      payload.securityLevel = securityLevel;
    }
    if (isUserRolesModified) {
      payload.userRoles = userRoles;
    }
    
    // NEW: Construct the event config payload for saving
    if (isEventConfigModified) {
        const config = {};
        const eventConfigPayload = {};
        Object.keys(currentEvents).forEach(eventKey => {
            const fullKey = CONFIG_PREFIX + eventKey;
            eventConfigPayload[fullKey] = currentEvents[eventKey];
        });
        config.config=eventConfigPayload;
        saveOrUpdateEventConfig(config)
    }
  
    if (Object.keys(payload).length === 0) {
        console.log("No changes to save.");
        return;
    }

    try {
      await api.patch(`/api-manager/services/action/${action.id}`, payload);

      console.log('Changes saved successfully!'); 

      setInitialUserRoles({ ...userRoles });
      setInitialSecurityLevel(securityLevel);
      setInitialEvents({ ...currentEvents });

    } catch (error) {
      console.error('Error saving changes:', error);
      console.log('Failed to save changes.'); 
    }
  };

  const saveOrUpdateEventConfig = async (params) => {
    try {
      const configId = appId+"-API-CONFIG"
      await api.patch(`/config-properties/${configId}`, params);

    } catch (error) {
      console.error('Error saving changes:', error);
      console.log('Failed to save changes.'); 
    }
  };
  
  // ----------------------------------------------------------------------
  // NEW renderEventConfig (List-based)
  // ----------------------------------------------------------------------
  const renderEventConfig = () => {
        
    // CASE 1: Config is saved (hasExistingEvents) but not active for editing
    if (hasExistingEvents && !isConfigSettingActive) {
        return (
            <Box sx={{ mt: 2 }}>
                <Box sx={{ p: 2, borderLeft: '4px solid #1976d2', backgroundColor: '#e3f2fd', borderRadius: 1, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                        {Object.keys(initialEvents).length} Event(s) Configured
                    </Typography>
                    <Button variant="text" onClick={handleEnableConfig} size="small">Edit</Button>
                </Box>
                <List disablePadding sx={{ mt: 1, border: '1px solid #ccc', borderRadius: 1, maxHeight: 150, overflowY: 'auto' }}>
                   {Object.entries(initialEvents).map(([key, value]) => (
                        <ListItem key={key} dense sx={{ borderBottom: '1px solid #eee' }}>
                            <ListItemText primary={key} />
                            <Chip label={value === "true" ? "Active" : "Inactive"} color={value === "true" ? "success" : "warning"} size="small" />
                        </ListItem>
                    ))}
                </List>
            </Box>
        );
    }

    // CASE 2: Config is not active and no events are saved (Needs to be enabled)
    if (!isConfigSettingActive) {
      return (
          <Box sx={{ mt: 2 }}>
              <Button variant="contained" onClick={handleEnableConfig} startIcon={<AddIcon />} size="small" color="secondary">
                  Enable Event Config
              </Button>
          </Box>
      );
    } 
    
    // CASE 3: Config is active for editing (isConfigSettingActive is true)
    return (
        <Box sx={{ mt: 2, background: '#fff', borderRadius: 1, border: '1px solid #ddd', overflow: 'hidden' }}>
            <Typography variant="subtitle2" sx={{ p: 2, pb: 1, fontWeight: 700, borderBottom: '1px solid #eee' }}>
                Configure Event Keys
            </Typography>
            
            {/* Add New Event Input */}
            <Box sx={{ display: 'flex', gap: 1, p: 2, pt: 1, alignItems: 'center', borderBottom: '1px solid #eee' }}>
                <TextField label="New Event Key" placeholder="e.g., failed_validation" value={newEventKey} onChange={(e) => setNewEventKey(e.target.value)} variant="outlined" size="small" sx={{ flexGrow: 1 }} error={!!currentEvents[newEventKey.trim()]} helperText={currentEvents[newEventKey.trim()] ? 'Event key already exists.' : ''}/>
                <Button variant="contained" onClick={handleAddEvent} disabled={!newEventKey.trim() || !!currentEvents[newEventKey.trim()]} startIcon={<AddIcon />} size="small">
                    Add
                </Button>
            </Box>

            {/* List of Current Events */}
            <List dense disablePadding sx={{ maxHeight: 250, overflowY: 'auto' }}>
                {Object.entries(currentEvents).map(([eventKey, eventStatus]) => (
                    <ListItem 
                        key={eventKey} 
                        secondaryAction={
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                <Switch edge="end" checked={eventStatus === "true"} onChange={(e) => handleToggleEvent(eventKey, e.target.checked)} inputProps={{ 'aria-labelledby': `switch-list-label-${eventKey}` }} color="primary"/>
                                <IconButton edge="end" aria-label="delete" onClick={() => handleDeleteEvent(eventKey)}>
                                    <CloseIcon fontSize="small" />
                                </IconButton>
                            </Box>
                        }
                    >
                        <ListItemText id={`switch-list-label-${eventKey}`} primary={eventKey} sx={{ fontFamily: 'monospace' }}/>
                    </ListItem>
                ))}
                {Object.keys(currentEvents).length === 0 && (
                    <ListItem>
                        <ListItemText primary="No events configured for this Action." sx={{ color: 'text.secondary', fontStyle: 'italic' }}/>
                    </ListItem>
                )}
            </List>

            {/* Action Buttons */}
            <Box sx={{ p: 2, pt: 1, display: 'flex', justifyContent: 'flex-end', gap: 1, borderTop: '1px solid #eee' }}>
                <Button variant="outlined" onClick={handleCancelConfig} >
                    {hasExistingEvents ? 'Cancel Edit' : 'Disable Config'}
                </Button>
                <Button variant="contained" color="primary" onClick={handleSave} disabled={!isEventConfigModified}>
                    Save Changes
                </Button>
            </Box>
        </Box>
    );
  };

  const renderSecuritySection = () => {

    if (!action) {
      return <div>No action data found. Please navigate from the previous page.</div>
    }    
    
    // Simple authentication modes
    if (securityLevel === 'PUBLIC') {
      return (
        <Paper sx={{ p: 3, mt: 2, background: '#e8f5e9', borderLeft: '4px solid #4caf50' }}>
          <Typography variant="body1" color="text.secondary">
            This API is **Public** and requires no security credentials.
          </Typography>
        </Paper>
      );
    }
    if (securityLevel === 'ANONYMOUS') {
      return (
        <Paper sx={{ p: 3, mt: 2, background: '#fff3e0', borderLeft: '4px solid #ff9800' }}>
          <Typography variant="body1" color="text.secondary">
            This API is accessible using **Client Id** and **Client Secret** only (machine-to-machine access).
          </Typography>
        </Paper>
      );
    }
    if (securityLevel === 'PRIVATE') {
      return (
        <Paper sx={{ p: 3, mt: 2, background: '#fce4ec', borderLeft: '4px solid #e91e63' }}>
          <Typography variant="body1" color="text.secondary">
            This API is **Private** and can only be accessed from within the **Microservice Network**.
          </Typography>
        </Paper>
      );
    }

    // AUTHENTICATED mode (Role-based security)
    return (
      <Box sx={{ mt: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6">Required User Roles</Typography>
          <Button variant="contained" color="primary" onClick={handleAddSecurityClick} startIcon={<AddIcon />}>
            Add Security
          </Button>
        </Box>

        <Box sx={{ width: '100%' }}>
          {Object.entries(userRoles).map(([idp, rolesMap]) => (
            <Paper key={idp} elevation={2} sx={{ mb: 2, borderRadius: 2, overflow: 'hidden' }}>
              
              {/* Header for IDP */}
              <Box sx={{ px: 2, py: 1, display: 'flex', alignItems: 'center', background: "#f0f4f8", cursor: 'pointer' }} onClick={() => handleToggleExpand(idp)}>
                <IconButton size="small">
                  {openRoles[idp] ? <ExpandLess /> : <ExpandMore />}
                </IconButton>
                <Typography variant="subtitle1" sx={{ flex: 1, fontWeight: 700, ml: 1 }}>
                  {idp}
                </Typography>
                <IconButton onClick={(e) => { e.stopPropagation(); handleDeleteIdp(idp); }} size="small" color="error" title="Remove Identity Provider and all associated roles">
                  <CloseIcon />
                </IconButton>
              </Box>
              
              <Collapse in={openRoles[idp]} timeout="auto" unmountOnExit>
                <TableContainer>
                  <Table size="small">
                    <TableBody>
                      {Object.entries(rolesMap).map(([roleId, roleName]) => (
                        <TableRow key={roleId} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                          <TableCell sx={{ fontWeight: 600, width: '40%' }}>{roleId}</TableCell>
                          <TableCell>{roleName}</TableCell>
                          <TableCell align="right" sx={{ width: '10%' }}>
                            <IconButton color="secondary" onClick={() => handleDeleteRole(idp, roleId)} sx={{ color: 'grey', '&:hover': { color: 'black' } }} size="small" title="Delete this role">
                              <CloseIcon fontSize="small" />
                            </IconButton>
                          </TableCell>
                        </TableRow>
                      ))}
                      
                      {Object.keys(rolesMap).length === 0 && (
                        <TableRow>
                          <TableCell colSpan={3} align="center" sx={{ color: 'grey.500', fontStyle: 'italic' }}>
                            No specific roles assigned for this IDP. Any authenticated user from {idp} can access.
                          </TableCell>
                        </TableRow>
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Collapse>
            </Paper>
          ))}
          
          {/* If no IDPs are defined */}
          {Object.keys(userRoles).length === 0 && (
            <Paper sx={{ p: 4, textAlign: 'center', color: 'grey.600', mt: 3 }}>
              No specific user roles defined. All authenticated users can access this API.
            </Paper>
          )}
        </Box>
      </Box>
    );
  };

  if (!action) {
    return (
      <Box sx={{ p: 4, textAlign: 'center', color: 'error.main' }}>
        <Typography variant="h6">Error: Action details are missing.</Typography>
        <Typography variant="body1">Please navigate to this page via the API Manager table.</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ width: '80vw', minHeight: '100vh', padding: '24px', backgroundColor: '#f5f7fa', pb: '100px', position: 'relative' }}>
      
      {/* Action Header */}
      <Paper elevation={1} sx={{ p: 3, mb: 3, background: 'white' }}>
        <Typography variant="h5" gutterBottom sx={{ fontWeight: 600 }}>{action.name}</Typography>
        <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 1, color: 'text.secondary' }}>
            <Typography variant="subtitle1">Method: <Box component="span" sx={{ fontWeight: 600, color: 'primary.main' }}>{action.requestMethod}</Box></Typography>
            <Typography variant="subtitle1">Action ID: {action.actionId}</Typography>
            <Typography variant="subtitle1">Access Level: {action.accessLevel}</Typography>
            <Typography variant="subtitle1">Path: <Box component="span" sx={{ fontFamily: 'monospace' }}>{action.path}</Box></Typography>
            <Typography variant="body2" sx={{ gridColumn: 'span 2', mt: 1 }}>Description: {action.description}</Typography>
        </Box>
      </Paper>

      {/* HORIZONTAL LAYOUT for Security and Config */}
      <Box sx={{ display: 'flex', gap: 4, mb: 3 }}>
          {/* Security Level Control (Left) */}
          <Paper elevation={1} sx={{ p: 3, background: 'white', flex: 1 }}>
              <Typography variant="h6" gutterBottom>Security Level</Typography>
              <FormControl fullWidth>
                  <InputLabel id="security-level-label">Security Level</InputLabel>
                  <Select labelId="security-level-label" value={securityLevel} onChange={handleSecurityLevelChange} label="Security Level">
                      <MenuItem value="PUBLIC">PUBLIC (No authentication required)</MenuItem>
                      <MenuItem value="PRIVATE">PRIVATE (Microservice only)</MenuItem>
                      <MenuItem value="AUTHENTICATED">AUTHENTICATED (User/Role based)</MenuItem>
                      <MenuItem value="ANONYMOUS">ANONYMOUS (Client Id/Secret based)</MenuItem>
                  </Select>
              </FormControl>
          </Paper>
          
          {/* Event Config Section (Right) */}
          <Paper elevation={1} sx={{ p: 3, background: 'white', flex: 1 }}>
              <Typography variant="h6" gutterBottom>Event Configuration</Typography>
              {renderEventConfig()}
          </Paper>
      </Box>


      {/* User Roles / Security Details Section */}
      {renderSecuritySection()}
      <Box  sx={{ display: 'flex',  justifyContent: 'flex-end', position: 'fixed', bottom: 0, left: 0, right: 0, width: '100%', backgroundColor: 'white', padding: '16px 24px', boxShadow: '0 -4px 12px rgba(0, 0, 0, 0.1)', zIndex: 1000 }}>
        <Button variant="outlined" color="secondary" startIcon={<UndoIcon />} onClick={handleUndo} sx={{ mr: 2 }} disabled={!isTotalModified}>
          Undo Changes
        </Button>
        <Button variant="contained" color="primary" disabled={!isTotalModified} onClick={handleSave}>
          Save All Changes
        </Button>
      </Box>

      {/* The Dialog is defined internally */}
      <AddSecurityDialog open={openDialog} onClose={handleCloseDialog} onAddRoles={handleAddRoles} accessLevel={action.accessLevel}/>
    </Box>
  );
};

export default ActionDetailsPage;