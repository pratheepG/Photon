import React, { useEffect, useState } from 'react';
import { Box, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, TablePagination, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const UserRolesPage = () => {
  const [roles, setRoles] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalRecords, setTotalRecords] = useState(0);
  const navigate = useNavigate(); // For navigation

  useEffect(() => {
    fetchRoles();
  }, [page, rowsPerPage]);

  const fetchRoles = async () => {
    try {
      const response = await api.get(`/role?pageNumber=${page}&pageSize=${rowsPerPage}`);
      if (response.data.success) {
        setRoles(response.data.responseData);
        setTotalRecords(response.data.totalRecords);
      }
    } catch (error) {
      console.error('Error fetching roles:', error);
    }
  };

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  // Function to handle row click and navigate to RoleDetailsPage
  const handleRoleClick = (role) => {
    navigate('/role-details', { state: { role } }); // Passing the role as state to the RoleDetailsPage
  };

  const handleCreateNewClick = () => {
    navigate('/user-roles/create'); // Navigate to the Create Role Page
  };

  const calculateTotalFeatureActions = (featureActions) => {
    if(!featureActions) return 0;
    return Object.values(featureActions).reduce((acc, actions) => acc + actions.length, 0);
  };

  return (
    <Box
      sx={{
        width: '80vw', // Full width of the viewport
        minHeight: '80vh', // Full height of the viewport
        padding: '20px',
        backgroundColor: '#f5f5f5', // Optional background color
      }}
    >
      {/* Header Section with Title and Create New Button */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h6">
          User Roles
        </Typography>

        {/* Create New Button */}
        <Button variant="contained" color="primary" onClick={handleCreateNewClick}>
          Create New
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Role Name</TableCell>
              <TableCell>IDP</TableCell>
              <TableCell>Description</TableCell>
              <TableCell>Total Feature Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {roles.map((role) => (
              <TableRow key={role.id} hover onClick={() => handleRoleClick(role)} style={{ cursor: 'pointer' }}>
                <TableCell>{role.name}</TableCell>
                <TableCell>{role.idp}</TableCell>
                <TableCell>{role.description}</TableCell>
                <TableCell>{calculateTotalFeatureActions(role.featureActions)}</TableCell>
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
        rowsPerPageOptions={[5, 10, 20]}
      />
    </Box>
  );
};

export default UserRolesPage;