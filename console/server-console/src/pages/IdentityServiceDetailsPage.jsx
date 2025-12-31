import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Box, Typography, Paper, TextField, Button, Switch, FormControlLabel, MenuItem, Divider, Grid, Select, InputLabel, FormControl, IconButton, Tooltip, CircularProgress, Dialog, DialogTitle, DialogContent, DialogActions, Snackbar, Alert } from '@mui/material';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import DeleteIcon from '@mui/icons-material/Delete';
import UndoIcon from '@mui/icons-material/Undo';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import api from '../services/api';

const IdentityServiceDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [identityService, setIdentityService] = useState(null);
  const [metaData, setMetaData] = useState({ firstFactors: [], secondFactors: [], certificates: [], mfaConditions: [], jwtSignatureAlgorithms: [] });
  const [originalData, setOriginalData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isChanged, setIsChanged] = useState(false);

  // New states for delete confirmation and snackbar
  const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [snack, setSnack] = useState({ open: false, severity: 'success', message: '' });

  useEffect(() => {
    fetchIdentityService();
    fetchMetaData();
  }, [id]);

  const fetchIdentityService = async () => {
    try {
      const response = await api.get(`/identity-provider/${id}`);
      if (response.data.success) {
        const data = response.data.responseData;

        const formattedData = {
          ...data,
          certificateId: data.certificate?.id || '',
          idpType: data.identityProviderType,
          identityAuthTypes: data.identityProviderAuthTypes.map((auth) => ({
            id: auth.id,
            name: auth.name,
            firstFactor: auth.firstFactor.id,
            secondFactors: auth.secondFactors.map((sf) => sf.id),
            active: auth.active,
          })),
          mfaConditions: data.mfaCondition ? [data.mfaCondition.id] : [],
        };

        setIdentityService(formattedData);
        setOriginalData(formattedData);
      }
    } catch (error) {
      console.error('Error fetching identity service:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchMetaData = async () => {
    try {
      const [authResponse, certResponse, mfaResponse, metaRes] = await Promise.all([
        api.get('/auth-type?pageNumber=0&pageSize=10'),
        api.get('/certs?pageNumber=0&pageSize=10'),
        api.get('/mfa-condition?pageNumber=0&pageSize=10'),
        api.get('/identity-meta')
      ]);

      if (authResponse.data.success && certResponse.data.success && mfaResponse.data.success && metaRes.data.success) {
        setMetaData({
          firstFactors: authResponse.data.responseData,
          secondFactors: authResponse.data.responseData,
          certificates: certResponse.data.responseData,
          mfaConditions: mfaResponse.data.responseData || [],
          authAdaptor: metaRes.data.responseData.authAdaptor,
          idpTypes: metaRes.data.responseData.idpTypes,
          jwtSignatureAlgorithms: metaRes.data.responseData.jwtSignatureAlgorithms
        });
      }
    } catch (error) {
      console.error('Error fetching metadata:', error);
    }
  };

  const isOnboarding = identityService?.idpType === 'ON_BOARDING';

  const filteredFirstFactors = isOnboarding? metaData.firstFactors.filter((t) => ['SMS_OTP', 'EML_OTP'].includes(metaData.authAdaptor[t.authAdapter])): metaData.firstFactors;


  const handleChange = (field, value) => {
    setIdentityService((prev) => ({ ...prev, [field]: value }));
    setIsChanged(true);
  };

  const handleToggle = (field) => {
    setIdentityService((prev) => ({ ...prev, [field]: !prev[field] }));
    setIsChanged(true);
  };

  const handleAuthTypeChange = (index, field, value) => {
    const updatedAuthTypes = [...identityService.identityAuthTypes];
    updatedAuthTypes[index][field] = value;

    if (field === 'firstFactor') {
      updatedAuthTypes[index].secondFactors = [];
    }

    setIdentityService((prev) => ({ ...prev, identityAuthTypes: updatedAuthTypes }));
    setIsChanged(true);
  };

  const handleAddAuthType = () => {
    setIdentityService((prev) => ({
      ...prev,
      identityAuthTypes: [...prev.identityAuthTypes, { name: '', firstFactor: '', secondFactors: [], active: true }],
    }));
    setIsChanged(true);
  };

  const handleRemoveAuthType = (index) => {
    const updatedAuthTypes = [...identityService.identityAuthTypes];
    updatedAuthTypes.splice(index, 1);
    setIdentityService((prev) => ({ ...prev, identityAuthTypes: updatedAuthTypes }));
    setIsChanged(true);
  };

  const handleUpdate = async () => {
    const payload = {
      ...identityService,
      certificate: identityService.certificateId || null,
      identityProviderAuthTypes: identityService.identityAuthTypes.map((auth) => ({
        id: auth.id,
        name: auth.name,
        firstFactor: auth.firstFactor,
        secondFactors: auth.secondFactors,
        active: auth.active,
      })),
      mfaCondition: identityService.mfaConditions.length > 0 ? identityService.mfaConditions[0] : null,
    };

    try {
      await api.patch(`/identity-provider/${id}`, payload);
      alert('Identity Service Updated Successfully!');
      setIsChanged(false);
    } catch (error) {
      console.error('Error updating identity service:', error);
      alert('Failed to update identity service');
    }
  };

  const handleUndo = () => {
    setIdentityService(originalData);
    setIsChanged(false);
  };

  // --- New: delete flow and back navigation ---
  const handleBack = () => {
    navigate(-1);
  };

  const openConfirmDelete = () => setConfirmDeleteOpen(true);
  const closeConfirmDelete = () => setConfirmDeleteOpen(false);

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await api.delete(`/identity-provider/${id}`);
      // clear any local state if needed and navigate back to list
      setSnack({ open: true, severity: 'success', message: 'Identity service deleted successfully' });
      setConfirmDeleteOpen(false);
      navigate('/identity-service');
    } catch (err) {
      console.error('Failed to delete identity service', err);
      setSnack({ open: true, severity: 'error', message: 'Failed to delete identity service' });
    } finally {
      setDeleting(false);
    }
  };
  // --- end new ---

  if (loading) return <CircularProgress sx={{ display: 'block', margin: 'auto', mt: 5 }} />;

  return (
    <Box sx={{ width: '80vw', minHeight: '80vh', padding: '20px', backgroundColor: '#f0f4f8' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Button variant="outlined" onClick={handleBack}>Back</Button>

        <Typography variant="h4" sx={{ color: '#1e3a8a' }}>
          Edit Identity Service
        </Typography>

        <Box>
          <Button variant="outlined" color="error" startIcon={<DeleteIcon />} onClick={openConfirmDelete} sx={{ mr: 1 }}>
            Delete
          </Button>
          <Button startIcon={<UndoIcon />} onClick={handleUndo} disabled={!isChanged} sx={{ mr: 1 }}>
            Undo
          </Button>
          <Button variant="contained" color="primary" onClick={handleUpdate} disabled={!isChanged}>
            Update
          </Button>
        </Box>
      </Box>

      <Paper sx={{ padding: '20px', borderRadius: '12px', boxShadow: 4, backgroundColor: '#ffffff' }}>
        {/* Basic Information */}
        <TextField label="ID" value={identityService.id} fullWidth margin="normal" disabled />
        <TextField label="Name" value={identityService.name} onChange={(e) => handleChange('name', e.target.value)} fullWidth margin="normal" />
        <TextField label="Description" value={identityService.description} onChange={(e) => handleChange('description', e.target.value)} fullWidth margin="normal" />
        
        <FormControl fullWidth margin="normal">
          <InputLabel>IDP Type</InputLabel>
            <Select value={identityService.idpType} disabled={true} onChange={(e) => handleChange('idpType', e.target.value)}>
              {metaData.idpTypes.map((type) => (
                <MenuItem key={type} value={type}>{type}</MenuItem>
              ))}
            </Select>
        </FormControl>

        <FormControlLabel control={<Switch checked={identityService.isActive} onChange={() => handleToggle('isActive')} />} label="Is Active" />
        <FormControlLabel control={<Switch checked={identityService.isMfaRequiredForEveryLogin} disabled={isOnboarding} onChange={() => handleToggle('isMfaRequiredForEveryLogin')} />} label="MFA For Every Login" />
        <FormControlLabel control={<Switch checked={identityService.isMfaEnabled} disabled={isOnboarding} onChange={() => handleToggle('isMfaEnabled')} />} label="Enable MFA" />

        <TextField
          label="MFA Condition Expiry (Minutes)"
          type="number"
          value={identityService.lastValidateMfaExpiresInMinutes}
          disabled={isOnboarding}
          onChange={(e) => handleChange('lastValidateMfaExpiresInMinutes', e.target.value)}
          fullWidth
          margin="normal"
          InputProps={{
            endAdornment: (
              <Tooltip title="Define when MFA should trigger after the last successful validation">
                <HelpOutlineIcon sx={{ ml: 1 }} />
              </Tooltip>
            ),
          }}
        />
        <Divider sx={{ my: 3 }} />

        {/* Token Selection */}
        <Typography variant="h6" sx={{ mt: 3 }}>JWT Configuration</Typography>
        <FormControl fullWidth margin="normal">
          <InputLabel>Certificate</InputLabel>
          <Select value={identityService.certificateId} onChange={(e) => handleChange('certificateId', e.target.value)}>
            {metaData.certificates.map((cert) => (
              <MenuItem key={cert.id} value={cert.id}>
                {cert.certificateName}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <InputLabel>Signature Algorith</InputLabel>
          <Select value={identityService.signatureAlgorithm} onChange={(e) => handleChange('signatureAlgorithm', e.target.value)}>
            {metaData.jwtSignatureAlgorithms.map((alg) => (
              <MenuItem key={alg.id} value={alg.id}>
                {alg.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControlLabel control={<Switch checked={identityService.includeX509InJwt} onChange={() => handleToggle('includeX509InJwt')} />} label="Include X509 In Jwt"/>
        <Divider sx={{ my: 3 }} />

        {/* Session Section */}
        <Typography variant="h6" sx={{ mt: 3 }}>Session Settings</Typography>
          <TextField
            label="Session Timeout (Minutes)"
            type="number"
            value={identityService.sessionTimeoutMinutes}
            onChange={(e) => handleChange('sessionTimeoutMinutes', e.target.value)}
            fullWidth
            margin="normal"
          />

          <TextField
            label="Active Sessions"
            type="number"
            value={identityService.activeSessions}
            disabled={isOnboarding}
            onChange={(e) => handleChange('activeSessions', e.target.value)}
            fullWidth
            margin="normal"
            InputProps={{
              endAdornment: (
                <Tooltip title="This will define the number of active logged-in sessions">
                  <HelpOutlineIcon sx={{ ml: 1 }} />
                </Tooltip>
              ),
            }}
          />

          <TextField
            label="Refresh Token Session Timeout (Minutes)"
            type="number"
            value={identityService.refreshTokenSessionTimeoutMinutes}
            disabled={isOnboarding}
            onChange={(e) => handleChange('refreshTokenSessionTimeoutMinutes', e.target.value)}
            fullWidth
            margin="normal"
          />

        {/* MFA Condition Selection */}
        <Typography variant="h6">MFA Condition</Typography>
        <FormControlLabel control={<Switch checked={identityService.isMfaConditionCheckEnabled} disabled={isOnboarding} onChange={() => handleToggle('isMfaConditionCheckEnabled')} />} label="Is MFA Condition Enabled" />
        
        {identityService.isMfaConditionCheckEnabled && (
        <FormControl fullWidth margin="normal">
          <InputLabel>Select MFA Condition</InputLabel>
          <Select value={identityService.mfaConditions[0] || ''} onChange={(e) => handleChange('mfaConditions', [e.target.value])}>
            {metaData.mfaConditions.map((condition) => (
              <MenuItem key={condition.id} value={condition.id}>
                {condition.name} - {condition.description}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        )}

        <Divider sx={{ my: 3 }} />

      {/* Identity Auth Types */}
      <Typography variant="h6">Identity Auth Types</Typography>
        {identityService.identityAuthTypes.map((authType, index) => (
          <Grid container spacing={2} key={index} sx={{ mb: 2 }}>
            <Grid item xs={4}>
              <TextField label="Name" value={authType.name} onChange={(e) => handleAuthTypeChange(index, 'name', e.target.value)} fullWidth />
            </Grid>

            <Grid item xs={4}>
              <FormControl fullWidth>
                <InputLabel>First Factor</InputLabel>
                <Select value={authType.firstFactor} onChange={(e) => handleAuthTypeChange(index, 'firstFactor', e.target.value)}>
                  {metaData.firstFactors.map((type) => (
                    <MenuItem key={type.id} value={type.id}>
                      {type.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {!isOnboarding && (
              <Grid item xs={3}>
                <FormControl fullWidth>
                  <InputLabel>Second Factors</InputLabel>
                  <Select
                    multiple
                    value={authType.secondFactors}
                    onChange={(e) => handleAuthTypeChange(index, 'secondFactors', e.target.value)}
                  >
                    {metaData.secondFactors.map((type) => (
                      <MenuItem key={type.id} value={type.id}>
                        {type.name}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
            )}

            <Grid item xs={1}>
              <IconButton color="error" onClick={() => handleRemoveAuthType(index)}>
                <DeleteIcon />
              </IconButton>
            </Grid>
          </Grid>
        ))}

        {/* Add Auth Type Button */}
        <Button startIcon={<AddCircleIcon />} onClick={handleAddAuthType} sx={{ mb: 3 }}>
          Add Auth Type
        </Button>

        <Divider sx={{ my: 3 }} />

        {/* Footer Buttons */}
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 4 }}>
          <Button startIcon={<UndoIcon />} onClick={handleUndo} disabled={!isChanged} sx={{ mr: 2 }}>
            Undo
          </Button>
          <Button variant="contained" color="primary" onClick={handleUpdate} disabled={!isChanged}>
            Update 
          </Button>
        </Box>
      </Paper>

      {/* Delete Confirmation Dialog */}
      <Dialog open={confirmDeleteOpen} onClose={closeConfirmDelete}>
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>
          <Typography>Are you sure you want to delete this identity service? This action cannot be undone.</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeConfirmDelete} disabled={deleting}>Cancel</Button>
          <Button onClick={handleDelete} color="error" variant="contained" disabled={deleting}>
            {deleting ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar */}
      <Snackbar open={snack.open} autoHideDuration={6000} onClose={() => setSnack(s => ({ ...s, open: false }))}>
        <Alert severity={snack.severity} onClose={() => setSnack(s => ({ ...s, open: false }))}>
          {snack.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default IdentityServiceDetailsPage;