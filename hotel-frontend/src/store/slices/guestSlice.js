import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { guestApi } from '../../api/guestApi'
import toast from 'react-hot-toast'

export const fetchGuests = createAsyncThunk('guests/fetchAll',
  async (params, { rejectWithValue }) => {
    try { return (await guestApi.getAll(params)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

export const fetchGuestById = createAsyncThunk('guests/fetchById',
  async (id, { rejectWithValue }) => {
    try { return (await guestApi.getById(id)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

export const createGuest = createAsyncThunk('guests/create',
  async (data, { rejectWithValue }) => {
    try { return (await guestApi.create(data)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail || 'Failed to create guest') }
  })

export const updateGuest = createAsyncThunk('guests/update',
  async ({ id, data }, { rejectWithValue }) => {
    try { return (await guestApi.update(id, data)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

export const deleteGuest = createAsyncThunk('guests/delete',
  async (id, { rejectWithValue }) => {
    try { await guestApi.delete(id); return id }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

const guestSlice = createSlice({
  name: 'guests',
  initialState: {
    guests:        { content: [], totalElements: 0, totalPages: 0, page: 0 },
    selectedGuest: null,
    loading:       false,
    error:         null,
  },
  reducers: {
    setSelectedGuest: (s, { payload }) => { s.selectedGuest = payload },
  },
  extraReducers: (b) => {
    b
      .addCase(fetchGuests.pending,   (s) => { s.loading = true })
      .addCase(fetchGuests.fulfilled, (s, { payload }) => { s.loading = false; s.guests = payload })
      .addCase(fetchGuests.rejected,  (s, { payload }) => { s.loading = false; s.error = payload })
      .addCase(fetchGuestById.fulfilled, (s, { payload }) => { s.selectedGuest = payload })
      .addCase(createGuest.fulfilled, (s, { payload }) => {
        s.guests.content.unshift(payload)
        toast.success('Guest profile created')
      })
      .addCase(updateGuest.fulfilled, (s, { payload }) => {
        const idx = s.guests.content.findIndex(g => g.id === payload.id)
        if (idx !== -1) s.guests.content[idx] = payload
        toast.success('Guest profile updated')
      })
      .addCase(deleteGuest.fulfilled, (s, { payload: id }) => {
        s.guests.content = s.guests.content.filter(g => g.id !== id)
      })
  },
})

export const { setSelectedGuest } = guestSlice.actions
export default guestSlice.reducer

export const selectGuests        = (s) => s.guests.guests
export const selectSelectedGuest = (s) => s.guests.selectedGuest
export const selectGuestsLoading = (s) => s.guests.loading
