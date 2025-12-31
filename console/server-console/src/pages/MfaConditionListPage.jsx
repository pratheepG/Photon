import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Button, TablePagination } from '@mui/material';
import api from '../services/api';

const MfaConditionListPage = () => {
  const navigate = useNavigate();
  const [mfaConditions, setMfaConditions] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalRecords, setTotalRecords] = useState(0);

  useEffect(() => {
    fetchMfaConditions(page, rowsPerPage);
  }, [page, rowsPerPage]);

  const fetchMfaConditions = async (pageNumber, pageSize) => {
    try {
      const response = await api.get(`/mfa-condition?pageNumber=${pageNumber}&pageSize=${pageSize}`);
      if (response.data.success) {
        setMfaConditions(response.data.responseData);
        setTotalRecords(response.data.totalRecords);
      }
    } catch (error) {
      console.error('Error fetching MFA conditions:', error);
    }
  };

  const handleChangePage = (_, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleViewDetails = (condition) => {
    navigate(`/mfa-condition/${condition.id}`, { state: { condition } });
  };

  const handleCreateNew = () => {
    navigate('/mfa-condition/create');
  };

  return (
    <Box
    sx={{
      width: '80vw', // Full width of the viewport
      minHeight: '80vh', // Full height of the viewport
      padding: '20px',
      backgroundColor: '#f5f5f5', // Optional background color
    }}>
      <Typography variant="h6" sx={{ mb: 2 }}>MFA Conditions</Typography>

      <Button variant="contained" color="primary" sx={{ mb: 3 }} onClick={handleCreateNew}>
        Create New Condition
      </Button>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Description</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {mfaConditions.map((condition) => (
              <TableRow key={condition.id}>
                <TableCell>{condition.name}</TableCell>
                <TableCell>{condition.description}</TableCell>
                <TableCell>
                  <Button variant="outlined" onClick={() => handleViewDetails(condition)}>
                    View Details
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <TablePagination
        component="div"
        count={totalRecords}
        page={page}
        onPageChange={handleChangePage}
        rowsPerPage={rowsPerPage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />
    </Box>
  );
};

export default MfaConditionListPage;