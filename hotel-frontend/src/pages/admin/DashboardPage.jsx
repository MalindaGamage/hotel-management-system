import React, { useEffect, useCallback } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { BedDouble, CalendarCheck, CalendarX, DollarSign, TrendingUp } from 'lucide-react'
import {
  LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend
} from 'recharts'
import { fetchKpiSummary, selectKpis, selectReportsLoading } from '../../store/slices/reportSlice'
import { fetchReservations, selectReservations } from '../../store/slices/reservationSlice'
import StatsCard from '../../components/common/StatsCard'
import Badge from '../../components/common/Badge'
import LoadingSpinner from '../../components/common/LoadingSpinner'

const HOTEL_ID = 1

// Static demo data for charts
const REVENUE_DATA = [
  { day: 'Mon', revenue: 4200 }, { day: 'Tue', revenue: 5100 },
  { day: 'Wed', revenue: 3800 }, { day: 'Thu', revenue: 6200 },
  { day: 'Fri', revenue: 7800 }, { day: 'Sat', revenue: 9400 },
  { day: 'Sun', revenue: 8100 },
]

const ROOM_STATUS_DATA = [
  { name: 'Available', value: 12, color: '#10b981' },
  { name: 'Occupied',  value: 6,  color: '#ef4444' },
  { name: 'Dirty',     value: 2,  color: '#f59e0b' },
  { name: 'Other',     value: 2,  color: '#6b7280' },
]

export default function DashboardPage() {
  const dispatch  = useDispatch()
  const kpis      = useSelector(selectKpis)
  const loading   = useSelector(selectReportsLoading)
  const reservations = useSelector(selectReservations)

  const loadData = useCallback(() => {
    dispatch(fetchKpiSummary({ hotelId: HOTEL_ID }))
    dispatch(fetchReservations({ hotelId: HOTEL_ID, page: 0, size: 5, sort: 'createdAt' }))
  }, [dispatch])

  useEffect(() => {
    loadData()
    // Poll room counts every 30 seconds
    const interval = setInterval(loadData, 30000)
    return () => clearInterval(interval)
  }, [loadData])

  return (
    <div className="space-y-6">
      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <StatsCard label="Total Rooms"        value={kpis?.totalRooms ?? 22}    icon={BedDouble}      color="blue"   change={0} />
        <StatsCard label="Today's Check-Ins"  value={kpis?.arrivalsToday ?? 3}  icon={CalendarCheck}  color="green"  change={12} changeLabel="vs yesterday" />
        <StatsCard label="Today's Check-Outs" value={kpis?.departurestoday ?? 2} icon={CalendarX}     color="yellow" change={-5} changeLabel="vs yesterday" />
        <StatsCard label="Occupancy Rate"     value={`${kpis?.occupancyPct ?? 0}%`} icon={TrendingUp} color="purple" change={8} changeLabel="vs last week" />
      </div>

      {/* Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Revenue trend */}
        <div className="card lg:col-span-2">
          <h3 className="text-sm font-semibold text-gray-700 mb-4">Revenue — Last 7 Days</h3>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={REVENUE_DATA}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="day" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} tickFormatter={(v) => `$${v/1000}k`} />
              <Tooltip formatter={(v) => [`$${v.toLocaleString()}`, 'Revenue']} />
              <Line type="monotone" dataKey="revenue" stroke="#1a56db" strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Room Status Pie */}
        <div className="card">
          <h3 className="text-sm font-semibold text-gray-700 mb-4">Room Status</h3>
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie data={ROOM_STATUS_DATA} cx="50%" cy="50%" innerRadius={55} outerRadius={80}
                dataKey="value" paddingAngle={2}>
                {ROOM_STATUS_DATA.map(entry => (
                  <Cell key={entry.name} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip />
              <Legend iconType="circle" iconSize={8} wrapperStyle={{ fontSize: 12 }} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Recent Reservations */}
      <div className="card">
        <h3 className="text-sm font-semibold text-gray-700 mb-4">Recent Reservations</h3>
        {loading ? <LoadingSpinner /> : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-gray-100">
                  <th className="table-header">Confirmation</th>
                  <th className="table-header">Guest</th>
                  <th className="table-header">Check-In</th>
                  <th className="table-header">Check-Out</th>
                  <th className="table-header">Amount</th>
                  <th className="table-header">Status</th>
                </tr>
              </thead>
              <tbody>
                {(reservations?.content ?? []).slice(0, 5).map(r => (
                  <tr key={r.id} className="table-row">
                    <td className="table-cell font-mono text-xs text-primary-600">{r.confirmationNumber}</td>
                    <td className="table-cell">{r.guest?.firstName} {r.guest?.lastName}</td>
                    <td className="table-cell text-sm">{r.checkInDate}</td>
                    <td className="table-cell text-sm">{r.checkOutDate}</td>
                    <td className="table-cell font-medium">${r.totalAmount?.toLocaleString()}</td>
                    <td className="table-cell"><Badge status={r.status} /></td>
                  </tr>
                ))}
                {!reservations?.content?.length && (
                  <tr><td colSpan={6} className="py-8 text-center text-sm text-gray-400">No recent reservations</td></tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
