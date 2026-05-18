import React, { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import {
  BarChart, Bar, LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, Legend, PieChart, Pie, Cell
} from 'recharts'
import { fetchKpiSummary, selectKpis, selectReportsLoading } from '../../store/slices/reportSlice'
import StatsCard from '../../components/common/StatsCard'
import { BedDouble, TrendingUp, DollarSign, Percent } from 'lucide-react'
import LoadingSpinner from '../../components/common/LoadingSpinner'

const HOTEL_ID = 1

const MONTHLY_DATA = [
  { month: 'Jan', occupancy: 68, revenue: 42000, revpar: 89 },
  { month: 'Feb', occupancy: 72, revenue: 48000, revpar: 95 },
  { month: 'Mar', occupancy: 81, revenue: 61000, revpar: 112 },
  { month: 'Apr', occupancy: 75, revenue: 55000, revpar: 104 },
  { month: 'May', occupancy: 88, revenue: 72000, revpar: 131 },
  { month: 'Jun', occupancy: 92, revenue: 85000, revpar: 148 },
]

const ROOM_TYPE_DATA = [
  { name: 'Standard Single', value: 28, color: '#3b82f6' },
  { name: 'Standard Double', value: 42, color: '#10b981' },
  { name: 'Deluxe Suite',    value: 20, color: '#f59e0b' },
  { name: 'Presidential',    value: 10, color: '#8b5cf6' },
]

export default function ReportsPage() {
  const dispatch = useDispatch()
  const kpis     = useSelector(selectKpis)
  const loading  = useSelector(selectReportsLoading)

  useEffect(() => {
    dispatch(fetchKpiSummary({ hotelId: HOTEL_ID }))
  }, [dispatch])

  return (
    <div className="space-y-6">
      {/* KPI Strip */}
      <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
        <StatsCard label="Occupancy Rate" value={`${kpis?.occupancyPct ?? 68}%`}  icon={Percent}    color="blue"   change={4} />
        <StatsCard label="RevPAR"         value="$131"                             icon={DollarSign} color="green"  change={12} />
        <StatsCard label="ADR"            value="$189"                             icon={TrendingUp} color="purple" change={7} />
        <StatsCard label="Total Revenue"  value="$72K"                             icon={DollarSign} color="yellow" change={18} />
      </div>

      {/* Monthly Occupancy */}
      <div className="card">
        <h3 className="text-sm font-semibold text-gray-700 mb-4">Monthly Occupancy Rate (%)</h3>
        <ResponsiveContainer width="100%" height={220}>
          <BarChart data={MONTHLY_DATA}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis dataKey="month" tick={{ fontSize: 12 }} />
            <YAxis tick={{ fontSize: 12 }} unit="%" domain={[0, 100]} />
            <Tooltip formatter={(v) => [`${v}%`, 'Occupancy']} />
            <Bar dataKey="occupancy" fill="#1a56db" radius={[4,4,0,0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {/* Revenue Trend */}
        <div className="card">
          <h3 className="text-sm font-semibold text-gray-700 mb-4">Monthly Revenue</h3>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={MONTHLY_DATA}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="month" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} tickFormatter={(v) => `$${v/1000}k`} />
              <Tooltip formatter={(v) => [`$${v.toLocaleString()}`, 'Revenue']} />
              <Line type="monotone" dataKey="revenue" stroke="#10b981" strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Room Type Distribution */}
        <div className="card">
          <h3 className="text-sm font-semibold text-gray-700 mb-4">Bookings by Room Type</h3>
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie data={ROOM_TYPE_DATA} cx="50%" cy="50%" outerRadius={80} dataKey="value" label={({name, percent}) => `${name} ${(percent*100).toFixed(0)}%`} labelLine={false}>
                {ROOM_TYPE_DATA.map(entry => <Cell key={entry.name} fill={entry.color} />)}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  )
}
