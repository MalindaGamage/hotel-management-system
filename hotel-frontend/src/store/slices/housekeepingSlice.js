import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { housekeepingApi } from '../../api/housekeepingApi'
import toast from 'react-hot-toast'

export const fetchTasks = createAsyncThunk('housekeeping/fetchTasks',
  async (params, { rejectWithValue }) => {
    try { return (await housekeepingApi.getTasks(params)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

export const updateTaskStatus = createAsyncThunk('housekeeping/updateTask',
  async ({ id, data }, { rejectWithValue }) => {
    try { return (await housekeepingApi.updateTask(id, data)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

export const createTask = createAsyncThunk('housekeeping/createTask',
  async (data, { rejectWithValue }) => {
    try { return (await housekeepingApi.createTask(data)).data }
    catch (e) { return rejectWithValue(e.response?.data?.detail) }
  })

const housekeepingSlice = createSlice({
  name: 'housekeeping',
  initialState: {
    tasks:   { content: [], totalElements: 0, totalPages: 0 },
    loading: false,
    error:   null,
    filter:  { status: null, priority: null, date: null },
  },
  reducers: {
    setFilter: (s, { payload }) => { s.filter = { ...s.filter, ...payload } },
  },
  extraReducers: (b) => {
    b
      .addCase(fetchTasks.pending,   (s) => { s.loading = true })
      .addCase(fetchTasks.fulfilled, (s, { payload }) => { s.loading = false; s.tasks = payload })
      .addCase(fetchTasks.rejected,  (s, { payload }) => { s.loading = false; s.error = payload })
      .addCase(updateTaskStatus.fulfilled, (s, { payload }) => {
        const idx = s.tasks.content.findIndex(t => t.id === payload.id)
        if (idx !== -1) s.tasks.content[idx] = payload
        toast.success('Task updated')
      })
      .addCase(createTask.fulfilled, (s, { payload }) => {
        s.tasks.content.unshift(payload)
        toast.success('Housekeeping task created')
      })
  },
})

export const { setFilter } = housekeepingSlice.actions
export default housekeepingSlice.reducer

export const selectTasks              = (s) => s.housekeeping.tasks
export const selectHousekeepingLoading = (s) => s.housekeeping.loading
export const selectHousekeepingFilter  = (s) => s.housekeeping.filter
