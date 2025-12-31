import { useState, useEffect } from 'react';
import { Box, Button, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TablePagination } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { Link } from 'react-router-dom';
import api from '../../services/api';

const AuthenticationPage = () => {
  const [authTypes, setAuthTypes] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(5);
  const [totalRecords, setTotalRecords] = useState(0);

  useEffect(() => {
    fetchAuthTypes();
  }, [page, rowsPerPage]);

  const fetchAuthTypes = async () => {
    try {
      const response = await api.get(`/auth-type?pageNumber=${page}&pageSize=${rowsPerPage}`);
      const { responseData, totalRecords } = response.data;
      setAuthTypes(responseData || []);
      setTotalRecords(totalRecords ?? (responseData || []).length);
    } catch (error) {
      console.error('Error fetching authentication types:', error);
    }
  };

  const handleChangePage = (event, newPage) => setPage(newPage);
  const handleChangeRowsPerPage = (event) => { setRowsPerPage(parseInt(event.target.value, 10)); setPage(0); };

  return (
    <Box sx={{ width: '80vw', minHeight: '80vh', padding: '20px', backgroundColor: '#f5f5f5' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h6">Authentication Types</Typography>
        <Button component={Link} to="/auth-types/create" variant="contained" startIcon={<AddIcon />}>Create New</Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
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
                  <Button component={Link} to={`/auth-types/${row.id}`} variant="outlined">Details</Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <TablePagination rowsPerPageOptions={[5, 10, 25]} component="div" count={totalRecords} rowsPerPage={rowsPerPage} page={page} onPageChange={handleChangePage} onRowsPerPageChange={handleChangeRowsPerPage} />
    </Box>
  );
};

export default AuthenticationPage;