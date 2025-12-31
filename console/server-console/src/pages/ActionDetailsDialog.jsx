import React, { useState } from 'react';
import { Dialog, DialogActions, DialogContent, DialogTitle, Button, TextField, MenuItem, Checkbox, FormControl, InputLabel, Select, ListItemText } from '@mui/material';

const ActionDetailsDialog = ({ open, action, handleClose, authTypes }) => {
  const [selectedAuthTypes, setSelectedAuthTypes] = useState(action.authTypes || []);

  const [securityLevel, setSecurityLevel] = useState(action.securityLevel);

  const handleAuthTypesChange = (event) => {
    const {
      target: { value },
    } = event;
    setSelectedAuthTypes(typeof value === 'string' ? value.split(',') : value);
  };

  const handleSecurityLevelChange = (event) => {
    setSecurityLevel(event.target.value);
  };

  const handleSave = () => {
    console.log('Updated Auth Types:', selectedAuthTypes);
    console.log('Updated Security Level:', securityLevel);
    handleClose();
  };

  return (
    <Dialog open={open} onClose={handleClose}>
      <DialogTitle>Action Details</DialogTitle>
      <DialogContent>
        {/* Editable Auth Types */}
        <FormControl fullWidth margin="normal">
          <InputLabel>Auth Types</InputLabel>
          <Select multiple value={selectedAuthTypes} onChange={handleAuthTypesChange} renderValue={(selected) => selected.join(', ')}>
            {authTypes.map((authType) => (
              <MenuItem key={authType} value={authType}>
                <Checkbox checked={selectedAuthTypes.indexOf(authType) > -1} />
                <ListItemText primary={authType} />
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        {/* Editable Security Level */}
        <FormControl fullWidth margin="normal">
          <InputLabel>Security Level</InputLabel>
          <Select value={securityLevel} onChange={handleSecurityLevelChange}>
            <MenuItem value="PUBLIC">PUBLIC</MenuItem>
            <MenuItem value="PRIVATE">PRIVATE</MenuItem>
            <MenuItem value="PROTECTED">PROTECTED</MenuItem>
          </Select>
        </FormControl>

        {/* Non-editable fields */}
        <TextField fullWidth margin="normal" label="Action Name" value={action.name} InputProps={{readOnly: true,}}/>
        <TextField fullWidth margin="normal" label="Request Method" value={action.requestMethod} InputProps={{ readOnly: true,}}/>
        <TextField fullWidth margin="normal" label="Description" value={action.description} InputProps={{readOnly: true,}}/>
        
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>Cancel</Button>
        <Button onClick={handleSave} color="primary">
          Save
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ActionDetailsDialog;