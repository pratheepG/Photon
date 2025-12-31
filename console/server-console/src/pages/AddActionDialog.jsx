import React, { useState, useEffect } from 'react';
import { Dialog, DialogActions, DialogContent, DialogTitle, Button, FormControl, InputLabel, MenuItem, Select, Checkbox, ListItemText, OutlinedInput } from '@mui/material';
import api from '../services/api';

const AddActionDialog = ({ open, onClose, role }) => {
  const [applications, setApplications] = useState([]);
  const [features, setFeatures] = useState([]);
  const [actions, setActions] = useState([]);
  const [selectedApp, setSelectedApp] = useState('');
  const [selectedFeature, setSelectedFeature] = useState('');
  const [selectedActions, setSelectedActions] = useState([]);
  const [selectedActionNames, setSelectedActionNames] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchApplications = async () => {
    try {
      const response = await api.get('/api-manager/apps');
      if (response.data.success) {
        setApplications(response.data.responseData);
      }
    } catch (error) {
      console.error('Error fetching applications:', error);
    }
  };

  const fetchFeatures = async (app) => {
    setLoading(true);
    try {
      const response = await api.get(`/api-manager/services/feature/${app.toUpperCase()}?pageNumber=0&pageSize=10`);
      if (response.data.success) {
        setFeatures(response.data.responseData);
      }
    } catch (error) {
      console.error('Error fetching features:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleFeatureSelect = (featureId) => {
    setSelectedFeature(featureId);
    const feature = features.find((f) => f.featureId === featureId);
    if (feature) {
      setActions(feature.actions);
    }
  };

  const handleActionSelect = (event) => {
    const selectedIds = event.target.value;
    setSelectedActions(selectedIds);

    const selectedNames = actions
      .filter((action) => selectedIds.includes(action.id))
      .map((action) => action.name);
    setSelectedActionNames(selectedNames);
  };

  const handleAddActions = async () => {
    var id = role.id;
    var idp = role.idp;
    var roleId = role.roleId;
    const payload = selectedActions.map((actionId) => ({
      id: actionId, 
      actionInfo: {
        userRoles: { [idp]: {[id]:roleId}},
      },
    }));

    try {
      await api.patch('/api-manager/services/action', payload);
      alert('Actions added successfully!');
      onClose();
    } catch (error) {
      console.error('Error updating actions:', error);
      alert('Failed to add actions.');
    }
  };

  useEffect(() => {
    if (open) {
      fetchApplications();
    }
  }, [open]);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Add Action</DialogTitle>
      <DialogContent>

        {/* Application Dropdown */}
        <FormControl fullWidth margin="normal">
          <InputLabel>Application</InputLabel>
          <Select value={selectedApp} onChange={(e) => { setSelectedApp(e.target.value); fetchFeatures(e.target.value); }}>
            {applications.map((app) => (
              <MenuItem key={app} value={app}>
                {app}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        {/* Feature Dropdown */}
        <FormControl fullWidth margin="normal" disabled={!selectedApp || loading}>
          <InputLabel>Feature</InputLabel>
          <Select value={selectedFeature} onChange={(e) => handleFeatureSelect(e.target.value)}>
            {features.map((feature) => (
              <MenuItem key={feature.featureId} value={feature.featureId}>
                {feature.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        {/* Action Dropdown (Multiple Selection) */}
        <FormControl fullWidth margin="normal" disabled={!selectedFeature}>
          <InputLabel>Action</InputLabel>
          <Select multiple value={selectedActions} onChange={handleActionSelect} input={<OutlinedInput />} renderValue={(selected) => selectedActionNames.join(', ')}>
            {actions.map((action) => (
              <MenuItem key={action.id} value={action.id}>
                <Checkbox checked={selectedActions.indexOf(action.id) > -1} />
                <ListItemText primary={action.name} />
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} color="secondary">Cancel</Button>
        <Button onClick={handleAddActions} color="primary" disabled={!selectedActions.length || !selectedApp || !selectedFeature}>
          Add Action
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AddActionDialog;