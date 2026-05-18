import React from 'react'
import { Outlet } from 'react-router-dom'
import { useSelector } from 'react-redux'
import { selectSidebarOpen } from '../../store/slices/uiSlice'
import Sidebar from './Sidebar'
import Header from './Header'

export default function AdminLayout() {
  const sidebarOpen = useSelector(selectSidebarOpen)

  return (
    <div className="flex h-screen bg-gray-50 overflow-hidden">
      {/* Sidebar */}
      <Sidebar />

      {/* Mobile overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 z-20 bg-black/40 lg:hidden" />
      )}

      {/* Main content */}
      <div className={`flex flex-col flex-1 min-w-0 transition-all duration-300 ${sidebarOpen ? 'lg:ml-64' : ''}`}>
        <Header />
        <main className="flex-1 overflow-y-auto p-4 lg:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
