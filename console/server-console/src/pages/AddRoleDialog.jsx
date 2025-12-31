import React, { useState, useEffect } from 'react';
import { Dialog, DialogActions, DialogContent, DialogTitle, Button, Autocomplete, TextField, Select, MenuItem, FormControl, InputLabel } from '@mui/material';
import api from '../services/api';

const AddRoleDialog = ({ open, onClose, actionId, appId }) => {
  const [selectedRoles, setSelectedRoles] = useState([]);
  const [roles, setRoles] = useState([]);
  const [loadingRoles, setLoadingRoles] = useState(false);

  useEffect(() => {
    if (open) {
      fetchRoles();
    }
  }, [open]);

  const fetchRoles = async () => {
    setLoadingRoles(true);
    try {
      const response = await api.get('/role?pageNumber=0&pageSize=10');
      if (response.data.success) {
        setRoles(response.data.responseData.map(role => role.id));
      }
    } catch (error) {
      console.error('Error fetching roles:', error);
    } finally {
      setLoadingRoles(false);
    }
  };

  const handleRolesChange = (event, value) => {
    setSelectedRoles(value);
  };

  const handleSubmit = async () => {
    try {
      const payload = {
        userRoles: selectedRoles,
      };

      await api.patch(`/api-manager/services/action/${actionId}`, payload);
      alert('Roles added successfully!');
      onClose();
    } catch (error) {
      console.error('Error adding roles:', error);
      alert('Failed to add roles.');
    }
  };

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Add User Roles</DialogTitle>
      <DialogContent>
        {/* Searchable Role Selection */}
        <Autocomplete
          multiple
          options={roles}
          getOptionLabel={(option) => option}
          loading={loadingRoles}
          onChange={handleRolesChange}
          value={selectedRoles}
          renderInput={(params) => (
            <TextField
              {...params}
              label="User Roles"
              placeholder="Select roles"
              InputProps={{
                ...params.InputProps,
                endAdornment: (
                  <>
                    {loadingRoles ? <span>Loading...</span> : null}
                    {params.InputProps.endAdornment}
                  </>
                ),
              }}
            />
          )}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button onClick={handleSubmit} variant="contained" color="primary">
          Add
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AddRoleDialog;