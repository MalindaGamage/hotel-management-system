import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { reservationApi } from '../../api/reservationApi'
import toast from 'react-hot-toast'

export const fetchReservations = createAsyncThunk('reservations/fetchAll',
  async (params, { rejectWithValue }) => {
    try { return (await reservationApi.getAll(params)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

export const fetchReservationById = createAsyncThunk('reservations/fetchById',
  async (id, { rejectWithValue }) => {
    try { return (await reservationApi.getById(id)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

export const createReservation = createAsyncThunk('reservations/create',
  async (data, { rejectWithValue }) => {
    try { return (await reservationApi.create(data)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail || 'Booking failed') }
  })

export const checkInReservation = createAsyncThunk('reservations/checkIn',
  async (id, { rejectWithValue }) => {
    try { return (await reservationApi.checkIn(id)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

export const checkOutReservation = createAsyncThunk('reservations/checkOut',
  async (id, { rejectWithValue }) => {
    try { return (await reservationApi.checkOut(id)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

export const cancelReservation = createAsyncThunk('reservations/cancel',
  async ({ id, reason }, { rejectWithValue }) => {
    try { return (await reservationApi.cancel(id, reason)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

const reservationSlice = createSlice({
  name: 'reservations',
  initialState: {
    reservations:    { content: [], totalElements: 0, totalPages: 0, page: 0 },
    selectedReservation: null,
    bookingWizard: {
      step:      1,
      dates:     { checkIn: null, checkOut: null, adults: 1, children: 0 },
      room:      null,
      roomType:  null,
      guestInfo: null,
    },
    loading: false,
    error:   null,
  },
  reducers: {
    setWizardStep:  (s, { payload }) => { s.bookingWizard.step = payload },
    setWizardDates: (s, { payload }) => { s.bookingWizard.dates = payload },
    setWizardRoom:  (s, { payload }) => { s.bookingWizard.room = payload },
    setWizardGuest: (s, { payload }) => { s.bookingWizard.guestInfo = payload },
    resetWizard:    (s) => {
      s.bookingWizard = { step: 1, dates: { checkIn: null, checkOut: null, adults: 1, children: 0 }, room: null, roomType: null, guestInfo: null }
    },
    setSelectedReservation: (s, { payload }) => { s.selectedReservation = payload },
  },
  extraReducers: (b) => {
    b
      .addCase(fetchReservations.pending,   (s) => { s.loading = true })
      .addCase(fetchReservations.fulfilled, (s, { payload }) => { s.loading = false; s.reservations = payload })
      .addCase(fetchReservations.rejected,  (s, { payload }) => { s.loading = false; s.error = payload })

      .addCase(fetchReservationById.fulfilled, (s, { payload }) => { s.selectedReservation = payload })

      .addCase(createReservation.fulfilled, (s, { payload }) => {
        s.reservations.content.unshift(payload)
        toast.success(`Reservation ${payload.confirmationNumber} created!`)
      })
      .addCase(createReservation.rejected, (s, { payload }) => {
        toast.error(payload || 'Booking failed')
      })

      .addCase(checkInReservation.fulfilled, (s, { payload }) => {
        const idx = s.reservations.content.findIndex(r => r.id === payload.id)
        if (idx !== -1) s.reservations.content[idx] = payload
        toast.success('Guest checked in successfully')
      })
      .addCase(checkOutReservation.fulfilled, (s, { payload }) => {
        const idx = s.reservations.content.findIndex(r => r.id === payload.id)
        if (idx !== -1) s.reservations.content[idx] = payload
        toast.success('Guest checked out successfully')
      })
      .addCase(cancelReservation.fulfilled, (s, { payload }) => {
        const idx = s.reservations.content.findIndex(r => r.id === payload.id)
        if (idx !== -1) s.reservations.content[idx] = payload
        toast.success('Reservation cancelled')
      })
  },
})

export const { setWizardStep, setWizardDates, setWizardRoom, setWizardGuest, resetWizard, setSelectedReservation } = reservationSlice.actions
export default reservationSlice.reducer

export const selectReservations        = (s) => s.reservations.reservations
export const selectSelectedReservation = (s) => s.reservations.selectedReservation
export const selectBookingWizard       = (s) => s.reservations.bookingWizard
export const selectReservationsLoading = (s) => s.reservations.loading
