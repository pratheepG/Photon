
import { useState, useEffect } from 'react';
import { Box, Button, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TablePagination, TextField, MenuItem, Dialog, DialogActions, DialogContent, DialogTitle, FormControl, FormLabel, RadioGroup, FormControlLabel, Radio } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import api from '../services/api.jsx';
import { Link } from 'react-router-dom';

const AuthenticationPage = () => {
  const [authTypes, setAuthTypes] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(5);
  const [totalRecords, setTotalRecords] = useState(0);
  const [openCreateDialog, setOpenCreateDialog] = useState(false);
  const [authAdaptorList, setAuthAdaptorList] = useState([]);
  const [certificates, setCertificates] = useState([]);
  const [formData, setFormData] = useState({
    authAdapter: '',
    certificate: '',
    name: '',
    id: '',
    isActive: true,
  });

  useEffect(() => {
    fetchAuthTypes();
    fetchMetaData();
  }, [page, rowsPerPage]);

  const fetchAuthTypes = async () => {
    try {
      const response = await api.get(`/auth-type?pageNumber=${page}&pageSize=${rowsPerPage}`);
      const { responseData, totalRecords } = response.data;
      setAuthTypes(responseData);
      setTotalRecords(totalRecords);
    } catch (error) {
      console.error('Error fetching authentication types:', error);
    }
  };

  const fetchMetaData = async () => {
    try {
      const response = await api.get('/identity-meta');
      setAuthAdaptorList(response.data.responseData.authAdaptor);

      const certResponse = await api.get('/certs?pageNumber=0&pageSize=100'); // Fetch all certificates
      setCertificates(certResponse.data.responseData);
    } catch (error) {
      console.error('Error fetching metadata:', error);
    }
  };

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleOpenCreateDialog = () => {
    setOpenCreateDialog(true);
  };

  const handleCloseCreateDialog = () => {
    setOpenCreateDialog(false);
  };

  const handleInputChange = (event) => {
    const { name, value } = event.target;
    
    if (name === 'name') {
      // Allow only alphabets and spaces, no numeric or special characters
      const regex = /^[A-Za-z\s]*$/;
      if (regex.test(value)) {
        setFormData({
          ...formData,
          [name]: value,
          id: value.toUpperCase().replace(/\s+/g, '_'), // Automatically generate ID
        });
      }
    } else {
      setFormData({
        ...formData,
        [name]: value,
      });
    }
  };

  const handleIsActiveChange = (event) => {
    setFormData({
      ...formData,
      isActive: event.target.value === 'true',
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      const payload = {
        id: formData.id,
        name: formData.name,
        description: `Authentication using ${formData.name}`,
        isActive: formData.isActive,
        authAdapter: formData.authAdapter,
        certificate: {
            id: formData.certificate
        }
      };

      await api.post('/auth-type', payload, {
        headers: {
          'Content-Type': 'application/json',
        },
      });

      alert('Authentication Type created successfully!');
      fetchAuthTypes(); // Refresh the list after creation
      handleCloseCreateDialog(); // Close the dialog
    } catch (error) {
      console.error('Error creating Authentication Type:', error);
      alert('Failed to create Authentication Type.');
    }
  };

  return (
    <Box
      sx={{
        width: '80vw', // Full width of the viewport
        minHeight: '80vh', // Full height of the viewport
        padding: '20px',
        backgroundColor: '#f5f5f5', // Optional background color
      }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
      <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
        Authentication Types
      </Typography>
      <Button
        variant="contained"
        color="primary"
        style={{ float: 'right', marginBottom: '16px' }}
        onClick={handleOpenCreateDialog}
        startIcon={<AddIcon />}>
        Create New
      </Button>
      </Box>
      <TableContainer component={Paper}>
        <Table sx={{ minWidth: 650 }} aria-label="authentication types table">
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Auth Id</TableCell>
              <TableCell>Description</TableCell>
              <TableCell>Adapter</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {authTypes.map((row) => (
              <TableRow key={row.id}>
                <TableCell>{row.name}</TableCell>
                <TableCell>{row.id}</TableCell>
                <TableCell>{row.description}</TableCell>
                <TableCell>{row.authAdapter}</TableCell>
                <TableCell>{row.isActive ? 'Active' : 'Inactive'}</TableCell>
                <TableCell>
                  <Button component={Link} to={`/auth-types/${row.id}`} variant="outlined">
                    Details
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        rowsPerPageOptions={[5, 10, 25]}
        component="div"
        count={totalRecords}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />

      {/* Create Authentication Type Dialog */}
      <Dialog open={openCreateDialog} onClose={handleCloseCreateDialog}>
        <DialogTitle>Create Authentication Type</DialogTitle>
        <DialogContent>
          <FormControl fullWidth margin="normal">
            <TextField select 
            label="Select Adaptor" 
            name="authAdapter" 
            value={formData.authAdapter} 
            onChange={handleInputChange} required>
              {Object.keys(authAdaptorList).map((adapter) => (
                <MenuItem key={adapter} value={adapter}>
                  {adapter}
                </MenuItem>
              ))}
            </TextField>
          </FormControl>

          <FormControl fullWidth margin="normal">
            <TextField
              select
              label="Select Certificate"
              name="certificate"
              value={formData.certificate}
              onChange={handleInputChange}
              required
            >
              {certificates.map((cert) => (
                <MenuItem key={cert.id} value={cert.id}>
                  {cert.alias}
                </MenuItem>
              ))}
            </TextField>
          </FormControl>

          <FormControl fullWidth margin="normal">
            <TextField
              label="Enter Auth Type Name"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
              required
            />
          </FormControl>

          <FormControl fullWidth margin="normal">
            <TextField
              label="Auth Type ID"
              name="id"
              value={formData.id}
              InputProps={{
                readOnly: true,
              }}
            />
          </FormControl>

          <FormControl component="fieldset" margin="normal">
            <FormLabel component="legend">Is Active</FormLabel>
            <RadioGroup row name="isActive" value={formData.isActive.toString()} onChange={handleIsActiveChange}>
              <FormControlLabel value="true" control={<Radio />} label="Enabled" />
              <FormControlLabel value="false" control={<Radio />} label="Disabled" />
            </RadioGroup>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseCreateDialog}>Cancel</Button>
          <Button onClick={handleSubmit} color="primary" variant="contained">
            Submit
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AuthenticationPage;