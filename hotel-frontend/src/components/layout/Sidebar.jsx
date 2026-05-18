import React from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import {
  LayoutDashboard, BedDouble, CalendarDays, Users, LogIn,
  Sparkles, Receipt, BarChart3, Settings, LogOut, Hotel, X
} from 'lucide-react'
import { selectSidebarOpen, toggleSidebar } from '../../store/slices/uiSlice'
import { selectCurrentUser, logoutUser } from '../../store/slices/authSlice'
import clsx from 'clsx'

const NAV_ITEMS = [
  { to: '/admin/dashboard',    label: 'Dashboard',    icon: LayoutDashboard },
  { to: '/admin/rooms',        label: 'Rooms',        icon: BedDouble       },
  { to: '/admin/reservations', label: 'Reservations', icon: CalendarDays    },
  { to: '/admin/guests',       label: 'Guests',       icon: Users           },
  { to: '/admin/checkin',      label: 'Check-In/Out', icon: LogIn           },
  { to: '/admin/housekeeping', label: 'Housekeeping', icon: Sparkles        },
  { to: '/admin/billing',      label: 'Billing',      icon: Receipt         },
  { to: '/admin/reports',      label: 'Reports',      icon: BarChart3       },
]

export default function Sidebar() {
  const sidebarOpen = useSelector(selectSidebarOpen)
  const user        = useSelector(selectCurrentUser)
  const dispatch    = useDispatch()
  const navigate    = useNavigate()

  const handleLogout = async () => {
    await dispatch(logoutUser())
    navigate('/login')
  }

  return (
    <aside className={clsx(
      'fixed inset-y-0 left-0 z-30 w-64 bg-white border-r border-gray-200',
      'flex flex-col transition-transform duration-300',
      sidebarOpen ? 'translate-x-0' : '-translate-x-full',
      'lg:translate-x-0'
    )}>
      {/* Logo */}
      <div className="flex items-center justify-between h-16 px-4 border-b border-gray-100">
        <div className="flex items-center gap-2">
          <Hotel className="w-7 h-7 text-primary-600" />
          <span className="font-bold text-gray-900 text-lg leading-tight">Grand Horizon</span>
        </div>
        <button
          onClick={() => dispatch(toggleSidebar())}
          className="lg:hidden p-1.5 rounded-md text-gray-400 hover:text-gray-600 hover:bg-gray-100"
        >
          <X size={18} />
        </button>
      </div>

      {/* Navigation */}
      <nav className="flex-1 overflow-y-auto py-4 px-3 space-y-0.5">
        {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) => clsx('sidebar-link', isActive && 'active')}
          >
            <Icon size={18} />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>

      {/* User info + logout */}
      <div className="border-t border-gray-100 p-4">
        {user && (
          <div className="flex items-center gap-3 mb-3">
            <div className="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center">
              <span className="text-xs font-semibold text-primary-700">
                {user.firstName?.[0] ?? user.email?.[0]?.toUpperCase()}
              </span>
            </div>
            <div className="min-w-0">
              <p className="text-sm font-medium text-gray-900 truncate">
                {user.firstName ? `${user.firstName} ${user.lastName}` : user.email}
              </p>
              <p className="text-xs text-gray-500 truncate">{user.role}</p>
            </div>
          </div>
        )}
        <button onClick={handleLogout}
          className="flex items-center gap-2 w-full px-3 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg transition-colors">
          <LogOut size={16} />
          Sign out
        </button>
      </div>
    </aside>
  )
}
