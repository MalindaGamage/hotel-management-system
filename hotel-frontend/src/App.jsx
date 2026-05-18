import React, { useEffect } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { rehydrateAuth } from './store/slices/authSlice'

import ProtectedRoute from './components/auth/ProtectedRoute'
import AdminLayout from './components/layout/AdminLayout'

// Public pages
import HomePage from './pages/public/HomePage'
import BookingPage from './pages/public/BookingPage'
import LoginPage from './pages/auth/LoginPage'

// Admin pages
import DashboardPage from './pages/admin/DashboardPage'
import RoomsPage from './pages/admin/RoomsPage'
import ReservationsPage from './pages/admin/ReservationsPage'
import GuestsPage from './pages/admin/GuestsPage'
import CheckInOutPage from './pages/admin/CheckInOutPage'
import HousekeepingPage from './pages/admin/HousekeepingPage'
import BillingPage from './pages/admin/BillingPage'
import ReportsPage from './pages/admin/ReportsPage'

export default function App() {
  const dispatch = useDispatch()

  // Rehydrate auth state from localStorage on app load
  useEffect(() => {
    dispatch(rehydrateAuth())
  }, [dispatch])

  return (
    <Routes>
      {/* Public routes */}
      <Route path="/"       element={<HomePage />} />
      <Route path="/booking" element={<BookingPage />} />
      <Route path="/login"  element={<LoginPage />} />

      {/* Protected admin routes */}
      <Route path="/admin" element={
        <ProtectedRoute>
          <AdminLayout />
        </ProtectedRoute>
      }>
        <Route index element={<Navigate to="/admin/dashboard" replace />} />
        <Route path="dashboard"    element={<DashboardPage />} />
        <Route path="rooms"        element={
          <ProtectedRoute requiredRoles={['SUPER_ADMIN','RECEPTIONIST']}>
            <RoomsPage />
          </ProtectedRoute>
        } />
        <Route path="reservations" element={
          <ProtectedRoute requiredRoles={['SUPER_ADMIN','RECEPTIONIST']}>
            <ReservationsPage />
          </ProtectedRoute>
        } />
        <Route path="guests"       element={
          <ProtectedRoute requiredRoles={['SUPER_ADMIN','RECEPTIONIST']}>
            <GuestsPage />
          </ProtectedRoute>
        } />
        <Route path="checkin"      element={
          <ProtectedRoute requiredRoles={['SUPER_ADMIN','RECEPTIONIST']}>
            <CheckInOutPage />
          </ProtectedRoute>
        } />
        <Route path="housekeeping" element={
          <ProtectedRoute requiredRoles={['SUPER_ADMIN','HOUSEKEEPING']}>
            <HousekeepingPage />
          </ProtectedRoute>
        } />
        <Route path="billing"      element={
          <ProtectedRoute requiredRoles={['SUPER_ADMIN','RECEPTIONIST']}>
            <BillingPage />
          </ProtectedRoute>
        } />
        <Route path="reports"      element={
          <ProtectedRoute requiredRoles={['SUPER_ADMIN']}>
            <ReportsPage />
          </ProtectedRoute>
        } />
      </Route>

      {/* Catch-all */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
