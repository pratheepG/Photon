import React, { useState, useEffect } from 'react';
import { Box, TextField, Button, Checkbox, FormControl, InputLabel, Select, MenuItem, FormControlLabel, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const CreateRolePage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    roleId: '',
    isActive: true,
    idp: '',
    accessLevel: ''
  });
  const [idpList, setIdpList] = useState([]);

  const accessLevel = [
    {id: "NONE", description: "It provide none of the resource access"},
    {id: "VIEWER", description: "It provide read-only access for the resources"},
    {id: "EDITOR", description: "It provide read and edit access for the resources"},
    {id: "OWNER", description: "It provide read and edit access for the owner's resources"},
    {id: "ADMIN", description: "It provide global access for all resources"},
    {id: "TENANT_ADMIN", description: "It provide tenant-level access for all resources"},
  ]

  useEffect(() => {
    const fetchIdps = async () => {
      try {
        const response = await api.get('/identity-provider?pageNumber=0&pageSize=10');
        if (response.data && response.data.responseData) {
          setIdpList(response.data.responseData);
        }
      } catch (error) {
        console.error('Failed to fetch IDPs:', error);
      }
    };
    fetchIdps();
  }, []);

  const handleInputChange = (event) => {
    const { name, value } = event.target;

    if (name === 'name') {
      const regex = /^[A-Za-z\s]*$/;
      if (regex.test(value)) {
        setFormData({
          ...formData,
          name: value,
          roleId: value.toUpperCase().replace(/\s+/g, '_'),
        });
      }
    } else {
      setFormData({
        ...formData,
        [name]: value,
      });
    }
  };

  const handleCheckboxChange = (event) => {
    setFormData({
      ...formData,
      isActive: event.target.checked,
    });
  };

  const handleSubmit = async () => {
    try {
      const payload = {
        roleId: formData.roleId,
        name: formData.name,
        description: formData.description,
        isActive: formData.isActive,
        idp: formData.idp,
        accessLevel: formData.accessLevel
      };

      await api.post('/role', payload, {
        headers: { 'Content-Type': 'application/json' },
      });

      alert('Role created successfully!');
      navigate('/user-roles');
    } catch (error) {
      console.error('Error creating role:', error);
      alert('Failed to create role.');
    }
  };

  return (
    <Box sx={{ mt: 3, width: '50%', margin: '0 auto' }}>
      <Typography variant="h6" sx={{ mb: 2 }}>
        Create New Role
      </Typography>

      <TextField fullWidth margin="normal" label="ID" name="roleId" value={formData.roleId} InputProps={{ readOnly: true }} />
      <TextField fullWidth margin="normal" label="Name" name="name" value={formData.name} onChange={handleInputChange} required />
      <TextField fullWidth margin="normal" label="Description" name="description" value={formData.description} onChange={handleInputChange} required />

      {/* IDP Dropdown */}
      <FormControl fullWidth margin="normal">
        <InputLabel>Select IDP</InputLabel>
        <Select name="idp" value={formData.idp} onChange={handleInputChange} required>
          {idpList.map((idp) => (
            <MenuItem key={idp.id} value={idp.id}>
              {idp.name || idp.id}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      {/* Api Access-Level Dropdown */}
      <FormControl fullWidth margin="normal">
        <InputLabel>Select Access-Level</InputLabel>
        <Select name="accessLevel" value={formData.accessLevel} onChange={handleInputChange} required>
          {accessLevel.map((accessLevel) => (
            <MenuItem key={accessLevel.id} value={accessLevel.id}>
              {accessLevel.id}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      <FormControlLabel control={<Checkbox checked={formData.isActive} onChange={handleCheckboxChange} />} label="Is Active"/>

      <Button variant="contained" color="primary" sx={{ mt: 2 }} onClick={handleSubmit}>
        Submit
      </Button>
    </Box>
  );
};

export default CreateRolePage;