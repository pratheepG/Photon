import React, { useState, useEffect } from 'react';
import {
  Box, Typography, Paper, Tabs, Tab, Radio, FormGroup, FormControlLabel, TextField,
  Button, Toolbar, Grid, CircularProgress, Divider, RadioGroup
} from '@mui/material';
import SaveIcon from '@mui/icons-material/Save';
import UndoIcon from '@mui/icons-material/Undo';
import api from '../services/api';

const PROVIDERS = {
  sms: ['twilio', 'vonage', 'plivo', 'messagebird', 'console'],
  email: ['smtp', 'gmail', 'sendgrid', 'outlook', 'console'],
  push: ['fcm', 'apns', 'onesignal', 'sns', 'console'],
};

const AlertSettingsPage = () => {
  const appId = 'ALERT-SETTINGS';
  const [activeTab, setActiveTab] = useState(0);
  const [loading, setLoading] = useState(true);
  const [config, setConfig] = useState({});
  const [originalConfig, setOriginalConfig] = useState({});
  const [selectedProviders, setSelectedProviders] = useState({
    sms: 'console',
    email: 'console',
    push: 'console',
  });

  useEffect(() => {
    fetchAlertConfig();
  }, []);

  const fetchAlertConfig = async () => {
    try {
      const res = await api.get(`/config-properties?applicationId=${appId}`);
      if (res.data.success) {
        const cfg = res.data.responseData.config || {};
        const selected = {
          sms: PROVIDERS.sms.find(p => Object.keys(cfg).some(k => k.startsWith(p))) || 'console',
          email: PROVIDERS.email.find(p => Object.keys(cfg).some(k => k.startsWith(p))) || 'console',
          push: PROVIDERS.push.find(p => Object.keys(cfg).some(k => k.startsWith(p))) || 'console',
        };        
        setConfig(cfg);
        setOriginalConfig(cfg);
        setSelectedProviders(selected);
      }
    } catch (err) {
      console.error('Failed to fetch alert config', err);
      // Fail-safe: empty config & tabs still show
      setConfig({});
      setOriginalConfig({});
      setSelectedProviders({
        sms: 'console',
        email: 'console',
        push: 'console',
      });
    } finally {
      setLoading(false);
    }
  };  

  const handleProviderToggle = (type, provider) => {
    const updated = selectedProviders[type].includes(provider)
      ? selectedProviders[type].filter(p => p !== provider)
      : [...selectedProviders[type], provider];
    setSelectedProviders(prev => ({ ...prev, [type]: updated }));
  };

  const handleProviderChange = (type, provider) => {
    const updated = { ...selectedProviders, [type]: provider };
    setSelectedProviders(updated);
    setConfig(prev => ({
      ...prev,
      'mail.provider': updated.email,
      'sms.provider': updated.sms,
      'push.provider': updated.push,
    }));
  };

  const handleInputChange = (key, value) => {
    setConfig(prev => ({ ...prev, [key]: value }));
  };

  const handleSave = async () => {
    let filteredConfig = { ...config };
    Object.entries(selectedProviders).forEach(([type, selected]) => {
      PROVIDERS[type].forEach((provider) => {
        if (provider !== selected && provider !== 'console') {
          const keysToRemove = providerKeys[provider] || [];
          keysToRemove.forEach((key) => {
            delete filteredConfig[key];
          });
        }
      });
    });

    filteredConfig['sms.provider'] = selectedProviders.sms;
    filteredConfig['mail.provider'] = selectedProviders.email;
    filteredConfig['push.provider'] = selectedProviders.push;
  
    try {
      await api.put(`/config-properties/${appId}`, {
        applicationId: appId,
        config: filteredConfig,
      });
      alert('Alert settings saved successfully!');
      fetchAlertConfig();
    } catch (err) {
      console.error('Failed to save alert settings', err);
      alert('Failed to save alert settings.');
    }
  };

  const handleUndo = () => {
    setConfig(originalConfig);
    setSelectedProviders({
      sms: originalConfig['sms.provider'] || 'console',
      email: originalConfig['mail.provider'] || 'console',
      push: originalConfig['push.provider'] || 'console',
    });
  };  

  const renderInputsForProvider = (provider, keys) => (
    keys.map(key => (
      <Grid item xs={6} key={key}>
        <TextField fullWidth label={key.split('.').pop()} value={config[key] || ''} onChange={(e) => handleInputChange(key, e.target.value)}/>
      </Grid>
    ))
  );

  const providerKeys = {
    twilio: ['twilio.sid', 'twilio.token', 'twilio.phonenumber'],
    vonage: ['vonage.apiKey', 'vonage.apiSecret'],
    plivo: ['plivo.authId', 'plivo.authToken'],
    messagebird: ['messagebird.accessKey'],
  
    smtp: ['email.smtp.host', 'email.smtp.port', 'email.smtp.username', 'email.smtp.password', 'email.smtp.tls'],
    gmail: ['gmail.username', 'gmail.password'],
    sendgrid: ['sendgrid.api.key'],
    outlook: ['outlook.client.id', 'outlook.client.secret', 'outlook.tenant.id', 'outlook.auth.url'],
  
    fcm: ['fcm.api.url', 'fcm.server.key', 'fcm.sender.id', 'fcm.device.token', 'fcm.project.id', 'fcm.application.id'],
    apns: ['apns.auth.key.id', 'apns.auth.team.id', 'apns.auth.bundle.id', 'apns.auth.key.file', 'apns.url', 'apns.environment', 'apns.device.token'],
    onesignal: ['onesignal.api.url', 'onesignal.app.id', 'onesignal.rest.api.key', 'onesignal.device.id', 'onesignal.segment.name'],
    sns: ['aws.access.key', 'aws.secret.key', 'aws.region', 'sns.platform.application.arn.android', 'sns.platform.application.arn.ios', 'sns.device.token']
  };
  

  const renderTabContent = (type) => {

    return (
      <>
        <FormGroup>
          <RadioGroup row value={selectedProviders[type]} onChange={(e) => handleProviderChange(type, e.target.value)}>
            {PROVIDERS[type].map((provider) => (
              <FormControlLabel key={provider} value={provider} control={<Radio />} label={provider.toUpperCase()}/>
            ))}
          </RadioGroup>
        </FormGroup>

        <Grid container spacing={2} sx={{ mt: 2 }}>
        {selectedProviders[type] && renderInputsForProvider(selectedProviders[type], providerKeys[selectedProviders[type]] || [])}
        </Grid>
      </>
    );
  };

  if (loading) return <CircularProgress sx={{ display: 'block', margin: 'auto', mt: 5 }} />;

  return (
    <Box sx={{ width: '80vw', minHeight: '80vh', padding: '20px', backgroundColor: '#f0f4f8' }}>
      <Typography variant="h5" sx={{ color: '#1e3a8a', mb: 2 }}>
        Alert Configuration Settings
      </Typography>

      <Paper sx={{ padding: '20px', borderRadius: '12px', boxShadow: 4, backgroundColor: '#ffffff' }}>
        <Tabs value={activeTab} onChange={(_, v) => setActiveTab(v)} aria-label="alert config tabs">
          <Tab label="SMS Config" />
          <Tab label="Email Config" />
          <Tab label="Push Notification Config" />
        </Tabs>

        <Divider sx={{ my: 2 }} />

        {activeTab === 0 && renderTabContent('sms')}
        {activeTab === 1 && renderTabContent('email')}
        {activeTab === 2 && renderTabContent('push')}

        <Toolbar sx={{ display: 'flex', justifyContent: 'flex-end', mt: 3 }}>
          <Button startIcon={<UndoIcon />} onClick={handleUndo} sx={{ mr: 2 }}>
            Undo
          </Button>
          <Button variant="contained" color="primary" startIcon={<SaveIcon />} onClick={handleSave}>
            Save
          </Button>
        </Toolbar>
      </Paper>
    </Box>
  );
};

export default AlertSettingsPage;