import React from 'react'
import clsx from 'clsx'
import LoadingSpinner from './LoadingSpinner'
import Pagination from './Pagination'

export default function DataTable({
  columns = [],
  data = [],
  pagination = null,
  onPageChange,
  loading = false,
  emptyMessage = 'No data found',
  onRowClick,
}) {
  if (loading) return <LoadingSpinner />

  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white">
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              {columns.map((col) => (
                <th key={col.key} className="table-header">{col.header}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {data.length === 0 ? (
              <tr>
                <td colSpan={columns.length} className="py-12 text-center text-sm text-gray-400">
                  {emptyMessage}
                </td>
              </tr>
            ) : (
              data.map((row, rowIdx) => (
                <tr
                  key={row.id ?? rowIdx}
                  className={clsx('table-row', onRowClick && 'cursor-pointer')}
                  onClick={() => onRowClick?.(row)}
                >
                  {columns.map((col) => (
                    <td key={col.key} className="table-cell">
                      {col.render ? col.render(row[col.key], row) : row[col.key] ?? '—'}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {pagination && (
        <div className="border-t border-gray-200 px-4 py-3">
          <Pagination
            page={pagination.page}
            totalPages={pagination.totalPages}
            totalElements={pagination.totalElements}
            size={pagination.size}
            onPageChange={onPageChange}
          />
        </div>
      )}
    </div>
  )
}
