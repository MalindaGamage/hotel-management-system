import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { ChevronLeft, ChevronRight, Hotel, Check } from 'lucide-react'
import DatePicker from 'react-datepicker'
import 'react-datepicker/dist/react-datepicker.css'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import {
  setWizardStep, setWizardDates, setWizardRoom, setWizardGuest,
  createReservation, selectBookingWizard
} from '../../store/slices/reservationSlice'
import { fetchRoomAvailability, selectRoomAvailability } from '../../store/slices/roomSlice'

const HOTEL_ID = 1
const STEPS = ['Dates & Rooms', 'Guest Info', 'Confirm & Pay']

const guestSchema = yup.object({
  firstName: yup.string().required('Required'),
  lastName:  yup.string().required('Required'),
  email:     yup.string().email().required('Required'),
  phone:     yup.string().required('Required'),
})

function StepIndicator({ current }) {
  return (
    <div className="flex items-center justify-center gap-0 mb-8">
      {STEPS.map((label, i) => (
        <React.Fragment key={label}>
          <div className="flex flex-col items-center">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-semibold transition-all ${
              i < current ? 'bg-green-500 text-white' :
              i === current ? 'bg-primary-600 text-white' :
              'bg-gray-200 text-gray-500'
            }`}>
              {i < current ? <Check size={14} /> : i + 1}
            </div>
            <span className="text-xs mt-1 text-gray-500 whitespace-nowrap">{label}</span>
          </div>
          {i < STEPS.length - 1 && (
            <div className={`h-0.5 w-16 sm:w-24 mx-1 mb-5 ${i < current ? 'bg-green-500' : 'bg-gray-200'}`} />
          )}
        </React.Fragment>
      ))}
    </div>
  )
}

export default function BookingPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const wizard   = useSelector(selectBookingWizard)
  const available = useSelector(selectRoomAvailability)

  const [checkIn,  setCheckIn]  = useState(null)
  const [checkOut, setCheckOut] = useState(null)
  const [adults,   setAdults]   = useState(1)
  const [loading,  setLoading]  = useState(false)

  const { register, handleSubmit, formState: { errors } } = useForm({ resolver: yupResolver(guestSchema) })

  const searchRooms = () => {
    if (!checkIn || !checkOut) return
    const ci = checkIn.toISOString().split('T')[0]
    const co = checkOut.toISOString().split('T')[0]
    dispatch(setWizardDates({ checkIn: ci, checkOut: co, adults, children: 0 }))
    dispatch(fetchRoomAvailability({ hotelId: HOTEL_ID, checkIn: ci, checkOut: co }))
  }

  const selectRoom = (room) => {
    dispatch(setWizardRoom(room))
    dispatch(setWizardStep(2))
  }

  const onGuestSubmit = (data) => {
    dispatch(setWizardGuest(data))
    dispatch(setWizardStep(3))
  }

  const onConfirm = async () => {
    setLoading(true)
    const { dates, room, guestInfo } = wizard
    try {
      // In a real app, first create/find guest, then create reservation
      const result = await dispatch(createReservation({
        hotelId: HOTEL_ID,
        guestId: 1, // Demo
        checkInDate:  dates.checkIn,
        checkOutDate: dates.checkOut,
        adults: dates.adults,
        children: 0,
        rooms: [{ roomId: room.id, roomTypeId: room.roomType?.id }],
        source: 'DIRECT',
      }))
      if (createReservation.fulfilled.match(result)) {
        navigate('/')
      }
    } finally {
      setLoading(false)
    }
  }

  const nights = checkIn && checkOut
    ? Math.round((checkOut - checkIn) / (1000*60*60*24))
    : 0

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white border-b border-gray-200 py-4 px-4 sm:px-6">
        <div className="max-w-2xl mx-auto flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 text-hotel-navy font-display font-bold text-lg">
            <Hotel size={22} className="text-primary-600" />
            Grand Horizon
          </Link>
          <Link to="/" className="text-sm text-gray-500 hover:text-gray-700 flex items-center gap-1">
            <ChevronLeft size={14} /> Back
          </Link>
        </div>
      </header>

      <div className="max-w-2xl mx-auto px-4 py-10">
        <StepIndicator current={wizard.step - 1} />

        {/* Step 1: Dates & Rooms */}
        {wizard.step === 1 && (
          <div className="space-y-6">
            <div className="card">
              <h2 className="font-semibold text-gray-800 mb-4">Select Your Dates</h2>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="form-label">Check-In</label>
                  <DatePicker
                    selected={checkIn}
                    onChange={setCheckIn}
                    selectsStart startDate={checkIn} endDate={checkOut}
                    minDate={new Date()}
                    placeholderText="Select date"
                    className="form-input w-full"
                  />
                </div>
                <div>
                  <label className="form-label">Check-Out</label>
                  <DatePicker
                    selected={checkOut}
                    onChange={setCheckOut}
                    selectsEnd startDate={checkIn} endDate={checkOut}
                    minDate={checkIn ?? new Date()}
                    placeholderText="Select date"
                    className="form-input w-full"
                  />
                </div>
              </div>
              <div className="mt-4">
                <label className="form-label">Adults</label>
                <select value={adults} onChange={(e) => setAdults(Number(e.target.value))} className="form-select w-24">
                  {[1,2,3,4].map(n => <option key={n} value={n}>{n}</option>)}
                </select>
              </div>
              <button onClick={searchRooms} disabled={!checkIn || !checkOut}
                className="btn-primary mt-4 w-full justify-center">
                Search Available Rooms
              </button>
            </div>

            {/* Available rooms */}
            {available.length > 0 && (
              <div className="space-y-3">
                <h3 className="font-semibold text-gray-700">
                  {available.length} rooms available for {nights} night{nights !== 1 ? 's' : ''}
                </h3>
                {available.map(room => (
                  <div key={room.id} className="card flex items-center justify-between gap-4">
                    <div>
                      <p className="font-medium">{room.roomType?.name}</p>
                      <p className="text-sm text-gray-500">Floor {room.floor} · Room {room.roomNumber}</p>
                      <div className="flex flex-wrap gap-1 mt-1">
                        {(room.roomType?.amenities ?? []).slice(0,3).map(a => (
                          <span key={a} className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded-full">{a}</span>
                        ))}
                      </div>
                    </div>
                    <div className="text-right flex-shrink-0">
                      <p className="text-xl font-bold text-hotel-navy">${room.roomType?.basePrice}<span className="text-sm font-normal text-gray-400">/night</span></p>
                      <p className="text-sm text-gray-500">${(room.roomType?.basePrice * nights).toLocaleString()} total</p>
                      <button onClick={() => selectRoom(room)} className="btn-primary mt-2 text-sm">Select</button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Step 2: Guest Info */}
        {wizard.step === 2 && (
          <div className="card">
            <h2 className="font-semibold text-gray-800 mb-4">Guest Information</h2>
            <form onSubmit={handleSubmit(onGuestSubmit)} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="form-label">First Name</label>
                  <input {...register('firstName')} className="form-input" />
                  {errors.firstName && <p className="form-error">{errors.firstName.message}</p>}
                </div>
                <div>
                  <label className="form-label">Last Name</label>
                  <input {...register('lastName')} className="form-input" />
                  {errors.lastName && <p className="form-error">{errors.lastName.message}</p>}
                </div>
              </div>
              <div>
                <label className="form-label">Email</label>
                <input {...register('email')} type="email" className="form-input" />
                {errors.email && <p className="form-error">{errors.email.message}</p>}
              </div>
              <div>
                <label className="form-label">Phone</label>
                <input {...register('phone')} type="tel" className="form-input" />
                {errors.phone && <p className="form-error">{errors.phone.message}</p>}
              </div>
              <div>
                <label className="form-label">Special Requests (optional)</label>
                <textarea {...register('specialRequests')} className="form-input" rows={3} />
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => dispatch(setWizardStep(1))} className="btn-secondary flex-1">
                  <ChevronLeft size={15} /> Back
                </button>
                <button type="submit" className="btn-primary flex-1">
                  Continue <ChevronRight size={15} />
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Step 3: Confirm */}
        {wizard.step === 3 && (
          <div className="space-y-4">
            <div className="card">
              <h2 className="font-semibold text-gray-800 mb-4">Booking Summary</h2>
              <div className="space-y-3 text-sm">
                <div className="flex justify-between"><span className="text-gray-500">Room</span><span className="font-medium">{wizard.room?.roomType?.name} — #{wizard.room?.roomNumber}</span></div>
                <div className="flex justify-between"><span className="text-gray-500">Check-In</span><span>{wizard.dates?.checkIn}</span></div>
                <div className="flex justify-between"><span className="text-gray-500">Check-Out</span><span>{wizard.dates?.checkOut}</span></div>
                <div className="flex justify-between"><span className="text-gray-500">Nights</span><span>{nights}</span></div>
                <div className="flex justify-between"><span className="text-gray-500">Guest</span><span>{wizard.guestInfo?.firstName} {wizard.guestInfo?.lastName}</span></div>
                <hr />
                <div className="flex justify-between text-base font-bold">
                  <span>Total</span>
                  <span>${(wizard.room?.roomType?.basePrice * nights).toLocaleString()}</span>
                </div>
              </div>
            </div>

            <div className="card">
              <h3 className="font-semibold text-gray-800 mb-3">Payment Details</h3>
              <div className="space-y-3">
                <div><label className="form-label">Card Number</label>
                  <input className="form-input" placeholder="4242 4242 4242 4242" /></div>
                <div className="grid grid-cols-2 gap-3">
                  <div><label className="form-label">Expiry</label><input className="form-input" placeholder="MM/YY" /></div>
                  <div><label className="form-label">CVV</label><input className="form-input" placeholder="•••" /></div>
                </div>
              </div>
            </div>

            <div className="flex gap-3">
              <button onClick={() => dispatch(setWizardStep(2))} className="btn-secondary flex-1">
                <ChevronLeft size={15} /> Back
              </button>
              <button onClick={onConfirm} disabled={loading} className="btn-primary flex-1 justify-center">
                {loading ? 'Confirming…' : 'Confirm Booking'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
