import React from 'react'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import clsx from 'clsx'

export default function Pagination({ page, totalPages, totalElements, size, onPageChange }) {
  const start = page * size + 1
  const end   = Math.min((page + 1) * size, totalElements)

  const pages = Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
    if (totalPages <= 5) return i
    if (page <= 2) return i
    if (page >= totalPages - 3) return totalPages - 5 + i
    return page - 2 + i
  })

  return (
    <div className="flex items-center justify-between text-sm text-gray-500">
      <span>Showing {start}–{end} of {totalElements}</span>
      <div className="flex items-center gap-1">
        <button
          disabled={page === 0}
          onClick={() => onPageChange(page - 1)}
          className="p-1.5 rounded-md hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed"
        >
          <ChevronLeft size={16} />
        </button>
        {pages.map(p => (
          <button
            key={p}
            onClick={() => onPageChange(p)}
            className={clsx('w-8 h-8 rounded-md text-sm font-medium',
              p === page ? 'bg-primary-600 text-white' : 'hover:bg-gray-100')}
          >
            {p + 1}
          </button>
        ))}
        <button
          disabled={page >= totalPages - 1}
          onClick={() => onPageChange(page + 1)}
          className="p-1.5 rounded-md hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed"
        >
          <ChevronRight size={16} />
        </button>
      </div>
    </div>
  )
}
