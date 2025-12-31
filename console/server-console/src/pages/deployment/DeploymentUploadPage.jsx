import React, { useState } from 'react';
import {
  Box, Paper, Typography, Button, Grid, TextField, IconButton, Alert, CircularProgress, Stack
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import RemoveCircleOutlineIcon from '@mui/icons-material/RemoveCircleOutline';
import api from '../../services/api';

export default function DeploymentUploadPage() {
  const navigate = useNavigate();

  const [serviceName, setServiceName] = useState('');
  const [jarFile, setJarFile] = useState(null);
  const [dockerfile, setDockerfile] = useState('');
  const [envPairs, setEnvPairs] = useState([{ key: '', value: '' }]);
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const onJarChange = (e) => {
    const f = e.target.files && e.target.files[0];
    setJarFile(f || null);
  };

  const addEnv = () => setEnvPairs(prev => ([ ...prev, { key: '', value: '' } ]));
  const removeEnv = (idx) => setEnvPairs(prev => prev.filter((_, i) => i !== idx));
  const updateEnv = (idx, field, val) => setEnvPairs(prev => {
    const copy = [...prev];
    copy[idx] = { ...copy[idx], [field]: val };
    return copy;
  });

  const validate = () => {
    if (!serviceName.trim()) return 'Service name is required';
    if (!jarFile) return 'Jar file is required';
    for (const e of envPairs) {
      if ((e.key && !e.value) || (!e.key && e.value)) return 'Env entries must have both key and value or be empty';
    }
    return null;
  };

  const handleSubmit = async () => {
    const v = validate();
    if (v) {
      setErrorMsg(v);
      return;
    }
    setErrorMsg('');
    setSuccessMsg('');
    setSubmitting(true);

    try {
      const formData = new FormData();
      formData.append('jar', jarFile);
      formData.append('dockerfile', dockerfile || '');
      formData.append('serviceName', serviceName);
      envPairs.forEach(({ key, value }) => {
        if (key) formData.append(`env[${key}]`, value);
      });

      const resp = await api.post('/deployment/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      if (resp.data?.success) {
        setSuccessMsg(resp.data?.message || 'Upload successful');
        setTimeout(() => navigate('/deployment'), 600);
      } else {
        setErrorMsg(resp.data?.message || 'Upload failed');
      }
    } catch (err) {
      console.error('Upload failed', err);
      setErrorMsg('Upload failed. See console for details.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box sx={{ width: '90vw', minHeight: '80vh', padding: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4">Upload Deployment Snapshot</Typography>
        <Button variant="outlined" onClick={() => navigate('/deployment')}>Back to list</Button>
      </Box>

      <Paper sx={{ p: 2 }}>
        {errorMsg && <Alert severity="error" sx={{ mb: 2 }}>{errorMsg}</Alert>}
        {successMsg && <Alert severity="success" sx={{ mb: 2 }}>{successMsg}</Alert>}

        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <TextField label="Service Name" value={serviceName} onChange={(e) => setServiceName(e.target.value)} fullWidth />
          </Grid>

          <Grid item xs={12} sm={6}>
            <Button variant="outlined" component="label">
              {jarFile ? `Jar: ${jarFile.name}` : 'Upload Jar File'}
              <input type="file" accept=".jar" hidden onChange={onJarChange} />
            </Button>
          </Grid>

          <Grid item xs={12}>
            <TextField label="Dockerfile (text)" value={dockerfile} onChange={(e) => setDockerfile(e.target.value)} fullWidth multiline minRows={6} placeholder="Paste Dockerfile contents (optional)"/>
          </Grid>

          <Grid item xs={12}><Typography variant="subtitle1">Environment Variables</Typography></Grid>

          {envPairs.map((env, idx) => (
            <React.Fragment key={idx}>
              <Grid item xs={5}>
                <TextField label="Key" value={env.key} onChange={(e) => updateEnv(idx, 'key', e.target.value)} fullWidth/>
              </Grid>
              <Grid item xs={5}>
                <TextField label="Value" value={env.value} onChange={(e) => updateEnv(idx, 'value', e.target.value)} fullWidth/>
              </Grid>
              <Grid item xs={2} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                {idx === envPairs.length - 1 ? (
                  <IconButton onClick={addEnv} aria-label="add env"><AddCircleOutlineIcon /></IconButton>
                ) : (
                  <IconButton onClick={() => removeEnv(idx)} aria-label="remove env"><RemoveCircleOutlineIcon /></IconButton>
                )}
              </Grid>
            </React.Fragment>
          ))}

        </Grid>

        <Stack direction="row" justifyContent="flex-end" spacing={2} sx={{ mt: 2 }}>
          <Button onClick={() => navigate('/deployment')}>Cancel</Button>
          <Button variant="contained" onClick={handleSubmit} disabled={submitting}>
            {submitting ? <><CircularProgress size={18} sx={{ mr: 1 }} /> Uploading...</> : 'Upload'}
          </Button>
        </Stack>
      </Paper>
    </Box>
  );
}