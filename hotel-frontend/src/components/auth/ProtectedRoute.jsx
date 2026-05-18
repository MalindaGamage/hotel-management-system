import React from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useSelector } from 'react-redux'
import { selectIsAuthenticated, selectUserRoles } from '../../store/slices/authSlice'

export default function ProtectedRoute({ children, requiredRoles = [] }) {
  const isAuthenticated = useSelector(selectIsAuthenticated)
  const userRoles       = useSelector(selectUserRoles)
  const location        = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (requiredRoles.length > 0) {
    const hasRole = requiredRoles.some(role =>
      userRoles.some(r => r === `ROLE_${role}` || r === role)
    )
    if (!hasRole) {
      return (
        <div className="flex flex-col items-center justify-center min-h-[60vh] text-center">
          <div className="text-6xl mb-4">🚫</div>
          <h2 className="text-2xl font-bold text-gray-800 mb-2">Access Denied</h2>
          <p className="text-gray-500">You don't have permission to view this page.</p>
        </div>
      )
    }
  }

  return children
}
