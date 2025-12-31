import { useEffect, useState } from 'react';
import {
  Box, Paper, Typography, Button, Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, TablePagination, CircularProgress, Alert
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';

export default function UsersListPage() {
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalRecords, setTotalRecords] = useState(0);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchUsers(page, rowsPerPage);
  }, []);

  useEffect(() => {
    fetchUsers(page, rowsPerPage);
  }, [page, rowsPerPage]);

  const fetchUsers = async (p = 0, size = 10) => {
    setLoading(true);
    setErrorMsg('');
    try {
      const resp = await api.get(`/user/get-user?pageNumber=${p}&pageSize=${size}`);
      if (resp.data && resp.data.success) {
        setUsers(resp.data.responseData || []);
        setTotalRecords(resp.data.totalRecords ?? (resp.data.responseData || []).length);
      } else {
        setUsers([]);
        setTotalRecords(0);
      }
    } catch (err) {
      console.error('Failed to fetch users', err);
      setErrorMsg('Failed to load users.');
      setUsers([]);
      setTotalRecords(0);
    } finally {
      setLoading(false);
    }
  };

  const handleRowClick = (user) => {
    if (!user?.userId) return;
    navigate(`/users/${user.userId}`, { state: { user } });
  };

  return (
    <Box sx={{ width: '90vw', minHeight: '80vh', padding: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4">Users</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/users/create')}>
          Create New User
        </Button>
      </Box>

      <Paper sx={{ p: 2 }}>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            {errorMsg && <Alert severity="error" sx={{ mb: 2 }}>{errorMsg}</Alert>}

            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>User ID</TableCell>
                    <TableCell>Name</TableCell>
                    <TableCell>User Name</TableCell>
                    <TableCell>Phone</TableCell>
                    <TableCell>Sex</TableCell>
                    <TableCell>Roles</TableCell>
                    <TableCell>Created On</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {users.map((u) => (
                    <TableRow
                      key={u.userId || u.userName}
                      hover
                      sx={{ cursor: 'pointer' }}
                      onClick={() => handleRowClick(u)} // 👈 navigation added here
                    >
                      <TableCell>{u.userId || '-'}</TableCell>
                      <TableCell>{`${u.firstName || ''} ${u.lastName || ''}`.trim()}</TableCell>
                      <TableCell>{u.userName || '-'}</TableCell>
                      <TableCell>{u.primaryPhoneNumber || u.phoneNumber || '-'}</TableCell>
                      <TableCell>{u.sex || '-'}</TableCell>
                      <TableCell>{(u.roles || []).map(r => r.name || r.roleId).join(', ')}</TableCell>
                      <TableCell>{u.createdOn ? new Date(u.createdOn).toLocaleString() : '-'}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>

            <TablePagination
              component="div"
              count={totalRecords}
              page={page}
              onPageChange={(_, newPage) => setPage(newPage)}
              rowsPerPage={rowsPerPage}
              onRowsPerPageChange={(e) => {
                setRowsPerPage(parseInt(e.target.value, 10));
                setPage(0);
              }}
              rowsPerPageOptions={[5, 10, 25]}
            />
          </>
        )}
      </Paper>
    </Box>
  );
}