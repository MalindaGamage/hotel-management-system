import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { CalendarCheck, CalendarX } from 'lucide-react'
import { fetchReservations, checkInReservation, checkOutReservation, selectReservations, selectReservationsLoading } from '../../store/slices/reservationSlice'
import Badge from '../../components/common/Badge'
import LoadingSpinner from '../../components/common/LoadingSpinner'

const HOTEL_ID = 1
const TODAY = new Date().toISOString().split('T')[0]

export default function CheckInOutPage() {
  const dispatch     = useDispatch()
  const reservations = useSelector(selectReservations)
  const loading      = useSelector(selectReservationsLoading)
  const [tab, setTab] = useState('arrivals')

  useEffect(() => {
    if (tab === 'arrivals') {
      dispatch(fetchReservations({ hotelId: HOTEL_ID, status: 'CONFIRMED', checkIn: TODAY, checkOut: TODAY, page: 0, size: 50 }))
    } else {
      dispatch(fetchReservations({ hotelId: HOTEL_ID, status: 'CHECKED_IN', checkIn: TODAY, checkOut: TODAY, page: 0, size: 50 }))
    }
  }, [dispatch, tab])

  const rows = reservations?.content ?? []

  return (
    <div className="space-y-5">
      {/* Tabs */}
      <div className="flex gap-1 bg-gray-100 p-1 rounded-xl w-fit">
        {[
          { id: 'arrivals',   label: 'Expected Arrivals',   icon: CalendarCheck },
          { id: 'departures', label: 'Expected Departures', icon: CalendarX },
        ].map(({ id, label, icon: Icon }) => (
          <button
            key={id}
            onClick={() => setTab(id)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-all ${
              tab === id ? 'bg-white shadow text-primary-700' : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            <Icon size={15} />
            {label}
          </button>
        ))}
      </div>

      <div className="card">
        <h3 className="text-sm font-semibold text-gray-700 mb-4">
          {tab === 'arrivals' ? `Check-Ins for ${TODAY}` : `Check-Outs for ${TODAY}`}
          <span className="ml-2 text-gray-400 font-normal">({rows.length})</span>
        </h3>

        {loading ? <LoadingSpinner /> : (
          <div className="space-y-3">
            {rows.length === 0 && (
              <p className="text-center py-8 text-gray-400 text-sm">
                No {tab === 'arrivals' ? 'arrivals' : 'departures'} scheduled for today
              </p>
            )}
            {rows.map(r => (
              <div key={r.id} className="flex items-center justify-between p-4 border border-gray-100 rounded-xl hover:bg-gray-50">
                <div className="flex items-center gap-4">
                  <div className="w-10 h-10 rounded-full bg-primary-100 flex items-center justify-center">
                    <span className="text-sm font-semibold text-primary-700">
                      {r.guest?.firstName?.[0]}{r.guest?.lastName?.[0]}
                    </span>
                  </div>
                  <div>
                    <p className="font-medium text-sm">{r.guest?.firstName} {r.guest?.lastName}</p>
                    <p className="text-xs text-gray-400">{r.confirmationNumber} · {r.adults} adult(s)</p>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <div className="text-right hidden sm:block">
                    <p className="text-xs text-gray-500">{r.checkInDate} → {r.checkOutDate}</p>
                    <p className="text-xs font-medium text-gray-700">${Number(r.totalAmount).toLocaleString()}</p>
                  </div>
                  <Badge status={r.status} />
                  {tab === 'arrivals' && r.status === 'CONFIRMED' && (
                    <button
                      onClick={() => dispatch(checkInReservation(r.id))}
                      className="btn-primary text-xs px-3 py-1.5"
                    >
                      Check In
                    </button>
                  )}
                  {tab === 'departures' && r.status === 'CHECKED_IN' && (
                    <button
                      onClick={() => dispatch(checkOutReservation(r.id))}
                      className="text-xs px-3 py-1.5 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
                    >
                      Check Out
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
