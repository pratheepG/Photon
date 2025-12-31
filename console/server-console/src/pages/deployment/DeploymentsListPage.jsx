import React, { useEffect, useState } from 'react';
import {
  Box, Paper, Typography, Button, Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, TablePagination, CircularProgress, Alert, Toolbar
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';

export default function DeploymentsListPage() {
  const [deployments, setDeployments] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalRecords, setTotalRecords] = useState(0);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchDeployments(page, rowsPerPage);
  }, []);

  useEffect(() => {
    fetchDeployments(page, rowsPerPage);
  }, [page, rowsPerPage]);

  const fetchDeployments = async (p = 0, size = 10) => {
    setLoading(true);
    setErrorMsg('');
    try {
      const resp = await api.get(`/deployment?page=${p}&size=${size}`);
      if (resp.data && resp.data.success) {
        const list = resp.data.responseData || [];
        setDeployments(list);
        setTotalRecords(resp.data.totalRecords ?? resp.data.totalElements ?? list.length);
      } else {
        setDeployments([]);
        setTotalRecords(0);
        setErrorMsg(resp.data?.message || 'Failed to load deployments');
      }
    } catch (err) {
      console.error('Failed to fetch deployments', err);
      setErrorMsg('Failed to load deployments.');
      setDeployments([]);
      setTotalRecords(0);
    } finally {
      setLoading(false);
    }
  };

  const handleRowClick = (d) => {
    if (!d?.id) return;
    navigate(`/deployment/details/${d.id}`, { state: { deployment: d } });
  };

  return (
    <Box sx={{ width: '90vw', minHeight: '80vh', padding: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4">Deployments</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/deployment/upload')}>
          Create Deployment
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
                    <TableCell>ID</TableCell>
                    <TableCell>Service Name</TableCell>
                    <TableCell>Jar File</TableCell>
                    <TableCell>Deployed</TableCell>
                  </TableRow>
                </TableHead>

                <TableBody>
                  {deployments.map((d) => (
                    <TableRow key={d.id} hover sx={{ cursor: d.id ? 'pointer' : 'default' }} onClick={() => handleRowClick(d)}>
                      <TableCell>{d.id || '-'}</TableCell>
                      <TableCell>{d.serviceName || '-'}</TableCell>
                      <TableCell>{d.jarFileName || '-'}</TableCell>
                      <TableCell>{d.deployed ? 'Yes' : 'No'}</TableCell>
                    </TableRow>
                  ))}

                  {deployments.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={4} align="center">No deployments found.</TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>

            <TablePagination component="div" count={totalRecords} page={page} onPageChange={(_, newPage) => setPage(newPage)} rowsPerPage={rowsPerPage}
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