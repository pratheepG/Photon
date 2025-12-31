import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Box, 
  Typography, 
  TextField, 
  Button, 
  IconButton, 
  MenuItem, 
  Select, 
  FormControl, 
  InputLabel 
} from '@mui/material';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import DeleteIcon from '@mui/icons-material/Delete';
import api from '../services/api';

const MfaConditionCreatePage = () => {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [groups, setGroups] = useState([
    { operator: 'OR', items: [{ operator: 'AND', condition: '' }] },
  ]);

  const conditionOptions = [
    "HIGH_RISK_IP", "EXPIRED_MFA", "NEW_DEVICE", "UNFAMILIAR_LOCATION"
  ];

  const handleAddGroup = () => {
    setGroups([...groups, { operator: 'OR', items: [{ operator: 'AND', condition: '' }] }]);
  };

  const handleRemoveGroup = (groupIndex) => {
    const updatedGroups = groups.filter((_, index) => index !== groupIndex);
    setGroups(updatedGroups);
  };

  const handleAddCondition = (groupIndex) => {
    const updatedGroups = [...groups];
    updatedGroups[groupIndex].items.push({ operator: 'AND', condition: '' });
    setGroups(updatedGroups);
  };

  const handleRemoveCondition = (groupIndex, itemIndex) => {
    const updatedGroups = [...groups];
    updatedGroups[groupIndex].items = updatedGroups[groupIndex].items.filter((_, idx) => idx !== itemIndex);
    if (updatedGroups[groupIndex].items.length === 0) {
      handleRemoveGroup(groupIndex);
    } else {
      setGroups(updatedGroups);
    }
  };

  const handleInputChange = (groupIndex, itemIndex, field, value) => {
    const updatedGroups = [...groups];
    updatedGroups[groupIndex].items[itemIndex][field] = value;
    setGroups(updatedGroups);
  };

  const handleSubmit = async () => {
    const payload = { name, description, groups };
    try {
      await api.post('/mfa-condition', payload);
      alert('MFA Condition Created Successfully');
      navigate('/mfa-condition');
    } catch (error) {
      console.error('Error creating MFA condition:', error);
      alert('Failed to create MFA condition');
    }
  };

  return (
    <Box sx={{ width: '80vw', minHeight: '80vh', padding: '20px', backgroundColor: '#f5f5f5' }}>
      <Typography variant="h5" sx={{ mb: 2 }}>Create MFA Condition</Typography>

      {/* Name & Description */}
      <TextField 
        label="Name" 
        fullWidth 
        value={name} 
        onChange={(e) => setName(e.target.value)} 
        sx={{ mt: 2 }} 
      />
      <TextField 
        label="Description" 
        fullWidth 
        value={description} 
        onChange={(e) => setDescription(e.target.value)} 
        sx={{ mt: 2 }} 
      />

      {/* Groups Section */}
      {groups.map((group, groupIndex) => (
        <Box key={groupIndex} sx={{ mt: 3, padding: 2, border: '1px solid #ddd', borderRadius: '8px' }}>
          <Typography variant="h6">Group {groupIndex + 1}</Typography>

          {group.items.map((item, itemIndex) => (
            <Box key={itemIndex} sx={{ display: 'flex', alignItems: 'center', gap: 2, mt: 2 }}>
              <FormControl sx={{ minWidth: 150 }}>
                <InputLabel>Condition</InputLabel>
                <Select 
                  value={item.condition} 
                  onChange={(e) => handleInputChange(groupIndex, itemIndex, 'condition', e.target.value)}
                >
                  {conditionOptions.map((option) => (
                    <MenuItem key={option} value={option}>{option}</MenuItem>
                  ))}
                </Select>
              </FormControl>

              <IconButton color="error" onClick={() => handleRemoveCondition(groupIndex, itemIndex)}>
                <DeleteIcon />
              </IconButton>
            </Box>
          ))}

          <Button 
            startIcon={<AddCircleOutlineIcon />} 
            onClick={() => handleAddCondition(groupIndex)} 
            sx={{ mt: 2 }}
          >
            Add Condition
          </Button>

          {groups.length > 1 && (
            <Button color="error" sx={{ mt: 2, ml: 2 }} onClick={() => handleRemoveGroup(groupIndex)}>
              Remove Group
            </Button>
          )}
        </Box>
      ))}

      <Button 
        startIcon={<AddCircleOutlineIcon />} 
        onClick={handleAddGroup} 
        sx={{ mt: 3 }}
      >
        Add Group
      </Button>

      {/* Submit Button */}
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 4 }}>
        <Button variant="contained" color="primary" onClick={handleSubmit}>
          Create MFA Condition
        </Button>
      </Box>
    </Box>
  );
};

export default MfaConditionCreatePage;