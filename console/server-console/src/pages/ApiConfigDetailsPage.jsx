import { useState, useEffect } from 'react';
import { Box, Typography, Table, TableBody, TableCell, TableContainer, TableRow, Paper, Button, TextField } from '@mui/material';
import { useParams } from 'react-router-dom';
import api from '../services/api';

const ApiConfigDetailsPage = () => {
  const { appId } = useParams();
  const [config, setConfig] = useState([]);
  const [editableRow, setEditableRow] = useState(null);
  const [newKey, setNewKey] = useState('');
  const [newValue, setNewValue] = useState('');
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchConfigDetails();
  }, [appId]);

  const fetchConfigDetails = async () => {
    try {
      const response = await api.get(`/config-properties?applicationId=${appId}`);
      if (response.data.success) {
        const configData = response.data.responseData.config;
        setConfig(Object.entries(configData));
        setError(null);
      }
    } catch (error) {
      if (error.response && error.response.data && error.response.data.errCode === 1006) {
        setConfig([]);
        setError(error.response.data.responseData.message);
      } else {
        console.error('Error fetching API Config:', error);
        setError('An unexpected error occurred.');
      }
    }
  };

  const handleAddOrUpdatePair = async (key, value) => {
    const payload = {
      id: appId,
      config: {
        [key]: value,
      },
    };

    try {
      await api.patch(`/config-properties/${appId}`, payload);
      alert(`Key "${key}" has been ${editableRow === null ? 'added' : 'updated'} successfully!`);
      fetchConfigDetails();
      setNewKey('');
      setNewValue('');
      setEditableRow(null);
    } catch (error) {
      console.error(`Error ${editableRow === null ? 'adding' : 'updating'} key "${key}":`, error);
      alert(`Failed to ${editableRow === null ? 'add' : 'update'} the key-value pair.`);
    }
  };

  const handleSaveClick = (index) => {
    const key = config[index][0];
    const value = config[index][1];
    handleAddOrUpdatePair(key, value);
  };

  const handleDeleteClick = async (key) => {
    const updatedConfig = config.filter(([k]) => k !== key);
    setConfig(updatedConfig);

    const payload = {
      id: appId,
      config: {
        [key]: null,
      },
    };

    try {
      await api.patch('/config-properties/'+appId, payload);
      alert(`Key "${key}" deleted successfully!`);
      fetchConfigDetails();
    } catch (error) {
      console.error(`Error deleting key "${key}":`, error);
      alert('Failed to delete the key.');
    }
  };

  const handleInputChange = (index, newValue) => {
    const updatedConfig = [...config];
    updatedConfig[index][1] = newValue;
    setConfig(updatedConfig);
  };

  const handleAddNewPair = () => {
    if (!newKey || !newValue) {
      alert('Both key and value are required.');
      return;
    }
    if (config.some(([key]) => key === newKey)) {
      alert('Key already exists.');
      return;
    }

    handleAddOrUpdatePair(newKey, newValue);
  };

  return (
    <Box
      sx={{
        width: "80vw",
        minHeight: "80vh",
        padding: "20px",
        backgroundColor: "#f5f5f5",
      }}
    >
      <Typography variant="h6" sx={{ mb: 4 }}>
        API Config: {appId}
      </Typography>

      <Box sx={{ display: "flex", alignItems: "center", gap: 2, mb: 2 }}>
        <TextField
          label="Key"
          variant="outlined"
          inputProps={{
            style: {
              padding: '10px',
              paddingInline: '5px',
            },
          }}
          value={newKey}
          onChange={(e) => setNewKey(e.target.value)}
          fullWidth
        />
        <TextField
          label="Value"
          variant="outlined"
          inputProps={{
            style: {
              padding: '10px',
            },
          }}
          value={newValue}
          onChange={(e) => setNewValue(e.target.value)}
          fullWidth
        />
        <Button variant="outlined" color="primary" onClick={handleAddNewPair}>
          Add
        </Button>
      </Box>

      {error ? (
        <Box
          sx={{
            padding: 2,
            border: '1px solid #f44336',
            borderRadius: '4px',
            backgroundColor: '#ffebee',
            color: '#d32f2f',
            textAlign: 'center',
          }}
        >
          {error}
        </Box>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableBody>
              {config.map(([key, value], index) => (
                <TableRow key={key}>
                  <TableCell sx={{ fontWeight: "bold", width: "30%", paddingLeft: "15px" }}>
                    {key}
                  </TableCell>
                  <TableCell sx={{ width: "40%", paddingTop: "10px", paddingBottom: "10px" }}>
                    <TextField
                      value={value}
                      variant="outlined"
                      fullWidth
                      inputProps={{
                        style: {
                          padding: '10px',
                        },
                      }}
                      disabled={editableRow !== index}
                      onChange={(e) => handleInputChange(index, e.target.value)}/>
                  </TableCell>
                  <TableCell sx={{ width: "2%", padding: "5px", textAlign: "center" }}>
                    {editableRow === index ? (
                      <Button
                        variant="outlined"
                        color="primary"
                        onClick={() => handleSaveClick(index)}>
                        SAVE
                      </Button>
                    ) : (
                      <Button
                        variant="outlined"
                        color="primary"
                        onClick={() => setEditableRow(index)}>
                        EDIT
                      </Button>
                    )}
                  </TableCell>
                  <TableCell sx={{ width: "2%", padding: "5px", textAlign: "center" }}>
                    <Button
                      variant="outlined"
                      color="error"
                      onClick={() => handleDeleteClick(key)}
                      sx={{
                        color: "#d32f2f",
                        "&:hover": { backgroundColor: "#fdecea" },
                      }}
                    >
                      DELETE
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

export default ApiConfigDetailsPage;