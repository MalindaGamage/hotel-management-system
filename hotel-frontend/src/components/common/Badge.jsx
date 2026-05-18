import React from 'react'
import clsx from 'clsx'

const STATUS_STYLES = {
  // Room statuses
  AVAILABLE:   'bg-green-100 text-green-700',
  OCCUPIED:    'bg-red-100 text-red-700',
  DIRTY:       'bg-yellow-100 text-yellow-700',
  CLEAN:       'bg-blue-100 text-blue-700',
  INSPECTED:   'bg-emerald-100 text-emerald-700',
  OUT_OF_ORDER:'bg-gray-100 text-gray-500',
  MAINTENANCE: 'bg-orange-100 text-orange-700',
  // Reservation statuses
  PENDING:     'bg-yellow-100 text-yellow-700',
  CONFIRMED:   'bg-blue-100 text-blue-700',
  CHECKED_IN:  'bg-green-100 text-green-700',
  CHECKED_OUT: 'bg-purple-100 text-purple-700',
  CANCELLED:   'bg-red-100 text-red-700',
  NO_SHOW:     'bg-gray-100 text-gray-600',
  // Loyalty tiers
  BRONZE:   'bg-orange-100 text-orange-700',
  SILVER:   'bg-gray-100 text-gray-600',
  GOLD:     'bg-yellow-100 text-yellow-700',
  PLATINUM: 'bg-purple-100 text-purple-700',
  // Task statuses
  IN_PROGRESS: 'bg-blue-100 text-blue-700',
  COMPLETED:   'bg-green-100 text-green-700',
  SKIPPED:     'bg-gray-100 text-gray-500',
  // Priority
  LOW:    'bg-gray-100 text-gray-500',
  NORMAL: 'bg-blue-50 text-blue-600',
  HIGH:   'bg-orange-100 text-orange-700',
  URGENT: 'bg-red-100 text-red-700',
  // Invoice
  DRAFT:   'bg-gray-100 text-gray-600',
  ISSUED:  'bg-blue-100 text-blue-700',
  PAID:    'bg-green-100 text-green-700',
}

export default function Badge({ status, label, className }) {
  const text   = label ?? status?.replace(/_/g, ' ')
  const styles = STATUS_STYLES[status] ?? 'bg-gray-100 text-gray-600'
  return (
    <span className={clsx('badge', styles, className)}>
      {text}
    </span>
  )
}
