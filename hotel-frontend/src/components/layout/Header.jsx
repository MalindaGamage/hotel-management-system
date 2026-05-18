import React from 'react'
import { useLocation } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { Menu, Bell } from 'lucide-react'
import { toggleSidebar } from '../../store/slices/uiSlice'
import { selectCurrentUser } from '../../store/slices/authSlice'

const PAGE_TITLES = {
  '/admin/dashboard':    'Dashboard',
  '/admin/rooms':        'Room Management',
  '/admin/reservations': 'Reservations',
  '/admin/guests':       'Guest Management',
  '/admin/checkin':      'Check-In / Check-Out',
  '/admin/housekeeping': 'Housekeeping',
  '/admin/billing':      'Billing & Invoicing',
  '/admin/reports':      'Reports & Analytics',
}

export default function Header() {
  const dispatch  = useDispatch()
  const location  = useLocation()
  const user      = useSelector(selectCurrentUser)
  const pageTitle = PAGE_TITLES[location.pathname] ?? 'Hotel Management'

  return (
    <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-4 lg:px-6 flex-shrink-0">
      <div className="flex items-center gap-3">
        <button
          onClick={() => dispatch(toggleSidebar())}
          className="p-2 rounded-md text-gray-400 hover:text-gray-600 hover:bg-gray-100 lg:hidden"
        >
          <Menu size={20} />
        </button>
        <h1 className="text-lg font-semibold text-gray-900">{pageTitle}</h1>
      </div>

      <div className="flex items-center gap-2">
        <button className="relative p-2 rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100">
          <Bell size={20} />
          <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full" />
        </button>
        <div className="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center cursor-pointer">
          <span className="text-xs font-semibold text-primary-700">
            {user?.firstName?.[0] ?? user?.email?.[0]?.toUpperCase() ?? 'U'}
          </span>
        </div>
      </div>
    </header>
  )
}
