import React, { useState, useEffect, useContext } from 'react';
import { 
  Box, Drawer, List, ListItem, ListItemText, Toolbar, 
  AppBar, Typography, IconButton, Collapse, Snackbar, Alert 
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import ExpandLess from '@mui/icons-material/ExpandLess';
import ExpandMore from '@mui/icons-material/ExpandMore';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';


// =================================================================
// REUSABLE NOTIFICATION SYSTEM MODULE (Decoupled Components)
// =================================================================

// 1. NOTIFICATION CONTEXT & HOOK 
const NotificationContext = React.createContext(null);

/**
 * Custom hook to access the showNotification function.
 * This is the Consumer of the Context.
 */
const useNotification = () => {
    const context = useContext(NotificationContext);
    if (context === null) {
        throw new Error('useNotification must be used within a NotificationProvider');
    }
    return context;
};

const NotificationBanner = ({ notification, handleClose }) => {
    const { open, message, type } = notification;

    return (
        <Snackbar open={open} autoHideDuration={4000} onClose={handleClose} anchorOrigin={{ vertical: "top", horizontal: "center" }}>
            <Alert onClose={handleClose} severity={type} variant="filled" sx={{ width: "100%", minWidth: '300px' }}>
                {message}
            </Alert>
        </Snackbar>
    );
};

// 3. NOTIFICATION PROVIDER
const NotificationProvider = ({ children }) => {
    const [notification, setNotification] = useState({
        open: false,
        message: '',
        type: 'success', 
    });
    
    const handleClose = () => {
        setNotification(prev => ({ ...prev, open: false }));
    };

    const showNotification = (message, type = 'success') => {
        if (!message) return;
        
        handleClose();

        setNotification({
            open: true,
            message,
            type,
        });
    };

    return (
        <NotificationContext.Provider value={{ showNotification }}>
            {children}
            <NotificationBanner notification={notification} handleClose={handleClose} />
        </NotificationContext.Provider>
    );
};


// =================================================================
// DASHBOARD LAYOUT (The component that USES the hook)
// This is separated from the wrapper to fix the Context error.
// =================================================================

const drawerWidth = 240;

const DashboardLayout = ({ children }) => {
  const navigate = useNavigate();
  const { showNotification } = useNotification(); 

  const [openApiManager, setOpenApiManager] = useState(false);
  const [openAuth, setOpenAuth] = useState(false);
  const [openSettings, setOpenSettings] = useState(false);
  const [openProperties, setOpenProperties] = useState(false);
  const [openUsers, setOpenUsers] = useState(false);
  const [applications, setApplications] = useState([]);
  const [openLogging, setOpenLogging] = useState(false);
  const [loggingApps, setLoggingApps] = useState([]);
  const [openFormJourney, setOpenFormJourney] = useState(false);

  const handleApiManagerClick = async () => {
    setOpenApiManager((prevOpen) => !prevOpen);

    if (!openApiManager) {
      try {
         const response = await api.get('/api-manager/apps');
        if (response.data.success) {
          setApplications(response.data.responseData);
        }
      } catch (error) {
        console.error('Error fetching applications:', error);
        showNotification('Failed to load API Manager applications.', 'error'); 
      }
    }
  };

  const handleLoggingClick = async () => {
    setOpenLogging((prevOpen) => !prevOpen);

    if (!openLogging) {
      try {
        const response = await api.get('/api-manager/logging-apps');
        if (response.data.success) {
          setLoggingApps(response.data.responseData);
        }
      } catch (error) {
        console.error('Error fetching logging apps:', error);
        showNotification('Failed to load logging applications.', 'error'); 
      }
    }
  };

  const handleFormJourneyClick = async () => {
    setOpenFormJourney((prevOpen) => !prevOpen);
  };

  const handleMenuItemClick = (path) => {
    navigate(path);
  };

  const handleAppClick = (appId) => {
    navigate(`/api-manager/${appId}`); 
  };

  const handleAuthClick = () => {
    setOpenAuth((prevOpen) => !prevOpen);
  };

  const handleSettingsClick = () => {
    setOpenSettings((prevOpen) => !prevOpen);
  };

  const handlePropertiesClick = () => {
    setOpenProperties((prevOpen) => !prevOpen);
  };

    const handleUsersClick = () => {
    setOpenUsers((prevOpen) => !prevOpen);
  };

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <Toolbar>
          <IconButton edge="start" color="inherit" aria-label="menu" sx={{ mr: 2 }}>
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div">
            Dashboard
          </Typography>
        </Toolbar>
      </AppBar>

      <Drawer
        variant="permanent"
        sx={{
          width: drawerWidth,
          flexShrink: 0,
          [`& .MuiDrawer-paper`]: { width: drawerWidth, boxSizing: 'border-box' },
        }}>
          
        <Toolbar />
        <Box sx={{ overflow: 'auto' }}>
          <List>
            {/* Authentication Section */}
            <ListItem button onClick={handleAuthClick}>
              <ListItemText primary="Authentication" />
              {openAuth ? <ExpandLess /> : <ExpandMore />}
            </ListItem>

            {/* Submenu for Authentication */}
            <Collapse in={openAuth} timeout="auto" unmountOnExit>
              <List component="div" disablePadding>
                <ListItem button sx={{ pl: 4 }} onClick={() => handleMenuItemClick('/identity-service')}>
                  <ListItemText primary="Identity Service" />
                </ListItem>
                <ListItem button sx={{ pl: 4 }} onClick={() => handleMenuItemClick('/auth-types')}>
                  <ListItemText primary="Auth Types" />
                </ListItem>
                <ListItem button sx={{ pl: 4 }} onClick={() => handleMenuItemClick('/user-roles')}>
                  <ListItemText primary="User Roles" />
                </ListItem>
                <ListItem button sx={{ pl: 4 }} onClick={() => handleMenuItemClick('/mfa-condition')}>
                  <ListItemText primary="MFA Condition" />
                </ListItem>
              </List>
            </Collapse>


            {/* Settings Section */}
            <ListItem button onClick={handleSettingsClick}>
              <ListItemText primary="Settings" />{openSettings ? <ExpandLess /> : <ExpandMore />}
            </ListItem>

            {/* Submenu for Settings */}
            <Collapse in={openSettings} timeout="auto" unmountOnExit>
              <List component="div" disablePadding>
                <ListItem button sx={{ pl: 4 }} onClick={() => handleMenuItemClick('/alert-settings')}>
                  <ListItemText primary="Alert Settings" />
                </ListItem>
                <ListItem button sx={{ pl: 4 }} onClick={() => handleMenuItemClick('/storage-settings')}>
                  <ListItemText primary="Storage Settings" />
                </ListItem>
              </List>
            </Collapse>


            {/* Other Menu Items */}
            {[{ text: 'Onboarding Config', path: '/onboarding-configuration'},
              { text: 'Certificates', path: '/certificates' },
              { text: 'Alerts', path: '/alerts' },
              { text: 'Deployments', path: '/deployments'},
              { text: 'SSL Configuration', path: '/ssl-configuration' }].map((item) => (
              <ListItem button key={item.text} onClick={() => handleMenuItemClick(item.path)}>
                <ListItemText primary={item.text} />
              </ListItem>
            ))}

            {/* Properties Section */}
            <ListItem button onClick={handlePropertiesClick}>
              <ListItemText primary="Properties" />
              {openProperties ? <ExpandLess /> : <ExpandMore />}
            </ListItem>

            {/* Submenu for Properties */}
            <Collapse in={openProperties} timeout="auto" unmountOnExit>
              <List component="div" disablePadding>
                <ListItem button sx={{ pl: 4 }} onClick={() => navigate(`/api-manager/CLIENT-PROPERTIES/config`)}>
                  <ListItemText primary="Client Properties" />
                </ListItem>
                <ListItem button sx={{ pl: 4 }} onClick={() => navigate(`/api-manager/SERVER-PROPERTIES/config`)}>
                  <ListItemText primary="Server Properties" />
                </ListItem>
              </List>
            </Collapse>

            {/* API Manager Section */}
            <ListItem button onClick={handleApiManagerClick}>
              <ListItemText primary="API Manager" />
              {openApiManager ? <ExpandLess /> : <ExpandMore />}
            </ListItem>

            {/* Submenu for Applications under API Manager */}
            <Collapse in={openApiManager} timeout="auto" unmountOnExit>
              <List component="div" disablePadding>
                {applications.length > 0 ? (
                  applications.map((app) => (
                    <ListItem button key={app} onClick={() => handleAppClick(app)} sx={{ pl: 4 }}>
                      <ListItemText primary={app} />
                    </ListItem>
                  ))
                ) : (
                  <ListItemText primary="No applications available" sx={{ pl: 4, color: 'gray' }} />
                )}
              </List>
            </Collapse>

            {/* UI Journey Section */}
            <ListItem button onClick={handleFormJourneyClick}>
              <ListItemText primary="UI-UX Form Journey" />
              {openFormJourney ? <ExpandLess /> : <ExpandMore />}
            </ListItem>

            <Collapse in={openFormJourney} timeout="auto" unmountOnExit>
              <List component="div" disablePadding>
                <ListItem button sx={{ pl: 4 }} onClick={() => handleMenuItemClick('/field-list')}>
                  <ListItemText primary="Field" />
                </ListItem>
                <ListItem button sx={{ pl: 4 }} onClick={() => handleMenuItemClick('/form-list')}>
                  <ListItemText primary="Form" />
                </ListItem>
                <ListItem button sx={{ pl: 4 }} onClick={() => handleMenuItemClick('/user-roles')}>
                  <ListItemText primary="Journey" />
                </ListItem>
              </List>
            </Collapse>

            {/* Logging Section */}
            <ListItem button onClick={handleLoggingClick}>
              <ListItemText primary="Logging" />
              {openLogging ? <ExpandLess /> : <ExpandMore />}
            </ListItem>

            <Collapse in={openLogging} timeout="auto" unmountOnExit>
              <List component="div" disablePadding>
                {loggingApps.length > 0 ? (
                  loggingApps.map((app) => (
                    <ListItem
                      button
                      key={app}
                      onClick={() => handleMenuItemClick(`/logging/${app}`)}
                      sx={{ pl: 4 }}
                    >
                      <ListItemText primary={app} />
                    </ListItem>
                  ))
                ) : (
                  <ListItemText primary="No logging apps" sx={{ pl: 4, color: 'gray' }} />
                )}
              </List>
            </Collapse>

            {/* Users Section */}
            <ListItem button onClick={handleUsersClick}>
              <ListItemText primary="Users" />
              {openUsers ? <ExpandLess /> : <ExpandMore />}
            </ListItem>

            {/* Submenu for Users */}
            <Collapse in={openUsers} timeout="auto" unmountOnExit>
              <List component="div" disablePadding>
                <ListItem button sx={{ pl: 4 }} onClick={() => navigate(`/api-manager/CLIENT-PROPERTIES/config`)}>
                  <ListItemText primary="Server Users" />
                </ListItem>
                <ListItem button sx={{ pl: 4 }} onClick={() => navigate(`/users`)}>
                  <ListItemText primary="App Users" />
                </ListItem>
              </List>
            </Collapse>

          </List>
        </Box>
      </Drawer>

      <Box 
        component="main" 
        sx={{ flexGrow: 1, width: 'auto', maxWidth: '100%', bgcolor: 'background.default', p: 3 }}
      >
        <Toolbar />
        {children}
      </Box>
    </Box>
  );
};

// =================================================================
// WRAPPER COMPONENT (Exported)
// This only provides the context and renders the layout component.
// =================================================================

/**
 * Main wrapper for the application layout.
 * It provides the global NotificationProvider context to its children.
 */
const Dashboard = (props) => (
    <NotificationProvider>
        <DashboardLayout {...props} />
    </NotificationProvider>
);

export default Dashboard;