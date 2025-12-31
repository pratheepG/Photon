import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Box, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Button } from '@mui/material';
import SettingsIcon from '@mui/icons-material/Settings';
import api from '../services/api';

const ApiManagerAppDetails = () => {
  const { appId } = useParams();
  const [appDetails, setAppDetails] = useState(null);
  const [appConfig, setAppConfig] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    fetchAppDetails();
  }, [appId]);

  const fetchAppDetails = async () => {
    let appConfigResponse = null;
    let appDetailsResponse = null;

    try {
      // 1. Fetch App Details (CRITICAL step)
      appDetailsResponse = await api.get(`/api-manager/services/${appId}`);
      
    } catch (error) {
      // If the primary app details fail, log and exit
      console.error('Error fetching primary app details:', error);
      return;
    }

    // Check if the primary details fetch was successful before proceeding
    if (appDetailsResponse && appDetailsResponse.data.success) {
      
      // 2. Fetch App Config (NON-CRITICAL step - isolated try/catch)
      try {
        appConfigResponse = await api.get(`/config-properties?applicationId=IDENTITY-API-CONFIG`);
      } catch (error) {
        // Axios throws an error on 4xx/5xx status codes.
        // We check if the error is a 404 (or the desired 1006 code) and treat it as a non-fatal config failure.
        if (error.response && error.response.status === 404) {
          console.warn('Config properties not found (404). Proceeding without config.');
          appConfigResponse = null; // Ensure the variable is clear
        } else {
          // Log other config errors but still proceed with app details
          console.error('Non-404 error fetching config properties:', error);
          appConfigResponse = null;
        }
      }

      // 3. Set State based on results
      setAppDetails(appDetailsResponse.data.responseData[0]);

      if (appConfigResponse && (appConfigResponse.data.success || appConfigResponse.data.errCode === 1006)) {
        setAppConfig( (appConfigResponse.data.errCode === 1006)?{}:appConfigResponse.data.responseData.config);
      } else {
        // This handles null appConfigResponse (from 404/non-fatal errors) 
        // or a config response that failed the success check.
        setAppConfig(null);
      }
    }
  };

  const handleFeatureClick = (feature) => {
    // Passing appConfig in the state
    navigate(`/api-manager/${appId}/features/${feature.id}`, { state: { feature, appConfig } });
  };

  const handleApiConfigClick = () => {
    navigate(`/api-manager/${appDetails.id}/config`);
  };  

  const handleLoggerClick = () => {
    // Assuming a route for logger configuration
    navigate(`/api-manager/${appDetails.id}/logger`);
  };

  return (
    <Box sx={{width: '80vw', minHeight: '80vh', padding: '20px', backgroundColor: '#f5f5f5'}}>
      {appDetails && (
        <>
          {/* Header Section */}
          <Box sx={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>

            {/* App Details */}
            <Box>
              <Typography variant="h6">{appDetails.name}</Typography>
              <Typography variant="subtitle1">Client ID: {appDetails.clientId}</Typography>
              <Typography variant="subtitle1">Client Secret: ********</Typography> {/* Masked */}
              <Typography variant="subtitle1">App ID: {appDetails.id}</Typography>
              <Typography variant="subtitle1">App Name: {appDetails.name}</Typography>
              {appConfig === null && (
                <Typography variant="caption" color="error">
                  (Configuration data unavailable)
                </Typography>
              )}
            </Box>

            {/* API Config Button */}
            <Box sx={{display: 'flex', gap: 2, alignItems: 'center' }}>
            <Button variant="outlined" color="primary" startIcon={<SettingsIcon />} onClick={handleApiConfigClick} sx={{ alignSelf: 'flex-start' }}>
              API Config
            </Button>
            {/* Using the new handler for Logger button */}
            <Button variant="outlined" color="primary" startIcon={<SettingsIcon />} onClick={handleLoggerClick} sx={{ alignSelf: 'flex-start' }}>
              Logger
            </Button>
            </Box>
          </Box>

          {/* Features Table */}
          <TableContainer component={Paper} sx={{ mt: 3 }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Feature Name</TableCell>
                  <TableCell>Feature ID</TableCell>
                  <TableCell>Feature Path</TableCell>
                  <TableCell>Description</TableCell>
                  <TableCell>Total Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {appDetails.features.map((feature) => (
                  <TableRow key={feature.id} onClick={() => handleFeatureClick(feature)} style={{ cursor: 'pointer' }} hover>
                    <TableCell>{feature.name}</TableCell>
                    <TableCell>{feature.featureId}</TableCell>
                    <TableCell>{feature.path}</TableCell>
                    <TableCell>{feature.description}</TableCell>
                    <TableCell>{feature.actions ? feature.actions.length : 0}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      )}
      {!appDetails && <Box sx={{ p: 4, textAlign: 'center' }}><Typography>Loading application details...</Typography></Box>}
    </Box>
  );
};

export default ApiManagerAppDetails;