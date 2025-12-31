import React from 'react';
import { useLocation } from 'react-router-dom';
import { Box, Typography, Paper, List, ListItem, ListItemText } from '@mui/material';

const MfaConditionDetailsPage = () => {
  const location = useLocation();
  const { condition } = location.state || {};

  return (
    <Box
    sx={{
      width: '80vw', // Full width of the viewport
      minHeight: '80vh', // Full height of the viewport
      padding: '20px',
      backgroundColor: '#f5f5f5', // Optional background color
    }}>
      <Typography variant="h6">MFA Condition Details</Typography>

      <Paper sx={{ padding: '16px', mt: 3 }}>
        <Typography variant="subtitle1"><strong>Name:</strong> {condition.name}</Typography>
        <Typography variant="subtitle1"><strong>Description:</strong> {condition.description}</Typography>

        {condition.groups.map((group) => (
          <Box key={group.id} sx={{ mt: 3 }}>
            <Typography variant="subtitle1"><strong>Group Operator:</strong> {group.operator}</Typography>

            <List>
              {group.items.map((item) => (
                <ListItem key={item.id}>
                  <ListItemText primary={`Condition: ${item.condition}`} secondary={`Operator: ${item.operator}`} />
                </ListItem>
              ))}
            </List>
          </Box>
        ))}
      </Paper>
    </Box>
  );
};

export default MfaConditionDetailsPage;