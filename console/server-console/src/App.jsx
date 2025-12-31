import { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import CssBaseline from '@mui/material/CssBaseline';
import Container from '@mui/material/Container';
import Dashboard from './components/Dashboard';
//import AuthenticationPage from './pages/AuthenticationPage';
import ErrorBanner from "./components/ErrorBanner";
import RegisterPage from './pages/RegisterPage';
import CertificatePage from './pages/CertificatePage';
import ActionDetailsPage from './pages/action/ActionDetailsPage';
//import AuthTypeDetailsPage from './pages/AuthTypeDetailsPage';
import UserRolesPage from './pages/UserRolesPage';
import RoleDetailsWrapper from './pages/RoleDetailsWrapper';
import CreateRolePage from './pages/CreateRolePage';
import ApiManagerAppDetails from './pages/ApiManagerAppDetails';
import FeatureDetails from './pages/FeatureDetails';
import ApiConfigDetailsPage from './pages/ApiConfigDetailsPage';
import MfaConditionListPage from './pages/MfaConditionListPage';
import MfaConditionDetailsPage from './pages/MfaConditionDetailsPage';
import MfaConditionCreatePage from './pages/MfaConditionCreatePage';
import IdentityServiceListPage from './pages/IdentityServiceListPage';
import IdentityServiceDetailsPage from './pages/IdentityServiceDetailsPage';
import CreateIdentityServicePage from './pages/CreateIdentityServicePage';
import StorageSettingsPage from './pages/StorageSettingsPage';
import LoggingConfigPage from './pages/LoggingConfigPage';
import FieldListPage from './pages/FieldListPage';
import FormListPage from './pages/FormListPage';
import FormDetailsPage from './pages/FormDetailsPage';
import AlertSettingsPage from './pages/AlertSettingsPage';
import AlertsPage from './pages/AlertsPage';
import AlertDetailsPage from './pages/AlertDetailsPage';
import AlertSubtypesPage from './pages/AlertSubtypesPage';
import LoginForm from './pages/login/LoginForm';
import SignupForm from './pages/login/SignupForm';
import UsersListPage from './pages/user/UsersListPage';
import CreateUserPage from './pages/user/CreateUserPage';
import UserDetailsPage from './pages/user/UserDetailsPage';
import DeploymentsListPage from './pages/deployment/DeploymentsListPage';
import DeploymentUploadPage from './pages/deployment/DeploymentUploadPage';
import DeploymentDetailsPage from './pages/deployment/DeploymentDetailsPage';
import OnBoardingConfiguration from './pages/onboarding/OnBoardingConfiguration';

import AuthTypeListPage from './pages/auth-type/AuthTypeListPage';
import AuthTypeDetailsPage from './pages/auth-type/AuthTypeDetailsPage';
import CreateAuthTypePage from './pages/auth-type/CreateAuthTypePage';

import api from './services/api';

function App() {

  useEffect(() => {
    const initApp = async () => {
      try {
        await api.post("/authentication/init");
        console.log("Init API called successfully ✔️");
      } catch (err) {
        console.error("Init API failed ❌", err);
      }
    };
    initApp();
  }, []);

  return (
    <Router>
      <ErrorBanner />
      <CssBaseline />
      <Container maxWidth={false} disableGutters>
        <Routes>
          {/* 🔹 Auth routes OUTSIDE Dashboard */}
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<LoginForm />} />
          <Route path="/signup" element={<SignupForm />} />

          {/* 🔹 Dashboard wrapped routes */}
          <Route
            path="/*"
            element={
              <Dashboard>
                <Routes>
                  {/* <Route path="/" element={<Navigate to="/authentication" replace />} /> */}
                  <Route path="/register" element={<RegisterPage />} />
                  <Route path="/user-roles" element={<UserRolesPage />} />
                  <Route path="/role-details" element={<RoleDetailsWrapper />} />
                  <Route path="/user-roles/create" element={<CreateRolePage />} />
                  <Route path="/auth-types" element={<AuthTypeListPage />} />
                  <Route path="/auth-types/:id" element={<AuthTypeDetailsPage />} />
                  <Route path="/auth-types/create" element={<CreateAuthTypePage />} />
                  <Route path="/certificates" element={<CertificatePage />} />
                  <Route path="/api-manager/:appId" element={<ApiManagerAppDetails />} />
                  <Route path="/api-manager/:appId/features/:id" element={<FeatureDetails />} />
                  <Route path="/action-details/:id" element={<ActionDetailsPage />} />
                  <Route path="/api-manager/:appId/config" element={<ApiConfigDetailsPage />} />
                  <Route path="/logging/:appId" element={<LoggingConfigPage />} />
                  <Route path="/mfa-condition" element={<MfaConditionListPage />} />
                  <Route path="/mfa-condition/:id" element={<MfaConditionDetailsPage />} />
                  <Route path="/mfa-condition/create" element={<MfaConditionCreatePage />} />
                  <Route path="/identity-service" element={<IdentityServiceListPage />} />
                  <Route path="/identity-service/:id" element={<IdentityServiceDetailsPage />} />
                  <Route path="/identity-service/create" element={<CreateIdentityServicePage />} />
                  <Route path="/storage-settings" element={<StorageSettingsPage />} />
                  <Route path="/alert-settings" element={<AlertSettingsPage />} />
                  <Route path="/field-list" element={<FieldListPage />} />
                  <Route path="/form-list" element={<FormListPage />} />
                  <Route path="/form/:formId" element={<FormDetailsPage />} />
                  <Route path="/alerts" element={<AlertsPage />} />
                  <Route path="/alerts/subtypes" element={<AlertSubtypesPage />} />
                  <Route path="/alerts/:alertId" element={<AlertDetailsPage />} />
                  <Route path="/users" element={<UsersListPage />} />
                  <Route path="/users/:userId" element={<UserDetailsPage />} />
                  <Route path="/users/create" element={<CreateUserPage />} />
                  <Route path="/deployments" element={<DeploymentsListPage/>} />
                  <Route path="/deployment/upload" element={<DeploymentUploadPage/>} />
                  <Route path="/deployment/details/:id" element={<DeploymentDetailsPage/>} />
                  <Route path="/onboarding-configuration" element={<OnBoardingConfiguration/>} />
                </Routes>
              </Dashboard>
            }
          />
        </Routes>
      </Container>
    </Router>
  );
}

export default App;