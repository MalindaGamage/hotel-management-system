import React, { useEffect, useState, useCallback } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { fetchTasks, updateTaskStatus, selectTasks, selectHousekeepingLoading } from '../../store/slices/housekeepingSlice'
import Badge from '../../components/common/Badge'
import LoadingSpinner from '../../components/common/LoadingSpinner'

const HOTEL_ID = 1
const TASK_STATUSES = ['PENDING','IN_PROGRESS','COMPLETED','SKIPPED']

export default function HousekeepingPage() {
  const dispatch = useDispatch()
  const tasks    = useSelector(selectTasks)
  const loading  = useSelector(selectHousekeepingLoading)
  const [statusFilter, setStatusFilter] = useState(null)

  const load = useCallback(() => {
    dispatch(fetchTasks({ hotelId: HOTEL_ID, page: 0, size: 50 }))
  }, [dispatch])

  useEffect(() => { load() }, [load])

  const filtered = statusFilter
    ? (tasks?.content ?? []).filter(t => t.status === statusFilter)
    : (tasks?.content ?? [])

  const handleStatusChange = (task, newStatus) => {
    dispatch(updateTaskStatus({ id: task.id, data: { status: newStatus } }))
  }

  const priorityColors = { LOW: 'text-gray-400', NORMAL: 'text-blue-500', HIGH: 'text-orange-500', URGENT: 'text-red-600' }

  return (
    <div className="space-y-5">
      {/* Filter tabs */}
      <div className="flex flex-wrap gap-2">
        {[null, ...TASK_STATUSES].map(s => (
          <button
            key={s ?? 'all'}
            onClick={() => setStatusFilter(s)}
            className={`px-3 py-1.5 rounded-full text-xs font-medium transition-all ${
              statusFilter === s
                ? 'bg-primary-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {s ? s.replace(/_/g, ' ') : 'All Tasks'}
          </button>
        ))}
      </div>

      {loading ? <LoadingSpinner /> : (
        <div className="grid gap-3">
          {filtered.length === 0 && (
            <div className="card text-center py-10 text-gray-400 text-sm">No tasks found</div>
          )}
          {filtered.map(task => (
            <div key={task.id} className="card flex items-center justify-between gap-4 py-4">
              <div className="flex items-center gap-3">
                <div className={`w-2 h-2 rounded-full ${
                  task.status === 'COMPLETED' ? 'bg-green-500' :
                  task.status === 'IN_PROGRESS' ? 'bg-blue-500' :
                  task.status === 'PENDING' ? 'bg-yellow-400' : 'bg-gray-300'
                }`} />
                <div>
                  <p className="font-medium text-sm">
                    Room <span className="text-primary-600">{task.room?.roomNumber}</span>
                    &nbsp;—&nbsp;{task.taskType?.replace(/_/g,' ')}
                  </p>
                  <p className="text-xs text-gray-400">
                    {task.scheduledDate}
                    {task.assignedTo && ` · ${task.assignedTo.firstName} ${task.assignedTo.lastName}`}
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-2 flex-shrink-0">
                <span className={`text-xs font-medium ${priorityColors[task.priority] ?? 'text-gray-500'}`}>
                  {task.priority}
                </span>
                <Badge status={task.status} />
                <select
                  value={task.status}
                  onChange={(e) => handleStatusChange(task, e.target.value)}
                  className="text-xs border border-gray-200 rounded-md px-2 py-1 bg-white focus:outline-none"
                >
                  {TASK_STATUSES.map(s => <option key={s} value={s}>{s.replace(/_/g,' ')}</option>)}
                </select>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
