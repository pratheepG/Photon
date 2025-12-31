import { useState, useEffect } from 'react';
import { Button, TextField, MenuItem, FormControl, FormLabel, RadioGroup, FormControlLabel, Radio, Typography, Box, Divider } from '@mui/material';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../services/api';

const AuthTypeDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [authAdaptorList, setAuthAdaptorList] = useState({});
  const [formData, setFormData] = useState({
    authAdapter: '',
    name: '',
    id: '',
    isActive: true,
    certificate: '',
    config: {}
  });

  useEffect(() => {
    fetchAuthTypeDetails();
    fetchMetaData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchAuthTypeDetails = async () => {
    try {
      const response = await api.get(`/auth-type/${id}`);
      const data = response.data.responseData;

      // normalize config
      setFormData({
        authAdapter: data.authAdapter || data.authadapter || '',
        name: data.name || '',
        id: data.id || '',
        isActive: data.isActive ?? true,
        certificate: (data.certificate && data.certificate.id) || '',
        config: data.config || {}
      });
    } catch (error) {
      console.error('Error fetching authentication type details:', error);
    }
  };

  const fetchMetaData = async () => {
    try {
      const response = await api.get('/identity-meta');
      setAuthAdaptorList(response.data.responseData?.authAdaptor || {});
    } catch (error) {
      console.error('Error fetching metadata:', error);
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

    // radio boolean values
    if (type === 'radio' && (name === 'isCaptchaEnabled' || name === 'isAutoUnlockEnabled')) {
      finalValue = value === 'true';
    } else if (type !== 'radio' && value !== '' && !isNaN(value) && ['maxLoginAttempts','captchaNeededOnAttempts','autoUnLockAfterInMinutes','passwordExpiryInMinutes','maxOtpAttempts','resendOtpInSeconds','otpExpiryInMinutes','maxAttemptResetAfterMinutes','otpLength'].includes(name)) {
      // numeric fields
      finalValue = Number(value);
    }

    setFormData((prev) => ({
      ...prev,
      config: {
        ...prev.config,
        [name]: finalValue,
      },
    }));
  };

  const renderConfigFields = () => {
    const adapter = (formData.authAdapter || '').toUpperCase();

    if (!adapter) return null;

    // support both names that may appear: 'EML_OTP', 'SMS_OTP', 'STATIC_PWD', 'STATIC_PASSWORD'
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

  const handleUpdate = async () => {
    try {
      const payload = {
        id: formData.id,
        name: formData.name,
        description: `Authentication using ${formData.name}`,
        isActive: formData.isActive,
        authAdapter: formData.authAdapter,
        config: formData.config,
        certificate: formData.certificate ? { id: formData.certificate } : null
      };

      await api.put(`/auth-type`, payload, {
        headers: { 'Content-Type': 'application/json' }
      });

      alert('Authentication Type updated successfully!');
      navigate('/authentication');
    } catch (error) {
      console.error('Error updating Authentication Type:', error);
      alert('Failed to update Authentication Type.');
    }
  };

  const handleDelete = async () => {
    try {
      await api.delete(`/auth-type/${id}`);
      alert('Authentication Type deleted successfully!');
      navigate('/authentication');
    } catch (error) {
      console.error('Error deleting Authentication Type:', error);
      alert('Failed to delete Authentication Type.');
    }
  };

  return (
    <Box sx={{ mt: 3 }}>
      <Typography variant="h6">Authentication Type Details</Typography>
      <Divider sx={{ mb: 2 }} />

      <FormControl fullWidth margin="normal">
        <TextField select label="Select Adaptor" name="authAdapter" value={formData.authAdapter} onChange={handleInputChange} required >
          {Object.keys(authAdaptorList).map((adapter) => (
            <MenuItem key={adapter} value={adapter}>
              {adapter}
            </MenuItem>
          ))}
        </TextField>
      </FormControl>

      <FormControl fullWidth margin="normal">
        <TextField label="Enter Auth Type Name" name="name" value={formData.name} onChange={handleInputChange} required/>
      </FormControl>

      <FormControl fullWidth margin="normal">
        <TextField label="Auth Type ID" name="id" value={formData.id} InputProps={{ readOnly: true }}/>
      </FormControl>

      {formData.authAdapter && (
        <Box mt={2} p={2} border={1} borderRadius={2} borderColor="grey.300">
          <Typography variant="subtitle1" gutterBottom>
            Config Details
          </Typography>
          {renderConfigFields()}
        </Box>
      )}

      <FormControl component="fieldset" margin="normal">
        <FormLabel component="legend">Is Active</FormLabel>
        <RadioGroup row name="isActive" value={String(formData.isActive)} onChange={handleIsActiveChange}>
          <FormControlLabel value="true" control={<Radio />} label="Enabled" />
          <FormControlLabel value="false" control={<Radio />} label="Disabled" />
        </RadioGroup>
      </FormControl>

      <Box sx={{ mt: 2 }}>
        <Button variant="contained" color="primary" sx={{ mr: 2 }} onClick={handleUpdate}>
          Update
        </Button>
        <Button variant="outlined" color="error" onClick={handleDelete}>
          Delete
        </Button>
      </Box>
    </Box>
  );
};

export default AuthTypeDetailsPage;