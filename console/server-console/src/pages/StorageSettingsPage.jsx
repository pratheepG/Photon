import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  Radio,
  RadioGroup,
  FormControlLabel,
  FormControl,
  Divider,
  TextField,
  Button,
  Grid,
  Toolbar,
  CircularProgress
} from '@mui/material';
import UndoIcon from '@mui/icons-material/Undo';
import SaveIcon from '@mui/icons-material/Save';
import api from '../services/api';

const StorageSettingsPage = () => {
  const appId = 'STORAGE';
  const [selectedCdn, setSelectedCdn] = useState('');
  const [loading, setLoading] = useState(true);
  const [isChanged, setIsChanged] = useState(false);
  const [originalConfig, setOriginalConfig] = useState({});
  const [config, setConfig] = useState({
    aws: { accessKey: '', secretKey: '', bucketName: '', region: '' },
    google: { projectId: '', clientEmail: '', privateKey: '', bucketName: '' },
    azure: { accountName: '', accountKey: '', containerName: '' },
  });

  useEffect(() => {
    fetchStorageConfig();
  }, []);

  const fetchStorageConfig = async () => {
    try {
      const response = await api.get(`/config-properties?applicationId=${appId}`);

      if (response.data.success) {
        const storageConfig = response.data.responseData.config;

        const providerMap = {
          AWS_S3: 'aws',
          GOOGLE_CLOUD: 'google',
          AZURE_CLOUD: 'azure',
        };

        const providerKey = storageConfig['photon.bucket.provider'] || '';
        const cdnType = providerMap[providerKey] || '';

        const updatedConfig = {
          aws: {
            accessKey: storageConfig['aws.accessKeyId'] || '',
            secretKey: storageConfig['aws.secretKey'] || '',
            bucketName: storageConfig['aws.s3.bucket'] || '',
            region: storageConfig['aws.region'] || '',
          },
          google: {
            projectId: storageConfig['google.projectId'] || '',
            clientEmail: storageConfig['google.clientEmail'] || '',
            privateKey: storageConfig['google.privateKey'] || '',
            bucketName: storageConfig['google.bucketName'] || '',
          },
          azure: {
            accountName: storageConfig['azure.accountName'] || '',
            accountKey: storageConfig['azure.accountKey'] || '',
            containerName: storageConfig['azure.containerName'] || '',
          },
        };

        setOriginalConfig(updatedConfig);
        setConfig(updatedConfig);
        setSelectedCdn(cdnType);
      }
    } catch (error) {
      console.error('Error fetching storage config:', error);
    } finally {
      setLoading(false);
      setIsChanged(false);
    }
  };

  const handleCdnChange = (event) => {
    setSelectedCdn(event.target.value);
    setIsChanged(true);
  };

  const handleInputChange = (cdn, field, value) => {
    setConfig((prev) => ({
      ...prev,
      [cdn]: { ...prev[cdn], [field]: value },
    }));
    setIsChanged(true);
  };

  const handleSave = async () => {
    const providerMap = {
      aws: 'AWS_S3',
      google: 'GOOGLE_CLOUD',
      azure: 'AZURE_CLOUD',
    };

    const payload = {
      applicationId: appId,
      config: {
        'photon.bucket.provider': providerMap[selectedCdn],
        ...(selectedCdn === 'aws' && {
          'aws.accessKeyId': config.aws.accessKey,
          'aws.secretKey': config.aws.secretKey,
          'aws.s3.bucket': config.aws.bucketName,
          'aws.region': config.aws.region,
        }),
        ...(selectedCdn === 'google' && {
          'google.projectId': config.google.projectId,
          'google.clientEmail': config.google.clientEmail,
          'google.privateKey': config.google.privateKey,
          'google.bucketName': config.google.bucketName,
        }),
        ...(selectedCdn === 'azure' && {
          'azure.accountName': config.azure.accountName,
          'azure.accountKey': config.azure.accountKey,
          'azure.containerName': config.azure.containerName,
        }),
      },
    };

    try {
      await api.patch(`/config-properties/${appId}`, payload);
      alert('Storage Configuration Saved Successfully!');
      fetchStorageConfig();
    } catch (error) {
      console.error('Error saving storage config:', error);
      alert('Failed to save storage configuration');
    }
  };

  const handleUndo = () => {
    setConfig(originalConfig);
    setIsChanged(false);
  };

  if (loading) return <CircularProgress sx={{ display: 'block', margin: 'auto', mt: 5 }} />;

  return (
    <Box sx={{ width: '80vw', minHeight: '80vh', padding: '20px', backgroundColor: '#f0f4f8' }}>
      <Typography variant="h5" sx={{ color: '#1e3a8a', mb: 2 }}>
        Storage Configuration Settings
      </Typography>

      <Paper sx={{ padding: '20px', borderRadius: '12px', boxShadow: 4, backgroundColor: '#ffffff' }}>
        {/* CDN Selection */}
        <Typography variant="h6">Select CDN Type</Typography>
        <FormControl component="fieldset" sx={{ mt: 1 }}>
          <RadioGroup row value={selectedCdn} onChange={handleCdnChange}>
            <FormControlLabel value="aws" control={<Radio />} label="AWS S3" />
            <FormControlLabel value="google" control={<Radio />} label="Google Cloud" />
            <FormControlLabel value="azure" control={<Radio />} label="Azure Cloud" />
          </RadioGroup>
        </FormControl>

        <Divider sx={{ my: 3 }} />

        {/* Configuration Fields Based on Selected CDN */}
        {selectedCdn && (
          <Box>
            <Typography variant="h6">Configuration Details</Typography>
            <Grid container spacing={2} sx={{ mt: 1 }}>
              {Object.keys(config[selectedCdn]).map((field) => (
                <Grid item xs={6} key={field}>
                  <TextField
                    label={field.replace(/([A-Z])/g, ' $1').replace(/^./, (str) => str.toUpperCase())}
                    value={config[selectedCdn][field]}
                    onChange={(e) => handleInputChange(selectedCdn, field, e.target.value)}
                    fullWidth
                  />
                </Grid>
              ))}
            </Grid>
          </Box>
        )}

        {/* Bottom Toolbar */}
        <Toolbar sx={{ display: 'flex', justifyContent: 'flex-end', mt: 3 }}>
          <Button startIcon={<UndoIcon />} onClick={handleUndo} sx={{ mr: 2 }}>
            Undo
          </Button>
          <Button
            variant="contained"
            color="primary"
            startIcon={<SaveIcon />}
            onClick={handleSave}
            disabled={!isChanged || !selectedCdn}
          >
            Save
          </Button>
        </Toolbar>
      </Paper>
    </Box>
  );
};

export default StorageSettingsPage;