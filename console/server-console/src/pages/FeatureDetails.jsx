import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { Box, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Button } from '@mui/material';

const FeatureDetails = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { feature, appConfig } = location.state || {};
  const { appId, id } = useParams();

  const handleActionClick = (action) => {
    navigate(`/action-details/${action.id}`, { state: { action, appId, appConfig }});
  };

  return (
    <Box
      sx={{
        width: '80vw',
        minHeight: '80vh',
        padding: '20px',
        backgroundColor: '#f5f5f5',
      }}>
      {feature && (
        <>
          <Typography variant="h6">Feature: {feature.name}</Typography>
          <Typography variant="subtitle1">Feature ID: {feature.featureId}</Typography>
          <Typography variant="subtitle1">Path: {feature.path}</Typography>
          <Typography variant="subtitle1">Description: {feature.description}</Typography>

          {/* Feature Actions Table */}
          <TableContainer component={Paper} sx={{ width: '100%', mt: 2 }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Action Name</TableCell>
                  <TableCell>Action ID</TableCell>
                  <TableCell>Security Level</TableCell>
                  <TableCell>Request Method</TableCell>
                  <TableCell>Description</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {feature.actions.map((action) => (
                  <TableRow key={action.id}>
                    <TableCell>{action.name}</TableCell>
                    <TableCell>{action.actionId}</TableCell>
                    <TableCell>{action.securityLevel}</TableCell>
                    <TableCell>{action.requestMethod}</TableCell>
                    <TableCell>{action.description}</TableCell>
                    <TableCell>
                      <Button variant="outlined" color="primary" onClick={() => handleActionClick(action)}>
                        Details
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      )}
    </Box>
  );
};

export default FeatureDetails;