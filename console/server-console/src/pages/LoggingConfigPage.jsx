import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import {
  Box,
  Typography,
  Paper,
  TextField,
  Button,
  Select,
  MenuItem,
  TableContainer,
  Table,
  TableBody,
  TableRow,
  TableCell
} from '@mui/material';
import api from '../services/api';

const LOGGING_POSTFIX = "-LOGGING";
const logLevels = ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL', 'OFF'];

const LoggingConfigPage = () => {
  const { appId } = useParams();
  const [config, setConfig] = useState([]);
  const [editableRow, setEditableRow] = useState(null);
  const [newKey, setNewKey] = useState('');
  const [newValue, setNewValue] = useState('INFO');
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchLoggingConfig();
  }, [appId]);

  const fetchLoggingConfig = async () => {
    try {
      const response = await api.get(`/config-properties?applicationId=${appId}${LOGGING_POSTFIX}`);
      if (response.data.success) {
        const configData = response.data.responseData.config;
        const configList = Object.entries(configData)
          .filter(([key]) => key.startsWith('logging.'));

        setConfig(configList);
        setError(null);
      }
    } catch (error) {
      console.error('Error fetching logging config:', error);
      setError('Failed to load logging configuration');
    }
  };

  const handleAddOrUpdate = async (key, value) => {
    const payload = {
      id: appId + LOGGING_POSTFIX,
      config: {
        [key]: value
      }
    };

    try {
      await api.patch(`/config-properties/${appId}${LOGGING_POSTFIX}`, payload);
      alert(`Key "${key}" ${editableRow === null ? 'added' : 'updated'} successfully!`);
      fetchLoggingConfig();
      setNewKey('');
      setNewValue('INFO');
      setEditableRow(null);
    } catch (error) {
      console.error('Save failed:', error);
      alert('Failed to save changes');
    }
  };

  const handleDelete = async (key) => {
    const payload = {
      id: appId + LOGGING_POSTFIX,
      config: {
        [key]: null
      }
    };

    try {
      await api.patch(`/config-properties/${appId}${LOGGING_POSTFIX}`, payload);
      alert(`Key "${key}" deleted successfully!`);
      fetchLoggingConfig();
    } catch (error) {
      console.error('Delete failed:', error);
      alert('Failed to delete key');
    }
  };

  const handleLevelChange = (index, newValue) => {
    const updated = [...config];
    updated[index][1] = newValue;
    setConfig(updated);
  };

  const handleSave = (index) => {
    const [key, value] = config[index];
    handleAddOrUpdate(key, value);
  };

  const handleAddNew = () => {
    if (!newKey || !newValue) {
      alert('Key and value are required');
      return;
    }

    if (config.some(([key]) => key === newKey)) {
      alert('Key already exists');
      return;
    }

    handleAddOrUpdate(newKey, newValue);
  };

  return (
    <Box sx={{ width: '80vw', padding: '20px', backgroundColor: '#f5f5f5' }}>
      <Typography variant="h6" sx={{ mb: 3 }}>
        Logging Configuration for {appId}
      </Typography>

      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
        <TextField
          label="Key"
          value={newKey}
          onChange={(e) => setNewKey(e.target.value)}
          fullWidth
        />
        <Select
          value={newValue}
          onChange={(e) => setNewValue(e.target.value)}
          fullWidth
        >
          {logLevels.map((level) => (
            <MenuItem key={level} value={level}>
              {level}
            </MenuItem>
          ))}
        </Select>
        <Button variant="outlined" onClick={handleAddNew}>
          Add
        </Button>
      </Box>

      {error ? (
        <Typography color="error">{error}</Typography>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableBody>
              {config.map(([key, value], index) => (
                <TableRow key={key}>
                  <TableCell sx={{ fontWeight: 'bold', width: '30%' }}>{key}</TableCell>
                  <TableCell sx={{ width: '40%' }}>
                    <Select
                      fullWidth
                      value={value}
                      disabled={editableRow !== index}
                      onChange={(e) => handleLevelChange(index, e.target.value)}
                    >
                      {logLevels.map((level) => (
                        <MenuItem key={level} value={level}>
                          {level}
                        </MenuItem>
                      ))}
                    </Select>
                  </TableCell>
                  <TableCell>
                    {editableRow === index ? (
                      <Button variant="outlined" onClick={() => handleSave(index)}>
                        Save
                      </Button>
                    ) : (
                      <Button variant="outlined" onClick={() => setEditableRow(index)}>
                        Edit
                      </Button>
                    )}
                  </TableCell>
                  <TableCell>
                    <Button variant="outlined" color="error" onClick={() => handleDelete(key)}>
                      Delete
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
};

export default LoggingConfigPage;