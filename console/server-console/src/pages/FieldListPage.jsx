import React, { useEffect, useState } from "react";
import {
  Box,
  Button,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TablePagination,
  Tooltip
} from "@mui/material";
import { Add, Delete, Edit, ContentCopy } from "@mui/icons-material";
import api from "../services/api";
import FieldFormDialog from "./FieldFormDialog";

const FieldListPage = () => {
  const [fields, setFields] = useState([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editField, setEditField] = useState(null);
  const [copiedField, setCopiedField] = useState(null); // 🆕 Copied field

  const fetchFields = async () => {
    try {
      const res = await api.get(`/onboarding/field?page=${page}&size=${size}`);
      const data = res.data;
      setFields(data.responseData || []);
      setTotal(data.totalElements || 0);
    } catch (error) {
      console.error("Error fetching fields:", error);
    }
  };

  useEffect(() => {
    fetchFields();
  }, [page, size]);

  const handleDelete = async (id) => {
    await api.delete(`/onboarding/field/${id}`);
    fetchFields();
  };

  const handleCreate = () => {
    setEditField(null);
    setDialogOpen(true);
  };

  const handleEdit = (field) => {
    try {
      const parsedConfig = field.config || {};
      setEditField({
        id: field.id,
        name: field.name,
        type: field.type,
        fieldConfig: parsedConfig,
      });
      setDialogOpen(true);
    } catch (e) {
      alert("Invalid field config, cannot edit.");
      console.error("Error parsing config:", e);
    }
  };

  const handleCopy = (field) => {
    try {
      const clone = {
        ...field,
        id: undefined,
        name: "",
        fieldConfig: { ...(field.config || {}) },
      };
      setCopiedField(clone);
    } catch (err) {
      console.error("Failed to copy field:", err);
    }
  };

  const handleDialogClose = () => {
    setDialogOpen(false);
    setEditField(null);
  };

  const handleFieldSaved = () => {
    fetchFields();
    handleDialogClose();
  };

  const handleChangePage = (_, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (e) => {
    setSize(parseInt(e.target.value, 10));
    setPage(0);
  };

  return (
    <Box sx={{ width: "80vw", minHeight: "100vh", padding: "20px", backgroundColor: "#f5f5f5" }}>
      <Button variant="contained" startIcon={<Add />} onClick={handleCreate}>
        Create Field
      </Button>

      <TableContainer component={Paper} sx={{ mt: 3 }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Field Name</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {fields.map((field) => (
              <TableRow key={field.id}>
                <TableCell>{field.name}</TableCell>
                <TableCell>{field.type}</TableCell>
                <TableCell>
                  <Tooltip title="Edit">
                    <IconButton onClick={() => handleEdit(field)}><Edit /></IconButton>
                  </Tooltip>
                  <Tooltip title="Delete">
                    <IconButton onClick={() => handleDelete(field.id)}><Delete /></IconButton>
                  </Tooltip>
                  <Tooltip title="Copy">
                    <IconButton onClick={() => handleCopy(field)}><ContentCopy /></IconButton>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>

        <TablePagination
          component="div"
          count={total}
          page={page}
          onPageChange={handleChangePage}
          rowsPerPage={size}
          onRowsPerPageChange={handleChangeRowsPerPage}
          rowsPerPageOptions={[5, 10, 25, 50]}
        />
      </TableContainer>

      <FieldFormDialog
        open={dialogOpen}
        onClose={handleDialogClose}
        onSaved={handleFieldSaved}
        initial={editField}
        copiedField={copiedField} // 🆕 Pass to dialog
      />
    </Box>
  );
};

export default FieldListPage;