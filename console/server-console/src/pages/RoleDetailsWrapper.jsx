import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import RoleDetailsPage from './RoleDetailsPage';

const RoleDetailsWrapper = () => {
  const location = useLocation();
  const navigate = useNavigate();

  // Get the role data from the location's state
  const { role } = location.state || {}; // Default to an empty object if no state is passed

  // If no role data is available, navigate back to the role list page
  if (!role) {
    navigate('/user-roles');
    return null;
  }

  return <RoleDetailsPage role={role} />;
};

export default RoleDetailsWrapper;