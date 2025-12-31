import { Button, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TablePagination, IconButton, TextField, MenuItem, Dialog, DialogActions, DialogContent, DialogTitle, Box } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import { useState, useEffect } from 'react';
import api from '../services/api';

const CertificatePage = () => {
  const [certificates, setCertificates] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(5);
  const [totalRecords, setTotalRecords] = useState(0);
  const [openCreateDialog, setOpenCreateDialog] = useState(false);
  const [newCertificate, setNewCertificate] = useState({
    keystoreType: '',
    alias: '',
    issuer: '',
    subject: '',
    serialNumber: '',
    signatureAlgorithm: '',
    certificateType: '',
    hostName: '',
    validFrom: '',
    validTo: '',
    certificateChain: null,
    keystore: null,
    keystorePassword: '',
    keyPassword: '',
    countryName: '',
    stateName: '',
    localityName: '',
    organizationName: '',
    organizationalUnitName: '',
    emailAddress: ''
  });
  const [openUploadDialog, setOpenUploadDialog] = useState(false);
  const [openGenerateDialog, setOpenGenerateDialog] = useState(false);
  const [generateForm, setGenerateForm] = useState({
    certificateName: '',
    keystorePassword: '',
    keyPassword: '',
    keystoreType: 'PKCS12',
    alias: '',
    countryName: '',
    stateName: '',
    localityName: '',
    organizationName: '',
    organizationalUnitName: '',
    hostName: '',
    certificateType: '',
    emailAddress: '',
    validityDays: 365,
    signatureAlgorithm: 'SHA256withRSA'
  });

  useEffect(() => {
    fetchCertificates();
  }, [page, rowsPerPage]);

  const fetchCertificates = async () => {
    try {
      const response = await api.get(`/certs?pageNumber=${page}&pageSize=${rowsPerPage}`);
      const { responseData, totalRecords } = response.data;
      setCertificates(responseData);
      setTotalRecords(totalRecords);
    } catch (error) {
      console.error('Error fetching certificates:', error);
    }
  };

  const handleDelete = async (ids) => {
    try {
      await api.delete(`/certs?ids=${ids}`);
      fetchCertificates();
    } catch (error) {
      console.error('Error deleting certificate:', error);
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

  const handleCloseCreateDialog = () => setOpenUploadDialog(false);

  const handleCreateCertificate = async () => {
    try {
      const keystoreBase64 = newCertificate.keystore ? await toBase64(newCertificate.keystore) : null;
      const certificateChainBase64 = newCertificate.certificateChain ? await toBase64(newCertificate.certificateChain) : null;

      const formattedValidFrom = formatDateToISO(newCertificate.validFrom);
      const formattedValidTo = formatDateToISO(newCertificate.validTo);
  
      const payload = {
        certificateName: newCertificate.certificateName,
        keystoreType: newCertificate.keystoreType,
        alias: newCertificate.alias,
        issuer: newCertificate.issuer,
        subject: newCertificate.subject,
        serialNumber: newCertificate.serialNumber,
        signatureAlgorithm: newCertificate.signatureAlgorithm,
        certificateType: newCertificate.certificateType,
        hostName: newCertificate.hostName,
        validFrom: formattedValidFrom,
        validTo: formattedValidTo,
        keystore: keystoreBase64,
        keystorePassword: newCertificate.keystorePassword,
        keyPassword: newCertificate.keyPassword,
        certificateChain: certificateChainBase64,
        countryName: newCertificate.countryName,
        stateName: newCertificate.stateName,
        localityName: newCertificate.localityName,
        organizationName: newCertificate.organizationName,
        organizationalUnitName: newCertificate.organizationalUnitName,
        emailAddress: newCertificate.emailAddress,
      };
  
      // Send JSON payload
      const response = await api.post('/certs', payload);
      
      if (response.status === 200 || response.status === 201) {
        console.log('Certificate created successfully:', response.data);
        fetchCertificates();
        handleCloseCreateDialog();
      } else {
        console.error('Failed to create certificate, status:', response.status);
      }
    } catch (error) {
      console.error('Error creating certificate:', error.response ? error.response.data : error.message);
    }
  };  

  const toBase64 = (file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result.split(',')[1]);
      reader.onerror = (error) => reject(error);
    });
  }; 

  const formatDateToISO = (date) => {
    const d = new Date(date);
    return d.toISOString().split('.')[0];
  };

  const handleInputChange = (event) => {
    const { name, value, files } = event.target;
    if (files) {
      setNewCertificate({ ...newCertificate, [name]: files[0] });
    } else {
      setNewCertificate({ ...newCertificate, [name]: value });
    }
  };

  const handleGenerateChange = (e) => {
    const { name, value } = e.target;
    setGenerateForm(prev => ({ ...prev, [name]: value }));
  };
  
  const handleGenerateCertificate = async () => {
    try {
      const response = await api.post('/certs/generate', generateForm);
      alert('Certificate generated successfully!');
      setOpenGenerateDialog(false);
      fetchCertificates();
    } catch (error) {
      console.error('Error generating certificate:', error);
      alert('Failed to generate certificate');
    }
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
      <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
        Certificate List
      </Typography>
      <Button variant="contained" color="primary" onClick={() => setOpenUploadDialog(true)} sx={{ mr: 2 }}>
        Upload Certificate
      </Button>
      <Button variant="outlined" color="secondary" onClick={() => setOpenGenerateDialog(true)}>
        Generate Certificate
      </Button>
      
      </Box>
      <TableContainer component={Paper}>
        <Table sx={{ minWidth: 650 }} aria-label="certificate table">
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Alias</TableCell>
              <TableCell>Key Store Type</TableCell>
              <TableCell>Signature Algorithm</TableCell>
              <TableCell>Valid From</TableCell>
              <TableCell>Valid To</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {certificates.map((row) => (
              <TableRow key={row.id}>
                <TableCell>{row.certificateName}</TableCell>
                <TableCell>{row.alias}</TableCell>
                <TableCell>{row.keystoreType}</TableCell>
                <TableCell>{row.signatureAlgorithm}</TableCell>
                <TableCell>{new Date(row.validFrom).toLocaleDateString()}</TableCell>
                <TableCell>{new Date(row.validTo).toLocaleDateString()}</TableCell>
                <TableCell>{new Date() > new Date(row.validTo) ? 'Expired' : 'Active'}</TableCell>
                <TableCell>
                  <IconButton color="error" onClick={() => handleDelete(row.id)}>
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination rowsPerPageOptions={[5, 10, 25]} component="div" count={totalRecords} rowsPerPage={rowsPerPage} page={page} onPageChange={handleChangePage} onRowsPerPageChange={handleChangeRowsPerPage}/>

      {/* Create Certificate Dialog */}
      <Dialog open={openUploadDialog} onClose={() => setOpenUploadDialog(false)}>
        <DialogTitle>Upload Certificate</DialogTitle>
        <DialogContent>
          {/* Add form fields for each required input */}
          <TextField label="Certificate Name" name="certificateName" fullWidth margin="normal" value={newCertificate.certificateName} onChange={handleInputChange} required />
          <TextField label="Key Store Type" name="keystoreType" fullWidth margin="normal" value={newCertificate.keystoreType} onChange={handleInputChange} required />
          <TextField label="Alias" name="alias" fullWidth margin="normal" value={newCertificate.alias} onChange={handleInputChange} required />
          <TextField label="Issuer" name="issuer" fullWidth margin="normal" value={newCertificate.issuer} onChange={handleInputChange} required />
          <TextField label="Subject" name="subject" fullWidth margin="normal" value={newCertificate.subject} onChange={handleInputChange} required />
          <TextField label="Serial Number" name="serialNumber" fullWidth margin="normal" value={newCertificate.serialNumber} onChange={handleInputChange} required />
          <TextField label="Signature Algorithm" name="signatureAlgorithm" select fullWidth margin="normal" value={newCertificate.signatureAlgorithm} onChange={handleInputChange} required>
            {['RS256', 'HS256', 'AES'].map((option) => (
              <MenuItem key={option} value={option}>
                {option}
              </MenuItem>
            ))}
          </TextField>
          <TextField label="Certificate Type" name="certificateType" fullWidth margin="normal" value={newCertificate.certificateType} onChange={handleInputChange} required />
          <TextField label="Host Name" name="hostName" fullWidth margin="normal" value={newCertificate.hostName} onChange={handleInputChange} required />
          <TextField label="Valid From" name="validFrom" type="date" fullWidth margin="normal" InputLabelProps={{ shrink: true }} value={newCertificate.validFrom} onChange={handleInputChange} required/>
          <TextField label="Valid To" name="validTo" type="date" fullWidth margin="normal" InputLabelProps={{ shrink: true }} value={newCertificate.validTo} onChange={handleInputChange} required/>
          <TextField label="Certificate Chain" name="certificateChain" type="file" fullWidth margin="normal" onChange={handleInputChange} />
          <TextField label="Key Store" name="keystore" type="file" fullWidth margin="normal" onChange={handleInputChange} required />
          <TextField label="Key Store Password" name="keystorePassword" fullWidth margin="normal" value={newCertificate.keystorePassword} onChange={handleInputChange} required />
          <TextField label="Key Password" name="keyPassword" fullWidth margin="normal" value={newCertificate.keyPassword} onChange={handleInputChange} required />
          
          {/* Optional Address Fields */}
          <TextField label="Country Name" name="countryName" fullWidth margin="normal" value={newCertificate.countryName} onChange={handleInputChange} />
          <TextField label="State Name" name="stateName" fullWidth margin="normal" value={newCertificate.stateName} onChange={handleInputChange} />
          <TextField label="Locality Name" name="localityName" fullWidth margin="normal" value={newCertificate.localityName} onChange={handleInputChange} />
          <TextField label="Organization Name" name="organizationName" fullWidth margin="normal" value={newCertificate.organizationName} onChange={handleInputChange} />
          <TextField label="Organizational Unit Name" name="organizationalUnitName" fullWidth margin="normal" value={newCertificate.organizationalUnitName} onChange={handleInputChange} />
          <TextField label="Email Address" name="emailAddress" fullWidth margin="normal" value={newCertificate.emailAddress} onChange={handleInputChange} />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseCreateDialog}>Cancel</Button>
          <Button onClick={handleCreateCertificate} color="primary" variant="contained">
            Submit
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={openGenerateDialog} onClose={() => setOpenGenerateDialog(false)} fullWidth maxWidth="md">
        <DialogTitle>Generate New Certificate</DialogTitle>
        <DialogContent>
          <TextField fullWidth label="Certificate Name" name="certificateName" margin="normal" value={generateForm.certificateName} onChange={handleGenerateChange} />
          <TextField fullWidth label="Keystore Password" name="keystorePassword" margin="normal" value={generateForm.keystorePassword} onChange={handleGenerateChange} />
          <TextField fullWidth label="Key Password" name="keyPassword" margin="normal" value={generateForm.keyPassword} onChange={handleGenerateChange} />
          <TextField fullWidth label="Alias" name="alias" margin="normal" value={generateForm.alias} onChange={handleGenerateChange} />
          <TextField fullWidth label="Country" name="countryName" margin="normal" value={generateForm.countryName} onChange={handleGenerateChange} />
          <TextField fullWidth label="State" name="stateName" margin="normal" value={generateForm.stateName} onChange={handleGenerateChange} />
          <TextField fullWidth label="Locality" name="localityName" margin="normal" value={generateForm.localityName} onChange={handleGenerateChange} />
          <TextField fullWidth label="Organization" name="organizationName" margin="normal" value={generateForm.organizationName} onChange={handleGenerateChange} />
          <TextField fullWidth label="Org Unit" name="organizationalUnitName" margin="normal" value={generateForm.organizationalUnitName} onChange={handleGenerateChange} />
          <TextField fullWidth label="Host Name" name="hostName" margin="normal" value={generateForm.hostName} onChange={handleGenerateChange} />
          <TextField fullWidth label="Email Address" name="emailAddress" margin="normal" value={generateForm.emailAddress} onChange={handleGenerateChange} />
          <TextField fullWidth label="Certificate Type" name="certificateType" margin="normal" value={generateForm.certificateType} onChange={handleGenerateChange} />
          <TextField fullWidth label="Validity (days)" name="validityDays" type="number" margin="normal" value={generateForm.validityDays} onChange={handleGenerateChange} />
          <TextField fullWidth label="Signature Algorithm" name="signatureAlgorithm" select margin="normal" value={generateForm.signatureAlgorithm} onChange={handleGenerateChange}>
            <MenuItem value="SHA256withRSA">SHA256withRSA</MenuItem>
            <MenuItem value="SHA512withRSA">SHA512withRSA</MenuItem>
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenGenerateDialog(false)}>Cancel</Button>
          <Button variant="contained" color="primary" onClick={handleGenerateCertificate}>Generate</Button>
        </DialogActions>
      </Dialog>

    </Box>
  );
};

export default CertificatePage;