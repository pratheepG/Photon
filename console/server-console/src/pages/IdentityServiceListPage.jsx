import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Button,
  TablePagination,
  Chip,
} from '@mui/material';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import api from '../services/api';

const IdentityServiceListPage = () => {
  const navigate = useNavigate();
  const [services, setServices] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalRecords, setTotalRecords] = useState(0);

  // Fetch identity services on component mount or page/rows change
  useEffect(() => {
    fetchIdentityServices(page, rowsPerPage);
  }, [page, rowsPerPage]);

  // API call to fetch Identity Services
  const fetchIdentityServices = async (pageNumber, pageSize) => {
    try {
      const response = await api.get(`/identity-provider?pageNumber=${pageNumber}&pageSize=${pageSize}`);
      if (response.data.success) {
        setServices(response.data.responseData);
        setTotalRecords(response.data.totalRecords);
      } else {
        alert('Failed to fetch identity services.');
      }
    } catch (error) {
      console.error('Error fetching Identity Services:', error);
    }
  };

  // Handle page change
  const handleChangePage = (_, newPage) => {
    setPage(newPage);
  };

  // Handle rows per page change
  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  // Navigate to Identity Service Details page
  const handleViewDetails = (service) => {
    navigate(`/identity-service/${service.id}`);
  };

  // Navigate to Create Identity Service page
  const handleCreateNew = () => {
    navigate('/identity-service/create');
  };

  return (
    <Box
      sx={{
        width: '80vw',
        minHeight: '80vh',
        padding: '20px',
        backgroundColor: '#f5f5f5',
        fontFamily: '"Roboto", sans-serif',
      }}
    >
      {/* Page Header */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          mb: 2,
        }}
      >
        <Typography variant="h4" sx={{ color: '#1e3a8a', fontWeight: 'bold' }}>
          Identity Services
        </Typography>

        <Button
          variant="contained"
          color="primary"
          startIcon={<AddCircleIcon />}
          onClick={handleCreateNew}
          sx={{
            backgroundColor: '#4CAF50',
            '&:hover': { backgroundColor: '#45A049' },
            fontWeight: 'bold',
          }}
        >
          Create New
        </Button>
      </Box>

      {/* Identity Services Table */}
      <TableContainer component={Paper} sx={{ borderRadius: '12px', boxShadow: 3 }}>
        <Table>
          {/* Table Head */}
          <TableHead sx={{ backgroundColor: '#1976D2' }}>
            <TableRow>
              <TableCell sx={{ color: 'white', fontWeight: 'bold' }}>Name</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 'bold' }}>Description</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 'bold' }}>Status</TableCell>
              <TableCell sx={{ color: 'white', fontWeight: 'bold', textAlign: 'center' }}>Actions</TableCell>
            </TableRow>
          </TableHead>

          {/* Table Body */}
          <TableBody>
            {services.length === 0 ? (
              <TableRow>
                <TableCell colSpan={4} align="center">
                  No Identity Services available.
                </TableCell>
              </TableRow>
            ) : (
              services.map((service) => (
                <TableRow
                  key={service.id}
                  hover
                  sx={{
                    '&:nth-of-type(odd)': { backgroundColor: '#E3F2FD' },
                    '&:hover': { backgroundColor: '#BBDEFB' },
                  }}
                >
                  <TableCell sx={{ fontWeight: 'medium' }}>{service.name}</TableCell>
                  <TableCell>{service.description}</TableCell>
                  <TableCell>
                    <Chip
                      label={service.isActive ? 'ACTIVE' : 'INACTIVE'}
                      color={service.isActive ? 'success' : 'error'}
                      variant="outlined"
                      sx={{ fontWeight: 'bold' }}
                    />
                  </TableCell>
                  <TableCell sx={{ textAlign: 'center' }}>
                    <Button
                      variant="outlined"
                      size="small"
                      onClick={() => handleViewDetails(service)}
                      sx={{
                        borderColor: '#1565C0',
                        color: '#1565C0',
                        '&:hover': { backgroundColor: '#1565C0', color: 'white' },
                      }}
                    >
                      View Details
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Pagination */}
      <TablePagination
        component="div"
        count={totalRecords}
        page={page}
        onPageChange={handleChangePage}
        rowsPerPage={rowsPerPage}
        onRowsPerPageChange={handleChangeRowsPerPage}
        sx={{
          mt: 2,
          '.MuiTablePagination-root': { color: '#1e88e5' },
          '.MuiSvgIcon-root': { color: '#1e88e5' },
        }}
      />
    </Box>
  );
};

export default IdentityServiceListPage;