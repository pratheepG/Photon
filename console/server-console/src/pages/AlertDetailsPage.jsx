import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Box, Typography, Divider, Grid, Paper, Button, Select, MenuItem, InputLabel, FormControl, TextField, CircularProgress, Toolbar, Card, CardContent, IconButton, Chip, List, ListItem} from '@mui/material';
// FIX: Changing to modular import 'services/api' to resolve path resolution error
import api from '../services/api'; 
import { Dialog, DialogTitle, DialogContent, DialogActions, TextareaAutosize} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import SaveIcon from '@mui/icons-material/Save';
import RestoreIcon from '@mui/icons-material/Restore';

// Helper function for deep comparison
const isEqual = (obj1, obj2) => {
    // Simple deep comparison for state objects (ignoring createdAt/id which might change)
    if (typeof obj1 !== 'object' || obj1 === null || typeof obj2 !== 'object' || obj2 === null) {
        return obj1 === obj2;
    }

    const keys1 = Object.keys(obj1).filter(k => k !== 'createdAt' && k !== 'id');
    const keys2 = Object.keys(obj2).filter(k => k !== 'createdAt' && k !== 'id');

    if (keys1.length !== keys2.length) {
        return false;
    }

    for (let key of keys1) {
        if (!keys2.includes(key) || !isEqual(obj1[key], obj2[key])) {
            return false;
        }
    }
    return true;
};

