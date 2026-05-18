import React, { useEffect, useState, useCallback } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Plus, RefreshCw } from 'lucide-react'
import { fetchRooms, updateRoomStatus, createRoom, selectRooms, selectRoomsLoading, optimisticStatusUpdate } from '../../store/slices/roomSlice'
import DataTable from '../../components/common/DataTable'
import Modal from '../../components/common/Modal'
import Badge from '../../components/common/Badge'
import SearchFilter from '../../components/common/SearchFilter'
import { useForm } from 'react-hook-form'

const HOTEL_ID = 1
const ROOM_STATUSES = ['AVAILABLE','OCCUPIED','DIRTY','CLEAN','INSPECTED','OUT_OF_ORDER','MAINTENANCE']

export default function RoomsPage() {
  const dispatch = useDispatch()
  const rooms    = useSelector(selectRooms)
  const loading  = useSelector(selectRoomsLoading)
  const [showCreate, setShowCreate] = useState(false)
  const [statusFilter, setStatusFilter] = useState(null)
  const [page, setPage] = useState(0)

  const load = useCallback(() => {
    dispatch(fetchRooms({ hotelId: HOTEL_ID, page, size: 20 }))
  }, [dispatch, page])

  useEffect(() => { load() }, [load])

  const { register, handleSubmit, reset, formState: { errors } } = useForm()

  const onCreateRoom = (data) => {
    dispatch(createRoom({ ...data, hotelId: HOTEL_ID, floor: Number(data.floor) }))
    setShowCreate(false)
    reset()
  }

  const handleStatusChange = (room, newStatus) => {
    dispatch(optimisticStatusUpdate({ id: room.id, status: newStatus }))
    dispatch(updateRoomStatus({ id: room.id, status: newStatus }))
  }

  const filtered = statusFilter
    ? (rooms?.content ?? []).filter(r => r.status === statusFilter)
    : (rooms?.content ?? [])

  const columns = [
    { key: 'roomNumber', header: 'Room #', render: (v) => <span className="font-mono font-medium">{v}</span> },
    { key: 'roomType',   header: 'Type',    render: (v) => v?.name ?? '—' },
    { key: 'floor',      header: 'Floor' },
    { key: 'status',     header: 'Status',  render: (v) => <Badge status={v} /> },
    {
      key: 'id', header: 'Actions',
      render: (_, row) => (
        <select
          value={row.status}
          onChange={(e) => handleStatusChange(row, e.target.value)}
          onClick={(e) => e.stopPropagation()}
          className="text-xs border border-gray-200 rounded-md px-2 py-1 bg-white focus:outline-none focus:ring-1 focus:ring-primary-500"
        >
          {ROOM_STATUSES.map(s => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
        </select>
      )
    },
  ]

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <p className="text-sm text-gray-500">{rooms?.totalElements ?? 0} rooms</p>
        <div className="flex gap-2">
          <button onClick={load} className="btn-secondary"><RefreshCw size={15} /> Refresh</button>
          <button onClick={() => setShowCreate(true)} className="btn-primary"><Plus size={15} /> Add Room</button>
        </div>
      </div>

      <SearchFilter
        placeholder="Search by room number..."
        filters={[{
          key: 'status', label: 'All Statuses',
          value: statusFilter,
          onChange: setStatusFilter,
          options: ROOM_STATUSES.map(s => ({ value: s, label: s.replace(/_/g, ' ') }))
        }]}
      />

      <DataTable
        columns={columns}
        data={filtered}
        loading={loading}
        pagination={{ page, size: 20, totalElements: rooms?.totalElements ?? 0, totalPages: rooms?.totalPages ?? 0 }}
        onPageChange={setPage}
        emptyMessage="No rooms found"
      />

      <Modal isOpen={showCreate} onClose={() => { setShowCreate(false); reset() }} title="Add New Room"
        footer={<>
          <button className="btn-secondary" onClick={() => { setShowCreate(false); reset() }}>Cancel</button>
          <button className="btn-primary" form="create-room-form" type="submit">Create</button>
        </>}>
        <form id="create-room-form" onSubmit={handleSubmit(onCreateRoom)} className="space-y-4">
          <div>
            <label className="form-label">Room Number</label>
            <input {...register('roomNumber', { required: 'Required' })} className="form-input" placeholder="e.g. 301" />
            {errors.roomNumber && <p className="form-error">{errors.roomNumber.message}</p>}
          </div>
          <div>
            <label className="form-label">Floor</label>
            <input {...register('floor', { required: 'Required', min: 0 })} type="number" className="form-input" defaultValue={1} />
          </div>
          <div>
            <label className="form-label">Room Type ID</label>
            <input {...register('roomTypeId', { required: 'Required' })} type="number" className="form-input" placeholder="1" />
          </div>
          <div>
            <label className="form-label">Notes</label>
            <textarea {...register('notes')} className="form-input" rows={2} />
          </div>
        </form>
      </Modal>
    </div>
  )
}
