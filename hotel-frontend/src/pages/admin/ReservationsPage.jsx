import React, { useEffect, useState, useCallback } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Plus } from 'lucide-react'
import {
  fetchReservations, checkInReservation, checkOutReservation, cancelReservation,
  selectReservations, selectReservationsLoading
} from '../../store/slices/reservationSlice'
import DataTable from '../../components/common/DataTable'
import Badge from '../../components/common/Badge'
import SearchFilter from '../../components/common/SearchFilter'
import Modal from '../../components/common/Modal'

const HOTEL_ID = 1
const STATUSES = ['PENDING','CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED','NO_SHOW']

export default function ReservationsPage() {
  const dispatch      = useDispatch()
  const reservations  = useSelector(selectReservations)
  const loading       = useSelector(selectReservationsLoading)
  const [statusFilter, setStatusFilter] = useState(null)
  const [search, setSearch] = useState('')
  const [page, setPage]     = useState(0)
  const [detail, setDetail] = useState(null)

  const load = useCallback(() => {
    dispatch(fetchReservations({ hotelId: HOTEL_ID, status: statusFilter, guestSearch: search, page, size: 20 }))
  }, [dispatch, statusFilter, search, page])

  useEffect(() => { load() }, [load])

  const columns = [
    { key: 'confirmationNumber', header: 'Confirmation', render: (v) => <span className="font-mono text-xs text-primary-600">{v}</span> },
    { key: 'guest',       header: 'Guest',     render: (_, r) => `${r.guest?.firstName} ${r.guest?.lastName}` },
    { key: 'checkInDate', header: 'Check-In'  },
    { key: 'checkOutDate',header: 'Check-Out' },
    { key: 'totalAmount', header: 'Total',     render: (v) => v ? `$${Number(v).toLocaleString()}` : '—' },
    { key: 'status',      header: 'Status',    render: (v) => <Badge status={v} /> },
    {
      key: 'id', header: 'Actions',
      render: (_, r) => (
        <div className="flex gap-1.5" onClick={(e) => e.stopPropagation()}>
          {r.status === 'CONFIRMED' && (
            <button onClick={() => dispatch(checkInReservation(r.id))}
              className="text-xs px-2 py-1 bg-green-600 text-white rounded-md hover:bg-green-700">
              Check In
            </button>
          )}
          {r.status === 'CHECKED_IN' && (
            <button onClick={() => dispatch(checkOutReservation(r.id))}
              className="text-xs px-2 py-1 bg-purple-600 text-white rounded-md hover:bg-purple-700">
              Check Out
            </button>
          )}
          {['PENDING','CONFIRMED'].includes(r.status) && (
            <button onClick={() => dispatch(cancelReservation({ id: r.id }))}
              className="text-xs px-2 py-1 bg-red-50 text-red-600 border border-red-200 rounded-md hover:bg-red-100">
              Cancel
            </button>
          )}
        </div>
      )
    },
  ]

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <p className="text-sm text-gray-500">{reservations?.totalElements ?? 0} reservations</p>
      </div>

      <SearchFilter
        placeholder="Search by guest name..."
        onSearch={setSearch}
        filters={[{
          key: 'status', label: 'All Statuses', value: statusFilter, onChange: setStatusFilter,
          options: STATUSES.map(s => ({ value: s, label: s.replace(/_/g,' ') }))
        }]}
      />

      <DataTable
        columns={columns}
        data={reservations?.content ?? []}
        loading={loading}
        pagination={{ page, size: 20, totalElements: reservations?.totalElements ?? 0, totalPages: reservations?.totalPages ?? 0 }}
        onPageChange={setPage}
        onRowClick={setDetail}
        emptyMessage="No reservations found"
      />

      <Modal isOpen={!!detail} onClose={() => setDetail(null)} title="Reservation Details" size="lg">
        {detail && (
          <div className="space-y-3 text-sm">
            <div className="grid grid-cols-2 gap-3">
              <div><p className="text-gray-500">Confirmation</p><p className="font-mono font-medium text-primary-600">{detail.confirmationNumber}</p></div>
              <div><p className="text-gray-500">Status</p><Badge status={detail.status} /></div>
              <div><p className="text-gray-500">Guest</p><p className="font-medium">{detail.guest?.firstName} {detail.guest?.lastName}</p></div>
              <div><p className="text-gray-500">Email</p><p>{detail.guest?.email}</p></div>
              <div><p className="text-gray-500">Check-In</p><p>{detail.checkInDate}</p></div>
              <div><p className="text-gray-500">Check-Out</p><p>{detail.checkOutDate}</p></div>
              <div><p className="text-gray-500">Total</p><p className="font-semibold">${Number(detail.totalAmount).toLocaleString()}</p></div>
              <div><p className="text-gray-500">Balance Due</p><p className="font-semibold text-red-600">${Number(detail.balanceDue ?? 0).toLocaleString()}</p></div>
            </div>
            {detail.specialRequests && <div><p className="text-gray-500">Special Requests</p><p className="bg-gray-50 rounded p-2 mt-1">{detail.specialRequests}</p></div>}
          </div>
        )}
      </Modal>
    </div>
  )
}
