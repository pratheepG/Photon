import { useState, useEffect } from 'react';
import { Box, Button, TextField, MenuItem, FormControl, FormLabel, RadioGroup, FormControlLabel, Radio, Typography, Divider } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';

const CreateAuthTypePage = () => {
  const navigate = useNavigate();
  const [authAdaptorList, setAuthAdaptorList] = useState({});
  const [certificates, setCertificates] = useState([]);
  const [formData, setFormData] = useState({
    authAdapter: '',
    certificate: '',
    name: '',
    id: '',
    isActive: true,
    config: {}
  });

  useEffect(() => {
    fetchMetaData();
  }, []);

  const fetchMetaData = async () => {
    try {
      const meta = await api.get('/identity-meta');
      setAuthAdaptorList(meta.data.responseData?.authAdaptor || {});
      const certResp = await api.get('/certs?pageNumber=0&pageSize=100');
      setCertificates(certResp.data.responseData || []);
    } catch (err) {
      console.error('meta fetch error', err);
    }
  };

  const handleInputChange = (event) => {
    const { name, value } = event.target;

    if (name === 'name') {
      const regex = /^[A-Za-z\s]*$/;
      if (!regex.test(value)) return;
      setFormData((prev) => ({
        ...prev,
        [name]: value,
        id: value.toUpperCase().replace(/\s+/g, '_'),
      }));
      return;
    }

    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleIsActiveChange = (event) => {
    setFormData((prev) => ({ ...prev, isActive: event.target.value === 'true' }));
  };

  const handleConfigChange = (event) => {
    const { name, value, type } = event.target;
    let finalValue = value;
    if (type === 'radio' && (name === 'isCaptchaEnabled' || name === 'isAutoUnlockEnabled')) {
      finalValue = value === 'true';
    } else if (!isNaN(value) && value !== '') {
      finalValue = Number(value);
    }
    setFormData((prev) => ({ ...prev, config: { ...prev.config, [name]: finalValue } }));
  };

  const renderConfigFields = () => {
    const adapter = (formData.authAdapter || '').toUpperCase();

    if (!adapter) return null;

    if (adapter === 'EML_OTP' || adapter === 'SMS_OTP') {
      return (
        <>
          <TextField label="Max OTP Attempts" type="number" name="maxOtpAttempts" value={formData.config?.maxOtpAttempts ?? ''} onChange={handleConfigChange} fullWidth margin="normal"/>
          <TextField label="Resend OTP (sec)" type="number" name="resendOtpInSeconds" value={formData.config?.resendOtpInSeconds ?? ''} onChange={handleConfigChange} fullWidth margin="normal"/>
          <TextField label="OTP Expiry (min)" type="number" name="otpExpiryInMinutes" value={formData.config?.otpExpiryInMinutes ?? ''} onChange={handleConfigChange} fullWidth margin="normal"/>
          <TextField label="Max Attempt Reset After (min)" type="number" name="maxAttemptResetAfterMinutes" value={formData.config?.maxAttemptResetAfterMinutes ?? ''} onChange={handleConfigChange} fullWidth margin="normal"/>
          <TextField label="OTP Length" type="number" name="otpLength" value={formData.config?.otpLength ?? ''} onChange={handleConfigChange} fullWidth margin="normal"/>
        </>
      );
    }

    if (adapter === 'STATIC_PWD' || adapter === 'STATIC_PASSWORD') {
      return (
        <>
          <TextField label="Max Login Attempts" type="number" name="maxLoginAttempts" value={formData.config?.maxLoginAttempts ?? ''} onChange={handleConfigChange} fullWidth margin="normal"/>
          <TextField label="Captcha Needed On Attempts" type="number" name="captchaNeededOnAttempts" value={formData.config?.captchaNeededOnAttempts ?? ''} onChange={handleConfigChange} fullWidth margin="normal"/>
          <FormControl fullWidth margin="normal">
            <FormLabel>Is Captcha Enabled?</FormLabel>
            <RadioGroup row name="isCaptchaEnabled" value={typeof formData.config?.isCaptchaEnabled === 'boolean' ? String(formData.config.isCaptchaEnabled) : ''} onChange={handleConfigChange}>
              <FormControlLabel value="true" control={<Radio />} label="Yes" />
              <FormControlLabel value="false" control={<Radio />} label="No" />
            </RadioGroup>
          </FormControl>
          <FormControl fullWidth margin="normal">
            <FormLabel>Is Auto Unlock Enabled?</FormLabel>
            <RadioGroup row name="isAutoUnlockEnabled" value={typeof formData.config?.isAutoUnlockEnabled === 'boolean' ? String(formData.config.isAutoUnlockEnabled) : ''} onChange={handleConfigChange}>
              <FormControlLabel value="true" control={<Radio />} label="Yes" />
              <FormControlLabel value="false" control={<Radio />} label="No" />
            </RadioGroup>
          </FormControl>
          <TextField label="Auto Unlock After (min)" type="number" name="autoUnLockAfterInMinutes" value={formData.config?.autoUnLockAfterInMinutes ?? ''} onChange={handleConfigChange} fullWidth margin="normal"/>
          <TextField label="Password Expiry (min)" type="number" name="passwordExpiryInMinutes" value={formData.config?.passwordExpiryInMinutes ?? ''} onChange={handleConfigChange} fullWidth margin="normal"/>
        </>
      );
    }

    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        id: formData.id,
        name: formData.name,
        description: `Authentication using ${formData.name}`,
        isActive: formData.isActive,
        authAdapter: formData.authAdapter,
        certificate: formData.certificate ? { id: formData.certificate } : null,
        config: formData.config
      };

      await api.post('/auth-type', payload, { headers: { 'Content-Type': 'application/json' } });
      alert('Authentication Type created successfully!');
      navigate('/authentication');
    } catch (err) {
      console.error('Create auth type failed', err);
      alert('Failed to create authentication type.');
    }
  };

  return (
    <Box sx={{ width: '80vw', minHeight: '80vh', padding: 4 }}>
      <Typography variant="h5" sx={{ mb: 2 }}>Create Authentication Type</Typography>
      <Divider sx={{ mb: 2 }} />

      <form onSubmit={handleSubmit}>
        <FormControl fullWidth margin="normal">
          <TextField select label="Select Adaptor" name="authAdapter" value={formData.authAdapter} onChange={handleInputChange} required>
            {Object.keys(authAdaptorList).map((adapter) => (
              <MenuItem key={adapter} value={adapter}>{adapter}</MenuItem>
            ))}
          </TextField>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <TextField select label="Select Certificate" name="certificate" value={formData.certificate} onChange={handleInputChange}>
            <MenuItem value=""><em>None</em></MenuItem>
            {certificates.map(c => <MenuItem key={c.id} value={c.id}>{c.alias || c.certificateName || c.id}</MenuItem>)}
          </TextField>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <TextField label="Enter Auth Type Name" name="name" value={formData.name} onChange={handleInputChange} required/>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <TextField label="Auth Type ID" name="id" value={formData.id} InputProps={{ readOnly: true }} />
        </FormControl>

        {formData.authAdapter && (
          <Box mt={2} p={2} border={1} borderColor="grey.300" borderRadius={1}>
            <Typography variant="subtitle1">Config Details</Typography>
            {renderConfigFields()}
          </Box>
        )}

        <FormControl fullWidth margin="normal">
          <FormLabel>Is Active</FormLabel>
          <RadioGroup row name="isActive" value={String(formData.isActive)} onChange={handleIsActiveChange}>
            <FormControlLabel value="true" control={<Radio />} label="Enabled" />
            <FormControlLabel value="false" control={<Radio />} label="Disabled" />
          </RadioGroup>
        </FormControl>

        <Box sx={{ mt: 2 }}>
          <Button variant="contained" color="primary" type="submit" sx={{ mr: 2 }}>Create</Button>
          <Button variant="outlined" onClick={() => navigate('/auth-types')}>Cancel</Button>
        </Box>
      </form>
    </Box>
  );
};

export default CreateAuthTypePage;