const AlertDetailsPage = () => {
  const {alertId } = useParams();
  const navigate = useNavigate();
  
  const [alert, setAlert] = useState(null);
  const [editableAlert, setEditableAlert] = useState(null);
  const [saving, setSaving] = useState(false);
  const [showAddTemplate, setShowAddTemplate] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [editIndex, setEditIndex] = useState(null);
  const [originalAlert, setOriginalAlert] = useState(null);
  const [hasChanges, setHasChanges] = useState(false);
  const [error, setError] = useState(''); // General page error/feedback
  const [dialogError, setDialogError] = useState(''); // Dialog specific error/feedback
  
  const [newTemplate, setNewTemplate] = useState({
    channel: '',
    subjectTemplate: '',
    messageTemplate: '',
    mailAttachments: [],
    smsDeeplinkUrl: '',
    smsMediaUrl: ''
  });

  useEffect(() => {
    fetchAlertDetails();
  }, [alertId]);

  // Effect to check for changes
  useEffect(() => {
    if (!alert || !editableAlert || !originalAlert) return;
  
    // Check for changes in high-level fields (Audience/Topic)
    const audienceTopicChanged = !isEqual({
        audience: editableAlert.audience,
        topic: editableAlert.topic
    }, {
        audience: originalAlert.audience,
        topic: originalAlert.topic
    });

    // Check for changes in templates array (must use deep copy for comparison)
    const templatesChanged = !isEqual(alert.templates || [], originalAlert.templates || []);
    
    setHasChanges(audienceTopicChanged || templatesChanged);
  }, [editableAlert, alert, originalAlert]);
  

  const fetchAlertDetails = async () => {
    try {
      const response = await api.get(`/alerts/${alertId}`);
      const fetchedAlert = response.data.responseData;
      
      setAlert(fetchedAlert);
      setOriginalAlert(JSON.parse(JSON.stringify(fetchedAlert))); 

      setEditableAlert({
        audience: fetchedAlert.audience || '',
        topic: fetchedAlert.topic || ''
      });
      setError('');
    } catch (error) {
      console.error('Error fetching alert detail:', error);
      setError('Failed to load alert details.');
    }
  };

  const handleOpenTemplateDialog = (template = null, index = null) => {
    setDialogError('');
    setShowAddTemplate(true);
    setIsEditMode(!!template);
    setEditIndex(index);
    setNewTemplate(
      template ? JSON.parse(JSON.stringify(template)) : { // Deep copy template for editing
        channel: '',
        subjectTemplate: '',
        messageTemplate: '',
        mailAttachments: [],
        smsDeeplinkUrl: '',
        smsMediaUrl: ''
      }
    );
  };  

  const ALL_CHANNELS = [
    "SMS",
    "E_MAIL",
    "WHATSAPP",
    "PUSH_NOTIFICATION",
    "IN_APP_NOTIFICATION",
    "CAMPAIGN"
  ];
  
  // Channels already used in existing templates
  const usedChannels = Array.isArray(alert?.templates) ? alert.templates.map(t => t.channel) : [];
  
  // Available channels for *new* templates, plus the current channel if in edit mode
  const availableChannels = ALL_CHANNELS.filter(c => !usedChannels.includes(c) || (isEditMode && newTemplate.channel === c));

  const handleAddTemplateSubmit = async () => {
    // Basic validation
    if (!newTemplate.channel || !newTemplate.messageTemplate) {
        setDialogError('Channel and Message Template are required.');
        return;
    }
    
    // Check if channel is already used (only for creation mode)
    if (!isEditMode && usedChannels.includes(newTemplate.channel)) {
        setDialogError(`Channel ${newTemplate.channel} is already configured.`);
        return;
    }

    const updatedTemplates = [...(alert.templates || [])];
  
    if (isEditMode && editIndex !== null) {
      updatedTemplates[editIndex] = {
        ...updatedTemplates[editIndex], // Preserve existing ID/metadata if available
        ...newTemplate
      }; 
    } else {
      updatedTemplates.push({
        id: `temp-${Date.now()}`, // Temporary ID for client state management
        createdAt: new Date().toISOString(),
        ...newTemplate
      }); 
    }
  
    const payload = {
      ...alert, 
      audience: editableAlert.audience,
      topic: editableAlert.audience === 'TOPIC' ? editableAlert.topic : '',
      templates: updatedTemplates
    };
  
    try {
      setSaving(true);
      await api.put(`/alerts/${alert.id}`, payload);
      // Success: Close dialog and refetch data to reset state/originalAlert
      handleCloseTemplateDialog();
      fetchAlertDetails(); 
      setError('Template saved successfully! Configuration updated.');
    } catch (error) {
      console.error('Failed to save template', error);
      setDialogError('Failed to save template due to an API error.');
    } finally {
        setSaving(false);
    }
  };
  
  const handleCloseTemplateDialog = () => {
    setShowAddTemplate(false);
    setIsEditMode(false);
    setEditIndex(null);
    setDialogError('');
  };  
  
  const handleFileUpload = (e) => {
    const files = Array.from(e.target.files);
    const attachments = files.map(file => ({
      fileName: file.name,
      downloadUrl: URL.createObjectURL(file) 
    }));
    setNewTemplate(prev => ({
      ...prev,
      mailAttachments: [...(prev.mailAttachments || []), ...attachments]
    }));
  };  
  
  const handleRemoveAttachment = (attachmentIndex) => {
    setNewTemplate(prev => ({
        ...prev,
        mailAttachments: prev.mailAttachments.filter((_, i) => i !== attachmentIndex)
    }));
  };

  const handleSave = async () => {
    if (!editableAlert || !hasChanges) return;
    setSaving(true);
    setError('');
    
    // Deep copy current alert templates to ensure we send the latest structure
    const templatesPayload = JSON.parse(JSON.stringify(alert.templates || []));

    try {
      const payload = {
        ...alert,
        audience: editableAlert.audience,
        topic: editableAlert.audience === 'TOPIC' ? editableAlert.topic : '',
        templates: templatesPayload 
      };
      
      await api.put(`/alerts/${alertId}`, payload); 
      fetchAlertDetails(); 
      setError('Alert configuration updated successfully!');
    } catch (err) {
      console.error('Error updating alert:', err);
      setError('Failed to update alert configuration. Check your inputs.');
    } finally {
      setSaving(false);
    }
  };

  const handleUndo = () => {
    if (!originalAlert) return;
    
    // Reset templates to the original state
    setAlert(JSON.parse(JSON.stringify(originalAlert)));
    
    // Reset editable fields
    setEditableAlert({
        audience: originalAlert.audience || '',
        topic: originalAlert.topic || ''
    });

    setHasChanges(false);
    setError('Changes reverted to original configuration.');
  };

  const handleDeleteTemplate = (index) => {
    if (window.confirm('Are you sure you want to delete this template? This will require clicking "Update" to save the change.')) {
        const updatedTemplates = (alert.templates || []).filter((_, i) => i !== index);
        
        // Optimistic UI update (triggers hasChanges flag)
        setAlert(prev => ({
          ...prev,
          templates: updatedTemplates
        }));
        setError('Template marked for deletion. Click "Update Configuration" to save this change.');
    }
  };  

  if (!alert || !editableAlert) {
    return <CircularProgress sx={{ display: 'block', margin: 'auto', mt: 5 }} />;
  }  

  return (
    <Box sx={{ width: '100%', minHeight: '100vh', padding: '32px', backgroundColor: '#f4f7f9' }}>
      
      {/* Header Toolbar (Sticky for Back Button) */}
      <Toolbar 
        component={Paper} 
        elevation={1} 
        sx={{ 
          mb: 4, 
          borderRadius: '12px', 
          backgroundColor: 'white', 
          display: 'flex', 
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <Typography variant="h5" sx={{ fontWeight: 700, color: '#1a202c' }}>
            {alert.alertType} / {alert.alertSubType}
        </Typography>
        <Button 
          variant="text" 
          startIcon={<ArrowBackIcon />} 
          onClick={() => navigate(-1)}
          sx={{ textTransform: 'none' }}
        >
          Go Back
        </Button>
      </Toolbar>

      {/* General Page Feedback */}
      {error && (
        <Paper elevation={1} sx={{ p: 2, mb: 3, backgroundColor: error.includes('success') ? '#e6fff0' : '#ffe6e6', border: error.includes('success') ? '1px solid #00c853' : '1px solid #ff1744', borderRadius: '8px' }}>
          <Typography color={error.includes('success') ? '#00c853' : '#ff1744'} variant="body2">{error}</Typography>
        </Paper>
      )}


      {/* Main Content Area */}
      <Grid container spacing={4}>
        {/* Basic Info & Editable Fields (Left/Top) */}
        <Grid item xs={12} md={5}>
          <Paper elevation={3} sx={{ padding: 4, borderRadius: '12px', height: '100%' }}>
            <Typography variant="h6" gutterBottom sx={{ fontWeight: 600, color: '#007bff' }}>Alert Configuration</Typography>
            <Divider sx={{ mb: 3 }} />

            <Typography variant="body1" sx={{ mb: 1 }}>
                <strong>Alert Type:</strong> <Chip label={alert.alertType} color="primary" size="small" sx={{ fontWeight: 600 }} />
            </Typography>
            <Typography variant="body1" sx={{ mb: 2 }}>
                <strong>Sub Type:</strong> <Chip label={alert.alertSubType} color="secondary" size="small" sx={{ fontWeight: 600 }} />
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 2, mb: 3 }}>
                * Created At: {new Date(alert.createdAt).toLocaleString()}
            </Typography>

            <FormControl fullWidth sx={{ mb: 3 }}>
                <InputLabel id="audience-label">Audience</InputLabel>
                <Select labelId="audience-label" value={editableAlert.audience} label="Audience"
                    onChange={(e) =>
                        setEditableAlert(prev => ({
                        ...prev,
                        audience: e.target.value,
                        topic: e.target.value === 'TOPIC' ? prev.topic : '' 
                        }))
                    }
                >
                    <MenuItem value="BROADCAST">BROADCAST (All)</MenuItem>
                    <MenuItem value="INDIVIDUAL">INDIVIDUAL (Targeted)</MenuItem>
                    <MenuItem value="TOPIC">TOPIC (Channel)</MenuItem>
                </Select>
            </FormControl>

            {editableAlert.audience === 'TOPIC' && (
                <TextField fullWidth label="Topic Name" value={editableAlert.topic || ''}
                    onChange={(e) =>
                        setEditableAlert(prev => ({
                        ...prev,
                        topic: e.target.value
                        }))
                    }
                    helperText="Enter the name of the Kafka/PubSub topic for this alert."
                />
            )}
          </Paper>
        </Grid>

        {/* Templates Section (Right/Bottom) */}
        <Grid item xs={12} md={7}>
          <Paper elevation={3} sx={{ padding: 4, borderRadius: '12px' }}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                <Typography variant="h6" sx={{ fontWeight: 600 }}>Channel Templates ({alert.templates?.length || 0})</Typography>
                <Button 
                    variant="contained" 
                    onClick={() => handleOpenTemplateDialog()}
                    disabled={availableChannels.length === 0}
                    sx={{ backgroundColor: '#007bff', '&:hover': { backgroundColor: '#0056b3' } }}
                >
                    Add New Template
                </Button>
            </Box>

            {alert.templates?.length ? (
                alert.templates.map((template, index) => (
                    <Card key={template.id || index} variant="outlined" sx={{ mb: 2, backgroundColor: '#f9f9f9' }}>
                        <CardContent>
                            <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                                <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#333' }}>
                                    {template.channel}
                                </Typography>
                                <Box>
                                    <IconButton size="small" onClick={() => handleOpenTemplateDialog(template, index)} color="primary">
                                        <EditIcon fontSize="small" />
                                    </IconButton>
                                    <IconButton size="small" onClick={() => handleDeleteTemplate(index)} color="error">
                                        <DeleteIcon fontSize="small" />
                                    </IconButton>
                                </Box>
                            </Box>
                            
                            <Grid container spacing={1} mt={1}>
                                {template.subjectTemplate && (
                                    <Grid item xs={12}>
                                        <Typography variant="body2"><strong>Subject:</strong> {template.subjectTemplate}</Typography>
                                    </Grid>
                                )}
                                <Grid item xs={12}>
                                    <Typography variant="body2"><strong>Message:</strong> {template.messageTemplate.substring(0, 100)}{template.messageTemplate.length > 100 ? '...' : ''}</Typography>
                                </Grid>
                                {template.mailAttachments?.length > 0 && (
                                     <Grid item xs={12}>
                                        <Typography variant="caption" color="text.secondary">
                                            Attachments: {template.mailAttachments.length}
                                        </Typography>
                                    </Grid>
                                )}
                            </Grid>
                        </CardContent>
                    </Card>
                ))
            ) : (
                <Box sx={{ textAlign: 'center', p: 3, border: '1px dashed #ccc', borderRadius: '8px' }}>
                    <Typography color="text.secondary">No templates configured. Start by adding a channel template.</Typography>
                </Box>
            )}
          </Paper>
        </Grid>
      </Grid>
      
      {/* Sticky Footer Toolbar for Save/Undo actions */}
      <Toolbar 
        component={Paper} 
        elevation={6} 
        sx={{ 
            position: 'sticky', 
            bottom: 0, 
            mt: 4, 
            py: 1, 
            borderRadius: '12px 12px 0 0', 
            display: 'flex', 
            justifyContent: 'flex-end', 
            gap: 2,
            backgroundColor: 'white',
            zIndex: 10
        }}
      >
        <Button 
            variant="outlined" 
            startIcon={<RestoreIcon />}
            disabled={!hasChanges || saving}
            onClick={handleUndo}
            sx={{ textTransform: 'none' }}
        >
            Undo Changes
        </Button>
        <Button 
            variant="contained" 
            startIcon={<SaveIcon />}
            color="primary" 
            disabled={!hasChanges || saving} 
            onClick={handleSave}
            sx={{ 
                backgroundColor: '#28a745', 
                '&:hover': { backgroundColor: '#1e7e34' },
                textTransform: 'none'
            }}
        >
            {saving ? <CircularProgress size={24} color="inherit" /> : 'Update Configuration'}
        </Button>
      </Toolbar>

      {/* Add/Edit Template Dialog */}
      <Dialog open={showAddTemplate} onClose={handleCloseTemplateDialog} maxWidth="md" fullWidth>
        <DialogTitle sx={{ backgroundColor: '#007bff', color: 'white' }}>
            {isEditMode ? 'Edit Alert Template' : 'Add New Alert Template'}
        </DialogTitle>
        <DialogContent dividers>
            {dialogError && <Typography color="error" sx={{ mb: 2 }}>{dialogError}</Typography>}
            <FormControl fullWidth sx={{ mb: 3 }}>
                <InputLabel id="channel-label">Channel</InputLabel>
                <Select 
                    labelId="channel-label" 
                    value={newTemplate.channel} 
                    label="Channel" 
                    disabled={isEditMode} // Cannot change channel when editing
                    onChange={(e) => setNewTemplate(prev => ({ ...prev, channel: e.target.value }))}
                >
                    {availableChannels.map(channel => (
                    <MenuItem key={channel} value={channel}>{channel}</MenuItem>
                    ))}
                    {isEditMode && <MenuItem key={newTemplate.channel} value={newTemplate.channel}>{newTemplate.channel}</MenuItem>}
                </Select>
                {!isEditMode && availableChannels.length === 0 && (
                    <Typography variant="caption" color="error">All channels are already configured for this alert subtype.</Typography>
                )}
            </FormControl>

            {(newTemplate.channel === 'E_MAIL') && (
            <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
                <Typography variant="h6" mb={2}>Email Content</Typography>
                <TextField fullWidth label="Subject Template" value={newTemplate.subjectTemplate || ''} onChange={(e) => setNewTemplate(prev => ({ ...prev, subjectTemplate: e.target.value }))} sx={{ mb: 2 }}/>
                
                <TextareaAutosize 
                    minRows={6} 
                    placeholder="Message Template (Supports HTML/Rich Text if the channel is configured for it)" 
                    style={{ width: '100%', padding: 10, border: '1px solid #ccc', borderRadius: '4px' }} 
                    value={newTemplate.messageTemplate} 
                    onChange={(e) => setNewTemplate(prev => ({ ...prev, messageTemplate: e.target.value }))}
                />
                
                <Box sx={{ mt: 2 }}>
                    <Button variant="outlined" component="label">
                        Upload Mail Attachments
                        <input type="file" hidden multiple onChange={handleFileUpload} />
                    </Button>
                </Box>
                
                {/* Attachment List */}
                {newTemplate.mailAttachments?.length > 0 && (
                <Box sx={{ mt: 2 }}>
                    <Typography variant="body2" sx={{ fontWeight: 600 }}>Current Attachments:</Typography>
                    <List dense>
                    {newTemplate.mailAttachments.map((file, index) => (
                        <ListItem key={index} secondaryAction={
                            <IconButton edge="end" aria-label="delete" onClick={() => handleRemoveAttachment(index)}>
                                <DeleteIcon fontSize="small" />
                            </IconButton>
                        }>
                            <Typography variant="body2">{file.fileName}</Typography>
                        </ListItem>
                    ))}
                    </List>
                </Box>
                )}
            </Paper>
            )}

            {(newTemplate.channel === 'SMS' || newTemplate.channel === 'WHATSAPP' || newTemplate.channel === 'PUSH_NOTIFICATION') && (
            <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
                <Typography variant="h6" mb={2}>{newTemplate.channel} Content</Typography>
               <TextareaAutosize 
                    minRows={6} 
                    placeholder="Message Template (Plain text for SMS/WhatsApp/Push)" 
                    style={{ width: '100%', padding: 10, border: '1px solid #ccc', borderRadius: '4px' }} 
                    value={newTemplate.messageTemplate} 
                    onChange={(e) => setNewTemplate(prev => ({ ...prev, messageTemplate: e.target.value }))}
                />
               <TextField fullWidth label="Deep Link URL (Optional)" sx={{ mt: 2 }} value={newTemplate.smsDeeplinkUrl || ''} onChange={(e) => setNewTemplate(prev => ({ ...prev, smsDeeplinkUrl: e.target.value }))} helperText="Link to open the app on alert tap." />
               <TextField fullWidth label="Media URL (Optional)" sx={{ mt: 2 }} value={newTemplate.smsMediaUrl || ''} onChange={(e) => setNewTemplate(prev => ({ ...prev, smsMediaUrl: e.target.value }))} helperText="URL for an image/video asset." />
            </Paper>
            )}
            
            {/* Fallback for other channels like IN_APP_NOTIFICATION, CAMPAIGN */}
            {(newTemplate.channel === 'IN_APP_NOTIFICATION' || newTemplate.channel === 'CAMPAIGN') && (
                 <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
                    <Typography variant="h6" mb={2}>{newTemplate.channel} Content</Typography>
                     <TextField fullWidth label="Title/Subject (Optional)" value={newTemplate.subjectTemplate || ''} onChange={(e) => setNewTemplate(prev => ({ ...prev, subjectTemplate: e.target.value }))} sx={{ mb: 2 }}/>
                    <TextareaAutosize 
                        minRows={6} 
                        placeholder="Message/Body Content" 
                        style={{ width: '100%', padding: 10, border: '1px solid #ccc', borderRadius: '4px' }} 
                        value={newTemplate.messageTemplate} 
                        onChange={(e) => setNewTemplate(prev => ({ ...prev, messageTemplate: e.target.value }))}
                    />
                </Paper>
            )}

        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
            <Button onClick={handleCloseTemplateDialog}>Cancel</Button>
            <Button variant="contained" onClick={handleAddTemplateSubmit} disabled={saving}>
                {saving ? <CircularProgress size={24} color="inherit" /> : (isEditMode ? 'Update Template' : 'Create Template')}
            </Button>
        </DialogActions>
        </Dialog>
    </Box>
  );

};

export default AlertDetailsPage;