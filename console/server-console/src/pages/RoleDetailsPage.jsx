import React, { useState } from 'react';
import { Box, Typography, Switch, Table, TableBody, TableCell, TableContainer, TableRow, Paper, Button, List, ListItem, ListItemText, Collapse } from '@mui/material';
import ExpandLess from '@mui/icons-material/ExpandLess';
import ExpandMore from '@mui/icons-material/ExpandMore';
import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';
import UndoIcon from '@mui/icons-material/Undo';
import AddActionDialog from './AddActionDialog';

const RoleDetailsPage = ({ role }) => {
  const [isActive, setIsActive] = useState(role.active);
  const [openAuthTypes, setOpenAuthTypes] = useState({});
  const [openFeatures, setOpenFeatures] = useState({});
  const [openDialog, setOpenDialog] = useState(false);

  const groupFeatureActions = (featureActions) => {
    const groupedActions = {};

    Object.keys(featureActions).forEach((authType) => {
      if (!groupedActions[authType]) {
        groupedActions[authType] = {};
      }
      featureActions[authType].forEach((action) => {
        const [feature, featureAction] = action.split(':');
        if (!groupedActions[authType][feature]) {
          groupedActions[authType][feature] = [];
        }
        groupedActions[authType][feature].push(featureAction);
      });
    });

    return groupedActions;
  };

  const groupedFeatureActions = groupFeatureActions(role.featureActions);

  const handleActiveToggle = () => {
    setIsActive((prevState) => !prevState);
  };

  const handleEdit = () => {
    alert('Edit Role functionality here.');
  };

  const handleDelete = () => {
    alert('Delete Role functionality here.');
  };

  const handleAddFeatureActionClick = () => {
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleAuthTypeClick = (authType) => {
    setOpenAuthTypes((prevState) => ({
      ...prevState,
      [authType]: !prevState[authType],
    }));
  };

  const handleFeatureClick = (authType, feature) => {
    setOpenFeatures((prevState) => ({
      ...prevState,
      [`${authType}_${feature}`]: !prevState[`${authType}_${feature}`],
    }));
  };

  return (
    <Box
      sx={{
        width: '80vw',
        minHeight: '80vh',
        padding: '20px',
        backgroundColor: '#f5f5f5',
      }}>
      
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6">Role Details</Typography>
      </Box>

      {/* Top Section: Role Details */}
      <TableContainer component={Paper} sx={{ mb: 4 }}>
        <Table>
          <TableBody>
            <TableRow>
              <TableCell variant="head">Role ID</TableCell>
              <TableCell>{role.roleId}</TableCell>
            </TableRow>
            <TableRow>
              <TableCell variant="head">IDP</TableCell>
              <TableCell>{role.idp}</TableCell>
            </TableRow>
            <TableRow>
              <TableCell variant="head">Role Name</TableCell>
              <TableCell>{role.name}</TableCell>
            </TableRow>
            <TableRow>
              <TableCell variant="head">Description</TableCell>
              <TableCell>{role.description}</TableCell>
            </TableRow>
            <TableRow>
              <TableCell variant="head">Active</TableCell>
              <TableCell>
                <Switch checked={isActive} onChange={handleActiveToggle} />
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </TableContainer>

      {/* Second Section: Feature Actions */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
      <Typography variant="h6" sx={{ mb: 2 }}>
        Feature Actions
      </Typography>
      <Button
          variant="contained"
          color="primary"
          style={{ float: 'right', marginBottom: '16px' }}
          onClick={handleAddFeatureActionClick}
          startIcon={<AddIcon />}>
          Add
        </Button>
      </Box>

      <Box sx={{ backgroundColor: 'white', padding: 2, borderRadius: '4px' }}>
        <List>
          {Object.keys(groupedFeatureActions).map((authType) => (
            <React.Fragment key={authType}>
              {/* Auth Type Header */}
              <ListItem button onClick={() => handleAuthTypeClick(authType)}>
                <ListItemText primary={authType} sx={{ fontWeight: 'bold' }} />
                {openAuthTypes[authType] ? <ExpandLess /> : <ExpandMore />}
              </ListItem>

              <Collapse in={openAuthTypes[authType]} timeout="auto" unmountOnExit>
                {Object.keys(groupedFeatureActions[authType]).map((feature) => (
                  <React.Fragment key={feature}>
                    {/* Feature Sub-Header */}
                    <ListItem button sx={{ pl: 4 }} onClick={() => handleFeatureClick(authType, feature)}>
                      <ListItemText primary={feature} sx={{ fontWeight: 'bold' }} />
                      {openFeatures[`${authType}_${feature}`] ? <ExpandLess /> : <ExpandMore />}
                    </ListItem>

                    {/* Actions List */}
                    <Collapse in={openFeatures[`${authType}_${feature}`]} timeout="auto" unmountOnExit>
                      <List component="div" disablePadding>
                        {groupedFeatureActions[authType][feature].map((action, index) => (
                          <ListItem key={index} sx={{ pl: 8 }}>
                            <ListItemText primary={action} />
                          </ListItem>
                        ))}
                      </List>
                    </Collapse>
                  </React.Fragment>
                ))}
              </Collapse>
            </React.Fragment>
          ))}
        </List>
      </Box>

      {/* Bottom Section: Edit and Delete Buttons */}
      <Box sx={{
        display: 'flex',
        justifyContent: 'flex-end',
        position: 'fixed',
        bottom: 0,
        right: 0,
        width: '100%',
        backgroundColor: 'white',
        padding: '16px',
        boxShadow: '0 -2px 5px rgba(0, 0, 0, 0.1)',
      }}>
          <Button variant="contained" color="primary" sx={{ mr: 2 }} onClick={handleEdit}>
            Edit
          </Button>
          <Button variant="contained" color="error" onClick={handleDelete}>
            Delete
          </Button>
      </Box>

      <AddActionDialog open={openDialog} onClose={handleCloseDialog} role={role}/>
    </Box>
  );
};

export default RoleDetailsPage;