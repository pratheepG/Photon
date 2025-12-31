import React, { useState, useEffect } from 'react';
import { Box,Typography,Paper,TextField,Button,Switch,FormControlLabel,
  MenuItem,Divider,Grid,Select,InputLabel,List,ListItem,ListItemText,
  FormControl,IconButton,Tooltip} from '@mui/material';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import DeleteIcon from '@mui/icons-material/Delete';
import UndoIcon from '@mui/icons-material/Undo';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import api from '../services/api';

const CreateIdentityServicePage = () => {
  const [metaData, setMetaData] = useState({
    firstFactors: [],
    secondFactors: [],
    mfaConditions: [],
    certificates: [],
    jwtSignatureAlgorithms: []
  });

  const initialFormData = {
    id: '',
    name: '',
    description: '',
    idpType: 'LOGIN',
    isActive: true,
    certificateId: '',
    sessionTimeoutMinutes: 30,
    activeSessions: 0,
    refreshTokenSessionTimeoutMinutes: 1440,
    identityAuthTypes: [],
    isMfaEnabled: false,
    isMfaConditionCheckEnabled: false,
    isMfaRequiredForEveryLogin: false,
    includeX509InJwt: false,
    lastValidateMfaExpiresInMinutes: 0,
    signatureAlgorithm: 'RS256',
    mfaConditions: [],
  };

  const [formData, setFormData] = useState(initialFormData);

  useEffect(() => {
    if (formData.idpType === 'ON_BOARDING') {
      setFormData((prev) => ({
        ...prev,
        isMfaEnabled: false,
        isMfaConditionCheckEnabled: false,
        isMfaRequiredForEveryLogin: false,
        lastValidateMfaExpiresInMinutes: 0,
        activeSessions: 1,
        refreshTokenSessionTimeoutMinutes: -1,
        identityAuthTypes: prev.identityAuthTypes.map(auth => ({
          ...auth,
          secondFactors: [],
        })),
      }));
    }
    fetchMetaData();
  }, [formData.idpType]);

  // Fetch Metadata from endpoints
  const fetchMetaData = async () => {
    try {
      const [authResponse, certResponse, metaResponse, mfaResponse] = await Promise.all([
        api.get('/auth-type?pageNumber=0&pageSize=10'),
        api.get('/certs?pageNumber=0&pageSize=10'),
        api.get('/identity-meta'),
        api.get('/mfa-condition?pageNumber=0&pageSize=10'),
      ]);

      if (authResponse.data.success && certResponse.data.success && metaResponse.data.success && mfaResponse.data.success) {
        const authTypes = authResponse.data.responseData;
        const certificates = certResponse.data.responseData;
        const authAdaptorMeta = metaResponse.data.responseData.authAdaptor;
        const jwtSignatureAlgorithms = metaResponse.data.responseData.jwtSignatureAlgorithms;
        const mfaConditions = mfaResponse.data.responseData || [];

        // Identify First and Second Factor Auth Types
        const firstFactors = authTypes.filter(
          (type) => authAdaptorMeta[type.authAdapter] === 'FIRST_FACTOR' || authAdaptorMeta[type.authAdapter] === 'BOTH'
        );
        const secondFactors = authTypes.filter(
          (type) => authAdaptorMeta[type.authAdapter] === 'SECOND_FACTOR' || authAdaptorMeta[type.authAdapter] === 'BOTH'
        );

        setMetaData({ firstFactors, secondFactors, mfaConditions, certificates, jwtSignatureAlgorithms });
      }
    } catch (error) {
      console.error('Error fetching metadata:', error);
    }
  };

  // Inside component function (not inside useEffect or fetchMetaData)
  const firstFactorOptions = formData.idpType === 'ON_BOARDING'? metaData.firstFactors.filter((t) => ['SMS_OTP', 'EML_OTP'].includes(t.authAdapter)): metaData.firstFactors;

  // Handle input changes
  const handleChange = (field, value) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  // Handle switch toggle
  const handleToggle = (field) => {
    setFormData((prev) => ({ ...prev, [field]: !prev[field] }));
  };

  // Handle Auth Type changes
  const handleAddAuthType = () => {
    setFormData((prev) => ({
      ...prev,
      identityAuthTypes: [...prev.identityAuthTypes, { name: '', firstFactor: '', secondFactors: [], active: true }],
    }));
  };

  const handleRemoveAuthType = (index) => {
    const updatedAuthTypes = [...formData.identityAuthTypes];
    updatedAuthTypes.splice(index, 1);
    setFormData((prev) => ({ ...prev, identityAuthTypes: updatedAuthTypes }));
  };

  const handleAuthTypeChange = (index, field, value) => {
    const updatedAuthTypes = [...formData.identityAuthTypes];
    updatedAuthTypes[index][field] = value;

    if (field === 'firstFactor') {
      updatedAuthTypes[index].secondFactors = [];
    }

    setFormData((prev) => ({ ...prev, identityAuthTypes: updatedAuthTypes }));
  };

  // Handle MFA Conditions
  const handleAddMfaCondition = (id) => {
    if (!formData.mfaConditions.includes(id)) {
      setFormData((prev) => ({
        ...prev,
        mfaConditions: [...prev.mfaConditions, id],
      }));
    }
  };

  const handleRemoveMfaCondition = (id) => {
    setFormData((prev) => ({
      ...prev,
      mfaConditions: prev.mfaConditions.filter((conditionId) => conditionId !== id),
    }));
  };

  const handleSubmit = async () => {
    const payload = {
      id: formData.id,
      name: formData.name,
      description: formData.description,
      identityProviderType: formData.idpType,
      isActive: formData.isActive,
      sessionTimeoutMinutes: formData.sessionTimeoutMinutes,
      activeSessions: parseInt(formData.activeSessions, 10),
      refreshTokenSessionTimeoutMinutes: parseInt(formData.refreshTokenSessionTimeoutMinutes, 1440),
      certificate: formData.certificateId || null,
      identityProviderAuthTypes: formData.identityAuthTypes.map((auth) => ({
        name: auth.name,
        firstFactor: auth.firstFactor, // Keep as string
        secondFactors: auth.secondFactors, // Keep as array of strings
        active: auth.active,
      })),
      includeX509InJwt: formData.includeX509InJwt,
      mfaCondition: formData.mfaConditions.length > 0 ? formData.mfaConditions[0] : null,
    };

    if (formData.idpType === 'ON_BOARDING') {
      payload.isMfaEnabled = false;
      payload.isMfaConditionCheckEnabled = false;
      payload.isMfaRequiredForEveryLogin = false;
      payload.lastValidateMfaExpiresInMinutes = null;
      payload.activeSessions = 1;
      payload.refreshTokenSessionTimeoutMinutes=-1;
    }
    
  
    console.log('Submitting payload:', JSON.stringify(payload, null, 2));
  
    try {
      await api.post('/identity-provider', payload);
      alert('Identity Service Created Successfully!');
    } catch (error) {
      console.error('Error creating identity service:', error);
      alert('Failed to create identity service');
    }
  };  
  

  const handleUndo = () => {
    setFormData(initialFormData);
  };

  return (
    <Box sx={{ width: '80vw', minHeight: '80vh', padding: '20px', backgroundColor: '#f0f4f8' }}>
      <Typography variant="h4" sx={{ color: '#1e3a8a', mb: 3 }}>
        Create Identity Service
      </Typography>

      <Paper sx={{ padding: '20px', borderRadius: '12px', boxShadow: 4, backgroundColor: '#ffffff' }}>
        {/* Basic Information */}
        <TextField label="ID" value={formData.id} onChange={(e) => handleChange('id', e.target.value)} fullWidth margin="normal" />
        <TextField label="Name" value={formData.name} onChange={(e) => handleChange('name', e.target.value)} fullWidth margin="normal" />
        <TextField label="Description" value={formData.description} onChange={(e) => handleChange('description', e.target.value)} fullWidth margin="normal" />

        <FormControl fullWidth margin="normal">
          <InputLabel>IDP Type</InputLabel>
            <Select value={formData.idpType} onChange={(e) => handleChange('idpType', e.target.value)}>
              <MenuItem value="LOGIN">LOGIN</MenuItem>
              <MenuItem value="ON_BOARDING">ON_BOARDING</MenuItem>
              <MenuItem value="GUEST_LOGIN">GUEST_LOGIN</MenuItem>
            </Select>
        </FormControl>


        <FormControlLabel control={<Switch checked={formData.isActive} onChange={() => handleToggle('isActive')} />} label="Is Active"/>
        <FormControlLabel control={<Switch checked={formData.isMfaEnabled} disabled={formData.idpType === 'ON_BOARDING'} onChange={() => handleToggle('isMfaEnabled')} />} label="Enable MFA"/>
        <FormControlLabel control={<Switch checked={formData.isMfaRequiredForEveryLogin} disabled={formData.idpType === 'ON_BOARDING'} onChange={() => handleToggle('isMfaRequiredForEveryLogin')} />} label="MFA For Every Login"/>
        
        <TextField label="MFA Condition Check Expiry" type="number" value={formData.lastValidateMfaExpiresInMinutes} disabled={formData.idpType === 'ON_BOARDING'} onChange={(e) => handleChange('lastValidateMfaExpiresInMinutes', e.target.value)} fullWidth margin="normal"
          InputProps={{
            endAdornment: (
              <Tooltip title="Define when should trigger MFA After the last successfull MFA validation">
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
          <Select value={formData.certificateId} onChange={(e) => handleChange('certificateId', e.target.value)}>
            {metaData.certificates.map((cert) => (
              <MenuItem key={cert.id} value={cert.id}>
                {cert.certificateName}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <InputLabel>Signature Algorith</InputLabel>
          <Select value={formData.signatureAlgorithm} onChange={(e) => handleChange('signatureAlgorithm', e.target.value)}>
            {metaData.jwtSignatureAlgorithms.map((alg) => (
              <MenuItem key={alg.id} value={alg.id}>
                {alg.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControlLabel control={<Switch checked={formData.includeX509InJwt} onChange={() => handleToggle('includeX509InJwt')} />} label="Include X509 In Jwt"/>
        <Divider sx={{ my: 3 }} />

        {/* Session Section */}
        <Typography variant="h6" sx={{ mt: 3 }}>Session Settings</Typography>
        <TextField label="Session Timeout (Minutes)" type="number" value={formData.sessionTimeoutMinutes} onChange={(e) => handleChange('sessionTimeoutMinutes', e.target.value)} fullWidth margin="normal" />
        <TextField label="Active Sessions" type="number" value={formData.activeSessions} disabled={formData.idpType === 'ON_BOARDING'} onChange={(e) => handleChange('activeSessions', e.target.value)} fullWidth margin="normal"
          InputProps={{
            endAdornment: (
              <Tooltip title="This will define the number of active logged-in sessions">
                <HelpOutlineIcon sx={{ ml: 1 }} />
              </Tooltip>
            ),
          }}
        />
        <TextField label="Refresh Token Session Timeout (Minutes)" type="number" value={formData.refreshTokenSessionTimeoutMinutes} disabled={formData.idpType !== 'LOGIN'} onChange={(e) => handleChange('refreshTokenSessionTimeoutMinutes', e.target.value)} fullWidth margin="normal"/>

        <Divider sx={{ my: 3 }} />
        {/* MFA Condition Section */}
        <Typography variant="h6">MFA Condition</Typography>
        <FormControlLabel
          control={<Switch checked={formData.isMfaConditionCheckEnabled} disabled={formData.idpType === 'ON_BOARDING'} onChange={() => handleToggle('isMfaConditionCheckEnabled')} />}
          label="Is MFA Condition Enabled"
        />

        {formData.isMfaConditionCheckEnabled && (
          <FormControl fullWidth margin="normal">
            <InputLabel>Select MFA Condition</InputLabel>
            <Select onChange={(e) => handleAddMfaCondition(e.target.value)}>
              {metaData.mfaConditions.map((condition) => (
                <MenuItem key={condition.id} value={condition.id}>
                  {condition.name} - {condition.description}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        )}

        <List>
          {formData.mfaConditions.map((id) => {
            const condition = metaData.mfaConditions.find((c) => c.id === id);
            return (
              <ListItem key={id}>
                <ListItemText primary={condition.name} secondary={condition.description} />
                <IconButton color="error" onClick={() => handleRemoveMfaCondition(id)}>
                  <DeleteIcon />
                </IconButton>
              </ListItem>
            );
          })}
        </List>

        <Divider sx={{ my: 3 }} />
        
        {/* Identity Auth Types */}
        <Typography variant="h6">Identity Auth Types</Typography>
        {formData.identityAuthTypes.map((authType, index) => (
          <Grid container spacing={2} key={index} sx={{ mb: 2 }}>
            <Grid item xs={4}>
              <TextField label="Name" value={authType.name} onChange={(e) => handleAuthTypeChange(index, 'name', e.target.value)} fullWidth />
            </Grid>

            <Grid item xs={4}>
              <FormControl fullWidth>
                <InputLabel>First Factor</InputLabel>
                <Select value={authType.firstFactor} onChange={(e) => handleAuthTypeChange(index, 'firstFactor', e.target.value)}>
                  {firstFactorOptions.map((type) => (
                    <MenuItem key={type.id} value={type.id}>
                      {type.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {formData.idpType !== 'ON_BOARDING' && (
              <Grid item xs={3}>
                <FormControl fullWidth>
                  <InputLabel>Second Factors</InputLabel>
                  <Select multiple value={authType.secondFactors} onChange={(e) => handleAuthTypeChange(index, 'secondFactors', e.target.value)}>
                    {metaData.secondFactors
                      .filter((type) => type.id !== authType.firstFactor)
                      .map((type) => (
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

        <Button startIcon={<AddCircleIcon />} onClick={handleAddAuthType}>
          Add Auth Type
        </Button>


        <Divider sx={{ my: 3 }} />
        
        {/* Footer Buttons */}
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 4 }}>
          <Button startIcon={<UndoIcon />} onClick={handleUndo} sx={{ mr: 2 }}>
            Undo
          </Button>
          <Button variant="contained" color="primary" onClick={handleSubmit}>
            Create 
          </Button>
        </Box>
      </Paper>
    </Box>
  );
};

export default CreateIdentityServicePage;