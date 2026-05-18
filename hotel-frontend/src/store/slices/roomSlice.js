import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { roomApi } from '../../api/roomApi'
import toast from 'react-hot-toast'

export const fetchRooms = createAsyncThunk('rooms/fetchAll',
  async (params, { rejectWithValue }) => {
    try { return (await roomApi.getAll(params)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail || 'Failed to load rooms') }
  })

export const fetchRoomTypes = createAsyncThunk('rooms/fetchTypes',
  async (params, { rejectWithValue }) => {
    try { return (await roomApi.getRoomTypes(params)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

export const fetchRoomAvailability = createAsyncThunk('rooms/availability',
  async (params, { rejectWithValue }) => {
    try { return (await roomApi.checkAvailability(params)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

export const createRoom = createAsyncThunk('rooms/create',
  async (data, { rejectWithValue }) => {
    try { return (await roomApi.create(data)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail || 'Failed to create room') }
  })

export const updateRoomStatus = createAsyncThunk('rooms/updateStatus',
  async ({ id, status, notes }, { rejectWithValue }) => {
    try { return (await roomApi.updateStatus(id, { status, notes })).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail || 'Failed to update status') }
  })

export const deleteRoom = createAsyncThunk('rooms/delete',
  async (id, { rejectWithValue }) => {
    try { await roomApi.delete(id); return id }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

const roomSlice = createSlice({
  name: 'rooms',
  initialState: {
    rooms:        { content: [], totalElements: 0, totalPages: 0, page: 0 },
    roomTypes:    { content: [], totalElements: 0 },
    availability: [],
    selectedRoom: null,
    loading:      false,
    error:        null,
  },
  reducers: {
    setSelectedRoom: (s, { payload }) => { s.selectedRoom = payload },
    // Optimistic update for room status changes — applied immediately before server confirms
    optimisticStatusUpdate: (s, { payload: { id, status } }) => {
      const idx = s.rooms.content.findIndex(r => r.id === id)
      if (idx !== -1) s.rooms.content[idx].status = status
    },
  },
  extraReducers: (b) => {
    b
      .addCase(fetchRooms.pending,   (s) => { s.loading = true })
      .addCase(fetchRooms.fulfilled, (s, { payload }) => { s.loading = false; s.rooms = payload })
      .addCase(fetchRooms.rejected,  (s, { payload }) => { s.loading = false; s.error = payload })

      .addCase(fetchRoomTypes.fulfilled, (s, { payload }) => { s.roomTypes = payload })
      .addCase(fetchRoomAvailability.fulfilled, (s, { payload }) => { s.availability = payload })

      .addCase(updateRoomStatus.fulfilled, (s, { payload }) => {
        const idx = s.rooms.content.findIndex(r => r.id === payload.id)
        if (idx !== -1) s.rooms.content[idx] = payload
        toast.success(`Room ${payload.roomNumber} marked as ${payload.status}`)
      })
      .addCase(updateRoomStatus.rejected, (s, { payload, meta }) => {
        // Revert optimistic update on failure
        toast.error(payload || 'Status update failed')
      })

      .addCase(createRoom.fulfilled, (s, { payload }) => {
        s.rooms.content.unshift(payload)
        toast.success('Room created successfully')
      })
      .addCase(deleteRoom.fulfilled, (s, { payload: id }) => {
        s.rooms.content = s.rooms.content.filter(r => r.id !== id)
        toast.success('Room deleted')
      })
  },
})

export const { setSelectedRoom, optimisticStatusUpdate } = roomSlice.actions
export default roomSlice.reducer

export const selectRooms          = (s) => s.rooms.rooms
export const selectRoomTypes      = (s) => s.rooms.roomTypes
export const selectRoomAvailability = (s) => s.rooms.availability
export const selectRoomsLoading   = (s) => s.rooms.loading
export const selectSelectedRoom   = (s) => s.rooms.selectedRoom
