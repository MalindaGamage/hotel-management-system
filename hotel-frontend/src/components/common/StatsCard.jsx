import React from 'react'
import { TrendingUp, TrendingDown } from 'lucide-react'
import clsx from 'clsx'

export default function StatsCard({ label, value, icon: Icon, change, changeLabel, color = 'blue' }) {
  const colorMap = {
    blue:   { bg: 'bg-blue-50',   icon: 'text-blue-600',   border: 'border-blue-100' },
    green:  { bg: 'bg-green-50',  icon: 'text-green-600',  border: 'border-green-100' },
    yellow: { bg: 'bg-yellow-50', icon: 'text-yellow-600', border: 'border-yellow-100' },
    purple: { bg: 'bg-purple-50', icon: 'text-purple-600', border: 'border-purple-100' },
    red:    { bg: 'bg-red-50',    icon: 'text-red-600',    border: 'border-red-100' },
  }
  const c = colorMap[color] ?? colorMap.blue
  const isPositive = change >= 0

  return (
    <div className="card hover:shadow-card-hover transition-shadow">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium text-gray-500 mb-1">{label}</p>
          <p className="text-2xl font-bold text-gray-900">{value}</p>
          {change !== undefined && (
            <div className={clsx('flex items-center gap-1 mt-1 text-xs font-medium',
              isPositive ? 'text-green-600' : 'text-red-600')}>
              {isPositive ? <TrendingUp size={12} /> : <TrendingDown size={12} />}
              <span>{Math.abs(change)}%</span>
              {changeLabel && <span className="text-gray-400 font-normal">{changeLabel}</span>}
            </div>
          )}
        </div>
        {Icon && (
          <div className={clsx('p-2.5 rounded-xl', c.bg, c.border, 'border')}>
            <Icon size={20} className={c.icon} />
          </div>
        )}
      </div>
    </div>
  )
}
