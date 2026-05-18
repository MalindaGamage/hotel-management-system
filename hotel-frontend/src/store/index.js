import { configureStore } from '@reduxjs/toolkit'
import authReducer from './slices/authSlice'
import roomReducer from './slices/roomSlice'
import reservationReducer from './slices/reservationSlice'
import guestReducer from './slices/guestSlice'
import housekeepingReducer from './slices/housekeepingSlice'
import billingReducer from './slices/billingSlice'
import reportReducer from './slices/reportSlice'
import uiReducer from './slices/uiSlice'

export const store = configureStore({
  reducer: {
    auth:         authReducer,
    rooms:        roomReducer,
    reservations: reservationReducer,
    guests:       guestReducer,
    housekeeping: housekeepingReducer,
    billing:      billingReducer,
    reports:      reportReducer,
    ui:           uiReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({ serializableCheck: false }),
  devTools: process.env.NODE_ENV !== 'production',
})

export default store
